package com.example.budgetboet.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetboet.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ExpenseListActivity : AppCompatActivity() {

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)

//        val nameInput = findViewById<EditText>(R.id.txtEntryName)
//        val amountInput = findViewById<EditText>(R.id.txtAmount)
//        val categoryDropdown = findViewById<Spinner>(R.id.categoryDropDown)
//        val dateDropdown = findViewById<Spinner>(R.id.datePicker)
//        val startTimeDropdown = findViewById<Spinner>(R.id.txtStartTime)
//        val endTimeDropdown = findViewById<Spinner>(R.id.txtEndTime)
//        val saveButton = findViewById<Button>(R.id.btnSaveEntry)
//
//        saveButton.setOnClickListener {
//            val name = nameInput.text.toString()
//            val amount = amountInput.text.toString().toDoubleOrNull() ?: 0.0
//            val category = categoryDropdown.selectedItem.toString()
//            val date = dateDropdown.selectedItem.toString()
//            val startTime = startTimeDropdown.selectedItem.toString()
//            val endTime = endTimeDropdown.selectedItem.toString()
//        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvExpenses)
        val fab = findViewById<FloatingActionButton>(R.id.fabNewExpense)

        fab.setOnClickListener {
            val intent = Intent(this, ExpenseEntryActivity::class.java)
            startActivity(intent)
        }
    }
}
