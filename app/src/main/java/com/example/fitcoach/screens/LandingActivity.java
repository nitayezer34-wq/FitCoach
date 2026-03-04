package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitcoach.R;
import com.example.fitcoach.utils.SharedPreferencesUtil;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // בדיקה אם המשתמש מחובר לפני יצירת ה-UI כדי לחסוך זמן טעינה
        if (SharedPreferencesUtil.isUserLoggedIn(this)) {
            navigateToMain();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing);

        setupWindowInsets();
        setupClickListeners();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupClickListeners() {
        Button btnSignUp = findViewById(R.id.btnSignUp);
        Button btnSignIn = findViewById(R.id.btnSignIn);

        btnSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        btnSignIn.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
