package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_CENTERED;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT1;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT2;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT3;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_TEXT1;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_TEXT2;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_TEXT_SCALING;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_LANDSCAPE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SIDEMARGIN;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_STYLE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_TOPMARGIN;
import static com.drdisagree.iconify.common.Resources.HEADER_CLOCK_FONT_DIR;
import static com.drdisagree.iconify.common.Resources.HEADER_CLOCK_LAYOUT;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentXposedHeaderClockBinding;
import com.drdisagree.iconify.ui.adapters.ClockPreviewAdapter;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.models.ClockModel;
import com.drdisagree.iconify.ui.utils.CarouselLayoutManager;
import com.drdisagree.iconify.ui.utils.ViewHelper;
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
                        Toast.makeText(Iconify.Companion.getAppContext(), Iconify.Companion.getAppContextLocale().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
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

        // Enable header clock
        binding.enableHeaderClock.setSwitchChecked(RPrefs.getBoolean(HEADER_CLOCK_SWITCH, false));
        binding.enableHeaderClock.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_SWITCH, isChecked);
            updateEnabled(isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });
        updateEnabled(RPrefs.getBoolean(HEADER_CLOCK_SWITCH, false));

        // Header clock style
        SnapHelper snapHelper = new LinearSnapHelper();
        binding.rvHeaderClockPreview.setLayoutManager(new CarouselLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        binding.rvHeaderClockPreview.setAdapter(initHeaderClockStyles());
        binding.rvHeaderClockPreview.setHasFixedSize(true);
        snapHelper.attachToRecyclerView(binding.rvHeaderClockPreview);
        binding.rvHeaderClockPreview.scrollToPosition(RPrefs.getInt(HEADER_CLOCK_STYLE, 0));

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
            RPrefs.clearPref(HEADER_CLOCK_COLOR_CODE_ACCENT1);
            RPrefs.clearPref(HEADER_CLOCK_COLOR_CODE_ACCENT2);
            RPrefs.clearPref(HEADER_CLOCK_COLOR_CODE_ACCENT3);
            RPrefs.clearPref(HEADER_CLOCK_COLOR_CODE_TEXT1);
            RPrefs.clearPref(HEADER_CLOCK_COLOR_CODE_TEXT2);

            binding.headerClockColorPicker.setVisibility(isChecked ? View.VISIBLE : View.GONE);

            if (!isChecked) {
                binding.colorPickerAccent1.setPreviewColor(ContextCompat.getColor(requireContext(), android.R.color.system_accent1_300));
                binding.colorPickerAccent2.setPreviewColor(ContextCompat.getColor(requireContext(), android.R.color.system_accent2_300));
                binding.colorPickerAccent3.setPreviewColor(ContextCompat.getColor(requireContext(), android.R.color.system_accent3_300));
                binding.colorPickerText1.setPreviewColor(Color.WHITE);
                binding.colorPickerText2.setPreviewColor(Color.BLACK);
            }
        });

        binding.headerClockColorPicker.setVisibility(RPrefs.getBoolean(HEADER_CLOCK_COLOR_SWITCH, false) ? View.VISIBLE : View.GONE);

        // Clock color picker accent 1
        binding.colorPickerAccent1.setColorPickerListener(
                requireActivity(),
                RPrefs.getInt(
                        HEADER_CLOCK_COLOR_CODE_ACCENT1,
                        ContextCompat.getColor(requireContext(), android.R.color.system_accent1_300)
                ),
                true,
                true,
                true
        );
        binding.colorPickerAccent1.setOnColorSelectedListener(
                color -> {
                    binding.colorPickerAccent1.setPreviewColor(color);
                    RPrefs.putInt(HEADER_CLOCK_COLOR_CODE_ACCENT1, color);
                }
        );

        // Clock color picker accent 2
        binding.colorPickerAccent2.setColorPickerListener(
                requireActivity(),
                RPrefs.getInt(
                        HEADER_CLOCK_COLOR_CODE_ACCENT2,
                        ContextCompat.getColor(requireContext(), android.R.color.system_accent2_300)
                ),
                true,
                true,
                true
        );
        binding.colorPickerAccent2.setOnColorSelectedListener(
                color -> {
                    binding.colorPickerAccent2.setPreviewColor(color);
                    RPrefs.putInt(HEADER_CLOCK_COLOR_CODE_ACCENT2, color);
                }
        );

        // Clock color picker accent 3
        binding.colorPickerAccent3.setColorPickerListener(
                requireActivity(),
                RPrefs.getInt(
                        HEADER_CLOCK_COLOR_CODE_ACCENT3,
                        ContextCompat.getColor(requireContext(), android.R.color.system_accent3_300)
                ),
                true,
                true,
                true
        );
        binding.colorPickerAccent3.setOnColorSelectedListener(
                color -> {
                    binding.colorPickerAccent3.setPreviewColor(color);
                    RPrefs.putInt(HEADER_CLOCK_COLOR_CODE_ACCENT3, color);
                }
        );

        // Clock color picker text 1
        binding.colorPickerText1.setColorPickerListener(
                requireActivity(),
                RPrefs.getInt(
                        HEADER_CLOCK_COLOR_CODE_TEXT1,
                        Color.WHITE
                ),
                true,
                true,
                true
        );
        binding.colorPickerText1.setOnColorSelectedListener(
                color -> {
                    binding.colorPickerText1.setPreviewColor(color);
                    RPrefs.putInt(HEADER_CLOCK_COLOR_CODE_TEXT1, color);
                }
        );

        // Clock color picker text 2
        binding.colorPickerText2.setColorPickerListener(
                requireActivity(),
                RPrefs.getInt(
                        HEADER_CLOCK_COLOR_CODE_TEXT2,
                        Color.BLACK
                ),
                true,
                true,
                true
        );
        binding.colorPickerText2.setOnColorSelectedListener(
                color -> {
                    binding.colorPickerText2.setPreviewColor(color);
                    RPrefs.putInt(HEADER_CLOCK_COLOR_CODE_TEXT2, color);
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
        binding.headerClockTextscaling.setResetClickListener(v -> {
            RPrefs.clearPref(HEADER_CLOCK_FONT_TEXT_SCALING);
            return true;
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

        // Hide in landscape
        binding.hideHeaderClockLandscape.setSwitchChecked(RPrefs.getBoolean(HEADER_CLOCK_LANDSCAPE_SWITCH, true));
        binding.hideHeaderClockLandscape.setSwitchChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(HEADER_CLOCK_LANDSCAPE_SWITCH, isChecked));

        return view;
    }

    @SuppressLint("DiscouragedApi")
    private ClockPreviewAdapter initHeaderClockStyles() {
        ArrayList<ClockModel> header_clock = new ArrayList<>();

        int maxIndex = 0;
        while (requireContext()
                .getResources()
                .getIdentifier(
                        HEADER_CLOCK_LAYOUT + maxIndex,
                        "layout",
                        BuildConfig.APPLICATION_ID
                ) != 0) {
            maxIndex++;
        }

        for (int i = 0; i < maxIndex; i++) {
            header_clock.add(new ClockModel(
                    i == 0 ?
                            "No Clock" :
                            "Clock Style " + i,
                    requireContext()
                            .getResources()
                            .getIdentifier(
                                    HEADER_CLOCK_LAYOUT + i,
                                    "layout",
                                    BuildConfig.APPLICATION_ID
                            )
            ));
        }

        return new ClockPreviewAdapter(requireContext(), header_clock, HEADER_CLOCK_SWITCH, HEADER_CLOCK_STYLE);
    }

    private void updateEnabled(boolean enabled) {
        binding.headerClockFont.setEnabled(enabled);
        binding.headerClockCustomColor.setEnabled(enabled);
        binding.colorPickerAccent1.setEnabled(enabled);
        binding.colorPickerAccent2.setEnabled(enabled);
        binding.colorPickerAccent3.setEnabled(enabled);
        binding.colorPickerText1.setEnabled(enabled);
        binding.colorPickerText2.setEnabled(enabled);
        binding.headerClockTextscaling.setEnabled(enabled);
        binding.headerClockSideMargin.setEnabled(enabled);
        binding.headerClockTopMargin.setEnabled(enabled);
        binding.centerClock.setEnabled(enabled);
        binding.hideHeaderClockLandscape.setEnabled(enabled);
    }
}