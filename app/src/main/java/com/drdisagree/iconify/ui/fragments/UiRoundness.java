package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Preferences.UI_CORNER_RADIUS;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentUiRoundnessBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.dialogs.LoadingDialog;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.manager.RoundnessManager;
import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class UiRoundness extends BaseFragment {

    private FragmentUiRoundnessBinding binding;
    private LoadingDialog loadingDialog;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUiRoundnessBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_ui_roundness);

        // Show loading dialog
        loadingDialog = new LoadingDialog(requireContext());

        // Header
        ViewHelper.setHeader(requireContext(), binding.header.toolbar, R.string.activity_title_ui_roundness);

        // Corner Radius
        GradientDrawable[] drawables = new GradientDrawable[]{
                (GradientDrawable) binding.qsTilePreview1.getBackground(),
                (GradientDrawable) binding.qsTilePreview2.getBackground(),
                (GradientDrawable) binding.qsTilePreview3.getBackground(),
                (GradientDrawable) binding.qsTilePreview4.getBackground(),
                (GradientDrawable) binding.brightnessBarBg.getBackground(),
                (GradientDrawable) binding.brightnessBarFg.getBackground(),
                (GradientDrawable) binding.autoBrightness.getBackground()
        };

        final int[] finalUiCornerRadius = {Prefs.getInt(UI_CORNER_RADIUS, 28)};

        if (finalUiCornerRadius[0] == 28) {
            binding.cornerRadiusOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalUiCornerRadius[0] + "dp " + Iconify.getAppContextLocale().getResources().getString(R.string.opt_default));
        } else {
            binding.cornerRadiusOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalUiCornerRadius[0] + "dp");
        }
        for (GradientDrawable drawable : drawables) {
            drawable.setCornerRadius(finalUiCornerRadius[0] * Iconify.getAppContextLocale().getResources().getDisplayMetrics().density);
        }
        binding.cornerRadiusSeekbar.setValue(finalUiCornerRadius[0]);

        binding.cornerRadiusSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalUiCornerRadius[0] = (int) slider.getValue();
                if (finalUiCornerRadius[0] == 28)
                    binding.cornerRadiusOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalUiCornerRadius[0] + "dp " + Iconify.getAppContextLocale().getResources().getString(R.string.opt_default));
                else
                    binding.cornerRadiusOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalUiCornerRadius[0] + "dp");
            }
        });

        binding.cornerRadiusSeekbar.addOnChangeListener((slider, value, fromUser) -> {
            for (GradientDrawable drawable : drawables) {
                drawable.setCornerRadius(value * Iconify.getAppContextLocale().getResources().getDisplayMetrics().density);
            }
        });

        binding.applyRadius.setOnClickListener(v -> {
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                // Show loading dialog
                loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));
                AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                new Thread(() -> {
                    try {
                        hasErroredOut.set(RoundnessManager.buildOverlay(finalUiCornerRadius[0], true));
                    } catch (IOException e) {
                        hasErroredOut.set(true);
                        Log.e("UiRoundness", e.toString());
                    }

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!hasErroredOut.get()) {
                            Prefs.putInt(UI_CORNER_RADIUS, finalUiCornerRadius[0]);
                            RPrefs.putInt(UI_CORNER_RADIUS, finalUiCornerRadius[0]);
                        }

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            // Hide loading dialog
                            loadingDialog.hide();

                            if (hasErroredOut.get())
                                Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                        }, 2000);
                    });
                }).start();
            }
        });

        // Change orientation in landscape / portrait mode
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.qsTileOrientation.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            binding.qsTileOrientation.setOrientation(LinearLayout.VERTICAL);
        }

        return view;
    }

    // Change tile orientation in landscape / portrait mode
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            binding.qsTileOrientation.setOrientation(LinearLayout.HORIZONTAL);
        else binding.qsTileOrientation.setOrientation(LinearLayout.VERTICAL);
    }

    @Override
    public void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
}