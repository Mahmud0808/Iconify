package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
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
import com.drdisagree.iconify.databinding.FragmentXposedLockscreenClockBinding;
import com.drdisagree.iconify.ui.activities.HomePage;
import com.drdisagree.iconify.ui.adapters.ClockPreviewAdapter;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.events.ColorSelectedEvent;
import com.drdisagree.iconify.ui.models.ClockModel;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.ui.views.LockscreenClockStyles;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.slider.Slider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class XposedLockscreenClock extends BaseFragment {

    private FragmentXposedLockscreenClockBinding binding;
    private static int colorLockscreenClock;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXposedLockscreenClockBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_lockscreen_clock);

        // Enable lockscreen clock
        binding.enableLockscreenClock.setChecked(RPrefs.getBoolean(LSCLOCK_SWITCH, false));
        binding.enableLockscreenClock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LSCLOCK_SWITCH, isChecked);
            if (!isChecked) RPrefs.putInt(LSCLOCK_STYLE, 0);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });
        binding.enableLockscreenClockContainer.setOnClickListener(v -> binding.enableLockscreenClock.toggle());

        // Lockscreen clock style
        binding.lockscreenClockPreview.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        ClockPreviewAdapter adapter = initLockscreenClockStyles();
        binding.lockscreenClockPreview.setAdapter(adapter);
        ViewHelper.disableNestedScrolling(binding.lockscreenClockPreview);

        binding.lockscreenClockPreview.setCurrentItem(RPrefs.getInt(LSCLOCK_STYLE, 0));
        binding.lockscreenClockPreviewIndicator.setViewPager(binding.lockscreenClockPreview);
        binding.lockscreenClockPreviewIndicator.tintIndicator(getResources().getColor(R.color.textColorSecondary, requireActivity().getTheme()));

        // Lockscreen clock font picker
        binding.pickLsclockFont.setOnClickListener(v -> {
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                browseLSClockFont();
            }
        });

        binding.disableLsclockFont.setVisibility(RPrefs.getBoolean(LSCLOCK_FONT_SWITCH, false) ? View.VISIBLE : View.GONE);

        binding.enableLsclockFont.setOnClickListener(v -> {
            RPrefs.putBoolean(LSCLOCK_FONT_SWITCH, false);
            RPrefs.putBoolean(LSCLOCK_FONT_SWITCH, true);
            binding.enableLsclockFont.setVisibility(View.GONE);
            binding.disableLsclockFont.setVisibility(View.VISIBLE);
        });

        binding.disableLsclockFont.setOnClickListener(v -> {
            RPrefs.putBoolean(LSCLOCK_FONT_SWITCH, false);
            binding.disableLsclockFont.setVisibility(View.GONE);
        });

        // Custom clock color
        binding.enableLsClockCustomColor.setChecked(RPrefs.getBoolean(LSCLOCK_COLOR_SWITCH, false));
        binding.enableLsClockCustomColor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LSCLOCK_COLOR_SWITCH, isChecked);
            binding.lsClockColorPicker.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        binding.lsClockCustomColorContainer.setOnClickListener(v -> binding.enableLsClockCustomColor.toggle());

        binding.lsClockColorPicker.setVisibility(RPrefs.getBoolean(LSCLOCK_COLOR_SWITCH, false) ? View.VISIBLE : View.GONE);

        // Clock color picker
        colorLockscreenClock = RPrefs.getInt(LSCLOCK_COLOR_CODE, Color.WHITE);
        binding.lsClockColorPicker.setOnClickListener(v -> ((HomePage) requireActivity()).showColorPickerDialog(1, colorLockscreenClock, true, true, true));
        updateColorPreview();

        // Line height
        final int[] lineHeight = {RPrefs.getInt(LSCLOCK_FONT_LINEHEIGHT, 0)};
        binding.lsclockLineheightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + lineHeight[0] + "dp");
        binding.lsclockLineheightSeekbar.setValue(lineHeight[0]);
        binding.lsclockLineheightSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                lineHeight[0] = (int) slider.getValue();
                binding.lsclockLineheightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + lineHeight[0] + "dp");
                RPrefs.putInt(LSCLOCK_FONT_LINEHEIGHT, lineHeight[0]);
            }
        });

        // Text Scaling
        final int[] textScaling = {RPrefs.getInt(LSCLOCK_FONT_TEXT_SCALING, 10)};
        binding.lsclockTextscalingOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + new DecimalFormat("#.#").format(textScaling[0] / 10.0) + "x");
        binding.lsclockTextscalingSeekbar.setValue(RPrefs.getInt(LSCLOCK_FONT_TEXT_SCALING, 10));
        binding.lsclockTextscalingSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                textScaling[0] = (int) slider.getValue();
                binding.lsclockTextscalingOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + new DecimalFormat("#.#").format(textScaling[0] / 10.0) + "x");
                RPrefs.putInt(LSCLOCK_FONT_TEXT_SCALING, textScaling[0]);
            }
        });

        // Top margin
        final int[] topMargin = {RPrefs.getInt(LSCLOCK_TOPMARGIN, 100)};
        binding.lsclockTopmarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + topMargin[0] + "dp");
        binding.lsclockTopmarginSeekbar.setValue(topMargin[0]);
        binding.lsclockTopmarginSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                topMargin[0] = (int) slider.getValue();
                binding.lsclockTopmarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + topMargin[0] + "dp");
                RPrefs.putInt(LSCLOCK_TOPMARGIN, topMargin[0]);
            }
        });

        // Bottom margin
        final int[] bottomMargin = {RPrefs.getInt(LSCLOCK_BOTTOMMARGIN, 40)};
        binding.lsclockBottommarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + bottomMargin[0] + "dp");
        binding.lsclockBottommarginSeekbar.setValue(bottomMargin[0]);
        binding.lsclockBottommarginSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                bottomMargin[0] = (int) slider.getValue();
                binding.lsclockBottommarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + bottomMargin[0] + "dp");
                RPrefs.putInt(LSCLOCK_BOTTOMMARGIN, bottomMargin[0]);
            }
        });

        // Force white text
        binding.enableForceWhiteText.setChecked(RPrefs.getBoolean(LSCLOCK_TEXT_WHITE, false));
        binding.enableForceWhiteText.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LSCLOCK_TEXT_WHITE, isChecked);
        });
        binding.forceWhiteTextContainer.setOnClickListener(v -> binding.enableForceWhiteText.toggle());

        return view;
    }

    private ClockPreviewAdapter initLockscreenClockStyles() {
        ArrayList<ClockModel> ls_clock = new ArrayList<>();

        for (int i = 0; i <= 8; i++)
            ls_clock.add(new ClockModel(LockscreenClockStyles.initLockscreenClockStyle(requireContext(), i)));

        return new ClockPreviewAdapter(requireContext(), ls_clock, LSCLOCK_SWITCH, LSCLOCK_STYLE);
    }

    public void browseLSClockFont() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("font/*");
        startActivityIntent.launch(chooseFile);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onColorSelected(ColorSelectedEvent event) {
        if (event.dialogId() == 1) {
            colorLockscreenClock = event.selectedColor();
            updateColorPreview();
            RPrefs.putInt(LSCLOCK_COLOR_CODE, colorLockscreenClock);
        }
    }

    private void updateColorPreview() {
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{colorLockscreenClock, colorLockscreenClock});
        gd.setCornerRadius(Iconify.getAppContextLocale().getResources().getDimension(R.dimen.preview_color_picker_radius) * Iconify.getAppContextLocale().getResources().getDisplayMetrics().density);
        binding.previewColorPickerClocktext.setBackground(gd);
    }

    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    if (path != null && copyToIconifyHiddenDir(path, LSCLOCK_FONT_DIR)) {
                        binding.enableLsclockFont.setVisibility(View.VISIBLE);
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