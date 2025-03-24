package com.gaganyatris.gaganyatri;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ImageSearchActivity extends AppCompatActivity {

    final int statusBarColor = R.color.primaryColor;
    private LinearLayout navLens, navUpload;
    private ImageView lensIcon, uploadIcon;
    private TextView lensText, uploadText;
    private int currentTab = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));
        //NavBar
        navLens = findViewById(R.id.nav_lens);
        navUpload = findViewById(R.id.nav_upload);

        //Icons
        lensIcon = findViewById(R.id.lensIcon);
        uploadIcon = findViewById(R.id.uploadIcon);

        //Nav_text
        lensText = findViewById(R.id.lensText);
        uploadText = findViewById(R.id.uploadText);

        navLens.setOnClickListener(view -> setActiveTab(1));
        navUpload.setOnClickListener(view -> setActiveTab(2));
        
        // Load the default fragment (HomeFragment)
        setActiveTab(2);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (currentTab != 2) { // If not on the Home tab
                    setActiveTab(2); // Navigate to Home Fragment
                } else {
                    finish(); // Exit the app
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        setActiveTab(2);
    }

    private void setActiveTab(int tabId) {
        resetTabs();
        int previousTab = currentTab; // Store the previous tab
        currentTab = tabId;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        int enterAnim;
        int exitAnim;

        if (tabId > previousTab) {
            // Sliding in from the right, sliding out to the left
            enterAnim = R.anim.slide_in_right;  // Create these files
            exitAnim = R.anim.slide_out_left; // Create these files
        } else {
            // Sliding in from the left, sliding out to the right
            enterAnim = R.anim.slide_in_left;  // Create these files
            exitAnim = R.anim.slide_out_right; // Create these files

        }

        ft.setCustomAnimations(enterAnim, exitAnim);


        Fragment fragment = null;

        switch (tabId) {
            case 1:
                lensIcon.setImageResource(R.drawable.camera_active);
                lensText.setTextColor(Color.parseColor("#FF4F5A"));
                fragment = new LensFragment(); // Replace with your HomeFragment class
                navLens.setEnabled(false);
                break;
            
            case 2:
                uploadIcon.setImageResource(R.drawable.images_active);
                uploadText.setTextColor(Color.parseColor("#FF4F5A"));
                fragment = new UploadImageFragment(); // Replace with your SettingsFragment class
                navUpload.setEnabled(false);
                break;
        }

        if (fragment != null) {
            ft.replace(R.id.fragment_container, fragment);
            ft.commit();
        }
    }


    private void resetTabs() {
        lensIcon.setImageResource(R.drawable.camera);
        lensText.setTextColor(ContextCompat.getColor(this, R.color.nav_icon_color));
        navLens.setEnabled(true);

        uploadIcon.setImageResource(R.drawable.images);
        uploadText.setTextColor(ContextCompat.getColor(this, R.color.nav_icon_color));
        navUpload.setEnabled(true);
    }
}
