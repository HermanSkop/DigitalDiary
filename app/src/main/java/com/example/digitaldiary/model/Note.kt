package com.example.digitaldiary.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity
data class Note(
    @PrimaryKey val id: Int,
    val title: String,
    val content: String,
    val date: LocalDate
)