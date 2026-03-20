package com.example.fitcoach.screens;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.example.fitcoach.views.BMIView;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final int GLASS_SIZE = 250;
    private static final float STEPS_PER_METER = 1.31f; // Generic steps per meter
    private static final float CALORIES_PER_STEP = 0.045f; // Generic calories burned per step

    private TextView tvGreeting, tvStepsValue, tvCaloriesValue;
    private ProgressBar pbSteps, pbCalories, pbWaterJug;
    private ImageButton btnSettingsGear, btnAddWater, btnRemoveWater;
    private BMIView bmiView;
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
        SharedPreferencesUtil.checkAndClearCompletedWorkouts(this);
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
        bmiView = findViewById(R.id.bmi_view);
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

        // יצירת כותרת מותאמת אישית מיושרת לימין (RTL)
        TextView titleView = new TextView(this);
        titleView.setText("לאן רצים?");
        titleView.setGravity(Gravity.RIGHT);
        titleView.setPadding(0, 50, 60, 0); // ריווח יפה לכותרת
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        titleView.setTextColor(getResources().getColor(android.R.color.black));
        builder.setCustomTitle(titleView);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); // הגדרת RTL לכל הדיאלוג
        layout.setPadding(50, 40, 50, 10);

        final EditText etOrigin = new EditText(this);
        etOrigin.setHint("מאיפה מתחילים? למשל: התזמורת 17 ראשון לציון");
        etOrigin.setGravity(Gravity.RIGHT);
        layout.addView(etOrigin);

        final EditText etDestination = new EditText(this);
        etDestination.setHint("הכנס יעד (למשל: פארק הירקון תל אביב)");
        etDestination.setGravity(Gravity.RIGHT);
        layout.addView(etDestination);

        TextView tvReminder = new TextView(this);
        tvReminder.setText("\nשימו לב: זכרו את מרחק המסלול (ק\"מ) בסיום כדי שנוכל לחשב את הקלוריות והצעדים שלכם! 💪");
        tvReminder.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        tvReminder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvReminder.setGravity(Gravity.RIGHT);
        layout.addView(tvReminder);

        builder.setView(layout);

        builder.setPositiveButton("צא לדרך", (dialog, which) -> {
            String origin = etOrigin.getText().toString().trim();
            String destination = etDestination.getText().toString().trim();

            if (!destination.isEmpty()) {
                Uri gmmIntentUri;
                if (origin.isEmpty()) {
                    // אם אין מוצא, נשתמש בפורמט הישן והטוב שמתחיל ניווט מיד
                    gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(destination) + "&mode=w");
                } else {
                    // אם יש מוצא, נשתמש בפורמט שתומך בשניהם עם דגש על מצב הליכה
                    String uriString = "https://www.google.com/maps/dir/?api=1" +
                            "&origin=" + Uri.encode(origin) +
                            "&destination=" + Uri.encode(destination) +
                            "&travelmode=walking";
                    gmmIntentUri = Uri.parse(uriString);
                }

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    mapActivityResultLauncher.launch(mapIntent);
                } else {
                    Toast.makeText(this, "אפליקציית Google Maps לא מותקנת", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "יש להזין יעד", Toast.LENGTH_SHORT).show();
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
                if (dbStats != null && dbStats.isThisToday()) {
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
            tvCaloriesValue.setText(String.format(Locale.getDefault(), "%d/%d\n%s", (int) stats.getCalories(), caloriesTarget, getString(R.string.calories_label)));
            pbCalories.setMax(caloriesTarget);

            // Explicitly set progress for calories
            int calorieProgress = (int) Math.min(stats.getCalories(), caloriesTarget);
            pbCalories.setProgress(calorieProgress);
            Log.d(TAG, "updateUI: Setting calorie progress to " + calorieProgress + "/" + caloriesTarget);
        }

        if (pbSteps != null && tvStepsValue != null) {
            int stepsTarget = user.getDailyStepsTarget() > 5000 ? user.getDailyStepsTarget() : 5000;
            tvStepsValue.setText(String.format(Locale.getDefault(), "%d/%d\n%s", stats.getSteps(), stepsTarget, getString(R.string.steps_label)));
            pbSteps.setMax(stepsTarget);
            pbSteps.setProgress(Math.min(stepsTarget, stats.getSteps()));
        }
    }

    private void updateBMIGauge(User user) {
        if (user != null && user.getHeightCm() > 0 && user.getWeightKg() > 0 && bmiView != null) {
            bmiView.setBMI((float) user.getWeightKg(), (float) user.getHeightCm());
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
