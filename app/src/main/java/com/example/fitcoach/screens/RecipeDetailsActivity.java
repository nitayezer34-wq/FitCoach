package com.example.fitcoach.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fitcoach.R;
import com.example.fitcoach.models.Recipe;
import com.example.fitcoach.services.DatabaseService;

public class RecipeDetailsActivity extends AppCompatActivity {

    private ImageView ivRecipeImage;
    private TextView tvTitle, tvAllergens;
    private LinearLayout llRatingContainer, llIngredientsContainer, llInstructionsContainer;
    private Button btnRateRecipe;

    private DatabaseService dbService;
    private String recipeId;
    private Recipe currentRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        dbService = DatabaseService.getInstance();
        recipeId = getIntent().getStringExtra("RECIPE_ID");

        bindViews();
        loadRecipeDetails();

        btnRateRecipe.setOnClickListener(v -> showRatingDialog());
    }

    private void bindViews() {
        ivRecipeImage = findViewById(R.id.ivRecipeImage_details);
        tvTitle = findViewById(R.id.tvRecipeTitle_details);
        tvAllergens = findViewById(R.id.tvAllergens_details);
        llRatingContainer = findViewById(R.id.llRating_details);
        llIngredientsContainer = findViewById(R.id.llIngredientsContainer);
        llInstructionsContainer = findViewById(R.id.llInstructionsContainer);
        btnRateRecipe = findViewById(R.id.btnRateRecipe);
    }

    private void loadRecipeDetails() {
        if (recipeId == null) return;

        dbService.getRecipe(recipeId, new DatabaseService.DatabaseCallback<Recipe>() {
            @Override
            public void onCompleted(Recipe recipe) {
                currentRecipe = recipe;
                displayRecipe();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(RecipeDetailsActivity.this, "Failed to load recipe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRecipe() {
        if (currentRecipe == null) return;

        tvTitle.setText(currentRecipe.getTitle());

        Glide.with(this)
                .load(currentRecipe.getImageUrl())
                .placeholder(R.drawable.ic_recipe_placeholder)
                .error(R.drawable.ic_recipe_placeholder)
                .into(ivRecipeImage);

        updateRatingDisplay(currentRecipe.getRating());

        if (currentRecipe.getAllergens() != null && !currentRecipe.getAllergens().isEmpty()) {
            tvAllergens.setVisibility(View.VISIBLE);
            tvAllergens.setText(String.format("אלרגנים: %s", String.join(", ", currentRecipe.getAllergens())));
        } else {
            tvAllergens.setVisibility(View.GONE);
        }

        llIngredientsContainer.removeAllViews();
        if (currentRecipe.getIngredients() != null) {
            for (String ingredient : currentRecipe.getIngredients()) {
                TextView tv = new TextView(this);
                tv.setText(String.format("• %s", ingredient));
                tv.setTextSize(16);
                llIngredientsContainer.addView(tv);
            }
        }

        llInstructionsContainer.removeAllViews();
        if (currentRecipe.getInstructions() != null) {
            for (int i = 0; i < currentRecipe.getInstructions().size(); i++) {
                TextView tv = new TextView(this);
                tv.setText(String.format("%d. %s", i + 1, currentRecipe.getInstructions().get(i)));
                tv.setTextSize(16);
                tv.setLineSpacing(0, 1.2f);
                llInstructionsContainer.addView(tv);
            }
        }
    }

    private void updateRatingDisplay(double rating) {
        llRatingContainer.removeAllViews();
        int fullStars = (int) rating;
        for (int i = 0; i < 5; i++) {
            ImageView star = new ImageView(this);
            star.setImageResource(R.drawable.ic_star_custom);
            if (i < fullStars) {
                star.setColorFilter(0xFFFFC107); // Yellow
            } else {
                star.setColorFilter(0xFFBDBDBD); // Grey
            }
            llRatingContainer.addView(star);
        }
    }

    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_rate_recipe, null);
        builder.setView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.dialog_rating_bar);
        Button btnSubmit = dialogView.findViewById(R.id.btn_submit_rating);

        AlertDialog dialog = builder.create();

        btnSubmit.setOnClickListener(v -> {
            float newRating = ratingBar.getRating();
            if (newRating > 0) {
                updateRecipeRating(newRating);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void updateRecipeRating(float newRating) {
        double newTotalSum = currentRecipe.getTotalRatingSum() + newRating;
        int newRatingCount = currentRecipe.getRatingCount() + 1;
        double newAverageRating = newTotalSum / newRatingCount;

        dbService.updateRecipeRating(recipeId, newAverageRating, newRatingCount, newTotalSum, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void aVoid) {
                Toast.makeText(RecipeDetailsActivity.this, "תודה על הדירוג!", Toast.LENGTH_SHORT).show();
                currentRecipe.setRating(newAverageRating);
                currentRecipe.setRatingCount(newRatingCount);
                currentRecipe.setTotalRatingSum(newTotalSum);
                updateRatingDisplay(newAverageRating);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(RecipeDetailsActivity.this, "שגיאה בעדכון הדירוג", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
