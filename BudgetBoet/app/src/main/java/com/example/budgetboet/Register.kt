package com.example.budgetboet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Register : AppCompatActivity() {

    // 🌟 FIX APPLIED: Use 'lazy' initialization for FirebaseAuth.
    // This ensures 'auth' is initialized safely before it is first accessed (e.g., in onStart()).
    private val auth by lazy { FirebaseAuth.getInstance() }

    // Changed lateinit var to val where appropriate (lazy initialization makes it a val)
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonReg: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textView: TextView


    public override fun onStart() {
        super.onStart()

        // auth is now safe to use here
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // ----------------------------------------------------
        // Initialization (Removed the old 'auth = FirebaseAuth.getInstance()')
        // ----------------------------------------------------
        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        buttonReg = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.loginNow)

        textView.setOnClickListener {
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }

        buttonReg.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            progressBar.visibility = View.VISIBLE

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all the details", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE // 🌟 ADDED: Hide progress bar on error
                return@setOnClickListener
            }

            // Use Firebase Authentication to create a new user (for registration)
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                        // 🌟 Suggested Improvement: Navigate to Main Activity after successful registration
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        // Display detailed error if possible
                        val errorMessage = task.exception?.localizedMessage ?: "Registration failed."
                        Toast.makeText(baseContext, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}