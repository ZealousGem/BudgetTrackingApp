package com.example.budgetboet.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetboet.CategorySpent
import com.example.budgetboet.Goals
import com.example.budgetboet.HomeScreen
import com.example.budgetboet.Login
import com.example.budgetboet.NewCategory
import com.example.budgetboet.R
import com.example.budgetboet.RewardsActivity
import com.example.budgetboet.adapter.ExpenseAdapter
import com.example.budgetboet.model.Category
import com.example.budgetboet.model.Expense
import com.example.budgetboet.utils.UserUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExpenseListActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var toggle : ActionBarDrawerToggle

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var expenseList: MutableList<Expense>
    private lateinit var adapter: ExpenseAdapter
    private val categoryMap = mutableMapOf<String, String>() // Map<ID, Name>

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Initialize RecyclerView components
        recyclerView = findViewById(R.id.rvExpenses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        expenseList = mutableListOf()
        // Initialize adapter with an empty category map for now
        adapter = ExpenseAdapter(expenseList, categoryMap)
        recyclerView.adapter = adapter

        // Load data from Firebase
        loadCategoriesAndExpenses()


        // --- Navigation Setup ---
        val drawerLayout : DrawerLayout = findViewById(R.id.main)
        val navView : NavigationView = findViewById(R.id.nav_view)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this,drawerLayout, R.string.open, R.string.close)
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

                R.id.nav_rewards -> startActivity(Intent(applicationContext, RewardsActivity::class.java))

                R.id.nav_logout ->{
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(applicationContext, Login::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(navView)
            true
        }
        // --- End Navigation Setup ---

        val fab = findViewById<FloatingActionButton>(R.id.fabNewExpense)
        fab.setOnClickListener {
            val intent = Intent(this, ExpenseEntryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadCategoriesAndExpenses() {
        val userId = auth.currentUser?.uid ?: return
        val categoriesRef = database.child("categories").child(userId)

        // 1. First, fetch the categories and populate the map
        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryMap.clear()
                for (categorySnapshot in snapshot.children) {
                    val category = categorySnapshot.getValue(Category::class.java)
                    val categoryId = categorySnapshot.key
                    if (category != null && categoryId != null) {
                        categoryMap[categoryId] = category.name
                    }
                }
                // 2. After categories are loaded, fetch the expenses
                loadExpenses(userId)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ExpenseListActivity, "Failed to load categories: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadExpenses(userId: String) {
        val expensesRef = database.child("expenses").child(userId)

        expensesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                expenseList.clear()
                for (expenseSnapshot in snapshot.children) {
                    val expense = expenseSnapshot.getValue(Expense::class.java)
                    if (expense != null) {
                        expenseList.add(expense)
                    }
                }
                // Sort expenses by date if needed (optional)
                // expenseList.sortByDescending { it.date }

                // Notify the adapter that the data set has changed
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ExpenseListActivity, "Failed to load expenses: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
