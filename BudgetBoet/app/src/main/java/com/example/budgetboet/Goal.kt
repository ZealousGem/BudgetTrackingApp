package com.example.budgetboet

// Add an empty constructor and default values for Firebase deserialization
data class Goal(
    var id: String = "", // Unique ID for each goal
    val name: String = "",
    val targetAmount: Int = 0,
    var savedAmount: Int = 0
)
