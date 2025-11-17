package com.example.student_grade.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.student_grade.R
import com.example.student_grade.database.Converters
import com.example.student_grade.database.Student
import com.example.student_grade.utils.ScoreColorHelper

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {
    
    private val students = ArrayList<Pair<Student, Double>>()
    private var startRank = 4
    private val converters = Converters()
    
    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        val tvGradeCount: TextView = itemView.findViewById(R.id.tvGradeCount)
        val tvAverage: TextView = itemView.findViewById(R.id.tvAverage)
        val scoreContainer: LinearLayout = itemView.findViewById(R.id.scoreContainer)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val (student, average) = students[position]
        val rank = startRank + position
        val grades = converters.toGradeList(student.gradesCsv)
        
        holder.tvRank.text = holder.itemView.context.getString(R.string.rank_format, rank)
        holder.tvStudentName.text = student.name
        holder.tvGradeCount.text = holder.itemView.context.getString(
            R.string.grade_count_format,
            grades.size
        )
        holder.tvAverage.text = holder.itemView.context.getString(
            R.string.average_format,
            average
        )
        
        // Apply score-based gradient to score container
        val gradient = ScoreColorHelper.getGradientForScore(holder.itemView.context, average)
        holder.scoreContainer.background = gradient
        
        // Apply score-based gradient to rank badge
        val rankGradient = ScoreColorHelper.getGradientForScore(holder.itemView.context, average)
        holder.tvRank.background = rankGradient
    }
    
    override fun getItemCount(): Int = students.size
    
    fun updateLeaderboard(newStudents: List<Pair<Student, Double>>, startRank: Int) {
        this.startRank = startRank
        students.clear()
        students.addAll(newStudents)
        notifyDataSetChanged()
    }
}

