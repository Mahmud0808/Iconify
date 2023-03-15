package com.drdisagree.iconify.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.overlaymanager.MediaPlayerIconManager;
import com.drdisagree.iconify.utils.AppUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class MediaPlayer extends AppCompatActivity {

    private final ArrayList<String[]> MPIP_KEY = new ArrayList<>();
    private final ArrayList<Object[]> mpip_list = new ArrayList<>();
    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_media_player));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        refreshPreview();

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mp_accent = findViewById(R.id.mp_accent);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mp_system = findViewById(R.id.mp_system);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mp_pitch_black = findViewById(R.id.mp_pitch_black);

        mp_accent.setChecked(Prefs.getBoolean("IconifyComponentMPA.overlay"));

        mp_accent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mp_system.setChecked(false);
                mp_pitch_black.setChecked(false);
                OverlayUtil.disableOverlay("IconifyComponentMPS.overlay");
                OverlayUtil.disableOverlay("IconifyComponentMPB.overlay");
                OverlayUtil.enableOverlay("IconifyComponentMPA.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentMPA.overlay");
            }
            refreshPreview();
        });

        mp_system.setChecked(Prefs.getBoolean("IconifyComponentMPS.overlay"));

        mp_system.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mp_accent.setChecked(false);
                mp_pitch_black.setChecked(false);
                OverlayUtil.disableOverlay("IconifyComponentMPA.overlay");
                OverlayUtil.disableOverlay("IconifyComponentMPB.overlay");
                OverlayUtil.enableOverlay("IconifyComponentMPS.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentMPS.overlay");
            }
            refreshPreview();
        });

        mp_pitch_black.setChecked(Prefs.getBoolean("IconifyComponentMPB.overlay"));

        mp_pitch_black.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mp_accent.setChecked(false);
                mp_system.setChecked(false);
                OverlayUtil.disableOverlay("IconifyComponentMPA.overlay");
                OverlayUtil.disableOverlay("IconifyComponentMPS.overlay");
                OverlayUtil.enableOverlay("IconifyComponentMPB.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentMPB.overlay");
            }
            refreshPreview();
        });

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
            MPIP_KEY.add(new String[]{
                    "IconifyComponentMPIP" + i + 1 + ".overlay",
                    "IconifyComponentMPIP" + i + 2 + ".overlay",
                    "IconifyComponentMPIP" + i + 3 + ".overlay"
            });
        }

        musicPlayerIconList();
    }

    private void refreshPreview() {
        findViewById(R.id.preview_mp_accent).setVisibility(View.GONE);
        findViewById(R.id.preview_mp_black).setVisibility(View.GONE);
        findViewById(R.id.preview_mp_system).setVisibility(View.GONE);

        if (Prefs.getBoolean("IconifyComponentMPA.overlay"))
            findViewById(R.id.preview_mp_accent).setVisibility(View.VISIBLE);
        else if (Prefs.getBoolean("IconifyComponentMPB.overlay"))
            findViewById(R.id.preview_mp_black).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.preview_mp_system).setVisibility(View.VISIBLE);
    }

    private void musicPlayerIconList() {
        LoadMusicPlayerList musicPlayerList = new LoadMusicPlayerList();
        musicPlayerList.execute();
    }

    // Function to check for button bg drawable changes
    private void refreshBackground() {
        for (int i = 0; i < mpip_list.size(); i++) {
            if ((Boolean) mpip_list.get(i)[1]) {
                Button[] buttons = {findViewById((Integer) mpip_list.get(i)[2]).findViewById(R.id.aurora),
                        findViewById((Integer) mpip_list.get(i)[2]).findViewById(R.id.gradicon),
                        findViewById((Integer) mpip_list.get(i)[2]).findViewById(R.id.plumpy)};

                for (int j = 0; j < 3; j++) {
                    if (Prefs.getBoolean(MPIP_KEY.get(i)[j])) {
                        buttons[j].setBackground(ContextCompat.getDrawable(MediaPlayer.this, R.drawable.button_red));
                    } else {
                        buttons[j].setBackground(ContextCompat.getDrawable(MediaPlayer.this, R.drawable.button));
                    }
                }
            }
        }
    }

    // Enable onClick event
    private void enableOnClickListener(int idx) {
        LinearLayout child = findViewById((int) mpip_list.get(idx)[2]);

        Button[] buttons = {child.findViewById(R.id.aurora),
                child.findViewById(R.id.gradicon),
                child.findViewById(R.id.plumpy)};

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
            if (packageName.equals("defaultA13"))
                launch.setOnClickListener(v -> {
                    // do nothing
                });
            else
                launch.setOnClickListener(v -> AppUtil.launchApp(MediaPlayer.this, packageName));
        }

        list.findViewById(R.id.app_icon).setBackground(appIcon);

        TextView name = list.findViewById(R.id.app_name);
        name.setText(appName);

        container.addView(list);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadMusicPlayerList extends AsyncTask<Integer, Integer, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Integer... integers) {
            // Check if packages are installed
            for (int i = 0; i < mpip_list.size(); i++) {
                if (i == 0) // default music player of a13
                    mpip_list.get(i)[1] = Build.VERSION.SDK_INT >= 33;
                else
                    mpip_list.get(i)[1] = AppUtil.isAppInstalled((String) mpip_list.get(i)[0]);
            }
            return "Finished!";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // ...
        }

        @Override
        protected void onPostExecute(String string) {
            boolean titleShown = false;
            boolean isSupportedPlayerShown = false;
            TextView title = findViewById(R.id.mediaplayer_icon_title);

            for (int i = 0; i < mpip_list.size(); i++) {
                if (!titleShown && isSupportedPlayerShown) {
                    title.setVisibility(View.VISIBLE);
                    titleShown = true;
                }

                if ((Boolean) mpip_list.get(i)[1]) {
                    if (i == 0) {
                        addItem(getResources().getString(R.string.a13_default_media_player), (String) mpip_list.get(i)[0], ContextCompat.getDrawable(MediaPlayer.this, R.drawable.ic_android), (int) mpip_list.get(i)[2]);
                    } else {
                        addItem(AppUtil.getAppName((String) mpip_list.get(i)[0]), (String) mpip_list.get(i)[0], AppUtil.getAppIcon((String) mpip_list.get(i)[0]), (int) mpip_list.get(i)[2]);
                    }
                    enableOnClickListener(i);
                    isSupportedPlayerShown = true;
                }
            }

            refreshBackground();
        }
    }
}