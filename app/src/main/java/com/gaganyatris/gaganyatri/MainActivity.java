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

public class MainActivity extends AppCompatActivity {

    private static final int STATUS_BAR_COLOR = R.color.primaryColor;
    private static final int ACTIVE_TAB_COLOR = Color.parseColor("#FF4F5A");
    private static final int INACTIVE_TAB_COLOR = R.color.nav_icon_color;
    private LinearLayout navHome, navHistory, navBlog, navSettings;
    private ImageView homeIcon, historyIcon, blogIcon, settingsIcon;
    private TextView homeText, historyText, blogText, settingsText;
    private int currentTab = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setupWindowInsets();
        getWindow().setStatusBarColor(ContextCompat.getColor(this, STATUS_BAR_COLOR));
        initializeViews();
        setupNavigationListeners();
        setActiveTab(1); // Load default fragment

        setupBackPressedCallback();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        navHome = findViewById(R.id.nav_home);
        navHistory = findViewById(R.id.nav_history);
        navBlog = findViewById(R.id.nav_blog);
        navSettings = findViewById(R.id.nav_settings);

        homeIcon = findViewById(R.id.HomeIcon);
        historyIcon = findViewById(R.id.HistoryIcon);
        blogIcon = findViewById(R.id.BlogIcon);
        settingsIcon = findViewById(R.id.SettingsIcon);

        homeText = findViewById(R.id.HomeText);
        historyText = findViewById(R.id.HistoryText);
        blogText = findViewById(R.id.BlogText);
        settingsText = findViewById(R.id.SettingsText);
    }

    private void setupNavigationListeners() {
        navHome.setOnClickListener(view -> setActiveTab(1));
        navHistory.setOnClickListener(view -> setActiveTab(2));
        navBlog.setOnClickListener(view -> setActiveTab(3));
        navSettings.setOnClickListener(view -> setActiveTab(4));
    }

    private void setupBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentTab != 1) {
                    setActiveTab(1);
                } else {
                    finish();
                }
            }
        });
    }

    private void setActiveTab(int tabId) {
        resetTabs();
        int previousTab = currentTab;
        currentTab = tabId;

        Fragment fragment = getFragmentForTab(tabId);
        if (fragment != null) {
            setTabActive(tabId);
            replaceFragment(fragment, previousTab);
        }
    }

    private Fragment getFragmentForTab(int tabId) {
        switch (tabId) {
            case 1: return new HomeFragment();
            case 2: return new HistoryFragment();
            case 3: return new BlogFragment();
            case 4: return new SettingsFragment();
            default: return null;
        }
    }

    private void setTabActive(int tabId) {
        switch (tabId) {
            case 1: homeIcon.setImageResource(R.drawable.home_active); homeText.setTextColor(ACTIVE_TAB_COLOR); navHome.setEnabled(false); break;
            case 2: historyIcon.setImageResource(R.drawable.history_active); historyText.setTextColor(ACTIVE_TAB_COLOR); navHistory.setEnabled(false); break;
            case 3: blogIcon.setImageResource(R.drawable.blog_active); blogText.setTextColor(ACTIVE_TAB_COLOR); navBlog.setEnabled(false); break;
            case 4: settingsIcon.setImageResource(R.drawable.settings_active); settingsText.setTextColor(ACTIVE_TAB_COLOR); navSettings.setEnabled(false); break;
        }
    }

    private void replaceFragment(Fragment fragment, int previousTab) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        int enterAnim = (currentTab > previousTab) ? R.anim.slide_in_right : R.anim.slide_in_left;
        int exitAnim = (currentTab > previousTab) ? R.anim.slide_out_left : R.anim.slide_out_right;

        ft.setCustomAnimations(enterAnim, exitAnim);
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    private void resetTabs() {
        homeIcon.setImageResource(R.drawable.home); homeText.setTextColor(ContextCompat.getColor(this, INACTIVE_TAB_COLOR)); navHome.setEnabled(true);
        historyIcon.setImageResource(R.drawable.history); historyText.setTextColor(ContextCompat.getColor(this, INACTIVE_TAB_COLOR)); navHistory.setEnabled(true);
        blogIcon.setImageResource(R.drawable.blog); blogText.setTextColor(ContextCompat.getColor(this, INACTIVE_TAB_COLOR)); navBlog.setEnabled(true);
        settingsIcon.setImageResource(R.drawable.settings); settingsText.setTextColor(ContextCompat.getColor(this, INACTIVE_TAB_COLOR)); navSettings.setEnabled(true);
    }
}