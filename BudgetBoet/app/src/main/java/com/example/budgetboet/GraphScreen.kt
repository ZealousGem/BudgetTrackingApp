package com.example.budgetboet

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.budgetboet.ui.ExpenseEntryActivity
import com.example.budgetboet.ui.ExpenseListActivity
import com.example.budgetboet.databinding.ActivityGraphScreenBinding
import com.example.budgetboet.utils.UserUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth



class GraphScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var toggle : ActionBarDrawerToggle

    private var _binding: ActivityGraphScreenBinding? = null
    private val binding get() = _binding!!


    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityGraphScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val drawerLayout : DrawerLayout = findViewById(R.id.main)
        val navView : NavigationView = findViewById(R.id.nav_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle) // This line is correct

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



        binding.apply {

            barGraph.animation.duration = animationDuration
            barGraph.animate(barSet)



        }




    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        // Data for the regular (vertical) bar chart. List of pairs: Label (String) to Value (Float)
        private val barSet = listOf(
            "JAN" to 4F,
            "FEB" to 7F,
            "MAR" to 2F,
            "MAY" to 2.3F,
            "APR" to 5F,
            "JUN" to 4F
        )

        // Data for the horizontal bar chart
        private val horizontalBarSet = listOf(
            "PORRO" to 5F,
            "FUSCE" to 6.4F,
            "EGET" to 3F
        )

        // Animation duration constant in milliseconds (1 second)
        private const val animationDuration = 1000L
    }
}





