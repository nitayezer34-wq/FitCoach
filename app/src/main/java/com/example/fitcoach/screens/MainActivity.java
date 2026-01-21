package com.example.fitcoach.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitcoach.R;
import com.example.fitcoach.models.User;
import com.example.fitcoach.utils.SharedPreferencesUtil;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // הגדרת משתנים לרכיבי המסך
    private TextView tvGreeting, tvStepsValue, tvCaloriesValue, tvBmiStatusText;
    private ProgressBar pbSteps, pbCalories, pbWaterJug;
    private ImageButton btnSettingsGear, btnAddWater, btnRemoveWater;
    private ImageView ivBmiNeedle;

    // משתני נתונים (מים, קלוריות, צעדים)
    private int waterToday = 0, waterTarget = 2000;
    private int caloriesToday = 0, caloriesTarget = 1000;
    private int stepsToday = 0, stepsTarget = 5000;
    private final int GLASS_SIZE = 250; // כמות מים בכל לחיצה

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // הגדרת שוליים למסכי טלפון חדשים
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();      // חיבור משתנים ל-XML
        setupButtons();   // הגדרת לחיצות על כפתורים
    }

    @Override
    protected void onResume() {
        super.onResume();
        // קוראים לטעינת נתונים בכל פעם שחוזרים למסך (למשל אחרי אימון)
        loadUserData();
    }

    private void bindViews() {
        // כותרת וברכה
        tvGreeting = findViewById(R.id.tv_main_welcome);
        btnSettingsGear = findViewById(R.id.btn_settings_gear);

        // עיגול שמאלי: צעדים
        pbSteps = findViewById(R.id.pb_main_steps);
        tvStepsValue = findViewById(R.id.tv_main_steps_val);

        // עיגול ימני: קלוריות
        pbCalories = findViewById(R.id.pb_main_calories);
        tvCaloriesValue = findViewById(R.id.tv_main_calories_val);

        // BMI
        ivBmiNeedle = findViewById(R.id.iv_bmi_needle);
        tvBmiStatusText = findViewById(R.id.tv_bmi_status_text);

        // מים (קנקן וכוסות)
        pbWaterJug = findViewById(R.id.pb_water_jug);
        btnAddWater = findViewById(R.id.btn_add_water);
        btnRemoveWater = findViewById(R.id.btn_remove_water);
    }

    private void loadUserData() {
        // שליפת אובייקט המשתמש מהזיכרון
        User user = SharedPreferencesUtil.getUser(this);

        // תיקון הצגת השם: בודק אם המשתמש קיים ואם השם לא ריק
        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
            tvGreeting.setText("שלום, " + user.getName());
        } else {
            tvGreeting.setText(user.getName() + "שלום, ");
        }

        // טעינת התקדמות יומית מקובץ Stats (צעדים וקלוריות שנשמרו)
        SharedPreferences stats = getSharedPreferences("Stats", MODE_PRIVATE);
        caloriesToday = stats.getInt("burned_today", 0);
        stepsToday = stats.getInt("steps_today", 0);
        waterToday = stats.getInt("water_today", 0);

        if (user != null) {
            waterTarget = user.getDailyWaterTargetMl() > 0 ? user.getDailyWaterTargetMl() : 2000;
            updateBMIGauge(user);
        }

        // הגדרת מקסימום למדדים
        pbWaterJug.setMax(waterTarget);
        pbCalories.setMax(caloriesTarget);
        pbSteps.setMax(stepsTarget);

        updateUI(); // עדכון התצוגה הגרפית
    }

    private void setupButtons() {
        // כפתור הגדרות
        btnSettingsGear.setOnClickListener(v -> showSettingsMenu(v));

        // הוספת מים
        btnAddWater.setOnClickListener(v -> {
            waterToday += GLASS_SIZE;
            saveStats();
            updateUI();
        });

        // הורדת מים
        btnRemoveWater.setOnClickListener(v -> {
            if (waterToday >= GLASS_SIZE) {
                waterToday -= GLASS_SIZE;
                saveStats();
                updateUI();
            }
        });

        // כפתור פורום מתכונים (נשאר כפי שביקשת)
        findViewById(R.id.btn_main_forum).setOnClickListener(v -> {
            // כאן יבוא המעבר לפורום
        });

        // כניסה לדף אימון (לעדכון קלוריות)
        findViewById(R.id.cv_workout_entry).setOnClickListener(v -> {
            startActivity(new Intent(this, WorkoutActivity.class));
        });
    }

    // שמירת הנתונים כדי שלא יימחקו ביציאה מהאפליקציה
    private void saveStats() {
        SharedPreferences.Editor editor = getSharedPreferences("Stats", MODE_PRIVATE).edit();
        editor.putInt("water_today", waterToday);
        editor.putInt("burned_today", caloriesToday);
        editor.putInt("steps_today", stepsToday);
        editor.apply();
    }

    private void updateUI() {
        // עדכון קנקן המים (כחול-לבן)
        pbWaterJug.setProgress(Math.min(waterToday, waterTarget));

        // עדכון עיגול קלוריות (ימין)
        tvCaloriesValue.setText(caloriesToday + "\nקלוריות");
        pbCalories.setProgress(Math.min(caloriesToday, caloriesTarget));

        // עדכון עיגול צעדים (שמאל)
        tvStepsValue.setText(stepsToday + "\nצעדים");
        pbSteps.setProgress(Math.min(stepsToday, stepsTarget));
    }

    private void updateBMIGauge(User user) {
        if (user.getHeightCm() > 0 && user.getWeightKg() > 0) {
            float heightM = user.getHeightCm() / 100f;
            float bmi = user.getWeightKg() / (heightM * heightM);
            float rotation = (bmi < 18.5) ? -65f : (bmi < 25) ? 0f : 65f;
            ivBmiNeedle.setRotation(rotation);
            tvBmiStatusText.setText(bmi < 18.5 ? "תת משקל" : bmi < 25 ? "משקל תקין" : "משקל עודף");
        }
    }

    private void showSettingsMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("עריכת פרופיל");
        if (SharedPreferencesUtil.getUser(MainActivity.this).isAdmin()) {
            popup.getMenu().add("אדמין");
        }
        popup.getMenu().add("התנתקות");
        popup.setOnMenuItemClickListener(item -> {
            String title = Objects.requireNonNull(item.getTitle()).toString();
            switch (title) {
                case "עריכת פרופיל":
                    startActivity(new Intent(this, UserProfileActivity.class));
                    break;
                case "אדמין":
                    startActivity(new Intent(this, AdminActivity.class));
                    break;
                case "התנתקות":
                    SharedPreferencesUtil.signOutUser(this);
                    finish();
                    break;
            }
            return true;
        });
        popup.show();
    }
}