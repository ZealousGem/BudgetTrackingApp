package com.example.budgetboet

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetboet.ui.ExpenseEntryActivity
import com.example.budgetboet.ui.ExpenseListActivity
import com.example.budgetboet.utils.UserUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Goals : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var dbRef: DatabaseReference

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GoalAdapter
    private lateinit var goalList: MutableList<Goal>
    private lateinit var addGoalButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        dbRef = FirebaseDatabase.getInstance().getReference("users").child(user.uid).child("goals")

        setupUI()

        goalList = mutableListOf()
        recyclerView = findViewById(R.id.recycle)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GoalAdapter(goalList, dbRef) // Pass dbRef to adapter
        recyclerView.adapter = adapter

        addGoalButton = findViewById(R.id.addGoalButton)
        addGoalButton.setOnClickListener {
            showAddGoalDialog()
        }

        loadGoalsFromFirebase()
    }

    private fun setupUI() {
        val drawerLayout: DrawerLayout = findViewById(R.id.main)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val user = auth.currentUser
        if (user != null) {
            CoroutineScope(Dispatchers.Main).launch {
                UserUtils.loadUserNameAndEmail(user.uid, navView)
            }
        }

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> startActivity(Intent(applicationContext, HomeScreen::class.java))
                R.id.nav_expense -> startActivity(Intent(applicationContext, ExpenseEntryActivity::class.java))
                R.id.nav_expense_view -> startActivity(Intent(applicationContext, ExpenseListActivity::class.java))
                R.id.nav_category -> startActivity(Intent(applicationContext, NewCategory::class.java))
                R.id.nav_category_view -> startActivity(Intent(applicationContext, CategorySpent::class.java))
                R.id.nav_goals -> startActivity(Intent(applicationContext, Goals::class.java))
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(applicationContext, Login::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(navView)
            true
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun loadGoalsFromFirebase() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                goalList.clear()
                for (goalSnapshot in snapshot.children) {
                    val goal = goalSnapshot.getValue(Goal::class.java)
                    if (goal != null) {
                        goalList.add(goal)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Goals, "Failed to load goals: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddGoalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_goal, null)
        val goalNameInput = dialogView.findViewById<EditText>(R.id.inputGoalName)
        val targetAmountInput = dialogView.findViewById<EditText>(R.id.inputTargetAmount)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Savings Goal")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val name = goalNameInput.text.toString().trim()
                val targetText = targetAmountInput.text.toString().trim()

                if (name.isNotEmpty() && targetText.isNotEmpty()) {
                    val target = targetText.toIntOrNull()
                    if (target != null && target > 0) {
                        val goalId = dbRef.push().key!!
                        val newGoal = Goal(goalId, name, target, 0)
                        dbRef.child(goalId).setValue(newGoal)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Enter a valid target amount", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
