package com.example.budgetboet

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetboet.ui.ExpenseEntryActivity
import com.example.budgetboet.ui.ExpenseListActivity
import com.example.budgetboet.utils.UserUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RewardsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var textViewUserPoints: TextView
    private lateinit var rewardsRecyclerView: RecyclerView
    private lateinit var pointsRef: DatabaseReference
    private lateinit var rewardsAdapter: RewardsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewards)

        textViewUserPoints = findViewById(R.id.textViewUserPoints)
        rewardsRecyclerView = findViewById(R.id.rewardsRecyclerView)

        val user = FirebaseAuth.getInstance().currentUser

        auth = FirebaseAuth.getInstance()

        val drawerLayout : DrawerLayout = findViewById(R.id.main)
        val navView : NavigationView = findViewById(R.id.nav_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle) // This line is correct

        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
        if (user == null) {
            // Send user to login if not authenticated
            Toast.makeText(this, "Please log in to view rewards.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize points reference
        pointsRef = FirebaseDatabase.getInstance().getReference("users").child(user.uid).child("points")

        setupRewardsList()
        listenForPointsUpdate()
    }

    private fun setupRewardsList() {
        val rewardsList = getAvailableRewards() // Load rewards

        // The adapter needs the list of rewards and the reference to update points
        rewardsAdapter = RewardsAdapter(rewardsList, pointsRef)
        rewardsRecyclerView.layoutManager = LinearLayoutManager(this)
        rewardsRecyclerView.adapter = rewardsAdapter
    }

    private fun listenForPointsUpdate() {
        // Listen for real-time changes to the user's points
        pointsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Read the integer value, defaulting to 0 if null
                val points = snapshot.getValue(Int::class.java) ?: 0
                textViewUserPoints.text = "$points Points"

                // Update adapter so it can enable/disable claim buttons based on new points
                rewardsAdapter.updateUserPoints(points)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RewardsActivity, "Failed to load points: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Static list of rewards available in your app
    private fun getAvailableRewards(): List<Reward> {
        return listOf(
            Reward("RWD001", "Small Discount", "R50 off your next expense", 250),
            Reward("RWD002", "Monthly Report", "Get an exclusive detailed monthly finance report", 500),
            Reward("RWD003", "Major Discount", "R200 off your next expense", 1000)
        )
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}