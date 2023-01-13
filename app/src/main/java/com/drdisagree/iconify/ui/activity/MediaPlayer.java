package com.drdisagree.iconify.ui.activity;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.topjohnwu.superuser.Shell;

import java.util.Objects;

public class MediaPlayer extends AppCompatActivity {

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

        // Media Player Icon list items
        container = (ViewGroup) findViewById(R.id.mediaplayer_icon_list);

        ViewGroup.MarginLayoutParams marginParams;
        LinearLayout.LayoutParams layoutParams;

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

        musicPlayerIconList();
    }

    private void refreshPreview() {
        ImageView preview_accent = findViewById(R.id.media_player_preview_accent);
        ImageView preview_system = findViewById(R.id.media_player_preview_system);
        ImageView preview_black = findViewById(R.id.media_player_preview_black);

        if (Prefs.getBoolean("IconifyComponentMPA.overlay")) {
            preview_accent.setVisibility(View.VISIBLE);
            preview_system.setVisibility(View.GONE);
            preview_black.setVisibility(View.GONE);
        } else if (Prefs.getBoolean("IconifyComponentMPB.overlay")) {
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
        LoadMusicPlayerList musicPlayerList = new LoadMusicPlayerList();
        musicPlayerList.execute();
    }

    private void enableOnClickListener(int aurora_id, int gradicon_id, int plumpy_id, int idx) {
        Button aurora = findViewById(aurora_id);
        Button gradicon = findViewById(gradicon_id);
        Button plumpy = findViewById(plumpy_id);

        checkIfApplied(aurora, gradicon, plumpy, idx);

        aurora.setOnClickListener(v -> {
            if (Prefs.getBoolean(("IconifyComponentMPIP" + idx + "1.overlay"))) {
                Shell.cmd("cmd overlay disable --user current IconifyComponentMPIP" + idx + "1.overlay").exec();
                Prefs.putBoolean(("IconifyComponentMPIP" + idx + "1.overlay"), false);
            } else {
                MediaPlayerIconManager.install_pack(idx, 1);
                Prefs.putBoolean(("IconifyComponentMPIP" + idx + "1.overlay"), true);
            }
            checkIfApplied(aurora, gradicon, plumpy, idx);
        });

        gradicon.setOnClickListener(v -> {
            if (Prefs.getBoolean(("IconifyComponentMPIP" + idx + "2.overlay"))) {
                Shell.cmd("cmd overlay disable --user current IconifyComponentMPIP" + idx + "2.overlay").exec();
                Prefs.putBoolean(("IconifyComponentMPIP" + idx + "2.overlay"), false);
            } else {
                MediaPlayerIconManager.install_pack(idx, 2);
                Prefs.putBoolean(("IconifyComponentMPIP" + idx + "2.overlay"), true);
            }
            checkIfApplied(aurora, gradicon, plumpy, idx);
        });

        plumpy.setOnClickListener(v -> {
            if (Prefs.getBoolean(("IconifyComponentMPIP" + idx + "3.overlay"))) {
                Shell.cmd("cmd overlay disable --user current IconifyComponentMPIP" + idx + "3.overlay").exec();
                Prefs.putBoolean(("IconifyComponentMPIP" + idx + "3.overlay"), false);
            } else {
                MediaPlayerIconManager.install_pack(idx, 3);
                Prefs.putBoolean(("IconifyComponentMPIP" + idx + "3.overlay"), true);
            }
            checkIfApplied(aurora, gradicon, plumpy, idx);
        });
    }

    private void checkIfApplied(Button aurora, Button gradicon, Button plumpy, int idx) {
        refreshButton(aurora, gradicon, plumpy, idx);
    }

    private void refreshButton(Button btn1, Button btn2, Button btn3, int m) {
        if (Prefs.getBoolean("IconifyComponentMPIP" + m + "1.overlay")) {
            btn1.setBackgroundResource(R.drawable.button_red);
            btn2.setBackgroundResource(R.drawable.button);
            btn3.setBackgroundResource(R.drawable.button);
        } else if (Prefs.getBoolean("IconifyComponentMPIP" + m + "2.overlay")) {
            btn2.setBackgroundResource(R.drawable.button_red);
            btn1.setBackgroundResource(R.drawable.button);
            btn3.setBackgroundResource(R.drawable.button);
        } else if (Prefs.getBoolean("IconifyComponentMPIP" + m + "3.overlay")) {
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

        if (packageName != null)
            launch.setOnClickListener(v -> AppUtil.launchApp(MediaPlayer.this, packageName));

        container.addView(list);
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadMusicPlayerList extends AsyncTask<Integer, Integer, String> {
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

        Boolean defaultA13 = Build.VERSION.SDK_INT >= 33;
        Boolean isPowerampInstalled = false;
        Boolean isRetroInstalled = false;
        Boolean isNyxInstalled = false;
        Boolean isYmusicInstalled = false;
        Boolean isBlackholeInstalled = false;
        Boolean isMusicoletInstalled = false;
        Boolean isYoutubeInstalled = false;
        Boolean isYtmusicInstalled = false;
        Boolean isYoutubetvancedInstalled = false;
        Boolean isYtmusicvancedInstalled = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // ...
        }

        @Override
        protected String doInBackground(Integer... integers) {
            isPowerampInstalled = AppUtil.isAppInstalled(poweramp);
            isRetroInstalled = AppUtil.isAppInstalled(retro);
            isNyxInstalled = AppUtil.isAppInstalled(nyx);
            isYmusicInstalled = AppUtil.isAppInstalled(ymusic);
            isBlackholeInstalled = AppUtil.isAppInstalled(blackhole);
            isMusicoletInstalled = AppUtil.isAppInstalled(musicolet);
            isYoutubeInstalled = AppUtil.isAppInstalled(youtube);
            isYtmusicInstalled = AppUtil.isAppInstalled(yt_music);
            isYoutubetvancedInstalled = AppUtil.isAppInstalled(youtube_revanced);
            isYtmusicvancedInstalled = AppUtil.isAppInstalled(yt_music_revanced);
            return "Finished!";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            // ...
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);

            TextView title = findViewById(R.id.mediaplayer_icon_title);
            if (defaultA13 || isPowerampInstalled || isRetroInstalled || isNyxInstalled || isYmusicInstalled || isBlackholeInstalled || isMusicoletInstalled || isYoutubeInstalled || isYtmusicInstalled || isYoutubetvancedInstalled || isYtmusicvancedInstalled)
                title.setVisibility(View.VISIBLE);

            if (defaultA13) {
                addItem(R.id.poweramp, ContextCompat.getDrawable(MediaPlayer.this, R.drawable.ic_android), "Android 13 Default Player", null, R.id.defaulta13mp_aurora, R.id.defaulta13mp_gradicon, R.id.defaulta13mp_plumpy);
                enableOnClickListener(R.id.defaulta13mp_aurora, R.id.defaulta13mp_gradicon, R.id.defaulta13mp_plumpy, 0);
            }

            if (isPowerampInstalled) {
                addItem(R.id.poweramp, AppUtil.getAppIcon(poweramp), AppUtil.getAppName(poweramp), poweramp, R.id.poweramp_aurora, R.id.poweramp_gradicon, R.id.poweramp_plumpy);
                enableOnClickListener(R.id.poweramp_aurora, R.id.poweramp_gradicon, R.id.poweramp_plumpy, 1);
            }

            if (isRetroInstalled) {
                addItem(R.id.retro, AppUtil.getAppIcon(retro), AppUtil.getAppName(retro), retro, R.id.retro_aurora, R.id.retro_gradicon, R.id.retro_plumpy);
                enableOnClickListener(R.id.retro_aurora, R.id.retro_gradicon, R.id.retro_plumpy, 2);
            }

            if (isNyxInstalled) {
                addItem(R.id.nyx, AppUtil.getAppIcon(nyx), AppUtil.getAppName(nyx), nyx, R.id.nyx_aurora, R.id.nyx_gradicon, R.id.nyx_plumpy);
                enableOnClickListener(R.id.nyx_aurora, R.id.nyx_gradicon, R.id.nyx_plumpy, 3);
            }

            if (isYmusicInstalled) {
                addItem(R.id.ymusic, AppUtil.getAppIcon(ymusic), AppUtil.getAppName(ymusic), ymusic, R.id.ymusic_aurora, R.id.ymusic_gradicon, R.id.ymusic_plumpy);
                enableOnClickListener(R.id.ymusic_aurora, R.id.ymusic_gradicon, R.id.ymusic_plumpy, 4);
            }

            if (isBlackholeInstalled) {
                addItem(R.id.blackhole, AppUtil.getAppIcon(blackhole), AppUtil.getAppName(blackhole), blackhole, R.id.blackhole_aurora, R.id.blackhole_gradicon, R.id.blackhole_plumpy);
                enableOnClickListener(R.id.blackhole_aurora, R.id.blackhole_gradicon, R.id.blackhole_plumpy, 5);
            }

            if (isMusicoletInstalled) {
                addItem(R.id.musicolet, AppUtil.getAppIcon(musicolet), AppUtil.getAppName(musicolet), musicolet, R.id.musicolet_aurora, R.id.musicolet_gradicon, R.id.musicolet_plumpy);
                enableOnClickListener(R.id.musicolet_aurora, R.id.musicolet_gradicon, R.id.musicolet_plumpy, 6);
            }

            if (isYoutubeInstalled) {
                addItem(R.id.youtube, AppUtil.getAppIcon(youtube), AppUtil.getAppName(youtube), youtube, R.id.youtube_aurora, R.id.youtube_gradicon, R.id.youtube_plumpy);
                enableOnClickListener(R.id.youtube_aurora, R.id.youtube_gradicon, R.id.youtube_plumpy, 7);
            }

            if (isYtmusicInstalled) {
                addItem(R.id.yt_music, AppUtil.getAppIcon(yt_music), AppUtil.getAppName(yt_music), yt_music, R.id.yt_music_aurora, R.id.yt_music_gradicon, R.id.yt_music_plumpy);
                enableOnClickListener(R.id.yt_music_aurora, R.id.yt_music_gradicon, R.id.yt_music_plumpy, 8);
            }

            if (isYoutubetvancedInstalled) {
                addItem(R.id.youtube_revanced, AppUtil.getAppIcon(youtube_revanced), AppUtil.getAppName(youtube_revanced), youtube_revanced, R.id.youtube_revanced_aurora, R.id.youtube_revanced_gradicon, R.id.youtube_revanced_plumpy);
                enableOnClickListener(R.id.youtube_revanced_aurora, R.id.youtube_revanced_gradicon, R.id.youtube_revanced_plumpy, 9);
            }

            if (isYtmusicvancedInstalled) {
                addItem(R.id.yt_music_revanced, AppUtil.getAppIcon(yt_music_revanced), AppUtil.getAppName(yt_music_revanced), yt_music_revanced, R.id.yt_music_revanced_aurora, R.id.yt_music_revanced_gradicon, R.id.yt_music_revanced_plumpy);
                enableOnClickListener(R.id.yt_music_revanced_aurora, R.id.yt_music_revanced_gradicon, R.id.yt_music_revanced_plumpy, 10);
            }
        }
    }
}