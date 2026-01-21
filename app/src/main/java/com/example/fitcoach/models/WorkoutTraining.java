package com.example.fitcoach.models;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.Objects;

@IgnoreExtraProperties
public class WorkoutTraining implements Serializable {

    private String id;
    private String name;              // שם התרגיל (למשל: בנץ' פרס)
    private String description;       // תיאור מפורט של אופן הביצוע
    private int caloriesPerSet;       // כמה קלוריות נשרפות בכל סט בודד
    private int sets;                 // כמות הסטים הנדרשת
    private int reps;                 // כמות החזרות בכל סט
    private int restTimeSeconds;      // זמן מנוחה בין הסטים (בשניות)

    public WorkoutTraining() {
    }

    public WorkoutTraining(String id, String name, String description, int caloriesPerSet, int sets, int reps, int restTimeSeconds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.caloriesPerSet = caloriesPerSet;
        this.sets = sets;
        this.reps = reps;
        this.restTimeSeconds = restTimeSeconds;
    }

    // --- Getters & Setters (מאפשרים גישה לנתונים ועדכון שלהם) ---


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCaloriesPerSet() { return caloriesPerSet; }
    public void setCaloriesPerSet(int caloriesPerSet) { this.caloriesPerSet = caloriesPerSet; }

    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public int getRestTimeSeconds() { return restTimeSeconds; }
    public void setRestTimeSeconds(int restTimeSeconds) { this.restTimeSeconds = restTimeSeconds; }

    /**
     * פונקציה לחישוב סך הקלוריות של התרגיל המלא
     */
    @Exclude
    public int getTotalExerciseCalories() {
        return this.sets * this.caloriesPerSet;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WorkoutTraining that = (WorkoutTraining) o;
        return caloriesPerSet == that.caloriesPerSet && sets == that.sets && reps == that.reps && restTimeSeconds == that.restTimeSeconds && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, caloriesPerSet, sets, reps, restTimeSeconds);
    }

    @NonNull
    @Override
    public String toString() {
        return "TrainingProgram{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", caloriesPerSet=" + caloriesPerSet +
                ", sets=" + sets +
                ", reps=" + reps +
                ", restTimeSeconds=" + restTimeSeconds +
                '}';
    }
}