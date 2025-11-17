package com.example.student_grade.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface StudentDao {
    
    @Query("SELECT * FROM students ORDER BY name ASC")
    suspend fun getAllStudents(): List<Student>
    
    @Query("SELECT * FROM students WHERE id = :studentId")
    suspend fun getStudentById(studentId: Int): Student?
    
    @Insert
    suspend fun insertStudent(student: Student): Long
    
    @Update
    suspend fun updateStudent(student: Student)
    
    @Delete
    suspend fun deleteStudent(student: Student)
}

