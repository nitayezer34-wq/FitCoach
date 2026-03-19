package com.example.fitcoach.screens;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.adapters.UserWorkoutAdapter;
import com.example.fitcoach.models.Stats;
import com.example.fitcoach.models.User;
import com.example.fitcoach.models.WeightCategory;
import com.example.fitcoach.models.WorkoutTraining;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class UserWorkoutsActivity extends AppCompatActivity implements UserWorkoutAdapter.OnWorkoutDoneListener {

    private static final String TAG = "UserWorkoutsActivity";

    private UserWorkoutAdapter adapter;
    private List<WorkoutTraining> workoutList;
    private TextView workoutHeaderTitle;
    private int caloriesBurnedInSession = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_workouts);

        SharedPreferencesUtil.checkAndClearCompletedWorkouts(this);

        RecyclerView recyclerView = findViewById(R.id.workouts_recycler_view);
        workoutHeaderTitle = findViewById(R.id.workout_header_title);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        workoutList = new ArrayList<>();
        adapter = new UserWorkoutAdapter(this, workoutList, this);
        recyclerView.setAdapter(adapter);

        loadWorkouts();
    }

    private void loadWorkouts() {
        User user = SharedPreferencesUtil.getUser(this);
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userName = (user.getName() != null && !user.getName().isEmpty()) ? user.getName() : "";
        workoutHeaderTitle.setText(String.format(Locale.getDefault(), "האימון שלך, %s", userName));

        WeightCategory userWeightCategory = user.getWeightCategory();
        List<String> completedWorkoutIds = SharedPreferencesUtil.getCompletedWorkouts(this);

        DatabaseService.getInstance().getWorkoutTrainingList(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<WorkoutTraining> workoutTrainings) {
                if (userWeightCategory != null) {
                    workoutTrainings.removeIf(workoutTraining ->
                            !Objects.equals(workoutTraining.getTargetAudience(), userWeightCategory));
                }

                workoutTrainings.removeIf(workout -> completedWorkoutIds.contains(workout.getId()));

                workoutList.clear();
                workoutList.addAll(workoutTrainings);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserWorkoutsActivity.this, "Failed to load workouts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onWorkoutDone(int position) {
        if (position < 0 || position >= workoutList.size()) {
            return;
        }

        WorkoutTraining workout = workoutList.get(position);
        
        // החישוב: קלוריות לסט * מספר סטים
        int workoutCalories = workout.getCaloriesPerSet() * workout.getSets();
        caloriesBurnedInSession += workoutCalories;

        // עדכון הסטטיסטיקה הכללית של המשתמש
        Stats stats = SharedPreferencesUtil.getStats(this);
        if (stats == null) stats = new Stats();
        
        // הוספת הקלוריות לערך הקיים
        stats.setCalories(stats.getCalories() + workoutCalories);
        
        // שמירה מקומית
        SharedPreferencesUtil.saveStats(this, stats);
        
        // סנכרון עם Firebase כדי ש-MainActivity יראה את זה
        String userId = SharedPreferencesUtil.getUserId(this);
        if (userId != null) {
            DatabaseService.getInstance().saveStats(userId, stats, null);
        }

        // סימון האימון כבוצע כדי שלא יופיע שוב היום
        SharedPreferencesUtil.addCompletedWorkout(this, workout.getId());

        // עדכון הרשימה במסך
        workoutList.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, workoutList.size());

        if (workoutList.isEmpty()) {
            User user = SharedPreferencesUtil.getUser(this);
            String userName = (user != null && user.getName() != null) ? user.getName() : "";
            String finalMessage = String.format(Locale.getDefault(), "כל הכבוד, %s! 💪 שרפת %d קלוריות באימון 🏅", userName, caloriesBurnedInSession);
            Toast.makeText(this, finalMessage, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "מעולה! שרפת " + workoutCalories + " 💪", Toast.LENGTH_SHORT).show();
        }
    }
}
