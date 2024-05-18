package com.example.digitaldiary.model

import java.time.LocalDate

data class Entry(val id: Int, val title: String, val content: String, val date: LocalDate)