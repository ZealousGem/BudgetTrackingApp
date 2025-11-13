package com.example.budgetboet

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.budgetboet.databinding.ActivityLineGraphScreenBinding
import com.example.budgetboet.ui.ExpenseEntryActivity
import com.example.budgetboet.ui.ExpenseListActivity
import com.example.budgetboet.utils.UserUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import android.graphics.Color
import android.provider.ContactsContract
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate


class LineGraphScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var toggle : ActionBarDrawerToggle

    private var _binding : ActivityLineGraphScreenBinding? = null

    private val binding get() = _binding!!

    private val value = ArrayList<Entry>()
    private val valueBarChart = ArrayList<BarEntry>()

    private val xLabels = mutableListOf<String>()

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLineGraphScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val drawerLayout : DrawerLayout = findViewById(R.id.main)
        val navView : NavigationView = findViewById(R.id.nav_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle) // This line is correct

        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val user = auth.currentUser
        if(user != null){
            UserUtils.loadUserNameAndEmail(user.uid, navView)
        }

        navView.setNavigationItemSelectedListener {
            when(it.itemId)
            {
                R.id.nav_home ->{
                    startActivity(Intent(applicationContext, HomeScreen ::class.java))
                }
                R.id.nav_expense ->{
                    startActivity(Intent(applicationContext, ExpenseEntryActivity ::class.java))
                }
                R.id.nav_expense_view ->{
                    startActivity(Intent(applicationContext, ExpenseListActivity ::class.java))
                }
                R.id.nav_category ->{
                    startActivity(Intent(applicationContext, NewCategory ::class.java))
                }
                R.id.nav_category_view ->{
                    startActivity(Intent(applicationContext, CategorySpent ::class.java))
                }
                R.id.nav_goals ->{
                    startActivity(Intent(applicationContext, Goals ::class.java))
                }
                R.id.nav_logout ->{
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(applicationContext, Login::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(navView)
            true
        }

       DataListing()

    }

    private fun DataListing(){
        value.add(Entry(0f, 150f))
        value.add(Entry(1f, 50f))
        value.add(Entry(2f, 180f))

        valueBarChart.add(BarEntry(0f, 140f))
        valueBarChart.add(BarEntry(1f, 30f))
        valueBarChart.add(BarEntry(2f, 130f))

        xLabels.add("Food")      // Corresponds to x=0f
        xLabels.add("Transport") // Corresponds to x=1f
        xLabels.add("Bills")     // Corresponds to x=2f

        setChart()
    }

    private fun setChart(){

        val LineInfo = LineDataSet(value, "Maxium Goals")
        LineInfo.setColors(*ColorTemplate.VORDIPLOM_COLORS)
        LineInfo.valueTextColor = Color.BLUE
        LineInfo.circleColors = listOf(Color.RED)
        LineInfo.lineWidth = 5f
        LineInfo.circleRadius = 10f
        LineInfo.setDrawValues(false)

        val BarInfo = BarDataSet(valueBarChart, "Progression to goal")
        BarInfo.setColors(*ColorTemplate.VORDIPLOM_COLORS)

       val data= CombinedData()

        data.setData(LineData(LineInfo))
        data.setData(BarData(BarInfo))


        val xAxis = binding.LineChart.xAxis
        xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(xLabels)
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.setCenterAxisLabels(false)
        xAxis.setGranularity(1f)
        xAxis.axisMinimum = 0f
        // ... (Your existing XAxis label formatter and position setup)

        // ⭐ HIDE GRID LINES ⭐
        xAxis.setDrawGridLines(false) // Hides vertical grid lines

        val leftAxis = binding.LineChart.axisLeft
        leftAxis.setDrawGridLines(false) // Hides horizontal grid lines on the left side

        binding.LineChart.setExtraOffsets(15f, 0f, 0f, 0f)

        val rightAxis = binding.LineChart.axisRight
        rightAxis.setDrawGridLines(false) // Hides horizontal grid lines on the right side
        rightAxis.setDrawLabels(false) // Optionally hide the right axis labels if they aren't used
        rightAxis.setDrawAxisLine(false)


        xAxis.axisMaximum = xLabels.size.toFloat()

        binding.LineChart.data = data
        binding.LineChart.description.isEnabled = false
        binding.LineChart.description.text = "graph shows visual represenation of goal progress"
        binding.LineChart.setNoDataText("No Current Goals Available")
        binding.LineChart.description.textColor = Color.BLACK
        binding.LineChart.description.textSize = 12f
        binding.LineChart.animateXY(1400, 1400)
        binding.LineChart.setTouchEnabled(true)
        binding.LineChart.setPinchZoom(true)

       // binding.LineChart.zoom(2f, 1f, 0f, 0f)


        val legend = binding.LineChart.legend
        legend.isEnabled = true
        legend.form = Legend.LegendForm.LINE
        legend.textSize = 12f
        legend.textColor = Color.BLACK

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    companion object {
        private val lineSet = listOf(
            "label1" to 5f,
            "label2" to 4.5f,
            "label3" to 4.7f,
            "label4" to 3.5f,
            "label5" to 3.6f,

        )




        private const val animationDuration = 1000L
    }
}