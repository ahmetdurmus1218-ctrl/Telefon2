package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val birthday: String = "",
    val group: String = "",
    val other: String = "",
    val isFavorite: Boolean = false,
    val favoriteBadge: String = "", // "Annem", "Babam", "Eşim", "Canım", or empty
    val storageAccount: String = "Cihaz Hafızası" // "Cihaz Hafızası", "Google Hesabı", "SIM Kart"
)
