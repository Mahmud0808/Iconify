package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Preferences.LSCLOCK_BOTTOMMARGIN;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_LINEHEIGHT;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_TEXT_SCALING;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_STYLE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_TEXT_WHITE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_TOPMARGIN;
import static com.drdisagree.iconify.common.Resources.LSCLOCK_FONT_DIR;
import static com.drdisagree.iconify.utils.FileUtil.moveToIconifyHiddenDir;
import static com.drdisagree.iconify.utils.FileUtil.getRealPath;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.drdisagree.iconify.databinding.FragmentXposedLockscreenClockBinding;
import com.drdisagree.iconify.ui.adapters.ClockPreviewAdapter;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.models.ClockModel;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.ui.views.LockscreenClockStyles;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

public class XposedLockscreenClock extends BaseFragment {

    private FragmentXposedLockscreenClockBinding binding;
    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    if (path != null && moveToIconifyHiddenDir(path, LSCLOCK_FONT_DIR)) {
                        binding.lockscreenClockFont.setEnableButtonVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXposedLockscreenClockBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_lockscreen_clock);

        // Enable lockscreen clock
        binding.enableLockscreenClock.setSwitchChecked(RPrefs.getBoolean(LSCLOCK_SWITCH, false));
        binding.enableLockscreenClock.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LSCLOCK_SWITCH, isChecked);
            if (!isChecked) RPrefs.putInt(LSCLOCK_STYLE, 0);
            updateEnabled(isChecked);
        });
        updateEnabled(RPrefs.getBoolean(LSCLOCK_SWITCH, false));

        // Lockscreen clock style
        binding.lockscreenClockPreview.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        ClockPreviewAdapter adapter = initLockscreenClockStyles();
        binding.lockscreenClockPreview.setAdapter(adapter);
        ViewHelper.disableNestedScrolling(binding.lockscreenClockPreview);

        binding.lockscreenClockPreview.setCurrentItem(RPrefs.getInt(LSCLOCK_STYLE, 0), false);
        binding.lockscreenClockPreviewIndicator.setViewPager(binding.lockscreenClockPreview);
        binding.lockscreenClockPreviewIndicator.tintIndicator(getResources().getColor(R.color.textColorSecondary, Iconify.getAppContext().getTheme()));

        // Lockscreen clock font picker
        binding.lockscreenClockFont.setActivityResultLauncher(startActivityIntent);

        binding.lockscreenClockFont.setDisableButtonVisibility(RPrefs.getBoolean(LSCLOCK_FONT_SWITCH, false) ? View.VISIBLE : View.GONE);

        binding.lockscreenClockFont.setEnableButtonOnClickListener(v -> {
            RPrefs.putBoolean(LSCLOCK_FONT_SWITCH, false);
            RPrefs.putBoolean(LSCLOCK_FONT_SWITCH, true);
            binding.lockscreenClockFont.setEnableButtonVisibility(View.GONE);
            binding.lockscreenClockFont.setDisableButtonVisibility(View.VISIBLE);
        });

        binding.lockscreenClockFont.setDisableButtonOnClickListener(v -> {
            RPrefs.putBoolean(LSCLOCK_FONT_SWITCH, false);
            binding.lockscreenClockFont.setDisableButtonVisibility(View.GONE);
        });

        // Custom clock color
        binding.lsClockCustomColor.setSwitchChecked(RPrefs.getBoolean(LSCLOCK_COLOR_SWITCH, false));
        binding.lsClockCustomColor.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LSCLOCK_COLOR_SWITCH, isChecked);
            binding.lsClockColorPicker.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        binding.lsClockColorPicker.setVisibility(RPrefs.getBoolean(LSCLOCK_COLOR_SWITCH, false) ? View.VISIBLE : View.GONE);

        // Clock color picker
        binding.lsClockColorPicker.setColorPickerListener(
                requireActivity(),
                RPrefs.getInt(LSCLOCK_COLOR_CODE, Color.WHITE),
                true,
                true,
                true
        );
        binding.lsClockColorPicker.setOnColorSelectedListener(
                color -> {
                    binding.lsClockColorPicker.setPreviewColor(color);
                    RPrefs.putInt(LSCLOCK_COLOR_CODE, color);
                }
        );

        // Line height
        binding.lsclockLineHeight.setSliderValue(RPrefs.getInt(LSCLOCK_FONT_LINEHEIGHT, 0));
        binding.lsclockLineHeight.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                RPrefs.putInt(LSCLOCK_FONT_LINEHEIGHT, (int) slider.getValue());
            }
        });

        // Text Scaling
        binding.lsClockTextscaling.setSliderValue(RPrefs.getInt(LSCLOCK_FONT_TEXT_SCALING, 10));
        binding.lsClockTextscaling.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                RPrefs.putInt(LSCLOCK_FONT_TEXT_SCALING, (int) slider.getValue());
            }
        });

        // Top margin
        binding.lsclockTopMargin.setSliderValue(RPrefs.getInt(LSCLOCK_TOPMARGIN, 100));
        binding.lsclockTopMargin.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                RPrefs.putInt(LSCLOCK_TOPMARGIN, (int) slider.getValue());
            }
        });

        // Bottom margin
        binding.lsclockBottomMargin.setSliderValue(RPrefs.getInt(LSCLOCK_BOTTOMMARGIN, 40));
        binding.lsclockBottomMargin.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                RPrefs.putInt(LSCLOCK_BOTTOMMARGIN, (int) slider.getValue());
            }
        });

        // Force white text
        binding.forceWhiteText.setSwitchChecked(RPrefs.getBoolean(LSCLOCK_TEXT_WHITE, false));
        binding.forceWhiteText.setSwitchChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(LSCLOCK_TEXT_WHITE, isChecked));

        return view;
    }

    private ClockPreviewAdapter initLockscreenClockStyles() {
        ArrayList<ClockModel> ls_clock = new ArrayList<>();

        for (int i = 0; i <= 8; i++)
            ls_clock.add(new ClockModel(LockscreenClockStyles.initLockscreenClockStyle(requireContext(), i)));

        return new ClockPreviewAdapter(requireContext(), ls_clock, LSCLOCK_SWITCH, LSCLOCK_STYLE);
    }

    private void updateEnabled(boolean enabled) {
        binding.lockscreenClockFont.setEnabled(enabled);
        binding.lsClockCustomColor.setEnabled(enabled);
        binding.lsClockColorPicker.setEnabled(enabled);
        binding.lsclockLineHeight.setEnabled(enabled);
        binding.lsClockTextscaling.setEnabled(enabled);
        binding.lsclockTopMargin.setEnabled(enabled);
        binding.lsclockBottomMargin.setEnabled(enabled);
        binding.forceWhiteText.setEnabled(enabled);
    }
}