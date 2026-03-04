package com.example.fitcoach.screens;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcoach.R;
import com.example.fitcoach.models.User;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.SharedPreferencesUtil;

public class UserProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail, etHeight, etWeight;
    private EditText etDailyStepsTarget, etDailyCaloriesTarget, etDailyWaterTarget;
    private Button btnSave;

    private User userToUpdate;
    private DatabaseService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        dbService = DatabaseService.getInstance();
        initViews();

        String userId = getIntent().getStringExtra("user_id");
        if (userId != null) {
            loadUserFromDb(userId);
        } else {
            // Fallback to logged-in user if no ID is provided
            loadCurrentUserFromPrefs();
        }

        btnSave.setOnClickListener(v -> saveUserChanges());
    }

    private void initViews() {
        etName = findViewById(R.id.et_user_name);
        etEmail = findViewById(R.id.et_user_email);
        etHeight = findViewById(R.id.et_user_height);
        etWeight = findViewById(R.id.et_user_weight);
        etDailyStepsTarget = findViewById(R.id.et_daily_steps_target);
        etDailyCaloriesTarget = findViewById(R.id.et_daily_calories_target);
        etDailyWaterTarget = findViewById(R.id.et_daily_water_target);
        btnSave = findViewById(R.id.btn_save_profile);

        etEmail.setEnabled(false); // Make sure email is not editable
    }

    private void loadUserFromDb(String userId) {
        dbService.getUser(userId, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(User user) {
                if (user != null) {
                    userToUpdate = user;
                    populateUserDetails();
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCurrentUserFromPrefs() {
        userToUpdate = SharedPreferencesUtil.getUser(this);
        if (userToUpdate != null) {
            populateUserDetails();
        }
    }

    private void populateUserDetails() {
        etName.setText(userToUpdate.getName());
        etEmail.setText(userToUpdate.getEmail());
        etHeight.setText(String.valueOf(userToUpdate.getHeightCm()));
        etWeight.setText(String.valueOf(userToUpdate.getWeightKg()));
        etDailyStepsTarget.setText(String.valueOf(userToUpdate.getDailyStepsTarget()));
        etDailyCaloriesTarget.setText(String.valueOf(userToUpdate.getDailyCaloriesTarget()));
        etDailyWaterTarget.setText(String.valueOf(userToUpdate.getDailyWaterTargetMl()));
    }

    private void saveUserChanges() {
        if (userToUpdate == null) return;

        try {
            // Update user object from the fields
            userToUpdate.setName(etName.getText().toString().trim());
            userToUpdate.setHeightCm(Integer.parseInt(etHeight.getText().toString().trim()));
            userToUpdate.setWeightKg(Float.parseFloat(etWeight.getText().toString().trim()));
            userToUpdate.setDailyStepsTarget(Integer.parseInt(etDailyStepsTarget.getText().toString().trim()));
            userToUpdate.setDailyCaloriesTarget(Integer.parseInt(etDailyCaloriesTarget.getText().toString().trim()));
            userToUpdate.setDailyWaterTargetMl(Integer.parseInt(etDailyWaterTarget.getText().toString().trim()));

            // Update user in Database
            dbService.updateUser(userToUpdate.getId(), user -> {
                user.setName(userToUpdate.getName());
                user.setHeightCm(userToUpdate.getHeightCm());
                user.setWeightKg(userToUpdate.getWeightKg());
                user.setDailyStepsTarget(userToUpdate.getDailyStepsTarget());
                user.setDailyCaloriesTarget(userToUpdate.getDailyCaloriesTarget());
                user.setDailyWaterTargetMl(userToUpdate.getDailyWaterTargetMl());
                return user;
            }, new DatabaseService.DatabaseCallback<>() {
                @Override
                public void onCompleted(User updatedUser) {
                    // Also update the user in SharedPreferences if it's the current user
                    if (userToUpdate.getId().equals(SharedPreferencesUtil.getUserId(UserProfileActivity.this))) {
                        SharedPreferencesUtil.saveUser(UserProfileActivity.this, userToUpdate);
                    }
                    Toast.makeText(UserProfileActivity.this, "הפרופיל עודכן בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(UserProfileActivity.this, "שגיאה בעדכון הפרופיל", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "נא למלא ערכים מספריים תקינים ביעדים, גובה ומשקל.", Toast.LENGTH_LONG).show();
        }
    }
}
