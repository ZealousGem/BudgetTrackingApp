package com.example.budgetboet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetboet.R
import com.example.budgetboet.model.Expense

class ExpenseAdapter(private val expenseList: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.txtItemName)
        val amount: TextView = itemView.findViewById(R.id.txtItemAmount)
        val category: TextView = itemView.findViewById(R.id.txtItemCategory)
        val date: TextView = itemView.findViewById(R.id.txtItemDate)
        val image: ImageView = itemView.findViewById(R.id.imgItemImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.name.text = expense.name
        holder.amount.text = "R${expense.amount}"
        holder.category.text = expense.category
        holder.date.text = "${expense.date}"
        holder.image.setImageResource(expense.image)
    }

    override fun getItemCount() = expenseList.size
}
