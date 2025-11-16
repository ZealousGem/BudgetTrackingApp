package com.example.budgetboet

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot // NEW IMPORT
import com.google.firebase.database.DatabaseError // NEW IMPORT
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener // NEW IMPORT
import java.util.Locale

class GoalAdapter(
    private val goals: MutableList<Goal>,
    private val dbRef: DatabaseReference,
    private val userPointsRef: DatabaseReference // This reference is for updating user points
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val goalName: TextView = itemView.findViewById(R.id.goalName)
        val goalProgressText: TextView = itemView.findViewById(R.id.goalProgressText)
        val goalProgressBar: ProgressBar = itemView.findViewById(R.id.goalProgressBar)
        val deleteButton: Button = itemView.findViewById(R.id.deleteGoalButton)
        val updateButton: Button = itemView.findViewById(R.id.button3)
        val amountEditText: EditText = itemView.findViewById(R.id.editTextText)
        val cardLayout: View = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]

        // Set basic properties
        holder.goalName.text = goal.name
        holder.goalProgressBar.max = goal.targetAmount
        holder.goalProgressBar.progress = goal.savedAmount

        // Display progress text
        val progressText = String.format(Locale.US, "\$%d / \$%d", goal.savedAmount, goal.targetAmount)
        holder.goalProgressText.text = progressText

        // Handle Goal Completion Visuals
        val isGoalComplete = goal.savedAmount >= goal.targetAmount
        if (isGoalComplete) {
            holder.cardLayout.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_light))
            holder.goalProgressText.setTextColor(Color.BLACK)
            holder.goalName.setTextColor(Color.BLACK)
            holder.updateButton.isEnabled = false // Disable update if goal is met
            holder.amountEditText.isEnabled = false
            holder.amountEditText.setText("Goal Reached!")
        } else {
            // Reset colors if goal is not complete
            holder.cardLayout.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
            holder.goalProgressText.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray))
            holder.goalName.setTextColor(Color.BLACK)
            holder.updateButton.isEnabled = true
            holder.amountEditText.isEnabled = true
            holder.amountEditText.text.clear()
            holder.amountEditText.hint = "Add Amount Saved"
        }

        // Delete Button
        holder.deleteButton.setOnClickListener {
            dbRef.child(goal.id).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "Goal deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(holder.itemView.context, "Failed to delete goal", Toast.LENGTH_SHORT).show()
                }
        }

        // Update Button
        holder.updateButton.setOnClickListener {
            val amountText = holder.amountEditText.text.toString().trim()
            if (amountText.isNotEmpty()) {
                val amount = amountText.toIntOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(holder.itemView.context, "Enter a valid positive amount", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val wasIncomplete = goal.savedAmount < goal.targetAmount

                val newSavedAmount = (goal.savedAmount + amount).coerceAtMost(goal.targetAmount)

                val isNowComplete = newSavedAmount == goal.targetAmount

                // Flag to check if this specific update completed the goal
                val isGoalNewlyCompleted = wasIncomplete && isNowComplete

                val update = mapOf<String, Any>(
                    "savedAmount" to newSavedAmount
                )

                dbRef.child(goal.id).updateChildren(update)
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Goal progress updated", Toast.LENGTH_SHORT).show()

                        if (isGoalNewlyCompleted) {
                            addRewardPoints(holder, goal.name)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(holder.itemView.context, "Update failed", Toast.LENGTH_SHORT).show()
                    }

                // Clear the input field
                holder.amountEditText.text.clear()
            } else {
                Toast.makeText(holder.itemView.context, "Please enter an amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = goals.size

    private fun addRewardPoints(holder: GoalViewHolder, goalName: String) {
        val rewardAmount = 500 // Points to award for completing a goal

        // Use addListenerForSingleValueEvent to read the points only once
        userPointsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Read current points, defaulting to 0 if the value does not exist
                val currentPoints = snapshot.getValue(Int::class.java) ?: 0
                val newPoints = currentPoints + rewardAmount

                // Write the new total back to Firebase
                userPointsRef.setValue(newPoints)
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Goal '$goalName' completed! You earned $rewardAmount points!", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(holder.itemView.context, "Failed to update points: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Toast.makeText(holder.itemView.context, "Database error when getting points: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
