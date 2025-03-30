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

    private TextView reminderFrequencyTextView;
    private Switch travelRemindersSwitch;
    private LinearLayout frequencyDropdownLayout;
    private String reminderFrequency = "Monthly";
    private static final int STATUS_BAR_COLOR = R.color.newStatusBar;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "TravelSettingsPrefs";
    private static final String KEY_REMINDERS_ENABLED = "remindersEnabled";
    private static final String KEY_REMINDER_FREQUENCY = "reminderFrequency";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    scheduleNotification();
                    savePreferences();
                } else {
                    travelRemindersSwitch.setChecked(false);
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
        getWindow().setStatusBarColor(ContextCompat.getColor(this, STATUS_BAR_COLOR));

        initializeViews();
        loadPreferences();
        setupListeners();
    }

    private void initializeViews() {
        ImageButton backButton = findViewById(R.id.backBtn);
        backButton.setOnClickListener(v -> finish());

        reminderFrequencyTextView = findViewById(R.id.textView8);
        travelRemindersSwitch = findViewById(R.id.travelreminders);
        frequencyDropdownLayout = findViewById(R.id.drpdown);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void loadPreferences() {
        boolean remindersEnabled = sharedPreferences.getBoolean(KEY_REMINDERS_ENABLED, false);
        reminderFrequency = sharedPreferences.getString(KEY_REMINDER_FREQUENCY, "Monthly");

        travelRemindersSwitch.setChecked(remindersEnabled);
        updateReminderFrequencyText(reminderFrequency);
        updateFrequencyDropdownState(remindersEnabled);
    }

    private void setupListeners() {
        travelRemindersSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkAndRequestNotificationPermission();
                updateFrequencyDropdownState(true);
            } else {
                updateFrequencyDropdownState(false);
                cancelNotification();
            }
            savePreferences();
        });

        frequencyDropdownLayout.setOnClickListener(this::showFrequencyDropdownMenu);
    }

    private void updateFrequencyDropdownState(boolean enabled) {
        frequencyDropdownLayout.setEnabled(enabled);
        frequencyDropdownLayout.setAlpha(enabled ? 1f : 0.5f);

        int color = enabled ? R.color.holo_green_dark : android.R.color.darker_gray;
        int trackColor = enabled ? R.color.holo_green_light : android.R.color.darker_gray;

        travelRemindersSwitch.setThumbTintList(enabled ? ColorStateList.valueOf(ContextCompat.getColor(this, color)) : null);
        travelRemindersSwitch.setTrackTintList(enabled ? ColorStateList.valueOf(ContextCompat.getColor(this, trackColor)) : null);
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REMINDERS_ENABLED, travelRemindersSwitch.isChecked());
        editor.putString(KEY_REMINDER_FREQUENCY, reminderFrequency);
        editor.apply();
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            scheduleNotification();
        }
    }

    private void showFrequencyDropdownMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.travel_reminder, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.weekly_option) reminderFrequency = "Weekly";
            else if (itemId == R.id.monthly_option) reminderFrequency = "Monthly";
            else if (itemId == R.id.quaterly_option) reminderFrequency = "Quarterly";
            else if (itemId == R.id.bi_yearly_option) reminderFrequency = "6 Months";
            else if (itemId == R.id.yearly_option) reminderFrequency = "Yearly";

            updateReminderFrequencyText(reminderFrequency);
            if (travelRemindersSwitch.isChecked()) {
                scheduleNotification();
            }
            savePreferences();
            return true;
        });

        popupMenu.show();
    }

    private void updateReminderFrequencyText(String text) {
        reminderFrequencyTextView.setText(text);
    }

    private void scheduleNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        long intervalMillis = calculateInterval();

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + intervalMillis, intervalMillis, pendingIntent);
    }

    private void cancelNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    private long calculateInterval() {
        switch (reminderFrequency) {
            case "Weekly": return TimeUnit.DAYS.toMillis(7);
            case "Monthly": return TimeUnit.DAYS.toMillis(30);
            case "Quarterly": return TimeUnit.DAYS.toMillis(90);
            case "6 Months": return TimeUnit.DAYS.toMillis(180);
            case "Yearly": return TimeUnit.DAYS.toMillis(365);
            default: return TimeUnit.DAYS.toMillis(30);
        }
    }
}