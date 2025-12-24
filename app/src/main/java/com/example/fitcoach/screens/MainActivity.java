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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

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

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (uid == null) {
            layoutGuest.setVisibility(View.VISIBLE);
            layoutLoggedIn.setVisibility(View.GONE);
            progressLoading.setVisibility(View.GONE);
            return;
        }

        layoutGuest.setVisibility(View.GONE);
        layoutLoggedIn.setVisibility(View.VISIBLE);
        progressLoading.setVisibility(View.VISIBLE);

        DocumentReference userRef = db.collection("users").document(uid);
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snap, @Nullable FirebaseFirestoreException e) {
                progressLoading.setVisibility(View.GONE);
                if (e != null) {
                    Toast.makeText(MainActivity.this, "שגיאה בטעינה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                if (snap != null && snap.exists()) {
                    User u = snap.toObject(User.class);
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
                } else {
                    Toast.makeText(MainActivity.this, "לא נמצא מסמך משתמש ב-Firestore.", Toast.LENGTH_LONG).show();
                    tvGreeting.setText("שלום, מתאמן");
                    pbSteps.setMax(stepTarget);
                    pbWater.setMax(waterTarget);
                    tvStepsTarget.setText("יעד: " + stepTarget);
                    tvWaterTarget.setText("יעד: " + waterTarget);
                }
            }
        });
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
