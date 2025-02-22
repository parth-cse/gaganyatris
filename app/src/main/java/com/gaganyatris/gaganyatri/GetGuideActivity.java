package com.gaganyatris.gaganyatri;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GetGuideActivity extends AppCompatActivity {

    final int statusBarColor = R.color.newStatusBar;
    LinearLayout optionsLayout;
    View bgView; // Add a reference to the background view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_get_guide);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));
        ImageButton backBTN = findViewById(R.id.backBtn);

        optionsLayout = findViewById(R.id.optionsLayout);
        bgView = findViewById(R.id.bg); // Initialize the background view

        // Example: Show the optionsLayout when a trip is clicked
        findViewById(R.id.trip1).setOnClickListener(v -> {
            bgView.setVisibility(View.VISIBLE);
            optionsLayout.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.trip2).setOnClickListener(v -> {
            bgView.setVisibility(View.VISIBLE);
            optionsLayout.setVisibility(View.VISIBLE);
        });

        backBTN.setOnClickListener(v -> {
            if (optionsLayout.getVisibility() == View.VISIBLE) {
                optionsLayout.setVisibility(View.GONE);
                bgView.setVisibility(View.GONE);
            } else {
                finish();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (optionsLayout.getVisibility() == View.VISIBLE) {
                    optionsLayout.setVisibility(View.GONE);
                    bgView.setVisibility(View.GONE);
                } else {
                    finish();
                }
            }
        });
    }

}