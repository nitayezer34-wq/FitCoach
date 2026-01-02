package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcoach.R;
import com.example.fitcoach.utils.SharedPreferencesUtil;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) אם המשתמש כבר מחובר -> ישר ל-MainActivity
        if (SharedPreferencesUtil.isUserLoggedIn(this)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // 2) אחרת מציגים את מסך ה-Landing
        setContentView(R.layout.activity_landing);

        // 3) חיבור הכפתורים
        Button btnSignUp = findViewById(R.id.btnSignUp);
        Button btnSignIn = findViewById(R.id.btnSignIn);

        btnSignUp.setOnClickListener(v ->
                startActivity(new Intent(LandingActivity.this, RegisterActivity.class)));

        btnSignIn.setOnClickListener(v ->
                startActivity(new Intent(LandingActivity.this, LoginActivity.class)));
    }
}
