package com.example.budgetboet.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetboet.R

class ExpenseEntryActivity : AppCompatActivity() {

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_entry)

        val nameInput = findViewById<EditText>(R.id.txtEntryName)
        val amountInput = findViewById<EditText>(R.id.txtAmount)
        val categoryDropdown = findViewById<Spinner>(R.id.categoryDropDown)
        val dateDropdown = findViewById<Spinner>(R.id.datePicker)
        val startTimeDropdown = findViewById<Spinner>(R.id.startTimePicker)
        val endTimeDropdown = findViewById<Spinner>(R.id.endTimePicker)
        val saveButton = findViewById<Button>(R.id.btnSaveEntry)

        saveButton.setOnClickListener {
            val name = nameInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull() ?: 0.0
            val category = categoryDropdown.selectedItem.toString()
            val date = dateDropdown.selectedItem.toString()
            val startTime = startTimeDropdown.selectedItem.toString()
            val endTime = endTimeDropdown.selectedItem.toString()
        }
    }
}
