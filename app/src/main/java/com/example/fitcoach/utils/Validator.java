package com.example.fitcoach.utils;

import java.util.regex.Pattern;

public class Validator {

    // Name pattern: allows Hebrew, English, spaces, and hyphens.
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[\\p{L} \\-']+$");

    // Password pattern: at least one digit, one lower case, one upper case, one special character, no whitespace, and at least 8 characters long
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    public static boolean isNameValid(String name) {
        if (name == null) return false;
        return NAME_PATTERN.matcher(name.trim()).matches() && name.trim().length() >= 2;
    }

    public static boolean isEmailValid(String email) {
        if (email == null) return false;
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordValid(String password) {
        if (password == null) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isGenderValid(String gender) {
        return gender != null && !gender.trim().isEmpty();
    }

    // Parsing with safe checks
    public static Integer parseBirthYear(String birthYearStr) {
        try {
            int year = Integer.parseInt(birthYearStr);
            if (year > 1900 && year <= 2026) return year;
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Integer parseHeightCm(String heightStr) {
        try {
            int height = Integer.parseInt(heightStr);
            if (height > 50 && height < 300) return height;
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Float parseWeightKg(String weightStr) {
        try {
            float weight = Float.parseFloat(weightStr);
            if (weight > 20 && weight < 500) return weight;
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Integer parseDailySteps(String stepStr) {
        try {
            int steps = Integer.parseInt(stepStr);
            if (steps >= 0 && steps <= 200000) return steps;
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Integer parseDailyWaterMl(String waterStr) {
        try {
            int water = Integer.parseInt(waterStr);
            if (water >= 0 && water <= 10000) return water;
        } catch (Exception ignored) {
        }
        return null;
    }

    public static boolean isActivityLevelValid(String level) {
        return level != null && (level.equals("Low") || level.equals("Medium") || level.equals("High"));
    }
}
