package com.example.fitcoach.models;

public class Recipe {
    private String id;
    private String title;
    private int healthScore;

    // בנאי ריק חובה ל-Firebase
    public Recipe() {}

    // בנאי רגיל
    public Recipe(String id, String title, int healthScore) {
        this.id = id;
        this.title = title;
        this.healthScore = healthScore;
    }

    // הנה הפונקציה שחסרה לך וגורמת לשגיאה:
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // שאר ה-Getters וה-Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getHealthScore() { return healthScore; }
    public void setHealthScore(int healthScore) { this.healthScore = healthScore; }
}