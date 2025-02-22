package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TripDetailsActivity extends AppCompatActivity {
    final int statusBarColor = R.color.primaryColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trip_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));

        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());

        LinearLayout tripPlan = findViewById(R.id.tripPlan);
        LinearLayout checkList = findViewById(R.id.checkList);
        LinearLayout mapView = findViewById(R.id.mapView);

        tripPlan.setOnClickListener(v -> startActivity(new Intent(TripDetailsActivity.this, TripPlanActivity.class)));
        checkList.setOnClickListener(v -> startActivity(new Intent(TripDetailsActivity.this, TripCheckListActivity.class)));
        mapView.setOnClickListener(v -> startActivity(new Intent(TripDetailsActivity.this, TripMapViewActivity.class)));
    }
}