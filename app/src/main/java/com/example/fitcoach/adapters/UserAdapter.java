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

        // אם המשתמש הוא אדמין - נצבע בכחול ונוסיף טקסט
        if (user.isAdmin()) {
            holder.tvName.setText(user.getName() + " (מנהל)");
            holder.tvName.setTextColor(Color.parseColor("#2196F3"));
            holder.btnMakeAdmin.setVisibility(View.GONE); // מנהל כבר לא צריך כפתור מינוי
        } else {
            holder.tvName.setText(user.getName());
            holder.tvName.setTextColor(Color.parseColor("#212121"));
            holder.btnMakeAdmin.setVisibility(View.VISIBLE);
        }

        holder.tvEmail.setText(user.getEmail());

        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(user));
        holder.btnMakeAdmin.setOnClickListener(v -> listener.onMakeAdminClick(user, position));
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