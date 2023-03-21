package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Preferences.SHOW_XPOSED_WARN;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.activities.XposedBackgroundChip;
import com.drdisagree.iconify.ui.activities.XposedBatteryStyle;
import com.drdisagree.iconify.ui.activities.XposedHeaderClock;
import com.drdisagree.iconify.ui.activities.XposedHeaderImage;
import com.drdisagree.iconify.ui.activities.XposedLockscreenClock;
import com.drdisagree.iconify.ui.activities.XposedOthers;
import com.drdisagree.iconify.ui.activities.XposedQuickSettings;
import com.drdisagree.iconify.ui.activities.XposedTransparencyBlur;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class XposedMenu extends Fragment {

    private ViewGroup listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_xposed_menu, container, false);

        listView = view.findViewById(R.id.xposed_list);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = view.findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_xposed_menu));
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(view1 ->
                new Handler().postDelayed(() -> {
                    getParentFragmentManager().popBackStack();
                }, 50));

        // Xposed warn
        LinearLayout xposed_warn = view.findViewById(R.id.xposed_warn);
        xposed_warn.setVisibility(Prefs.getBoolean(SHOW_XPOSED_WARN, true) ? View.VISIBLE : View.GONE);

        FrameLayout close_xposed_warn = view.findViewById(R.id.close_xposed_warn);
        close_xposed_warn.setOnClickListener(v -> {
            new Handler().postDelayed(() -> {
                Prefs.putBoolean(SHOW_XPOSED_WARN, false);
                xposed_warn.animate().translationX(xposed_warn.getWidth() * 2f).alpha(0f).withEndAction(() -> xposed_warn.setVisibility(View.GONE)).start();
            }, 50);
        });

        // Restart SystemUI
        Button button_restartSysui = view.findViewById(R.id.button_restartSysui);

        button_restartSysui.setOnClickListener(v -> Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_restart_sysui), Toast.LENGTH_SHORT).show());

        button_restartSysui.setOnLongClickListener(v -> {
            SystemUtil.restartSystemUI();
            return true;
        });

        // Xposed menu list items
        ArrayList<Object[]> xposed_menu = new ArrayList<>();

        xposed_menu.add(new Object[]{XposedTransparencyBlur.class, getResources().getString(R.string.activity_title_transparency_blur), getResources().getString(R.string.activity_desc_transparency_blur), R.drawable.ic_xposed_transparency_blur});
        xposed_menu.add(new Object[]{XposedQuickSettings.class, getResources().getString(R.string.activity_title_quick_settings), getResources().getString(R.string.activity_desc_quick_settings), R.drawable.ic_xposed_quick_settings});
        xposed_menu.add(new Object[]{XposedBatteryStyle.class, getResources().getString(R.string.activity_title_battery_style), getResources().getString(R.string.activity_desc_battery_style), R.drawable.ic_colored_battery});
        xposed_menu.add(new Object[]{XposedHeaderImage.class, getResources().getString(R.string.activity_title_header_image), getResources().getString(R.string.activity_desc_header_image), R.drawable.ic_xposed_header_image});
        xposed_menu.add(new Object[]{XposedHeaderClock.class, getResources().getString(R.string.activity_title_header_clock), getResources().getString(R.string.activity_desc_header_clock), R.drawable.ic_xposed_header_clock});
        xposed_menu.add(new Object[]{XposedLockscreenClock.class, getResources().getString(R.string.activity_title_lockscreen_clock), getResources().getString(R.string.activity_desc_lockscreen_clock), R.drawable.ic_xposed_lockscreen});
        xposed_menu.add(new Object[]{XposedBackgroundChip.class, getResources().getString(R.string.activity_title_background_chip), getResources().getString(R.string.activity_desc_background_chip), R.drawable.ic_xposed_background_chip});
        xposed_menu.add(new Object[]{XposedOthers.class, getResources().getString(R.string.activity_title_xposed_others), getResources().getString(R.string.activity_desc_xposed_others), R.drawable.ic_xposed_misc});

        addItem(xposed_menu);

        // Enable onClick event
        for (int i = 0; i < xposed_menu.size(); i++) {
            LinearLayout child = listView.getChildAt(i).findViewById(R.id.list_info_item);
            int finalI = i;
            child.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), (Class<?>) xposed_menu.get(finalI)[0]);
                startActivity(intent);
            });
        }

        return view;
    }

    // Function to add new item in list
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(requireActivity()).inflate(R.layout.view_list_menu, listView, false);

            TextView title = list.findViewById(R.id.list_title);
            title.setText((String) pack.get(i)[1]);

            TextView desc = list.findViewById(R.id.list_desc);
            desc.setText((String) pack.get(i)[2]);

            ImageView preview = list.findViewById(R.id.list_preview);
            preview.setImageResource((int) pack.get(i)[3]);

            listView.addView(list);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Toast.makeText(requireActivity(), "Test", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}