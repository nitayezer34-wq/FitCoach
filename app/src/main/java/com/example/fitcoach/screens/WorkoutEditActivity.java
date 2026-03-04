package com.example.fitcoach.screens;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcoach.R;
import com.example.fitcoach.models.WeightCategory;
import com.example.fitcoach.models.WorkoutTraining;
import com.example.fitcoach.services.DatabaseService;

public class WorkoutEditActivity extends AppCompatActivity {

    private EditText etName, etDesc, etCal, etSets, etReps, etRest;
    private RadioGroup rgTargetAudience;
    private Button btnUpdate;
    private DatabaseService dbService;

    private String workoutId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_edit);

        dbService = DatabaseService.getInstance();
        initViews();

        workoutId = getIntent().getStringExtra("WORKOUT_ID");
        if (workoutId == null) {
            Toast.makeText(this, "No workout ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle("עריכת אימון");
        loadWorkoutData();

        btnUpdate.setOnClickListener(v -> updateWorkout());
    }

    private void initViews() {
        etName = findViewById(R.id.et_workout_name);
        etDesc = findViewById(R.id.et_workout_description);
        etCal = findViewById(R.id.et_calories_per_set);
        etSets = findViewById(R.id.et_sets);
        etReps = findViewById(R.id.et_reps);
        etRest = findViewById(R.id.et_rest_time);
        rgTargetAudience = findViewById(R.id.rg_target_audience);
        btnUpdate = findViewById(R.id.btn_update_workout);
    }

    private void loadWorkoutData() {
        dbService.getWorkoutTraining(workoutId, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(WorkoutTraining workout) {
                if (workout != null) {
                    etName.setText(workout.getName());
                    etDesc.setText(workout.getDescription());
                    etCal.setText(String.valueOf(workout.getCaloriesPerSet()));
                    etSets.setText(String.valueOf(workout.getSets()));
                    etReps.setText(String.valueOf(workout.getReps()));
                    etRest.setText(String.valueOf(workout.getRestTimeMinutes()));

                    if (workout.getTargetAudience() != null) {
                        switch (workout.getTargetAudience()) {
                            case UNDERWEIGHT:
                                rgTargetAudience.check(R.id.rb_underweight);
                                break;
                            case NORMAL:
                                rgTargetAudience.check(R.id.rb_normal_weight);
                                break;
                            case OVERWEIGHT:
                                rgTargetAudience.check(R.id.rb_overweight);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(WorkoutEditActivity.this, "Failed to load workout data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWorkout() {
        String name = etName.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String calStr = etCal.getText().toString().trim();
        String setsStr = etSets.getText().toString().trim();
        String repsStr = etReps.getText().toString().trim();
        String restStr = etRest.getText().toString().trim();
        WeightCategory targetAudience = getSelectedAudience();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(calStr) ||
                TextUtils.isEmpty(setsStr) || TextUtils.isEmpty(repsStr) || TextUtils.isEmpty(restStr) ||
                targetAudience == null) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int calories = Integer.parseInt(calStr);
            int sets = Integer.parseInt(setsStr);
            int reps = Integer.parseInt(repsStr);
            double restTime = Double.parseDouble(restStr);

            WorkoutTraining workout = new WorkoutTraining(workoutId, name, desc, calories, sets, reps, restTime, targetAudience);

            dbService.createNewWorkoutTraining(workout, new DatabaseService.DatabaseCallback<>() {
                @Override
                public void onCompleted(Void object) {
                    Toast.makeText(WorkoutEditActivity.this, "האימון עודכן בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(WorkoutEditActivity.this, "שגיאה בעדכון האימון", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "נא להזין ערכים מספריים תקינים", Toast.LENGTH_SHORT).show();
        }
    }

    private WeightCategory getSelectedAudience() {
        int selectedId = rgTargetAudience.getCheckedRadioButtonId();
        if (selectedId == R.id.rb_underweight) {
            return WeightCategory.UNDERWEIGHT;
        }
        if (selectedId == R.id.rb_normal_weight) {
            return WeightCategory.NORMAL;
        }
        if (selectedId == R.id.rb_overweight) {
            return WeightCategory.OVERWEIGHT;
        }
        return null;
    }
}
