package com.example.budgetboet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction

class RewardsAdapter(
    private val rewardsList: List<Reward>,
    private val pointsRef: DatabaseReference
) : RecyclerView.Adapter<RewardsAdapter.RewardViewHolder>() {

    private var userPoints: Int = 0

    // Method called by RewardsActivity to update the current points
    fun updateUserPoints(newPoints: Int) {
        userPoints = newPoints
        notifyDataSetChanged() // Refresh the list to update button states
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reward, parent, false)
        return RewardViewHolder(view)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        val reward = rewardsList[position]
        holder.rewardName.text = reward.name
        holder.rewardDescription.text = reward.description
        holder.claimButton.text = "Claim (${reward.cost} Points)"

        // Check if user has enough points
        val canClaim = userPoints >= reward.cost
        holder.claimButton.isEnabled = canClaim

        // Visually indicate if the button is disabled
        holder.claimButton.alpha = if (canClaim) 1.0f else 0.5f

        holder.claimButton.setOnClickListener {
            if (canClaim) {
                claimReward(reward, holder.itemView.context)
            } else {
                Toast.makeText(holder.itemView.context, "Not enough points to claim this reward.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun claimReward(reward: Reward, context: Context) {
        // Use a transaction to safely deduct points and prevent race conditions
        pointsRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentPoints = mutableData.getValue(Int::class.java) ?: 0

                // Double check if points are sufficient within the transaction
                if (currentPoints < reward.cost) {
                    return Transaction.abort() // Abort if they don't have enough points
                }

                // Deduct points
                mutableData.value = currentPoints - reward.cost
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                databaseError: DatabaseError?,
                committed: Boolean,
                dataSnapshot: com.google.firebase.database.DataSnapshot?
            ) {
                if (committed) {
                    // Successful deduction
                    Toast.makeText(context, "Successfully claimed ${reward.name}!", Toast.LENGTH_LONG).show()
                    // TODO: Add logic here to apply the actual reward benefit (e.g., save a record of the claimed reward)
                } else {
                    // Transaction failed (likely due to not enough points after all)
                    Toast.makeText(context, "Claim failed. Check your points balance.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun getItemCount() = rewardsList.size

    class RewardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rewardName: TextView = itemView.findViewById(R.id.rewardName)
        val rewardDescription: TextView = itemView.findViewById(R.id.rewardDescription)
        val claimButton: Button = itemView.findViewById(R.id.claimButton)
    }
}