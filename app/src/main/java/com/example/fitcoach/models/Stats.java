package com.example.fitcoach.models;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Stats {

    private String date;
    private int steps;
    private double calories;
    private double water;
    private List<WorkoutTraining> workoutTrainings;


    public Stats() {
        this.date = getCurrentDateString();
        this.steps = 0;
        this.calories = 0;
        this.water = 0;
        this.workoutTrainings = new ArrayList<>();
    }

    public Stats(String date, int steps, double calories, double water,
                 List<WorkoutTraining> workoutTrainings) {
        this.date = date;
        this.steps = steps;
        this.calories = calories;
        this.water = water;
        this.workoutTrainings = workoutTrainings;
    }

    private String getCurrentDateString() {
        Calendar c = Calendar.getInstance();
        return String.format("%d-%02d-%02d", 
            c.get(Calendar.YEAR), 
            c.get(Calendar.MONTH) + 1, 
            c.get(Calendar.DAY_OF_MONTH));
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getWater() {
        return water;
    }

    public void setWater(double water) {
        this.water = water;
    }

    public List<WorkoutTraining> getWorkoutTrainings() {
        return workoutTrainings;
    }

    public void setWorkoutTrainings(List<WorkoutTraining> workoutTrainings) {
        this.workoutTrainings = workoutTrainings;
    }

    @NonNull
    @Override
    public String toString() {
        return "Stats{" +
                "date='" + date + '\'' +
                ", steps=" + steps +
                ", calories=" + calories +
                ", water=" + water +
                ", workoutTrainings=" + workoutTrainings +
                '}';
    }

    public void incrementWater() {
        this.water++;
    }

    public void decrementWater() {
        this.water--;
    }

    public void addCalories(double calories) {
        this.calories += calories;
    }

    public boolean isThisToday() {
        return this.date != null && this.date.equals(getCurrentDateString());
    }
}