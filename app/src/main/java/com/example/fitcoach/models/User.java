package com.example.fitcoach.models;

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
    private int dailyStepTarget;
    private int dailyWaterTargetMl;

    public User() {}

    public User(String id, String name, String email, String password, String gender, int birthYear,
                int heightCm, float weightKg, String activityLevel, int dailyStepTarget, int dailyWaterTargetMl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.birthYear = birthYear;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.activityLevel = activityLevel;
        this.dailyStepTarget = dailyStepTarget;
        this.dailyWaterTargetMl = dailyWaterTargetMl;
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

    public int getDailyStepTarget() {
        return dailyStepTarget;
    }

    public void setDailyStepTarget(int dailyStepTarget) {
        this.dailyStepTarget = dailyStepTarget;
    }

    public int getDailyWaterTargetMl() {
        return dailyWaterTargetMl;
    }

    public void setDailyWaterTargetMl(int dailyWaterTargetMl) {
        this.dailyWaterTargetMl = dailyWaterTargetMl;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", gender='" + gender + '\'' +
                ", birthYear=" + birthYear +
                ", heightCm=" + heightCm +
                ", weightKg=" + weightKg +
                ", activityLevel='" + activityLevel + '\'' +
                ", dailyStepTarget=" + dailyStepTarget +
                ", dailyWaterTargetMl=" + dailyWaterTargetMl +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return birthYear == user.birthYear && heightCm == user.heightCm && Float.compare(weightKg, user.weightKg) == 0 && dailyStepTarget == user.dailyStepTarget && dailyWaterTargetMl == user.dailyWaterTargetMl && Objects.equals(id, user.id) && Objects.equals(name, user.name) && Objects.equals(email, user.email) && Objects.equals(password, user.password) && Objects.equals(gender, user.gender) && Objects.equals(activityLevel, user.activityLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, password, gender, birthYear, heightCm, weightKg, activityLevel, dailyStepTarget, dailyWaterTargetMl);
    }
}
