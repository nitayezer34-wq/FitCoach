package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcoach.R;
import com.example.fitcoach.models.User;
import com.example.fitcoach.utils.SharedPreferencesUtil;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    private TextView tvGreeting, tvStepsValue, tvWaterValue, tvStepsTarget, tvWaterTarget;
    private ProgressBar pbSteps, pbWater, progressLoading;
    private Button btnAddWater, btnAddSteps, btnStartWorkout;

    private int stepsToday = 0;
    private int waterToday = 0;
    private int stepTarget = 8000;
    private int waterTarget = 2000;

    private View layoutGuest, layoutLoggedIn;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        loadUserAndBindUI();
        setupButtons();
    }

    private void bindViews() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        progressLoading = findViewById(R.id.progressLoading);

        layoutGuest = findViewById(R.id.layoutGuest);
        layoutLoggedIn = findViewById(R.id.layoutLoggedIn);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvStepsValue = findViewById(R.id.tvStepsValue);
        tvStepsTarget = findViewById(R.id.tvStepsTarget);
        pbSteps = findViewById(R.id.pbSteps);

        tvWaterValue = findViewById(R.id.tvWaterValue);
        tvWaterTarget = findViewById(R.id.tvWaterTarget);
        pbWater = findViewById(R.id.pbWater);

        btnAddWater = findViewById(R.id.btnAddWater);
        btnAddSteps = findViewById(R.id.btnAddSteps);
        btnStartWorkout = findViewById(R.id.btnStartWorkout);
    }

    private void loadUserAndBindUI() {
        String uid = SharedPreferencesUtil.getUserId(MainActivity.this);

        if (uid == null) {
            layoutGuest.setVisibility(View.VISIBLE);
            layoutLoggedIn.setVisibility(View.GONE);
            progressLoading.setVisibility(View.GONE);
            return;
        }

        layoutGuest.setVisibility(View.GONE);
        layoutLoggedIn.setVisibility(View.VISIBLE);
        progressLoading.setVisibility(View.VISIBLE);

        User u = SharedPreferencesUtil.getUser(MainActivity.this);
        String name = "מתאמן";
        if (u != null) {
            if (u.getName() != null && !u.getName().trim().isEmpty()) {
                name = u.getName();
            }
            if (u.getDailyStepTarget() > 0) {
                stepTarget = u.getDailyStepTarget();
            }
            if (u.getDailyWaterTargetMl() > 0) {
                waterTarget = u.getDailyWaterTargetMl();
            }
        }
        tvGreeting.setText("שלום, " + name);
        pbSteps.setMax(stepTarget);
        pbWater.setMax(waterTarget);
        tvStepsTarget.setText("יעד: " + stepTarget);
        tvWaterTarget.setText("יעד: " + waterTarget);
        updateStepsUI();
        updateWaterUI();
    }

    private void setupButtons() {
        btnAddWater.setOnClickListener(v -> {
            waterToday += 250;
            updateWaterUI();
        });

        btnAddSteps.setOnClickListener(v -> {
            stepsToday += 100;
            updateStepsUI();
        });

        btnStartWorkout.setOnClickListener(v ->
                Toast.makeText(this, "מסך אימון – בהמשך", Toast.LENGTH_SHORT).show()
        );

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginActivity.class))
        );

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegisterActivity.class))
        );
    }

    private void updateStepsUI() {
        tvStepsValue.setText(String.valueOf(stepsToday));
        pbSteps.setProgress(Math.min(stepsToday, stepTarget));
    }

    private void updateWaterUI() {
        tvWaterValue.setText(String.valueOf(waterToday));
        pbWater.setProgress(Math.min(waterToday, waterTarget));
    }
}
