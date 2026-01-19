package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitcoach.R;
import com.example.fitcoach.models.User;
import com.example.fitcoach.utils.SharedPreferencesUtil;

public class MainActivity extends AppCompatActivity {

    private TextView tvGreeting, tvStepsValue, tvWaterValue, tvBmiStatusText;
    private ProgressBar pbSteps, pbWater, pbWaterJug;
    private ImageButton btnSettingsGear, btnAddWater, btnRemoveWater;
    private ImageView ivBmiNeedle;
    private Button btnStartWorkout;

    private int waterToday = 0, waterTarget = 2000;
    private final int GLASS_SIZE = 250;

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
        loadUserData();
        setupButtons();
    }

    private void bindViews() {
        tvGreeting = findViewById(R.id.tv_main_welcome);
        btnSettingsGear = findViewById(R.id.btn_settings_gear);
        pbSteps = findViewById(R.id.pb_main_steps);
        tvStepsValue = findViewById(R.id.tv_main_steps_val);
        pbWater = findViewById(R.id.pb_main_water);
        tvWaterValue = findViewById(R.id.tv_main_water_val);
        ivBmiNeedle = findViewById(R.id.iv_bmi_needle);
        tvBmiStatusText = findViewById(R.id.tv_bmi_status_text);

        pbWaterJug = findViewById(R.id.pb_water_jug);
        btnAddWater = findViewById(R.id.btn_add_water);
        btnRemoveWater = findViewById(R.id.btn_remove_water);
    }

    private void loadUserData() {
        User user = SharedPreferencesUtil.getUser(this);
        if (user != null) {
            tvGreeting.setText("שלום, " + (user.getName() != null ? user.getName() : "מתאמן"));
            waterTarget = user.getDailyWaterTargetMl() > 0 ? user.getDailyWaterTargetMl() : 2000;
            updateBMIGauge(user);
        }

        pbWater.setMax(waterTarget);
        pbWaterJug.setMax(waterTarget);
        updateWaterUI();
    }

    private void setupButtons() {
        btnSettingsGear.setOnClickListener(v -> showSettingsMenu(v));

        btnAddWater.setOnClickListener(v -> {
            waterToday += GLASS_SIZE;
            updateWaterUI();
        });

        btnRemoveWater.setOnClickListener(v -> {
            if (waterToday >= GLASS_SIZE) {
                waterToday -= GLASS_SIZE;
                updateWaterUI();
            }
        });
    }

    private void updateWaterUI() {
        tvWaterValue.setText(waterToday + "\nמ\"ל");
        pbWater.setProgress(Math.min(waterToday, waterTarget));
        pbWaterJug.setProgress(Math.min(waterToday, waterTarget));
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
        popup.getMenu().add("התנתקות");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("עריכת פרופיל")) {
                startActivity(new Intent(this, UserProfileActivity.class));
            } else {
                SharedPreferencesUtil.clearUser(this);
                finish();
            }
            return true;
        });
        popup.show();
    }
}