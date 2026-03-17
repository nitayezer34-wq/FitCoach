package com.example.fitcoach.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.models.WorkoutTraining;

import java.util.List;
import java.util.Objects;

public class AdminWorkoutAdapter extends RecyclerView.Adapter<AdminWorkoutAdapter.WorkoutViewHolder> {

    private final OnWorkoutActionListener listener;
    private List<WorkoutTraining> workouts;

    public AdminWorkoutAdapter(List<WorkoutTraining> workouts, OnWorkoutActionListener listener) {
        this.workouts = workouts;
        this.listener = listener;
    }

    public void setWorkouts(List<WorkoutTraining> newWorkouts) {
        if (this.workouts == null) {
            this.workouts = newWorkouts;
            notifyItemRangeInserted(0, newWorkouts.size());
            return;
        }

        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return workouts.size();
            }

            @Override
            public int getNewListSize() {
                return newWorkouts.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(workouts.get(oldItemPosition).getId(), 
                                     newWorkouts.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                WorkoutTraining oldW = workouts.get(oldItemPosition);
                WorkoutTraining newW = newWorkouts.get(newItemPosition);
                return Objects.equals(oldW.getName(), newW.getName()) &&
                       oldW.getSets() == newW.getSets() &&
                       oldW.getReps() == newW.getReps();
            }
        });

        this.workouts = newWorkouts;
        result.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        WorkoutTraining workout = workouts.get(position);
        holder.bind(workout, listener);
    }

    @Override
    public int getItemCount() {
        return workouts != null ? workouts.size() : 0;
    }

    public interface OnWorkoutActionListener {
        void onEditWorkout(WorkoutTraining workout);

        void onDeleteWorkout(WorkoutTraining workout);
    }

    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final ImageButton btnEdit;
        final ImageButton btnDelete;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_admin_workout_name);
            btnEdit = itemView.findViewById(R.id.btn_edit_workout);
            btnDelete = itemView.findViewById(R.id.btn_delete_workout);
        }

        public void bind(final WorkoutTraining workout, final OnWorkoutActionListener listener) {
            tvName.setText(workout.getName());
            btnEdit.setOnClickListener(v -> listener.onEditWorkout(workout));
            btnDelete.setOnClickListener(v -> listener.onDeleteWorkout(workout));
        }
    }
}
