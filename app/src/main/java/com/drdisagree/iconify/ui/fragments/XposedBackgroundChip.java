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
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;

import java.util.ArrayList;

public class XposedBackgroundChip extends BaseFragment {

    private FragmentXposedBackgroundChipBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXposedBackgroundChipBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_background_chip);

        // Statusbar clock Chip
        binding.clockBgChip.setSwitchChecked(RPrefs.getBoolean(STATUSBAR_CLOCKBG_SWITCH, false));
        binding.clockBgChip.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(STATUSBAR_CLOCKBG_SWITCH, isChecked);
            binding.clockTextColor.setEnabled(isChecked);
            binding.clockTextColorPicker.setEnabled(isChecked);

            if (!isChecked) {
                new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
            }
        });

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
        binding.clockTextColor.setEnabled(RPrefs.getBoolean(STATUSBAR_CLOCKBG_SWITCH, false));
        binding.clockTextColor.setSelectedIndex(RPrefs.getInt(STATUSBAR_CLOCK_COLOR_OPTION, 0));
        binding.clockTextColor.setOnItemSelectedListener(index -> {
            RPrefs.putInt(STATUSBAR_CLOCK_COLOR_OPTION, index);
            binding.clockTextColorPicker.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        });

        // Clock Color Picker
        binding.clockTextColorPicker.setEnabled(RPrefs.getBoolean(STATUSBAR_CLOCKBG_SWITCH, false));
        binding.clockTextColorPicker.setVisibility(
                RPrefs.getInt(STATUSBAR_CLOCK_COLOR_OPTION, 0) == 2 ?
                        View.VISIBLE :
                        View.GONE
        );
        binding.clockTextColorPicker.setColorPickerListener(
                requireActivity(),
                RPrefs.getInt(STATUSBAR_CLOCK_COLOR_CODE, Color.WHITE),
                true,
                true,
                true
        );
        binding.clockTextColorPicker.setOnColorSelectedListener(
                color -> {
                    binding.clockTextColorPicker.setPreviewColor(color);
                    RPrefs.putInt(STATUSBAR_CLOCK_COLOR_CODE, color);
                }
        );

        // Status icons chip
        binding.statusIconsChip.setSwitchChecked(RPrefs.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false));
        binding.statusIconsChip.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(QSPANEL_STATUSICONSBG_SWITCH, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                OverlayUtil.enableOverlay("IconifyComponentIXCC.overlay");
                new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
            }, SWITCH_ANIMATION_DELAY);
        });

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
}