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

public class UiModeActivity extends AppCompatActivity {
    ImageButton backBtn;
    final int statusBarColor = R.color.newStatusBar;

    ConstraintLayout sys, dark, light;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ui_mode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view -> finish());
        sys = findViewById(R.id.sysDefault);
        dark = findViewById(R.id.dark);
        light = findViewById(R.id.light);
        sys.setOnClickListener(v->setActiveMode(sys));
        light.setOnClickListener(v->setActiveMode(light));
        dark.setOnClickListener(v->setActiveMode(dark));
    }

    void setActiveMode(ConstraintLayout a){
        resetTabs();
        a.setBackgroundResource(R.drawable.card_language_active);
    }

    void resetTabs(){
        sys.setBackgroundResource(R.drawable.card_settings);
        dark.setBackgroundResource(R.drawable.card_settings);
        light.setBackgroundResource(R.drawable.card_settings);
    }
}