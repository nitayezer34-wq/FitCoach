package com.example.fitcoach.screens;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcoach.R;
import com.example.fitcoach.models.Recipe;
import com.example.fitcoach.services.DatabaseService;

public class AddRecipeActivity extends AppCompatActivity {

    private EditText etTitle, etScore;
    private Button btnSave;
    private DatabaseService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        dbService = DatabaseService.getInstance();

        etTitle = findViewById(R.id.etRecipeTitle);
        etScore = findViewById(R.id.etHealthScore);
        btnSave = findViewById(R.id.btnSaveRecipe);

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String scoreStr = etScore.getText().toString().trim();

            if (title.isEmpty() || scoreStr.isEmpty()) {
                Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            int score = Integer.parseInt(scoreStr);
            saveRecipeTodb(title, score);
        });
    }

    private void saveRecipeTodb(String title, int score) {
        // 1. יצירת ID ייחודי דרך ה-Service
        String id = dbService.generateRecipeId();

        // 2. יצירת אובייקט המתכון
        Recipe newRecipe = new Recipe(id, title, score);

        // 3. שמירה ב-Firebase
        dbService.createNewRecipe(newRecipe, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(AddRecipeActivity.this, "המתכון עלה לפורום!", Toast.LENGTH_SHORT).show();
                finish(); // סוגר את הדף וחוזר אחורה
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AddRecipeActivity.this, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}