package com.gaganyatris.gaganyatri;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class TravelSettingsActivity extends AppCompatActivity {

    private TextView textView8;
    private Switch travelReminders;
    private LinearLayout dropDown;
    private String reminderFrequency = "Monthly"; // Default frequency
    final int statusBarColor = R.color.newStatusBar;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "TravelSettingsPrefs";
    private static final String KEY_REMINDERS_ENABLED = "remindersEnabled";
    private static final String KEY_REMINDER_FREQUENCY = "reminderFrequency";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your app.
                    scheduleNotification();
                    savePreferences();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the same time,
                    // respect the user's decision. Don't link to system settings in an effort
                    // to convince the user to change their decision.
                    Toast.makeText(this, "Notifications permission denied.", Toast.LENGTH_SHORT).show();
                    travelReminders.setChecked(false); // Make sure the switch is off if permission is denied.
                }
            });

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
        backBTN.setOnClickListener(v -> finish());
        textView8 = findViewById(R.id.textView8);
        travelReminders = findViewById(R.id.travelreminders);
        dropDown = findViewById(R.id.drpdown);
//        dropDown.setEnabled(false);
//        dropDown.setAlpha(0.5f);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadPreferences(); // Load saved preferences



        travelReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkAndRequestNotificationPermission();
                travelReminders.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.holo_green_dark)));
                travelReminders.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.holo_green_light)));
                dropDown.setEnabled(true);
                dropDown.setAlpha(1f);
            } else {
                travelReminders.setThumbTintList(null);
                travelReminders.setTrackTintList(null);
                dropDown.setEnabled(false);
                dropDown.setAlpha(0.5f);
                cancelNotification();
            }
        });

        dropDown.setOnClickListener(this::showDropdownMenu);
    }

    private void loadPreferences() {
        boolean remindersEnabled = sharedPreferences.getBoolean(KEY_REMINDERS_ENABLED, false);
        reminderFrequency = sharedPreferences.getString(KEY_REMINDER_FREQUENCY, "Monthly");
        travelReminders.setChecked(remindersEnabled);
        updateTextView(reminderFrequency);

        if(remindersEnabled){
            dropDown.setEnabled(true);
            dropDown.setAlpha(1f);
            travelReminders.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.holo_green_dark)));
            travelReminders.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.holo_green_light)));
        } else {
            dropDown.setEnabled(false);
            dropDown.setAlpha(0.5f);
        }
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REMINDERS_ENABLED, travelReminders.isChecked());
        editor.putString(KEY_REMINDER_FREQUENCY, reminderFrequency);
        editor.apply();
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // You can use the API that requires the permission.
                scheduleNotification();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Toast.makeText(this, "Notifications are needed for travel reminders.", Toast.LENGTH_LONG).show();
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            scheduleNotification();
        }
    }

    private void showDropdownMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.travel_reminder, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.weekly_option) {
                updateTextView("Weekly");
                reminderFrequency = "Weekly";
            } else if (itemId == R.id.monthly_option) {
                updateTextView("Monthly");
                reminderFrequency = "Monthly";
            } else if (itemId == R.id.quaterly_option) {
                updateTextView("Quarterly");
                reminderFrequency = "Quarterly";
            } else if (itemId == R.id.bi_yearly_option) {
                updateTextView("6 Months");
                reminderFrequency = "6 Months";
            } else if (itemId == R.id.yearly_option) {
                updateTextView("Yearly");
                reminderFrequency = "Yearly";
            }
            if (travelReminders.isChecked()) {
                scheduleNotification(); // Re-schedule with new frequency
            }
            return true;
        });

        popupMenu.show();
    }

    private void updateTextView(String text) {
        textView8.setText(text);
    }

    private void scheduleNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        long intervalMillis = calculateInterval();

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + intervalMillis, intervalMillis, pendingIntent);
        Toast.makeText(this, "Notification Scheduled", Toast.LENGTH_SHORT).show();
    }

    private void cancelNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        Toast.makeText(this, "Notification Canceled", Toast.LENGTH_SHORT).show();
    }

    private long calculateInterval() {
        switch (reminderFrequency) {
            case "Weekly":
                return TimeUnit.DAYS.toMillis(7);
            case "Monthly":
                return TimeUnit.DAYS.toMillis(30);
            case "Quarterly":
                return TimeUnit.DAYS.toMillis(90);
            case "6 Months":
                return TimeUnit.DAYS.toMillis(180);
            case "Yearly":
                return TimeUnit.DAYS.toMillis(365);
            default:
                return TimeUnit.DAYS.toMillis(30); // Default to monthly
        }
    }
}