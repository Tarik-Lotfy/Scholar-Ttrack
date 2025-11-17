package com.example.student_grade.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromGradeList(grades: List<Grade>): String {
        return gson.toJson(grades)
    }
    
    @TypeConverter
    fun toGradeList(gradesString: String): List<Grade> {
        if (gradesString.isEmpty()) return emptyList()
        val type = object : TypeToken<List<Grade>>() {}.type
        return gson.fromJson(gradesString, type)
    }
    
    // Legacy support for CSV format (for backward compatibility)
    fun fromCsvToList(csv: String): List<Int> {
        if (csv.isEmpty()) return emptyList()
        return csv.split(",").mapNotNull { it.trim().toIntOrNull() }
    }
    
    fun fromListToCsv(grades: List<Int>): String {
        return grades.joinToString(",")
    }
}

