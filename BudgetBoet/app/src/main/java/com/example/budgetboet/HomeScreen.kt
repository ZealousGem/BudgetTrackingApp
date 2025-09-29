package com.example.budgetboet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

    // private  lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_screen)

        auth = FirebaseAuth.getInstance()
        button = findViewById(R.id.logout)
        textView = findViewById(R.id.user_details)

        val user = auth.currentUser
        if(user == null){
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()

        }

        else
        {
            LoadingUserName(user.uid)
        }

        button.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun LoadingUserName(uid : String)
    {
      val ref = database.getReference("users").child(uid)

        ref.addListenerForSingleValueEvent(object  : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    val  profile = snapshot.getValue(UserProfile::class.java)

                    val disInfo = profile?.username ?: profile?.email ?: "User"

                    textView.text = "Welcome, $disInfo"
                }

                else {
                    textView.text = "Welcome, ${auth.currentUser?.email?: "User"}"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                textView.text = "Welcome, ${auth.currentUser?.email?: "User"}. (profile load failed)"
            }
        })
    }
}