package com.gaganyatris.gaganyatri;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TravelSettingsActivity extends AppCompatActivity {

    private TextView textView8;

    final int statusBarColor = R.color.newStatusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_travel_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));

        ImageButton backBTN = findViewById(R.id.backBtn);
        backBTN.setOnClickListener(v -> {finish();});
        textView8 = findViewById(R.id.textView8);
        Switch travelReminders = findViewById(R.id.travelreminders);
        LinearLayout dropDown = findViewById(R.id.drpdown);
        dropDown.setEnabled(false);
        dropDown.setAlpha(0.5f);

        travelReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                travelReminders.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.holo_green_dark)));
                travelReminders.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.holo_green_light)));
                dropDown.setEnabled(true);
                dropDown.setAlpha(1f);
            } else {
                travelReminders.setThumbTintList(null); // Revert to default
                travelReminders.setTrackTintList(null);
                dropDown.setEnabled(false);
                dropDown.setAlpha(0.5f);
            }
        });

        dropDown.setOnClickListener(view -> showDropdownMenu(view));
    }

    private void showDropdownMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.travel_reminder, popupMenu.getMenu());

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.weekly_option) {
                updateTextView("Weekly");
            } else if (itemId == R.id.monthly_option) {
                updateTextView("Monthly");
            } else if (itemId == R.id.quaterly_option) {
                updateTextView("Quarterly");
            } else if (itemId == R.id.bi_yearly_option) {
                updateTextView("6 Months");
            } else if (itemId == R.id.yearly_option) {
                updateTextView("Yearly");
            }
            return true;
        });

        popupMenu.show();
    }

    // Method to update the TextView with the selected option
    private void updateTextView(String text) {
        textView8.setText(text);
    }
}
