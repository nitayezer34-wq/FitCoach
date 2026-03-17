package com.example.fitcoach.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.example.fitcoach.models.Stats;
import com.example.fitcoach.models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SharedPreferencesUtil {
    private static final String PREF_NAME = "com.example.fitcoach.PREFERENCE_FILE_KEY";
    private static final String COMPLETED_WORKOUTS_KEY = "completed_workouts";
    private static final String LAST_CLEARED_DATE_KEY = "last_cleared_date";
    private static final String USER_KEY = "user";
    private static final String STATS_KEY = "stats";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static void saveString(Context context, String key, String value) {
        getPrefs(context).edit().putString(key, value).apply();
    }

    private static String getString(Context context, String key) {
        return getPrefs(context).getString(key, null);
    }

    private static void saveInt(Context context, String key, int value) {
        getPrefs(context).edit().putInt(key, value).apply();
    }

    private static int getInt(Context context, String key, int defaultValue) {
        return getPrefs(context).getInt(key, defaultValue);
    }

    private static void remove(Context context, String key) {
        getPrefs(context).edit().remove(key).apply();
    }

    private static boolean contains(Context context, String key) {
        return getPrefs(context).contains(key);
    }

    private static <T> void saveObject(Context context, String key, T object) {
        saveString(context, key, new Gson().toJson(object));
    }

    private static <T> T getObject(Context context, String key, Class<T> type) {
        String json = getString(context, key);
        return json == null ? null : new Gson().fromJson(json, type);
    }

    public static void saveUser(Context context, User user) {
        saveObject(context, USER_KEY, user);
    }

    public static User getUser(Context context) {
        return isUserLoggedIn(context) ? getObject(context, USER_KEY, User.class) : null;
    }

    public static void signOutUser(Context context) {
        remove(context, USER_KEY);
    }

    public static boolean isUserLoggedIn(Context context) {
        return contains(context, USER_KEY);
    }

    @Nullable
    public static String getUserId(Context context) {
        User user = getUser(context);
        return user != null ? user.getId() : null;
    }

    public static void saveStats(Context context, Stats stats) {
        saveObject(context, STATS_KEY, stats);
    }

    public static Stats getStats(Context context) {
        Stats stats = getObject(context, STATS_KEY, Stats.class);
        if (stats == null) {
            stats = new Stats();
            saveStats(context, stats);
        }
        return stats;
    }

    public static void addCompletedWorkout(Context context, String workoutId) {
        List<String> completedWorkouts = getCompletedWorkouts(context);
        if (!completedWorkouts.contains(workoutId)) {
            completedWorkouts.add(workoutId);
            saveCompletedWorkouts(context, completedWorkouts);
        }
    }

    public static void removeCompletedWorkout(Context context, String workoutId) {
        List<String> completedWorkouts = getCompletedWorkouts(context);
        if (completedWorkouts.contains(workoutId)) {
            completedWorkouts.remove(workoutId);
            saveCompletedWorkouts(context, completedWorkouts);
        }
    }

    public static List<String> getCompletedWorkouts(Context context) {
        String json = getString(context, COMPLETED_WORKOUTS_KEY);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    public static void clearCompletedWorkouts(Context context) {
        remove(context, COMPLETED_WORKOUTS_KEY);
        saveLastClearedDate(context);
    }

    private static void saveCompletedWorkouts(Context context, List<String> completedWorkouts) {
        saveString(context, COMPLETED_WORKOUTS_KEY, new Gson().toJson(completedWorkouts));
    }

    private static void saveLastClearedDate(Context context) {
        saveInt(context, LAST_CLEARED_DATE_KEY, Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
    }

    public static void checkAndClearCompletedWorkouts(Context context) {
        int lastClearedDay = getInt(context, LAST_CLEARED_DATE_KEY, -1);
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        if (lastClearedDay != currentDay) {
            clearCompletedWorkouts(context);
        }
    }
}
