package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.fitcoach.utils.SharedPreferencesUtil;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etName, etEmail, etPassword;
    private EditText etGender, etBirthYear, etHeightCm, etWeightKg;
    private EditText etActivityLevel, etDailyStepTarget, etDailyWaterTargetMl;

    private Button btnRegisterConfirm, btnGoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // שים לב: ב-XML החדש ה-root נקרא registerRoot. אם השארת id="login" תחליף כאן בהתאם.
        View root = findViewById(R.id.registerRoot);
        if (root == null) {
            // אם עדיין נשאר לך id בשם login מה-XML הישן
            root = findViewById(R.id.login);
        }

        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // bind views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        etGender = findViewById(R.id.etGender);
        etBirthYear = findViewById(R.id.etBirthYear);
        etHeightCm = findViewById(R.id.etHeightCm);
        etWeightKg = findViewById(R.id.etWeightKg);

        etActivityLevel = findViewById(R.id.etActivityLevel);
        etDailyStepTarget = findViewById(R.id.etDailyStepTarget);
        etDailyWaterTargetMl = findViewById(R.id.etDailyWaterTargetMl);

        btnRegisterConfirm = findViewById(R.id.btnRegisterConfirm);
        btnGoLogin = findViewById(R.id.btnGoLogin);

        btnRegisterConfirm.setOnClickListener(this);
        btnGoLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnRegisterConfirm.getId()) {
            register();
        } else if (v.getId() == btnGoLogin.getId()) {
            finish();
        }
    }

    private void register() {
        String name = getText(etName);
        String email = getText(etEmail);
        String password = getText(etPassword);

        String gender = getText(etGender);
        String birthYearStr = getText(etBirthYear);
        String heightStr = getText(etHeightCm);
        String weightStr = getText(etWeightKg);

        String activityLevel = getText(etActivityLevel);
        String stepTargetStr = getText(etDailyStepTarget);
        String waterTargetStr = getText(etDailyWaterTargetMl);

        if (!validate(name, email, password, gender, birthYearStr, heightStr, weightStr, activityLevel, stepTargetStr, waterTargetStr)) {
            return;
        }

        int birthYear = Integer.parseInt(birthYearStr);
        int heightCm = Integer.parseInt(heightStr);
        float weightKg = Float.parseFloat(weightStr);
        int dailyStepTarget = Integer.parseInt(stepTargetStr);
        int dailyWaterTargetMl = Integer.parseInt(waterTargetStr);

        DatabaseService db = DatabaseService.getInstance();

        // בדיקה אם אימייל קיים (כמו המורה)
        db.checkIfEmailExists(email, new DatabaseService.DatabaseCallback<Boolean>() {
            @Override
            public void onCompleted(Boolean exists) {
                if (exists != null && exists) {
                    Toast.makeText(RegisterActivity.this, "האימייל כבר קיים", Toast.LENGTH_SHORT).show();
                    return;
                }

                String uid = db.generateUserId();

                User user = new User(
                        uid,
                        name,
                        email,
                        password,
                        gender,
                        birthYear,
                        heightCm,
                        weightKg,
                        activityLevel,
                        dailyStepTarget,
                        dailyWaterTargetMl
                );

                createUserInDatabase(user);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(RegisterActivity.this, "שגיאה בבדיקת אימייל: " + (e != null ? e.getMessage() : ""), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUserInDatabase(User user) {
        DatabaseService.getInstance().createNewUser(user, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                SharedPreferencesUtil.saveUser(RegisterActivity.this, user);

                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(RegisterActivity.this, "שגיאה בהרשמה: " + (e != null ? e.getMessage() : ""), Toast.LENGTH_SHORT).show();
                SharedPreferencesUtil.signOutUser(RegisterActivity.this);
            }
        });
    }

    private boolean validate(
            String name, String email, String password,
            String gender, String birthYearStr, String heightStr, String weightStr,
            String activityLevel, String stepTargetStr, String waterTargetStr
    ) {
        if (TextUtils.isEmpty(name)) { etName.setError("נדרש שם"); etName.requestFocus(); return false; }
        if (TextUtils.isEmpty(email) || !email.contains("@")) { etEmail.setError("אימייל לא תקין"); etEmail.requestFocus(); return false; }
        if (TextUtils.isEmpty(password) || password.length() < 6) { etPassword.setError("סיסמה מינימום 6"); etPassword.requestFocus(); return false; }

        if (TextUtils.isEmpty(gender)) { etGender.setError("נדרש מגדר"); etGender.requestFocus(); return false; }

        Integer birthYear = tryParseInt(birthYearStr);
        if (birthYear == null || birthYear < 1900 || birthYear > 2100) { etBirthYear.setError("שנת לידה לא תקינה"); etBirthYear.requestFocus(); return false; }

        Integer height = tryParseInt(heightStr);
        if (height == null || height < 50 || height > 250) { etHeightCm.setError("גובה לא תקין"); etHeightCm.requestFocus(); return false; }

        Float weight = tryParseFloat(weightStr);
        if (weight == null || weight < 20 || weight > 400) { etWeightKg.setError("משקל לא תקין"); etWeightKg.requestFocus(); return false; }

        if (TextUtils.isEmpty(activityLevel)) { etActivityLevel.setError("נדרשת רמת פעילות"); etActivityLevel.requestFocus(); return false; }

        Integer steps = tryParseInt(stepTargetStr);
        if (steps == null || steps < 1000 || steps > 50000) { etDailyStepTarget.setError("יעד צעדים לא תקין"); etDailyStepTarget.requestFocus(); return false; }

        Integer water = tryParseInt(waterTargetStr);
        if (water == null || water < 200 || water > 10000) { etDailyWaterTargetMl.setError("יעד מים לא תקין"); etDailyWaterTargetMl.requestFocus(); return false; }

        return true;
    }

    private String getText(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private Integer tryParseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    private Float tryParseFloat(String s) {
        try { return Float.parseFloat(s); } catch (Exception e) { return null; }
    }
}
