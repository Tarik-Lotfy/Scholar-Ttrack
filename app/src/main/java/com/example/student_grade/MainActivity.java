package com.example.student_grade;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.student_grade.adapter.StudentAdapter;
import com.example.student_grade.database.AppDatabase;
import com.example.student_grade.database.Student;
import com.example.student_grade.database.StudentRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigation;
    private StudentAdapter studentAdapter;
    private StudentRepository repository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        // Initialize database and repository
        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        repository = new StudentRepository(database.studentDao());
        
        // Setup RecyclerView
        studentAdapter = new StudentAdapter(new ArrayList<>(), student -> openStudentDetail(student));
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(studentAdapter);
        
        // Setup bottom navigation
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                // Already on home, do nothing or refresh
                return true;
            } else if (item.getItemId() == R.id.nav_add_student) {
                Intent intent = new Intent(this, AddStudentActivity.class);
                startActivity(intent);
                return false; // Don't select this item
            } else if (item.getItemId() == R.id.nav_leaderboard) {
                Intent intent = new Intent(this, LeaderboardActivity.class);
                startActivity(intent);
                return false; // Don't select this item
            } else {
                return false;
            }
        });
        
        loadStudents();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Set home as selected when returning to this activity
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        loadStudents();
    }
    
    private void loadStudents() {
        repository.getAllStudents().observe(this, students -> {
            if (students == null || students.isEmpty()) {
                Toast.makeText(
                    MainActivity.this,
                    R.string.toast_no_students,
                    Toast.LENGTH_LONG
                ).show();
            }
            
            if (studentAdapter != null) {
                studentAdapter.updateStudents(students);
            }
        });
    }
    
    private void openStudentDetail(Student student) {
        Intent intent = new Intent(this, StudentDetailActivity.class);
        intent.putExtra("STUDENT_ID", student.getId());
        startActivity(intent);
    }
}

