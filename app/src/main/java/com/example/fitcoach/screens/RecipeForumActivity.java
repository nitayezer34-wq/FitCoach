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

    private RecyclerView rvRecipes;
    private FloatingActionButton fabAddRecipe;
    private RecipeAdapter adapter;
    private DatabaseService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_forum);

        dbService = DatabaseService.getInstance();

        // חיבור ה-IDs
        rvRecipes = findViewById(R.id.rvRecipes);
        fabAddRecipe = findViewById(R.id.fabAddRecipe);

        // הגדרת ה-RecyclerView
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));

        // יצירת ה-Adapter (כרגע בלי לוגיקת לחיצה מורכבת, רק מעבר לפרטים אם תרצה בעתיד)
        adapter = new RecipeAdapter(recipe -> {
            // כאן אפשר להוסיף מעבר לדף RecipeDetailsActivity אם תרצה
            Toast.makeText(this, "בחרת במתכון: " + recipe.getTitle(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RecipeForumActivity.this, RecipeDetailsActivity.class));
        });

        rvRecipes.setAdapter(adapter);

        // מעבר לדף הוספת מתכון
        fabAddRecipe.setOnClickListener(v -> {
            startActivity(new Intent(RecipeForumActivity.this, AddRecipeActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // קריאה לנתונים בכל פעם שחוזרים למסך (למשל אחרי הוספת מתכון)
        loadRecipes();
    }

    private void loadRecipes() {
        dbService.getRecipeList(new DatabaseService.DatabaseCallback<List<Recipe>>() {
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