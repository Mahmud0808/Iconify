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
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.ActivityXposedBackgroundChipBinding;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.ui.views.RadioDialog;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.ArrayList;
import java.util.Arrays;

public class XposedBackgroundChip extends BaseActivity implements RadioDialog.RadioDialogListener, ColorPickerDialogListener {

    private static int colorStatusbarClock, selectedClockColorOption = 0;
    private ActivityXposedBackgroundChipBinding binding;
    private ColorPickerDialog.Builder colorPickerDialogStatusbarClock;
    private RadioDialog rd_sb_clock_color_option;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXposedBackgroundChipBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.toolbar, R.string.activity_title_background_chip);

        // Statusbar clock Chip
        binding.enableClockBgChip.setChecked(RPrefs.getBoolean(STATUSBAR_CLOCKBG_SWITCH, false));
        binding.enableClockBgChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(STATUSBAR_CLOCKBG_SWITCH, isChecked);

            if (!isChecked) {
                new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
            }
        });
        binding.clockBgChip.setOnClickListener(v -> binding.enableClockBgChip.toggle());

        // Statusbar clock chip style
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
        rd_sb_clock_color_option = new RadioDialog(this, 0, RPrefs.getInt(STATUSBAR_CLOCK_COLOR_OPTION, 0));
        rd_sb_clock_color_option.setRadioDialogListener(this);
        binding.sbClockColor.setOnClickListener(v -> rd_sb_clock_color_option.show(R.string.statusbar_clock_color_title, R.array.statusbar_clock_color, binding.selectedSbClockColorOption));
        selectedClockColorOption = rd_sb_clock_color_option.getSelectedIndex();
        binding.selectedSbClockColorOption.setText(Arrays.asList(getResources().getStringArray(R.array.statusbar_clock_color)).get(selectedClockColorOption));
        binding.selectedSbClockColorOption.setText(getResources().getString(R.string.opt_selected) + ' ' + binding.selectedSbClockColorOption.getText().toString().replaceAll(getResources().getString(R.string.opt_selected) + ' ', ""));
        binding.sbClockColorPicker.setVisibility(selectedClockColorOption == 2 ? View.VISIBLE : View.GONE);
        colorStatusbarClock = RPrefs.getInt(STATUSBAR_CLOCK_COLOR_CODE, Color.WHITE);

        // Clock Color Picker
        colorPickerDialogStatusbarClock = ColorPickerDialog.newBuilder();
        colorPickerDialogStatusbarClock.setDialogStyle(R.style.ColorPicker).setColor(colorStatusbarClock).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(true).setShowColorShades(true);
        binding.sbClockColorPicker.setOnClickListener(v -> colorPickerDialogStatusbarClock.show(this));
        updateColorPreview();

        // Status icons chip
        if (Build.VERSION.SDK_INT >= 33) {
            binding.statusiconsChipContainer.setVisibility(View.GONE);
            RPrefs.putBoolean(QSPANEL_STATUSICONSBG_SWITCH, false);
        }

        binding.enableStatusIconsChip.setChecked(RPrefs.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false));
        binding.enableStatusIconsChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(QSPANEL_STATUSICONSBG_SWITCH, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                OverlayUtil.enableOverlay("IconifyComponentIXCC.overlay");
                SystemUtil.doubleToggleDarkMode();
            }, SWITCH_ANIMATION_DELAY);
        });
        binding.statusIconsChip.setOnClickListener(v -> binding.enableStatusIconsChip.toggle());

        // Status icons chip style
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
            View list = LayoutInflater.from(this).inflate(R.layout.view_status_bar_chip, binding.statusBarChipContainer, false);

            LinearLayout clock_container = list.findViewById(R.id.clock_container);
            clock_container.setBackground(ContextCompat.getDrawable(getApplicationContext(), (int) pack.get(i)[0]));

            TextView style_name = list.findViewById(R.id.style_name);
            style_name.setText(getResources().getString((int) pack.get(i)[1]));

            int finalI = i;
            list.setOnClickListener(v -> {
                RPrefs.putInt(CHIP_STATUSBAR_CLOCKBG_STYLE, finalI);
                refreshBackgroundStatusBar();
            });

            binding.statusBarChipContainer.addView(list);
        }
    }

    // Function to check for bg drawable changes
    @SuppressLint("SetTextI18n")
    private void refreshBackgroundStatusBar() {
        for (int i = 0; i < binding.statusBarChipContainer.getChildCount(); i++) {
            LinearLayout child = binding.statusBarChipContainer.getChildAt(i).findViewById(R.id.list_item_chip);
            TextView title = child.findViewById(R.id.style_name);
            if (i == RPrefs.getInt(CHIP_STATUSBAR_CLOCKBG_STYLE, 0)) {
                title.setTextColor(getResources().getColor(R.color.colorAccent, getTheme()));
            } else {
                title.setTextColor(getResources().getColor(R.color.textColorSecondary, getTheme()));
            }
        }
    }

    // Function to add new item in list
    @SuppressLint("UseCompatLoadingForDrawables")
    private void addItemStatusIcons(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_status_icons_chip, binding.statusIconsChipContainer, false);

            LinearLayout icon_container = list.findViewById(R.id.clock_container);
            icon_container.setBackground(ContextCompat.getDrawable(getApplicationContext(), (int) pack.get(i)[0]));

            TextView style_name = list.findViewById(R.id.style_name);
            style_name.setText(getResources().getString((int) pack.get(i)[1]));

            int finalI = i;
            list.setOnClickListener(v -> {
                RPrefs.putInt(CHIP_QSSTATUSICONS_STYLE, finalI);
                refreshBackgroundStatusIcons();
                if (RPrefs.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false)) {
                    new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
                }
            });

            binding.statusIconsChipContainer.addView(list);
        }
    }

    // Function to check for bg drawable changes
    @SuppressLint("SetTextI18n")
    private void refreshBackgroundStatusIcons() {
        for (int i = 0; i < binding.statusIconsChipContainer.getChildCount(); i++) {
            LinearLayout child = binding.statusIconsChipContainer.getChildAt(i).findViewById(R.id.list_item_chip);
            TextView title = child.findViewById(R.id.style_name);
            if (i == RPrefs.getInt(CHIP_QSSTATUSICONS_STYLE, 0)) {
                title.setTextColor(getResources().getColor(R.color.colorAccent, getTheme()));
            } else {
                title.setTextColor(getResources().getColor(R.color.textColorSecondary, getTheme()));
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onItemSelected(int dialogId, int selectedIndex) {
        if (dialogId == 0) {
            selectedClockColorOption = selectedIndex;
            RPrefs.putInt(STATUSBAR_CLOCK_COLOR_OPTION, selectedIndex);
            binding.sbClockColorPicker.setVisibility(selectedClockColorOption == 2 ? View.VISIBLE : View.GONE);
            binding.selectedSbClockColorOption.setText(getResources().getString(R.string.opt_selected) + ' ' + binding.selectedSbClockColorOption.getText().toString().replaceAll(getResources().getString(R.string.opt_selected) + ' ', ""));
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
        GradientDrawable gd;

        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{colorStatusbarClock, colorStatusbarClock});
        gd.setCornerRadius(getResources().getDimension(R.dimen.preview_color_picker_radius) * getResources().getDisplayMetrics().density);
        binding.previewColorPickerClocktext.setBackground(gd);
    }
}