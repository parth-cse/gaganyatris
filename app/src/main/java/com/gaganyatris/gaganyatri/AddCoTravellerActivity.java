package com.gaganyatris.gaganyatri;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class AddCoTravellerActivity extends AppCompatActivity {

    private static final int STATUS_BAR_COLOR = R.color.newStatusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_co_traveller);
        setupWindowInsets();
        setStatusBarColor();
        setBackButtonListener();
        loadInitialFragment(savedInstanceState);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setStatusBarColor() {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, STATUS_BAR_COLOR));
    }

    private void setBackButtonListener() {
        ImageButton backButton = findViewById(R.id.backBtn);
        backButton.setOnClickListener(v -> finish());
    }

    private void loadInitialFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            CoTravellerDetailsFragment detailsFragment = new CoTravellerDetailsFragment();
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey("coTraveller_id")) {
                Bundle args = new Bundle();
                args.putString("coTraveller_id", extras.getString("coTraveller_id"));
                detailsFragment.setArguments(args);
            }
            loadFragment(detailsFragment, false);
        }
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
}