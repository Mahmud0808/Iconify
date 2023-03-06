package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.LATEST_VERSION;
import static com.drdisagree.iconify.common.Preferences.FIRST_INSTALL;
import static com.drdisagree.iconify.common.Preferences.LAST_UPDATE_CHECK_TIME;
import static com.drdisagree.iconify.common.Preferences.MONET_ENGINE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.NEW_UPDATE;
import static com.drdisagree.iconify.common.Preferences.ON_HOME_PAGE;
import static com.drdisagree.iconify.common.Preferences.REBOOT_NEEDED;
import static com.drdisagree.iconify.common.Preferences.UPDATE_CHECK_TIME;
import static com.drdisagree.iconify.common.Preferences.UPDATE_DETECTED;
import static com.drdisagree.iconify.common.Preferences.VER_CODE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.adapters.MenuAdapter;
import com.drdisagree.iconify.ui.adapters.ViewAdapter;
import com.drdisagree.iconify.ui.models.MenuModel;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HomePage extends AppCompatActivity {

    public static boolean isServiceRunning = false;
    public static String latest_version = "{latestVersionName}";
    RecyclerView container;
    ConcatAdapter adapter;
    @SuppressLint("StaticFieldLeak")
    private HomePage.CheckForUpdate checkForUpdate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_home_page));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Prefs.putBoolean(ON_HOME_PAGE, true);
        Prefs.putBoolean(FIRST_INSTALL, false);
        Prefs.putBoolean(UPDATE_DETECTED, false);
        Prefs.putInt(VER_CODE, BuildConfig.VERSION_CODE);
        SystemUtil.getBootId();

        // Get list of enabled overlays
        Runnable runnable1 = () -> {
            List<String> AllOverlays = OverlayUtil.getOverlayList();
            List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
            for (String overlay : AllOverlays)
                Prefs.putBoolean(overlay, OverlayUtil.isOverlayEnabled(EnabledOverlays, overlay));

            List<String> FabricatedEnabledOverlays = FabricatedUtil.getEnabledOverlayList();
            for (String overlay : FabricatedEnabledOverlays)
                Prefs.putBoolean("fabricated" + overlay, true);

            Prefs.putBoolean(MONET_ENGINE_SWITCH, OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentME.overlay"));
        };
        Thread thread1 = new Thread(runnable1);
        thread1.start();

        /*
         * Background service which was used
         * previously to enable fabricated
         * overlays on boot
         *
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
                ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    Runnable runnable2 = () -> {
                        if (!isServiceRunning)
                            startService(new Intent(Iconify.getAppContext(), BackgroundService.class));
                    };
                    Thread thread2 = new Thread(runnable2);
                    thread2.start();
                });
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            Runnable runnable2 = () -> {
                if (!isServiceRunning)
                    startService(new Intent(Iconify.getAppContext(), BackgroundService.class));
            };
            Thread thread2 = new Thread(runnable2);
            thread2.start();
        }
         *
         */

        // RecyclerView
        container = findViewById(R.id.home_page_container);
        container.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConcatAdapter(initActivityItems());
        container.setAdapter(adapter);
        container.setHasFixedSize(false);

        // New update available dialog
        long lastChecked = Prefs.getLong(LAST_UPDATE_CHECK_TIME, -1);
        if (Prefs.getLong(UPDATE_CHECK_TIME, 0) != -1 && (lastChecked == -1 || (System.currentTimeMillis() - lastChecked >= Prefs.getLong(UPDATE_CHECK_TIME, 0)))) {
            Prefs.putLong(LAST_UPDATE_CHECK_TIME, System.currentTimeMillis());
            checkForUpdate = new HomePage.CheckForUpdate();
            checkForUpdate.execute();
        }

        // Reboot needed dialog
        if (!Prefs.getBoolean(FIRST_INSTALL) && Prefs.getBoolean(UPDATE_DETECTED)) {
            adapter.addAdapter(0, new ViewAdapter(this, R.layout.view_reboot, REBOOT_NEEDED));
            adapter.notifyItemChanged(0);
        }

        new Handler().postDelayed(() -> {
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
            }
        }, 1000);
    }

    private MenuAdapter initActivityItems() {
        ArrayList<MenuModel> home_page = new ArrayList<>();

        home_page.add(new MenuModel(ColorEngine.class, getResources().getString(R.string.activity_title_color_engine), getResources().getString(R.string.activity_desc_color_engine), R.drawable.ic_home_color));
        home_page.add(new MenuModel(IconPack.class, getResources().getString(R.string.activity_title_icon_pack), getResources().getString(R.string.activity_desc_icon_pack), R.drawable.ic_home_iconpack));
        home_page.add(new MenuModel(BrightnessBar.class, getResources().getString(R.string.activity_title_brightness_bar), getResources().getString(R.string.activity_desc_brightness_bar), R.drawable.ic_home_brightness));
        home_page.add(new MenuModel(QsPanelTile.class, getResources().getString(R.string.activity_title_qs_shape), getResources().getString(R.string.activity_desc_qs_shape), R.drawable.ic_home_qs_shape));
        home_page.add(new MenuModel(Notification.class, getResources().getString(R.string.activity_title_notification), getResources().getString(R.string.activity_desc_notification), R.drawable.ic_home_notification));
        home_page.add(new MenuModel(MediaPlayer.class, getResources().getString(R.string.activity_title_media_player), getResources().getString(R.string.activity_desc_media_player), R.drawable.ic_home_media));
        home_page.add(new MenuModel(VolumePanel.class, getResources().getString(R.string.activity_title_volume_panel), getResources().getString(R.string.activity_desc_volume_panel), R.drawable.ic_home_volume));
        home_page.add(new MenuModel(Extras.class, getResources().getString(R.string.activity_title_extras), getResources().getString(R.string.activity_desc_extras), R.drawable.ic_home_extras));
        home_page.add(new MenuModel(Settings.class, getResources().getString(R.string.activity_title_settings), getResources().getString(R.string.activity_desc_settings), R.drawable.ic_home_settings));
        home_page.add(new MenuModel(Info.class, getResources().getString(R.string.activity_title_info), getResources().getString(R.string.activity_desc_info), R.drawable.ic_home_info));

        return new MenuAdapter(this, home_page);
    }

    private void showUpdateNotification() {
        Intent notificationIntent = new Intent(this, AppUpdates.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getResources().getString(R.string.update_notification_channel_name)).setSmallIcon(R.drawable.ic_launcher_fg).setContentTitle(getResources().getString(R.string.new_update_title)).setContentText(getResources().getString(R.string.new_update_desc)).setContentIntent(pendingIntent).setOnlyAlertOnce(true).setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel(notificationManager);
        notificationManager.notify(0, notificationBuilder.build());
    }

    public void createChannel(NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(getResources().getString(R.string.update_notification_channel_name), getResources().getString(R.string.update_notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(getResources().getString(R.string.update_notification_channel_desc));
        notificationManager.createNotificationChannel(channel);
    }

    @SuppressLint("StaticFieldLeak")
    private class CheckForUpdate extends AsyncTask<Integer, Integer, String> {

        String jsonURL = LATEST_VERSION;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Integer... integers) {
            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            try {
                URL url = new URL(jsonURL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder stringBuffer = new StringBuilder();

                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line).append("\n");
                }
                if (stringBuffer.length() == 0) {
                    return null;
                } else {
                    return stringBuffer.toString();
                }
            } catch (Exception e) {
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String jsonStr) {
            if (jsonStr != null) {
                try {
                    JSONObject latestVersion = new JSONObject(jsonStr);

                    if (Integer.parseInt(latestVersion.getString(VER_CODE)) > BuildConfig.VERSION_CODE) {
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        createChannel(notificationManager);
                        NotificationManager manager = (NotificationManager) Iconify.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationChannel channel = manager.getNotificationChannel(getResources().getString(R.string.update_notification_channel_name));
                        if (ContextCompat.checkSelfPermission(HomePage.this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED && channel.getImportance() != NotificationManager.IMPORTANCE_NONE) {
                            showUpdateNotification();
                        } else {
                            latest_version = latestVersion.getString("versionName");
                            adapter.addAdapter(0, new ViewAdapter(HomePage.this, R.layout.view_new_update, NEW_UPDATE));
                            adapter.notifyItemChanged(0);
                            container.scrollToPosition(0);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (checkForUpdate != null && (checkForUpdate.getStatus() == AsyncTask.Status.PENDING || checkForUpdate.getStatus() == AsyncTask.Status.RUNNING))
            checkForUpdate.cancel(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkForUpdate.getStatus() != AsyncTask.Status.RUNNING) {
            checkForUpdate = new HomePage.CheckForUpdate();
            checkForUpdate.execute();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (checkForUpdate != null && (checkForUpdate.getStatus() == AsyncTask.Status.PENDING || checkForUpdate.getStatus() == AsyncTask.Status.RUNNING))
            checkForUpdate.cancel(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (checkForUpdate != null && (checkForUpdate.getStatus() == AsyncTask.Status.PENDING || checkForUpdate.getStatus() == AsyncTask.Status.RUNNING))
            checkForUpdate.cancel(true);
    }
}