package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcoach.R;
import com.example.fitcoach.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnDoLogin, btnDoRegister;
    private ProgressBar progressAuth;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnDoLogin = findViewById(R.id.btnDoLogin);
        btnDoRegister = findViewById(R.id.btnDoRegister);
        progressAuth = findViewById(R.id.progressAuth);

        btnDoLogin.setOnClickListener(v -> doLogin());
        btnDoRegister.setOnClickListener(v -> doRegister());
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (!validate(email, pass)) return;

        setLoading(true);
        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(res -> {
                    ensureUserDocExists();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "שגיאה בהתחברות: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void doRegister() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (!validate(email, pass)) return;

        setLoading(true);
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(res -> {
                    ensureUserDocExists();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "שגיאה בהרשמה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private boolean validate(String email, String pass) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("נדרש אימייל");
            etEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            etPassword.setError("סיסמה מינ' 6 תווים");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void setLoading(boolean on) {
        progressAuth.setVisibility(on ? View.VISIBLE : View.GONE);
        btnDoLogin.setEnabled(!on);
        btnDoRegister.setEnabled(!on);
    }

    // יוצר מסמך משתמש ברירת מחדל אם לא קיים
    private void ensureUserDocExists() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(snap -> {
            if (!snap.exists()) {
                User u = new User();
                u.setUid(uid);
                u.setName("מתאמן");
                u.setEmail(auth.getCurrentUser().getEmail());
                u.setDailyStepTarget(8000);
                u.setDailyWaterTargetMl(2000);

                db.collection("users").document(uid).set(u)
                        .addOnSuccessListener(v -> goHome())
                        .addOnFailureListener(err -> {
                            setLoading(false);
                            Toast.makeText(this, "יצירת משתמש נכשלה: " + err.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                goHome();
            }
        }).addOnFailureListener(e -> {
            setLoading(false);
            Toast.makeText(this, "שגיאה בקריאת משתמש: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void goHome() {
        setLoading(false);
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}
