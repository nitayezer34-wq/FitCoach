package com.example.fitcoach.models;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.Objects;

@IgnoreExtraProperties
public class WorkoutTraining implements Serializable {

    private String id;
    private String name;
    private String description;
    private int caloriesPerSet;
    private int sets;
    private int reps;
    private double restTimeMinutes; // Changed to double for minutes (e.g. 3.5)
    private WeightCategory targetAudience;  // Categories: Underweight, Normal, Overweight

    @Exclude
    private boolean isChecked = false;

    public WorkoutTraining() {
    }

    public WorkoutTraining(String id, String name, String description, int caloriesPerSet, int sets, int reps, double restTimeMinutes, WeightCategory targetAudience) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.caloriesPerSet = caloriesPerSet;
        this.sets = sets;
        this.reps = reps;
        this.restTimeMinutes = restTimeMinutes;
        this.targetAudience = targetAudience;
    }

    @Exclude
    public boolean isChecked() {
        return isChecked;
    }

    @Exclude
    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCaloriesPerSet() {
        return caloriesPerSet;
    }

    public void setCaloriesPerSet(int caloriesPerSet) {
        this.caloriesPerSet = caloriesPerSet;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public double getRestTimeMinutes() {
        return restTimeMinutes;
    }

    public void setRestTimeMinutes(double restTimeMinutes) {
        this.restTimeMinutes = restTimeMinutes;
    }

    public WeightCategory getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(WeightCategory targetAudience) {
        this.targetAudience = targetAudience;
    }

    @Exclude
    public int getTotalExerciseCalories() {
        return this.sets * this.caloriesPerSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkoutTraining that = (WorkoutTraining) o;
        return caloriesPerSet == that.caloriesPerSet && sets == that.sets && reps == that.reps && Double.compare(that.restTimeMinutes, restTimeMinutes) == 0 && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(targetAudience, that.targetAudience);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, caloriesPerSet, sets, reps, restTimeMinutes, targetAudience);
    }

    @NonNull
    @Override
    public String toString() {
        return "WorkoutTraining{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", targetAudience='" + targetAudience + '\'' +
                ", restMinutes=" + restTimeMinutes +
                '}';
    }
}
