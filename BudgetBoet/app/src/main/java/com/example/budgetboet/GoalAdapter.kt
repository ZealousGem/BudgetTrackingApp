
package com.example.budgetboet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GoalAdapter(private val goals: MutableList<Goal>) :
    RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val goalName: TextView = itemView.findViewById(R.id.goalName)
        val goalProgressBar: ProgressBar = itemView.findViewById(R.id.goalProgressBar)
        val deleteButton: Button = itemView.findViewById(R.id.deleteGoalButton)
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
            goals.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, goals.size)
        }
    }

    override fun getItemCount(): Int = goals.size
}

