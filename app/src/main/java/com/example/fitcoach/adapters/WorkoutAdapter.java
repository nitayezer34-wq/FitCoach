package com.example.fitcoach.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.models.WorkoutTraining;

import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private final List<WorkoutTraining> workoutList;
    private final OnWorkoutListener onWorkoutListener;

    public interface OnWorkoutListener {
        void onEdit(WorkoutTraining workout);
        void onDelete(WorkoutTraining workout);
    }

    public WorkoutAdapter(List<WorkoutTraining> workoutList, OnWorkoutListener onWorkoutListener) {
        this.workoutList = workoutList;
        this.onWorkoutListener = onWorkoutListener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view, onWorkoutListener);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        holder.bind(workoutList.get(position));
    }

    @Override
    public int getItemCount() {
        return workoutList != null ? workoutList.size() : 0;
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvCategory;
        private final Button btnEdit, btnDelete;

        public WorkoutViewHolder(@NonNull View itemView, OnWorkoutListener onWorkoutListener) {
            super(itemView);
            tvName = itemView.findViewById(R.id.workout_name);
            tvCategory = itemView.findViewById(R.id.workout_category);
            btnEdit = itemView.findViewById(R.id.btn_edit_workout);
            btnDelete = itemView.findViewById(R.id.btn_delete_workout);

        }

        public void bind(final WorkoutTraining workout) {
            tvName.setText(workout.getName());
            tvCategory.setText(workout.getTargetAudience());
            btnEdit.setOnClickListener(v -> {
                 if (onWorkoutListener != null) {
                    onWorkoutListener.onEdit(workout);
                }
            });
            btnDelete.setOnClickListener(v -> {
                 if (onWorkoutListener != null) {
                    onWorkoutListener.onDelete(workout);
                }
            });
        }
    }
}
