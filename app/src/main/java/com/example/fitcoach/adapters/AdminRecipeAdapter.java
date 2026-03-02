package com.example.fitcoach.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.models.Recipe;

import java.util.List;

public class AdminRecipeAdapter extends RecyclerView.Adapter<AdminRecipeAdapter.RecipeViewHolder> {

    private final OnRecipeActionListener listener;
    private List<Recipe> recipes;

    public AdminRecipeAdapter(List<Recipe> recipes, OnRecipeActionListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe, listener);
    }

    @Override
    public int getItemCount() {
        return recipes != null ? recipes.size() : 0;
    }

    public interface OnRecipeActionListener {
        void onDeleteRecipe(Recipe recipe);
        void onEditRecipe(Recipe recipe);
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageButton btnDelete, btnEdit;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_admin_recipe_name);
            btnDelete = itemView.findViewById(R.id.btn_delete_recipe);
            btnEdit = itemView.findViewById(R.id.btn_edit_recipe);
        }

        public void bind(final Recipe recipe, final OnRecipeActionListener listener) {
            tvName.setText(recipe.getTitle());
            btnDelete.setOnClickListener(v -> listener.onDeleteRecipe(recipe));
            btnEdit.setOnClickListener(v -> listener.onEditRecipe(recipe));
        }
    }
}
