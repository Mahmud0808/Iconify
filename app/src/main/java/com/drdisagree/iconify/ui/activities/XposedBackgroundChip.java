package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.CHIP_QSSTATUSICONS_STYLE;
import static com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCKBG_STYLE;
import static com.drdisagree.iconify.common.Preferences.QSPANEL_STATUSICONSBG_SWITCH;
import static com.drdisagree.iconify.common.Preferences.STATUSBAR_CLOCKBG_SWITCH;
import static com.drdisagree.iconify.common.Preferences.STATUSBAR_CLOCK_COLOR_CODE;
import static com.drdisagree.iconify.common.Preferences.STATUSBAR_CLOCK_COLOR_OPTION;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.ui.views.RadioDialog;
import com.drdisagree.iconify.utils.HelperUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.flexbox.FlexboxLayout;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.ArrayList;
import java.util.Arrays;

public class XposedBackgroundChip extends BaseActivity implements RadioDialog.RadioDialogListener, ColorPickerDialogListener {

    private static int colorStatusbarClock;
    private static int selectedClockColorOption = 0;
    TextView selected_sb_clock_color_option;
    RadioDialog rd_sb_clock_color_option;
    ColorPickerDialog.Builder colorPickerDialogStatusbarClock;
    private FlexboxLayout containerStatusBar, containerStatusIcons;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_background_chip);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_background_chip);

        // Statusbar clock Chip
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_clock_bg_chip = findViewById(R.id.enable_clock_bg_chip);
        enable_clock_bg_chip.setChecked(RPrefs.getBoolean(STATUSBAR_CLOCKBG_SWITCH, false));
        enable_clock_bg_chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(STATUSBAR_CLOCKBG_SWITCH, isChecked);

            if (!isChecked) {
                new Handler().postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
            }
        });

        // Statusbar clock chip style
        containerStatusBar = findViewById(R.id.status_bar_chip_container);
        ArrayList<Object[]> status_bar_chip_style = new ArrayList<>();

        status_bar_chip_style.add(new Object[]{R.drawable.chip_status_bar_1, R.string.style_1});
        status_bar_chip_style.add(new Object[]{R.drawable.chip_status_bar_2, R.string.style_2});
        status_bar_chip_style.add(new Object[]{R.drawable.chip_status_bar_3, R.string.style_3});
        status_bar_chip_style.add(new Object[]{R.drawable.chip_status_bar_4, R.string.style_4});
        status_bar_chip_style.add(new Object[]{R.drawable.chip_status_bar_5, R.string.style_5});
        status_bar_chip_style.add(new Object[]{R.drawable.chip_status_bar_6, R.string.style_6});
        status_bar_chip_style.add(new Object[]{R.drawable.chip_status_bar_7, R.string.style_7});

        addItemStatusBar(status_bar_chip_style);

        refreshBackgroundStatusBar();

        // Statusbar Clock Color
        LinearLayout sb_clock_color = findViewById(R.id.sb_clock_color);
        selected_sb_clock_color_option = findViewById(R.id.selected_sb_clock_color_option);
        rd_sb_clock_color_option = new RadioDialog(this, 0, RPrefs.getInt(STATUSBAR_CLOCK_COLOR_OPTION, 0));
        rd_sb_clock_color_option.setRadioDialogListener(this);
        sb_clock_color.setOnClickListener(v -> rd_sb_clock_color_option.show(R.string.battery_style_title, R.array.statusbar_clock_color, selected_sb_clock_color_option));
        selectedClockColorOption = rd_sb_clock_color_option.getSelectedIndex();
        selected_sb_clock_color_option.setText(Arrays.asList(getResources().getStringArray(R.array.statusbar_clock_color)).get(selectedClockColorOption));
        selected_sb_clock_color_option.setText(getResources().getString(R.string.opt_selected) + ' ' + selected_sb_clock_color_option.getText().toString().replaceAll(getResources().getString(R.string.opt_selected) + ' ', ""));
        findViewById(R.id.sb_clock_color_picker).setVisibility(selectedClockColorOption == 2 ? View.VISIBLE : View.GONE);
        colorStatusbarClock = RPrefs.getInt(STATUSBAR_CLOCK_COLOR_CODE, Color.WHITE);

        // Clock Color Picker
        colorPickerDialogStatusbarClock = ColorPickerDialog.newBuilder();
        colorPickerDialogStatusbarClock.setDialogStyle(R.style.ColorPicker).setColor(colorStatusbarClock).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(true).setShowColorShades(true);
        LinearLayout sb_clock_color_picker = findViewById(R.id.sb_clock_color_picker);
        sb_clock_color_picker.setOnClickListener(v -> colorPickerDialogStatusbarClock.show(this));
        updateColorPreview();

        // Status icons chip
        if (Build.VERSION.SDK_INT >= 33) {
            findViewById(R.id.statusicons_chip_container).setVisibility(View.GONE);
            RPrefs.putBoolean(QSPANEL_STATUSICONSBG_SWITCH, false);
        }

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_status_icons_chip = findViewById(R.id.enable_status_icons_chip);
        enable_status_icons_chip.setChecked(RPrefs.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false));
        enable_status_icons_chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(QSPANEL_STATUSICONSBG_SWITCH, isChecked);
            new Handler().postDelayed(() -> {
                OverlayUtil.enableOverlay("IconifyComponentIXCC.overlay");
                HelperUtil.forceApply();
            }, SWITCH_ANIMATION_DELAY);
        });

        // Status icons chip style
        containerStatusIcons = findViewById(R.id.status_icons_chip_container);
        ArrayList<Object[]> status_icons_chip_style = new ArrayList<>();

        status_icons_chip_style.add(new Object[]{R.drawable.chip_status_icons_1, R.string.style_1});
        status_icons_chip_style.add(new Object[]{R.drawable.chip_status_icons_2, R.string.style_2});
        status_icons_chip_style.add(new Object[]{R.drawable.chip_status_icons_3, R.string.style_3});
        status_icons_chip_style.add(new Object[]{R.drawable.chip_status_icons_4, R.string.style_4});
        status_icons_chip_style.add(new Object[]{R.drawable.chip_status_icons_5, R.string.style_5});
        status_icons_chip_style.add(new Object[]{R.drawable.chip_status_icons_6, R.string.style_6});

        addItemStatusIcons(status_icons_chip_style);

        refreshBackgroundStatusIcons();
    }

    // Function to add new item in list
    @SuppressLint("UseCompatLoadingForDrawables")
    private void addItemStatusBar(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_status_bar_chip, containerStatusBar, false);

            LinearLayout clock_container = list.findViewById(R.id.clock_container);
            clock_container.setBackground(getResources().getDrawable((int) pack.get(i)[0]));

            TextView style_name = list.findViewById(R.id.style_name);
            style_name.setText(getResources().getString((int) pack.get(i)[1]));

            int finalI = i;
            list.setOnClickListener(v -> {
                RPrefs.putInt(CHIP_STATUSBAR_CLOCKBG_STYLE, finalI);
                refreshBackgroundStatusBar();
            });

            containerStatusBar.addView(list);
        }
    }

    // Function to check for bg drawable changes
    @SuppressLint("SetTextI18n")
    private void refreshBackgroundStatusBar() {
        for (int i = 0; i < containerStatusBar.getChildCount(); i++) {
            LinearLayout child = containerStatusBar.getChildAt(i).findViewById(R.id.list_item_chip);
            TextView title = child.findViewById(R.id.style_name);
            if (i == RPrefs.getInt(CHIP_STATUSBAR_CLOCKBG_STYLE, 0)) {
                title.setTextColor(getResources().getColor(R.color.colorSuccess));
            } else {
                title.setTextColor(getResources().getColor(R.color.textColorSecondary));
            }
        }
    }

    // Function to add new item in list
    @SuppressLint("UseCompatLoadingForDrawables")
    private void addItemStatusIcons(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_status_icons_chip, containerStatusIcons, false);

            LinearLayout icon_container = list.findViewById(R.id.clock_container);
            icon_container.setBackground(getResources().getDrawable((int) pack.get(i)[0]));

            TextView style_name = list.findViewById(R.id.style_name);
            style_name.setText(getResources().getString((int) pack.get(i)[1]));

            int finalI = i;
            list.setOnClickListener(v -> {
                RPrefs.putInt(CHIP_QSSTATUSICONS_STYLE, finalI);
                refreshBackgroundStatusIcons();
                if (RPrefs.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false)) {
                    new Handler().postDelayed(HelperUtil::forceApply, SWITCH_ANIMATION_DELAY);
                }
            });

            containerStatusIcons.addView(list);
        }
    }

    // Function to check for bg drawable changes
    @SuppressLint("SetTextI18n")
    private void refreshBackgroundStatusIcons() {
        for (int i = 0; i < containerStatusIcons.getChildCount(); i++) {
            LinearLayout child = containerStatusIcons.getChildAt(i).findViewById(R.id.list_item_chip);
            TextView title = child.findViewById(R.id.style_name);
            if (i == RPrefs.getInt(CHIP_QSSTATUSICONS_STYLE, 0)) {
                title.setTextColor(getResources().getColor(R.color.colorSuccess));
            } else {
                title.setTextColor(getResources().getColor(R.color.textColorSecondary));
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onItemSelected(int dialogId, int selectedIndex) {
        if (dialogId == 0) {
            boolean needSysuiRestart = selectedClockColorOption == 2 && selectedIndex != 2;

            selectedClockColorOption = selectedIndex;
            RPrefs.putInt(STATUSBAR_CLOCK_COLOR_OPTION, selectedIndex);
            findViewById(R.id.sb_clock_color_picker).setVisibility(selectedClockColorOption == 2 ? View.VISIBLE : View.GONE);
            selected_sb_clock_color_option.setText(getResources().getString(R.string.opt_selected) + ' ' + selected_sb_clock_color_option.getText().toString().replaceAll(getResources().getString(R.string.opt_selected) + ' ', ""));

            if (needSysuiRestart) {
                new Handler().postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
            }
        }
    }

    @Override
    public void onDestroy() {
        rd_sb_clock_color_option.dismiss();
        super.onDestroy();
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        if (dialogId == 1) {
            colorStatusbarClock = color;
            updateColorPreview();
            RPrefs.putInt(STATUSBAR_CLOCK_COLOR_CODE, colorStatusbarClock);
            colorPickerDialogStatusbarClock.setDialogStyle(R.style.ColorPicker).setColor(colorStatusbarClock).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(true).setShowColorShades(true);
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    private void updateColorPreview() {
        View preview_color_picker_clocktext = findViewById(R.id.preview_color_picker_clocktext);
        GradientDrawable gd;

        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{colorStatusbarClock, colorStatusbarClock});
        gd.setCornerRadius(getResources().getDimension(com.intuit.sdp.R.dimen._24sdp) * getResources().getDisplayMetrics().density);
        preview_color_picker_clocktext.setBackgroundDrawable(gd);
    }
}