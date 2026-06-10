package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leads")
data class Lead(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val businessName: String,
    val location: String,
    val whatsappNumber: String,
    val description: String,
    val status: String = "Scouted", // Scouted, Waiting, Hired, Rejected
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val audioNode: String, // E.g., "Alert 1", "Bell", "Synth"
    val timestamp: Long = System.currentTimeMillis()
)
