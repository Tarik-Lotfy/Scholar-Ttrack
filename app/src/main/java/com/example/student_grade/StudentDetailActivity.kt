package com.example.student_grade

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.student_grade.database.AppDatabase
import com.example.student_grade.database.Converters
import com.example.student_grade.database.Student
import com.example.student_grade.database.StudentRepository
import com.example.student_grade.utils.ScoreColorHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class StudentDetailActivity : AppCompatActivity() {
    
    private lateinit var btnBack: FloatingActionButton
    private lateinit var tvStudentName: TextView
    private lateinit var tableGrades: TableLayout
    private lateinit var tvAverage: TextView
    private lateinit var btnAddGrade: FloatingActionButton
    private lateinit var btnDeleteStudent: MaterialButton
    private lateinit var repository: StudentRepository
    private val converters = Converters()
    
    private var currentStudent: Student? = null
    private var studentId: Int = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_detail)
        
        // Get student ID from intent
        studentId = intent.getIntExtra("STUDENT_ID", -1)
        if (studentId == -1) {
            Toast.makeText(this, R.string.toast_student_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Initialize views
        btnBack = findViewById(R.id.btnBack)
        tvStudentName = findViewById(R.id.tvStudentName)
        tableGrades = findViewById(R.id.tableGrades)
        tvAverage = findViewById(R.id.tvAverage)
        btnAddGrade = findViewById(R.id.btnAddGrade)
        btnDeleteStudent = findViewById(R.id.btnDeleteStudent)
        
        // Initialize database and repository
        val database = AppDatabase.getDatabase(applicationContext)
        repository = StudentRepository(database.studentDao())
        
        // Setup button click listeners
        btnBack.setOnClickListener {
            finish()
        }
        
        btnAddGrade.setOnClickListener {
            showAddGradeDialog()
        }
        
        btnDeleteStudent.setOnClickListener {
            showDeleteConfirmationDialog()
        }
        
        loadStudentDetails()
    }
    
    private fun loadStudentDetails() {
        lifecycleScope.launch {
            val student = repository.getStudentById(studentId)
            
            if (student == null) {
                Toast.makeText(
                    this@StudentDetailActivity,
                    R.string.toast_student_not_found,
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }
            
            currentStudent = student
            displayStudentDetails(student)
        }
    }
    
    private fun displayStudentDetails(student: Student) {
        // Display student name
        tvStudentName.text = student.name
        
        // Get grades list
        val grades = converters.toGradeList(student.gradesCsv)
        
        // Clear existing table rows
        tableGrades.removeAllViews()
        
        // Add grade rows with cards
        grades.forEachIndexed { index, grade ->
            // Create a card for each grade
            val cardView = layoutInflater.inflate(R.layout.item_grade_display, tableGrades, false)
            
            val tvSubject = cardView.findViewById<TextView>(R.id.tvSubject)
            val tvScore = cardView.findViewById<TextView>(R.id.tvScore)
            val scoreContainer = cardView.findViewById<View>(R.id.scoreContainer)
            
            tvSubject.text = grade.subject
            tvScore.text = grade.score.toString()
            
            // Apply score-based gradient to score container
            val gradient = ScoreColorHelper.getGradientForScore(this, grade.score.toDouble())
            scoreContainer.background = gradient
            
            val row = TableRow(this)
            row.layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            row.addView(cardView)
            tableGrades.addView(row)
        }
        
        // Calculate and display average
        val average = if (grades.isNotEmpty()) {
            grades.map { it.score }.average()
        } else {
            0.0
        }
        
        tvAverage.text = getString(R.string.average_format, average)
    }
    
    private fun showAddGradeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_grade, null)
        val etSubject = dialogView.findViewById<EditText>(R.id.etDialogSubject)
        val etScore = dialogView.findViewById<EditText>(R.id.etDialogScore)
        
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.dialog_add_grade_title)
            .setView(dialogView)
            .setPositiveButton(R.string.add) { _, _ ->
                val subject = etSubject.text.toString().trim()
                val scoreText = etScore.text.toString().trim()
                val score = scoreText.toIntOrNull()
                
                if (subject.isEmpty()) {
                    Toast.makeText(
                        this,
                        R.string.toast_subject_empty,
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (score == null || score < 0 || score > 100) {
                    Toast.makeText(
                        this,
                        R.string.toast_grade_invalid,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    addGrade(subject, score)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
        
        dialog.show()
    }
    
    private fun addGrade(subject: String, score: Int) {
        val student = currentStudent ?: return
        
        val currentGrades = converters.toGradeList(student.gradesCsv).toMutableList()
        currentGrades.add(com.example.student_grade.database.Grade(subject, score))
        
        val updatedGradesJson = converters.fromGradeList(currentGrades)
        val updatedStudent = student.copy(gradesCsv = updatedGradesJson)
        
        lifecycleScope.launch {
            repository.updateStudent(updatedStudent)
            
            Toast.makeText(
                this@StudentDetailActivity,
                R.string.toast_grade_added,
                Toast.LENGTH_SHORT
            ).show()
            
            loadStudentDetails()
        }
    }
    
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(R.string.dialog_delete_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteStudent()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
            .show()
    }
    
    private fun deleteStudent() {
        val student = currentStudent ?: return
        
        lifecycleScope.launch {
            repository.deleteStudent(student)
            
            Toast.makeText(
                this@StudentDetailActivity,
                R.string.toast_student_deleted,
                Toast.LENGTH_SHORT
            ).show()
            
            finish()
        }
    }
}

