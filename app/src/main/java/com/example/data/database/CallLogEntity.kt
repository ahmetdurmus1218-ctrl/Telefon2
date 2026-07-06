package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: Long? = null,
    val number: String,
    val name: String = "",
    val callType: String, // "Gelen" (Incoming), "Giden" (Outgoing), "Cevapsız" (Missed)
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val category: String = "Mobil"
)
