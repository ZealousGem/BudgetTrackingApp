package com.example.budgetboet

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeScreen : AppCompatActivity() {

    private val database by lazy { FirebaseDatabase.getInstance() }

    private lateinit var auth: FirebaseAuth

    private lateinit var textView: TextView

    private lateinit var button: Button

    private lateinit var toggle : ActionBarDrawerToggle

    // private  lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_screen)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        auth = FirebaseAuth.getInstance()
        button = findViewById(R.id.logout)
        textView = findViewById(R.id.user_details)
        val drawerLayout : DrawerLayout = findViewById(R.id.main)
        val navView : NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this,drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val user = auth.currentUser
        if(user == null){
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()

        }

        else
        {
            LoadingUserName(user.uid, navView)
        }

        button.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }

       navView.setNavigationItemSelectedListener {

           true
       }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun LoadingUserName(uid : String, navView: NavigationView)
    {

        val headerView = navView.getHeaderView(0)

        // 2. Find the TextViews inside the header
        val unameTextView: TextView = headerView.findViewById(R.id.Uname)
        val uemailTextView: TextView = headerView.findViewById(R.id.Uemail)

        uemailTextView.text = auth.currentUser?.email ?: "Email does not exist"
        val ref = database.getReference("users").child(uid)

        ref.addListenerForSingleValueEvent( object  : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    val  profile = snapshot.getValue(UserProfile::class.java)

                    val disInfo = profile?.username ?: profile?.email ?: "User"

                    unameTextView.text = disInfo

                    textView.text = "Welcome, $disInfo"

                }

                else {
                    textView.text = "Welcome, ${auth.currentUser?.email?: "User"}"
                    unameTextView.text = "Welcome, ${auth.currentUser?.email?: "User"}"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                textView.text = "Welcome, ${auth.currentUser?.email?: "User"}. (profile load failed)"
            }
        })
    }
}