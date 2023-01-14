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
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.services.BackgroundService;
import com.drdisagree.iconify.ui.fragment.LoadingDialog;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends AppCompatActivity {

    public static boolean isServiceRunning = false;
    private ViewGroup container;

    // Save unique id of each boot
    public static void getBootId() {
        Prefs.putString("boot_id", Shell.cmd("cat /proc/sys/kernel/random/boot_id").exec().getOut().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Prefs.putBoolean("onHomePage", true);

        container = (ViewGroup) findViewById(R.id.home_page_list);
        View list_view = LayoutInflater.from(this).inflate(R.layout.dialog_reboot, container, false);
        LinearLayout reboot_reminder = list_view.findViewById(R.id.reboot_reminder);
        container.addView(list_view);
        reboot_reminder.setVisibility(View.GONE);

        if (!Prefs.getBoolean("firstInstall") && Prefs.getBoolean("updateDetected")) {
            Prefs.putBoolean("firstInstall", false);
            Prefs.putBoolean("updateDetected", false);

            reboot_reminder.setVisibility(View.VISIBLE);
            Button reboot_now = findViewById(R.id.reboot_phone);
            reboot_now.setOnClickListener(v -> {
                LoadingDialog rebootingDialog = new LoadingDialog(HomePage.this);
                rebootingDialog.show(getResources().getString(R.string.rebooting_desc));

                runOnUiThread(() -> new Handler().postDelayed(() -> {
                    rebootingDialog.hide();

                    SystemUtil.restartDevice();
                }, 5000));
            });
        }

        Prefs.putInt("versionCode", BuildConfig.VERSION_CODE);
        getBootId();

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_home_page));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Home page list items
        ArrayList<Object[]> home_page = new ArrayList<>();

        home_page.add(new Object[]{ColorEngine.class, getResources().getString(R.string.activity_title_color_engine), getResources().getString(R.string.activity_desc_color_engine), R.drawable.ic_home_color});
        home_page.add(new Object[]{IconPacks.class, getResources().getString(R.string.activity_title_icon_pack), getResources().getString(R.string.activity_desc_icon_pack), R.drawable.ic_home_iconpack});
        home_page.add(new Object[]{BrightnessBars.class, getResources().getString(R.string.activity_title_brightness_bar), getResources().getString(R.string.activity_desc_brightness_bar), R.drawable.ic_home_brightness});
        home_page.add(new Object[]{QsShapes.class, getResources().getString(R.string.activity_title_qs_shape), getResources().getString(R.string.activity_desc_qs_shape), R.drawable.ic_home_qs_shape});
        home_page.add(new Object[]{Notifications.class, getResources().getString(R.string.activity_title_notification), getResources().getString(R.string.activity_desc_notification), R.drawable.ic_home_notification});
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

            List<String> FabricatedEnabledOverlays = FabricatedOverlayUtil.getEnabledOverlayList();
            for (String overlay : FabricatedEnabledOverlays)
                Prefs.putBoolean("fabricated" + overlay, true);
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

        // Enable onClick event
        // reboot dialog is in the first index
        for (int i = 1; i < home_page.size() + 1; i++) {
            LinearLayout child = container.getChildAt(i).findViewById(R.id.list_item);
            int finalI = i - 1;
            child.setOnClickListener(v -> {
                Intent intent = new Intent(HomePage.this, (Class<?>) home_page.get(finalI)[0]);
                startActivity(intent);
            });
        }
    }

    // Function to add new item in list
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.list_view, container, false);

            TextView title = (TextView) list.findViewById(R.id.list_title);
            title.setText((String) pack.get(i)[1]);

            TextView desc = (TextView) list.findViewById(R.id.list_desc);
            desc.setText((String) pack.get(i)[2]);

            ImageView preview = (ImageView) list.findViewById(R.id.list_preview);
            preview.setImageResource((int) pack.get(i)[3]);

            container.addView(list);
        }
    }
}