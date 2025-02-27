package com.gaganyatris.gaganyatri;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChangeLanguageActivity extends AppCompatActivity {

    ImageButton backBtn;
    ConstraintLayout eng, hindi, marathi;
    final int statusBarColor = R.color.newStatusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_language);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));
        backBtn = findViewById(R.id.backBtn);
        eng = findViewById(R.id.english);
        marathi = findViewById(R.id.marathi);
        hindi = findViewById(R.id.hindi);
        backBtn.setOnClickListener(view -> finish());
        eng.setOnClickListener(v -> {
            resetTabs();
            eng.setBackgroundResource(R.drawable.card_language_active);
        });
        hindi.setOnClickListener(v -> {
            resetTabs();
            hindi.setBackgroundResource(R.drawable.card_language_active);
        });
        marathi.setOnClickListener(v -> {
            resetTabs();
            marathi.setBackgroundResource(R.drawable.card_language_active);
        });
    }

    void resetTabs(){
        eng.setBackgroundResource(R.drawable.card_settings);
        hindi.setBackgroundResource(R.drawable.card_settings);
        marathi.setBackgroundResource(R.drawable.card_settings);
    }
}