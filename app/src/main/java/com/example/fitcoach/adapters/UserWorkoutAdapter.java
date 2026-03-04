package com.example.fitcoach.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.models.WorkoutTraining;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class UserWorkoutAdapter extends RecyclerView.Adapter<UserWorkoutAdapter.UserWorkoutViewHolder> {

    private final List<WorkoutTraining> workoutList;
    private final OnWorkoutDoneListener onWorkoutDoneListener;

    public UserWorkoutAdapter(Context context, List<WorkoutTraining> workoutList, OnWorkoutDoneListener onWorkoutDoneListener) {
        this.workoutList = workoutList;
        this.onWorkoutDoneListener = onWorkoutDoneListener;
    }

    @NonNull
    @Override
    public UserWorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_workout, parent, false);
        return new UserWorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserWorkoutViewHolder holder, int position) {
        WorkoutTraining workout = workoutList.get(position);
        holder.bind(workout, position);
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    public interface OnWorkoutDoneListener {
        void onWorkoutDone(int position);
    }

    class UserWorkoutViewHolder extends RecyclerView.ViewHolder {
        final TextView workoutName;
        final TextView workoutSets;
        final TextView workoutReps;
        final TextView workoutDescription;
        final FloatingActionButton fabDone;

        public UserWorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            workoutName = itemView.findViewById(R.id.workout_name);
            workoutSets = itemView.findViewById(R.id.workout_sets);
            workoutReps = itemView.findViewById(R.id.workout_reps);
            workoutDescription = itemView.findViewById(R.id.workout_description);
            fabDone = itemView.findViewById(R.id.fab_done);
        }

        public void bind(final WorkoutTraining workout, final int position) {
            workoutName.setText(workout.getName());
            workoutSets.setText("סטים: " + workout.getSets());
            workoutReps.setText("חזרות: " + workout.getReps());
            workoutDescription.setText(workout.getDescription());

            fabDone.setOnClickListener(v -> {
                if (onWorkoutDoneListener != null) {
                    onWorkoutDoneListener.onWorkoutDone(getAdapterPosition());
                }
            });
        }
    }
}
