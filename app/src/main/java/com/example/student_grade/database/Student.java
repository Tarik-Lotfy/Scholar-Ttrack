package com.example.student_grade.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "students")
public class Student {
    @PrimaryKey(autoGenerate = true)
    private int id = 0;
    private String name;
    private String gradesCsv;
    
    // Default constructor for Room
    public Student() {
    }
    
    // Constructor with parameters
    public Student(int id, String name, String gradesCsv) {
        this.id = id;
        this.name = name;
        this.gradesCsv = gradesCsv;
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getGradesCsv() {
        return gradesCsv;
    }
    
    public void setGradesCsv(String gradesCsv) {
        this.gradesCsv = gradesCsv;
    }
}

