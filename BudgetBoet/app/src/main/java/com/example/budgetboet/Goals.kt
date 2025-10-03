package com.example.budgetboet

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.budgetboet.ui.ExpenseEntryActivity
import com.example.budgetboet.ui.ExpenseListActivity
import com.example.budgetboet.utils.UserUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class Goals : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

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
        }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    }
