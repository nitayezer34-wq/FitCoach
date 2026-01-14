package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnDoLogin);
        tvRegister = findViewById(R.id.btnDoRegister);

        btnLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnLogin.getId()) {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            if (!checkInput(email, password)) return;

            loginUser(email, password);

        } else if (v.getId() == tvRegister.getId()) {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        }
    }

    private boolean checkInput(String email, String password) {
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

    private void loginUser(String email, String password) {
        DatabaseService.getInstance().getUserByEmailAndPassword(email, password, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                SharedPreferencesUtil.saveUser(LoginActivity.this, user);
                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
            }

            @Override
            public void onFailed(Exception e) {
                etPassword.setError("אימייל או סיסמה שגויים");
                etPassword.requestFocus();
                SharedPreferencesUtil.signOutUser(LoginActivity.this);
                Toast.makeText(LoginActivity.this, "התחברות נכשלה", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
