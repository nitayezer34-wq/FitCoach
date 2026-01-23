package com.example.fitcoach.screens;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fitcoach.R;
import com.example.fitcoach.models.WorkoutTraining;
import com.example.fitcoach.services.DatabaseService;
import java.util.ArrayList;

public class WorkoutCreationActivity extends AppCompatActivity {

    private EditText etName, etDesc, etCal, etSets, etReps, etRest;
    private CheckBox cbUnder, cbNormal, cbOver;
    private Button btnAdd;
    private DatabaseService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_creation);

        dbService = DatabaseService.getInstance();

        // אתחול רכיבי ה-UI
        etName = findViewById(R.id.et_name);
        etDesc = findViewById(R.id.et_desc);
        etCal = findViewById(R.id.et_cal);
        etSets = findViewById(R.id.et_sets);
        etReps = findViewById(R.id.et_reps);
        etRest = findViewById(R.id.et_rest);
        cbUnder = findViewById(R.id.cb_under);
        cbNormal = findViewById(R.id.cb_normal);
        cbOver = findViewById(R.id.cb_over);
        btnAdd = findViewById(R.id.btn_add_workout);

        btnAdd.setOnClickListener(v -> saveWorkout());
    }

    private void saveWorkout() {
        // שליפת טקסט מהשדות
        String name = etName.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String calStr = etCal.getText().toString().trim();
        String setsStr = etSets.getText().toString().trim();
        String repsStr = etReps.getText().toString().trim();
        String restStr = etRest.getText().toString().trim();

        // בניית רשימת קהל יעד
        ArrayList<String> categories = new ArrayList<>();
        if (cbUnder.isChecked()) categories.add("Underweight");
        if (cbNormal.isChecked()) categories.add("Normal");
        if (cbOver.isChecked()) categories.add("Overweight");

        // בדיקה שכל השדות מלאים
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(calStr) ||
                TextUtils.isEmpty(setsStr) || TextUtils.isEmpty(repsStr) || TextUtils.isEmpty(restStr) ||
                categories.isEmpty()) {

            showCustomDialog("פרטים חסרים", "נא למלא את כל שדות האימון ולבחור לפחות קהל יעד אחד.", false);
            return;
        }

        try {
            // יצירת מזהה ייחודי מ-Firebase
            String id = dbService.generateWorkoutId();

            // המרת נתונים למספרים
            int calories = Integer.parseInt(calStr);
            int sets = Integer.parseInt(setsStr);
            int reps = Integer.parseInt(repsStr);
            double restTime = Double.parseDouble(restStr);

            // חיבור הקטגוריות למחרוזת אחת (למשל: "Underweight, Normal")
            String targetAudience = TextUtils.join(", ", categories);

            // יצירת אובייקט לפי הקונסטרקטור המדויק שלך
            WorkoutTraining workout = new WorkoutTraining(
                    id,
                    name,
                    desc,
                    calories,
                    sets,
                    reps,
                    restTime,
                    targetAudience
            );

            // שמירה ב-Database
            dbService.createNewWorkoutTraining(workout, new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onCompleted(Void object) {
                    showCustomDialog("הצלחה!", "האימון '" + name + "' נוסף בהצלחה למאגר הנתונים.", true);
                }

                @Override
                public void onFailed(Exception e) {
                    showCustomDialog("שגיאת שמירה", "אירעה שגיאה בחיבור ל-Firebase: " + e.getMessage(), false);
                }
            });

        } catch (NumberFormatException e) {
            showCustomDialog("שגיאת פורמט", "נא להזין מספרים תקינים בשדות המתאימים.", false);
        }
    }

    private void showCustomDialog(String title, String message, boolean shouldFinish) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("אישור", (dialog, which) -> {
                    if (shouldFinish) {
                        finish(); // סוגר את המסך וחוזר לדף האדמין
                    }
                })
                .setCancelable(false)
                .show();
    }
}