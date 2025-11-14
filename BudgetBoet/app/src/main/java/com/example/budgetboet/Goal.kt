package com.example.budgetboet

data class Goal(
    var id: String = "", // Unique ID for each goal
    val name: String = "",
    val targetAmount: Int = 0,
    var savedAmount: Int = 0,
    val rewardPoints: Int = 100, // Default points awarded for completing this goal
    val isCompleted: Boolean = false // Track completion status to prevent re-awarding
)