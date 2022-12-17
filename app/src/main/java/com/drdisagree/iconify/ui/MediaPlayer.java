package com.drdisagree.iconify.ui;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.installer.MediaPlayerIconInstaller;
import com.drdisagree.iconify.utils.AppUtils;
import com.drdisagree.iconify.utils.OverlayUtils;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Objects;

public class MediaPlayer extends AppCompatActivity {

    private ViewGroup container;
    private static List<String> overlays = OverlayUtils.getEnabledOverlayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_player);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("Media Player");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        refreshPreview();

        // Media Player Icon list items
        container = (ViewGroup) findViewById(R.id.mediaplayer_icon_list);

        ViewGroup.MarginLayoutParams marginParams;
        LinearLayout.LayoutParams layoutParams;

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mp_accent = findViewById(R.id.mp_accent);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mp_system = findViewById(R.id.mp_system);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mp_pitch_black = findViewById(R.id.mp_pitch_black);

        mp_accent.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentMPA.overlay"));

        mp_accent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mp_system.setChecked(false);
                mp_pitch_black.setChecked(false);
                OverlayUtils.disableOverlay("IconifyComponentMPS.overlay");
                OverlayUtils.disableOverlay("IconifyComponentMPB.overlay");
                OverlayUtils.enableOverlay("IconifyComponentMPA.overlay");
            } else {
                OverlayUtils.disableOverlay("IconifyComponentMPA.overlay");
            }
            refreshPreview();
        });

        mp_system.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentMPS.overlay"));

        mp_system.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mp_accent.setChecked(false);
                mp_pitch_black.setChecked(false);
                OverlayUtils.disableOverlay("IconifyComponentMPA.overlay");
                OverlayUtils.disableOverlay("IconifyComponentMPB.overlay");
                OverlayUtils.enableOverlay("IconifyComponentMPS.overlay");
            } else {
                OverlayUtils.disableOverlay("IconifyComponentMPS.overlay");
            }
            refreshPreview();
        });

        mp_pitch_black.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentMPB.overlay"));

        mp_pitch_black.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mp_accent.setChecked(false);
                mp_system.setChecked(false);
                OverlayUtils.disableOverlay("IconifyComponentMPA.overlay");
                OverlayUtils.disableOverlay("IconifyComponentMPS.overlay");
                OverlayUtils.enableOverlay("IconifyComponentMPB.overlay");
            } else {
                OverlayUtils.disableOverlay("IconifyComponentMPB.overlay");
            }
            refreshPreview();
        });

        musicPlayerIconList();
    }

    private void refreshPreview() {
        ImageView preview_accent = findViewById(R.id.media_player_preview_accent);
        ImageView preview_system = findViewById(R.id.media_player_preview_system);
        ImageView preview_black = findViewById(R.id.media_player_preview_black);

        if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentMPA.overlay")) {
            preview_accent.setVisibility(View.VISIBLE);
            preview_system.setVisibility(View.GONE);
            preview_black.setVisibility(View.GONE);
        } else if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentMPB.overlay")) {
            preview_black.setVisibility(View.VISIBLE);
            preview_accent.setVisibility(View.GONE);
            preview_system.setVisibility(View.GONE);
        } else {
            preview_system.setVisibility(View.VISIBLE);
            preview_accent.setVisibility(View.GONE);
            preview_black.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void musicPlayerIconList() {
        final String poweramp = "com.maxmpz.audioplayer";
        final String retro = "code.name.monkey.retromusic";
        final String nyx = "com.awedea.nyx";
        final String ymusic = "com.kapp.youtube.final";
        final String blackhole = "com.shadow.blackhole";
        final String musicolet = "in.krosbits.musicolet";
        final String youtube = "com.google.android.youtube";
        final String yt_music = "com.google.android.apps.youtube.music";
        final String youtube_revanced = "app.revanced.android.youtube";
        final String yt_music_revanced = "app.revanced.android.apps.youtube.music";

        TextView title = findViewById(R.id.mediaplayer_icon_title);
        if (AppUtils.isAppInstalled(poweramp) || AppUtils.isAppInstalled(retro) || AppUtils.isAppInstalled(nyx) || AppUtils.isAppInstalled(ymusic) || AppUtils.isAppInstalled(blackhole) || AppUtils.isAppInstalled(musicolet) || AppUtils.isAppInstalled(youtube) || AppUtils.isAppInstalled(yt_music) || AppUtils.isAppInstalled(youtube_revanced) || AppUtils.isAppInstalled(yt_music_revanced))
            title.setVisibility(View.VISIBLE);

        if (AppUtils.isAppInstalled(poweramp)) {
            addItem(R.id.poweramp, AppUtils.getAppIcon(poweramp), AppUtils.getAppName(poweramp), poweramp, R.id.poweramp_aurora, R.id.poweramp_gradicon, R.id.poweramp_plumpy);
            enableOnClickListener(R.id.poweramp_aurora, R.id.poweramp_gradicon, R.id.poweramp_plumpy, 1);
        }

        if (AppUtils.isAppInstalled(retro)) {
            addItem(R.id.retro, AppUtils.getAppIcon(retro), AppUtils.getAppName(retro), retro, R.id.retro_aurora, R.id.retro_gradicon, R.id.retro_plumpy);
            enableOnClickListener(R.id.retro_aurora, R.id.retro_gradicon, R.id.retro_plumpy, 2);
        }

        if (AppUtils.isAppInstalled(nyx)) {
            addItem(R.id.nyx, AppUtils.getAppIcon(nyx), AppUtils.getAppName(nyx), nyx, R.id.nyx_aurora, R.id.nyx_gradicon, R.id.nyx_plumpy);
            enableOnClickListener(R.id.nyx_aurora, R.id.nyx_gradicon, R.id.nyx_plumpy, 3);
        }

        if (AppUtils.isAppInstalled(ymusic)) {
            addItem(R.id.ymusic, AppUtils.getAppIcon(ymusic), AppUtils.getAppName(ymusic), ymusic, R.id.ymusic_aurora, R.id.ymusic_gradicon, R.id.ymusic_plumpy);
            enableOnClickListener(R.id.ymusic_aurora, R.id.ymusic_gradicon, R.id.ymusic_plumpy, 4);
        }

        if (AppUtils.isAppInstalled(blackhole)) {
            addItem(R.id.blackhole, AppUtils.getAppIcon(blackhole), AppUtils.getAppName(blackhole), blackhole, R.id.blackhole_aurora, R.id.blackhole_gradicon, R.id.blackhole_plumpy);
            enableOnClickListener(R.id.blackhole_aurora, R.id.blackhole_gradicon, R.id.blackhole_plumpy, 5);
        }

        if (AppUtils.isAppInstalled(musicolet)) {
            addItem(R.id.musicolet, AppUtils.getAppIcon(musicolet), AppUtils.getAppName(musicolet), musicolet, R.id.musicolet_aurora, R.id.musicolet_gradicon, R.id.musicolet_plumpy);
            enableOnClickListener(R.id.musicolet_aurora, R.id.musicolet_gradicon, R.id.musicolet_plumpy, 6);
        }

        if (AppUtils.isAppInstalled(youtube)) {
            addItem(R.id.youtube, AppUtils.getAppIcon(youtube), AppUtils.getAppName(youtube), youtube, R.id.youtube_aurora, R.id.youtube_gradicon, R.id.youtube_plumpy);
            enableOnClickListener(R.id.youtube_aurora, R.id.youtube_gradicon, R.id.youtube_plumpy, 7);
        }

        if (AppUtils.isAppInstalled(yt_music)) {
            addItem(R.id.yt_music, AppUtils.getAppIcon(yt_music), AppUtils.getAppName(yt_music), yt_music, R.id.yt_music_aurora, R.id.yt_music_gradicon, R.id.yt_music_plumpy);
            enableOnClickListener(R.id.yt_music_aurora, R.id.yt_music_gradicon, R.id.yt_music_plumpy, 8);
        }

        if (AppUtils.isAppInstalled(youtube_revanced)) {
            addItem(R.id.youtube_revanced, AppUtils.getAppIcon(youtube_revanced), AppUtils.getAppName(youtube_revanced), youtube_revanced, R.id.youtube_revanced_aurora, R.id.youtube_revanced_gradicon, R.id.youtube_revanced_plumpy);
            enableOnClickListener(R.id.youtube_revanced_aurora, R.id.youtube_revanced_gradicon, R.id.youtube_revanced_plumpy, 9);
        }

        if (AppUtils.isAppInstalled(yt_music_revanced)) {
            addItem(R.id.yt_music_revanced, AppUtils.getAppIcon(yt_music_revanced), AppUtils.getAppName(yt_music_revanced), yt_music_revanced, R.id.yt_music_revanced_aurora, R.id.yt_music_revanced_gradicon, R.id.yt_music_revanced_plumpy);
            enableOnClickListener(R.id.yt_music_revanced_aurora, R.id.yt_music_revanced_gradicon, R.id.yt_music_revanced_plumpy, 10);
        }
    }

    private void enableOnClickListener(int aurora_id, int gradicon_id, int plumpy_id, int idx) {
        Button aurora = findViewById(aurora_id);
        Button gradicon = findViewById(gradicon_id);
        Button plumpy = findViewById(plumpy_id);

        checkIfApplied(aurora, gradicon, plumpy, idx);

        aurora.setOnClickListener(v -> {
            if (PrefConfig.loadPrefBool(Iconify.getAppContext(), ("IconifyComponentMPIP" + idx + "1.overlay"))) {
                Shell.cmd("cmd overlay disable --user current IconifyComponentMPIP" + idx + "1.overlay").exec();
                PrefConfig.savePrefBool(Iconify.getAppContext(), ("IconifyComponentMPIP" + idx + "1.overlay"), false);
            } else {
                MediaPlayerIconInstaller.install_pack(idx, 1);
                PrefConfig.savePrefBool(Iconify.getAppContext(), ("IconifyComponentMPIP" + idx + "1.overlay"), true);
            }
            checkIfApplied(aurora, gradicon, plumpy, idx);
        });

        gradicon.setOnClickListener(v -> {
            if (PrefConfig.loadPrefBool(Iconify.getAppContext(), ("IconifyComponentMPIP" + idx + "2.overlay"))) {
                Shell.cmd("cmd overlay disable --user current IconifyComponentMPIP" + idx + "2.overlay").exec();
                PrefConfig.savePrefBool(Iconify.getAppContext(), ("IconifyComponentMPIP" + idx + "2.overlay"), false);
            } else {
                MediaPlayerIconInstaller.install_pack(idx, 2);
                PrefConfig.savePrefBool(Iconify.getAppContext(), ("IconifyComponentMPIP" + idx + "2.overlay"), true);
            }
            checkIfApplied(aurora, gradicon, plumpy, idx);
        });

        plumpy.setOnClickListener(v -> {
            if (PrefConfig.loadPrefBool(Iconify.getAppContext(), ("IconifyComponentMPIP" + idx + "3.overlay"))) {
                Shell.cmd("cmd overlay disable --user current IconifyComponentMPIP" + idx + "3.overlay").exec();
                PrefConfig.savePrefBool(Iconify.getAppContext(), ("IconifyComponentMPIP" + idx + "3.overlay"), false);
            } else {
                MediaPlayerIconInstaller.install_pack(idx, 3);
                PrefConfig.savePrefBool(Iconify.getAppContext(), ("IconifyComponentMPIP" + idx + "3.overlay"), true);
            }
            checkIfApplied(aurora, gradicon, plumpy, idx);
        });
    }

    private void checkIfApplied(Button aurora, Button gradicon, Button plumpy, int idx) {
        refreshButton(aurora, gradicon, plumpy, idx);
    }

    private void refreshButton(Button btn1, Button btn2, Button btn3, int m) {
        if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentMPIP" + m + "1.overlay")) {
            btn1.setBackgroundResource(R.drawable.button_red);
            btn2.setBackgroundResource(R.drawable.button);
            btn3.setBackgroundResource(R.drawable.button);
        } else if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentMPIP" + m + "2.overlay")) {
            btn2.setBackgroundResource(R.drawable.button_red);
            btn1.setBackgroundResource(R.drawable.button);
            btn3.setBackgroundResource(R.drawable.button);
        } else if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentMPIP" + m + "3.overlay")) {
            btn3.setBackgroundResource(R.drawable.button_red);
            btn1.setBackgroundResource(R.drawable.button);
            btn2.setBackgroundResource(R.drawable.button);
        } else {
            btn1.setBackgroundResource(R.drawable.button);
            btn2.setBackgroundResource(R.drawable.button);
            btn3.setBackgroundResource(R.drawable.button);
        }
    }

    private void addItem(int id, Drawable ic, String appName, String packageName, int aurora_id, int gradicon_id, int plumpy_id) {
        View list = LayoutInflater.from(this).inflate(R.layout.list_option_mediaplayer_icons, container, false);

        LinearLayout launch = list.findViewById(R.id.launch_app);
        ImageView icon = list.findViewById(R.id.app_icon);
        TextView name = list.findViewById(R.id.app_name);
        Button aurora = list.findViewById(R.id.aurora);
        Button gradicon = list.findViewById(R.id.gradicon);
        Button plumpy = list.findViewById(R.id.plumpy);

        list.setId(id);
        icon.setBackground(ic);
        name.setText(appName);

        aurora.setId(aurora_id);
        gradicon.setId(gradicon_id);
        plumpy.setId(plumpy_id);

        launch.setOnClickListener(v -> AppUtils.launchApp(MediaPlayer.this, packageName));

        container.addView(list);
    }
}