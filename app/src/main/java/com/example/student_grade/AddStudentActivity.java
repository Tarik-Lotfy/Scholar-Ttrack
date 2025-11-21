package com.example.student_grade;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.student_grade.database.AppDatabase;
import com.example.student_grade.database.Converters;
import com.example.student_grade.database.Grade;
import com.example.student_grade.database.Student;
import com.example.student_grade.database.StudentRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class AddStudentActivity extends AppCompatActivity {
    
    private BottomNavigationView bottomNavigation;
    private TextInputEditText etStudentName;
    private LinearLayout gradesContainer;
    private MaterialButton btnAddGrade;
    private MaterialButton btnSaveStudent;
    private StudentRepository repository;
    private final Converters converters = new Converters();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);
        
        // Initialize views
        bottomNavigation = findViewById(R.id.bottomNavigation);
        etStudentName = findViewById(R.id.etStudentName);
        gradesContainer = findViewById(R.id.gradesContainer);
        btnAddGrade = findViewById(R.id.btnAddGrade);
        btnSaveStudent = findViewById(R.id.btnSaveStudent);
        
        // Setup bottom navigation
        bottomNavigation.setSelectedItemId(R.id.nav_add_student);
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_add_student) {
                // Already here
                return true;
            } else if (item.getItemId() == R.id.nav_leaderboard) {
                startActivity(new Intent(this, LeaderboardActivity.class));
                return false;
            } else {
                return false;
            }
        });
        
        // Initialize database and repository
        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        repository = new StudentRepository(database.studentDao());
        
        // Add first grade input by default
        addGradeInput();
        
        // Setup button click listeners
        btnAddGrade.setOnClickListener(v -> addGradeInput());
        btnSaveStudent.setOnClickListener(v -> saveStudent());
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    private void addGradeInput() {
        View gradeView = LayoutInflater.from(this)
            .inflate(R.layout.item_grade_input, gradesContainer, false);
        
        ImageButton btnRemove = gradeView.findViewById(R.id.btnRemove);
        
        // Only show remove button if there's more than one grade
        btnRemove.setOnClickListener(v -> {
            if (gradesContainer.getChildCount() > 1) {
                gradesContainer.removeView(gradeView);
            } else {
                Toast.makeText(this, R.string.toast_grades_empty, Toast.LENGTH_SHORT).show();
            }
        });
        
        gradesContainer.addView(gradeView);
    }
    
    private void saveStudent() {
        String name = etStudentName.getText() != null ? 
            etStudentName.getText().toString().trim() : "";
        
        // Validate name
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.toast_name_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Collect all grades
        List<Grade> grades = new ArrayList<>();
        
        for (int i = 0; i < gradesContainer.getChildCount(); i++) {
            View gradeView = gradesContainer.getChildAt(i);
            TextInputEditText etSubject = gradeView.findViewById(R.id.etSubject);
            TextInputEditText etScore = gradeView.findViewById(R.id.etScore);
            
            String subject = etSubject.getText() != null ? 
                etSubject.getText().toString().trim() : "";
            String scoreText = etScore.getText() != null ? 
                etScore.getText().toString().trim() : "";
            
            // Check if both fields are filled
            if (subject.isEmpty() && scoreText.isEmpty()) {
                continue; // Skip empty grade fields
            }
            
            if (subject.isEmpty()) {
                Toast.makeText(this, R.string.toast_subject_empty, Toast.LENGTH_SHORT).show();
                etSubject.requestFocus();
                return;
            }
            
            try {
                int score = Integer.parseInt(scoreText);
                if (score < 0 || score > 100) {
                    Toast.makeText(this, R.string.toast_grades_invalid, Toast.LENGTH_SHORT).show();
                    etScore.requestFocus();
                    return;
                }
                grades.add(new Grade(subject, score));
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.toast_grades_invalid, Toast.LENGTH_SHORT).show();
                etScore.requestFocus();
                return;
            }
        }
        
        // Validate that at least one grade was entered
        if (grades.isEmpty()) {
            Toast.makeText(this, R.string.toast_grades_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Convert grades to JSON string
        String gradesJson = converters.fromGradeList(grades);
        Student student = new Student();
        student.setName(name);
        student.setGradesCsv(gradesJson);
        
        // Insert into database
        repository.insertStudent(student, new StudentRepository.RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                Toast.makeText(
                    AddStudentActivity.this,
                    R.string.toast_student_saved,
                    Toast.LENGTH_SHORT
                ).show();
                
                finish();
            }
            
            @Override
            public void onError(Exception error) {
                Toast.makeText(
                    AddStudentActivity.this,
                    "Error saving student: " + error.getMessage(),
                    Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}

