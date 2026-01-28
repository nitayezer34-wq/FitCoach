package com.example.fitcoach.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitcoach.R;
import com.example.fitcoach.models.User;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList = new ArrayList<>();
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
        void onDeleteClick(User user);
        void onMakeAdminClick(User user, int position);
    }

    public UserAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setUserList(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        // עיצוב שם המשתמש
        holder.tvName.setText(user.getName());
        holder.tvEmail.setText(user.getEmail());

        // לוגיקת המנהל - עיצוב כחול/לבן/אפור
        if (user.isAdmin()) {
            // אם הוא מנהל: כוכב כחול מודגש
            holder.btnMakeAdmin.setImageResource(R.drawable.ic_star_custom); // הכוכב ששלחת
            holder.btnMakeAdmin.setColorFilter(Color.parseColor("#2196F3")); // כחול
            holder.tvName.setTextColor(Color.parseColor("#2196F3")); // גם השם בכחול
        } else {
            // אם הוא משתמש רגיל: כוכב אפור/חלול
            holder.btnMakeAdmin.setImageResource(R.drawable.ic_star_custom);
            holder.btnMakeAdmin.setColorFilter(Color.LTGRAY); // אפור ניטרלי
            holder.tvName.setTextColor(Color.parseColor("#212121")); // שם בצבע רגיל
        }

        // מאזינים
        holder.btnMakeAdmin.setOnClickListener(v -> {
            // כאן אנחנו קוראים לפונקציה ב-Activity/Fragment
            listener.onMakeAdminClick(user, position);
        });

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        ImageButton btnDelete, btnMakeAdmin;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvEmail = itemView.findViewById(R.id.tv_user_email);
            btnDelete = itemView.findViewById(R.id.btn_delete_user);
            btnMakeAdmin = itemView.findViewById(R.id.btn_make_admin);
        }
    }
}