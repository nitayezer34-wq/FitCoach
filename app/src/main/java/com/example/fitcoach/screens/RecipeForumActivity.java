package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.adapters.RecipeAdapter;
import com.example.fitcoach.models.Recipe;
import com.example.fitcoach.services.DatabaseService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class RecipeForumActivity extends AppCompatActivity {

    private RecipeAdapter adapter;
    private DatabaseService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_forum);

        dbService = DatabaseService.getInstance();

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
                    adapter.setRecipeList(recipes);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(RecipeForumActivity.this, "שגיאה בטעינת המתכונים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
