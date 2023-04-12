package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_BG;
import static com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_COLOR;
import static com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_SET;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.overlaymanager.SettingsIconsManager;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.ui.views.RadioDialog;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class SettingsIcons extends AppCompatActivity implements RadioDialog.RadioDialogListener {

    private static int selectedIconColor = 1, selectedBackground = 1, selectedIcon = 1;
    LoadingDialog loadingDialog;
    private ViewGroup container;
    RadioDialog rd_bg_style, rd_icon_color;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_icons);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_settings_icons);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // Retrieve previously saved preferenced
        selectedIcon = Prefs.getInt(SELECTED_SETTINGS_ICONS_SET, 1);

        // Background style
        LinearLayout bg_style = findViewById(R.id.bg_style);
        TextView selected_bg_style = findViewById(R.id.selected_bg_style);
        rd_bg_style = new RadioDialog(this, 0, Prefs.getInt(SELECTED_SETTINGS_ICONS_BG, 1) - 1);
        rd_bg_style.setRadioDialogListener(this);
        bg_style.setOnClickListener(v -> rd_bg_style.show(R.string.settings_icons_background, R.array.settings_icon_bg, selected_bg_style));
        selectedBackground = rd_bg_style.getSelectedIndex() + 1;
        selected_bg_style.setText(Arrays.asList(getResources().getStringArray(R.array.settings_icon_bg)).get(selectedBackground - 1));

        // Icon color
        LinearLayout icon_color = findViewById(R.id.icon_color);
        TextView selected_icon_color = findViewById(R.id.selected_icon_color);
        rd_icon_color = new RadioDialog(this, 1, Prefs.getInt(SELECTED_SETTINGS_ICONS_COLOR, 1) - 1);
        rd_icon_color.setRadioDialogListener(this);
        icon_color.setOnClickListener(v -> rd_icon_color.show(R.string.settins_icons_icon_color, R.array.settings_icon_color, findViewById(R.id.selected_icon_color)));
        selectedIconColor = rd_icon_color.getSelectedIndex() + 1;
        selected_icon_color.setText(Arrays.asList(getResources().getStringArray(R.array.settings_icon_color)).get(selectedIconColor - 1));

        // Icon Pack list items
        container = findViewById(R.id.icon_packs_list);
        ArrayList<Object[]> iconpack_list = new ArrayList<>();

        // Icon Pack add items in list
        iconpack_list.add(new Object[]{"Aurora", "Dual tone linear icon pack", R.drawable.preview_aurora_wifi, R.drawable.preview_aurora_signal, R.drawable.preview_aurora_airplane, R.drawable.preview_aurora_location});
        iconpack_list.add(new Object[]{"Gradicon", "Gradient shaded filled icon pack", R.drawable.preview_gradicon_wifi, R.drawable.preview_gradicon_signal, R.drawable.preview_gradicon_airplane, R.drawable.preview_gradicon_location});
        iconpack_list.add(new Object[]{"Lorn", "Thick linear icon pack", R.drawable.preview_lorn_wifi, R.drawable.preview_lorn_signal, R.drawable.preview_lorn_airplane, R.drawable.preview_lorn_location});
        iconpack_list.add(new Object[]{"Plumpy", "Dual tone filled icon pack", R.drawable.preview_plumpy_wifi, R.drawable.preview_plumpy_signal, R.drawable.preview_plumpy_airplane, R.drawable.preview_plumpy_location});
        iconpack_list.add(new Object[]{"Bubble", "Inline multicolored icon pack", R.drawable.preview_bubble_1, R.drawable.preview_bubble_2, R.drawable.preview_bubble_3, R.drawable.preview_bubble_4});
        iconpack_list.add(new Object[]{"Bubble v2", "Outline multicolored icon pack", R.drawable.preview_bubble_v2_1, R.drawable.preview_bubble_v2_2, R.drawable.preview_bubble_v2_3, R.drawable.preview_bubble_v2_4});

        addItem(iconpack_list);

        for (int i = 0; i < container.getChildCount(); i++) {
            LinearLayout child = container.getChildAt(i).findViewById(R.id.icon_pack_child);
            if (((TextView) child.findViewById(R.id.iconpack_title)).getText() == "Bubble" || ((TextView) child.findViewById(R.id.iconpack_title)).getText() == "Bubble v2") {
                ((ImageView) child.findViewById(R.id.iconpack_preview1)).setColorFilter(0);
                ((ImageView) child.findViewById(R.id.iconpack_preview2)).setColorFilter(0);
                ((ImageView) child.findViewById(R.id.iconpack_preview3)).setColorFilter(0);
                ((ImageView) child.findViewById(R.id.iconpack_preview4)).setColorFilter(0);
            }
        }

        // Enable onClick event
        for (int i = 0; i < container.getChildCount(); i++) {
            enableOnClickListener(container.getChildAt(i).findViewById(R.id.icon_pack_child), i);
        }

        refreshBackground();

        // Enable and disable button
        Button enable_settings_icons = findViewById(R.id.enable_settings_icons);
        Button disable_settings_icons = findViewById(R.id.disable_settings_icons);

        if (Prefs.getBoolean("IconifyComponentSIP1.overlay"))
            disable_settings_icons.setVisibility(View.VISIBLE);

        enable_settings_icons.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                SystemUtil.getStoragePermission(this);
            } else {
                // Show loading dialog
                loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));
                AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                Runnable runnable = () -> {
                    try {
                        hasErroredOut.set(SettingsIconsManager.enableOverlay(selectedIcon, selectedBackground, selectedIconColor));
                    } catch (IOException e) {
                        hasErroredOut.set(true);
                        Log.e("SettingsIcons", e.toString());
                    }

                    runOnUiThread(() -> {
                        if (!hasErroredOut.get()) {
                            Prefs.putInt(SELECTED_SETTINGS_ICONS_SET, selectedIcon);
                            Prefs.putInt(SELECTED_SETTINGS_ICONS_BG, selectedBackground);
                            Prefs.putInt(SELECTED_SETTINGS_ICONS_COLOR, selectedIconColor);

                            disable_settings_icons.setVisibility(View.VISIBLE);
                            OverlayUtil.enableOverlay("IconifyComponentCR1.overlay");
                            OverlayUtil.enableOverlay("IconifyComponentCR2.overlay");
                        }

                        new Handler().postDelayed(() -> {
                            // Hide loading dialog
                            loadingDialog.hide();

                            if (hasErroredOut.get())
                                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                        }, 10);
                    });
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });

        disable_settings_icons.setOnClickListener(v -> {
            Prefs.clearPref(SELECTED_SETTINGS_ICONS_SET);
            Prefs.clearPref(SELECTED_SETTINGS_ICONS_BG);
            Prefs.clearPref(SELECTED_SETTINGS_ICONS_COLOR);

            disable_settings_icons.setVisibility(View.GONE);

            for (int i = 1; i <= 3; i++)
                OverlayUtil.disableOverlay("IconifyComponentSIP" + i + ".overlay");
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Function to check for layout changes
    private void refreshLayout(LinearLayout layout) {
        for (int i = 0; i < container.getChildCount(); i++) {
            LinearLayout child = container.getChildAt(i).findViewById(R.id.icon_pack_child);
            if (!(child == layout)) {
                container.getChildAt(i).setBackground(ContextCompat.getDrawable(SettingsIcons.this, R.drawable.container));
            }
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        for (int i = 0; i < container.getChildCount(); i++) {
            LinearLayout child = container.getChildAt(i).findViewById(R.id.icon_pack_child);
            if (Prefs.getInt(SELECTED_SETTINGS_ICONS_SET, 1) == i + 1) {
                child.setBackground(ContextCompat.getDrawable(SettingsIcons.this, R.drawable.container_selected));
            } else {
                child.setBackground(ContextCompat.getDrawable(SettingsIcons.this, R.drawable.container));
            }
        }
    }

    // Function for onClick events
    private void enableOnClickListener(LinearLayout layout, int index) {
        // Set onClick operation for options in list
        layout.setOnClickListener(v -> {
            refreshLayout(layout);
            layout.setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), R.drawable.container_selected));
            selectedIcon = index + 1;
        });
    }

    // Function to add new item in list
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_list_option_settings_icons, container, false);

            TextView name = list.findViewById(R.id.iconpack_title);
            name.setText((String) pack.get(i)[0]);

            TextView description = list.findViewById(R.id.iconpack_desc);
            description.setText((String) pack.get(i)[1]);

            ImageView ic1 = list.findViewById(R.id.iconpack_preview1);
            ic1.setImageResource((int) pack.get(i)[2]);

            ImageView ic2 = list.findViewById(R.id.iconpack_preview2);
            ic2.setImageResource((int) pack.get(i)[3]);

            ImageView ic3 = list.findViewById(R.id.iconpack_preview3);
            ic3.setImageResource((int) pack.get(i)[4]);

            ImageView ic4 = list.findViewById(R.id.iconpack_preview4);
            ic4.setImageResource((int) pack.get(i)[5]);

            container.addView(list);
        }
    }

    @Override
    public void onItemSelected(int dialogId, int selectedIndex) {
        switch (dialogId) {
            case 0:
                selectedBackground = selectedIndex + 1;
                break;
            case 1:
                selectedIconColor = selectedIndex + 1;
                break;
        }
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        rd_bg_style.dismiss();
        rd_icon_color.dismiss();
        super.onDestroy();
    }
}