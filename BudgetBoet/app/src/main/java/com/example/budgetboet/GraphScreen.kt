package com.example.budgetboet

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.budgetboet.ui.ExpenseEntryActivity
import com.example.budgetboet.ui.ExpenseListActivity
import com.example.budgetboet.databinding.ActivityGraphScreenBinding
import com.example.budgetboet.model.Category
import com.example.budgetboet.model.Expense
import com.example.budgetboet.utils.UserUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class GraphScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var database: DatabaseReference // Added
    private val categoryDetails = mutableMapOf<String, String>() // Map<ID, Name> - Added
    private var _binding: ActivityGraphScreenBinding? = null
    private val binding get() = _binding!!

    // Static data moved inside companion object for reference, but we will use dynamic data

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityGraphScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference // Initialize database reference

        // ... [Navigation Drawer Setup remains the same] ...
        val drawerLayout : DrawerLayout = findViewById(R.id.main)
        val navView : NavigationView = findViewById(R.id.nav_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)

        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val user = auth.currentUser
        if(user != null){
            UserUtils.loadUserNameAndEmail(user.uid, navView)
        }

        navView.setNavigationItemSelectedListener {
            when(it.itemId)
            {
                R.id.nav_home ->{
                    startActivity(Intent(applicationContext, HomeScreen ::class.java))
                }
                R.id.nav_expense ->{
                    startActivity(Intent(applicationContext, ExpenseEntryActivity ::class.java))
                }
                R.id.nav_expense_view ->{
                    startActivity(Intent(applicationContext, ExpenseListActivity ::class.java))
                }
                R.id.nav_category ->{
                    startActivity(Intent(applicationContext, NewCategory ::class.java))
                }
                R.id.nav_category_view ->{
                    startActivity(Intent(applicationContext, CategorySpent ::class.java))
                }
                R.id.nav_goals ->{
                    startActivity(Intent(applicationContext, Goals ::class.java))
                }
                R.id.nav_logout ->{
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(applicationContext, Login::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(navView)
            true
        }
        // ... [End Navigation Drawer Setup] ...

        // Call the data loading function
        loadAndDisplayCategoryExpenses()
    }

    // New function to load category expenses
    private fun loadAndDisplayCategoryExpenses() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val categoriesRef = database.child("categories").child(userId)
        val expensesRef = database.child("expenses").child(userId)

        // 1. Fetch all category names
        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(categoriesSnapshot: DataSnapshot) {
                categoryDetails.clear()
                val categoryTotals = mutableMapOf<String, Double>()
                for (catSnapshot in categoriesSnapshot.children) {
                    val category = catSnapshot.getValue(Category::class.java)
                    val categoryId = catSnapshot.key
                    if (category != null && categoryId != null) {
                        categoryDetails[categoryId] = category.name
                        categoryTotals[categoryId] = 0.0 // Initialize total
                    }
                }

                // 2. Fetch expenses and aggregate totals
                expensesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(expensesSnapshot: DataSnapshot) {
                        for (expSnapshot in expensesSnapshot.children) {
                            val expense = expSnapshot.getValue(Expense::class.java)

                            if (expense != null && categoryTotals.containsKey(expense.category)) {
                                val currentTotal = categoryTotals[expense.category] ?: 0.0
                                // Safely convert amount, assuming the value is stored as a String
                                val expenseAmount = expense.amount.toDoubleOrNull() ?: 0.0
                                categoryTotals[expense.category] = currentTotal + expenseAmount
                            }
                        }

                        // 3. Convert data for the graph and update the UI
                        updateGraph(categoryTotals)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@GraphScreen, "Failed to load expenses: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GraphScreen, "Failed to load categories: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // New function to transform data and display the graph
    private fun updateGraph(categoryTotals: Map<String, Double>) {
        val graphData = categoryTotals
            .mapNotNull { (categoryId, total) ->
                val categoryName = categoryDetails[categoryId]
                // Only include categories with spent amount > 0, and convert Double to Float
                if (categoryName != null && total > 0) {

                    val shorterName = categoryName.split(' ').take(3).joinToString (" ")

                    shorterName to total.toFloat()



                } else {
                    null
                }
            }
            // Sort by total spent for better visualization (optional)
            .sortedByDescending { it.second }


        if (graphData.isEmpty()) {
            Toast.makeText(this, "No expenses found to display in the graph.", Toast.LENGTH_LONG).show()
            // Optional: Clear the graph or display a placeholder message
            return
        }

        // The bar graph expects a List<Pair<String, Float>>
        binding.apply {
            // Note: The variable name `barSet` from the companion object is now being overwritten locally
            barGraph.animation.duration = animationDuration
            // Animate the graph with the new, real data
            barGraph.animate(graphData)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // The companion object can be simplified or kept for the animation duration constant
    companion object {
        private const val animationDuration = 1000L
    }
}