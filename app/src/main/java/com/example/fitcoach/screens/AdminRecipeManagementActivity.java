package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.adapters.AdminRecipeAdapter;
import com.example.fitcoach.models.Recipe;
import com.example.fitcoach.services.DatabaseService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AdminRecipeManagementActivity extends AppCompatActivity implements AdminRecipeAdapter.OnRecipeActionListener {

    private RecyclerView rvRecipes;
    private AdminRecipeAdapter adapter;
    private List<Recipe> recipeList = new ArrayList<>();
    private DatabaseService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_recipe_management);

        dbService = DatabaseService.getInstance();
        initViews();
        setupRecyclerView();

        FloatingActionButton fabAddRecipe = findViewById(R.id.fab_add_recipe);
        fabAddRecipe.setOnClickListener(v -> startActivity(new Intent(AdminRecipeManagementActivity.this, AddRecipeActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipes();
    }

    private void initViews() {
        rvRecipes = findViewById(R.id.rv_recipes);
    }

    private void setupRecyclerView() {
        adapter = new AdminRecipeAdapter(recipeList, this);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        rvRecipes.setAdapter(adapter);
    }

    private void loadRecipes() {
        dbService.getRecipeList(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Recipe> recipes) {
                if (recipes != null) {
                    recipeList = recipes;
                    adapter.setRecipes(recipeList);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AdminRecipeManagementActivity.this, "שגיאה בטעינת מתכונים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteRecipe(Recipe recipe) {
        dbService.deleteRecipe(recipe.getId(), new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void result) {
                Toast.makeText(AdminRecipeManagementActivity.this, "מתכון נמחק בהצלחה", Toast.LENGTH_SHORT).show();
                loadRecipes();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AdminRecipeManagementActivity.this, "מחיקת המתכון נכשלה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditRecipe(Recipe recipe) {
        Intent intent = new Intent(this, AddRecipeActivity.class);
        intent.putExtra("RECIPE_ID", recipe.getId());
        startActivity(intent);
    }
}
