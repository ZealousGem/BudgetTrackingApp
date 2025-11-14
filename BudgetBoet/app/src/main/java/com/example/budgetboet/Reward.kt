package com.example.budgetboet

data class Reward(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int // Points required to claim
)