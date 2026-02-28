package com.example.fitcoach.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.models.WorkoutTraining;

import java.util.List;

public class WorkoutChecklistAdapter extends RecyclerView.Adapter<WorkoutChecklistAdapter.WorkoutViewHolder> {

    private List<WorkoutTraining> workoutList;
    private Context context;

    public WorkoutChecklistAdapter(Context context, List<WorkoutTraining> workoutList) {
        this.context = context;
        this.workoutList = workoutList;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_checklist, parent, false);
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

    public void setWorkoutList(List<WorkoutTraining> workouts) {
        this.workoutList = workouts;
        notifyDataSetChanged();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvExerciseName;
        CheckBox cbDone;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tv_exercise_name);
            cbDone = itemView.findViewById(R.id.cb_done);
        }

        public void bind(final WorkoutTraining workout) {
            tvExerciseName.setText(workout.getName());
            // You can add a listener to the checkbox to handle clicks
            cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Handle the case where the workout is marked as done
                }
            });
        }
    }
}
