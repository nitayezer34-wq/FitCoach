package com.example.fitcoach.screens;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcoach.R;
import com.example.fitcoach.models.User;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.SharedPreferencesUtil;
import com.example.fitcoach.utils.Validator;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilBirthDate;
    private TextInputEditText etName, etEmail, etPassword, etBirthDate;
    private RadioGroup rgGender;
    private NumberPicker npHeight, npWeight;
    private ChipGroup cgActivityLevel;
    private SeekBar sbStepTarget, sbCaloriesTarget, sbWaterTarget;
    private TextView tvStepTargetValue, tvCaloriesTargetValue, tvWaterTargetValue;
    private int selectedBirthYear = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();
        setupUI();
        setupValidation();
    }

    private void initViews() {
        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilBirthDate = findViewById(R.id.tilBirthDate);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etBirthDate = findViewById(R.id.etBirthDate);
        rgGender = findViewById(R.id.rgGender);
        npHeight = findViewById(R.id.npHeight);
        npWeight = findViewById(R.id.npWeight);
        cgActivityLevel = findViewById(R.id.cgActivityLevel);
        sbStepTarget = findViewById(R.id.sbStepTarget);
        tvStepTargetValue = findViewById(R.id.tvStepTargetValue);
        sbCaloriesTarget = findViewById(R.id.sbCaloriesTarget);
        tvCaloriesTargetValue = findViewById(R.id.tvCaloriesTargetValue);
        sbWaterTarget = findViewById(R.id.sbWaterTarget);
        tvWaterTargetValue = findViewById(R.id.tvWaterTargetValue);
        Button btnRegister = findViewById(R.id.btnRegisterConfirm);
        Button btnGoLogin = findViewById(R.id.btnGoLogin);

        btnRegister.setOnClickListener(v -> handleRegistration());
        btnGoLogin.setOnClickListener(view -> finish());

        etBirthDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = selectedBirthYear != -1 ? selectedBirthYear : c.get(Calendar.YEAR) - 20;
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.FitDatePickerTheme,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedBirthYear = year1;
                    etBirthDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year1));
                    tilBirthDate.setError(null);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        
        // הצגת הדיאלוג
        datePickerDialog.show();

        // הכרחת צבע וכיתוב לכפתורים כדי שלא ייעלמו
        Button positiveButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE);
        Button negativeButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE);
        
        if (positiveButton != null) {
            positiveButton.setText("אישור");
            positiveButton.setTextColor(Color.parseColor("#1976D2")); // כחול כהה
            positiveButton.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        if (negativeButton != null) {
            negativeButton.setText("ביטול");
            negativeButton.setTextColor(Color.parseColor("#1976D2"));
        }
    }

    private void setupUI() {
        npHeight.setMinValue(120);
        npHeight.setMaxValue(220);
        npHeight.setValue(170);

        npWeight.setMinValue(40);
        npWeight.setMaxValue(150);
        npWeight.setValue(70);

        int blueColor = Color.parseColor("#2196F3");
        setNumberPickerTextColor(npHeight, blueColor);
        setNumberPickerTextColor(npWeight, blueColor);

        setupSeekBar(sbStepTarget, tvStepTargetValue, 500);
        setupSeekBar(sbCaloriesTarget, tvCaloriesTargetValue, 100);
        setupSeekBar(sbWaterTarget, tvWaterTargetValue, 100);
    }

    private void setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        for (int i = 0; i < numberPicker.getChildCount(); i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                ((EditText) child).setTextColor(color);
            }
        }
    }

    private void setupSeekBar(SeekBar seekBar, TextView textView, int step) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = (progress / step) * step;
                textView.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        textView.setText(String.valueOf(seekBar.getProgress()));
    }

    private void setupValidation() {
        addTextWatcher(etName, tilName);
        addTextWatcher(etEmail, tilEmail);
        addTextWatcher(etPassword, tilPassword);
    }

    private void addTextWatcher(TextInputEditText editText, TextInputLayout layout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout.isErrorEnabled()) {
                    layout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void handleRegistration() {
        if (!validateInput()) {
            Toast.makeText(this, "נא לתקן את השגיאות ולמלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        DatabaseService.getInstance().checkIfEmailExists(email, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Boolean exists) {
                if (exists) {
                    tilEmail.setError("האימייל כבר רשום במערכת");
                } else {
                    saveNewUser();
                }
            }
            @Override
            public void onFailed(Exception e) {
                Toast.makeText(RegisterActivity.this, "שגיאה בבדיקת אימייל: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveNewUser() {
        String userId = DatabaseService.getInstance().generateUserId();
        String name = Objects.requireNonNull(etName.getText()).toString().trim();
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

        RadioButton selectedGender = findViewById(rgGender.getCheckedRadioButtonId());
        String gender = selectedGender.getText().toString();

        int birthYear = selectedBirthYear;
        int height = npHeight.getValue();
        float weight = npWeight.getValue();

        Chip selectedChip = findViewById(cgActivityLevel.getCheckedChipId());
        String activityLevel = selectedChip.getText().toString();

        int stepTarget = sbStepTarget.getProgress();
        int caloriesTarget = sbCaloriesTarget.getProgress();
        int waterTarget = sbWaterTarget.getProgress();

        User user = new User(userId, name, email, password, gender, birthYear, height, weight, activityLevel, stepTarget, caloriesTarget, waterTarget, false);

        DatabaseService.getInstance().createNewUser(user, new DatabaseService.DatabaseCallback<>() {
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
                Toast.makeText(RegisterActivity.this, "שגיאה ברישום: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInput() {
        boolean isValid = true;
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilBirthDate.setError(null);

        String name = Objects.requireNonNull(etName.getText()).toString().trim();
        if (name.isEmpty()) {
            tilName.setError("שדה חובה");
            isValid = false;
        }

        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        if (email.isEmpty() || !Validator.isEmailValid(email)) {
            tilEmail.setError("אימייל לא תקין");
            isValid = false;
        }

        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();
        if (password.isEmpty() || !Validator.isPasswordValid(password)) {
            tilPassword.setError("סיסמה לא תקינה");
            isValid = false;
        }

        if (selectedBirthYear == -1) {
            tilBirthDate.setError("יש לבחור תאריך לידה");
            isValid = false;
        }
        if (rgGender.getCheckedRadioButtonId() == -1) isValid = false;
        if (cgActivityLevel.getCheckedChipId() == -1) isValid = false;

        return isValid;
    }
}
