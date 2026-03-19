package com.example.fitcoach.screens;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitcoach.R;
import com.example.fitcoach.models.Recipe;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddRecipeActivity extends AppCompatActivity {

    private EditText etTitle, etImageUrl, etPrepTime, etCalories, etAllergens, etIngredients, etInstructions;
    private Button btnSave;
    private TextView tvTitle;
    private DatabaseService dbService;
    private String recipeIdToEdit = null;
    private String originalUserId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_recipe);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbService = DatabaseService.getInstance();
        bindViews();

        recipeIdToEdit = getIntent().getStringExtra("RECIPE_ID");
        if (recipeIdToEdit != null) {
            tvTitle.setText("עריכת מתכון");
            btnSave.setText("עדכן מתכון");
            loadRecipeData(recipeIdToEdit);
        }

        btnSave.setOnClickListener(v -> saveRecipe());
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.tvAddRecipeTitle);
        etTitle = findViewById(R.id.etRecipeTitle);
        etImageUrl = findViewById(R.id.etImageUrl);
        etPrepTime = findViewById(R.id.etPrepTime);
        etCalories = findViewById(R.id.etCalories);
        etAllergens = findViewById(R.id.etAllergens);
        etIngredients = findViewById(R.id.etIngredients);
        etInstructions = findViewById(R.id.etInstructions);
        btnSave = findViewById(R.id.btnSaveRecipe);
    }

    private void loadRecipeData(String recipeId) {
        dbService.getRecipe(recipeId, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Recipe recipe) {
                if (recipe != null) {
                    originalUserId = recipe.getUserId();
                    etTitle.setText(recipe.getTitle());
                    etImageUrl.setText(recipe.getImageUrl());
                    etPrepTime.setText(String.valueOf(recipe.getPrepTimeInMinutes()));
                    etCalories.setText(String.valueOf(recipe.getCalories()));
                    etAllergens.setText(recipe.getAllergens() != null ? String.join(", ", recipe.getAllergens()) : "");
                    etIngredients.setText(recipe.getIngredients() != null ? String.join("\n", recipe.getIngredients()) : "");
                    etInstructions.setText(recipe.getInstructions() != null ? String.join("\n", recipe.getInstructions()) : "");
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AddRecipeActivity.this, "שגיאה בטעינת נתוני המתכון", Toast.LENGTH_SHORT).show();
            }
        });
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

        String id = (recipeIdToEdit != null) ? recipeIdToEdit : dbService.generateRecipeId();
        String userId = (recipeIdToEdit != null) ? originalUserId : SharedPreferencesUtil.getUserId(this);

        Recipe recipe = new Recipe(id, userId, title, imageUrl, calories, prepTime, allergens, ingredients, instructions);

        dbService.createNewRecipe(recipe, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                String msg = (recipeIdToEdit != null) ? "המתכון עודכן בהצלחה!" : "המתכון עלה לפורום!";
                Toast.makeText(AddRecipeActivity.this, msg, Toast.LENGTH_SHORT).show();
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
