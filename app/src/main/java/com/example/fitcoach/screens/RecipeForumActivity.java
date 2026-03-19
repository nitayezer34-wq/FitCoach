package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.adapters.RecipeAdapter;
import com.example.fitcoach.models.Recipe;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.SharedPreferencesUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeForumActivity extends AppCompatActivity {

    private RecipeAdapter adapter;
    private DatabaseService dbService;
    private List<Recipe> allRecipes = new ArrayList<>();
    
    private EditText etSearchRecipe, etMaxCalories, etMaxTime;
    private CheckBox cbMyRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_forum);

        dbService = DatabaseService.getInstance();

        initViews();
        setupFilters();

        RecyclerView rvRecipes = findViewById(R.id.rvRecipes);
        FloatingActionButton fabAddRecipe = findViewById(R.id.fabAddRecipe);

        rvRecipes.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecipeAdapter(this, recipe -> {
            Intent intent = new Intent(RecipeForumActivity.this, RecipeDetailsActivity.class);
            intent.putExtra("RECIPE_ID", recipe.getId());
            startActivity(intent);
        });

        rvRecipes.setAdapter(adapter);

        fabAddRecipe.setOnClickListener(v -> startActivity(new Intent(RecipeForumActivity.this, AddRecipeActivity.class)));
    }

    private void initViews() {
        etSearchRecipe = findViewById(R.id.etSearchRecipe);
        etMaxCalories = findViewById(R.id.etMaxCalories);
        etMaxTime = findViewById(R.id.etMaxTime);
        cbMyRecipes = findViewById(R.id.cbMyRecipes);
    }

    private void setupFilters() {
        TextWatcher filterWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                applyFilters();
            }
        };

        etSearchRecipe.addTextChangedListener(filterWatcher);
        etMaxCalories.addTextChangedListener(filterWatcher);
        etMaxTime.addTextChangedListener(filterWatcher);
        cbMyRecipes.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilters());
    }

    private void applyFilters() {
        String searchQuery = etSearchRecipe.getText().toString().toLowerCase().trim();
        String maxCalStr = etMaxCalories.getText().toString().trim();
        String maxTimeStr = etMaxTime.getText().toString().trim();
        boolean showOnlyMine = cbMyRecipes.isChecked();
        String currentUserId = SharedPreferencesUtil.getUserId(this);

        int maxCalories = maxCalStr.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(maxCalStr);
        int maxTime = maxTimeStr.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(maxTimeStr);

        List<Recipe> filteredList = allRecipes.stream()
                .filter(recipe -> {
                    if (searchQuery.isEmpty()) return true;
                    String title = recipe.getTitle().toLowerCase();
                    // חיפוש מחמיר: רק אם השם מתחיל במילת החיפוש
                    return title.startsWith(searchQuery);
                })
                .filter(recipe -> recipe.getCalories() <= maxCalories)
                .filter(recipe -> recipe.getPrepTimeInMinutes() <= maxTime)
                .filter(recipe -> !showOnlyMine || (recipe.getUserId() != null && recipe.getUserId().equals(currentUserId)))
                .collect(Collectors.toList());

        adapter.setRecipeList(filteredList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipes();
    }

    private void loadRecipes() {
        dbService.getRecipeList(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Recipe> recipes) {
                if (recipes != null) {
                    allRecipes = recipes;
                    applyFilters();
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(RecipeForumActivity.this, "שגיאה בטעינת המתכונים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
