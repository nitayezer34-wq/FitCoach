package com.example.fitcoach.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitcoach.R;
import com.example.fitcoach.models.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private final OnRecipeClickListener listener;
    private final Context context;
    private List<Recipe> recipeList = new ArrayList<>();

    public RecipeAdapter(Context context, OnRecipeClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setRecipeList(List<Recipe> recipes) {
        this.recipeList = recipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.bind(recipe, listener, context);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivRecipeImage;
        final TextView tvRecipeTitle;
        final TextView tvPrepTime;
        final TextView tvCalories;
        final TextView tvAllergens;
        final LinearLayout llRecipeRating;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipeImage);
            tvRecipeTitle = itemView.findViewById(R.id.tvRecipeTitle);
            tvPrepTime = itemView.findViewById(R.id.tvPrepTime);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            tvAllergens = itemView.findViewById(R.id.tvAllergens);
            llRecipeRating = itemView.findViewById(R.id.llRecipeRating);
        }

        public void bind(final Recipe recipe, final OnRecipeClickListener listener, Context context) {
            tvRecipeTitle.setText(recipe.getTitle());

            if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(recipe.getImageUrl())
                        .placeholder(R.drawable.ic_recipe_placeholder)
                        .error(R.drawable.ic_recipe_placeholder)
                        .into(ivRecipeImage);
            } else {
                ivRecipeImage.setImageResource(R.drawable.ic_recipe_placeholder);
            }

            tvPrepTime.setText(formatPrepTime(recipe.getPrepTimeInMinutes()));
            tvCalories.setText(String.format(Locale.getDefault(), "%d קלוריות", recipe.getCalories()));

            updateRatingStars(recipe.getRating());

            if (recipe.getAllergens() != null && !recipe.getAllergens().isEmpty()) {
                tvAllergens.setVisibility(View.VISIBLE);
                tvAllergens.setText(String.format(Locale.getDefault(), "אלרגנים: %s", String.join(", ", recipe.getAllergens())));
            } else {
                tvAllergens.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
        }

        private String formatPrepTime(int minutes) {
            if (minutes < 60) {
                return minutes + " דקות";
            }
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours + " שעות";
            }
            return String.format(Locale.getDefault(), "%d hr %d min", hours, remainingMinutes);
        }

        private void updateRatingStars(double rating) {
            int fullStars = (int) rating;
            for (int i = 0; i < llRecipeRating.getChildCount(); i++) {
                ImageView star = (ImageView) llRecipeRating.getChildAt(i);
                if (i < fullStars) {
                    star.setColorFilter(0xFFFFC107); // Yellow
                } else {
                    star.setColorFilter(0xFFBDBDBD); // Grey
                }
            }
        }
    }
}
