package com.example.student_grade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import com.example.student_grade.database.AppDatabase;
import com.example.student_grade.database.Converters;
import com.example.student_grade.database.Grade;
import com.example.student_grade.database.Student;
import com.example.student_grade.database.StudentRepository;
import com.example.student_grade.utils.ScoreColorHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class StudentDetailActivity extends AppCompatActivity {
    
    private FloatingActionButton btnBack;
    private TextView tvStudentName;
    private TableLayout tableGrades;
    private TextView tvAverage;
    private FloatingActionButton btnAddGrade;
    private MaterialButton btnDeleteStudent;
    private StudentRepository repository;
    private final Converters converters = new Converters();
    
    private Student currentStudent;
    private int studentId = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);
        
        // Get student ID from intent
        studentId = getIntent().getIntExtra("STUDENT_ID", -1);
        if (studentId == -1) {
            Toast.makeText(this, R.string.toast_student_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        tvStudentName = findViewById(R.id.tvStudentName);
        tableGrades = findViewById(R.id.tableGrades);
        tvAverage = findViewById(R.id.tvAverage);
        btnAddGrade = findViewById(R.id.btnAddGrade);
        btnDeleteStudent = findViewById(R.id.btnDeleteStudent);
        
        // Initialize database and repository
        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        repository = new StudentRepository(database.studentDao());
        
        // Setup button click listeners
        btnBack.setOnClickListener(v -> finish());
        btnAddGrade.setOnClickListener(v -> showAddGradeDialog());
        btnDeleteStudent.setOnClickListener(v -> showDeleteConfirmationDialog());
        
        loadStudentDetails();
    }
    
    private void loadStudentDetails() {
        repository.getStudentById(studentId).observe(this, student -> {
            if (student == null) {
                Toast.makeText(
                    StudentDetailActivity.this,
                    R.string.toast_student_not_found,
                    Toast.LENGTH_SHORT
                ).show();
                finish();
                return;
            }
            
            currentStudent = student;
            displayStudentDetails(student);
        });
    }
    
    private void displayStudentDetails(Student student) {
        // Display student name
        tvStudentName.setText(student.getName());
        
        // Get grades list
        List<Grade> grades = converters.toGradeList(student.getGradesCsv());
        
        // Clear existing table rows
        tableGrades.removeAllViews();
        
        // Add grade rows with cards
        for (int index = 0; index < grades.size(); index++) {
            Grade grade = grades.get(index);
            
            // Create a card for each grade
            View cardView = LayoutInflater.from(this)
                .inflate(R.layout.item_grade_display, tableGrades, false);
            
            TextView tvSubject = cardView.findViewById(R.id.tvSubject);
            TextView tvScore = cardView.findViewById(R.id.tvScore);
            View scoreContainer = cardView.findViewById(R.id.scoreContainer);
            
            tvSubject.setText(grade.getSubject());
            tvScore.setText(String.valueOf(grade.getScore()));
            
            // Apply score-based gradient to score container
            android.graphics.drawable.Drawable gradient = 
                ScoreColorHelper.getGradientForScore(this, (double) grade.getScore());
            scoreContainer.setBackground(gradient);
            
            TableRow row = new TableRow(this);
            TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            );
            row.setLayoutParams(params);
            row.addView(cardView);
            tableGrades.addView(row);
        }
        
        // Calculate and display average
        double average = 0.0;
        if (!grades.isEmpty()) {
            average = grades.stream()
                .mapToInt(Grade::getScore)
                .average()
                .orElse(0.0);
        }
        
        tvAverage.setText(getString(R.string.average_format, average));
    }
    
    private void showAddGradeDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_grade, null);
        EditText etSubject = dialogView.findViewById(R.id.etDialogSubject);
        EditText etScore = dialogView.findViewById(R.id.etDialogScore);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_add_grade_title)
            .setView(dialogView)
            .setPositiveButton(R.string.add, (dialog1, which) -> {
                String subject = etSubject.getText() != null ? 
                    etSubject.getText().toString().trim() : "";
                String scoreText = etScore.getText() != null ? 
                    etScore.getText().toString().trim() : "";
                
                try {
                    int score = Integer.parseInt(scoreText);
                    
                    if (subject.isEmpty()) {
                        Toast.makeText(
                            this,
                            R.string.toast_subject_empty,
                            Toast.LENGTH_SHORT
                        ).show();
                    } else if (score < 0 || score > 100) {
                        Toast.makeText(
                            this,
                            R.string.toast_grade_invalid,
                            Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        addGrade(subject, score);
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(
                        this,
                        R.string.toast_grade_invalid,
                        Toast.LENGTH_SHORT
                    ).show();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .create();
        
        dialog.show();
    }
    
    private void addGrade(String subject, int score) {
        if (currentStudent == null) return;
        
        List<Grade> currentGrades = new ArrayList<>(converters.toGradeList(currentStudent.getGradesCsv()));
        currentGrades.add(new Grade(subject, score));
        
        String updatedGradesJson = converters.fromGradeList(currentGrades);
        Student updatedStudent = new Student();
        updatedStudent.setId(currentStudent.getId());
        updatedStudent.setName(currentStudent.getName());
        updatedStudent.setGradesCsv(updatedGradesJson);
        
        repository.updateStudent(updatedStudent, new StudentRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(
                    StudentDetailActivity.this,
                    R.string.toast_grade_added,
                    Toast.LENGTH_SHORT
                ).show();
                
                loadStudentDetails();
            }
            
            @Override
            public void onError(Exception error) {
                Toast.makeText(
                    StudentDetailActivity.this,
                    "Error adding grade: " + error.getMessage(),
                    Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
    
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(R.string.dialog_delete_message)
            .setPositiveButton(R.string.delete, (dialog, which) -> deleteStudent())
            .setNegativeButton(R.string.cancel, null)
            .create()
            .show();
    }
    
    private void deleteStudent() {
        if (currentStudent == null) return;
        
        repository.deleteStudent(currentStudent, new StudentRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(
                    StudentDetailActivity.this,
                    R.string.toast_student_deleted,
                    Toast.LENGTH_SHORT
                ).show();
                
                finish();
            }
            
            @Override
            public void onError(Exception error) {
                Toast.makeText(
                    StudentDetailActivity.this,
                    "Error deleting student: " + error.getMessage(),
                    Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}

