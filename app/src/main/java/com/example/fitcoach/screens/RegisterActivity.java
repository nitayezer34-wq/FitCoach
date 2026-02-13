package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitcoach.R;
import com.example.fitcoach.models.User;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.SharedPreferencesUtil;
import com.example.fitcoach.utils.Validator;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    // Added etCaloriesTarget
    private EditText etName, etEmail, etPassword, etGender, etBirthYear, etHeight, etWeight, etActivityLevel, etStepTarget, etCaloriesTarget, etWaterTarget;
    private Button btnRegister, btnGoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etGender = findViewById(R.id.etGender);
        etBirthYear = findViewById(R.id.etBirthYear);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        etActivityLevel = findViewById(R.id.etActivityLevel);
        etStepTarget = findViewById(R.id.etStepTarget);
        etCaloriesTarget = findViewById(R.id.etCaloriesTarget); // Initialized the new EditText
        etWaterTarget = findViewById(R.id.etWaterTarget);
        btnRegister = findViewById(R.id.btnRegisterConfirm);
        btnGoLogin = findViewById(R.id.btnGoLogin);

        btnRegister.setOnClickListener(this);
        btnGoLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnGoLogin) {
            finish();
        } else if (v.getId() == R.id.btnRegisterConfirm) {
            handleRegistration();
        }
    }

    private void handleRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInput()) return;

        DatabaseService.getInstance().checkIfEmailExists(email, new DatabaseService.DatabaseCallback<Boolean>() {
            @Override
            public void onCompleted(Boolean exists) {
                if (exists) {
                    etEmail.setError("האימייל כבר רשום במערכת");
                } else {
                    saveNewUser(name, email, password);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(RegisterActivity.this, "שגיאה בבדיקה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveNewUser(String name, String email, String password) {
        String userId = DatabaseService.getInstance().generateUserId();
        try {
            // The User constructor now includes the daily calories target
            User user = new User(userId, name, email, password,
                    etGender.getText().toString(),
                    Integer.parseInt(etBirthYear.getText().toString()),
                    Integer.parseInt(etHeight.getText().toString()),
                    Float.parseFloat(etWeight.getText().toString()),
                    etActivityLevel.getText().toString(),
                    Integer.parseInt(etStepTarget.getText().toString()),
                    Integer.parseInt(etCaloriesTarget.getText().toString()), // Added the new value
                    Integer.parseInt(etWaterTarget.getText().toString()),
                    false);

            DatabaseService.getInstance().createNewUser(user, new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onCompleted(Void result) {
                    SharedPreferencesUtil.saveUser(RegisterActivity.this, user);
                    Toast.makeText(RegisterActivity.this, "חשבון נוצר בהצלחה!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(RegisterActivity.this, "שגיאה ברישום", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "נא למלא ערכים מספריים תקינים בשדות הנדרשים.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateInput() {
        // Basic validation, can be expanded
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("שדה חובה");
            return false;
        }
        if (!Validator.isEmailValid(etEmail.getText().toString())) {
            etEmail.setError("אימייל לא תקין");
            return false;
        }
        if (!Validator.isPasswordValid(etPassword.getText().toString())) {
            etPassword.setError("סיסמה חייבת להכיל לפחות 6 תווים");
            return false;
        }
        return true;
    }
}
