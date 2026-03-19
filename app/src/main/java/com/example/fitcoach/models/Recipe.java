package com.example.fitcoach.models;

import java.util.List;

public class Recipe {
    private String id;
    private String userId; // The ID of the user who created the recipe
    private String title;
    private String imageUrl;
    private int calories;
    private int prepTimeInMinutes;
    private List<String> allergens;
    private List<String> ingredients; // New: List of ingredients
    private List<String> instructions; // New: List of instructions

    // Rating fields
    private double rating; // The calculated average rating (totalRatingSum / ratingCount)
    private int ratingCount; // How many users have rated this
    private double totalRatingSum; // The sum of all ratings received

    // Empty constructor required for Firebase
    public Recipe() {
    }

    // Full constructor for creating a new recipe
    public Recipe(String id, String userId, String title, String imageUrl, int calories, int prepTimeInMinutes, List<String> allergens, List<String> ingredients, List<String> instructions) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.calories = calories;
        this.prepTimeInMinutes = prepTimeInMinutes;
        this.allergens = allergens;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.rating = 0.0; // Initial rating
        this.ratingCount = 0;
        this.totalRatingSum = 0.0;
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getPrepTimeInMinutes() {
        return prepTimeInMinutes;
    }

    public void setPrepTimeInMinutes(int prepTimeInMinutes) {
        this.prepTimeInMinutes = prepTimeInMinutes;
    }

    public List<String> getAllergens() {
        return allergens;
    }

    public void setAllergens(List<String> allergens) {
        this.allergens = allergens;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public double getTotalRatingSum() {
        return totalRatingSum;
    }

    public void setTotalRatingSum(double totalRatingSum) {
        this.totalRatingSum = totalRatingSum;
    }
}
