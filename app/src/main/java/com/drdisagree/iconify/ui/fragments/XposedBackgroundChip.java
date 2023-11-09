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
import com.drdisagree.iconify.ui.dialogs.RadioDialog;
import com.drdisagree.iconify.ui.events.ColorSelectedEvent;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.drdisagree.iconify.xposed.modules.utils.Helpers;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public class XposedBackgroundChip extends BaseFragment implements RadioDialog.RadioDialogListener {

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

            if (!isChecked && getContext() != null) {
                Helpers.forceReloadUI(getContext());
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
        binding.clockTextColor.setRadioDialogListener(
                this,
                0,
                RPrefs.getInt(STATUSBAR_CLOCK_COLOR_OPTION, 0),
                R.string.statusbar_clock_color_title,
                R.array.statusbar_clock_color
        );

        // Clock Color Picker
        binding.clockTextColorPicker.setVisibility(
                RPrefs.getInt(STATUSBAR_CLOCK_COLOR_OPTION, 0) == 2 ?
                        View.VISIBLE :
                        View.GONE
        );
        binding.clockTextColorPicker.setColorPickerListener(
                requireActivity(),
                1,
                RPrefs.getInt(STATUSBAR_CLOCK_COLOR_CODE, Color.WHITE),
                true,
                true,
                true
        );

        // Status icons chip
        binding.statusIconsChip.setSwitchChecked(RPrefs.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false));
        binding.statusIconsChip.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(QSPANEL_STATUSICONSBG_SWITCH, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                OverlayUtil.enableOverlay("IconifyComponentIXCC.overlay");

                if (getContext() != null) {
                    Helpers.forceReloadUI(getContext());
                }
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

    @Override
    public void onItemSelected(int dialogId, int selectedIndex) {
        if (dialogId == 0) {
            RPrefs.putInt(STATUSBAR_CLOCK_COLOR_OPTION, selectedIndex);
            binding.clockTextColorPicker.setVisibility(selectedIndex == 2 ? View.VISIBLE : View.GONE);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onColorSelected(ColorSelectedEvent event) {
        if (event.dialogId() == 1) {
            binding.clockTextColorPicker.setPreviewColor(event.selectedColor());
            RPrefs.putInt(STATUSBAR_CLOCK_COLOR_CODE, event.selectedColor());
        }
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
}