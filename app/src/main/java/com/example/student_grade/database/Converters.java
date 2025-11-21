package com.example.student_grade.database;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Converters {
    
    private final Gson gson = new Gson();
    
    @TypeConverter
    public String fromGradeList(List<Grade> grades) {
        return gson.toJson(grades);
    }
    
    @TypeConverter
    public List<Grade> toGradeList(String gradesString) {
        if (gradesString == null || gradesString.isEmpty()) {
            return Collections.emptyList();
        }
        Type listType = new TypeToken<List<Grade>>() {}.getType();
        List<Grade> result = gson.fromJson(gradesString, listType);
        return result != null ? result : Collections.<Grade>emptyList();
    }
    
    // Legacy support for CSV format (for backward compatibility)
    public List<Integer> fromCsvToList(String csv) {
        if (csv == null || csv.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
            .map(String::trim)
            .map(s -> {
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    return null;
                }
            })
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    public String fromListToCsv(List<Integer> grades) {
        if (grades == null || grades.isEmpty()) {
            return "";
        }
        return grades.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
    }
}

