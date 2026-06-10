package com.example.ui

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.KnzRepository
import com.example.data.Lead
import com.example.data.Routine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.util.PreferencesHelper

class KnzViewModel(private val repository: KnzRepository, private val context: Context, val prefs: PreferencesHelper) : ViewModel() {

    val themeIndex = MutableStateFlow(3)
    fun setThemeIndex(index: Int) { themeIndex.value = index }

    val leads: StateFlow<List<Lead>> = repository.allLeads.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val searchQuery = MutableStateFlow("")
    val statusFilter = MutableStateFlow("All")

    val filteredLeads: StateFlow<List<Lead>> = combine(leads, searchQuery, statusFilter) { list, query, status ->
        list.filter { lead ->
            (status == "All" || lead.status == status) &&
            (lead.businessName.contains(query, ignoreCase = true) || lead.location.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routines: StateFlow<List<Routine>> = repository.allRoutines.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val selectedLead = MutableStateFlow<Lead?>(null)

    val activeTimers = MutableStateFlow<Map<Int, Long>>(emptyMap())
    private val activeTimerEndTimes = mutableMapOf<Int, Long>()
    private val timerJobs = mutableMapOf<Int, Job>()
    private var mediaPlayer: MediaPlayer? = null

    init {
        restoreTimers()
    }

    private fun saveTimers() {
        val str = activeTimerEndTimes.entries.joinToString(",") { "${it.key}:${it.value}" }
        prefs.activeTimersJson = str
    }

    private fun restoreTimers() {
        val str = prefs.activeTimersJson
        if (str.isNotBlank() && str != "{}") {
            str.split(",").forEach {
                val parts = it.split(":")
                if (parts.size == 2) {
                    val id = parts[0].toIntOrNull()
                    val endTime = parts[1].toLongOrNull()
                    if (id != null && endTime != null) {
                        val remaining = (endTime - System.currentTimeMillis()) / 1000L
                        if (remaining > 0) {
                            activeTimerEndTimes[id] = endTime
                            // We don't have the routine object here easily, but we can just use the UI's routine list.
                            // But wait, the startTimer logic requires `routine`, we can just launch a generic countdown scope
                            // Or better yet, we just start counting down generic timers here
                            launchRestoredTimer(id, remaining)
                        }
                    }
                }
            }
        }
    }

    private fun launchRestoredTimer(id: Int, duration: Long) {
        val job = viewModelScope.launch {
            var remaining = duration
            while (remaining > 0) {
                delay(1000)
                remaining--
                activeTimers.value = activeTimers.value.toMutableMap().apply { put(id, remaining) }
            }
            stopTimer(id, stopService = false)
            activeTimerEndTimes.remove(id)
            saveTimers()
        }
        timerJobs[id] = job
    }

    fun addLead(businessName: String, location: String, whatsappNumber: String, description: String) {
        viewModelScope.launch {
            repository.insertLead(
                Lead(
                    businessName = businessName,
                    location = location,
                    whatsappNumber = whatsappNumber,
                    description = description
                )
            )
        }
    }

    fun updateLeadStatus(lead: Lead, newStatus: String) {
        viewModelScope.launch {
            repository.updateLead(lead.copy(status = newStatus))
        }
    }

    fun deleteLead(lead: Lead) {
        viewModelScope.launch {
            repository.deleteLead(lead)
        }
    }

    fun addRoutine(name: String, category: String, durationSeconds: Long, audioUri: String?) {
        viewModelScope.launch {
            repository.insertRoutine(Routine(name = name, category = category, durationSeconds = durationSeconds, audioUri = audioUri))
        }
    }

    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch {
            stopTimer(routine.id)
            repository.deleteRoutine(routine)
        }
    }

    fun toggleTimer(routine: Routine) {
        if (timerJobs.containsKey(routine.id)) {
            stopTimer(routine.id)
        } else {
            startTimer(routine)
        }
    }

    private fun startTimer(routine: Routine) {
        val currentRemaining = activeTimers.value[routine.id] ?: routine.durationSeconds
        if (currentRemaining <= 0) return

        activeTimerEndTimes[routine.id] = System.currentTimeMillis() + (currentRemaining * 1000L)
        saveTimers()

        val intent = android.content.Intent(context, com.example.service.TimerService::class.java).apply {
            action = com.example.service.TimerService.ACTION_START_TIMER
            putExtra(com.example.service.TimerService.EXTRA_ROUTINE_ID, routine.id)
            putExtra(com.example.service.TimerService.EXTRA_DURATION, currentRemaining)
            putExtra(com.example.service.TimerService.EXTRA_NAME, routine.name)
            putExtra(com.example.service.TimerService.EXTRA_AUDIO_URI, routine.audioUri)
        }
        
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val job = viewModelScope.launch {
            var remaining = currentRemaining
            while (remaining > 0) {
                delay(1000)
                remaining--
                activeTimers.value = activeTimers.value.toMutableMap().apply { put(routine.id, remaining) }
            }
            // Timer Finished - service will handle ringtone
            stopTimer(routine.id, stopService = false)
        }
        timerJobs[routine.id] = job
    }

    fun stopTimer(routineId: Int, stopService: Boolean = true) {
        timerJobs[routineId]?.cancel()
        timerJobs.remove(routineId)
        activeTimerEndTimes.remove(routineId)
        saveTimers()
        
        if (stopService) {
            val intent = android.content.Intent(context, com.example.service.TimerService::class.java).apply {
                action = com.example.service.TimerService.ACTION_STOP_TIMER
                putExtra(com.example.service.TimerService.EXTRA_ROUTINE_ID, routineId)
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetTimer(routine: Routine) {
        stopTimer(routine.id)
        activeTimers.value = activeTimers.value.toMutableMap().apply { put(routine.id, routine.durationSeconds) }
    }

    private fun playRingtone(uriString: String?) {
        try {
            mediaPlayer?.release()
            mediaPlayer = if (uriString != null) {
                MediaPlayer.create(context, Uri.parse(uriString))
            } else {
                // Ignore if null, could fallback to default ringtone
                null
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun stopRingtone() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }
}

class KnzViewModelFactory(private val repository: KnzRepository, private val context: Context, private val prefs: PreferencesHelper) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KnzViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KnzViewModel(repository, context, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
