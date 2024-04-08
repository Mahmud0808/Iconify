package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_CHANGED;
import static com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_FADE_ANIMATION;
import static com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_SWITCH;
import static com.drdisagree.iconify.common.Resources.DEPTH_WALL_BG_DIR;
import static com.drdisagree.iconify.common.Resources.DEPTH_WALL_FG_DIR;
import static com.drdisagree.iconify.utils.FileUtil.getRealPath;
import static com.drdisagree.iconify.utils.FileUtil.moveToIconifyHiddenDir;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentXposedDepthWallpaperBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class XposedDepthWallpaper extends BaseFragment {

    private FragmentXposedDepthWallpaperBinding binding;
    ActivityResultLauncher<Intent> intentForegroundImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    if (path != null && moveToIconifyHiddenDir(path, DEPTH_WALL_FG_DIR)) {
                        RPrefs.putBoolean(DEPTH_WALLPAPER_CHANGED, !binding.depthWallpaper.isSwitchChecked());
                        RPrefs.putBoolean(DEPTH_WALLPAPER_CHANGED, binding.depthWallpaper.isSwitchChecked());
                        Toast.makeText(Iconify.Companion.getAppContext(), Iconify.Companion.getAppContextLocale().getResources().getString(R.string.toast_selected_successfully), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Iconify.Companion.getAppContext(), Iconify.Companion.getAppContextLocale().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });
    ActivityResultLauncher<Intent> intentBackgroundImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    if (path != null && moveToIconifyHiddenDir(path, DEPTH_WALL_BG_DIR)) {
                        RPrefs.putBoolean(DEPTH_WALLPAPER_CHANGED, !binding.depthWallpaper.isSwitchChecked());
                        RPrefs.putBoolean(DEPTH_WALLPAPER_CHANGED, binding.depthWallpaper.isSwitchChecked());
                        Toast.makeText(Iconify.Companion.getAppContext(), Iconify.Companion.getAppContextLocale().getResources().getString(R.string.toast_selected_successfully), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Iconify.Companion.getAppContext(), Iconify.Companion.getAppContextLocale().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXposedDepthWallpaperBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_depth_wallpaper);

        // Alert dialog
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.attention))
                .setMessage(getString(R.string.depth_wallpaper_alert_msg))
                .setPositiveButton(getString(R.string.understood), (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();

        // Enable depth wallpaper
        binding.depthWallpaper.setSwitchChecked(RPrefs.getBoolean(DEPTH_WALLPAPER_SWITCH, false));
        binding.depthWallpaper.setSwitchChangeListener((buttonView, isSwitchChecked) -> {
            RPrefs.putBoolean(DEPTH_WALLPAPER_SWITCH, isSwitchChecked);
            binding.wallpaperFadeAnimation.setEnabled(isSwitchChecked);
            binding.foregroundImage.setEnabled(isSwitchChecked);
            binding.backgroundImage.setEnabled(isSwitchChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });

        // Fade animation
        binding.wallpaperFadeAnimation.setEnabled(binding.depthWallpaper.isSwitchChecked());
        binding.wallpaperFadeAnimation.setSwitchChecked(RPrefs.getBoolean(DEPTH_WALLPAPER_FADE_ANIMATION, false));
        binding.wallpaperFadeAnimation.setSwitchChangeListener((buttonView, isSwitchChecked) -> RPrefs.putBoolean(DEPTH_WALLPAPER_FADE_ANIMATION, isSwitchChecked));

        // Foreground image
        binding.foregroundImage.setEnabled(binding.depthWallpaper.isSwitchChecked());
        binding.foregroundImage.setActivityResultLauncher(intentForegroundImage);

        // Background image
        binding.backgroundImage.setEnabled(binding.depthWallpaper.isSwitchChecked());
        binding.backgroundImage.setActivityResultLauncher(intentBackgroundImage);

        return view;
    }
}