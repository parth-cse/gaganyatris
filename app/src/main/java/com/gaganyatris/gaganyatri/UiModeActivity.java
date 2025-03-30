package com.gaganyatris.gaganyatri;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UiModeActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private ConstraintLayout sys, dark, light;
    private static final int STATUS_BAR_COLOR = R.color.newStatusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ui_mode);
        setupWindowInsets();
        getWindow().setStatusBarColor(ContextCompat.getColor(this, STATUS_BAR_COLOR));
        initializeViews();
        setupListeners();
        setActiveMode(light); // Set light mode as active by default
        disableOtherModes(); // Disable dark and system default
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
        sys = findViewById(R.id.sysDefault);
        dark = findViewById(R.id.dark);
        light = findViewById(R.id.light);
    }

    private void setupListeners() {
        backBtn.setOnClickListener(view -> finish());
        sys.setOnClickListener(v -> setActiveMode(sys));
        light.setOnClickListener(v -> setActiveMode(light));
        dark.setOnClickListener(v -> setActiveMode(dark));
    }

    private void setActiveMode(ConstraintLayout activeMode) {
        resetTabs();
        activeMode.setBackgroundResource(R.drawable.card_language_active);
    }

    private void resetTabs() {
        sys.setBackgroundResource(R.drawable.card_settings);
        dark.setBackgroundResource(R.drawable.card_settings);
        light.setBackgroundResource(R.drawable.card_settings);
    }

    private void disableOtherModes() {
        sys.setEnabled(false);
        dark.setEnabled(false);
        sys.setAlpha(0.5f); // Reduce opacity
        dark.setAlpha(0.5f);
    }
}