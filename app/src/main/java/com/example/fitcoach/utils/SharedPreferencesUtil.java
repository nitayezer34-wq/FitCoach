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

/// Utility class for shared preferences operations
/// Contains methods for saving and retrieving data from shared preferences
/// Also contains methods for clearing and removing data from shared preferences
///
/// @see SharedPreferences
public class SharedPreferencesUtil {
    /// The name of the shared preferences file
    ///
    /// @see Context#getSharedPreferences(String, int)
    private static final String PREF_NAME = "com.example.fitcoach.PREFERENCE_FILE_KEY";
    private static final String COMPLETED_WORKOUTS_KEY = "completed_workouts";
    private static final String LAST_CLEARED_DATE_KEY = "last_cleared_date";


    /// Save a string to shared preferences
    ///
    /// @param context The context to use
    /// @param key     The key to save the string with
    /// @param value   The string to save
    /// @see SharedPreferences.Editor#putString(String, String)
    private static void saveString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /// Get a string from shared preferences
    ///
    /// @param context      The context to use
    /// @param key          The key to get the string with
    /// @param defaultValue The default value to return if the key is not found
    /// @return The string value stored in shared preferences
    /// @see SharedPreferences#getString(String, String)
    private static String getString(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    /// Save an integer to shared preferences
    ///
    /// @param context The context to use
    /// @param key     The key to save the integer with
    /// @param value   The integer to save
    /// @see SharedPreferences.Editor#putInt(String, int)
    private static void saveInt(Context context, String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /// Get an integer from shared preferences
    ///
    /// @param context      The context to use
    /// @param key          The key to get the integer with
    /// @param defaultValue The default value to return if the key is not found
    /// @return The integer value stored in shared preferences
    /// @see SharedPreferences#getInt(String, int)
    private static int getInt(Context context, String key, int defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, defaultValue);
    }

    // Add more methods for other data types as needed

    /// Clear all data from shared preferences
    ///
    /// @param context The context to use
    /// @see SharedPreferences.Editor#clear()
    public static void clear(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    /// Remove a specific key from shared preferences
    ///
    /// @param context The context to use
    /// @param key     The key to remove
    /// @see SharedPreferences.Editor#remove(String)
    private static void remove(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    /// Check if a key exists in shared preferences
    ///
    /// @param context The context to use
    /// @param key     The key to check
    /// @return true if the key exists, false otherwise
    /// @see SharedPreferences#contains(String)
    private static boolean contains(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.contains(key);
    }

    private static <T> void saveObject(Context context, String key, T object) {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        saveString(context, key, json);
    }

    private static <T> T getObject(Context context, String key, Class<T> type) {
        String json = getString(context, key, null);
        if (json == null) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

    // Add more utility methods as needed

    /// Save a user object to shared preferences
    ///
    /// @param context The context to use
    /// @param user    The user object to save
    /// @see User
    public static void saveUser(Context context, User user) {
        saveObject(context, "user", user);
    }

    /// Get the user object from shared preferences
    ///
    /// @param context The context to use
    /// @return The user object stored in shared preferences
    /// @see User
    /// @see #isUserLoggedIn(Context)
    public static User getUser(Context context) {
        if (!isUserLoggedIn(context)) {
            return null;
        }
        return getObject(context, "user", User.class);
    }

    /// Sign out the user by removing user data from shared preferences
    ///
    /// @param context The context to use
    public static void signOutUser(Context context) {
        remove(context, "user");
    }

    /// Check if a user is logged in by checking if the user id is present in shared preferences
    ///
    /// @param context The context to use
    /// @return true if the user is logged in, false otherwise
    /// @see #contains(Context, String)
    public static boolean isUserLoggedIn(Context context) {
        return contains(context, "user");
    }

    /// Get the user id of the logged in user
    ///
    /// @param context The context to use
    /// @return The user id of the logged in user, or null if no user is logged in
    @Nullable
    public static String getUserId(Context context) {
        User user = getUser(context);
        if (user != null) {
            return user.getId();
        }
        return null;
    }


    /// Save a user object to shared preferences
    ///
    /// @param context The context to use
    /// @param stats   The stats object to save
    /// @see Stats
    public static void saveStats(Context context, Stats stats) {
        saveObject(context, "stats", stats);
    }

    /// Get the user object from shared preferences
    ///
    /// @param context The context to use
    /// @return The user object stored in shared preferences
    /// @see Stats
    /// @see #isUserLoggedIn(Context)
    public static Stats getStats(Context context) {
        if (!isUserLoggedIn(context)) {
            return null;
        }
        return getObject(context, "stats", Stats.class);
    }

    /// Sign out the user by removing user data from shared preferences
    ///
    /// @param context The context to use
    public static void clearStats(Context context) {
        remove(context, "stats");
    }

    // Methods for managing completed workouts

    public static void addCompletedWorkout(Context context, String workoutId) {
        List<String> completedWorkouts = getCompletedWorkouts(context);
        if (!completedWorkouts.contains(workoutId)) {
            completedWorkouts.add(workoutId);
            saveCompletedWorkouts(context, completedWorkouts);
        }
    }

    public static List<String> getCompletedWorkouts(Context context) {
        String json = getString(context, COMPLETED_WORKOUTS_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    public static void clearCompletedWorkouts(Context context) {
        remove(context, COMPLETED_WORKOUTS_KEY);
        saveLastClearedDate(context);
    }

    private static void saveCompletedWorkouts(Context context, List<String> completedWorkouts) {
        Gson gson = new Gson();
        String json = gson.toJson(completedWorkouts);
        saveString(context, COMPLETED_WORKOUTS_KEY, json);
    }

    private static void saveLastClearedDate(Context context) {
        Calendar calendar = Calendar.getInstance();
        saveInt(context, LAST_CLEARED_DATE_KEY, calendar.get(Calendar.DAY_OF_YEAR));
    }

    public static void checkAndClearCompletedWorkouts(Context context) {
        int lastClearedDay = getInt(context, LAST_CLEARED_DATE_KEY, -1);
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_YEAR);

        if (lastClearedDay != currentDay) {
            clearCompletedWorkouts(context);
        }
    }
}
