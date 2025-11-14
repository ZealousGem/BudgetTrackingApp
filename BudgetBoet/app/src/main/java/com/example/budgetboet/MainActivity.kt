package com.example.budgetboet

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.google.firebase.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GoalAdapter
    private lateinit var goalList: MutableList<Goal>
    private lateinit var addGoalButton: FloatingActionButton
    private lateinit var dbRef: DatabaseReference
    private lateinit var userPointsRef: DatabaseReference // 1. Declare a new reference for user points

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)
        dbRef = Firebase.database.reference.child("goals")
        userPointsRef = Firebase.database.reference.child("userPoints") // 2. Initialize the reference

        goalList = mutableListOf()

        recyclerView = findViewById(R.id.recycle)
        recyclerView.layoutManager = LinearLayoutManager(this)


        // 3. Pass the new 'userPointsRef' to the GoalAdapter constructor
        adapter = GoalAdapter(goalList, dbRef, userPointsRef)
        recyclerView.adapter = adapter

        addGoalButton = findViewById(R.id.addGoalButton)
        addGoalButton.setOnClickListener {
            showAddGoalDialog()
        }
    }

    private fun showAddGoalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_goal, null)
        val goalNameInput = dialogView.findViewById<EditText>(R.id.inputGoalName)
        val targetAmountInput = dialogView.findViewById<EditText>(R.id.inputTargetAmount)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Savings Goal")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val name = goalNameInput.text.toString().trim()
                val targetText = targetAmountInput.text.toString().trim()

                if (name.isNotEmpty() && targetText.isNotEmpty()) {
                    val target = targetText.toIntOrNull()
                    if (target != null && target > 0) {
                        goalList.add(Goal(name, target.toString(), 0))
                        adapter.notifyItemInserted(goalList.size - 1)
                        recyclerView.scrollToPosition(goalList.size - 1)
                        // You should also save the new goal to Firebase here
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Enter a valid target amount", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }
}
