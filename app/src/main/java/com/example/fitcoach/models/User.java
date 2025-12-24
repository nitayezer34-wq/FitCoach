package com.example.fitcoach.models;

public class User {
    private String uid;
    private String name;
    private String email;
    private String gender;
    private int birthYear;
    private int heightCm;
    private float weightKg;
    private String activityLevel;
    private int dailyStepTarget;
    private int dailyWaterTargetMl;

    public User() {} // חובה ל-Firestore

    // אפשר להוסיף ctor מלא אם בא לך
    // getters & setters לכל השדות למטה

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public int getBirthYear() { return birthYear; }
    public void setBirthYear(int birthYear) { this.birthYear = birthYear; }
    public int getHeightCm() { return heightCm; }
    public void setHeightCm(int heightCm) { this.heightCm = heightCm; }
    public float getWeightKg() { return weightKg; }
    public void setWeightKg(float weightKg) { this.weightKg = weightKg; }
    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }
    public int getDailyStepTarget() { return dailyStepTarget; }
    public void setDailyStepTarget(int dailyStepTarget) { this.dailyStepTarget = dailyStepTarget; }
    public int getDailyWaterTargetMl() { return dailyWaterTargetMl; }
    public void setDailyWaterTargetMl(int dailyWaterTargetMl) { this.dailyWaterTargetMl = dailyWaterTargetMl; }
}
