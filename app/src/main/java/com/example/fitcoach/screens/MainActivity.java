package com.example.fitcoach.screens;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.health.connect.client.HealthConnectClient;
import androidx.health.connect.client.PermissionController;

import com.example.fitcoach.R;
import com.example.fitcoach.models.Stats;
import com.example.fitcoach.models.User;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.HealthConnectManager;
import com.example.fitcoach.utils.SharedPreferencesUtil;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final int GLASS_SIZE = 250;
    private static final float STEPS_PER_METER = 1.31f; // Generic steps per meter
    private static final float CALORIES_PER_STEP = 0.045f; // Generic calories burned per step

    private TextView tvGreeting, tvStepsValue, tvCaloriesValue, tvBmiStatusText;
    private ProgressBar pbSteps, pbCalories, pbWaterJug;
    private ImageButton btnSettingsGear, btnAddWater, btnRemoveWater;
    private ImageView ivBmiNeedle;
    private Button btnMainForum, btnMainNavigation;

    private HealthConnectManager healthConnectManager;
    private ActivityResultLauncher<Set<String>> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> mapActivityResultLauncher;
    private boolean permissionRequestedInSession = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Initializing");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        healthConnectManager = new HealthConnectManager(this);
        
        // Register launcher ALWAYS in onCreate
        ActivityResultContract<Set<String>, Set<String>> contract = 
                PermissionController.createRequestPermissionResultContract();
        
        requestPermissionLauncher = registerForActivityResult(
                contract,
                granted -> {
                    Log.d(TAG, "Permissions callback result: " + granted);
                    if (granted != null && granted.containsAll(healthConnectManager.getPermissions())) {
                        fetchStepsFromHealthConnect();
                    } else {
                        Toast.makeText(this, "יש לאשר הרשאות בריאות כדי לצפות בצעדים", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Launcher for tracking when the user returns from Maps
        mapActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // This is a simplified simulation since we can't get data back from Google Maps app easily
                    // In a real scenario with Google Maps SDK we would have the exact distance.
                    // Here we show a dialog to "complete" the simulated walk.
                    showSimulatedWalkDialog();
                }
        );

        bindViews();
        setupButtons();
    }

    private void showSimulatedWalkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("סיום מסלול");
        builder.setMessage("הזן את אורך המסלול בקילומטרים כפי שהופיע ב-Google Maps:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("עדכן נתונים", (dialog, which) -> {
            String val = input.getText().toString();
            if (!val.isEmpty()) {
                try {
                    float distanceKm = Float.parseFloat(val);
                    int addedSteps = (int) (distanceKm * 1000 * STEPS_PER_METER);
                    double addedCalories = addedSteps * CALORIES_PER_STEP;
                    
                    Stats stats = SharedPreferencesUtil.getStats(this);
                    if (stats == null) stats = new Stats();
                    
                    stats.setSteps(stats.getSteps() + addedSteps);
                    stats.setCalories(stats.getCalories() + addedCalories);
                    
                    saveAndRefreshStats(stats);
                    String msg = String.format(Locale.getDefault(), "נוספו %d צעדים ו-%d קלוריות!", addedSteps, (int)addedCalories);
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "ערך לא תקין", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("ביטול", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called");
        updateUI(); // Immediate UI update from local storage
        loadUserData();
        checkAndRequestHealthConnectPermissions();
    }

    private void checkAndRequestHealthConnectPermissions() {
        int status = HealthConnectClient.getSdkStatus(this);
        Log.d(TAG, "checkAndRequestHealthConnectPermissions: Status=" + status);

        if (status == HealthConnectClient.SDK_AVAILABLE) {
            healthConnectManager.checkPermissions(granted -> {
                Log.d(TAG, "Permissions granted status: " + granted);
                if (granted) {
                    fetchStepsFromHealthConnect();
                } else if (requestPermissionLauncher != null && !permissionRequestedInSession) {
                    Log.d(TAG, "Launching permission request launcher");
                    permissionRequestedInSession = true;
                    runOnUiThread(() -> requestPermissionLauncher.launch(healthConnectManager.getPermissions()));
                }
            });
        } else if (status == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            Log.w(TAG, "Health Connect update required (Status 3)");
            if (!permissionRequestedInSession) {
                permissionRequestedInSession = true;
                Toast.makeText(this, "יש לעדכן את Health Connect ב-Play Store", Toast.LENGTH_LONG).show();
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.healthdata"));
                    intent.setPackage("com.android.vending");
                    startActivity(intent);
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")));
                }
            }
        }
    }

    private void fetchStepsFromHealthConnect() {
        Log.d(TAG, "fetchStepsFromHealthConnect: Fetching...");
        new Thread(() -> {
            Long steps = healthConnectManager.getTodaySteps();
            Log.d(TAG, "fetchStepsFromHealthConnect: Retrieved steps=" + steps);
            if (steps != null) {
                runOnUiThread(() -> {
                    Stats stats = SharedPreferencesUtil.getStats(this);
                    if (stats != null) {
                        stats.setSteps(steps.intValue());
                        saveAndRefreshStats(stats);
                    }
                });
            }
        }).start();
    }

    private void bindViews() {
        tvGreeting = findViewById(R.id.tv_main_welcome);
        btnSettingsGear = findViewById(R.id.btn_settings_gear);
        pbSteps = findViewById(R.id.pb_main_steps);
        tvStepsValue = findViewById(R.id.tv_main_steps_val);
        pbCalories = findViewById(R.id.pb_main_calories);
        tvCaloriesValue = findViewById(R.id.tv_main_calories_val);
        ivBmiNeedle = findViewById(R.id.iv_bmi_needle);
        tvBmiStatusText = findViewById(R.id.tv_bmi_status_text);
        pbWaterJug = findViewById(R.id.pb_water_jug);
        btnAddWater = findViewById(R.id.btn_add_water);
        btnRemoveWater = findViewById(R.id.btn_remove_water);
        btnMainForum = findViewById(R.id.btn_main_forum);
        btnMainNavigation = findViewById(R.id.btn_main_navigation);
    }

    private void setupButtons() {
        if (btnSettingsGear != null) {
            btnSettingsGear.setOnClickListener(this::showSettingsMenu);
        }

        if (pbSteps != null) {
            // לחיצה רגילה: סנכרון עם Health Connect
            pbSteps.setOnClickListener(v -> {
                Log.d(TAG, "Manual steps click detected");
                int status = HealthConnectClient.getSdkStatus(this);
                if (status == HealthConnectClient.SDK_AVAILABLE) {
                    healthConnectManager.checkPermissions(granted -> {
                        if (!granted && requestPermissionLauncher != null) {
                            runOnUiThread(() -> requestPermissionLauncher.launch(healthConnectManager.getPermissions()));
                        } else if (granted) {
                            fetchStepsFromHealthConnect();
                        }
                    });
                } else {
                    checkAndRequestHealthConnectPermissions();
                }
            });

            // לחיצה ארוכה: עדכון ידני
            pbSteps.setOnLongClickListener(v -> {
                showManualStepsDialog();
                return true;
            });
        }

        if (btnAddWater != null) {
            btnAddWater.setOnClickListener(v -> {
                Stats stats = SharedPreferencesUtil.getStats(this);
                if (stats != null) {
                    stats.setWater(stats.getWater() + GLASS_SIZE);
                    saveAndRefreshStats(stats);
                } else {
                    Stats newStats = new Stats();
                    newStats.setWater(GLASS_SIZE);
                    saveAndRefreshStats(newStats);
                }
            });
        }

        if (btnRemoveWater != null) {
            btnRemoveWater.setOnClickListener(v -> {
                Stats stats = SharedPreferencesUtil.getStats(this);
                if (stats != null && stats.getWater() >= GLASS_SIZE) {
                    stats.setWater(stats.getWater() - GLASS_SIZE);
                    saveAndRefreshStats(stats);
                }
            });
        }

        if (btnMainForum != null) {
            btnMainForum.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RecipeForumActivity.class);
                startActivity(intent);
            });
        }

        if (btnMainNavigation != null) {
            btnMainNavigation.setOnClickListener(v -> {
                showDestinationDialog();
            });
        }

        View workoutEntry = findViewById(R.id.cv_workout_entry);
        if (workoutEntry != null) {
            workoutEntry.setOnClickListener(v -> {
                startActivity(new Intent(this, UserWorkoutsActivity.class));
            });
        }
    }

    private void showDestinationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("לאן רצים?");
        
        final EditText input = new EditText(this);
        input.setHint("הכנס יעד (למשל: פארק הירקון)");
        builder.setView(input);

        builder.setPositiveButton("צא לדרך", (dialog, which) -> {
            String destination = input.getText().toString();
            if (!destination.isEmpty()) {
                // Uri for navigation in Google Maps
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(destination) + "&mode=w");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    mapActivityResultLauncher.launch(mapIntent);
                } else {
                    Toast.makeText(this, "אפליקציית Google Maps לא מותקנת", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("ביטול", null);
        builder.show();
    }

    private void showManualStepsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("עדכון צעדים ידני");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        Stats currentStats = SharedPreferencesUtil.getStats(this);
        if (currentStats != null) {
            input.setText(String.valueOf(currentStats.getSteps()));
        }
        builder.setView(input);

        builder.setPositiveButton("עדכן", (dialog, which) -> {
            String val = input.getText().toString();
            if (!val.isEmpty()) {
                int manualSteps = Integer.parseInt(val);
                Stats stats = SharedPreferencesUtil.getStats(this);
                if (stats == null) stats = new Stats();
                stats.setSteps(manualSteps);
                saveAndRefreshStats(stats);
                Toast.makeText(this, "הצעדים עודכנו בהצלחה", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("ביטול", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveAndRefreshStats(Stats stats) {
        SharedPreferencesUtil.saveStats(this, stats);
        String userId = SharedPreferencesUtil.getUserId(this);
        if (userId != null) {
            DatabaseService.getInstance().saveStats(userId, stats, null);
        }
        updateUI();
    }

    private void loadUserData() {
        User user = SharedPreferencesUtil.getUser(this);
        if (user == null) {
            SharedPreferencesUtil.signOutUser(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        DatabaseService.getInstance().getUser(user.getId(), new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(User updatedUser) {
                if (updatedUser != null) {
                    SharedPreferencesUtil.saveUser(MainActivity.this, updatedUser);
                    updateBMIGauge(updatedUser);

                    if (updatedUser.getName() != null && !updatedUser.getName().isEmpty() && tvGreeting != null) {
                        tvGreeting.setText(String.format(Locale.getDefault(), "שלום, %s", updatedUser.getName()));
                    }

                    if (pbWaterJug != null)
                        pbWaterJug.setMax(updatedUser.getDailyWaterTargetMl() > 0 ? updatedUser.getDailyWaterTargetMl() : 2000);
                    if (pbCalories != null)
                        pbCalories.setMax(updatedUser.getDailyCaloriesTarget() > 0 ? updatedUser.getDailyCaloriesTarget() : 2000);
                    if (pbSteps != null)
                        pbSteps.setMax(updatedUser.getDailyStepsTarget() > 0 ? updatedUser.getDailyStepsTarget() : 5000);
                }
                updateUI();
            }

            @Override
            public void onFailed(Exception e) {
                updateUI();
            }
        });

        DatabaseService.getInstance().getStats(user.getId(), new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Stats dbStats) {
                if (dbStats != null) {
                    SharedPreferencesUtil.saveStats(MainActivity.this, dbStats);
                } else {
                    Stats newStats = new Stats();
                    SharedPreferencesUtil.saveStats(MainActivity.this, newStats);
                    DatabaseService.getInstance().saveStats(user.getId(), newStats, null);
                }
                updateUI();
            }

            @Override
            public void onFailed(Exception e) {
                updateUI();
            }
        });
    }

    private void updateUI() {
        Stats stats = SharedPreferencesUtil.getStats(this);
        User user = SharedPreferencesUtil.getUser(this);
        if (stats == null || user == null) return;

        if (pbWaterJug != null) {
            int waterTarget = user.getDailyWaterTargetMl() > 0 ? user.getDailyWaterTargetMl() : 2000;
            pbWaterJug.setMax(waterTarget);
            int targetProgress = (int) Math.min(stats.getWater(), waterTarget);

            ObjectAnimator animation = ObjectAnimator.ofInt(pbWaterJug, "progress", pbWaterJug.getProgress(), targetProgress);
            animation.setDuration(800);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }

        if (pbCalories != null && tvCaloriesValue != null) {
            int caloriesTarget = user.getDailyCaloriesTarget() > 0 ? user.getDailyCaloriesTarget() : 2000;
            tvCaloriesValue.setText(String.format(Locale.getDefault(), "%d/%d", (int) stats.getCalories(), caloriesTarget));
            pbCalories.setMax(caloriesTarget);
            
            // Explicitly set progress for calories
            int calorieProgress = (int) Math.min(stats.getCalories(), caloriesTarget);
            pbCalories.setProgress(calorieProgress);
            Log.d(TAG, "updateUI: Setting calorie progress to " + calorieProgress + "/" + caloriesTarget);
        }

        if (pbSteps != null && tvStepsValue != null) {
            int stepsTarget = user.getDailyStepsTarget() > 0 ? user.getDailyStepsTarget() : 5000;
            tvStepsValue.setText(String.format(Locale.getDefault(), "%d/%d", stats.getSteps(), stepsTarget));
            pbSteps.setMax(stepsTarget);
            pbSteps.setProgress(Math.min(stepsTarget, stats.getSteps()));
        }
    }

    private float map(float value, float fromLow, float fromHigh, float toLow, float toHigh) {
        return toLow + (value - fromLow) * (toHigh - toLow) / (fromHigh - fromLow);
    }

    private void updateBMIGauge(User user) {
        if (user != null && user.getHeightCm() > 0 && user.getWeightKg() > 0 && ivBmiNeedle != null) {
            ivBmiNeedle.post(() -> {
                ivBmiNeedle.setPivotX(ivBmiNeedle.getWidth() / 2f);
                ivBmiNeedle.setPivotY(ivBmiNeedle.getHeight() - (ivBmiNeedle.getPaddingBottom()));
                float heightM = user.getHeightCm() / 100f;
                float bmi = user.getWeightKg() / (heightM * heightM);

                float rotation;
                if (bmi < 18.5) {
                    rotation = map(bmi, 15f, 18.5f, -65f, -30f);
                } else if (bmi < 25) {
                    rotation = map(bmi, 18.5f, 25f, -30f, 30f);
                } else {
                    rotation = map(bmi, 25f, 30f, 30f, 65f);
                }

                rotation = Math.max(-65f, Math.min(rotation, 65f));
                ivBmiNeedle.setRotation(rotation);
                if (tvBmiStatusText != null) {
                    tvBmiStatusText.setText(bmi < 18.5 ? "תת משקל" : bmi < 25 ? "משקל תקין" : "משקל עודף");
                }
            });
        }
    }

    private void showSettingsMenu(View view) {
        User user = SharedPreferencesUtil.getUser(this);
        if (user == null) return;

        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("עריכת פרופיל");
        if (user.isAdmin()) {
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
                    handleSignOut();
                    break;
            }
            return true;
        });
        popup.show();
    }

    private void handleSignOut() {
        SharedPreferencesUtil.signOutUser(this);
        Intent intent = new Intent(this, LandingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
