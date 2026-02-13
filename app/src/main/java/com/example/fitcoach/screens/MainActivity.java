package com.example.fitcoach.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitcoach.R;
import com.example.fitcoach.models.User;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.SharedPreferencesUtil;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private final int GLASS_SIZE = 250;
    private TextView tvGreeting, tvStepsValue, tvCaloriesValue, tvBmiStatusText;
    private ProgressBar pbSteps, pbCalories, pbWaterJug;
    private ImageButton btnSettingsGear, btnAddWater, btnRemoveWater;
    private ImageView ivBmiNeedle;
    // 1. הוספת משתנה לכפתור הפורום
    private Button btnMainForum;
    private int waterToday = 0, waterTarget = 2000;
    private int caloriesToday = 0, caloriesTarget = 1000;
    private int stepsToday = 0, stepsTarget = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void bindViews() {
        tvGreeting = findViewById(R.id.tv_main_welcome);
        btnSettingsGear = findViewById(R.id.btn_settings_gear);
        pbSteps = findViewById(R.id.pb_main_steps);
        tvStepsValue = findViewById(R.id.tv_main_steps_val);
        pbCalories = findViewById(R.id.pb_main_calories);
        tvCaloriesValue = findViewById(R.id.tv_main_calories_val);
        ivBmiNeedle = findViewById(R.id.iv_bmi_needle);
        tvBmiStatusText = findViewById(R.id.tv_bmi_status_text);
        pbWaterJug = findViewById(R.id.pb_water_jug);
        btnAddWater = findViewById(R.id.btn_add_water);
        btnRemoveWater = findViewById(R.id.btn_remove_water);

        // 2. קישור הכפתור מה-XML
        btnMainForum = findViewById(R.id.btn_main_forum);
    }

    private void setupButtons() {
        btnSettingsGear.setOnClickListener(v -> showSettingsMenu(v));

        btnAddWater.setOnClickListener(v -> {
            waterToday += GLASS_SIZE;
            saveStats();
            updateUI();
        });

        btnRemoveWater.setOnClickListener(v -> {
            if (waterToday >= GLASS_SIZE) {
                waterToday -= GLASS_SIZE;
                saveStats();
                updateUI();
            }
        });

        // 3. תיקון הלוגיקה למעבר לפורום
        btnMainForum.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RecipeForumActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cv_workout_entry).setOnClickListener(v -> {
            startActivity(new Intent(this, WorkoutActivity.class));
        });
    }

    // שאר הפונקציות (loadUserData, saveStats, updateUI, וכו') נשארות ללא שינוי...
    private void loadUserData() {
        User user = SharedPreferencesUtil.getUser(this);
        DatabaseService.getInstance().getUser(user.getId(), new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User updatedUser) {
                SharedPreferencesUtil.saveUser(MainActivity.this, updatedUser);
            }

            @Override
            public void onFailed(Exception e) {
            }
        });

        if (user.getName() != null && !user.getName().isEmpty()) {
            tvGreeting.setText("שלום, " + user.getName());
        } else {
            tvGreeting.setText("שלום, מתאמן");
        }

        waterTarget = user.getDailyWaterTargetMl() > 0 ? user.getDailyWaterTargetMl() : 2000;
        pbWaterJug.setMax(waterTarget);
        pbCalories.setMax(caloriesTarget);
        pbSteps.setMax(stepsTarget);
        updateUI();
    }

    private void saveStats() {
        SharedPreferences.Editor editor = getSharedPreferences("Stats", MODE_PRIVATE).edit();
        editor.putInt("water_today", waterToday);
        editor.putInt("burned_today", caloriesToday);
        editor.putInt("steps_today", stepsToday);
        editor.apply();
    }

    private void updateUI() {
        pbWaterJug.setProgress(Math.min(waterToday, waterTarget), true);
        tvCaloriesValue.setText(caloriesToday + "\nקלוריות");
        pbCalories.setProgress(Math.min(caloriesToday, caloriesTarget), true);
        tvStepsValue.setText(stepsToday + "\nצעדים");
        pbSteps.setProgress(Math.min(stepsToday, stepsTarget), true);
    }

    private void updateBMIGauge(User user) {
        if (user.getHeightCm() > 0 && user.getWeightKg() > 0) {
            float heightM = user.getHeightCm() / 100f;
            float bmi = user.getWeightKg() / (heightM * heightM);
            float rotation = (bmi < 18.5) ? -65f : (bmi < 25) ? 0f : 65f;
            ivBmiNeedle.setRotation(rotation);
            tvBmiStatusText.setText(bmi < 18.5 ? "תת משקל" : bmi < 25 ? "משקל תקין" : "משקל עודף");
        }
    }

    private void showSettingsMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("עריכת פרופיל");
        if (SharedPreferencesUtil.getUser(MainActivity.this).isAdmin()) {
            popup.getMenu().add("אדמין");
        }
        popup.getMenu().add("התנתקות");
        popup.setOnMenuItemClickListener(item -> {
            String title = Objects.requireNonNull(item.getTitle()).toString();
            switch (title) {
                case "עריכת פרופיל":
                    startActivity(new Intent(this, UserProfileActivity.class));
                    break;
                case "אדמין":
                    startActivity(new Intent(this, AdminActivity.class));
                    break;
                case "התנתקות":
                    SharedPreferencesUtil.signOutUser(this);
                    finish();
                    break;
            }
            return true;
        });
        popup.show();
    }
}