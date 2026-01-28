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

import java.util.List;
import java.util.function.UnaryOperator;

public class UsersListActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private UserAdapter adapter;
    private DatabaseService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list); // מחבר לעיצוב עם הכותרת הכחולה

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.users_list), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbService = DatabaseService.getInstance();
        rvUsers = findViewById(R.id.rv_users);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        // אתחול האדאפטר עם הממשק החדש (כולל מחיקה)
        adapter = new UserAdapter(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                // מעבר לדף הפרופיל הקיים עם ה-ID של המשתמש
                Intent intent = new Intent(UsersListActivity.this, UserProfileActivity.class);
                intent.putExtra("user_id", user.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(User user) {
                // שימוש בפונקציית המחיקה שקיימת אצלך ב-DatabaseService
                dbService.deleteUser(user.getId(), new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) {
                        Toast.makeText(UsersListActivity.this, "משתמש נמחק", Toast.LENGTH_SHORT).show();
                        loadUsers(); // רענון הרשימה
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
                user.setAdmin(!user.isAdmin());
                dbService.updateUser(user.getId(), new UnaryOperator<User>() {
                    @Override
                    public User apply(User u) {
                        if (u == null) return u;
                        u.setAdmin(user.isAdmin());
                        return u;
                    }
                }, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void v) {
                        adapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });
            }
        });

        rvUsers.setAdapter(adapter);
        loadUsers();
    }

    private void loadUsers() {
        // שימוש בשם הפונקציה הנכון מה-Service שלך
        dbService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                if (users != null) {
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