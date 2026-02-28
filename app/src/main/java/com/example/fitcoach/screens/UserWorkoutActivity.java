package com.example.fitcoach.screens;

import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.adapters.UserWorkoutAdapter;
import com.example.fitcoach.models.WorkoutTraining;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class UserWorkoutActivity extends AppCompatActivity implements UserWorkoutAdapter.OnWorkoutDoneListener {

    private RecyclerView rvUserWorkouts;
    private UserWorkoutAdapter adapter;
    private List<WorkoutTraining> workoutList;
    private int initialWorkoutCount;
    private int caloriesBurned = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_workout);

        rvUserWorkouts = findViewById(R.id.rv_user_workouts);
        rvUserWorkouts.setLayoutManager(new LinearLayoutManager(this));

        // Initialize with dummy data for now
        workoutList = new ArrayList<>();
        workoutList.add(new WorkoutTraining(null, "Push Ups", "Description for Push Ups", 50, 3, 12, 1.5, null));
        workoutList.add(new WorkoutTraining(null, "Squats", "Description for Squats", 50, 3, 15, 1.5, null));
        workoutList.add(new WorkoutTraining(null, "Plank", "Description for Plank", 50, 3, 60, 1.5, null));
        initialWorkoutCount = workoutList.size();

        adapter = new UserWorkoutAdapter(this, workoutList, this);
        rvUserWorkouts.setAdapter(adapter);
    }

    @Override
    public void onWorkoutDone(int position) {
        WorkoutTraining workout = workoutList.get(position);
        caloriesBurned += workout.getTotalExerciseCalories();
        
        workoutList.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, workoutList.size());

        // Show encouragement message
        if (workoutList.size() > 1) {
            Toast.makeText(this, "כל הכבוד, אלוף! המשך כך", Toast.LENGTH_SHORT).show();
        } else if (workoutList.size() == 1) {
            Toast.makeText(this, "רק עוד קצת!", Toast.LENGTH_SHORT).show();
        } else {
            // Last workout finished
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String userName = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "";
            String finalMessage = String.format("כל הכבוד, %s! \uD83D\uDCAA שרפת %d קלוריות \uD83C\uDF96", userName, caloriesBurned);
            Toast.makeText(this, finalMessage, Toast.LENGTH_LONG).show();

        }
    }
}
