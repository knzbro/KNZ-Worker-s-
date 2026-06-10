package com.example.data

import kotlinx.coroutines.flow.Flow

class KnzRepository(private val knzDao: KnzDao) {
    val allLeads: Flow<List<Lead>> = knzDao.getAllLeads()
    val allRoutines: Flow<List<Routine>> = knzDao.getAllRoutines()

    suspend fun insertLead(lead: Lead) = knzDao.insertLead(lead)
    suspend fun updateLead(lead: Lead) = knzDao.updateLead(lead)
    suspend fun deleteLead(lead: Lead) = knzDao.deleteLead(lead)

    suspend fun insertRoutine(routine: Routine) = knzDao.insertRoutine(routine)
    suspend fun deleteRoutine(routine: Routine) = knzDao.deleteRoutine(routine)
}
