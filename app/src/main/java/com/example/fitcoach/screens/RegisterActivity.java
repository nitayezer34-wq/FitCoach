package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitcoach.R;
import com.example.fitcoach.models.User;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.Validator;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etName, etEmail, etPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegisterConfirm); // תואם ל-ID ב-XML

        btnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (!checkInput(name, email, password)) return;

        String userId = DatabaseService.getInstance().generateUserId();

        User user = new User(userId, name, email, password, "", 0, 0, 0f, "", 0, 0);

        DatabaseService.getInstance().createNewUser(user, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void result) {
                Toast.makeText(RegisterActivity.this, "נרשמת בהצלחה!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(RegisterActivity.this, "הרשמה נכשלה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkInput(String name, String email, String password) {
        if (name.trim().isEmpty()) {
            etName.setError("יש להזין שם");
            etName.requestFocus();
            return false;
        }
        if (!Validator.isEmailValid(email)) {
            etEmail.setError("כתובת אימייל לא תקינה");
            etEmail.requestFocus();
            return false;
        }
        if (!Validator.isPasswordValid(password)) {
            etPassword.setError("סיסמה חייבת להיות לפחות 6 תווים");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }
}
