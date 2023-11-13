package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_CENTERED;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_TEXT_SCALING;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_LANDSCAPE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SIDEMARGIN;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_STYLE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_TEXT_WHITE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_TOPMARGIN;
import static com.drdisagree.iconify.common.Resources.HEADER_CLOCK_FONT_DIR;
import static com.drdisagree.iconify.utils.FileUtil.getRealPath;
import static com.drdisagree.iconify.utils.FileUtil.moveToIconifyHiddenDir;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
import androidx.viewpager2.widget.ViewPager2;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentXposedHeaderClockBinding;
import com.drdisagree.iconify.ui.adapters.ClockPreviewAdapter;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.models.ClockModel;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.ui.views.HeaderClockStyles;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

public class XposedHeaderClock extends BaseFragment {

    private FragmentXposedHeaderClockBinding binding;
    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    if (path != null && moveToIconifyHiddenDir(path, HEADER_CLOCK_FONT_DIR)) {
                        binding.headerClockFont.setEnableButtonVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXposedHeaderClockBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_header_clock);

        // Custom header clock
        binding.enableHeaderClock.setSwitchChecked(RPrefs.getBoolean(HEADER_CLOCK_SWITCH, false));
        binding.enableHeaderClock.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_SWITCH, isChecked);
            if (!isChecked) RPrefs.putInt(HEADER_CLOCK_STYLE, 1);
            updateEnabled(isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });
        updateEnabled(RPrefs.getBoolean(HEADER_CLOCK_SWITCH, false));

        // Header clock style
        binding.headerClockPreview.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        ClockPreviewAdapter adapter = initHeaderClockStyles();
        binding.headerClockPreview.setAdapter(adapter);
        ViewHelper.disableNestedScrolling(binding.headerClockPreview);

        binding.headerClockPreview.setCurrentItem(RPrefs.getInt(HEADER_CLOCK_STYLE, 1) - 1, false);
        binding.headerClockPreviewIndicator.setViewPager(binding.headerClockPreview);
        binding.headerClockPreviewIndicator.tintIndicator(getResources().getColor(R.color.textColorSecondary, Iconify.getAppContext().getTheme()));

        // Lockscreen clock font picker
        binding.headerClockFont.setActivityResultLauncher(startActivityIntent);
        binding.headerClockFont.setDisableButtonVisibility(RPrefs.getBoolean(HEADER_CLOCK_FONT_SWITCH, false) ? View.VISIBLE : View.GONE);

        binding.headerClockFont.setEnableButtonOnClickListener(v -> {
            RPrefs.putBoolean(HEADER_CLOCK_FONT_SWITCH, false);
            RPrefs.putBoolean(HEADER_CLOCK_FONT_SWITCH, true);
            binding.headerClockFont.setEnableButtonVisibility(View.GONE);
            binding.headerClockFont.setDisableButtonVisibility(View.VISIBLE);
        });

        binding.headerClockFont.setDisableButtonOnClickListener(v -> {
            RPrefs.putBoolean(HEADER_CLOCK_FONT_SWITCH, false);
            binding.headerClockFont.setDisableButtonVisibility(View.GONE);
        });

        // Custom clock color
        binding.headerClockCustomColor.setSwitchChecked(RPrefs.getBoolean(HEADER_CLOCK_COLOR_SWITCH, false));
        binding.headerClockCustomColor.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_COLOR_SWITCH, isChecked);
            binding.headerClockColorPicker.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Clock color picker
        binding.headerClockColorPicker.setVisibility(RPrefs.getBoolean(HEADER_CLOCK_COLOR_SWITCH, false) ? View.VISIBLE : View.GONE);
        binding.headerClockColorPicker.setColorPickerListener(
                requireActivity(),
                RPrefs.getInt(HEADER_CLOCK_COLOR_CODE, Color.WHITE),
                true,
                true,
                true
        );
        binding.headerClockColorPicker.setOnColorSelectedListener(
                color -> {
                    binding.headerClockColorPicker.setPreviewColor(color);
                    RPrefs.putInt(HEADER_CLOCK_COLOR_CODE, color);
                }
        );

        // Text Scaling
        binding.headerClockTextscaling.setSliderValue(RPrefs.getInt(HEADER_CLOCK_FONT_TEXT_SCALING, 10));
        binding.headerClockTextscaling.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                RPrefs.putInt(HEADER_CLOCK_FONT_TEXT_SCALING, (int) slider.getValue());
            }
        });

        // Header clock side margin
        binding.headerClockSideMargin.setSliderValue(RPrefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0));
        binding.headerClockSideMargin.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                RPrefs.putInt(HEADER_CLOCK_SIDEMARGIN, (int) slider.getValue());
            }
        });

        // Header clock top margin
        binding.headerClockTopMargin.setSliderValue(RPrefs.getInt(HEADER_CLOCK_TOPMARGIN, 8));
        binding.headerClockTopMargin.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                RPrefs.putInt(HEADER_CLOCK_TOPMARGIN, (int) slider.getValue());
            }
        });

        // Center clock
        binding.centerClock.setSwitchChecked(RPrefs.getBoolean(HEADER_CLOCK_CENTERED, false));
        binding.centerClock.setSwitchChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(HEADER_CLOCK_CENTERED, isChecked));

        // Force white text
        binding.forceWhiteText.setSwitchChecked(RPrefs.getBoolean(HEADER_CLOCK_TEXT_WHITE, false));
        binding.forceWhiteText.setSwitchChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(HEADER_CLOCK_TEXT_WHITE, isChecked));

        // Hide in landscape
        binding.hideHeaderClockLandscape.setSwitchChecked(RPrefs.getBoolean(HEADER_CLOCK_LANDSCAPE_SWITCH, true));
        binding.hideHeaderClockLandscape.setSwitchChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(HEADER_CLOCK_LANDSCAPE_SWITCH, isChecked));

        return view;
    }

    private ClockPreviewAdapter initHeaderClockStyles() {
        ArrayList<ClockModel> header_clock = new ArrayList<>();

        for (int i = 1; i <= 8; i++)
            header_clock.add(new ClockModel(HeaderClockStyles.initHeaderClockStyle(requireContext(), i)));

        return new ClockPreviewAdapter(requireContext(), header_clock, HEADER_CLOCK_SWITCH, HEADER_CLOCK_STYLE);
    }

    private void updateEnabled(boolean enabled) {
        binding.headerClockFont.setEnabled(enabled);
        binding.headerClockCustomColor.setEnabled(enabled);
        binding.headerClockColorPicker.setEnabled(enabled);
        binding.headerClockTextscaling.setEnabled(enabled);
        binding.headerClockSideMargin.setEnabled(enabled);
        binding.headerClockTopMargin.setEnabled(enabled);
        binding.centerClock.setEnabled(enabled);
        binding.forceWhiteText.setEnabled(enabled);
        binding.hideHeaderClockLandscape.setEnabled(enabled);
    }
}