package com.example.fitcoach.screens;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.adapters.WorkoutChecklistAdapter;
import com.example.fitcoach.models.User;
import com.example.fitcoach.models.WeightCategory;
import com.example.fitcoach.models.WorkoutTraining;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class WorkoutChecklistActivity extends AppCompatActivity {

    private WorkoutChecklistAdapter adapter;
    private DatabaseService dbService;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_checklist);

        dbService = DatabaseService.getInstance();
        currentUser = SharedPreferencesUtil.getUser(this);

        RecyclerView rvWorkouts = findViewById(R.id.rv_workout_checklist);
        rvWorkouts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new WorkoutChecklistAdapter(this, new ArrayList<>());
        rvWorkouts.setAdapter(adapter);

        loadWorkoutsForUser();
    }

    private void loadWorkoutsForUser() {
        if (currentUser == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        WeightCategory userWeightCategory = currentUser.getWeightCategory();

        dbService.getWorkoutTrainingList(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<WorkoutTraining> allWorkouts) {
                if (allWorkouts != null && userWeightCategory != null) {
                    List<WorkoutTraining> filteredWorkouts = new ArrayList<>();
                    for (WorkoutTraining workout : allWorkouts) {
                        if (workout.getTargetAudience() != null && workout.getTargetAudience().equals(userWeightCategory)) {
                            filteredWorkouts.add(workout);
                        }
                    }
                    adapter.setWorkoutList(filteredWorkouts);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(WorkoutChecklistActivity.this, "Failed to load workouts", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
