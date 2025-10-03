// In a new file named Goal.kt
package com.example.budgetboet

data class Goal(
    val name: String,
    val targetAmount: Int,
    var currentAmount: Int
) {
    val savedAmount: Int
        get() {
            TODO()
        }
}
    