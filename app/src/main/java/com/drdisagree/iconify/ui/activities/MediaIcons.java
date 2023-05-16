package com.drdisagree.iconify.ui.activities;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.overlaymanager.MediaPlayerIconManager;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.AppUtil;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.util.ArrayList;

public class MediaIcons extends BaseActivity {

    private final ArrayList<String[]> MPIP_KEY = new ArrayList<>();
    private final ArrayList<Object[]> mpip_list = new ArrayList<>();
    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_icons);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_media_icons);

        // Media Player Icon list items
        container = findViewById(R.id.mediaplayer_icon_list);

        mpip_list.add(new Object[]{"defaultA13", false, R.id.defaulta13mp});
        mpip_list.add(new Object[]{"com.maxmpz.audioplayer", false, R.id.poweramp});
        mpip_list.add(new Object[]{"code.name.monkey.retromusic", false, R.id.retro});
        mpip_list.add(new Object[]{"com.awedea.nyx", false, R.id.nyx});
        mpip_list.add(new Object[]{"com.kapp.youtube.final", false, R.id.ymusic});
        mpip_list.add(new Object[]{"com.shadow.blackhole", false, R.id.blackhole});
        mpip_list.add(new Object[]{"in.krosbits.musicolet", false, R.id.musicolet});
        mpip_list.add(new Object[]{"com.google.android.youtube", false, R.id.youtube});
        mpip_list.add(new Object[]{"com.google.android.apps.youtube.music", false, R.id.yt_music});
        mpip_list.add(new Object[]{"app.revanced.android.youtube", false, R.id.youtube_revanced});
        mpip_list.add(new Object[]{"app.revanced.android.apps.youtube.music", false, R.id.yt_music_revanced});

        // Generate keys for preference
        for (int i = 0; i < mpip_list.size(); i++) {
            MPIP_KEY.add(new String[]{"IconifyComponentMPIP" + i + 1 + ".overlay", "IconifyComponentMPIP" + i + 2 + ".overlay", "IconifyComponentMPIP" + i + 3 + ".overlay"});
        }

        Runnable runnable = () -> {
            // Check if packages are installed
            for (int i = 0; i < mpip_list.size(); i++) {
                if (i == 0) // default music player of a13
                    mpip_list.get(i)[1] = Build.VERSION.SDK_INT >= 33;
                else mpip_list.get(i)[1] = AppUtil.isAppInstalledRoot((String) mpip_list.get(i)[0]);
            }

            runOnUiThread(() -> {
                boolean isMusicPlayerShown = false;
                TextView noSupportedPlayer = findViewById(R.id.no_supported_musicplayer);

                for (int i = 0; i < mpip_list.size(); i++) {
                    if ((Boolean) mpip_list.get(i)[1]) {
                        if (i == 0) {
                            addItem(getResources().getString(R.string.a13_default_media_player), (String) mpip_list.get(i)[0], ContextCompat.getDrawable(MediaIcons.this, R.drawable.ic_android), (int) mpip_list.get(i)[2]);
                        } else {
                            addItem(AppUtil.getAppName((String) mpip_list.get(i)[0]), (String) mpip_list.get(i)[0], AppUtil.getAppIcon((String) mpip_list.get(i)[0]), (int) mpip_list.get(i)[2]);
                        }
                        enableOnClickListener(i);
                        isMusicPlayerShown = true;
                    }
                }

                refreshBackground();

                if (!isMusicPlayerShown) {
                    noSupportedPlayer.setVisibility(View.VISIBLE);
                }
            });
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    // Function to check for button bg drawable changes
    private void refreshBackground() {
        for (int i = 0; i < mpip_list.size(); i++) {
            if ((Boolean) mpip_list.get(i)[1]) {
                Button[] buttons = {findViewById((Integer) mpip_list.get(i)[2]).findViewById(R.id.aurora), findViewById((Integer) mpip_list.get(i)[2]).findViewById(R.id.gradicon), findViewById((Integer) mpip_list.get(i)[2]).findViewById(R.id.plumpy)};

                for (int j = 0; j < 3; j++) {
                    if (Prefs.getBoolean(MPIP_KEY.get(i)[j])) {
                        buttons[j].setBackground(ContextCompat.getDrawable(MediaIcons.this, R.drawable.button_red));
                    } else {
                        buttons[j].setBackground(ContextCompat.getDrawable(MediaIcons.this, R.drawable.button));
                    }
                }
            }
        }
    }

    // Enable onClick event
    private void enableOnClickListener(int idx) {
        LinearLayout child = findViewById((int) mpip_list.get(idx)[2]);

        Button[] buttons = {child.findViewById(R.id.aurora), child.findViewById(R.id.gradicon), child.findViewById(R.id.plumpy)};

        for (int i = 0; i < 3; i++) {
            int finalI = i + 1;
            buttons[i].setOnClickListener(v -> {
                if (Prefs.getBoolean(("IconifyComponentMPIP" + idx + finalI + ".overlay"))) {
                    OverlayUtil.disableOverlay("IconifyComponentMPIP" + idx + finalI + ".overlay");
                } else {
                    MediaPlayerIconManager.enableOverlay(idx, finalI);
                }
                refreshBackground();
            });
        }
    }

    private void addItem(String appName, String packageName, Drawable appIcon, int viewId) {
        View list = LayoutInflater.from(this).inflate(R.layout.view_list_option_mediaplayer_icons, container, false);
        list.setId(viewId);

        LinearLayout launch = list.findViewById(R.id.launch_app);
        if (packageName != null) {
            if (packageName.equals("defaultA13")) launch.setOnClickListener(v -> {
                // do nothing
            });
            else launch.setOnClickListener(v -> AppUtil.launchApp(MediaIcons.this, packageName));
        }

        list.findViewById(R.id.app_icon).setBackground(appIcon);

        TextView name = list.findViewById(R.id.app_name);
        name.setText(appName);

        container.addView(list);
    }
}