package com.example.fitcoach.screens;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcoach.R;
import com.example.fitcoach.models.Recipe;
import com.example.fitcoach.services.DatabaseService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddRecipeActivity extends AppCompatActivity {

    private EditText etTitle, etImageUrl, etPrepTime, etCalories, etAllergens, etIngredients, etInstructions;
    private Button btnSave;
    private DatabaseService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        dbService = DatabaseService.getInstance();
        bindViews();

        btnSave.setOnClickListener(v -> saveRecipe());
    }

    private void bindViews() {
        etTitle = findViewById(R.id.etRecipeTitle);
        etImageUrl = findViewById(R.id.etImageUrl);
        etPrepTime = findViewById(R.id.etPrepTime);
        etCalories = findViewById(R.id.etCalories);
        etAllergens = findViewById(R.id.etAllergens);
        etIngredients = findViewById(R.id.etIngredients);
        etInstructions = findViewById(R.id.etInstructions);
        btnSave = findViewById(R.id.btnSaveRecipe);
    }

    private void saveRecipe() {
        String title = etTitle.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();
        String prepTimeStr = etPrepTime.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();
        String allergensStr = etAllergens.getText().toString().trim();
        String ingredientsStr = etIngredients.getText().toString().trim();
        String instructionsStr = etInstructions.getText().toString().trim();

        if (title.isEmpty() || prepTimeStr.isEmpty() || caloriesStr.isEmpty() || ingredientsStr.isEmpty() || instructionsStr.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות.", Toast.LENGTH_SHORT).show();
            return;
        }

        int prepTime = Integer.parseInt(prepTimeStr);
        int calories = Integer.parseInt(caloriesStr);

        List<String> allergens = textToList(allergensStr, ",");
        List<String> ingredients = textToList(ingredientsStr, "\n");
        List<String> instructions = textToList(instructionsStr, "\n");

        String id = dbService.generateRecipeId();

        // Using the new constructor for a new recipe
        Recipe newRecipe = new Recipe(id, title, imageUrl, calories, prepTime, allergens, ingredients, instructions);

        dbService.createNewRecipe(newRecipe, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(AddRecipeActivity.this, "המתכון עלה לפורום!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AddRecipeActivity.this, "שגיאה בשמירת המתכון: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<String> textToList(String text, String delimiter) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(text.split("\\s*" + delimiter + "\\s*")));
    }
}
