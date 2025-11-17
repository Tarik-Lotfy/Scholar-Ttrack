package com.example.student_grade

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.student_grade.adapter.LeaderboardAdapter
import com.example.student_grade.database.AppDatabase
import com.example.student_grade.database.Converters
import com.example.student_grade.database.StudentRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class LeaderboardActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var podiumContainer: LinearLayout
    private lateinit var firstPlaceContainer: LinearLayout
    private lateinit var secondPlaceContainer: LinearLayout
    private lateinit var thirdPlaceContainer: LinearLayout
    private lateinit var tvFirstName: TextView
    private lateinit var tvFirstAverage: TextView
    private lateinit var tvSecondName: TextView
    private lateinit var tvSecondAverage: TextView
    private lateinit var tvThirdName: TextView
    private lateinit var tvThirdAverage: TextView
    private lateinit var recyclerViewRankings: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var repository: StudentRepository
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private val converters = Converters()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)
        
        // Initialize views
        bottomNavigation = findViewById(R.id.bottomNavigation)
        podiumContainer = findViewById(R.id.podiumContainer)
        firstPlaceContainer = findViewById(R.id.firstPlaceContainer)
        secondPlaceContainer = findViewById(R.id.secondPlaceContainer)
        thirdPlaceContainer = findViewById(R.id.thirdPlaceContainer)
        tvFirstName = findViewById(R.id.tvFirstName)
        tvFirstAverage = findViewById(R.id.tvFirstAverage)
        tvSecondName = findViewById(R.id.tvSecondName)
        tvSecondAverage = findViewById(R.id.tvSecondAverage)
        tvThirdName = findViewById(R.id.tvThirdName)
        tvThirdAverage = findViewById(R.id.tvThirdAverage)
        recyclerViewRankings = findViewById(R.id.recyclerViewRankings)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        
        // Initialize database and repository
        val database = AppDatabase.getDatabase(applicationContext)
        repository = StudentRepository(database.studentDao())
        
        // Setup RecyclerView
        leaderboardAdapter = LeaderboardAdapter()
        recyclerViewRankings.apply {
            layoutManager = LinearLayoutManager(this@LeaderboardActivity)
            adapter = leaderboardAdapter
        }
        
        // Setup bottom navigation
        bottomNavigation.selectedItemId = R.id.nav_leaderboard
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_add_student -> {
                    startActivity(Intent(this, AddStudentActivity::class.java))
                    false
                }
                R.id.nav_leaderboard -> {
                    // Already here
                    true
                }
                else -> false
            }
        }
        
        loadLeaderboard()
    }
    
    override fun onResume() {
        super.onResume()
        bottomNavigation.selectedItemId = R.id.nav_leaderboard
        loadLeaderboard()
    }
    
    private fun loadLeaderboard() {
        lifecycleScope.launch {
            val students = repository.getAllStudents()
            
            if (students.isEmpty()) {
                // Show empty state
                tvEmptyState.visibility = View.VISIBLE
                podiumContainer.visibility = View.GONE
                recyclerViewRankings.visibility = View.GONE
                return@launch
            }
            
            tvEmptyState.visibility = View.GONE
            podiumContainer.visibility = View.VISIBLE
            recyclerViewRankings.visibility = View.VISIBLE
            
            // Calculate averages and sort by average descending
            val studentsWithAverages = students.map { student ->
                val grades = converters.toGradeList(student.gradesCsv)
                val average = if (grades.isNotEmpty()) {
                    grades.map { it.score }.average()
                } else {
                    0.0
                }
                Pair(student, average)
            }.sortedByDescending { it.second }
            
            // Display top 3 in podium
            if (studentsWithAverages.isNotEmpty()) {
                // First place
                firstPlaceContainer.visibility = View.VISIBLE
                tvFirstName.text = studentsWithAverages[0].first.name
                tvFirstAverage.text = getString(R.string.average_format, studentsWithAverages[0].second)
            } else {
                firstPlaceContainer.visibility = View.GONE
            }
            
            if (studentsWithAverages.size > 1) {
                // Second place
                secondPlaceContainer.visibility = View.VISIBLE
                tvSecondName.text = studentsWithAverages[1].first.name
                tvSecondAverage.text = getString(R.string.average_format, studentsWithAverages[1].second)
            } else {
                secondPlaceContainer.visibility = View.GONE
            }
            
            if (studentsWithAverages.size > 2) {
                // Third place
                thirdPlaceContainer.visibility = View.VISIBLE
                tvThirdName.text = studentsWithAverages[2].first.name
                tvThirdAverage.text = getString(R.string.average_format, studentsWithAverages[2].second)
            } else {
                thirdPlaceContainer.visibility = View.GONE
            }
            
            // Display ALL students in RecyclerView with rankings
            leaderboardAdapter.updateLeaderboard(studentsWithAverages, startRank = 1)
        }
    }
}

