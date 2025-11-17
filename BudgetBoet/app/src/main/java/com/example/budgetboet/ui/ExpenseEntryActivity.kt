package com.example.budgetboet.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.ArrayAdapter
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
import com.example.budgetboet.RewardsActivity
import com.example.budgetboet.model.Category
import com.example.budgetboet.model.Expense
import com.example.budgetboet.utils.UserUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    private lateinit var imageButton: Button
    private lateinit var categoryDropdown: Spinner

    // --- List to hold category data ---
    private val categoryList = mutableListOf<Pair<String, String>>() // Pair of (ID, Name)

    // --- Activity Result Launcher for Camera Intent (Modern approach) ---
    private val cameraResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // The image is saved to the 'imageUri' provided in dispatchTakePictureIntent()
            imageUri?.let {
                // Display the image in the ImageButton
             //   imageButton.setImageURI(it)
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
        storageRef = FirebaseStorage.getInstance().reference

        /// navigation stuff
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // --- Get reference to ImageButton ---
        imageButton = findViewById(R.id.imageButton)
        categoryDropdown = findViewById(R.id.categoryDropDown)

        // Existing view references
        val nameInput = findViewById<EditText>(R.id.txtEntryName)
        val amountInput = findViewById<EditText>(R.id.txtAmount)
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
            loadCategories(user.uid)
        } else {
            // Handle user not logged in
            Toast.makeText(this, "Please log in to manage expenses.", Toast.LENGTH_LONG).show()
            finish() // or redirect to login
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

                R.id.nav_rewards -> startActivity(Intent(applicationContext, RewardsActivity::class.java))

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
            val selectedPosition = categoryDropdown.selectedItemPosition

            if (name.isEmpty() || amount <= 0.0) {
                Toast.makeText(this, "Please enter a valid name and amount.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedPosition < 0 || selectedPosition >= categoryList.size) {
                Toast.makeText(this, "Please select a valid category.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val categoryId = categoryList[selectedPosition].first


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
                    uploadImageAndSaveExpense(userId, name, amount, categoryId, date, startTime, endTime)
                } else {
                    // Save the expense without an image URL
                    saveExpenseToDatabase(userId, name, amount, categoryId, date, startTime, endTime, "")
                }

            } else {
                Toast.makeText(this, "User not authenticated. Cannot save expense.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCategories(userId: String) {
        val categoriesRef = database.child("categories").child(userId)
        categoriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()
                val categoryNames = mutableListOf<String>()
                for (categorySnapshot in snapshot.children) {
                    val category = categorySnapshot.getValue(Category::class.java)
                    val categoryId = categorySnapshot.key
                    if (category != null && categoryId != null) {
                        categoryList.add(Pair(categoryId, category.name))
                        categoryNames.add(category.name)
                    }
                }
                val adapter = ArrayAdapter(this@ExpenseEntryActivity, android.R.layout.simple_spinner_item, categoryNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categoryDropdown.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ExpenseEntryActivity, "Failed to load categories: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
                    null // Return null if file creation fails
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    // Create a content URI for the file using FileProvider
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "${applicationContext.packageName}.fileprovider",
                        it
                    )

                    // Save the URI to be used after the camera returns a result
                    imageUri = photoURI

                    // Add the URI as an extra to the intent. This tells the camera where to save the image.
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                    // Launch the camera intent using the modern Activity Result Launcher
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
