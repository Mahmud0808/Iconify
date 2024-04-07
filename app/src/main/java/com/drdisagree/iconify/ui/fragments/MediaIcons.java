package com.drdisagree.iconify.ui.fragments;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentMediaIconsBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.AppUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.drdisagree.iconify.utils.overlay.manager.MediaPlayerIconManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;

public class MediaIcons extends BaseFragment {

    private final ArrayList<String[]> MPIP_KEY = new ArrayList<>();
    private final ArrayList<Object[]> mpip_list = new ArrayList<>();
    private FragmentMediaIconsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMediaIconsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_media_icons);

        // Media Player Icon list items

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

        new Thread(() -> {
            // Check if packages are installed
            for (int i = 0; i < mpip_list.size(); i++) {
                if (i == 0) // default music player of a13
                    mpip_list.get(i)[1] = Build.VERSION.SDK_INT >= 33;
                else mpip_list.get(i)[1] = AppUtil.isAppInstalledRoot((String) mpip_list.get(i)[0]);
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                boolean isMusicPlayerShown = false;
                try {
                    TextView noSupportedPlayer = requireActivity().findViewById(R.id.no_supported_musicplayer);

                    for (int i = 0; i < mpip_list.size(); i++) {
                        if ((Boolean) mpip_list.get(i)[1]) {
                            if (i == 0) {
                                addItem(getResources().getString(R.string.a13_default_media_player), (String) mpip_list.get(i)[0], ContextCompat.getDrawable(Iconify.Companion.getAppContext(), R.drawable.ic_android), (int) mpip_list.get(i)[2]);
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
                } catch (Exception ignored) {
                }
            });
        }).start();

        return view;
    }

    // Function to check for button bg drawable changes
    private void refreshBackground() {
        for (int i = 0; i < mpip_list.size(); i++) {
            if ((Boolean) mpip_list.get(i)[1]) {
                MaterialButtonToggleGroup toggleButtonGroup = binding.getRoot().findViewById((Integer) mpip_list.get(i)[2]).findViewById(R.id.toggleButtonGroup);
                int[] buttons = {R.id.aurora, R.id.gradicon, R.id.plumpy};

                for (int j = 0; j < 3; j++) {
                    if (Prefs.getBoolean(MPIP_KEY.get(i)[j])) {
                        toggleButtonGroup.check(buttons[j]);
                    } else {
                        toggleButtonGroup.uncheck(buttons[j]);
                    }
                }
            }
        }
    }

    // Enable onClick event
    private void enableOnClickListener(int idx) {
        LinearLayout child = binding.getRoot().findViewById((int) mpip_list.get(idx)[2]);

        int[] buttons = {R.id.aurora, R.id.gradicon, R.id.plumpy};

        for (int i = 0; i < 3; i++) {
            int finalI = i + 1;
            ((MaterialButton) child.findViewById(buttons[i])).addOnCheckedChangeListener((button, isChecked) -> {
                if (isChecked) {
                    MediaPlayerIconManager.enableOverlay(idx, finalI);
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentMPIP" + idx + finalI + ".overlay");
                }
                refreshBackground();
            });
        }
    }

    private void addItem(String appName, String packageName, Drawable appIcon, int viewId) {
        View list = LayoutInflater.from(requireContext()).inflate(R.layout.view_list_option_mediaplayer_icons, binding.mediaplayerIconList, false);
        list.setId(viewId);

        LinearLayout launch = list.findViewById(R.id.launch_app);
        if (packageName != null) {
            if (packageName.equals("defaultA13")) launch.setOnClickListener(v -> {
                // do nothing
            });
            else launch.setOnClickListener(v -> AppUtil.launchApp(requireActivity(), packageName));
        }

        list.findViewById(R.id.app_icon).setBackground(appIcon);

        TextView name = list.findViewById(R.id.app_name);
        name.setText(appName);

        binding.mediaplayerIconList.addView(list);
    }
}