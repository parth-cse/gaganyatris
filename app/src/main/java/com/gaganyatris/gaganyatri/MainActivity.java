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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    final int statusBarColor = R.color.primaryColor;
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
        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));
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

        navHome.setOnClickListener(view -> setActiveTab(1));
        navHistory.setOnClickListener(view -> setActiveTab(2));
        navBlog.setOnClickListener(view -> setActiveTab(3));
        navSettings.setOnClickListener(view -> setActiveTab(4));

        // Load the default fragment (HomeFragment)
        setActiveTab(1);
    }

    private void setActiveTab(int tabId) {
        resetTabs();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment = null;

        switch (tabId) {
            case 1:
                homeIcon.setImageResource(R.drawable.home_active);
                homeText.setTextColor(Color.parseColor("#FF4F5A"));
                fragment = new HomeFragment(); // Replace with your HomeFragment class
                break;
            case 2:
                historyIcon.setImageResource(R.drawable.history_active);
                historyText.setTextColor(Color.parseColor("#FF4F5A"));
                fragment = new HistoryFragment(); // Replace with your HistoryFragment class
                break;
            case 3:
                blogIcon.setImageResource(R.drawable.blog_active);
                blogText.setTextColor(Color.parseColor("#FF4F5A"));
                fragment = new BlogFragment(); // Replace with your BlogFragment class
                break;
            case 4:
                settingsIcon.setImageResource(R.drawable.settings_active);
                settingsText.setTextColor(Color.parseColor("#FF4F5A"));
                fragment = new SettingsFragment(); // Replace with your SettingsFragment class
                break;
        }

        if (fragment != null) {
            ft.replace(R.id.fragment_container, fragment);
            ft.commit();
        }
    }


    private void resetTabs() {
        homeIcon.setImageResource(R.drawable.home);
        homeText.setTextColor(ContextCompat.getColor(this, R.color.nav_icon_color));

        historyIcon.setImageResource(R.drawable.history);
        historyText.setTextColor(ContextCompat.getColor(this, R.color.nav_icon_color));

        blogIcon.setImageResource(R.drawable.blog);
        blogText.setTextColor(ContextCompat.getColor(this, R.color.nav_icon_color));

        settingsIcon.setImageResource(R.drawable.settings);
        settingsText.setTextColor(ContextCompat.getColor(this, R.color.nav_icon_color));
    }
}