package com.gaganyatris.gaganyatri;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navHome, navHistory, navBlog, navSettings;
    private ImageView homeIcon, historyIcon, blogIcon, settingsIcon;
    private TextView homeText, historyText, blogText, settingsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //NavBar
        navHome = findViewById(R.id.nav_home);
        navHistory = findViewById(R.id.nav_history);
        navBlog = findViewById(R.id.nav_blog);
        navSettings = findViewById(R.id.nav_settings);

        //Icons
        homeIcon = findViewById(R.id.HomeIcon);
        historyIcon = findViewById(R.id.HistoryIcon);
        blogIcon = findViewById(R.id.BlogIcon);
        settingsIcon = findViewById(R.id.SettingsIcon);

        //Nav_text
        homeText = findViewById(R.id.HomeText);
        historyText = findViewById(R.id.HistoryText);
        blogText = findViewById(R.id.BlogText);
        settingsText = findViewById(R.id.SettingsText);

        setActiveTab(1);

        navHome.setOnClickListener(view -> setActiveTab(1));
        navHistory.setOnClickListener(view -> setActiveTab(2));
        navBlog.setOnClickListener(view -> setActiveTab(3));
        navSettings.setOnClickListener(view -> setActiveTab(4));

    }

    private void setActiveTab(int tabId) {
        resetTabs(); // Clear previous active state

        switch (tabId) {
            case 1:
                homeIcon.setImageResource(R.drawable.home_active); // Use your active icon
                homeText.setTextColor(Color.parseColor("#FF4F5A"));
                break;
            case 2:
                historyIcon.setImageResource(R.drawable.history_active); // Use your active icon
                historyText.setTextColor(Color.parseColor("#FF4F5A"));
                break;
            case 3:
                blogIcon.setImageResource(R.drawable.blog_active); // Use your active icon
                blogText.setTextColor(Color.parseColor("#FF4F5A"));
                break;
            case 4:
                settingsIcon.setImageResource(R.drawable.settings_active); // Use your active icon
                settingsText.setTextColor(Color.parseColor("#FF4F5A"));
                break;
        }
    }

    private void resetTabs() {
        // Reset all icons and text colors to their default state
        homeIcon.setImageResource(R.drawable.home);  // Your default icon
        homeText.setTextColor(ContextCompat.getColor(this, R.color.nav_icon_color)); // Or your default color resource

        historyIcon.setImageResource(R.drawable.history);
        historyText.setTextColor(ContextCompat.getColor(this, R.color.nav_icon_color));

        blogIcon.setImageResource(R.drawable.blog);
        blogText.setTextColor(ContextCompat.getColor(this, R.color.nav_icon_color));

        settingsIcon.setImageResource(R.drawable.settings);
        settingsText.setTextColor(ContextCompat.getColor(this, R.color.nav_icon_color));
    }
}