package com.example.budgetboet

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class NewCategory : AppCompatActivity() {

    private lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_category)

        val drawerLayout : DrawerLayout = findViewById(R.id.main)
        val navView : NavigationView = findViewById(R.id.nav_view)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this,drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val catTypeSpinner: Spinner = findViewById(R.id.CatType)
        val catNameEditText: EditText = findViewById(R.id.CatName)
        val catDescEditText: EditText = findViewById(R.id.CatDesc)
        val createCatButton: Button = findViewById(R.id.CreateCat)

        // Populate spinner
        val categoryTypes = arrayOf("Income", "Expense", "Savings", "Investment")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        catTypeSpinner.adapter = adapter

        createCatButton.setOnClickListener {
            val categoryName = catNameEditText.text.toString().trim()
            val categoryType = catTypeSpinner.selectedItem.toString()
            val categoryDesc = catDescEditText.text.toString().trim()
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (categoryName.isEmpty()) {
                catNameEditText.error = "Category name is required"
                catNameEditText.requestFocus()
                return@setOnClickListener
            }

            if (userId == null) {
                Toast.makeText(this, "You need to be logged in to create a category.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance().reference
            val categoryId = database.child("categories").push().key

            if (categoryId != null) {
                val category = HashMap<String, Any>()
                category["name"] = categoryName
                category["type"] = categoryType
                category["description"] = categoryDesc
                category["userId"] = userId

                database.child("categories").child(categoryId).setValue(category)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Category created successfully", Toast.LENGTH_SHORT).show()
                            finish() // Close the activity
                        } else {
                            Toast.makeText(this, "Failed to create category: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}