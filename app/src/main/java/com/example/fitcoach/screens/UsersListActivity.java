package com.example.fitcoach.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.adapters.UserAdapter;
import com.example.fitcoach.models.User;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.SharedPreferencesUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;

public class UsersListActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private UserAdapter adapter;
    private DatabaseService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.users_list), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbService = DatabaseService.getInstance();
        rvUsers = findViewById(R.id.rv_users);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdapter(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(UsersListActivity.this, UserProfileActivity.class);
                intent.putExtra("user_id", user.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(User user) {
                dbService.deleteUser(user.getId(), new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) {
                        Toast.makeText(UsersListActivity.this, "משתמש נמחק", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(UsersListActivity.this, "המחיקה נכשלה", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onMakeAdminClick(User user, int position) {
                String currentId = SharedPreferencesUtil.getUserId(UsersListActivity.this);
                if (user.getId().equals(currentId)) {
                    return;
                }
                boolean newAdminStatus = !user.isAdmin();
                user.setAdmin(newAdminStatus);
                dbService.updateUser(user.getId(), new UnaryOperator<User>() {
                    @Override
                    public User apply(User u) {
                        if (u == null) return u;
                        u.setAdmin(newAdminStatus);
                        return u;
                    }
                }, new DatabaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User updatedUser) {
                        String message = newAdminStatus ? "מונה למנהל" : "הוסר מניהול";
                        Toast.makeText(UsersListActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadUsers();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(UsersListActivity.this, "העדכון נכשל", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        rvUsers.setAdapter(adapter);
        loadUsers();
    }

    private void loadUsers() {
        dbService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                if (users != null) {
                    String currentUserId = SharedPreferencesUtil.getUserId(UsersListActivity.this);
                    Collections.sort(users, new Comparator<User>() {
                        @Override
                        public int compare(User u1, User u2) {
                            // Main admin (current user) on top
                            if (u1.getId().equals(currentUserId)) return -1;
                            if (u2.getId().equals(currentUserId)) return 1;

                            // Other admins
                            if (u1.isAdmin() && !u2.isAdmin()) return -1;
                            if (!u1.isAdmin() && u2.isAdmin()) return 1;

                            // Regular users (sorted by name)
                            return u1.getName().compareTo(u2.getName());
                        }
                    });
                    adapter.setUserList(users);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UsersListActivity.this, "שגיאה בטעינת משתמשים", Toast.LENGTH_SHORT).show();
            }
        });
    }
}