package com.drdisagree.iconify.ui.fragments;

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
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentXposedBackgroundChipBinding;
import com.drdisagree.iconify.ui.activities.HomePage;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.dialogs.RadioDialog;
import com.drdisagree.iconify.ui.events.ColorSelectedEvent;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;

public class XposedBackgroundChip extends BaseFragment implements RadioDialog.RadioDialogListener {

    private static int colorStatusbarClock, selectedClockColorOption = 0;
    private FragmentXposedBackgroundChipBinding binding;
    private RadioDialog rd_sb_clock_color_option;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXposedBackgroundChipBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_background_chip);

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
        rd_sb_clock_color_option = new RadioDialog(requireContext(), 0, RPrefs.getInt(STATUSBAR_CLOCK_COLOR_OPTION, 0));
        rd_sb_clock_color_option.setRadioDialogListener(this);
        binding.sbClockColor.setOnClickListener(v -> rd_sb_clock_color_option.show(R.string.statusbar_clock_color_title, R.array.statusbar_clock_color, binding.selectedSbClockColorOption));
        selectedClockColorOption = rd_sb_clock_color_option.getSelectedIndex();
        binding.selectedSbClockColorOption.setText(Arrays.asList(getResources().getStringArray(R.array.statusbar_clock_color)).get(selectedClockColorOption));
        binding.selectedSbClockColorOption.setText(getResources().getString(R.string.opt_selected) + ' ' + binding.selectedSbClockColorOption.getText().toString().replaceAll(getResources().getString(R.string.opt_selected) + ' ', ""));
        binding.sbClockColorPicker.setVisibility(selectedClockColorOption == 2 ? View.VISIBLE : View.GONE);
        colorStatusbarClock = RPrefs.getInt(STATUSBAR_CLOCK_COLOR_CODE, Color.WHITE);

        // Clock Color Picker
        binding.sbClockColorPicker.setOnClickListener(v -> ((HomePage) requireActivity()).showColorPickerDialog(1, colorStatusbarClock, true, true, true));
        updateColorPreview();

        // Status icons chip
        binding.enableStatusIconsChip.setChecked(RPrefs.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false));
        binding.enableStatusIconsChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(QSPANEL_STATUSICONSBG_SWITCH, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                OverlayUtil.enableOverlay("IconifyComponentIXCC.overlay");

                if (Build.VERSION.SDK_INT >= 33) {
                    SystemUtil.handleSystemUIRestart();
                } else {
                    SystemUtil.doubleToggleDarkMode();
                }
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

        return view;
    }

    // Function to add new item in list
    @SuppressLint("UseCompatLoadingForDrawables")
    private void addItemStatusBar(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(requireContext()).inflate(R.layout.view_status_bar_chip, binding.statusBarChipContainer, false);

            LinearLayout clock_container = list.findViewById(R.id.clock_container);
            clock_container.setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), (int) pack.get(i)[0]));

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
                title.setTextColor(getResources().getColor(R.color.colorAccent, Iconify.getAppContext().getTheme()));
            } else {
                title.setTextColor(getResources().getColor(R.color.textColorSecondary, Iconify.getAppContext().getTheme()));
            }
        }
    }

    // Function to add new item in list
    @SuppressLint("UseCompatLoadingForDrawables")
    private void addItemStatusIcons(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(requireContext()).inflate(R.layout.view_status_icons_chip, binding.statusIconsChipContainer, false);

            LinearLayout icon_container = list.findViewById(R.id.clock_container);
            icon_container.setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), (int) pack.get(i)[0]));

            TextView style_name = list.findViewById(R.id.style_name);
            style_name.setText(getResources().getString((int) pack.get(i)[1]));

            int finalI = i;
            list.setOnClickListener(v -> {
                RPrefs.putInt(CHIP_QSSTATUSICONS_STYLE, finalI);
                refreshBackgroundStatusIcons();
                if (RPrefs.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false) && Build.VERSION.SDK_INT < 33) {
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
                title.setTextColor(getResources().getColor(R.color.colorAccent, Iconify.getAppContext().getTheme()));
            } else {
                title.setTextColor(getResources().getColor(R.color.textColorSecondary, Iconify.getAppContext().getTheme()));
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

    @SuppressWarnings("unused")
    @Subscribe
    public void onColorSelected(ColorSelectedEvent event) {
        if (event.dialogId() == 1) {
            colorStatusbarClock = event.selectedColor();
            updateColorPreview();
            RPrefs.putInt(STATUSBAR_CLOCK_COLOR_CODE, colorStatusbarClock);
        }
    }

    private void updateColorPreview() {
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{colorStatusbarClock, colorStatusbarClock});
        gd.setCornerRadius(getResources().getDimension(R.dimen.preview_color_picker_radius) * getResources().getDisplayMetrics().density);
        binding.previewColorPickerClocktext.setBackground(gd);
    }

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

    @Override
    public void onDestroy() {
        rd_sb_clock_color_option.dismiss();
        super.onDestroy();
    }
}