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

class KnzViewModel(private val repository: KnzRepository, private val context: Context) : ViewModel() {

    val themeIndex = MutableStateFlow(0)
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
    private val timerJobs = mutableMapOf<Int, Job>()
    private var mediaPlayer: MediaPlayer? = null

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

        val job = viewModelScope.launch {
            var remaining = currentRemaining
            while (remaining > 0) {
                delay(1000)
                remaining--
                activeTimers.value = activeTimers.value.toMutableMap().apply { put(routine.id, remaining) }
            }
            // Timer Finished
            playRingtone(routine.audioUri)
            stopTimer(routine.id)
        }
        timerJobs[routine.id] = job
    }

    fun stopTimer(routineId: Int) {
        timerJobs[routineId]?.cancel()
        timerJobs.remove(routineId)
        // Reset or keep paused state. We'll just keep the current paused state.
        // If finished, it's already 0.
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

class KnzViewModelFactory(private val repository: KnzRepository, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KnzViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KnzViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
