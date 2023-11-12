package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_BG;
import static com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_COLOR;
import static com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_SET;
import static com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_SHAPE;
import static com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_SIZE;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentSettingsIconsBinding;
import com.drdisagree.iconify.ui.dialogs.LoadingDialog;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.drdisagree.iconify.utils.overlay.manager.SettingsIconResourceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class SettingsIcons extends Fragment {

    private static int selectedBackground = 1, selectedShape = 1, selectedSize = 1, selectedIconColor = 1, selectedIcon = 1;
    private FragmentSettingsIconsBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsIconsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_settings_icons);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        // Retrieve previously saved preferenced
        selectedIcon = Prefs.getInt(SELECTED_SETTINGS_ICONS_SET, 1);

        // Background style
        binding.bgStyle.setSelectedIndex(Prefs.getInt(SELECTED_SETTINGS_ICONS_BG, 1) - 1);
        binding.bgStyle.setOnItemSelectedListener(index -> selectedBackground = index + 1);
        selectedBackground = binding.bgStyle.getSelectedIndex() + 1;

        // Background Shape
        binding.bgShape.setSelectedIndex(Prefs.getInt(SELECTED_SETTINGS_ICONS_SHAPE, 1) - 1);
        binding.bgShape.setOnItemSelectedListener(index -> selectedShape = index + 1);
        selectedShape = binding.bgShape.getSelectedIndex() + 1;

        // Icon Size
        binding.iconSize.setSelectedIndex(Prefs.getInt(SELECTED_SETTINGS_ICONS_SIZE, 1) - 1);
        binding.iconSize.setOnItemSelectedListener(index -> selectedSize = index + 1);
        selectedSize = binding.iconSize.getSelectedIndex() + 1;

        // Icon color
        binding.iconColor.setSelectedIndex(Prefs.getInt(SELECTED_SETTINGS_ICONS_COLOR, 1) - 1);
        binding.iconColor.setOnItemSelectedListener(index -> selectedIconColor = index + 1);
        selectedIconColor = binding.iconColor.getSelectedIndex() + 1;

        // Icon Pack list items
        ArrayList<Object[]> iconpack_list = new ArrayList<>();

        // Icon Pack add items in list
        iconpack_list.add(new Object[]{"Aurora", R.string.iconpack_aurora_desc, R.drawable.preview_aurora_wifi, R.drawable.preview_aurora_signal, R.drawable.preview_aurora_airplane, R.drawable.preview_aurora_location});
        iconpack_list.add(new Object[]{"Gradicon", R.string.iconpack_gradicon_desc, R.drawable.preview_gradicon_wifi, R.drawable.preview_gradicon_signal, R.drawable.preview_gradicon_airplane, R.drawable.preview_gradicon_location});
        iconpack_list.add(new Object[]{"Lorn", R.string.iconpack_lorn_desc, R.drawable.preview_lorn_wifi, R.drawable.preview_lorn_signal, R.drawable.preview_lorn_airplane, R.drawable.preview_lorn_location});
        iconpack_list.add(new Object[]{"Plumpy", R.string.iconpack_plumpy_desc, R.drawable.preview_plumpy_wifi, R.drawable.preview_plumpy_signal, R.drawable.preview_plumpy_airplane, R.drawable.preview_plumpy_location});
        iconpack_list.add(new Object[]{"Bubble v1", R.string.settings_iconpack_bubble_v1, R.drawable.preview_bubble_v1_1, R.drawable.preview_bubble_v1_2, R.drawable.preview_bubble_v1_3, R.drawable.preview_bubble_v1_4});
        iconpack_list.add(new Object[]{"Bubble v2", R.string.settings_iconpack_bubble_v2, R.drawable.preview_bubble_v2_1, R.drawable.preview_bubble_v2_2, R.drawable.preview_bubble_v2_3, R.drawable.preview_bubble_v2_4});

        addItem(iconpack_list);

        for (int i = 0; i < binding.iconPacksList.getChildCount(); i++) {
            RelativeLayout child = binding.iconPacksList.getChildAt(i).findViewById(R.id.icon_pack_child);
            if (((TextView) child.findViewById(R.id.iconpack_title)).getText() == "Bubble" || ((TextView) child.findViewById(R.id.iconpack_title)).getText() == "Bubble v2") {
                ((ImageView) child.findViewById(R.id.iconpack_preview1)).setColorFilter(0);
                ((ImageView) child.findViewById(R.id.iconpack_preview2)).setColorFilter(0);
                ((ImageView) child.findViewById(R.id.iconpack_preview3)).setColorFilter(0);
                ((ImageView) child.findViewById(R.id.iconpack_preview4)).setColorFilter(0);
            }
        }

        // Enable onClick event
        for (int i = 0; i < binding.iconPacksList.getChildCount(); i++) {
            enableOnClickListener(binding.iconPacksList.getChildAt(i).findViewById(R.id.icon_pack_child), i);
        }

        refreshBackground();

        // Enable and disable button
        if (Prefs.getBoolean("IconifyComponentSIP1.overlay"))
            binding.disableSettingsIcons.setVisibility(View.VISIBLE);

        binding.enableSettingsIcons.setOnClickListener(v -> {
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                // Show loading dialog
                loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));
                AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                new Thread(() -> {
                    try {
                        hasErroredOut.set(SettingsIconResourceManager.buildOverlay(selectedIcon, selectedBackground, selectedShape, selectedSize, selectedIconColor, true));
                    } catch (IOException e) {
                        hasErroredOut.set(true);
                        Log.e("SettingsIcons", e.toString());
                    }

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!hasErroredOut.get()) {
                            Prefs.putInt(SELECTED_SETTINGS_ICONS_SET, selectedIcon);
                            Prefs.putInt(SELECTED_SETTINGS_ICONS_BG, selectedBackground);
                            Prefs.putInt(SELECTED_SETTINGS_ICONS_SHAPE, selectedShape);
                            Prefs.putInt(SELECTED_SETTINGS_ICONS_SIZE, selectedSize);
                            Prefs.putInt(SELECTED_SETTINGS_ICONS_COLOR, selectedIconColor);

                            binding.disableSettingsIcons.setVisibility(View.VISIBLE);
                            binding.iconPacksList.getChildAt(selectedIcon - 1)
                                    .findViewById(R.id.icon_selected)
                                    .setVisibility(View.VISIBLE);
                            OverlayUtil.enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay");
                        }

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            // Hide loading dialog
                            loadingDialog.hide();

                            if (hasErroredOut.get())
                                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                        }, 10);
                    });
                }).start();
            }
        });

        binding.disableSettingsIcons.setOnClickListener(v -> {
            binding.iconPacksList.getChildAt(Prefs.getInt(SELECTED_SETTINGS_ICONS_SET, 1) - 1)
                    .findViewById(R.id.icon_selected)
                    .setVisibility(View.INVISIBLE);

            Prefs.clearPrefs(SELECTED_SETTINGS_ICONS_SET, SELECTED_SETTINGS_ICONS_BG, SELECTED_SETTINGS_ICONS_COLOR);

            binding.disableSettingsIcons.setVisibility(View.GONE);

            OverlayUtil.disableOverlays("IconifyComponentSIP1.overlay", "IconifyComponentSIP2.overlay", "IconifyComponentSIP3.overlay");
        });

        return view;
    }

    // Function to check for layout changes
    private void refreshLayout(RelativeLayout layout) {
        for (int i = 0; i < binding.iconPacksList.getChildCount(); i++) {
            RelativeLayout child = binding.iconPacksList.getChildAt(i).findViewById(R.id.icon_pack_child);
            itemSelected(child, child == layout, i + 1);
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        for (int i = 0; i < binding.iconPacksList.getChildCount(); i++) {
            RelativeLayout child = binding.iconPacksList.getChildAt(i).findViewById(R.id.icon_pack_child);
            itemSelected(child, Prefs.getInt(SELECTED_SETTINGS_ICONS_SET, 1) == i + 1, i + 1);
        }
    }

    // Function for onClick events
    private void enableOnClickListener(RelativeLayout layout, int index) {
        // Set onClick operation for options in list
        layout.setOnClickListener(v -> {
            refreshLayout(layout);
            selectedIcon = index + 1;
        });
    }

    // Function to add new item in list
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(requireContext()).inflate(R.layout.view_list_option_settings_icons, binding.iconPacksList, false);

            TextView name = list.findViewById(R.id.iconpack_title);
            name.setText((String) pack.get(i)[0]);

            TextView description = list.findViewById(R.id.iconpack_desc);
            description.setText(getResources().getString((int) pack.get(i)[1]));

            ImageView ic1 = list.findViewById(R.id.iconpack_preview1);
            ic1.setImageResource((int) pack.get(i)[2]);

            ImageView ic2 = list.findViewById(R.id.iconpack_preview2);
            ic2.setImageResource((int) pack.get(i)[3]);

            ImageView ic3 = list.findViewById(R.id.iconpack_preview3);
            ic3.setImageResource((int) pack.get(i)[4]);

            ImageView ic4 = list.findViewById(R.id.iconpack_preview4);
            ic4.setImageResource((int) pack.get(i)[5]);

            binding.iconPacksList.addView(list);
        }
    }

    private void itemSelected(View parent, boolean state, int selectedIndex) {
        if (state) {
            parent.setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), R.drawable.container_selected));
            ((TextView) parent.findViewById(R.id.iconpack_title)).setTextColor(ContextCompat.getColor(Iconify.getAppContext(), R.color.colorAccent));
            ((TextView) parent.findViewById(R.id.iconpack_desc)).setTextColor(ContextCompat.getColor(Iconify.getAppContext(), R.color.colorAccent));
            parent.findViewById(R.id.icon_selected).setVisibility(
                    Prefs.getBoolean("IconifyComponentSIP1.overlay") &&
                            Prefs.getInt(SELECTED_SETTINGS_ICONS_SET, 1) == selectedIndex ?
                            View.VISIBLE : View.INVISIBLE);
            parent.findViewById(R.id.iconpack_desc).setAlpha(0.8f);
        } else {
            parent.setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), R.drawable.item_background_material));
            ((TextView) parent.findViewById(R.id.iconpack_title)).setTextColor(ContextCompat.getColor(Iconify.getAppContext(), R.color.text_color_primary));
            ((TextView) parent.findViewById(R.id.iconpack_desc)).setTextColor(ContextCompat.getColor(Iconify.getAppContext(), R.color.text_color_secondary));
            parent.findViewById(R.id.icon_selected).setVisibility(View.INVISIBLE);
            parent.findViewById(R.id.iconpack_desc).setAlpha(1f);
        }
    }

    @Override
    public void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
}