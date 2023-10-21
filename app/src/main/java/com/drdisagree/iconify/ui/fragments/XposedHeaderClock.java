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
import static com.drdisagree.iconify.utils.FileUtil.copyToIconifyHiddenDir;
import static com.drdisagree.iconify.utils.FileUtil.getRealPath;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import com.drdisagree.iconify.ui.activities.HomePage;
import com.drdisagree.iconify.ui.adapters.ClockPreviewAdapter;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.events.ColorSelectedEvent;
import com.drdisagree.iconify.ui.models.ClockModel;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.ui.views.HeaderClockStyles;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.slider.Slider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class XposedHeaderClock extends BaseFragment {

    private FragmentXposedHeaderClockBinding binding;
    private static int colorHeaderClock;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXposedHeaderClockBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_header_clock);

        // Custom header clock
        binding.enableHeaderClock.setChecked(RPrefs.getBoolean(HEADER_CLOCK_SWITCH, false));
        binding.enableHeaderClock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_SWITCH, isChecked);
            if (!isChecked) RPrefs.putInt(HEADER_CLOCK_STYLE, 1);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });
        binding.enableHeaderClockContainer.setOnClickListener(v -> binding.enableHeaderClock.toggle());

        // Header clock style
        binding.headerClockPreview.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        ClockPreviewAdapter adapter = initHeaderClockStyles();
        binding.headerClockPreview.setAdapter(adapter);
        ViewHelper.disableNestedScrolling(binding.headerClockPreview);

        binding.headerClockPreview.setCurrentItem(RPrefs.getInt(HEADER_CLOCK_STYLE, 1) - 1);
        binding.headerClockPreviewIndicator.setViewPager(binding.headerClockPreview);
        binding.headerClockPreviewIndicator.tintIndicator(getResources().getColor(R.color.textColorSecondary, Iconify.getAppContext().getTheme()));

        // Lockscreen clock font picker
        binding.pickHeaderClockFont.setOnClickListener(v -> {
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                browseHeaderClockFont();
            }
        });

        binding.disableHeaderClockFont.setVisibility(RPrefs.getBoolean(HEADER_CLOCK_FONT_SWITCH, false) ? View.VISIBLE : View.GONE);

        binding.enableHeaderClockFont.setOnClickListener(v -> {
            RPrefs.putBoolean(HEADER_CLOCK_FONT_SWITCH, false);
            RPrefs.putBoolean(HEADER_CLOCK_FONT_SWITCH, true);
            binding.enableHeaderClockFont.setVisibility(View.GONE);
            binding.disableHeaderClockFont.setVisibility(View.VISIBLE);
        });

        binding.disableHeaderClockFont.setOnClickListener(v -> {
            RPrefs.putBoolean(HEADER_CLOCK_FONT_SWITCH, false);
            binding.disableHeaderClockFont.setVisibility(View.GONE);
        });

        // Custom clock color
        binding.enableHeaderClockCustomColor.setChecked(RPrefs.getBoolean(HEADER_CLOCK_COLOR_SWITCH, false));
        binding.enableHeaderClockCustomColor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_COLOR_SWITCH, isChecked);
            binding.headerClockColorPicker.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        binding.headerClockCustomColorContainer.setOnClickListener(v -> binding.enableHeaderClockCustomColor.toggle());

        binding.headerClockColorPicker.setVisibility(RPrefs.getBoolean(HEADER_CLOCK_COLOR_SWITCH, false) ? View.VISIBLE : View.GONE);

        // Clock color picker
        colorHeaderClock = RPrefs.getInt(HEADER_CLOCK_COLOR_CODE, Color.WHITE);
        binding.headerClockColorPicker.setOnClickListener(v -> ((HomePage) requireActivity()).showColorPickerDialog(1, colorHeaderClock, true, true, true));
        updateColorPreview();

        // Text Scaling
        final int[] textScaling = {RPrefs.getInt(HEADER_CLOCK_FONT_TEXT_SCALING, 10)};
        binding.headerClockTextscalingOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + new DecimalFormat("#.#").format(textScaling[0] / 10.0) + "x");
        binding.headerClockTextscalingSeekbar.setValue(RPrefs.getInt(HEADER_CLOCK_FONT_TEXT_SCALING, 10));
        binding.headerClockTextscalingSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                textScaling[0] = (int) slider.getValue();
                binding.headerClockTextscalingOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + new DecimalFormat("#.#").format(textScaling[0] / 10.0) + "x");
                RPrefs.putInt(HEADER_CLOCK_FONT_TEXT_SCALING, textScaling[0]);
            }
        });

        // Header clock side margin
        binding.headerClockSideMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0) + "dp");
        binding.headerClockSideMarginSeekbar.setValue(RPrefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0));
        final int[] sideMargin = {RPrefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0)};
        binding.headerClockSideMarginSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                sideMargin[0] = (int) slider.getValue();
                binding.headerClockSideMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + sideMargin[0] + "dp");
                RPrefs.putInt(HEADER_CLOCK_SIDEMARGIN, sideMargin[0]);
            }
        });

        // Header clock top margin
        binding.headerClockTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(HEADER_CLOCK_TOPMARGIN, 8) + "dp");
        binding.headerClockTopMarginSeekbar.setValue(RPrefs.getInt(HEADER_CLOCK_TOPMARGIN, 8));
        final int[] topMargin = {RPrefs.getInt(HEADER_CLOCK_TOPMARGIN, 8)};
        binding.headerClockTopMarginSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                topMargin[0] = (int) slider.getValue();
                binding.headerClockTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + topMargin[0] + "dp");
                RPrefs.putInt(HEADER_CLOCK_TOPMARGIN, topMargin[0]);
            }
        });

        // Center clock
        binding.enableCenterClock.setChecked(RPrefs.getBoolean(HEADER_CLOCK_CENTERED, false));
        binding.enableCenterClock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_CENTERED, isChecked);
        });
        binding.centerClockContainer.setOnClickListener(v -> binding.enableCenterClock.toggle());

        // Force white text
        binding.enableForceWhiteText.setChecked(RPrefs.getBoolean(HEADER_CLOCK_TEXT_WHITE, false));
        binding.enableForceWhiteText.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_TEXT_WHITE, isChecked);
        });
        binding.forceWhiteTextContainer.setOnClickListener(v -> binding.enableForceWhiteText.toggle());

        // Hide in landscape
        binding.enableHideHeaderClockLandscape.setChecked(RPrefs.getBoolean(HEADER_CLOCK_LANDSCAPE_SWITCH, true));
        binding.enableHideHeaderClockLandscape.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_LANDSCAPE_SWITCH, isChecked);
        });
        binding.hideHeaderClockLandscapeContainer.setOnClickListener(v -> binding.enableHideHeaderClockLandscape.toggle());

        return view;
    }

    private ClockPreviewAdapter initHeaderClockStyles() {
        ArrayList<ClockModel> header_clock = new ArrayList<>();

        for (int i = 1; i <= 8; i++)
            header_clock.add(new ClockModel(HeaderClockStyles.initHeaderClockStyle(requireContext(), i)));

        return new ClockPreviewAdapter(requireContext(), header_clock, HEADER_CLOCK_SWITCH, HEADER_CLOCK_STYLE);
    }

    public void browseHeaderClockFont() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("font/*");
        startActivityIntent.launch(chooseFile);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onColorSelected(ColorSelectedEvent event) {
        if (event.dialogId() == 1) {
            colorHeaderClock = event.selectedColor();
            updateColorPreview();
            RPrefs.putInt(HEADER_CLOCK_COLOR_CODE, colorHeaderClock);
        }
    }

    private void updateColorPreview() {
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{colorHeaderClock, colorHeaderClock});
        gd.setCornerRadius(Iconify.getAppContextLocale().getResources().getDimension(R.dimen.preview_color_picker_radius) * Iconify.getAppContextLocale().getResources().getDisplayMetrics().density);
        binding.previewColorPickerClocktext.setBackground(gd);
    }

    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    if (path != null && copyToIconifyHiddenDir(path, HEADER_CLOCK_FONT_DIR)) {
                        binding.enableHeaderClockFont.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}