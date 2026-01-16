package com.example.fitcoach.screens;

import androidx.appcompat.app.AppCompatActivity;
import com.example.fitcoach.services.DatabaseService;

public class BaseActivity extends AppCompatActivity {
    // זה יפתור את השגיאה של databaseService ב-UserProfileActivity
    protected DatabaseService databaseService = DatabaseService.getInstance();
}