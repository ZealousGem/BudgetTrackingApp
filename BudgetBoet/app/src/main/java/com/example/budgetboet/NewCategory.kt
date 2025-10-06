package com.example.budgetboet

import android.content.Intent
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
import com.example.budgetboet.model.Category
import com.example.budgetboet.ui.ExpenseEntryActivity
import com.example.budgetboet.ui.ExpenseListActivity
import com.example.budgetboet.utils.UserUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class NewCategory : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_category)

        val drawerLayout : DrawerLayout = findViewById(R.id.main)
        val navView : NavigationView = findViewById(R.id.nav_view)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        /// navigation stuff
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
        }  ///  end of navigation stuff

        val catTypeSpinner: Spinner = findViewById(R.id.CatType)
        val catNameEditText: EditText = findViewById(R.id.CatName)
        val catDescEditText: EditText = findViewById(R.id.CatDesc)
        val createCatButton: Button = findViewById(R.id.CreateCat)

        // Populate spinner
        val categoryTypes = arrayOf("Housing", "Transportation", "Food & Groceries", "Health & Wellness", "Personal Care", "Entertainment & Leisure", "Education", "Savings & Investments", "Debt", "Miscellaneous")
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
            val userCategoriesRef = database.child("categories").child(userId)
            val categoryId = userCategoriesRef.push().key

            if (categoryId != null) {
                val newCategory = Category(categoryName, categoryType, categoryDesc)

                userCategoriesRef.child(categoryId).setValue(newCategory)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean { ///  navigation stuff, do not touch or i will kill you

        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
