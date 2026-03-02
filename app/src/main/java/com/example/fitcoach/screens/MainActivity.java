package com.example.fitcoach.screens;

import android.content.Intent;
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
import com.example.fitcoach.models.Stats;
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
    private Button btnMainForum;

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
        updateUI();
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
        btnMainForum = findViewById(R.id.btn_main_forum);
    }

    private void setupButtons() {
        btnSettingsGear.setOnClickListener(v -> showSettingsMenu(v));

        btnAddWater.setOnClickListener(v -> {
            Stats stats = SharedPreferencesUtil.getStats(this);
            if(stats != null){
                stats.setWater(stats.getWater() + GLASS_SIZE);
                SharedPreferencesUtil.saveStats(this, stats);
                updateUI();
            }
        });

        btnRemoveWater.setOnClickListener(v -> {
            Stats stats = SharedPreferencesUtil.getStats(this);
            if (stats != null && stats.getWater() >= GLASS_SIZE) {
                stats.setWater(stats.getWater() - GLASS_SIZE);
                SharedPreferencesUtil.saveStats(this, stats);
                updateUI();
            }
        });

        btnMainForum.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RecipeForumActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cv_workout_entry).setOnClickListener(v -> {
            startActivity(new Intent(this, UserWorkoutsActivity.class));
        });
    }

    private void loadUserData() {
        User user = SharedPreferencesUtil.getUser(this);
        if (user == null) return;

        DatabaseService.getInstance().getUser(user.getId(), new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User updatedUser) {
                SharedPreferencesUtil.saveUser(MainActivity.this, updatedUser);
                updateBMIGauge(updatedUser);
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

        pbWaterJug.setMax(user.getDailyWaterTargetMl() > 0 ? user.getDailyWaterTargetMl() : 2000);
        pbCalories.setMax(user.getDailyCaloriesTarget() > 0 ? user.getDailyCaloriesTarget() : 2000);
        pbSteps.setMax(user.getDailyStepsTarget() > 0 ? user.getDailyStepsTarget() : 5000);
        updateUI();
    }


    private void updateUI() {
        Stats stats = SharedPreferencesUtil.getStats(this);
        User user = SharedPreferencesUtil.getUser(this);
        if (stats == null || user == null) return;

        pbWaterJug.setProgress((int) Math.min(stats.getWater(), user.getDailyWaterTargetMl()), true);

        int caloriesTarget = user.getDailyCaloriesTarget() > 0 ? user.getDailyCaloriesTarget() : 2000;
        tvCaloriesValue.setText(String.format("%d/%d", (int)stats.getCalories(), caloriesTarget));
        pbCalories.setMax(caloriesTarget);
        pbCalories.setProgress((int) Math.min(stats.getCalories(), caloriesTarget), true);

        int stepsTarget = user.getDailyStepsTarget() > 0 ? user.getDailyStepsTarget() : 5000;
        tvStepsValue.setText(String.format("%d/%d", stats.getSteps(), stepsTarget));
        pbSteps.setMax(stepsTarget);
        pbSteps.setProgress(Math.min(stats.getSteps(), stepsTarget), true);
    }

    private float map(float value, float fromLow, float fromHigh, float toLow, float toHigh) {
        return toLow + (value - fromLow) * (toHigh - toLow) / (fromHigh - fromLow);
    }

    private void updateBMIGauge(User user) {
        if (user.getHeightCm() > 0 && user.getWeightKg() > 0) {
            ivBmiNeedle.post(() -> {
                ivBmiNeedle.setPivotX(ivBmiNeedle.getWidth() / 2f);
                ivBmiNeedle.setPivotY(ivBmiNeedle.getHeight() - (ivBmiNeedle.getPaddingBottom()));
                float heightM = user.getHeightCm() / 100f;
                float bmi = user.getWeightKg() / (heightM * heightM);

                float rotation;
                if (bmi < 18.5) { // Underweight
                    rotation = map(bmi, 15f, 18.5f, -65f, -30f);
                } else if (bmi < 25) { // Normal weight
                    rotation = map(bmi, 18.5f, 25f, -30f, 30f);
                } else { // Overweight
                    rotation = map(bmi, 25f, 30f, 30f, 65f);
                }

                // Clamp rotation to prevent extreme values
                rotation = Math.max(-65f, Math.min(rotation, 65f));

                ivBmiNeedle.setRotation(rotation);
                tvBmiStatusText.setText(bmi < 18.5 ? "תת משקל" : bmi < 25 ? "משקל תקין" : "משקל עודף");
            });
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
