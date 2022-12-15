package com.drdisagree.iconify.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.drdisagree.iconify.utils.FabricatedOverlay;
import com.drdisagree.iconify.utils.OverlayUtils;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.topjohnwu.superuser.Shell;

import java.util.List;

public class HomePage extends AppCompatActivity {

    public static boolean isServiceRunning = false;
    private final String TAG = "WelcomePage";
    LinearLayout home_monetColor, home_iconPack, home_brightnessBar, home_qsShape, home_notification, home_mediaPlayer, home_volumePanel, home_progressBar, home_extras, home_info;
    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        PrefConfig.savePrefBool(Iconify.getAppContext(), "onHomePage", true);

        if (PrefConfig.loadPrefInt(Iconify.getAppContext(), "versionCode") < BuildConfig.VERSION_CODE && PrefConfig.loadPrefInt(Iconify.getAppContext(), "versionCode") != 0)
            Toast.makeText(Iconify.getAppContext(), "Reboot to Apply Changes", Toast.LENGTH_LONG).show();
        PrefConfig.savePrefInt(this, "versionCode", BuildConfig.VERSION_CODE);
        getBootId();

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("Iconify");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Home page list items
        container = (ViewGroup) findViewById(R.id.home_page_list);
        addItem(R.id.home_monetColor, "Color Engine", "Have control over colors", R.drawable.ic_color_home);
        addItem(R.id.home_iconPack, "Icon Pack", "Change system icon pack", R.drawable.ic_wifi_home);
        addItem(R.id.home_brightnessBar, "Brightness Bar", "Customize brightness slider", R.drawable.ic_brightness_home);
        addItem(R.id.home_qsShape, "QS Panel Tiles", "Customize qs panel tiles", R.drawable.ic_shape_home);
        addItem(R.id.home_notification, "Notification", "Customize notification style", R.drawable.ic_notification_home);
        addItem(R.id.home_mediaPlayer, "Media Player", "Change how media player looks", R.drawable.ic_media_home);
        addItem(R.id.home_volumePanel, "Volume Panel", "Customize volume panel", R.drawable.ic_volume_home);
        // addItem(R.id.home_progressBar, "Progress Bar", "Change progress bar style", R.drawable.ic_progress_home);
        addItem(R.id.home_extras, "Extras", "Additions tweaks and settings", R.drawable.ic_extras_home);
        addItem(R.id.home_info, "About", "Information about this app", R.drawable.ic_info_home);

        // Get list of enabled overlays
        Runnable runnable1 = () -> {
            List<String> EnabledOverlays = OverlayUtils.getEnabledOverlayList();
            for (String overlay : EnabledOverlays)
                PrefConfig.savePrefBool(Iconify.getAppContext(), overlay, true);

            List<String> DisabledOverlays = OverlayUtils.getDisabledOverlayList();
            for (String overlay : DisabledOverlays)
                PrefConfig.savePrefBool(Iconify.getAppContext(), overlay, false);

            List<String> EnabledFabricatedOverlays = FabricatedOverlay.getEnabledOverlayList();
            for (String overlay : EnabledFabricatedOverlays)
                PrefConfig.savePrefBool(Iconify.getAppContext(), "fabricated" + overlay, true);

            List<String> DisabledFabricatedOverlays = FabricatedOverlay.getDisabledOverlayList();
            for (String overlay : DisabledFabricatedOverlays)
                PrefConfig.savePrefBool(Iconify.getAppContext(), "fabricated" + overlay, false);
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
            Intent intent = new Intent(HomePage.this, MonetColor.class);
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