package com.example.budgetboet.utils

import android.widget.TextView
import com.example.budgetboet.R
import com.example.budgetboet.UserProfile
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserUtils {
    companion object {
        private val database by lazy { FirebaseDatabase.getInstance() }
        private val auth by lazy { FirebaseAuth.getInstance() }

        fun loadUserNameAndEmail(uid: String, navView: NavigationView) {
            val headerView = navView.getHeaderView(0)
            val unameTextView: TextView = headerView.findViewById(R.id.Uname)
            val uemailTextView: TextView = headerView.findViewById(R.id.Uemail)

            val userEmail = auth.currentUser?.email ?: "Email not available"
            uemailTextView.text = userEmail

            val defaultDisplayName = auth.currentUser?.email ?: "User"
            unameTextView.text = defaultDisplayName

            val ref = database.getReference("users").child(uid)

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val profile = snapshot.getValue(UserProfile::class.java)
                        val disInfo = profile?.username ?: defaultDisplayName
                        unameTextView.text = disInfo
                    } else {
                        unameTextView.text = "Welcome, ${auth.currentUser?.email ?: "User"}"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    val errorDisplayName = auth.currentUser?.email ?: "User"
                    unameTextView.text = errorDisplayName
                }
            })
        }
    }
}