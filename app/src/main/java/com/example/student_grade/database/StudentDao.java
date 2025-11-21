package com.example.student_grade.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface StudentDao {
    
    @Query("SELECT * FROM students ORDER BY name ASC")
    LiveData<List<Student>> getAllStudents();
    
    @Query("SELECT * FROM students WHERE id = :studentId")
    LiveData<Student> getStudentById(int studentId);
    
    @Insert
    long insertStudent(Student student);
    
    @Update
    void updateStudent(Student student);
    
    @Delete
    void deleteStudent(Student student);
}

