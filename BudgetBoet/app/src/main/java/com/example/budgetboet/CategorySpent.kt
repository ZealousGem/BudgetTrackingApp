package com.example.budgetboet

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.budgetboet.ui.ExpenseEntryActivity
import com.example.budgetboet.ui.ExpenseListActivity
import com.example.budgetboet.utils.UserUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.DecimalFormat

// Data classes to represent your Firebase data structure
data class Category(val name: String = "", val userId: String = "", val type: String = "", val description: String = "")
data class Expense(val userId: String = "", val categoryId: String = "", val amount: Double = 0.0)

class CategorySpent : AppCompatActivity() {

    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var tableLayout: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.category_spent)

        val drawerLayout : DrawerLayout = findViewById(R.id.main)
        val navView : NavigationView = findViewById(R.id.nav_view)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

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
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        tableLayout = findViewById(R.id.Table)

        loadAndDisplayCategoryExpenses()
    }

    private fun loadAndDisplayCategoryExpenses() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            // Handle not logged in user
            return
        }

        val categoriesRef = database.child("categories").orderByChild("userId").equalTo(userId)
        val expensesRef = database.child("expenses").orderByChild("userId").equalTo(userId)

        val categoryDetails = mutableMapOf<String, Category>()
        val categoryTotals = mutableMapOf<String, Double>()

        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(categoriesSnapshot: DataSnapshot) {
                for (catSnapshot in categoriesSnapshot.children) {
                    val category = catSnapshot.getValue(Category::class.java)
                    val categoryId = catSnapshot.key
                    if (category != null && categoryId != null) {
                        categoryDetails[categoryId] = category
                        categoryTotals[categoryId] = 0.0 // Initialize all category totals to 0
                    }
                }

                expensesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(expensesSnapshot: DataSnapshot) {
                        for (expSnapshot in expensesSnapshot.children) {
                            val expense = expSnapshot.getValue(Expense::class.java)
                            if (expense != null && categoryTotals.containsKey(expense.categoryId)) {
                                val currentTotal = categoryTotals[expense.categoryId] ?: 0.0
                                categoryTotals[expense.categoryId] = currentTotal + expense.amount
                            }
                        }
                        populateTable(categoryDetails, categoryTotals)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error reading expenses
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error reading categories
            }
        })
    }

    private fun populateTable(categoryDetails: Map<String, Category>, categoryTotals: Map<String, Double>) {
        tableLayout.removeAllViews() // Clear existing rows

        // Header Row
        val headerRow = TableRow(this)
        val categoryHeader = TextView(this).apply {
            text = "Category"
            setTextAppearance(android.R.style.TextAppearance_Medium)
            setPadding(16, 16, 16, 16)
        }
        val totalHeader = TextView(this).apply {
            text = "Total Spent"
            setTextAppearance(android.R.style.TextAppearance_Medium)
            setPadding(16, 16, 16, 16)
        }
        headerRow.addView(categoryHeader)
        headerRow.addView(totalHeader)
        tableLayout.addView(headerRow)

        val decimalFormat = DecimalFormat("#,##0.00")

        for ((categoryId, category) in categoryDetails) {
            val total = categoryTotals[categoryId] ?: 0.0

            val tableRow = TableRow(this)
            val categoryNameView = TextView(this).apply {
                text = category.name
                setPadding(16, 16, 16, 16)
            }
            val totalView = TextView(this).apply {
                text = "R ${decimalFormat.format(total)}"
                setPadding(16, 16, 16, 16)
            }

            tableRow.addView(categoryNameView)
            tableRow.addView(totalView)
            tableLayout.addView(tableRow)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}