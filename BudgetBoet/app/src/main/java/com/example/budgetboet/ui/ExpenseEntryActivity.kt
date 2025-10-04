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
import com.example.budgetboet.CategorySpent
import com.example.budgetboet.Goals
import com.example.budgetboet.HomeScreen
import com.example.budgetboet.Login
import com.example.budgetboet.NewCategory
import com.example.budgetboet.R
import com.example.budgetboet.utils.UserUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.example.budgetboet.model.Expense
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.Toast


class ExpenseEntryActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var toggle : ActionBarDrawerToggle

    private lateinit var database: DatabaseReference


    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_entry)
        database = FirebaseDatabase.getInstance().reference

        /// navigation stuff
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

                R.id.nav_expense_view ->{ val intent = Intent(applicationContext,
                    ExpenseListActivity ::class.java)
                    startActivity(intent)}

                R.id.nav_category ->{ val intent = Intent(applicationContext, NewCategory ::class.java)
                    startActivity(intent)}

                R.id.nav_category_view ->{ val intent = Intent(applicationContext, CategorySpent ::class.java)
                    startActivity(intent)}

                R.id.nav_goals ->{ val intent = Intent(applicationContext, Goals ::class.java)
                    startActivity(intent)}

                R.id.nav_logout ->{
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(applicationContext, Login::class.java)
                    startActivity(intent)
                    finish()}


            }

            drawerLayout.closeDrawer(navView)
            true
        } ///  end of navigation stuff

        saveButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val amount = amountInput.text.toString().toDoubleOrNull() ?: 0.0
            val category = categoryDropdown.selectedItem.toString()

            if (name.isEmpty() || amount <= 0.0) {
                Toast.makeText(this, "Please enter a valid name and amount.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val day = dateDropdown.dayOfMonth
            val month = dateDropdown.month
            val year = dateDropdown.year
            // Format the date as a string (e.g., "DD/MM/YYYY")
            val calendar = Calendar.getInstance().apply { set(year, month, day) }
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = dateFormat.format(calendar.time)

            val startHour = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) startTimeDropdown.hour else startTimeDropdown.currentHour
            val startMinute = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) startTimeDropdown.minute else startTimeDropdown.currentMinute
            val startTime = String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute)

            val endHour = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) endTimeDropdown.hour else endTimeDropdown.currentHour
            val endMinute = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) endTimeDropdown.minute else endTimeDropdown.currentMinute
            val endTime = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute)

            val userId = auth.currentUser?.uid

            if (userId != null) {
                // Path: /expenses/{userId}/{unique_expense_id}
                val expensesRef = database.child("expenses").child(userId)
                val expenseId = expensesRef.push().key ?: return@setOnClickListener

                val expense = Expense(
                    id = expenseId, // Save the key as the ID in the object
                    name = name,
                    amount = amount,
                    category = category,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    image = 0
                )

                expensesRef.child(expenseId).setValue(expense)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_LONG).show()
                        finish() // Closes the entry screen and returns to the list
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save expense: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "User not authenticated. Cannot save expense.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean { ///  navigation stuff, do not touch or i will kill you

        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}
