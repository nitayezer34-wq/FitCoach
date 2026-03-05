package com.example.fitcoach.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.models.Stats;
import com.example.fitcoach.models.WorkoutTraining;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.SharedPreferencesUtil;

import java.util.List;

public class WorkoutChecklistAdapter extends RecyclerView.Adapter<WorkoutChecklistAdapter.WorkoutViewHolder> {

    private final Context context;
    private final String userId;
    private List<WorkoutTraining> workoutList;

    public WorkoutChecklistAdapter(Context context, List<WorkoutTraining> workoutList) {
        this.context = context;
        this.workoutList = workoutList;
        this.userId = SharedPreferencesUtil.getUserId(context);
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
        holder.bind(workout, context, userId);
    }

    @Override
    public int getItemCount() {
        return workoutList != null ? workoutList.size() : 0;
    }

    public void setWorkoutList(List<WorkoutTraining> workouts) {
        this.workoutList = workouts;
        notifyDataSetChanged();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        final TextView tvExerciseName;
        final CheckBox cbDone;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tv_exercise_name);
            cbDone = itemView.findViewById(R.id.cb_done);
        }

        public void bind(final WorkoutTraining workout, Context context, String userId) {
            tvExerciseName.setText(workout.getName());

            List<String> completedIds = SharedPreferencesUtil.getCompletedWorkouts(context);
            cbDone.setOnCheckedChangeListener(null);
            cbDone.setChecked(completedIds.contains(workout.getId()));

            cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int caloriesToChange = workout.getCaloriesPerSet() * workout.getSets();

                // 1. Update LOCAL stats immediately
                Stats localStats = SharedPreferencesUtil.getStats(context);
                if (localStats == null) localStats = new Stats();

                if (isChecked) {
                    localStats.setCalories(localStats.getCalories() + caloriesToChange);
                    SharedPreferencesUtil.addCompletedWorkout(context, workout.getId());
                } else {
                    localStats.setCalories(Math.max(0, localStats.getCalories() - caloriesToChange));
                    // Note: You might want a removeCompletedWorkout method in SharedPreferencesUtil
                }
                
                // CRITICAL: Save the updated stats back to SharedPreferences
                SharedPreferencesUtil.saveStats(context, localStats);

                // 2. Sync with Firebase
                if (userId != null) {
                    DatabaseService.getInstance().saveStats(userId, localStats, new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            // Optional: show feedback
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(context, "שגיאה בסנכרון: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }
}
