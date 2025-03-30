package com.gaganyatris.gaganyatri;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChangeLanguageActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private Button saveBtn;
    private ConstraintLayout eng, hindi, marathi;
    private static final int STATUS_BAR_COLOR = R.color.newStatusBar;
    private String selectedLanguage = "English"; // Default language

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_language);
        setupWindowInsets();
        getWindow().setStatusBarColor(ContextCompat.getColor(this, STATUS_BAR_COLOR));
        initializeViews();
        setupListeners();
        disableSaveButton(); // Initially disable save button
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        backBtn = findViewById(R.id.backBtn);
        saveBtn = findViewById(R.id.saveBtn);
        eng = findViewById(R.id.english);
        marathi = findViewById(R.id.marathi);
        hindi = findViewById(R.id.hindi);
    }

    private void setupListeners() {
        backBtn.setOnClickListener(view -> finish());
        eng.setOnClickListener(v -> setLanguage("English"));
        hindi.setOnClickListener(v -> setLanguage("Hindi"));
        marathi.setOnClickListener(v -> setLanguage("Marathi"));
        saveBtn.setOnClickListener(view -> saveLanguageAndExit());
    }

    private void setLanguage(String language) {
        resetTabs();
        selectedLanguage = language;
        switch (language) {
            case "English":
                eng.setBackgroundResource(R.drawable.card_language_active);
                break;
            case "Hindi":
                hindi.setBackgroundResource(R.drawable.card_language_active);
                break;
            case "Marathi":
                marathi.setBackgroundResource(R.drawable.card_language_active);
                break;
        }
        enableSaveButton();
    }

    private void saveLanguageAndExit() {
        Toast.makeText(this, "Language changed to " + selectedLanguage, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void resetTabs() {
        eng.setBackgroundResource(R.drawable.card_settings);
        hindi.setBackgroundResource(R.drawable.card_settings);
        marathi.setBackgroundResource(R.drawable.card_settings);
    }

    private void disableSaveButton() {
        hindi.setEnabled(false);
        marathi.setEnabled(false);
        saveBtn.setEnabled(false);
        saveBtn.setAlpha(0.5f); // Reduce opacity
        hindi.setAlpha(0.5f);
        marathi.setAlpha(0.5f);
    }

    private void enableSaveButton() {
        saveBtn.setEnabled(true);
        saveBtn.setAlpha(1.0f); // Restore full opacity
    }
}