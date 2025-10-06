package com.example.budgetboet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.budgetboet.R
import com.example.budgetboet.model.Expense
import java.text.DecimalFormat

class ExpenseAdapter(
    private val expenseList: List<Expense>,
    private val categoryMap: Map<String, String> // Map of Category ID to Category Name
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.bind(expense, categoryMap)
    }

    override fun getItemCount(): Int = expenseList.size

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // --- References to your existing TextViews (these were already correct) ---
        private val nameTextView: TextView = itemView.findViewById(R.id.txtItemName)
        private val amountTextView: TextView = itemView.findViewById(R.id.txtItemAmount)
        private val categoryTextView: TextView = itemView.findViewById(R.id.txtItemCategory)
        private val dateTextView: TextView = itemView.findViewById(R.id.txtItemDate)

        // --- CORRECTED: Reference to the ImageView using the ID from your XML ---
        private val receiptImageView: ImageView = itemView.findViewById(R.id.imgItemImage)

        fun bind(expense: Expense, categoryMap: Map<String, String>) {
            // --- Your existing code to bind text data ---
            nameTextView.text = expense.name
            val decimalFormat = DecimalFormat("#,##0.00")
            amountTextView.text = "R ${decimalFormat.format(expense.amount.toDoubleOrNull() ?: 0.0)}"
            dateTextView.text = expense.date

            val categoryName = categoryMap[expense.category] ?: "Unknown" // Fallback text
            categoryTextView.text = categoryName

            // --- Logic to load the image (this part was already correct) ---
            // Check if the expense has a valid image URL
            if (expense.image.isNotEmpty()) {
                // Use Glide to load the image from the URL into the ImageView
                Glide.with(itemView.context)
                    .load(expense.image) // The download URL from Firebase
                    .placeholder(R.drawable.ic_launcher_background) // Optional: Show a placeholder while loading
                    .error(R.drawable.ic_launcher_background) // Optional: Show an error image if loading fails
                    .into(receiptImageView)
            } else {
                // If no image URL, you can hide the ImageView or set a default icon
                receiptImageView.setImageResource(R.drawable.ic_launcher_background) // A default drawable
            }
        }
    }
}
