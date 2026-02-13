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

    private User currentUser;
    private DatabaseService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        dbService = DatabaseService.getInstance();
        initViews();
        loadUserData();

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
    }

    private void loadUserData() {
        currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser != null) {
            etName.setText(currentUser.getName());
            etEmail.setText(currentUser.getEmail());
            etHeight.setText(String.valueOf(currentUser.getHeightCm()));
            etWeight.setText(String.valueOf(currentUser.getWeightKg()));
            etDailyStepsTarget.setText(String.valueOf(currentUser.getDailyStepsTarget()));
            etDailyCaloriesTarget.setText(String.valueOf(currentUser.getDailyCaloriesTarget()));
            etDailyWaterTarget.setText(String.valueOf(currentUser.getDailyWaterTargetMl()));
        }
    }

    private void saveUserChanges() {
        if (currentUser == null) return;

        try {
            // Update user object from the fields
            currentUser.setName(etName.getText().toString().trim());
            currentUser.setHeightCm(Integer.parseInt(etHeight.getText().toString().trim()));
            currentUser.setWeightKg(Float.parseFloat(etWeight.getText().toString().trim()));
            currentUser.setDailyStepsTarget(Integer.parseInt(etDailyStepsTarget.getText().toString().trim()));
            currentUser.setDailyCaloriesTarget(Integer.parseInt(etDailyCaloriesTarget.getText().toString().trim()));
            currentUser.setDailyWaterTargetMl(Integer.parseInt(etDailyWaterTarget.getText().toString().trim()));

            // Update user in Database
            dbService.updateUser(currentUser.getId(), user -> {
                user.setName(currentUser.getName());
                user.setHeightCm(currentUser.getHeightCm());
                user.setWeightKg(currentUser.getWeightKg());
                user.setDailyStepsTarget(currentUser.getDailyStepsTarget());
                user.setDailyCaloriesTarget(currentUser.getDailyCaloriesTarget());
                user.setDailyWaterTargetMl(currentUser.getDailyWaterTargetMl());
                return user;
            }, new DatabaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User updatedUser) {
                    // Also update the user in SharedPreferences
                    SharedPreferencesUtil.saveUser(UserProfileActivity.this, currentUser);
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
