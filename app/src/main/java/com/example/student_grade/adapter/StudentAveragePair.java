package com.example.student_grade.adapter;

import com.example.student_grade.database.Student;

public class StudentAveragePair {
    private final Student student;
    private final Double average;
    
    public StudentAveragePair(Student student, Double average) {
        this.student = student;
        this.average = average;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public Double getAverage() {
        return average;
    }
}

