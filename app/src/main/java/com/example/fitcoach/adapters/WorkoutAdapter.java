package com.example.fitcoach.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.models.WorkoutTraining;

import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<WorkoutTraining> workoutList;

    public WorkoutAdapter(List<WorkoutTraining> workoutList) {
        this.workoutList = workoutList;
    }

    public void setWorkoutList(List<WorkoutTraining> workoutList) {
        this.workoutList = workoutList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_activity, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        WorkoutTraining workout = workoutList.get(position);
        holder.bind(workout);
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvDescription, tvDetails, tvCalories;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_workout_item_name);
            tvDescription = itemView.findViewById(R.id.tv_workout_item_description);
            tvDetails = itemView.findViewById(R.id.tv_workout_item_details);
            tvCalories = itemView.findViewById(R.id.tv_workout_item_calories);
        }

        public void bind(WorkoutTraining workout) {
            tvName.setText(workout.getName());
            tvDescription.setText(workout.getDescription());
            tvDetails.setText(String.format("%d סטים, %d חזרות", workout.getSets(), workout.getReps()));
            tvCalories.setText(String.format("~%d קלוריות", workout.getTotalExerciseCalories()));
        }
    }
}
