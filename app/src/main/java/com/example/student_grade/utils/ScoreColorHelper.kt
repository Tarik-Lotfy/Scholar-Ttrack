package com.example.student_grade.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.student_grade.R

object ScoreColorHelper {
    
    /**
     * Returns the appropriate gradient drawable based on average score
     * 90-100: Gold gradient
     * 80-89: Excellent (blue-cyan)
     * 70-79: Good (cyan-teal)
     * 60-69: Average (darker cyan)
     * Below 60: Poor (muted teal-gray)
     */
    fun getGradientForScore(context: Context, average: Double): Drawable? {
        val drawableRes = when {
            average >= 90 -> R.drawable.gradient_score_gold
            average >= 80 -> R.drawable.gradient_score_excellent
            average >= 70 -> R.drawable.gradient_score_good
            average >= 60 -> R.drawable.gradient_score_average
            else -> R.drawable.gradient_score_poor
        }
        return ContextCompat.getDrawable(context, drawableRes)
    }
    
    /**
     * Returns the appropriate color based on average score
     */
    fun getColorForScore(context: Context, average: Double): Int {
        val colorRes = when {
            average >= 90 -> R.color.score_gold
            average >= 80 -> R.color.score_excellent
            average >= 70 -> R.color.score_good
            average >= 60 -> R.color.score_average
            else -> R.color.score_poor
        }
        return ContextCompat.getColor(context, colorRes)
    }
    
    /**
     * Calculates average from list of grades
     */
    fun calculateAverage(scores: List<Int>): Double {
        return if (scores.isNotEmpty()) {
            scores.average()
        } else {
            0.0
        }
    }
}

