package com.example.student_grade.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.student_grade.R
import com.example.student_grade.database.Converters
import com.example.student_grade.database.Student
import com.example.student_grade.utils.ScoreColorHelper

class StudentAdapter(
    private val students: ArrayList<Student>,
    private val onItemClick: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {
    
    private val converters = Converters()
    
    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val nameTextView: TextView = itemView.findViewById(R.id.tvStudentName)
        val gradeCountTextView: TextView = itemView.findViewById(R.id.tvGradeCount)
        val averageTextView: TextView = itemView.findViewById(R.id.tvAverage)
        val scoreContainer: LinearLayout = itemView.findViewById(R.id.scoreContainer)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        val grades = converters.toGradeList(student.gradesCsv)
        
        holder.nameTextView.text = student.name
        holder.gradeCountTextView.text = holder.itemView.context.getString(
            R.string.grade_count_format,
            grades.size
        )
        
        // Calculate and display average
        val average = if (grades.isNotEmpty()) {
            grades.map { it.score }.average()
        } else {
            0.0
        }
        
        holder.averageTextView.text = holder.itemView.context.getString(
            R.string.average_format,
            average
        )
        
        // Apply score-based gradient to score container
        val gradient = ScoreColorHelper.getGradientForScore(holder.itemView.context, average)
        holder.scoreContainer.background = gradient
        
        holder.cardView.setOnClickListener {
            onItemClick(student)
        }
    }
    
    override fun getItemCount(): Int = students.size
    
    fun updateStudents(newStudents: List<Student>) {
        students.clear()
        students.addAll(newStudents)
        notifyDataSetChanged()
    }
}

