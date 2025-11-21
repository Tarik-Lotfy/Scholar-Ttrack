package com.example.student_grade.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.student_grade.R;
import com.example.student_grade.database.Converters;
import com.example.student_grade.database.Grade;
import com.example.student_grade.database.Student;
import com.example.student_grade.utils.ScoreColorHelper;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {
    
    private final ArrayList<StudentAveragePair> students = new ArrayList<>();
    private int startRank = 4;
    private final Converters converters = new Converters();
    
    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank;
        TextView tvStudentName;
        TextView tvGradeCount;
        TextView tvAverage;
        LinearLayout scoreContainer;
        
        LeaderboardViewHolder(View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvGradeCount = itemView.findViewById(R.id.tvGradeCount);
            tvAverage = itemView.findViewById(R.id.tvAverage);
            scoreContainer = itemView.findViewById(R.id.scoreContainer);
        }
    }
    
    @Override
    public LeaderboardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(LeaderboardViewHolder holder, int position) {
        StudentAveragePair pair = students.get(position);
        Student student = pair.getStudent();
        double average = pair.getAverage();
        int rank = startRank + position;
        List<Grade> grades = converters.toGradeList(student.getGradesCsv());
        
        holder.tvRank.setText(holder.itemView.getContext().getString(R.string.rank_format, rank));
        holder.tvStudentName.setText(student.getName());
        holder.tvGradeCount.setText(holder.itemView.getContext().getString(
            R.string.grade_count_format,
            grades.size()
        ));
        holder.tvAverage.setText(holder.itemView.getContext().getString(
            R.string.average_format,
            average
        ));
        
        // Apply score-based gradient to score container
        android.graphics.drawable.Drawable gradient = 
            ScoreColorHelper.getGradientForScore(holder.itemView.getContext(), average);
        holder.scoreContainer.setBackground(gradient);
        
        // Apply score-based gradient to rank badge
        android.graphics.drawable.Drawable rankGradient = 
            ScoreColorHelper.getGradientForScore(holder.itemView.getContext(), average);
        holder.tvRank.setBackground(rankGradient);
    }
    
    @Override
    public int getItemCount() {
        return students.size();
    }
    
    public void updateLeaderboard(List<StudentAveragePair> newStudents, int startRank) {
        this.startRank = startRank;
        students.clear();
        if (newStudents != null) {
            students.addAll(newStudents);
        }
        notifyDataSetChanged();
    }
}

