package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.KnzRepository
import com.example.data.Lead
import com.example.data.Routine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KnzViewModel(private val repository: KnzRepository) : ViewModel() {

    val leads: StateFlow<List<Lead>> = repository.allLeads.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val routines: StateFlow<List<Routine>> = repository.allRoutines.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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

    fun addRoutine(name: String, audioNode: String) {
        viewModelScope.launch {
            repository.insertRoutine(Routine(name = name, audioNode = audioNode))
        }
    }

    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.deleteRoutine(routine)
        }
    }
}

class KnzViewModelFactory(private val repository: KnzRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KnzViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KnzViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
