package com.example.budgetboet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class GoalAdapter(
    private val goals: MutableList<Goal>,
    private val dbRef: DatabaseReference
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val goalName: TextView = itemView.findViewById(R.id.goalName)
        val goalProgressBar: ProgressBar = itemView.findViewById(R.id.goalProgressBar)
        val deleteButton: Button = itemView.findViewById(R.id.deleteGoalButton)
        val updateButton: Button = itemView.findViewById(R.id.button3)
        val amountEditText: EditText = itemView.findViewById(R.id.editTextText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        holder.goalName.text = goal.name
        holder.goalProgressBar.max = goal.targetAmount
        holder.goalProgressBar.progress = goal.savedAmount

        holder.deleteButton.setOnClickListener {
            dbRef.child(goal.id).removeValue()
        }

        holder.updateButton.setOnClickListener {
            val amountText = holder.amountEditText.text.toString()
            if (amountText.isNotEmpty()) {
                val amount = amountText.toInt()
                
                // Perform an "optimistic" UI update for immediate feedback
                val newSavedAmount = goal.savedAmount + amount
                holder.goalProgressBar.progress = newSavedAmount


                val update = mapOf<String, Any>("${goal.id}/savedAmount" to newSavedAmount)

                dbRef.updateChildren(update).addOnSuccessListener{
                    Toast.makeText(holder.itemView.context, "successs", Toast.LENGTH_SHORT).show()
                }


                // Clear the input field
                holder.amountEditText.text.clear()
            } else {
                Toast.makeText(holder.itemView.context, "Please enter an amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = goals.size
}
