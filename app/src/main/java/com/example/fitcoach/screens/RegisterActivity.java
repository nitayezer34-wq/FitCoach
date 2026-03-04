package com.example.fitcoach.screens;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword;
    private TextInputEditText etName, etEmail, etPassword;
    private RadioGroup rgGender;
    private Spinner spinnerBirthYear;
    private NumberPicker npHeight, npWeight;
    private ChipGroup cgActivityLevel;
    private SeekBar sbStepTarget, sbCaloriesTarget, sbWaterTarget;
    private TextView tvStepTargetValue, tvCaloriesTargetValue, tvWaterTargetValue;

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
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        rgGender = findViewById(R.id.rgGender);
        spinnerBirthYear = findViewById(R.id.spinnerBirthYear);
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
    }

    private void setupUI() {
        // Birth Year Spinner
        ArrayList<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear; i >= 1950; i--) {
            years.add(Integer.toString(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBirthYear.setAdapter(yearAdapter);

        // Height NumberPicker
        npHeight.setMinValue(120);
        npHeight.setMaxValue(220);
        npHeight.setValue(170);

        // Weight NumberPicker
        npWeight.setMinValue(40);
        npWeight.setMaxValue(150);
        npWeight.setValue(70);

        // Set NumberPicker text color
        int blueColor = Color.parseColor("#2196F3");
        setNumberPickerTextColor(npHeight, blueColor);
        setNumberPickerTextColor(npWeight, blueColor);

        // SeekBars
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
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        // Initial text update
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout.isErrorEnabled()) {
                    layout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
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

        int birthYear = Integer.parseInt(spinnerBirthYear.getSelectedItem().toString());
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

        // Clear previous errors
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);

        String name = Objects.requireNonNull(etName.getText()).toString().trim();
        if (name.isEmpty()) {
            tilName.setError("שדה חובה");
            isValid = false;
        } else if (!Validator.isNameValid(name)) {
            tilName.setError("שם יכול להכיל אותיות בעברית ובאנגלית בלבד");
            isValid = false;
        }

        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        if (email.isEmpty()) {
            tilEmail.setError("שדה חובה");
            isValid = false;
        } else if (!Validator.isEmailValid(email)) {
            tilEmail.setError("אימייל לא תקין");
            isValid = false;
        }

        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();
        if (password.isEmpty()) {
            tilPassword.setError("שדה חובה");
            isValid = false;
        } else if (!Validator.isPasswordValid(password)) {
            tilPassword.setError("סיסמה חזקה צריכה להכיל לפחות 8 תווים, אות גדולה, אות קטנה, מספר ותו מיוחד");
            isValid = false;
        }

        if (rgGender.getCheckedRadioButtonId() == -1) {
            // You can optionally highlight the RadioGroup or its title
            isValid = false;
        }

        if (spinnerBirthYear.getSelectedItem() == null) {
            // This case is unlikely with the current setup but good practice
            isValid = false;
        }

        if (cgActivityLevel.getCheckedChipId() == -1) {
            // You can optionally highlight the ChipGroup or its title
            isValid = false;
        }

        return isValid;
    }
}
