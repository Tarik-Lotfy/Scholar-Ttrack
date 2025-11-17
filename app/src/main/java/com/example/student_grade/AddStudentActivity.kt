package com.example.student_grade

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.student_grade.database.AppDatabase
import com.example.student_grade.database.Converters
import com.example.student_grade.database.Grade
import com.example.student_grade.database.Student
import com.example.student_grade.database.StudentRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AddStudentActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var etStudentName: TextInputEditText
    private lateinit var gradesContainer: LinearLayout
    private lateinit var btnAddGrade: MaterialButton
    private lateinit var btnSaveStudent: MaterialButton
    private lateinit var repository: StudentRepository
    private val converters = Converters()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_student)
        
        // Initialize views
        bottomNavigation = findViewById(R.id.bottomNavigation)
        etStudentName = findViewById(R.id.etStudentName)
        gradesContainer = findViewById(R.id.gradesContainer)
        btnAddGrade = findViewById(R.id.btnAddGrade)
        btnSaveStudent = findViewById(R.id.btnSaveStudent)
        
        // Setup bottom navigation
        bottomNavigation.selectedItemId = R.id.nav_add_student
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
                    // Already here
                    true
                }
                R.id.nav_leaderboard -> {
                    startActivity(Intent(this, LeaderboardActivity::class.java))
                    false
                }
                else -> false
            }
        }
        
        // Initialize database and repository
        val database = AppDatabase.getDatabase(applicationContext)
        repository = StudentRepository(database.studentDao())
        
        // Add first grade input by default
        addGradeInput()
        
        // Setup button click listeners
        btnAddGrade.setOnClickListener {
            addGradeInput()
        }
        
        btnSaveStudent.setOnClickListener {
            saveStudent()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    private fun addGradeInput() {
        val gradeView = LayoutInflater.from(this)
            .inflate(R.layout.item_grade_input, gradesContainer, false)
        
        val btnRemove = gradeView.findViewById<ImageButton>(R.id.btnRemove)
        
        // Only show remove button if there's more than one grade
        btnRemove.setOnClickListener {
            if (gradesContainer.childCount > 1) {
                gradesContainer.removeView(gradeView)
            } else {
                Toast.makeText(this, R.string.toast_grades_empty, Toast.LENGTH_SHORT).show()
            }
        }
        
        gradesContainer.addView(gradeView)
    }
    
    private fun saveStudent() {
        val name = etStudentName.text.toString().trim()
        
        // Validate name
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.toast_name_empty, Toast.LENGTH_SHORT).show()
            return
        }
        
        // Collect all grades
        val grades = mutableListOf<Grade>()
        
        for (i in 0 until gradesContainer.childCount) {
            val gradeView = gradesContainer.getChildAt(i)
            val etSubject = gradeView.findViewById<TextInputEditText>(R.id.etSubject)
            val etScore = gradeView.findViewById<TextInputEditText>(R.id.etScore)
            
            val subject = etSubject.text.toString().trim()
            val scoreText = etScore.text.toString().trim()
            
            // Check if both fields are filled
            if (subject.isEmpty() && scoreText.isEmpty()) {
                continue // Skip empty grade fields
            }
            
            if (subject.isEmpty()) {
                Toast.makeText(this, R.string.toast_subject_empty, Toast.LENGTH_SHORT).show()
                etSubject.requestFocus()
                return
            }
            
            val score = scoreText.toIntOrNull()
            if (score == null || score < 0 || score > 100) {
                Toast.makeText(this, R.string.toast_grades_invalid, Toast.LENGTH_SHORT).show()
                etScore.requestFocus()
                return
            }
            
            grades.add(Grade(subject, score))
        }
        
        // Validate that at least one grade was entered
        if (grades.isEmpty()) {
            Toast.makeText(this, R.string.toast_grades_empty, Toast.LENGTH_SHORT).show()
            return
        }
        
        // Convert grades to JSON string
        val gradesJson = converters.fromGradeList(grades)
        val student = Student(name = name, gradesCsv = gradesJson)
        
        // Insert into database
        lifecycleScope.launch {
            repository.insertStudent(student)
            
            Toast.makeText(
                this@AddStudentActivity,
                R.string.toast_student_saved,
                Toast.LENGTH_SHORT
            ).show()
            
            finish()
        }
    }
}

