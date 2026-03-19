package com.example.fitcoach.screens;

import android.app.DatePickerDialog;
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

public class UserProfileActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilBirthDate;
    private TextInputEditText etName, etEmail, etPassword, etBirthDate;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private NumberPicker npHeight, npWeight;
    private ChipGroup cgActivityLevel;
    private Chip chipLow, chipMedium, chipHigh;
    private SeekBar sbStepTarget, sbCaloriesTarget, sbWaterTarget;
    private TextView tvStepTargetValue, tvCaloriesTargetValue, tvWaterTargetValue;
    private Button btnSave;
    
    private int selectedBirthYear = -1;
    private User currentUser;
    private DatabaseService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        dbService = DatabaseService.getInstance();
        initViews();
        setupUI();
        loadUserData();
        setupValidation();
        
        btnSave.setOnClickListener(v -> saveUserChanges());
        etBirthDate.setOnClickListener(v -> showDatePicker());
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
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        npHeight = findViewById(R.id.npHeight);
        npWeight = findViewById(R.id.npWeight);
        cgActivityLevel = findViewById(R.id.cgActivityLevel);
        chipLow = findViewById(R.id.chipLow);
        chipMedium = findViewById(R.id.chipMedium);
        chipHigh = findViewById(R.id.chipHigh);
        sbStepTarget = findViewById(R.id.sbStepTarget);
        tvStepTargetValue = findViewById(R.id.tvStepTargetValue);
        sbCaloriesTarget = findViewById(R.id.sbCaloriesTarget);
        tvCaloriesTargetValue = findViewById(R.id.tvCaloriesTargetValue);
        sbWaterTarget = findViewById(R.id.sbWaterTarget);
        tvWaterTargetValue = findViewById(R.id.tvWaterTargetValue);
        btnSave = findViewById(R.id.btn_save_profile);
        
        // Only Full Name remains disabled
        etName.setEnabled(false);
        // Email is now editable
        etEmail.setEnabled(true);
    }

    private void setupUI() {
        npHeight.setMinValue(120);
        npHeight.setMaxValue(220);
        npWeight.setMinValue(40);
        npWeight.setMaxValue(150);

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
                int snapped = (progress / step) * step;
                textView.setText(String.valueOf(snapped));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int snapped = (seekBar.getProgress() / step) * step;
                seekBar.setProgress(snapped);
            }
        });
    }

    private void setupValidation() {
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

    private void loadUserData() {
        currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null) {
            finish();
            return;
        }

        etName.setText(currentUser.getName());
        etEmail.setText(currentUser.getEmail());
        etPassword.setText(currentUser.getPassword());
        
        selectedBirthYear = currentUser.getBirthYear();
        etBirthDate.setText(String.format(Locale.getDefault(), "01/01/%d", selectedBirthYear));

        if (getString(R.string.male).equals(currentUser.getGender())) {
            rbMale.setChecked(true);
        } else if (getString(R.string.female).equals(currentUser.getGender())) {
            rbFemale.setChecked(true);
        }

        npHeight.setValue(currentUser.getHeightCm());
        npWeight.setValue((int) currentUser.getWeightKg());

        if (getString(R.string.activity_low).equals(currentUser.getActivityLevel())) {
            chipLow.setChecked(true);
        } else if (getString(R.string.activity_medium).equals(currentUser.getActivityLevel())) {
            chipMedium.setChecked(true);
        } else if (getString(R.string.activity_high).equals(currentUser.getActivityLevel())) {
            chipHigh.setChecked(true);
        }

        sbStepTarget.setProgress(currentUser.getDailyStepsTarget());
        tvStepTargetValue.setText(String.valueOf(currentUser.getDailyStepsTarget()));
        
        sbCaloriesTarget.setProgress(currentUser.getDailyCaloriesTarget());
        tvCaloriesTargetValue.setText(String.valueOf(currentUser.getDailyCaloriesTarget()));
        
        sbWaterTarget.setProgress(currentUser.getDailyWaterTargetMl());
        tvWaterTargetValue.setText(String.valueOf(currentUser.getDailyWaterTargetMl()));
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
        
        datePickerDialog.show();

        Button positiveButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE);
        Button negativeButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE);
        
        if (positiveButton != null) {
            positiveButton.setText("אישור");
            positiveButton.setTextColor(Color.parseColor("#1976D2"));
            positiveButton.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        if (negativeButton != null) {
            negativeButton.setText("ביטול");
            negativeButton.setTextColor(Color.parseColor("#1976D2"));
        }
    }

    private void saveUserChanges() {
        if (!validateInput()) return;

        final String newEmail = Objects.requireNonNull(etEmail.getText()).toString().trim();
        final String password = Objects.requireNonNull(etPassword.getText()).toString().trim();
        RadioButton selectedGender = findViewById(rgGender.getCheckedRadioButtonId());
        final String gender = selectedGender.getText().toString();
        final int height = npHeight.getValue();
        final float weight = npWeight.getValue();
        Chip selectedChip = findViewById(cgActivityLevel.getCheckedChipId());
        final String activityLevel = selectedChip.getText().toString();
        
        // Use snapped values for saving to avoid "random" looking values
        final int stepTarget = (sbStepTarget.getProgress() / 500) * 500;
        final int caloriesTarget = (sbCaloriesTarget.getProgress() / 100) * 100;
        final int waterTarget = (sbWaterTarget.getProgress() / 100) * 100;

        // Check if email changed and if it already exists
        if (!newEmail.equals(currentUser.getEmail())) {
            dbService.checkIfEmailExists(newEmail, new DatabaseService.DatabaseCallback<>() {
                @Override
                public void onCompleted(Boolean exists) {
                    if (exists) {
                        tilEmail.setError("האימייל כבר רשום במערכת");
                    } else {
                        performUpdate(newEmail, password, gender, height, weight, activityLevel, stepTarget, caloriesTarget, waterTarget);
                    }
                }
                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(UserProfileActivity.this, "שגיאה בבדיקת אימייל", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            performUpdate(newEmail, password, gender, height, weight, activityLevel, stepTarget, caloriesTarget, waterTarget);
        }
    }

    private void performUpdate(String email, String password, String gender, int height, float weight, String activityLevel, int stepTarget, int caloriesTarget, int waterTarget) {
        dbService.updateUser(currentUser.getId(), user -> {
            user.setEmail(email);
            user.setPassword(password);
            user.setGender(gender);
            user.setBirthYear(selectedBirthYear);
            user.setHeightCm(height);
            user.setWeightKg(weight);
            user.setActivityLevel(activityLevel);
            user.setDailyStepsTarget(stepTarget);
            user.setDailyCaloriesTarget(caloriesTarget);
            user.setDailyWaterTargetMl(waterTarget);
            return user;
        }, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(User updatedUser) {
                SharedPreferencesUtil.saveUser(UserProfileActivity.this, updatedUser);
                Toast.makeText(UserProfileActivity.this, "הפרופיל עודכן בהצלחה!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserProfileActivity.this, "שגיאה בעדכון: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput() {
        boolean isValid = true;
        tilEmail.setError(null);
        tilPassword.setError(null);

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

        if (selectedBirthYear == -1) isValid = false;
        if (rgGender.getCheckedRadioButtonId() == -1) isValid = false;
        if (cgActivityLevel.getCheckedChipId() == -1) isValid = false;
        return isValid;
    }
}
