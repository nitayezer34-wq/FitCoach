package com.example.fitcoach.models;

import androidx.annotation.NonNull;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private String id;
    private String name;
    private String email;
    private String password;
    private String gender;
    private int birthYear;
    private int heightCm;
    private float weightKg;
    private String activityLevel;
    private int dailyStepsTarget;
    private int dailyCaloriesTarget; // Added field
    private int dailyWaterTargetMl;
    private boolean admin;

    public User() {
    }

    // Constructor updated to include dailyCaloriesTarget
    public User(String id, String name, String email, String password, String gender, int birthYear,
                int heightCm, float weightKg, String activityLevel, int dailyStepsTarget, int dailyCaloriesTarget, int dailyWaterTargetMl, boolean admin) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.birthYear = birthYear;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.activityLevel = activityLevel;
        this.dailyStepsTarget = dailyStepsTarget;
        this.dailyCaloriesTarget = dailyCaloriesTarget;
        this.dailyWaterTargetMl = dailyWaterTargetMl;
        this.admin = admin;
    }

    // Getters and Setters

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public int getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(int heightCm) {
        this.heightCm = heightCm;
    }

    public float getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(float weightKg) {
        this.weightKg = weightKg;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public int getDailyStepsTarget() {
        return dailyStepsTarget;
    }

    public void setDailyStepsTarget(int dailyStepsTarget) {
        this.dailyStepsTarget = dailyStepsTarget;
    }

    public int getDailyCaloriesTarget() {
        return dailyCaloriesTarget;
    }

    public void setDailyCaloriesTarget(int dailyCaloriesTarget) {
        this.dailyCaloriesTarget = dailyCaloriesTarget;
    }

    public int getDailyWaterTargetMl() {
        return dailyWaterTargetMl;
    }

    public void setDailyWaterTargetMl(int dailyWaterTargetMl) {
        this.dailyWaterTargetMl = dailyWaterTargetMl;
    }

    @PropertyName("admin")
    public boolean isAdmin() {
        return admin;
    }

    @PropertyName("admin")
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public double calcBMI() {
        if (this.heightCm <= 0) return 0;
        double heightInMeters = this.heightCm / 100.0;
        return this.weightKg / (heightInMeters * heightInMeters);
    }
}
