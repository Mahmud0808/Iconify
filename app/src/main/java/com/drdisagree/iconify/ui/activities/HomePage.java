package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.LATEST_VERSION;
import static com.drdisagree.iconify.common.Preferences.FIRST_INSTALL;
import static com.drdisagree.iconify.common.Preferences.LAST_UPDATE_CHECK_TIME;
import static com.drdisagree.iconify.common.Preferences.MONET_ENGINE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.ON_HOME_PAGE;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.services.BackgroundService;
import com.drdisagree.iconify.ui.views.LoadingDialog;
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
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout check_update;
    TextView update_desc;
    private ViewGroup container;
    @SuppressLint("StaticFieldLeak")
    private HomePage.CheckForUpdate checkForUpdate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Prefs.putBoolean(ON_HOME_PAGE, true);

        container = findViewById(R.id.home_page_list);

        // New update available dialog
        View list_view1 = LayoutInflater.from(this).inflate(R.layout.view_new_update, container, false);
        check_update = list_view1.findViewById(R.id.check_update);
        container.addView(list_view1);
        check_update.setVisibility(View.GONE);
        update_desc = findViewById(R.id.update_desc);

        long lastChecked = Prefs.getLong(LAST_UPDATE_CHECK_TIME, -1);

        if (Prefs.getLong(UPDATE_CHECK_TIME, 0) != -1 && (lastChecked == -1 || (System.currentTimeMillis() - lastChecked >= Prefs.getLong("UPDATE_CHECK_TIME", 0)))) {
            Prefs.putLong(LAST_UPDATE_CHECK_TIME, System.currentTimeMillis());
            checkForUpdate = new HomePage.CheckForUpdate();
            checkForUpdate.execute();
        }

        // Reboot needed dialog
        View list_view2 = LayoutInflater.from(this).inflate(R.layout.view_reboot, container, false);
        LinearLayout reboot_reminder = list_view2.findViewById(R.id.reboot_reminder);
        container.addView(list_view2);
        reboot_reminder.setVisibility(View.GONE);

        if (!Prefs.getBoolean(FIRST_INSTALL) && Prefs.getBoolean(UPDATE_DETECTED)) {
            reboot_reminder.setVisibility(View.VISIBLE);
            Button reboot_now = findViewById(R.id.btn_reboot);
            reboot_now.setOnClickListener(v -> {
                LoadingDialog rebootingDialog = new LoadingDialog(HomePage.this);
                rebootingDialog.show(getResources().getString(R.string.rebooting_desc));

                runOnUiThread(() -> new Handler().postDelayed(() -> {
                    rebootingDialog.hide();

                    SystemUtil.restartDevice();
                }, 5000));
            });
        }

        Prefs.putBoolean(FIRST_INSTALL, false);
        Prefs.putBoolean(UPDATE_DETECTED, false);
        Prefs.putInt(VER_CODE, BuildConfig.VERSION_CODE);
        SystemUtil.getBootId();

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_home_page));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Home page list items
        ArrayList<Object[]> home_page = new ArrayList<>();

        home_page.add(new Object[]{ColorEngine.class, getResources().getString(R.string.activity_title_color_engine), getResources().getString(R.string.activity_desc_color_engine), R.drawable.ic_home_color});
        home_page.add(new Object[]{IconPack.class, getResources().getString(R.string.activity_title_icon_pack), getResources().getString(R.string.activity_desc_icon_pack), R.drawable.ic_home_iconpack});
        home_page.add(new Object[]{BrightnessBar.class, getResources().getString(R.string.activity_title_brightness_bar), getResources().getString(R.string.activity_desc_brightness_bar), R.drawable.ic_home_brightness});
        home_page.add(new Object[]{QsPanelTile.class, getResources().getString(R.string.activity_title_qs_shape), getResources().getString(R.string.activity_desc_qs_shape), R.drawable.ic_home_qs_shape});
        home_page.add(new Object[]{Notification.class, getResources().getString(R.string.activity_title_notification), getResources().getString(R.string.activity_desc_notification), R.drawable.ic_home_notification});
        home_page.add(new Object[]{MediaPlayer.class, getResources().getString(R.string.activity_title_media_player), getResources().getString(R.string.activity_desc_media_player), R.drawable.ic_home_media});
        home_page.add(new Object[]{VolumePanel.class, getResources().getString(R.string.activity_title_volume_panel), getResources().getString(R.string.activity_desc_volume_panel), R.drawable.ic_home_volume});
        home_page.add(new Object[]{Extras.class, getResources().getString(R.string.activity_title_extras), getResources().getString(R.string.activity_desc_extras), R.drawable.ic_home_extras});
        home_page.add(new Object[]{Settings.class, getResources().getString(R.string.activity_title_settings), getResources().getString(R.string.activity_desc_settings), R.drawable.ic_home_settings});
        home_page.add(new Object[]{Info.class, getResources().getString(R.string.activity_title_info), getResources().getString(R.string.activity_desc_info), R.drawable.ic_home_info});

        addItem(home_page);

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

        // Enable onClick event
        // new update dilaog is in the first index
        // reboot dialog is in the second index
        int extra_items = container.getChildCount() - home_page.size();
        for (int i = extra_items; i < home_page.size() + extra_items; i++) {
            LinearLayout child = container.getChildAt(i).findViewById(R.id.list_item);
            int finalI = i - extra_items;
            child.setOnClickListener(v -> {
                if (checkForUpdate != null && (checkForUpdate.getStatus() == AsyncTask.Status.PENDING || checkForUpdate.getStatus() == AsyncTask.Status.RUNNING))
                    checkForUpdate.cancel(true);
                Intent intent = new Intent(HomePage.this, (Class<?>) home_page.get(finalI)[0]);
                startActivity(intent);
            });
        }

        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
        }
    }

    // Function to add new item in list
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_list_menu, container, false);

            TextView title = list.findViewById(R.id.list_title);
            title.setText((String) pack.get(i)[1]);

            TextView desc = list.findViewById(R.id.list_desc);
            desc.setText((String) pack.get(i)[2]);

            ImageView preview = list.findViewById(R.id.list_preview);
            preview.setImageResource((int) pack.get(i)[3]);

            container.addView(list);
        }
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
            super.onPreExecute();
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

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String jsonStr) {
            super.onPostExecute(jsonStr);

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
                            check_update.setOnClickListener(v -> {
                                Intent intent = new Intent(HomePage.this, AppUpdates.class);
                                startActivity(intent);
                            });
                            update_desc.setText(getResources().getString(R.string.update_dialog_desc).replace("{latestVersionName}", latestVersion.getString("versionName")));
                            check_update.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }
}