package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.adapters.WorkoutAdapter;
import com.example.fitcoach.models.WorkoutTraining;
import com.example.fitcoach.services.DatabaseService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AdminWorkoutManagementActivity extends AppCompatActivity {

    private RecyclerView underweightRecyclerView, normalRecyclerView, overweightRecyclerView;
    private WorkoutAdapter underweightAdapter, normalAdapter, overweightAdapter;
    private final List<WorkoutTraining> underweightWorkouts = new ArrayList<>();
    private final List<WorkoutTraining> normalWorkouts = new ArrayList<>();
    private final List<WorkoutTraining> overweightWorkouts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_workout_management);

        initViews();
        setupRecyclerViews();
        loadWorkouts();

        FloatingActionButton fabAddWorkout = findViewById(R.id.fab_add_workout);
        fabAddWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminWorkoutManagementActivity.this, WorkoutCreationActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        underweightRecyclerView = findViewById(R.id.rv_underweight_workouts);
        normalRecyclerView = findViewById(R.id.rv_normal_workouts);
        overweightRecyclerView = findViewById(R.id.rv_overweight_workouts);
    }

    private void setupRecyclerViews() {
        underweightAdapter = new WorkoutAdapter(underweightWorkouts);
        normalAdapter = new WorkoutAdapter(normalWorkouts);
        overweightAdapter = new WorkoutAdapter(overweightWorkouts);

        underweightRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        normalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        overweightRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        underweightRecyclerView.setAdapter(underweightAdapter);
        normalRecyclerView.setAdapter(normalAdapter);
        overweightRecyclerView.setAdapter(overweightAdapter);
    }

    private void loadWorkouts() {
        DatabaseService.getInstance().getWorkoutTrainingList(new DatabaseService.DatabaseCallback<List<WorkoutTraining>>() {
            @Override
            public void onCompleted(List<WorkoutTraining> workouts) {
                if (workouts != null) {
                    categorizeWorkouts(workouts);
                    underweightAdapter.notifyDataSetChanged();
                    normalAdapter.notifyDataSetChanged();
                    overweightAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AdminWorkoutManagementActivity.this, "Failed to load workouts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void categorizeWorkouts(List<WorkoutTraining> workouts) {
        underweightWorkouts.clear();
        normalWorkouts.clear();
        overweightWorkouts.clear();

        for (WorkoutTraining workout : workouts) {
            if(workout.getTargetAudience().contains("תת משקל")) {
                underweightWorkouts.add(workout);
                continue;
            }
            if (workout.getTargetAudience().contains("משקל תקין")) {
                normalWorkouts.add(workout);
                continue;
            }
            if (workout.getTargetAudience().contains("עודף משקל")) {
                overweightWorkouts.add(workout);
                continue;
            }
        }
    }
}
