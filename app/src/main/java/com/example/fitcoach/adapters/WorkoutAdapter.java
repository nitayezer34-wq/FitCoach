package com.example.fitcoach.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.models.WeightCategory;
import com.example.fitcoach.models.WorkoutTraining;

import java.util.List;
import java.util.Objects;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private final OnWorkoutListener onWorkoutListener;
    private List<WorkoutTraining> workoutList;

    public WorkoutAdapter(List<WorkoutTraining> workoutList, OnWorkoutListener onWorkoutListener) {
        this.workoutList = workoutList;
        this.onWorkoutListener = onWorkoutListener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        holder.bind(workoutList.get(position), onWorkoutListener);
    }

    @Override
    public int getItemCount() {
        return workoutList != null ? workoutList.size() : 0;
    }

    public void setWorkoutList(List<WorkoutTraining> newWorkoutList) {
        if (this.workoutList == null) {
            this.workoutList = newWorkoutList;
            notifyItemRangeInserted(0, newWorkoutList.size());
            return;
        }

        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return workoutList.size();
            }

            @Override
            public int getNewListSize() {
                return newWorkoutList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(workoutList.get(oldItemPosition).getId(), 
                                     newWorkoutList.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                WorkoutTraining oldW = workoutList.get(oldItemPosition);
                WorkoutTraining newW = newWorkoutList.get(newItemPosition);
                return Objects.equals(oldW.getName(), newW.getName()) &&
                       oldW.getTargetAudience() == newW.getTargetAudience();
            }
        });

        this.workoutList = newWorkoutList;
        result.dispatchUpdatesTo(this);
    }

    public interface OnWorkoutListener {
        void onEdit(WorkoutTraining workout);

        void onDelete(WorkoutTraining workout);
    }

    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvCategory;
        private final ImageView btnEdit, btnDelete;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.workout_name);
            tvCategory = itemView.findViewById(R.id.workout_category);
            btnEdit = itemView.findViewById(R.id.btn_edit_workout);
            btnDelete = itemView.findViewById(R.id.btn_delete_workout);

        }

        public void bind(final WorkoutTraining workout, final OnWorkoutListener onWorkoutListener) {
            tvName.setText(workout.getName());
            if (workout.getTargetAudience() != null) {
                tvCategory.setText(getTranslatedAudience(workout.getTargetAudience()));
            } else {
                tvCategory.setText(""); // Or some default text
            }
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

        private String getTranslatedAudience(WeightCategory audience) {
            if (audience == null) {
                return "";
            }
            switch (audience) {
                case UNDERWEIGHT:
                    return "תת משקל";
                case NORMAL:
                    return "משקל תקין";
                case OVERWEIGHT:
                    return "עודף משקל";
                default:
                    return audience.toString();
            }
        }
    }
}
