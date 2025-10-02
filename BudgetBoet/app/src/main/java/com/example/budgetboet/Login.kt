
package com.example.budgetboet
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetboet.HomeScreen
import com.example.budgetboet.R
import com.example.budgetboet.Register
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {


    private val auth by lazy { FirebaseAuth.getInstance() }

    private val database by lazy { FirebaseDatabase.getInstance() }

    // Declared views as before
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textView: TextView

    public override fun onStart() {
        super.onStart()



        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(applicationContext, HomeScreen::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_login)


        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        buttonLogin = findViewById(R.id.btn_login)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.registerNow)


        textView.setOnClickListener {
            // Navigate to Register screen
            val intent = Intent(applicationContext, Register::class.java)
            startActivity(intent)
            finish()
        }

        buttonLogin.setOnClickListener {

            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            progressBar.visibility = View.VISIBLE

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all the details", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }
             LoginAttempt(email, password)


        }
    }

    private fun LoginAttempt(text : String, password: String)
    {

        if(text.contains("@") && text.contains("."))
        {
            LoginWithEmail(text, password)
        }

        else {
            LoginWithUserName(text, password)
        }
    }


    private fun LoginWithUserName(userName : String, password: String)
    {
        val usernameRef = database.getReference("users")

        usernameRef.orderByChild("username").equalTo(userName).addListenerForSingleValueEvent( object  : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.childrenCount > 0) {
                    val userSnapshot = snapshot.children.first()

                    // 3. Deserialize the UserProfile object from the user's specific data snapshot
                    val profile = userSnapshot.getValue(UserProfile::class.java)

                    val email =  profile?.email

                    if(email != null){
                        // Found email: proceed with Firebase Authentication login
                        LoginWithEmail(email, password)
                        return
                    }

                }
                else {
                    // Handle case where no user is found
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@Login, "Login failed: User not found.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle any error that prevents the database query from completing
                progressBar.visibility = View.GONE
                Toast.makeText(this@Login, "Login failed due to database error: ${error.message}", Toast.LENGTH_LONG).show()
            }

        })

    }

    private fun LoginWithEmail(email : String, password : String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT).show()

                    // Navigate to Main screen
                    val intent = Intent(applicationContext, HomeScreen::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Display error message
                    Toast.makeText(
                        this,
                        "Authentication failed. Check credentials or register.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
}