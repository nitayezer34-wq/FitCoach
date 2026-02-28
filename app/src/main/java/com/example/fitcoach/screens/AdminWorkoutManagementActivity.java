package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.adapters.WorkoutAdapter;
import com.example.fitcoach.models.WeightCategory;
import com.example.fitcoach.models.WorkoutTraining;
import com.example.fitcoach.services.DatabaseService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdminWorkoutManagementActivity extends AppCompatActivity implements WorkoutAdapter.OnWorkoutListener {

    private final List<WorkoutTraining> allWorkouts = new ArrayList<>();
    private final List<WorkoutTraining> filteredWorkouts = new ArrayList<>();
    private RecyclerView workoutsRecyclerView;
    private WorkoutAdapter workoutAdapter;
    private Spinner filterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_workout_management);

        initViews();
        setupSpinner();
        setupRecyclerView();

        FloatingActionButton fabAddWorkout = findViewById(R.id.fab_add_workout);
        fabAddWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminWorkoutManagementActivity.this, WorkoutCreationActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWorkouts();
    }

    private void initViews() {
        workoutsRecyclerView = findViewById(R.id.rv_workouts);
        filterSpinner = findViewById(R.id.spinner_filter);
        // Add bottom padding to the RecyclerView
        workoutsRecyclerView.setPadding(0, 0, 0, 100);
        workoutsRecyclerView.setClipToPadding(false);
    }

    private void setupSpinner() {
        String[] filterOptions = {"כל האימונים", "תת משקל", "משקל תקין", "עודף משקל"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_custom, filterOptions);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom);
        filterSpinner.setAdapter(adapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterWorkouts(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupRecyclerView() {
        workoutAdapter = new WorkoutAdapter(filteredWorkouts, this);
        workoutsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        workoutsRecyclerView.setAdapter(workoutAdapter);
    }

    private void loadWorkouts() {
        DatabaseService.getInstance().getWorkoutTrainingList(new DatabaseService.DatabaseCallback<List<WorkoutTraining>>() {
            @Override
            public void onCompleted(List<WorkoutTraining> workouts) {
                if (workouts != null) {
                    allWorkouts.clear();
                    allWorkouts.addAll(workouts);
                    filterWorkouts(filterSpinner.getSelectedItem().toString());
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AdminWorkoutManagementActivity.this, "Failed to load workouts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterWorkouts(String category) {
        filteredWorkouts.clear();

        if ("כל האימונים".equals(category)) {
            List<WorkoutTraining> sortedList = new ArrayList<>(allWorkouts);
            // Sort by target audience to group them
            Collections.sort(sortedList, new Comparator<WorkoutTraining>() {
                @Override
                public int compare(WorkoutTraining o1, WorkoutTraining o2) {
                    return o1.getTargetAudience().compareTo(o2.getTargetAudience());
                }
            });
            filteredWorkouts.addAll(sortedList);
        } else {
            WeightCategory targetAudienceFilter;
            switch (category) {
                case "תת משקל":
                    targetAudienceFilter = WeightCategory.UNDERWEIGHT;
                    break;
                case "משקל תקין":
                    targetAudienceFilter = WeightCategory.NORMAL;
                    break;
                case "עודף משקל":
                    targetAudienceFilter = WeightCategory.OVERWEIGHT;
                    break;
                default:
                    return; // Should not happen
            }

            for (WorkoutTraining workout : allWorkouts) {
                if (targetAudienceFilter.equals(workout.getTargetAudience())) {
                    filteredWorkouts.add(workout);
                }
            }
        }
        workoutAdapter.notifyDataSetChanged();
    }


    @Override
    public void onEdit(WorkoutTraining workout) {
        Intent intent = new Intent(this, WorkoutCreationActivity.class);
        intent.putExtra("EDIT_WORKOUT_ID", workout.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(WorkoutTraining workout) {
        DatabaseService.getInstance().deleteWorkoutTraining(workout.getId(), new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void result) {
                Toast.makeText(AdminWorkoutManagementActivity.this, "אימון נמחק בהצלחה", Toast.LENGTH_SHORT).show();
                loadWorkouts(); // Refresh the list
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AdminWorkoutManagementActivity.this, "מחיקת האימון נכשלה", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
