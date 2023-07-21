package com.drdisagree.iconify.ui.activities;

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
import static com.drdisagree.iconify.ui.utils.ViewBindingHelpers.disableNestedScrolling;
import static com.drdisagree.iconify.utils.FileUtil.copyToIconifyHiddenDir;
import static com.drdisagree.iconify.utils.FileUtil.getRealPath;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.viewpager2.widget.ViewPager2;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.ActivityXposedHeaderClockBinding;
import com.drdisagree.iconify.ui.adapters.ClockPreviewAdapter;
import com.drdisagree.iconify.ui.models.ClockModel;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.ui.views.HeaderClockStyles;
import com.drdisagree.iconify.utils.SystemUtil;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class XposedHeaderClock extends BaseActivity implements ColorPickerDialogListener {

    private static int colorHeaderClock;
    private ActivityXposedHeaderClockBinding binding;
    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    if (path != null && copyToIconifyHiddenDir(path, HEADER_CLOCK_FONT_DIR)) {
                        binding.enableHeaderClockFont.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });
    private ColorPickerDialog.Builder colorPickerDialogHeaderClock;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXposedHeaderClockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.collapsingToolbar, binding.header.toolbar, R.string.activity_title_header_clock);

        // Custom header clock
        binding.enableHeaderClock.setChecked(RPrefs.getBoolean(HEADER_CLOCK_SWITCH, false));
        binding.enableHeaderClock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_SWITCH, isChecked);
            if (!isChecked) RPrefs.putInt(HEADER_CLOCK_STYLE, 1);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // Header clock style
        binding.headerClockPreview.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        ClockPreviewAdapter adapter = initHeaderClockStyles();
        binding.headerClockPreview.setAdapter(adapter);
        disableNestedScrolling(binding.headerClockPreview);

        binding.headerClockPreview.setCurrentItem(RPrefs.getInt(HEADER_CLOCK_STYLE, 1) - 1);
        binding.headerClockPreviewIndicator.setViewPager(binding.headerClockPreview);
        binding.headerClockPreviewIndicator.tintIndicator(getResources().getColor(R.color.textColorSecondary, getTheme()));

        // Lockscreen clock font picker
        binding.pickHeaderClockFont.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                SystemUtil.getStoragePermission(this);
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

        binding.headerClockColorPicker.setVisibility(RPrefs.getBoolean(HEADER_CLOCK_COLOR_SWITCH, false) ? View.VISIBLE : View.GONE);

        // Clock color picker
        colorPickerDialogHeaderClock = ColorPickerDialog.newBuilder();
        colorHeaderClock = RPrefs.getInt(HEADER_CLOCK_COLOR_CODE, Color.WHITE);
        colorPickerDialogHeaderClock.setDialogStyle(R.style.ColorPicker).setColor(colorHeaderClock).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(true).setShowColorShades(true);
        binding.headerClockColorPicker.setOnClickListener(v -> colorPickerDialogHeaderClock.show(this));
        updateColorPreview();

        // Text Scaling
        final int[] textScaling = {RPrefs.getInt(HEADER_CLOCK_FONT_TEXT_SCALING, 10)};
        binding.headerClockTextscalingOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + new DecimalFormat("#.#").format(textScaling[0] / 10.0) + "x");
        binding.headerClockTextscalingSeekbar.setProgress(RPrefs.getInt(HEADER_CLOCK_FONT_TEXT_SCALING, 10));
        binding.headerClockTextscalingSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textScaling[0] = progress;
                binding.headerClockTextscalingOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + new DecimalFormat("#.#").format(progress / 10.0) + "x");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(HEADER_CLOCK_FONT_TEXT_SCALING, textScaling[0]);
            }
        });

        // Header clock side margin
        binding.headerClockSideMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0) + "dp");
        binding.headerClockSideMarginSeekbar.setProgress(RPrefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0));
        final int[] sideMargin = {RPrefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0)};
        binding.headerClockSideMarginSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sideMargin[0] = progress;
                binding.headerClockSideMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(HEADER_CLOCK_SIDEMARGIN, sideMargin[0]);
            }
        });

        // Header clock top margin
        binding.headerClockTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(HEADER_CLOCK_TOPMARGIN, 8) + "dp");
        binding.headerClockTopMarginSeekbar.setProgress(RPrefs.getInt(HEADER_CLOCK_TOPMARGIN, 8));
        final int[] topMargin = {RPrefs.getInt(HEADER_CLOCK_TOPMARGIN, 8)};
        binding.headerClockTopMarginSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                topMargin[0] = progress;
                binding.headerClockTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(HEADER_CLOCK_TOPMARGIN, topMargin[0]);
            }
        });

        // Center clock
        binding.enableCenterClock.setChecked(RPrefs.getBoolean(HEADER_CLOCK_CENTERED, false));
        binding.enableCenterClock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_CENTERED, isChecked);
        });

        // Force white text
        binding.enableForceWhiteText.setChecked(RPrefs.getBoolean(HEADER_CLOCK_TEXT_WHITE, false));
        binding.enableForceWhiteText.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_TEXT_WHITE, isChecked);
        });

        // Hide in landscape
        binding.enableHideHeaderClockLandscape.setChecked(RPrefs.getBoolean(HEADER_CLOCK_LANDSCAPE_SWITCH, true));
        binding.enableHideHeaderClockLandscape.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_LANDSCAPE_SWITCH, isChecked);
        });
    }

    private ClockPreviewAdapter initHeaderClockStyles() {
        ArrayList<ClockModel> header_clock = new ArrayList<>();

        for (int i = 1; i <= 8; i++)
            header_clock.add(new ClockModel(HeaderClockStyles.initHeaderClockStyle(this, i)));

        return new ClockPreviewAdapter(this, header_clock, HEADER_CLOCK_SWITCH, HEADER_CLOCK_STYLE);
    }

    public void browseHeaderClockFont() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("font/*");
        startActivityIntent.launch(chooseFile);
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        if (dialogId == 1) {
            colorHeaderClock = color;
            updateColorPreview();
            RPrefs.putInt(HEADER_CLOCK_COLOR_CODE, colorHeaderClock);
            colorPickerDialogHeaderClock.setDialogStyle(R.style.ColorPicker).setColor(colorHeaderClock).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(true).setShowColorShades(true);
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    private void updateColorPreview() {
        GradientDrawable gd;

        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{colorHeaderClock, colorHeaderClock});
        gd.setCornerRadius(getResources().getDimension(R.dimen.preview_color_picker_radius) * getResources().getDisplayMetrics().density);
        binding.previewColorPickerClocktext.setBackground(gd);
    }
}