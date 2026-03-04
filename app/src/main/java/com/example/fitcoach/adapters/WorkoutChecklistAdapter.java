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

                if (isChecked) {
                    // Update SharedPreferences immediately for UI responsiveness
                    Stats localStats = SharedPreferencesUtil.getStats(context);
                    if (localStats != null) {
                        localStats.setCalories(localStats.getCalories() + caloriesToChange);
                        SharedPreferencesUtil.saveStats(context, localStats);
                    }
                    SharedPreferencesUtil.addCompletedWorkout(context, workout.getId());

                    // Sync with Firebase Database
                    if (userId != null) {
                        DatabaseService.getInstance().updateStats(userId, stats -> {
                            if (stats == null) stats = new Stats();
                            stats.setCalories(stats.getCalories() + caloriesToChange);
                            return stats;
                        }, new DatabaseService.DatabaseCallback<>() {
                            @Override
                            public void onCompleted(Stats updatedStats) {
                                Toast.makeText(context, "כל הכבוד! האימון סונכרן למסד הנתונים", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailed(Exception e) {
                                Toast.makeText(context, "שגיאה בסנכרון: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    // Handle uncheck (optional)
                    Stats localStats = SharedPreferencesUtil.getStats(context);
                    if (localStats != null) {
                        localStats.setCalories(Math.max(0, localStats.getCalories() - caloriesToChange));
                        SharedPreferencesUtil.saveStats(context, localStats);
                    }
                    // Sync subtraction with Firebase
                    if (userId != null) {
                        DatabaseService.getInstance().updateStats(userId, stats -> {
                            if (stats != null) {
                                stats.setCalories(Math.max(0, stats.getCalories() - caloriesToChange));
                            }
                            return stats;
                        }, new DatabaseService.DatabaseCallback<>() {
                            @Override
                            public void onCompleted(Stats object) {
                            }

                            @Override
                            public void onFailed(Exception e) {
                            }
                        });
                    }
                }
            });
        }
    }
}
