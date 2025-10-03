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

// This class will act as a holder for static utility functions related to the user.
class UserUtils {


    // A companion object allows you to call methods on the class itself (like a static method in Java).
    companion object {


        private val database by lazy { FirebaseDatabase.getInstance() }
        private val auth by lazy { FirebaseAuth.getInstance() }

        /**
         * Loads the user's name and email into the Navigation View header.
         *
         * @param uid The Firebase User ID.
         * @param navView The NavigationView whose header needs updating.
         * @param welcomeTextView Optional: The TextView on the main screen to show a welcome message.
         */
        fun loadUserNameAndEmail(uid: String, navView: NavigationView) {

            // Get the header view of the navigation drawer
            val headerView = navView.getHeaderView(0)

            // Find the TextViews inside the header (assuming R.id.Uname and R.id.Uemail are in the header layout)
            val unameTextView: TextView = headerView.findViewById(R.id.Uname)
            val uemailTextView: TextView = headerView.findViewById(R.id.Uemail)

            // Set email immediately as it's locally available
            val userEmail = auth.currentUser?.email ?: "Email not available"
            uemailTextView.text = userEmail

            // Default display name if profile load fails or doesn't exist
            val defaultDisplayName = auth.currentUser?.email ?: "User"
            unameTextView.text = defaultDisplayName


            // Database reference to fetch the username
            val ref = database.getReference("users").child(uid)

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Assuming you have a UserProfile data class defined somewhere
                        val profile = snapshot.getValue(UserProfile::class.java)

                        // Prioritize username, then email, otherwise default to "User"
                        val disInfo = profile?.username ?: defaultDisplayName

                        unameTextView.text = disInfo


                    } else {
                        // If user profile doesn't exist in the database, use default values already set
                        unameTextView.text = "Welcome, ${auth.currentUser?.email?: "User"}"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error case
                    val errorDisplayName = auth.currentUser?.email ?: "User"
                    unameTextView.text = errorDisplayName

                }
            })
        }
    }
}