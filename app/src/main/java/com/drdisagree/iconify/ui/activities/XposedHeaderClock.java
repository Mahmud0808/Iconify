package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_CENTERED;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_TEXT_SCALING;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_LANDSCAPE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SIDEMARGIN;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_STYLE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_TEXT_WHITE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_SWITCH;
import static com.drdisagree.iconify.ui.utils.ViewBindingHelpers.disableNestedScrolling;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.viewpager2.widget.ViewPager2;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.ui.adapters.ClockPreviewAdapter;
import com.drdisagree.iconify.ui.models.ClockModel;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.ui.views.HeaderClockStyles;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator3;

public class XposedHeaderClock extends BaseActivity implements ColorPickerDialogListener {

    private static final int PICKFILE_RESULT_CODE = 100;
    private Button enable_header_clock_font;
    private static int colorHeaderClock;
    ColorPickerDialog.Builder colorPickerDialogHeaderClock;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_header_clock);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_header_clock);

        // Custom header clock
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_header_clock = findViewById(R.id.enable_header_clock);
        enable_header_clock.setChecked(RPrefs.getBoolean(HEADER_CLOCK_SWITCH, false));
        enable_header_clock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_SWITCH, isChecked);
            if (!isChecked) RPrefs.putInt(HEADER_CLOCK_STYLE, 1);
            new Handler().postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // Header clock style
        ViewPager2 container = findViewById(R.id.header_clock_preview);
        container.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        ClockPreviewAdapter adapter = initHeaderClockStyles();
        container.setAdapter(adapter);
        disableNestedScrolling(container);

        CircleIndicator3 indicator = findViewById(R.id.header_clock_preview_indicator);
        container.setCurrentItem(RPrefs.getInt(HEADER_CLOCK_STYLE, 1) - 1);
        indicator.setViewPager(container);
        indicator.tintIndicator(getResources().getColor(R.color.textColorSecondary));

        // Lockscreen clock font picker
        Button pick_header_clock_font = findViewById(R.id.pick_header_clock_font);
        pick_header_clock_font.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                SystemUtil.getStoragePermission(this);
            } else {
                browseHeaderClockFont();
            }
        });

        Button disable_header_clock_font = findViewById(R.id.disable_header_clock_font);
        disable_header_clock_font.setVisibility(RPrefs.getBoolean(HEADER_CLOCK_FONT_SWITCH, false) ? View.VISIBLE : View.GONE);

        enable_header_clock_font = findViewById(R.id.enable_header_clock_font);
        enable_header_clock_font.setOnClickListener(v -> {
            RPrefs.putBoolean(HEADER_CLOCK_FONT_SWITCH, false);
            RPrefs.putBoolean(HEADER_CLOCK_FONT_SWITCH, true);
            enable_header_clock_font.setVisibility(View.GONE);
            disable_header_clock_font.setVisibility(View.VISIBLE);
        });

        disable_header_clock_font.setOnClickListener(v -> {
            RPrefs.putBoolean(HEADER_CLOCK_FONT_SWITCH, false);
            disable_header_clock_font.setVisibility(View.GONE);
        });

        // Custom clock color
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_header_clock_custom_color = findViewById(R.id.enable_header_clock_custom_color);
        enable_header_clock_custom_color.setChecked(RPrefs.getBoolean(HEADER_CLOCK_COLOR_SWITCH, false));
        enable_header_clock_custom_color.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_COLOR_SWITCH, isChecked);
            findViewById(R.id.header_clock_color_picker).setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        findViewById(R.id.header_clock_color_picker).setVisibility(RPrefs.getBoolean(HEADER_CLOCK_COLOR_SWITCH, false) ? View.VISIBLE : View.GONE);

        // Clock color picker
        colorPickerDialogHeaderClock = ColorPickerDialog.newBuilder();
        colorHeaderClock = RPrefs.getInt(HEADER_CLOCK_COLOR_CODE, Color.WHITE);
        colorPickerDialogHeaderClock.setDialogStyle(R.style.ColorPicker).setColor(colorHeaderClock).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(true).setShowColorShades(true);
        LinearLayout header_clock_color_picker = findViewById(R.id.header_clock_color_picker);
        header_clock_color_picker.setOnClickListener(v -> colorPickerDialogHeaderClock.show(this));
        updateColorPreview();

        // Text Scaling
        SeekBar header_clock_textscaling_seekbar = findViewById(R.id.header_clock_textscaling_seekbar);
        TextView header_clock_textscaling_output = findViewById(R.id.header_clock_textscaling_output);
        final int[] textScaling = {RPrefs.getInt(HEADER_CLOCK_FONT_TEXT_SCALING, 10)};
        header_clock_textscaling_output.setText(getResources().getString(R.string.opt_selected) + ' ' + new DecimalFormat("#.#").format(textScaling[0] / 10.0) + "x");
        header_clock_textscaling_seekbar.setProgress(RPrefs.getInt(HEADER_CLOCK_FONT_TEXT_SCALING, 10));
        header_clock_textscaling_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textScaling[0] = progress;
                header_clock_textscaling_output.setText(getResources().getString(R.string.opt_selected) + ' ' + new DecimalFormat("#.#").format(progress / 10.0) + "x");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(HEADER_CLOCK_FONT_TEXT_SCALING, textScaling[0]);
            }
        });

        // Header clock side margin
        SeekBar header_clock_side_margin_seekbar = findViewById(R.id.header_clock_side_margin_seekbar);
        TextView header_clock_side_margin_output = findViewById(R.id.header_clock_side_margin_output);
        header_clock_side_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0) + "dp");
        header_clock_side_margin_seekbar.setProgress(RPrefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0));
        final int[] sideMargin = {RPrefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0)};
        header_clock_side_margin_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sideMargin[0] = progress;
                header_clock_side_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(HEADER_CLOCK_SIDEMARGIN, sideMargin[0]);
            }
        });

        // Header clock top margin
        SeekBar header_clock_top_margin_seekbar = findViewById(R.id.header_clock_top_margin_seekbar);
        TextView header_clock_top_margin_output = findViewById(R.id.header_clock_top_margin_output);
        header_clock_top_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(HEADER_CLOCK_TOPMARGIN, 8) + "dp");
        header_clock_top_margin_seekbar.setProgress(RPrefs.getInt(HEADER_CLOCK_TOPMARGIN, 8));
        final int[] topMargin = {RPrefs.getInt(HEADER_CLOCK_TOPMARGIN, 8)};
        header_clock_top_margin_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                topMargin[0] = progress;
                header_clock_top_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(HEADER_CLOCK_TOPMARGIN, topMargin[0]);
            }
        });

        // Center clock
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_center_clock = findViewById(R.id.enable_center_clock);
        enable_center_clock.setChecked(RPrefs.getBoolean(HEADER_CLOCK_CENTERED, false));
        enable_center_clock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_CENTERED, isChecked);
        });

        // Force white text
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_force_white_text = findViewById(R.id.enable_force_white_text);
        enable_force_white_text.setChecked(RPrefs.getBoolean(HEADER_CLOCK_TEXT_WHITE, false));
        enable_force_white_text.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_TEXT_WHITE, isChecked);
        });

        // Hide in landscape
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_hide_header_clock_landscape = findViewById(R.id.enable_hide_header_clock_landscape);
        enable_hide_header_clock_landscape.setChecked(RPrefs.getBoolean(HEADER_CLOCK_LANDSCAPE_SWITCH, true));
        enable_hide_header_clock_landscape.setOnCheckedChangeListener((buttonView, isChecked) -> {
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
        chooseFile.setType("font/ttf");
        startActivityForResult(Intent.createChooser(chooseFile, getResources().getString(R.string.choose_clock_font)), PICKFILE_RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FileUtil.copyToIconifyHiddenDir(this, requestCode, resultCode, data, PICKFILE_RESULT_CODE, "headerclock_font.ttf", enable_header_clock_font);
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
        View preview_color_picker_clocktext = findViewById(R.id.preview_color_picker_clocktext);
        GradientDrawable gd;

        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{colorHeaderClock, colorHeaderClock});
        gd.setCornerRadius(getResources().getDimension(com.intuit.sdp.R.dimen._24sdp) * getResources().getDisplayMetrics().density);
        preview_color_picker_clocktext.setBackgroundDrawable(gd);
    }
}