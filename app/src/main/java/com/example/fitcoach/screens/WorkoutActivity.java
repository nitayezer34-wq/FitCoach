package com.example.fitcoach.screens;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.adapters.WorkoutAdapter;
import com.example.fitcoach.models.User;
import com.example.fitcoach.models.WorkoutTraining;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class WorkoutActivity extends AppCompatActivity implements WorkoutAdapter.OnWorkoutListener {

    private WorkoutAdapter adapter;
    private DatabaseService dbService;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        dbService = DatabaseService.getInstance();
        currentUser = SharedPreferencesUtil.getUser(this);

        RecyclerView rvWorkouts = findViewById(R.id.rv_workouts);
        rvWorkouts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new WorkoutAdapter(new ArrayList<>(), this);
        rvWorkouts.setAdapter(adapter);

        loadWorkoutsForUser();
    }

    private void loadWorkoutsForUser() {
        if (currentUser == null) return;

        String userBmiCategory = getUserBmiCategory(currentUser.calcBMI());

        dbService.getWorkoutTrainingList(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<WorkoutTraining> allWorkouts) {
                if (allWorkouts != null) {
                    List<WorkoutTraining> filteredWorkouts = new ArrayList<>();
                    adapter.setWorkoutList(filteredWorkouts);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(WorkoutActivity.this, "Failed to load workouts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getUserBmiCategory(double bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi < 25) {
            return "Normal";
        } else {
            return "Overweight";
        }
    }

    @Override
    public void onEdit(WorkoutTraining workout) {
        // TODO: Implement edit functionality
    }

    @Override
    public void onDelete(WorkoutTraining workout) {
        // TODO: Implement delete functionality
    }
}
