package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_BOTTOMMARGIN;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT1;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT2;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT3;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_TEXT1;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_TEXT2;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_LINEHEIGHT;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_TEXT_SCALING;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_STYLE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_TOPMARGIN;
import static com.drdisagree.iconify.common.Resources.LOCKSCREEN_CLOCK_LAYOUT;
import static com.drdisagree.iconify.common.Resources.LSCLOCK_FONT_DIR;
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
import com.drdisagree.iconify.databinding.FragmentXposedLockscreenClockBinding;
import com.drdisagree.iconify.ui.adapters.ClockPreviewAdapter;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.models.ClockModel;
import com.drdisagree.iconify.ui.utils.CarouselLayoutManager;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
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
            updateEnabled(isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });
        updateEnabled(RPrefs.getBoolean(LSCLOCK_SWITCH, false));

        // Lockscreen clock style
        SnapHelper snapHelper = new LinearSnapHelper();
        binding.rvLockscreenClockPreview.setLayoutManager(new CarouselLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        binding.rvLockscreenClockPreview.setAdapter(initLockscreenClockStyles());
        binding.rvLockscreenClockPreview.setHasFixedSize(true);
        snapHelper.attachToRecyclerView(binding.rvLockscreenClockPreview);
        binding.rvLockscreenClockPreview.scrollToPosition(RPrefs.getInt(LSCLOCK_STYLE, 0));

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
            RPrefs.clearPref(LSCLOCK_COLOR_CODE_ACCENT1);
            RPrefs.clearPref(LSCLOCK_COLOR_CODE_ACCENT2);
            RPrefs.clearPref(LSCLOCK_COLOR_CODE_ACCENT3);
            RPrefs.clearPref(LSCLOCK_COLOR_CODE_TEXT1);
            RPrefs.clearPref(LSCLOCK_COLOR_CODE_TEXT2);

            binding.lsClockColorPicker.setVisibility(isChecked ? View.VISIBLE : View.GONE);

            if (!isChecked) {
                binding.colorPickerAccent1.setPreviewColor(ContextCompat.getColor(requireContext(), android.R.color.system_accent1_300));
                binding.colorPickerAccent2.setPreviewColor(ContextCompat.getColor(requireContext(), android.R.color.system_accent2_300));
                binding.colorPickerAccent3.setPreviewColor(ContextCompat.getColor(requireContext(), android.R.color.system_accent3_300));
                binding.colorPickerText1.setPreviewColor(Color.WHITE);
                binding.colorPickerText2.setPreviewColor(Color.BLACK);
            }
        });

        binding.lsClockColorPicker.setVisibility(RPrefs.getBoolean(LSCLOCK_COLOR_SWITCH, false) ? View.VISIBLE : View.GONE);

        // Clock color picker accent 1
        binding.colorPickerAccent1.setColorPickerListener(
                requireActivity(),
                RPrefs.getInt(
                        LSCLOCK_COLOR_CODE_ACCENT1,
                        ContextCompat.getColor(requireContext(), android.R.color.system_accent1_300)
                ),
                true,
                true,
                true
        );
        binding.colorPickerAccent1.setOnColorSelectedListener(
                color -> {
                    binding.colorPickerAccent1.setPreviewColor(color);
                    RPrefs.putInt(LSCLOCK_COLOR_CODE_ACCENT1, color);
                }
        );

        // Clock color picker accent 2
        binding.colorPickerAccent2.setColorPickerListener(
                requireActivity(),
                RPrefs.getInt(
                        LSCLOCK_COLOR_CODE_ACCENT2,
                        ContextCompat.getColor(requireContext(), android.R.color.system_accent2_300)
                ),
                true,
                true,
                true
        );
        binding.colorPickerAccent2.setOnColorSelectedListener(
                color -> {
                    binding.colorPickerAccent2.setPreviewColor(color);
                    RPrefs.putInt(LSCLOCK_COLOR_CODE_ACCENT2, color);
                }
        );

        // Clock color picker accent 3
        binding.colorPickerAccent3.setColorPickerListener(
                requireActivity(),
                RPrefs.getInt(
                        LSCLOCK_COLOR_CODE_ACCENT3,
                        ContextCompat.getColor(requireContext(), android.R.color.system_accent3_300)
                ),
                true,
                true,
                true
        );
        binding.colorPickerAccent3.setOnColorSelectedListener(
                color -> {
                    binding.colorPickerAccent3.setPreviewColor(color);
                    RPrefs.putInt(LSCLOCK_COLOR_CODE_ACCENT3, color);
                }
        );

        // Clock color picker text 1
        binding.colorPickerText1.setColorPickerListener(
                requireActivity(),
                RPrefs.getInt(
                        LSCLOCK_COLOR_CODE_TEXT1,
                        Color.WHITE
                ),
                true,
                true,
                true
        );
        binding.colorPickerText1.setOnColorSelectedListener(
                color -> {
                    binding.colorPickerText1.setPreviewColor(color);
                    RPrefs.putInt(LSCLOCK_COLOR_CODE_TEXT1, color);
                }
        );

        // Clock color picker text 2
        binding.colorPickerText2.setColorPickerListener(
                requireActivity(),
                RPrefs.getInt(
                        LSCLOCK_COLOR_CODE_TEXT2,
                        Color.BLACK
                ),
                true,
                true,
                true
        );
        binding.colorPickerText2.setOnColorSelectedListener(
                color -> {
                    binding.colorPickerText2.setPreviewColor(color);
                    RPrefs.putInt(LSCLOCK_COLOR_CODE_TEXT2, color);
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
        binding.lsclockLineHeight.setResetClickListener(v -> {
            RPrefs.clearPref(LSCLOCK_FONT_LINEHEIGHT);
            return true;
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
        binding.lsClockTextscaling.setResetClickListener(v -> {
            RPrefs.clearPref(LSCLOCK_FONT_TEXT_SCALING);
            return true;
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

        return view;
    }

    @SuppressLint("DiscouragedApi")
    private ClockPreviewAdapter initLockscreenClockStyles() {
        ArrayList<ClockModel> ls_clock = new ArrayList<>();

        int maxIndex = 0;
        while (requireContext()
                .getResources()
                .getIdentifier(
                        LOCKSCREEN_CLOCK_LAYOUT + maxIndex,
                        "layout",
                        BuildConfig.APPLICATION_ID
                ) != 0) {
            maxIndex++;
        }

        for (int i = 0; i < maxIndex; i++) {
            ls_clock.add(new ClockModel(
                    i == 0 ?
                            "No Clock" :
                            "Clock Style " + i,
                    requireContext()
                            .getResources()
                            .getIdentifier(
                                    LOCKSCREEN_CLOCK_LAYOUT + i,
                                    "layout",
                                    BuildConfig.APPLICATION_ID
                            )
            ));
        }

        return new ClockPreviewAdapter(requireContext(), ls_clock, LSCLOCK_SWITCH, LSCLOCK_STYLE);
    }

    private void updateEnabled(boolean enabled) {
        binding.lockscreenClockFont.setEnabled(enabled);
        binding.lsClockCustomColor.setEnabled(enabled);
        binding.colorPickerAccent1.setEnabled(enabled);
        binding.colorPickerAccent2.setEnabled(enabled);
        binding.colorPickerAccent3.setEnabled(enabled);
        binding.colorPickerText1.setEnabled(enabled);
        binding.colorPickerText2.setEnabled(enabled);
        binding.lsclockLineHeight.setEnabled(enabled);
        binding.lsClockTextscaling.setEnabled(enabled);
        binding.lsclockTopMargin.setEnabled(enabled);
        binding.lsclockBottomMargin.setEnabled(enabled);
    }
}