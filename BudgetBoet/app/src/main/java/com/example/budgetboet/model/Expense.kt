package com.example.budgetboet.model

import android.media.Image

// Represents a single expense entry
data class Expense(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val image: String = ""
)
