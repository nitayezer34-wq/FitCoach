package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitcoach.R;
import com.example.fitcoach.models.User;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.SharedPreferencesUtil;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // קישור השדות מה-XML
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnDoLogin);
        tvRegister = findViewById(R.id.btnDoRegister); // זה הקישור למעבר דף

        btnLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this); // מאזין ללחיצה על "הירשם עכשיו"
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnDoLogin) {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "יש למלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }
            loginUser(email, password);

        } else if (v.getId() == R.id.btnDoRegister) {
            // מעבר לדף הרשמה
            startActivity(new Intent(this, RegisterActivity.class));
        }
    }

    private void loginUser(String email, String password) {
        DatabaseService.getInstance().getUserByEmailAndPassword(email, password, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user != null) {
                    SharedPreferencesUtil.saveUser(LoginActivity.this, user);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "פרטים שגויים", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailed(Exception e) { Toast.makeText(LoginActivity.this, "שגיאה בחיבור", Toast.LENGTH_SHORT).show(); }
        });
    }
}