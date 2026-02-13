package com.example.fitcoach.screens;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcoach.R;
import com.example.fitcoach.models.WorkoutTraining;
import com.example.fitcoach.services.DatabaseService;

public class WorkoutCreationActivity extends AppCompatActivity {

    private EditText etName, etDesc, etCal, etSets, etReps, etRest;
    private RadioGroup rgTargetAudience;
    private Button btnCreateOrUpdate;
    private DatabaseService dbService;

    private String existingWorkoutId; // To check if we are in edit mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_creation);

        dbService = DatabaseService.getInstance();
        initViews();

        existingWorkoutId = getIntent().getStringExtra("EDIT_WORKOUT_ID");
        if (existingWorkoutId != null) {
            // Edit mode
            setTitle("עריכת אימון");
            btnCreateOrUpdate.setText("עדכן אימון");
            loadWorkoutData();
        } else {
            // Create mode
            setTitle("יצירת אימון חדש");
            btnCreateOrUpdate.setText("צור אימון");
        }

        btnCreateOrUpdate.setOnClickListener(v -> saveWorkout());
    }

    private void initViews() {
        etName = findViewById(R.id.et_workout_name);
        etDesc = findViewById(R.id.et_workout_description);
        etCal = findViewById(R.id.et_calories_per_set);
        etSets = findViewById(R.id.et_sets);
        etReps = findViewById(R.id.et_reps);
        etRest = findViewById(R.id.et_rest_time);
        rgTargetAudience = findViewById(R.id.rg_target_audience);
        btnCreateOrUpdate = findViewById(R.id.btn_create_workout);
    }

    private void loadWorkoutData() {
        dbService.getWorkoutTraining(existingWorkoutId, new DatabaseService.DatabaseCallback<WorkoutTraining>() {
            @Override
            public void onCompleted(WorkoutTraining workout) {
                if (workout != null) {
                    etName.setText(workout.getName());
                    etDesc.setText(workout.getDescription());
                    etCal.setText(String.valueOf(workout.getCaloriesPerSet()));
                    etSets.setText(String.valueOf(workout.getSets()));
                    etReps.setText(String.valueOf(workout.getReps()));
                    etRest.setText(String.valueOf(workout.getRestTimeMinutes()));

                    switch (workout.getTargetAudience()) {
                        case "Underweight":
                            rgTargetAudience.check(R.id.rb_underweight);
                            break;
                        case "Normal":
                            rgTargetAudience.check(R.id.rb_normal_weight);
                            break;
                        case "Overweight":
                            rgTargetAudience.check(R.id.rb_overweight);
                            break;
                    }
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(WorkoutCreationActivity.this, "Failed to load workout data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveWorkout() {
        String name = etName.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String calStr = etCal.getText().toString().trim();
        String setsStr = etSets.getText().toString().trim();
        String repsStr = etReps.getText().toString().trim();
        String restStr = etRest.getText().toString().trim();
        String targetAudience = getSelectedAudience();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(calStr) ||
                TextUtils.isEmpty(setsStr) || TextUtils.isEmpty(repsStr) || TextUtils.isEmpty(restStr) ||
                TextUtils.isEmpty(targetAudience)) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int calories = Integer.parseInt(calStr);
            int sets = Integer.parseInt(setsStr);
            int reps = Integer.parseInt(repsStr);
            double restTime = Double.parseDouble(restStr);

            String id = (existingWorkoutId != null) ? existingWorkoutId : dbService.generateWorkoutId();

            WorkoutTraining workout = new WorkoutTraining(id, name, desc, calories, sets, reps, restTime, targetAudience);

            dbService.createNewWorkoutTraining(workout, new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onCompleted(Void object) {
                    String message = (existingWorkoutId == null) ? "האימון נוצר בהצלחה!" : "האימון עודכן בהצלחה!";
                    Toast.makeText(WorkoutCreationActivity.this, message, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(WorkoutCreationActivity.this, "שגיאה בשמירת האימון", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "נא להזין ערכים מספריים תקינים", Toast.LENGTH_SHORT).show();
        }
    }

    private String getSelectedAudience() {
        int selectedId = rgTargetAudience.getCheckedRadioButtonId();
        if (selectedId == R.id.rb_underweight) return "Underweight";
        if (selectedId == R.id.rb_normal_weight) return "Normal";
        if (selectedId == R.id.rb_overweight) return "Overweight";
        return "";
    }
}
