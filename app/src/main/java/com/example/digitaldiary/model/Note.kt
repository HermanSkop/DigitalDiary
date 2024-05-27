package com.example.digitaldiary.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val date: LocalDate,
    val location: String,
    val imageUri: String?,
    val audioUri: String?
)