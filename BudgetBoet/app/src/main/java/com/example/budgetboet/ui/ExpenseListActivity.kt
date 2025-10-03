package com.example.budgetboet.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetboet.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.example.budgetboet.utils.UserUtils

class ExpenseListActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var toggle : ActionBarDrawerToggle
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)

        val drawerLayout : DrawerLayout = findViewById(R.id.main)
        val navView : NavigationView = findViewById(R.id.nav_view)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        auth = FirebaseAuth.getInstance()
        toggle = ActionBarDrawerToggle(this,drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val user = auth.currentUser
        if(user != null){
            // ... login redirect ...
            UserUtils.loadUserNameAndEmail(user.uid, navView)
        }
        // this code was causing it to crash
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
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
