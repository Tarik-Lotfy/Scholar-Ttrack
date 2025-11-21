package com.example.student_grade.database;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudentRepository {
    
    private final StudentDao studentDao;
    private final ExecutorService executor;
    private final Handler mainHandler;
    
    public StudentRepository(StudentDao studentDao) {
        this.studentDao = studentDao;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public LiveData<List<Student>> getAllStudents() {
        return studentDao.getAllStudents();
    }
    
    public LiveData<Student> getStudentById(int studentId) {
        return studentDao.getStudentById(studentId);
    }
    
    public void insertStudent(Student student, RepositoryCallback<Long> callback) {
        executor.execute(() -> {
            try {
                Long id = studentDao.insertStudent(student);
                if (callback != null) {
                    mainHandler.post(() -> callback.onSuccess(id));
                }
            } catch (Exception e) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onError(e));
                }
            }
        });
    }
    
    public void updateStudent(Student student, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                studentDao.updateStudent(student);
                if (callback != null) {
                    mainHandler.post(() -> callback.onSuccess(null));
                }
            } catch (Exception e) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onError(e));
                }
            }
        });
    }
    
    public void deleteStudent(Student student, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                studentDao.deleteStudent(student);
                if (callback != null) {
                    mainHandler.post(() -> callback.onSuccess(null));
                }
            } catch (Exception e) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onError(e));
                }
            }
        });
    }
    
    // Callback interface for async operations
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }
}

