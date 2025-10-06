package com.example.budgetboet.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import com.example.budgetboet.CategorySpent
import com.example.budgetboet.Goals
import com.example.budgetboet.HomeScreen
import com.example.budgetboet.Login
import com.example.budgetboet.NewCategory
import com.example.budgetboet.R
import com.example.budgetboet.model.Expense
import com.example.budgetboet.utils.UserUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpenseEntryActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var database: DatabaseReference

    // --- Firebase Storage reference ---
    private lateinit var storageRef: StorageReference

    // --- Variables to manage the photo file ---
    private var currentPhotoPath: String? = null
    private var imageUri: Uri? = null
    private lateinit var imageButton: ImageButton

    // --- Activity Result Launcher for Camera Intent (Modern approach) ---
    private val cameraResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // The image is saved to the 'imageUri' provided in dispatchTakePictureIntent()
            imageUri?.let {
                // Display the image in the ImageButton
                imageButton.setImageURI(it)
                Toast.makeText(this, "Receipt photo captured.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Handle case where user canceled the camera
            currentPhotoPath = null
            imageUri = null
        }
    }


    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_entry)

        // --- Initialize Firebase Storage ---
        database = FirebaseDatabase.getInstance().reference
        val bucketUrl = "gs://budgetboet-receipts-free"
        storageRef = FirebaseStorage.getInstance().reference

        /// navigation stuff
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // --- Get reference to ImageButton ---
        imageButton = findViewById(R.id.imageButton)

        // Existing view references
        val nameInput = findViewById<EditText>(R.id.txtEntryName)
        val amountInput = findViewById<EditText>(R.id.txtAmount)
        val categoryDropdown = findViewById<Spinner>(R.id.categoryDropDown)
        val dateDropdown = findViewById<DatePicker>(R.id.datePicker)
        val startTimeDropdown = findViewById<TimePicker>(R.id.startTimePicker)
        val endTimeDropdown = findViewById<TimePicker>(R.id.endTimePicker)
        val saveButton = findViewById<Button>(R.id.btnSaveEntry)


        val drawerLayout : DrawerLayout = findViewById(R.id.expense_entry)
        val navView : NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if(user != null){
            // ... login redirect ...
            UserUtils.loadUserNameAndEmail(user.uid, navView)
        }

        navView.setNavigationItemSelectedListener {
            // (Navigation logic remains the same)
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
        } ///  end of navigation stuff

        // --- ImageButton Listener to launch camera ---
        imageButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        saveButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val amount = amountInput.text.toString().toDoubleOrNull() ?: 0.0
            val category = categoryDropdown.selectedItem.toString()

            if (name.isEmpty() || amount <= 0.0) {
                Toast.makeText(this, "Please enter a valid name and amount.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Date and Time formatting logic (remains the same)
            val day = dateDropdown.dayOfMonth
            val month = dateDropdown.month
            val year = dateDropdown.year
            val calendar = Calendar.getInstance().apply { set(year, month, day) }
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = dateFormat.format(calendar.time)

            val startHour = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) startTimeDropdown.hour else startTimeDropdown.currentHour
            val startMinute = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) startTimeDropdown.minute else startTimeDropdown.currentMinute
            val startTime = String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute)

            val endHour = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) endTimeDropdown.hour else endTimeDropdown.currentHour
            val endMinute = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) endTimeDropdown.minute else endTimeDropdown.currentMinute
            val endTime = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute)

            val userId = auth.currentUser?.uid

            if (userId != null) {

                // If an image was taken, upload it first
                if (imageUri != null) {
                    uploadImageAndSaveExpense(userId, name, amount, category, date, startTime, endTime)
                } else {
                    // Save the expense without an image URL
                    saveExpenseToDatabase(userId, name, amount, category, date, startTime, endTime, "")
                }

            } else {
                Toast.makeText(this, "User not authenticated. Cannot save expense.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- Function to create a temporary file for the photo ---
    @Throws(java.io.IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    // --- Function to dispatch the camera intent ---
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: java.io.IOException) {
                    // Error occurred while creating the File
                    Toast.makeText(this, "Error creating image file.", Toast.LENGTH_SHORT).show()
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    imageUri = FileProvider.getUriForFile(
                        this,
                        "com.example.budgetboet.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    cameraResultLauncher.launch(takePictureIntent)
                }
            }
        }
    }

    // --- Function to upload image to Firebase Storage and save expense data ---
    private fun uploadImageAndSaveExpense(
        userId: String,
        name: String,
        amount: Double,
        category: String,
        date: String,
        startTime: String,
        endTime: String
    ) {
        imageUri?.let { uri ->
            Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show()

            // Create a unique file name in Storage
            val fileName = "${userId}_${System.currentTimeMillis()}.jpg"
            val imageRef = storageRef.child("receipt_images/$userId/$fileName")

            imageRef.putFile(uri)
                .addOnSuccessListener {
                    // Get the download URL for the uploaded image
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        // Save the expense with the image URL
                        saveExpenseToDatabase(userId, name, amount, category, date, startTime, endTime, downloadUrl.toString())
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to get image URL: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_LONG).show()
                    saveExpenseToDatabase(userId, name, amount, category, date, startTime, endTime, "")
                }
        }
    }

    private fun saveExpenseToDatabase(    userId: String,
                                          name: String,
                                          amount: Double,
                                          category: String,
                                          date: String,
                                          startTime: String,
                                          endTime: String,
                                          imageUrl: String // The URL from Firebase Storage, or "" if no image
    )
    {

        try {
        } catch (e: Exception) {
            TODO("Not yet implemented")
        }
        val userExpensesRef = database.child("expenses").child(userId)

        // Create a unique key for the new expense
        val expenseId = userExpensesRef.push().key

        if (expenseId == null) {
            Toast.makeText(this, "Could not create expense entry.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create the Expense object with all the data
        val expense = Expense(
            id = expenseId,
            name = name,
            amount = amount.toString(),
            category = category,
            date = date,
            startTime = startTime,
            endTime = endTime,
            image = imageUrl
        )

        // Save the expense object to the correct location: /expenses/{userId}/{expenseId}
        userExpensesRef.child(expenseId).setValue(expense)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show()
                // Go back to the previous screen (e.g., the home screen or expense list)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save expense: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean { ///  navigation stuff, do not touch or i will kill you

        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}