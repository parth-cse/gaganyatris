package com.gaganyatris.gaganyatri;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("travel_reminder", "Travel Reminders", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "travel_reminder")
                .setContentTitle("Travel Reminder")
                .setContentText("It's time to plan your next trip!")
                .setSmallIcon(R.mipmap.ic_launcher_round) // Replace with your notification icon
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}