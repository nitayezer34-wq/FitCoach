package com.example.fitcoach.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fitcoach.models.Recipe;
import com.example.fitcoach.models.User;
import com.example.fitcoach.models.WorkoutTraining;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public class DatabaseService {
    private static final String
            USERS_PATH = "users",
            WORKOUT_PATH = "workouts",
            RECIPES_PATH = "recipes";

    private static final String DB_URL = "https://fitcoach-55d45-default-rtdb.europe-west1.firebasedatabase.app/";
    private static DatabaseService instance;
    private final DatabaseReference databaseReference;
    private DatabaseService() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(DB_URL);
        databaseReference = firebaseDatabase.getReference();
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    // ... (generic methods: writeData, deleteData, etc.)
    private void writeData(@NotNull final String path, @NotNull final Object data, final @Nullable DatabaseCallback<Void> callback) {
        readData(path).setValue(data, (error, ref) -> {
            if (error != null) {
                if (callback == null) return;
                callback.onFailed(error.toException());
            } else {
                if (callback == null) return;
                callback.onCompleted(null);
            }
        });
    }

    private void deleteData(@NotNull final String path, @Nullable final DatabaseCallback<Void> callback) {
        readData(path).removeValue((error, ref) -> {
            if (error != null) {
                if (callback == null) return;
                callback.onFailed(error.toException());
            } else {
                if (callback == null) return;
                callback.onCompleted(null);
            }
        });
    }

    private DatabaseReference readData(@NotNull final String path) {
        return databaseReference.child(path);
    }

    private <T> void getData(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<T> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onFailed(task.getException());
                return;
            }
            T data = task.getResult().getValue(clazz);
            callback.onCompleted(data);
        });
    }

    private <T> void getDataList(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<List<T>> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onFailed(task.getException());
                return;
            }
            List<T> tList = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                T t = dataSnapshot.getValue(clazz);
                tList.add(t);
            });
            callback.onCompleted(tList);
        });
    }

    private String generateNewId(@NotNull final String path) {
        return databaseReference.child(path).push().getKey();
    }

    private <T> void runTransaction(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull UnaryOperator<T> function, @NotNull final DatabaseCallback<T> callback) {
        readData(path).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                T currentValue = currentData.getValue(clazz);
                if (currentValue == null) {
                    currentValue = function.apply(null);
                } else {
                    currentValue = function.apply(currentValue);
                }
                currentData.setValue(currentValue);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    callback.onFailed(error.toException());
                    return;
                }
                T result = currentData != null ? currentData.getValue(clazz) : null;
                callback.onCompleted(result);
            }
        });
    }

    // region User Section
    public String generateUserId() {
        return generateNewId(USERS_PATH);
    }

    public void createNewUser(@NotNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        writeData(USERS_PATH + "/" + user.getId(), user, callback);
    }

    public void getUser(@NotNull final String uid, @NotNull final DatabaseCallback<User> callback) {
        getData(USERS_PATH + "/" + uid, User.class, callback);
    }

    public void getUserList(@NotNull final DatabaseCallback<List<User>> callback) {
        getDataList(USERS_PATH, User.class, callback);
    }

    public void deleteUser(@NotNull final String uid, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(USERS_PATH + "/" + uid, callback);
    }

    public void getUserByEmailAndPassword(@NotNull final String email, @NotNull final String password, @NotNull final DatabaseCallback<User> callback) {
        getUserList(new DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                for (User user : users) {
                    if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                        callback.onCompleted(user);
                        return;
                    }
                }
                callback.onCompleted(null);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    public void checkIfEmailExists(@NotNull final String email, @NotNull final DatabaseCallback<Boolean> callback) {
        getUserList(new DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                for (User user : users) {
                    if (user.getEmail().equals(email)) {
                        callback.onCompleted(true);
                        return;
                    }
                }
                callback.onCompleted(false);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    public void updateUser(@NotNull final String uid, UnaryOperator<User> function, @NotNull final DatabaseCallback<User> callback) {
        runTransaction(USERS_PATH + "/" + uid, User.class, function, callback);
    }

    // region Workout Section
    public String generateWorkoutId() {
        return generateNewId(WORKOUT_PATH);
    }

    public void createNewWorkoutTraining(@NotNull final WorkoutTraining workoutTraining, @Nullable final DatabaseCallback<Void> callback) {
        writeData(WORKOUT_PATH + "/" + workoutTraining.getId(), workoutTraining, callback);
    }

    public void getWorkoutTraining(@NotNull final String id, @NotNull final DatabaseCallback<WorkoutTraining> callback) {
        getData(WORKOUT_PATH + "/" + id, WorkoutTraining.class, callback);
    }

    public void getWorkoutTrainingList(@NotNull final DatabaseCallback<List<WorkoutTraining>> callback) {
        getDataList(WORKOUT_PATH, WorkoutTraining.class, callback);
    }

    public void deleteWorkoutTraining(@NotNull final String id, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(WORKOUT_PATH + "/" + id, callback);
    }

    public String generateRecipeId() {
        return generateNewId(RECIPES_PATH);
    }

    // region Recipe Section

    public void createNewRecipe(@NotNull final Recipe recipe, @Nullable final DatabaseCallback<Void> callback) {
        writeData(RECIPES_PATH + "/" + recipe.getId(), recipe, callback);
    }

    public void getRecipeList(@NotNull final DatabaseCallback<List<Recipe>> callback) {
        getDataList(RECIPES_PATH, Recipe.class, callback);
    }

    // New method to get a single recipe
    public void getRecipe(@NotNull final String id, @NotNull final DatabaseCallback<Recipe> callback) {
        getData(RECIPES_PATH + "/" + id, Recipe.class, callback);
    }

    public void deleteRecipe(@NotNull final String id, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(RECIPES_PATH + "/" + id, callback);
    }

    // New method to update a recipe's rating
    public void updateRecipeRating(@NotNull final String recipeId, double newAverageRating, int newRatingCount, double newTotalRatingSum, @Nullable final DatabaseCallback<Void> callback) {
        Map<String, Object> ratingUpdates = new HashMap<>();
        ratingUpdates.put("rating", newAverageRating);
        ratingUpdates.put("ratingCount", newRatingCount);
        ratingUpdates.put("totalRatingSum", newTotalRatingSum);

        readData(RECIPES_PATH + "/" + recipeId).updateChildren(ratingUpdates, (error, ref) -> {
            if (error != null) {
                if (callback != null) callback.onFailed(error.toException());
            } else {
                if (callback != null) callback.onCompleted(null);
            }
        });
    }

    public interface DatabaseCallback<T> {
        void onCompleted(T object);

        void onFailed(Exception e);
    }

    // endregion
}
