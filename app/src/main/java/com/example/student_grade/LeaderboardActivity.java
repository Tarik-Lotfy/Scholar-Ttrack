package com.example.student_grade;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.student_grade.adapter.LeaderboardAdapter;
import com.example.student_grade.adapter.StudentAveragePair;
import com.example.student_grade.database.AppDatabase;
import com.example.student_grade.database.Converters;
import com.example.student_grade.database.Grade;
import com.example.student_grade.database.Student;
import com.example.student_grade.database.StudentRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderboardActivity extends AppCompatActivity {
    
    private BottomNavigationView bottomNavigation;
    private LinearLayout podiumContainer;
    private LinearLayout firstPlaceContainer;
    private LinearLayout secondPlaceContainer;
    private LinearLayout thirdPlaceContainer;
    private TextView tvFirstName;
    private TextView tvFirstAverage;
    private TextView tvSecondName;
    private TextView tvSecondAverage;
    private TextView tvThirdName;
    private TextView tvThirdAverage;
    private RecyclerView recyclerViewRankings;
    private TextView tvEmptyState;
    private StudentRepository repository;
    private LeaderboardAdapter leaderboardAdapter;
    private final Converters converters = new Converters();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        
        // Initialize views
        bottomNavigation = findViewById(R.id.bottomNavigation);
        podiumContainer = findViewById(R.id.podiumContainer);
        firstPlaceContainer = findViewById(R.id.firstPlaceContainer);
        secondPlaceContainer = findViewById(R.id.secondPlaceContainer);
        thirdPlaceContainer = findViewById(R.id.thirdPlaceContainer);
        tvFirstName = findViewById(R.id.tvFirstName);
        tvFirstAverage = findViewById(R.id.tvFirstAverage);
        tvSecondName = findViewById(R.id.tvSecondName);
        tvSecondAverage = findViewById(R.id.tvSecondAverage);
        tvThirdName = findViewById(R.id.tvThirdName);
        tvThirdAverage = findViewById(R.id.tvThirdAverage);
        recyclerViewRankings = findViewById(R.id.recyclerViewRankings);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        
        // Initialize database and repository
        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        repository = new StudentRepository(database.studentDao());
        
        // Setup RecyclerView
        leaderboardAdapter = new LeaderboardAdapter();
        recyclerViewRankings.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRankings.setAdapter(leaderboardAdapter);
        
        // Setup bottom navigation
        bottomNavigation.setSelectedItemId(R.id.nav_leaderboard);
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_add_student) {
                startActivity(new Intent(this, AddStudentActivity.class));
                return false;
            } else if (item.getItemId() == R.id.nav_leaderboard) {
                // Already here
                return true;
            } else {
                return false;
            }
        });
        
        loadLeaderboard();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.setSelectedItemId(R.id.nav_leaderboard);
        loadLeaderboard();
    }
    
    private void loadLeaderboard() {
        repository.getAllStudents().observe(this, students -> {
            if (students == null || students.isEmpty()) {
                // Show empty state
                tvEmptyState.setVisibility(View.VISIBLE);
                podiumContainer.setVisibility(View.GONE);
                recyclerViewRankings.setVisibility(View.GONE);
                return;
            }
            
            tvEmptyState.setVisibility(View.GONE);
            podiumContainer.setVisibility(View.VISIBLE);
            recyclerViewRankings.setVisibility(View.VISIBLE);
            
            // Calculate averages and sort by average descending
            List<StudentAveragePair> studentsWithAverages = students.stream()
                .map(student -> {
                    List<Grade> grades = converters.toGradeList(student.getGradesCsv());
                    double average;
                    if (grades.isEmpty()) {
                        average = 0.0;
                    } else {
                        average = grades.stream()
                            .mapToInt(Grade::getScore)
                            .average()
                            .orElse(0.0);
                    }
                    return new StudentAveragePair(student, average);
                })
                .sorted(Comparator.comparing(StudentAveragePair::getAverage).reversed())
                .collect(Collectors.toList());
            
            // Display top 3 in podium
            if (!studentsWithAverages.isEmpty()) {
                // First place
                firstPlaceContainer.setVisibility(View.VISIBLE);
                tvFirstName.setText(studentsWithAverages.get(0).getStudent().getName());
                tvFirstAverage.setText(getString(R.string.average_format, studentsWithAverages.get(0).getAverage()));
            } else {
                firstPlaceContainer.setVisibility(View.GONE);
            }
            
            if (studentsWithAverages.size() > 1) {
                // Second place
                secondPlaceContainer.setVisibility(View.VISIBLE);
                tvSecondName.setText(studentsWithAverages.get(1).getStudent().getName());
                tvSecondAverage.setText(getString(R.string.average_format, studentsWithAverages.get(1).getAverage()));
            } else {
                secondPlaceContainer.setVisibility(View.GONE);
            }
            
            if (studentsWithAverages.size() > 2) {
                // Third place
                thirdPlaceContainer.setVisibility(View.VISIBLE);
                tvThirdName.setText(studentsWithAverages.get(2).getStudent().getName());
                tvThirdAverage.setText(getString(R.string.average_format, studentsWithAverages.get(2).getAverage()));
            } else {
                thirdPlaceContainer.setVisibility(View.GONE);
            }
            
            // Display ALL students in RecyclerView with rankings
            List<StudentAveragePair> remainingStudents = studentsWithAverages.size() > 3 ?
                studentsWithAverages.subList(3, studentsWithAverages.size()) :
                new ArrayList<>();
            leaderboardAdapter.updateLeaderboard(remainingStudents, 4);
        });
    }
}

