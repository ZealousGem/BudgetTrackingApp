package com.example.budgetboet.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.TimePicker
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.budgetboet.R
import com.google.android.material.navigation.NavigationView

class ExpenseEntryActivity : AppCompatActivity() {

    private lateinit var toggle : ActionBarDrawerToggle
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_entry)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val nameInput = findViewById<EditText>(R.id.txtEntryName)
        val amountInput = findViewById<EditText>(R.id.txtAmount)
        val categoryDropdown = findViewById<Spinner>(R.id.categoryDropDown)
        val dateDropdown = findViewById<DatePicker>(R.id.datePicker)
        val startTimeDropdown = findViewById<TimePicker>(R.id.startTimePicker)
        val endTimeDropdown = findViewById<TimePicker>(R.id.endTimePicker)
        val saveButton = findViewById<Button>(R.id.btnSaveEntry)
        val drawerLayout : DrawerLayout = findViewById(R.id.expense_entry)
        val navView : NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this,drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        saveButton.setOnClickListener {
            val name = nameInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull() ?: 0.0
            val category = categoryDropdown.selectedItem.toString()

//            val date = dateDropdown.selectedItem.toString()
//            val startTime = startTimeDropdown.selectedItem.toString()
//            val endTime = endTimeDropdown.selectedItem.toString()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}
