package com.example.budgetboet   // same package


import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.budgetboet.GoalAdapter


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GoalAdapter
    private lateinit var goalList: MutableList<Goal>
    private lateinit var addGoalButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        goalList = mutableListOf()

        recyclerView = findViewById(R.id.goalRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        adapter = GoalAdapter(goalList)
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

        // Use a variable for the dialog to dismiss it from the listener
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Savings Goal")
            .setView(dialogView)
            .setPositiveButton("Add", null) // Set listener to null initially
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            // Get the button from the dialog itself
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val name = goalNameInput.text.toString().trim()
                val targetText = targetAmountInput.text.toString().trim()

                if (name.isNotEmpty() && targetText.isNotEmpty()) {
                    val target = targetText.toIntOrNull()
                    if (target != null && target > 0) {
                        // --- SUCCESS ---
                        goalList.add(Goal(name, target, 0))
                        adapter.notifyItemInserted(goalList.size - 1)
                        recyclerView.scrollToPosition(goalList.size - 1)

                        // Only dismiss when everything is successful
                        dialog.dismiss()
                    } else {
                        // --- VALIDATION FAILED ---
                        Toast.makeText(this, "Enter a valid target amount", Toast.LENGTH_SHORT).show()
                        // DO NOT dismiss the dialog
                    }
                } else {
                    // --- VALIDATION FAILED ---
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    // DO NOT dismiss the dialog
                }
            }
        }

        dialog.show()
    }

}

private fun Unit.setNegativeButton(string: String, function: Any) {
    TODO("Not yet implemented")
}

private fun ERROR.setPositiveButton(string: String, nothing: Nothing?) {}

annotation class ERROR

