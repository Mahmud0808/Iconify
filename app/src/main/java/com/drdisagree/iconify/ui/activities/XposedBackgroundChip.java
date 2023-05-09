package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.CHIP_QSSTATUSICONS_STYLE;
import static com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCKBG_STYLE;
import static com.drdisagree.iconify.common.Preferences.QSPANEL_STATUSICONSBG_SWITCH;
import static com.drdisagree.iconify.common.Preferences.STATUSBAR_CLOCKBG_SWITCH;

import android.annotation.SuppressLint;
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
import com.drdisagree.iconify.utils.HelperUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;

public class XposedBackgroundChip extends BaseActivity {

    private FlexboxLayout containerStatusBar, containerStatusIcons;

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

        addItemStatusBar(status_bar_chip_style);

        refreshBackgroundStatusBar();

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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}