package com.example.fitcoach.screens;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitcoach.R;
import com.example.fitcoach.models.User;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.SharedPreferencesUtil;

public class UserProfileActivity extends BaseActivity {

    // הגדרת רכיבי המסך
    private EditText etName, etEmail, etHeight, etWeight;
    private TextView tvDisplayName, tvUserSubtitle;
    private Button btnSave;

    private User selectedUser;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        // הגדרת רווחים למערכת (כמו אצל המורה)
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();

        // בדיקה האם הגענו מרשימת המשתמשים (אדמין) או מהפרופיל האישי
        userId = getIntent().getStringExtra("USER_UID");
        if (userId == null) {
            // אם לא עבר ID ב-Intent, ניקח את המשתמש המחובר כרגע
            User currentUser = SharedPreferencesUtil.getUser(this);
            if (currentUser != null) userId = currentUser.getId();
        }

        if (userId != null) {
            fetchUserData();
        }
    }

    private void initViews() {
        etName = findViewById(R.id.et_user_name);
        etEmail = findViewById(R.id.et_user_email);
        etHeight = findViewById(R.id.et_user_height);
        etWeight = findViewById(R.id.et_user_weight);
        tvDisplayName = findViewById(R.id.tv_display_name);
        tvUserSubtitle = findViewById(R.id.tv_user_subtitle);
        btnSave = findViewById(R.id.btn_save_profile);

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveUserChanges());
        }
    }

    private void fetchUserData() {
        // שימוש ב-databaseService שקיים ב-BaseActivity
        databaseService.getUser(userId, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user != null) {
                    selectedUser = user;
                    fillFields();
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillFields() {
        if (selectedUser == null) return;

        etName.setText(selectedUser.getName());
        etEmail.setText(selectedUser.getEmail());
        etHeight.setText(String.valueOf(selectedUser.getHeightCm()));
        etWeight.setText(String.valueOf(selectedUser.getWeightKg()));
        tvDisplayName.setText(selectedUser.getName());
        tvUserSubtitle.setText(selectedUser.getEmail());
    }

    private void saveUserChanges() {
        if (selectedUser == null) return;

        try {
            // עדכון האובייקט מהשדות במסך
            selectedUser.setName(etName.getText().toString());
            selectedUser.setHeightCm(Integer.parseInt(etHeight.getText().toString()));
            selectedUser.setWeightKg(Float.parseFloat(etWeight.getText().toString()));

            databaseService.updateUser(selectedUser, new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onCompleted(Void result) {
                    Toast.makeText(UserProfileActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                    finish(); // סגירת הדף וחזרה אחורה
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(UserProfileActivity.this,"Save Failed", Toast.LENGTH_SHORT).show();                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Check your inputs", Toast.LENGTH_SHORT).show();
        }
    }
}