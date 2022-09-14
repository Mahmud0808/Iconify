package com.drdisagree.iconify.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.HomePage;

public class BackgroundService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "Background Service";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "Background Service Started", Toast.LENGTH_LONG).show();

        startForeground();

        return super.onStartCommand(intent, flags, startId);
    }

    private void startForeground() {
        showNotification();
    }

    public void showNotification() {
        Intent notificationIntent = new Intent(this, HomePage.class);

        PendingIntent pendingIntent = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_fg)
                .setContentTitle("Background Service")
                .setContentText("Service is running in background")
                .setContentIntent(pendingIntent)
                .setSound(null, AudioManager.STREAM_NOTIFICATION)
                .setColor(getResources().getColor(R.color.colorAccent));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel(notificationManager);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    public void createChannel(NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Background Service", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("You can turn off these notifications.");
        notificationManager.createNotificationChannel(channel);
    }

}