package com.example.fitcoach.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitcoach.R;
import com.example.fitcoach.models.Recipe;
import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipeList = new ArrayList<>();
    private final OnRecipeClickListener listener;

    // ממשק להאזנה ללחיצות (לחיצה רגילה לצפייה בפרטים)
    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    public void setRecipeList(List<Recipe> recipes) {
        this.recipeList = recipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // שימוש ב-layout שיצרנו קודם לכרטיסיית מתכון
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        // הגדרת נתונים
        holder.tvTitle.setText(recipe.getTitle());
        holder.tvScore.setText("מדד בריאות: " + recipe.getHealthScore() + "/10");

        // עיצוב כחול-לבן לפי הקו של האפליקציה
        holder.tvTitle.setTextColor(Color.parseColor("#003366")); // כחול כהה

        // לחיצה על כל הכרטיסייה תפתח את פרטי המתכון
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvScore;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRecipeTitle);
            tvScore = itemView.findViewById(R.id.tvRecipeScore);
        }
    }
}