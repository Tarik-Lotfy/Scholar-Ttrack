package com.example.student_grade.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import com.example.student_grade.R;
import java.util.List;

public class ScoreColorHelper {
    
    /**
     * Returns the appropriate gradient drawable based on average score
     * 90-100: Gold gradient
     * 80-89: Excellent (blue-cyan)
     * 70-79: Good (cyan-teal)
     * 60-69: Average (darker cyan)
     * Below 60: Poor (muted teal-gray)
     */
    public static Drawable getGradientForScore(Context context, double average) {
        int drawableRes;
        if (average >= 90) {
            drawableRes = R.drawable.gradient_score_gold;
        } else if (average >= 80) {
            drawableRes = R.drawable.gradient_score_excellent;
        } else if (average >= 70) {
            drawableRes = R.drawable.gradient_score_good;
        } else if (average >= 60) {
            drawableRes = R.drawable.gradient_score_average;
        } else {
            drawableRes = R.drawable.gradient_score_poor;
        }
        return ContextCompat.getDrawable(context, drawableRes);
    }
    
    /**
     * Returns the appropriate color based on average score
     */
    public static int getColorForScore(Context context, double average) {
        int colorRes;
        if (average >= 90) {
            colorRes = R.color.score_gold;
        } else if (average >= 80) {
            colorRes = R.color.score_excellent;
        } else if (average >= 70) {
            colorRes = R.color.score_good;
        } else if (average >= 60) {
            colorRes = R.color.score_average;
        } else {
            colorRes = R.color.score_poor;
        }
        return ContextCompat.getColor(context, colorRes);
    }
    
    /**
     * Calculates average from list of grades
     */
    public static double calculateAverage(List<Integer> scores) {
        if (scores == null || scores.isEmpty()) {
            return 0.0;
        }
        return scores.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
    }
}

