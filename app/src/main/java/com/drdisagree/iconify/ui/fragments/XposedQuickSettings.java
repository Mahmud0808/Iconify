package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Preferences.HIDE_QSLABEL_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QQS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.QS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.VERTICAL_QSTILE_SWITCH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentXposedQuickSettingsBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.google.android.material.slider.Slider;

public class XposedQuickSettings extends BaseFragment {

    private FragmentXposedQuickSettingsBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXposedQuickSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_quick_settings);

        // Vertical QS Tile
        binding.enableVerticalTile.setChecked(RPrefs.getBoolean(VERTICAL_QSTILE_SWITCH, false));
        binding.enableVerticalTile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(VERTICAL_QSTILE_SWITCH, isChecked);
            binding.hideTileLabel.setEnabled(isChecked);
        });
        binding.verticalTileContainer.setOnClickListener(v -> binding.enableVerticalTile.toggle());
        binding.hideTileLabel.setEnabled(binding.enableVerticalTile.isChecked());

        // Hide label for vertical tiles
        binding.hideTileLabel.setChecked(RPrefs.getBoolean(HIDE_QSLABEL_SWITCH, false));
        binding.hideTileLabel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_QSLABEL_SWITCH, isChecked);
        });
        binding.hideTileLabelContainer.setOnClickListener(v -> {
            if (binding.hideTileLabel.isEnabled())
                binding.hideTileLabel.toggle();
        });

        // QQS panel top margin slider
        binding.qqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(QQS_TOPMARGIN, 100) + "dp");
        binding.qqsTopMarginSeekbar.setValue(RPrefs.getInt(QQS_TOPMARGIN, 100));
        final int[] qqsTopMargin = {RPrefs.getInt(QQS_TOPMARGIN, 100)};
        binding.qqsTopMarginSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                qqsTopMargin[0] = (int) slider.getValue();
                binding.qqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + qqsTopMargin[0] + "dp");
                binding.resetQqsTopMargin.setVisibility(View.VISIBLE);
                RPrefs.putInt(QQS_TOPMARGIN, qqsTopMargin[0]);
            }
        });

        // QQS Reset button
        binding.resetQqsTopMargin.setVisibility(RPrefs.getInt(QQS_TOPMARGIN, -1) != -1 ? View.VISIBLE : View.INVISIBLE);
        binding.resetQqsTopMargin.setOnLongClickListener(v -> {
            binding.qqsTopMarginSeekbar.setValue(100);
            binding.qqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + 100 + "dp");
            binding.resetQqsTopMargin.setVisibility(View.INVISIBLE);
            RPrefs.clearPref(QQS_TOPMARGIN);
            return true;
        });

        // QS panel top margin slider
        binding.qsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(QS_TOPMARGIN, 100) + "dp");
        binding.qsTopMarginSeekbar.setValue(RPrefs.getInt(QS_TOPMARGIN, 100));
        final int[] qsTopMargin = {RPrefs.getInt(QS_TOPMARGIN, 100)};
        binding.qsTopMarginSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                qsTopMargin[0] = (int) slider.getValue();
                binding.qsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + qsTopMargin[0] + "dp");
                binding.resetQsTopMargin.setVisibility(View.VISIBLE);
                RPrefs.putInt(QS_TOPMARGIN, qsTopMargin[0]);
            }
        });

        // QS Reset button
        binding.resetQsTopMargin.setVisibility(RPrefs.getInt(QS_TOPMARGIN, -1) != -1 ? View.VISIBLE : View.INVISIBLE);
        binding.resetQsTopMargin.setOnLongClickListener(v -> {
            binding.qsTopMarginSeekbar.setValue(100);
            binding.qsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + 100 + "dp");
            binding.resetQsTopMargin.setVisibility(View.INVISIBLE);
            RPrefs.clearPref(QS_TOPMARGIN);
            return true;
        });

        return view;
    }
}