package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitcoach.R;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        CardView cvAddWorkout = findViewById(R.id.cv_add_workout);
        CardView cvManageWorkouts = findViewById(R.id.cv_manage_workouts);
        CardView cvManageUsers = findViewById(R.id.cv_manage_users);

        // לחיצה על הוספת אימון
        cvAddWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, WorkoutCreationActivity.class);
            startActivity(intent);
        });

        // ניהול אימונים (בינתיים נשאיר ריק או תוסיף דף אם יש לך)
        cvManageWorkouts.setOnClickListener(v -> {
            // Toast.makeText(this, "בקרוב: ניהול אימונים", Toast.LENGTH_SHORT).show();
        });

        // כאן התיקון: מעבר לדף ניהול משתמשים
        cvManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, UsersListActivity.class);
            startActivity(intent);
        });
    }
}