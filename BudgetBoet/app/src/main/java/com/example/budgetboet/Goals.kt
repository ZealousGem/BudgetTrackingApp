package com.example.budgetboet

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
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
        }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    }
