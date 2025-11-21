package com.example.student_grade.database;

public class Grade {
    private String subject;
    private int score;
    
    // Default constructor for Room/Gson
    public Grade() {
    }
    
    // Constructor with parameters
    public Grade(String subject, int score) {
        this.subject = subject;
        this.score = score;
    }
    
    // Getters and setters
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
}

