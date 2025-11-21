package com.example.student_grade.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.student_grade.R;
import com.example.student_grade.database.Converters;
import com.example.student_grade.database.Grade;
import com.example.student_grade.database.Student;
import com.example.student_grade.utils.ScoreColorHelper;
import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
    
    private final ArrayList<Student> students;
    private final OnItemClickListener onItemClick;
    private final Converters converters = new Converters();
    
    public interface OnItemClickListener {
        void onItemClick(Student student);
    }
    
    public StudentAdapter(ArrayList<Student> students, OnItemClickListener onItemClick) {
        this.students = students != null ? students : new ArrayList<>();
        this.onItemClick = onItemClick;
    }
    
    static class StudentViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView nameTextView;
        TextView gradeCountTextView;
        TextView averageTextView;
        LinearLayout scoreContainer;
        
        StudentViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            nameTextView = itemView.findViewById(R.id.tvStudentName);
            gradeCountTextView = itemView.findViewById(R.id.tvGradeCount);
            averageTextView = itemView.findViewById(R.id.tvAverage);
            scoreContainer = itemView.findViewById(R.id.scoreContainer);
        }
    }
    
    @Override
    public StudentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(StudentViewHolder holder, int position) {
        Student student = students.get(position);
        List<Grade> grades = converters.toGradeList(student.getGradesCsv());
        
        holder.nameTextView.setText(student.getName());
        holder.gradeCountTextView.setText(holder.itemView.getContext().getString(
            R.string.grade_count_format,
            grades.size()
        ));
        
        // Calculate and display average
        double average;
        if (!grades.isEmpty()) {
            average = grades.stream()
                .mapToInt(Grade::getScore)
                .average()
                .orElse(0.0);
        } else {
            average = 0.0;
        }
        
        holder.averageTextView.setText(holder.itemView.getContext().getString(
            R.string.average_format,
            average
        ));
        
        // Apply score-based gradient to score container
        android.graphics.drawable.Drawable gradient = 
            ScoreColorHelper.getGradientForScore(holder.itemView.getContext(), average);
        holder.scoreContainer.setBackground(gradient);
        
        holder.cardView.setOnClickListener(v -> {
            if (onItemClick != null) {
                onItemClick.onItemClick(student);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return students.size();
    }
    
    public void updateStudents(List<Student> newStudents) {
        students.clear();
        if (newStudents != null) {
            students.addAll(newStudents);
        }
        notifyDataSetChanged();
    }
}

