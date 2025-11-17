package com.example.student_grade

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.student_grade.adapter.StudentAdapter
import com.example.student_grade.database.AppDatabase
import com.example.student_grade.database.Student
import com.example.student_grade.database.StudentRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var studentAdapter: StudentAdapter
    private lateinit var repository: StudentRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        
        // Initialize database and repository
        val database = AppDatabase.getDatabase(applicationContext)
        repository = StudentRepository(database.studentDao())
        
        // Setup RecyclerView
        studentAdapter = StudentAdapter(ArrayList()) { student ->
            openStudentDetail(student)
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = studentAdapter
        }
        
        // Setup bottom navigation
        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home, do nothing or refresh
                    true
                }
                R.id.nav_add_student -> {
                    val intent = Intent(this, AddStudentActivity::class.java)
                    startActivity(intent)
                    false // Don't select this item
                }
                R.id.nav_leaderboard -> {
                    val intent = Intent(this, LeaderboardActivity::class.java)
                    startActivity(intent)
                    false // Don't select this item
                }
                else -> false
            }
        }
        
        loadStudents()
    }
    
    override fun onResume() {
        super.onResume()
        // Set home as selected when returning to this activity
        bottomNavigation.selectedItemId = R.id.nav_home
        loadStudents()
    }
    
    private fun loadStudents() {
        lifecycleScope.launch {
            val students = repository.getAllStudents()
            
            if (students.isEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    R.string.toast_no_students,
                    Toast.LENGTH_LONG
                ).show()
            }
            
            studentAdapter.updateStudents(students)
        }
    }
    
    private fun openStudentDetail(student: Student) {
        val intent = Intent(this, StudentDetailActivity::class.java)
        intent.putExtra("STUDENT_ID", student.id)
        startActivity(intent)
    }
}