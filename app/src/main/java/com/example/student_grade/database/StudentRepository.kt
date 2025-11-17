package com.example.student_grade.database

class StudentRepository(private val studentDao: StudentDao) {
    
    suspend fun getAllStudents(): List<Student> {
        return studentDao.getAllStudents()
    }
    
    suspend fun getStudentById(studentId: Int): Student? {
        return studentDao.getStudentById(studentId)
    }
    
    suspend fun insertStudent(student: Student): Long {
        return studentDao.insertStudent(student)
    }
    
    suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(student)
    }
    
    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
    }
}

