package com.example.fitcoach.screens;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.fitcoach.R;
import com.example.fitcoach.models.Recipe;
import com.example.fitcoach.services.DatabaseService;
import com.example.fitcoach.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddRecipeActivity extends AppCompatActivity {

    private EditText etTitle, etPrepTime, etCalories, etAllergens, etIngredients, etInstructions;
    private Button btnSave, btnQuickPaste;
    private TextView tvTitle;
    private ImageView ivPreview;
    private CardView cvSelectImage;
    private LinearLayout llUploadPrompt;
    
    private DatabaseService dbService;
    private String recipeIdToEdit = null;
    private String originalUserId = null;
    private String currentImageUrl = null;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    handleImageSelection(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_recipe);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbService = DatabaseService.getInstance();
        bindViews();

        recipeIdToEdit = getIntent().getStringExtra("RECIPE_ID");
        if (recipeIdToEdit != null) {
            tvTitle.setText("עריכת מתכון");
            btnSave.setText("עדכן מתכון");
            loadRecipeData(recipeIdToEdit);
        }

        cvSelectImage.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        btnQuickPaste.setOnClickListener(v -> pasteImageFromClipboard());

        btnSave.setOnClickListener(v -> handleSaveProcess());
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.tvAddRecipeTitle);
        etTitle = findViewById(R.id.etRecipeTitle);
        etPrepTime = findViewById(R.id.etPrepTime);
        etCalories = findViewById(R.id.etCalories);
        etAllergens = findViewById(R.id.etAllergens);
        etIngredients = findViewById(R.id.etIngredients);
        etInstructions = findViewById(R.id.etInstructions);
        btnSave = findViewById(R.id.btnSaveRecipe);
        btnQuickPaste = findViewById(R.id.btnQuickPaste);
        ivPreview = findViewById(R.id.ivRecipePreview);
        cvSelectImage = findViewById(R.id.cvSelectImage);
        llUploadPrompt = findViewById(R.id.llUploadPrompt);
    }

    private void handleImageSelection(Uri uri) {
        selectedImageUri = uri;
        ivPreview.setImageURI(uri);
        llUploadPrompt.setVisibility(View.GONE);
        currentImageUrl = null; // Reset if we had an old URL
    }

    private void pasteImageFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                ClipData.Item item = clip.getItemAt(0);
                Uri uri = item.getUri();
                if (uri != null) {
                    handleImageSelection(uri);
                    Toast.makeText(this, "התמונה הודבקה בהצלחה!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "לא נמצאה תמונה בלוח. העתק תמונה או צילום מסך ונסה שוב.", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Toast.makeText(this, "הלוח ריק", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRecipeData(String recipeId) {
        dbService.getRecipe(recipeId, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Recipe recipe) {
                if (recipe != null) {
                    originalUserId = recipe.getUserId();
                    etTitle.setText(recipe.getTitle());
                    currentImageUrl = recipe.getImageUrl();
                    etPrepTime.setText(String.valueOf(recipe.getPrepTimeInMinutes()));
                    etCalories.setText(String.valueOf(recipe.getCalories()));
                    etAllergens.setText(recipe.getAllergens() != null ? String.join(", ", recipe.getAllergens()) : "");
                    etIngredients.setText(recipe.getIngredients() != null ? String.join("\n", recipe.getIngredients()) : "");
                    etInstructions.setText(recipe.getInstructions() != null ? String.join("\n", recipe.getInstructions()) : "");
                    
                    if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                        Glide.with(AddRecipeActivity.this).load(currentImageUrl).into(ivPreview);
                        llUploadPrompt.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AddRecipeActivity.this, "שגיאה בטעינת נתוני המתכון", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSaveProcess() {
        if (selectedImageUri != null) {
            btnSave.setEnabled(false);
            btnSave.setText("מעלה תמונה...");
            dbService.uploadImage(selectedImageUri, "recipe_images", new DatabaseService.DatabaseCallback<String>() {
                @Override
                public void onCompleted(String downloadUrl) {
                    currentImageUrl = downloadUrl;
                    saveRecipe();
                }

                @Override
                public void onFailed(Exception e) {
                    btnSave.setEnabled(true);
                    btnSave.setText(recipeIdToEdit != null ? "עדכן מתכון" : "הוסף מתכון");
                    Toast.makeText(AddRecipeActivity.this, "שגיאה בהעלאת תמונה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            saveRecipe();
        }
    }

    private void saveRecipe() {
        String title = etTitle.getText().toString().trim();
        String prepTimeStr = etPrepTime.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();
        String allergensStr = etAllergens.getText().toString().trim();
        String ingredientsStr = etIngredients.getText().toString().trim();
        String instructionsStr = etInstructions.getText().toString().trim();

        if (title.isEmpty() || prepTimeStr.isEmpty() || caloriesStr.isEmpty() || ingredientsStr.isEmpty() || instructionsStr.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות.", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true);
            btnSave.setText(recipeIdToEdit != null ? "עדכן מתכון" : "הוסף מתכון");
            return;
        }

        if (currentImageUrl == null || currentImageUrl.isEmpty()) {
            Toast.makeText(this, "נא לבחור תמונה למתכון.", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true);
            btnSave.setText(recipeIdToEdit != null ? "עדכן מתכון" : "הוסף מתכון");
            return;
        }

        int prepTime = Integer.parseInt(prepTimeStr);
        int calories = Integer.parseInt(caloriesStr);

        List<String> allergens = textToList(allergensStr, ",");
        List<String> ingredients = textToList(ingredientsStr, "\n");
        List<String> instructions = textToList(instructionsStr, "\n");

        String id = (recipeIdToEdit != null) ? recipeIdToEdit : dbService.generateRecipeId();
        String userId = (recipeIdToEdit != null) ? originalUserId : SharedPreferencesUtil.getUserId(this);

        Recipe recipe = new Recipe(id, userId, title, currentImageUrl, calories, prepTime, allergens, ingredients, instructions);

        dbService.createNewRecipe(recipe, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                String msg = (recipeIdToEdit != null) ? "המתכון עודכן בהצלחה!" : "המתכון עלה לפורום!";
                Toast.makeText(AddRecipeActivity.this, msg, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                btnSave.setEnabled(true);
                btnSave.setText(recipeIdToEdit != null ? "עדכן מתכון" : "הוסף מתכון");
                Toast.makeText(AddRecipeActivity.this, "שגיאה בשמירת המתכון: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<String> textToList(String text, String delimiter) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(text.split("\\s*" + delimiter + "\\s*")));
    }
}
