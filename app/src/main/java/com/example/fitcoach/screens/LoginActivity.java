package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitcoach.R;
import com.example.fitcoach.models.User;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.SharedPreferencesUtil;
import com.example.fitcoach.utils.Validator;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnDoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        initViews();
        setupWindowInsets();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnDoLogin = findViewById(R.id.btnDoLogin);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupClickListeners() {
        TextView btnDoRegister = findViewById(R.id.btnDoRegister);
        btnDoLogin.setOnClickListener(v -> handleLogin());
        btnDoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "יש למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Validator.isEmailValid(email)) {
            etEmail.setError("אימייל לא תקין");
            return;
        }

        btnDoLogin.setEnabled(false);
        loginUser(email, password);
    }

    private void loginUser(String email, String password) {
        DatabaseService.getInstance().getUserByEmailAndPassword(email, password, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                btnDoLogin.setEnabled(true);
                if (user == null) {
                    Toast.makeText(LoginActivity.this, "פרטים שגויים", Toast.LENGTH_LONG).show();
                    return;
                }
                SharedPreferencesUtil.saveUser(LoginActivity.this, user);
                Toast.makeText(LoginActivity.this, "התחברות מוצלחת!", Toast.LENGTH_SHORT).show();
                navigateToMain();
            }

            @Override
            public void onFailed(Exception e) {
                btnDoLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "שגיאה בחיבור: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
