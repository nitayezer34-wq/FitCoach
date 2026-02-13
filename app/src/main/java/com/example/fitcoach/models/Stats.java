package com.example.fitcoach.models;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Stats {

    private LocalDate date;
    private int steps;
    private double calories;
    private double water;
    private List<WorkoutTraining> workoutTrainings;


    public Stats() {
        date = LocalDate.now();
        steps = 0;
        calories = 0;
        water = 0;
        workoutTrainings = new ArrayList<>();
    }

    public Stats(LocalDate date, int steps, double calories, double water,
                 List<WorkoutTraining> workoutTrainings) {
        this.date = date;
        this.steps = steps;
        this.calories = calories;
        this.water = water;
        this.workoutTrainings = workoutTrainings;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
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
                "date=" + date +
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
        return this.date.equals(LocalDate.now());
    }
}