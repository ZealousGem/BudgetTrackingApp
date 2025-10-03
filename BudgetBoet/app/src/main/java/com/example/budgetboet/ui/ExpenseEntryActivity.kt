package com.example.budgetboet.ui

import android.annotation.SuppressLint
import android.content.Intent
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
import com.example.budgetboet.Goals
import com.example.budgetboet.HomeScreen
import com.example.budgetboet.R
import com.example.budgetboet.utils.UserUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class ExpenseEntryActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
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

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if(user != null){
            // ... login redirect ...
            UserUtils.loadUserNameAndEmail(user.uid, navView)
        }

        navView.setNavigationItemSelectedListener {

            when(it.itemId)
            {
              R.id.nav_home ->{ val intent = Intent(applicationContext, HomeScreen ::class.java)
                  startActivity(intent)}

                R.id.nav_expense ->{ val intent = Intent(applicationContext, ExpenseEntryActivity ::class.java)
                    startActivity(intent)}





            }
            true
        }
        saveButton.setOnClickListener {
            val name = nameInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull() ?: 0.0
            val category = categoryDropdown.selectedItem.toString()

            // this code was causing it to crash
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
