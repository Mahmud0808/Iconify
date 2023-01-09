package com.drdisagree.iconify.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.services.BackgroundService;
import com.drdisagree.iconify.ui.fragment.LoadingDialog;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.topjohnwu.superuser.Shell;

import java.util.List;

public class HomePage extends AppCompatActivity {

    public static boolean isServiceRunning = false;
    LinearLayout home_monetColor, home_iconPack, home_brightnessBar, home_qsShape, home_notification, home_mediaPlayer, home_volumePanel, home_progressBar, home_extras, home_settings, home_info;
    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        PrefConfig.savePrefBool(Iconify.getAppContext(), "onHomePage", true);

        container = (ViewGroup) findViewById(R.id.home_page_list);
        View list_view = LayoutInflater.from(this).inflate(R.layout.dialog_reboot, container, false);
        LinearLayout reboot_reminder = list_view.findViewById(R.id.reboot_reminder);
        container.addView(list_view);
        reboot_reminder.setVisibility(View.GONE);

        if (PrefConfig.loadPrefInt(Iconify.getAppContext(), "versionCode") < BuildConfig.VERSION_CODE && PrefConfig.loadPrefInt(Iconify.getAppContext(), "versionCode") != 0) {
            reboot_reminder.setVisibility(View.VISIBLE);
            Button reboot_now = findViewById(R.id.reboot_phone);
            reboot_now.setOnClickListener(v -> {
                LoadingDialog rebootingDialog = new LoadingDialog(HomePage.this);
                rebootingDialog.show(getResources().getString(R.string.rebooting_desc));

                runOnUiThread(() -> new Handler().postDelayed(() -> {
                    rebootingDialog.hide();

                    Shell.cmd("su -c 'svc power reboot'").exec();
                }, 5000));
            });
        }

        PrefConfig.savePrefInt(this, "versionCode", BuildConfig.VERSION_CODE);
        getBootId();

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_home_page));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Home page list items

        addItem(R.id.home_monetColor, getResources().getString(R.string.activity_title_color_engine), getResources().getString(R.string.activity_desc_color_engine), R.drawable.ic_home_color);
        addItem(R.id.home_iconPack, getResources().getString(R.string.activity_title_icon_pack), getResources().getString(R.string.activity_desc_icon_pack), R.drawable.ic_home_iconpack);
        addItem(R.id.home_brightnessBar, getResources().getString(R.string.activity_title_brightness_bar), getResources().getString(R.string.activity_desc_brightness_bar), R.drawable.ic_home_brightness);
        addItem(R.id.home_qsShape, getResources().getString(R.string.activity_title_qs_shape), getResources().getString(R.string.activity_desc_qs_shape), R.drawable.ic_home_shape);
        addItem(R.id.home_notification, getResources().getString(R.string.activity_title_notification), getResources().getString(R.string.activity_desc_notification), R.drawable.ic_home_notification);
        addItem(R.id.home_mediaPlayer, getResources().getString(R.string.activity_title_media_player), getResources().getString(R.string.activity_desc_media_player), R.drawable.ic_home_media);
        addItem(R.id.home_volumePanel, getResources().getString(R.string.activity_title_volume_panel), getResources().getString(R.string.activity_desc_volume_panel), R.drawable.ic_home_volume);
        // addItem(R.id.home_progressBar, "Progress Bar", "Change progress bar style", R.drawable.ic_progress_home);
        addItem(R.id.home_extras, getResources().getString(R.string.activity_title_extras), getResources().getString(R.string.activity_desc_extras), R.drawable.ic_home_extras);
        addItem(R.id.home_settings, getResources().getString(R.string.activity_title_settings), getResources().getString(R.string.activity_desc_settings), R.drawable.ic_home_settings);
        addItem(R.id.home_info, getResources().getString(R.string.activity_title_info), getResources().getString(R.string.activity_desc_info), R.drawable.ic_home_info);

        // Get list of enabled overlays
        Runnable runnable1 = () -> {
            List<String> AllOverlays = OverlayUtil.getOverlayList();
            List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
            for (String overlay : AllOverlays)
                PrefConfig.savePrefBool(Iconify.getAppContext(), overlay, OverlayUtil.isOverlayEnabled(EnabledOverlays, overlay));

            List<String> EnabledFabricatedOverlays = FabricatedOverlayUtil.getEnabledOverlayList();
            for (String overlay : EnabledFabricatedOverlays)
                PrefConfig.savePrefBool(Iconify.getAppContext(), "fabricated" + overlay, true);
        };
        Thread thread1 = new Thread(runnable1);
        thread1.start();

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
                ActivityResultLauncher<String> launcher = registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(), isGranted -> {
                            Runnable runnable2 = () -> {
                                if (!isServiceRunning)
                                    startService(new Intent(Iconify.getAppContext(), BackgroundService.class));
                            };
                            Thread thread2 = new Thread(runnable2);
                            thread2.start();
                        }
                );
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

        // Color engine item onClick
        home_monetColor = findViewById(R.id.home_monetColor);
        home_monetColor.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, ColorEngine.class);
            startActivity(intent);
        });

        // Icon pack item onClick
        home_iconPack = findViewById(R.id.home_iconPack);
        home_iconPack.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, IconPacks.class);
            startActivity(intent);
        });

        // Brightness bar item onClick
        home_brightnessBar = findViewById(R.id.home_brightnessBar);
        home_brightnessBar.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, BrightnessBars.class);
            startActivity(intent);
        });

        // QS Shape item onClick
        home_qsShape = findViewById(R.id.home_qsShape);
        home_qsShape.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, QsShapes.class);
            startActivity(intent);
        });

        // Notification item onClick
        home_notification = findViewById(R.id.home_notification);
        home_notification.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, Notifications.class);
            startActivity(intent);
        });

        // Media player item onClick
        home_mediaPlayer = findViewById(R.id.home_mediaPlayer);
        home_mediaPlayer.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, MediaPlayer.class);
            startActivity(intent);
        });

        // Volume panel item onClick
        home_volumePanel = findViewById(R.id.home_volumePanel);
        home_volumePanel.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, VolumePanel.class);
            startActivity(intent);
        });

        /* Progress bar item onClick
        home_progressBar = findViewById(R.id.home_progressBar);
        home_progressBar.setOnClickListener(v -> {
                Intent intent = new Intent(HomePage.this, ProgressBar.class);
                startActivity(intent);
            }
        }); */

        // Extras item onClick
        home_extras = findViewById(R.id.home_extras);
        home_extras.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, Extras.class);
            startActivity(intent);
        });

        // Settings item onClick
        home_settings = findViewById(R.id.home_settings);
        home_settings.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, Settings.class);
            startActivity(intent);
        });

        // About item onClick
        home_info = findViewById(R.id.home_info);
        home_info.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, Info.class);
            startActivity(intent);
        });
    }

    // Function to add new item in list
    private void addItem(int id, String title, String desc, int preview) {
        View list_view = LayoutInflater.from(this).inflate(R.layout.list_view, container, false);

        TextView list_title = (TextView) list_view.findViewById(R.id.list_title);
        TextView list_desc = (TextView) list_view.findViewById(R.id.list_desc);
        ImageView list_preview = (ImageView) list_view.findViewById(R.id.list_preview);

        list_view.setId(id);
        list_title.setText(title);
        list_desc.setText(desc);
        list_preview.setImageResource(preview);

        container.addView(list_view);
    }

    // Save unique id of each boot
    public static void getBootId() {
        PrefConfig.savePrefSettings(Iconify.getAppContext(), "boot_id", Shell.cmd("cat /proc/sys/kernel/random/boot_id").exec().getOut().toString());
    }
}