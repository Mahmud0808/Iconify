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

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.SplashActivity;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.ui.HomePage;
import com.drdisagree.iconify.utils.FabricatedOverlay;
import com.drdisagree.iconify.utils.OverlayUtils;

import java.util.List;
import java.util.Objects;

public class BackgroundService extends Service {

    private Context context;
    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "Background Service";

    @Override
    public void onCreate() {
        context = SplashActivity.getContext();
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        HomePage.isServiceRunning = true;
        List<String> overlays = FabricatedOverlay.getEnabledOverlayList();
        if (OverlayUtils.isOverlayDisabled(overlays, "IconifyComponentAMC.overlay") && (FabricatedOverlay.isOverlayDisabled(overlays, "colorAccentPrimary") && PrefConfig.loadPrefBool(Iconify.getAppContext(), "fabricatedcolorAccentPrimary") && !Objects.equals(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentPrimary"), "null")) || (FabricatedOverlay.isOverlayDisabled(overlays, "colorAccentSecondary") && PrefConfig.loadPrefBool(Iconify.getAppContext(), "fabricatedcolorAccentSecondary") && !Objects.equals(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentSecondary"), "null")))
            ApplyOnBoot.applyColor();

        startForeground();

        return super.onStartCommand(intent, flags, startId);
    }

    private void startForeground() {
        showNotification();
    }

    public void showNotification() {
        Intent notificationIntent = new Intent();
        notificationIntent.setAction(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        notificationIntent.putExtra(Settings.EXTRA_CHANNEL_ID, NOTIFICATION_CHANNEL_ID);

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
                .setContentText("Useless notification. Tap to disable.")
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