package com.gaganyatris.gaganyatri;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

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

    final int statusBarColor = R.color.newStatusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_co_traveller);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));

        // Load PersonalDetailsFragment initially


        ImageButton back = findViewById(R.id.backBtn);
        back.setOnClickListener(v -> finish());

        CoTravellerDetailsFragment fragment = new CoTravellerDetailsFragment();
        String coTravellerId = getIntent().getStringExtra("coTraveller_id");
        if(coTravellerId != null){
//            TextView header = findViewById(R.id.textView2);
//            header.setText("Edit Co Traveller");
            Bundle bundle = new Bundle();
            bundle.putString("coTraveller_id", coTravellerId);
            fragment.setArguments(bundle);
        }

        if (savedInstanceState == null) {
            loadFragment(fragment, false);
        }
    }

    public void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

}