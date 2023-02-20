package com.drdisagree.iconify.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.overlaymanager.IconPackManager;
import com.drdisagree.iconify.ui.view.LoadingDialog;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class IconPacks extends AppCompatActivity {

    ArrayList<String> ICONPACK_KEY = new ArrayList<>();

    LoadingDialog loadingDialog;
    private ViewGroup container, container_activity;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_packs);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_icon_pack));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // Activities list
        container_activity = findViewById(R.id.icon_packs_list_activity);
        ArrayList<Object[]> iconpack_list_activity = new ArrayList<>();

        // Activities add items in list
        iconpack_list_activity.add(new Object[]{ColoredBattery.class, getResources().getString(R.string.activity_title_colored_battery), getResources().getString(R.string.activity_desc_colored_battery), R.drawable.ic_colored_battery});
        iconpack_list_activity.add(new Object[]{SettingsIcons.class, getResources().getString(R.string.activity_title_settings_icons), getResources().getString(R.string.activity_desc_settings_icons), R.drawable.ic_settings_icon_pack});

        addActivityItem(iconpack_list_activity);
        fixViewGroup(container_activity);

        // Enable onClick event
        for (int i = 0; i < iconpack_list_activity.size(); i++) {
            LinearLayout child = container_activity.getChildAt(i).findViewById(R.id.list_item);
            int finalI = i;
            child.setOnClickListener(v -> {
                Intent intent = new Intent(IconPacks.this, (Class<?>) iconpack_list_activity.get(finalI)[0]);
                startActivity(intent);
            });
        }

        // Icon Pack list items
        container = findViewById(R.id.icon_packs_list);
        ArrayList<Object[]> iconpack_list = new ArrayList<>();

        // Icon Pack add items in list
        iconpack_list.add(new Object[]{"Aurora", "Dual tone linear icon pack", R.drawable.preview_aurora_wifi, R.drawable.preview_aurora_signal, R.drawable.preview_aurora_airplane, R.drawable.preview_aurora_location});
        iconpack_list.add(new Object[]{"Gradicon", "Gradient shaded filled icon pack", R.drawable.preview_gradicon_wifi, R.drawable.preview_gradicon_signal, R.drawable.preview_gradicon_airplane, R.drawable.preview_gradicon_location});
        iconpack_list.add(new Object[]{"Lorn", "Thick linear icon pack", R.drawable.preview_lorn_wifi, R.drawable.preview_lorn_signal, R.drawable.preview_lorn_airplane, R.drawable.preview_lorn_location});
        iconpack_list.add(new Object[]{"Plumpy", "Dual tone filled icon pack", R.drawable.preview_plumpy_wifi, R.drawable.preview_plumpy_signal, R.drawable.preview_plumpy_airplane, R.drawable.preview_plumpy_location});
        iconpack_list.add(new Object[]{"Acherus", "Acherus sub icon pack", R.drawable.preview_acherus_wifi, R.drawable.preview_acherus_signal, R.drawable.preview_acherus_airplane, R.drawable.preview_acherus_location});
        iconpack_list.add(new Object[]{"Circular", "Thin line icon pack", R.drawable.preview_circular_wifi, R.drawable.preview_circular_signal, R.drawable.preview_circular_airplane, R.drawable.preview_circular_location});
        iconpack_list.add(new Object[]{"Filled", "Dual tone filled icon pack", R.drawable.preview_filled_wifi, R.drawable.preview_filled_signal, R.drawable.preview_filled_airplane, R.drawable.preview_filled_location});
        iconpack_list.add(new Object[]{"Kai", "Thin line icon pack", R.drawable.preview_kai_wifi, R.drawable.preview_kai_signal, R.drawable.preview_kai_airplane, R.drawable.preview_kai_location});
        iconpack_list.add(new Object[]{"OOS", "Oxygen OS icon pack", R.drawable.preview_oos_wifi, R.drawable.preview_oos_signal, R.drawable.preview_oos_airplane, R.drawable.preview_oos_location});
        iconpack_list.add(new Object[]{"Outline", "Thin outline icon pack", R.drawable.preview_outline_wifi, R.drawable.preview_outline_signal, R.drawable.preview_outline_airplane, R.drawable.preview_outline_location});
        iconpack_list.add(new Object[]{"PUI", "Thick dualtone icon pack", R.drawable.preview_pui_wifi, R.drawable.preview_pui_signal, R.drawable.preview_pui_airplane, R.drawable.preview_pui_location});
        iconpack_list.add(new Object[]{"Rounded", "Rounded corner icon pack", R.drawable.preview_rounded_wifi, R.drawable.preview_rounded_signal, R.drawable.preview_rounded_airplane, R.drawable.preview_rounded_location});
        iconpack_list.add(new Object[]{"Sam", "Filled icon pack", R.drawable.preview_sam_wifi, R.drawable.preview_sam_signal, R.drawable.preview_sam_airplane, R.drawable.preview_sam_location});
        iconpack_list.add(new Object[]{"Victor", "Edgy icon pack", R.drawable.preview_victor_wifi, R.drawable.preview_victor_signal, R.drawable.preview_victor_airplane, R.drawable.preview_victor_location});

        addItem(iconpack_list);

        // Generate keys for preference
        for (int i = 0; i < container.getChildCount(); i++) {
            ICONPACK_KEY.add("IconifyComponentIPAS" + (i + 1) + ".overlay");
        }

        // Enable onClick event
        for (int i = 0; i < container.getChildCount(); i++) {
            enableOnClickListener(container.getChildAt(i).findViewById(R.id.icon_pack_child),
                    container.getChildAt(i).findViewById(R.id.list_button_enable_iconpack),
                    container.getChildAt(i).findViewById(R.id.list_button_disable_iconpack),
                    ICONPACK_KEY.get(i), i);
        }

        refreshBackground();
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
                container.getChildAt(i).findViewById(R.id.list_button_enable_iconpack).setVisibility(View.GONE);
                container.getChildAt(i).findViewById(R.id.list_button_disable_iconpack).setVisibility(View.GONE);
            }
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        for (int i = 0; i < container.getChildCount(); i++) {
            LinearLayout child = container.getChildAt(i).findViewById(R.id.icon_pack_child);
            if (Prefs.getBoolean(ICONPACK_KEY.get(i))) {
                child.setBackground(ContextCompat.getDrawable(IconPacks.this, R.drawable.container_selected));
            } else {
                child.setBackground(ContextCompat.getDrawable(IconPacks.this, R.drawable.container));
            }
        }
    }

    // Function for onClick events
    private void enableOnClickListener(LinearLayout layout, Button enable, Button disable, String key, int index) {
        // Set onClick operation for options in list
        layout.setOnClickListener(v -> {
            refreshLayout(layout);
            if (!Prefs.getBoolean(key)) {
                disable.setVisibility(View.GONE);
                if (enable.getVisibility() == View.VISIBLE)
                    enable.setVisibility(View.GONE);
                else
                    enable.setVisibility(View.VISIBLE);
            } else {
                enable.setVisibility(View.GONE);
                if (disable.getVisibility() == View.VISIBLE)
                    disable.setVisibility(View.GONE);
                else
                    disable.setVisibility(View.VISIBLE);
            }
        });

        // Set onClick operation for Enable button
        enable.setOnClickListener(v -> {
            refreshLayout(layout);
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                IconPackManager.install_pack(index + 1);

                runOnUiThread(() -> {
                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Change button visibility
                        enable.setVisibility(View.GONE);
                        disable.setVisibility(View.VISIBLE);
                        refreshBackground();

                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    }, 3000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });

        // Set onClick operation for Disable button
        disable.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                IconPackManager.disable_pack(index + 1);

                runOnUiThread(() -> {
                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Change button visibility
                        disable.setVisibility(View.GONE);
                        enable.setVisibility(View.VISIBLE);
                        refreshBackground();

                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                    }, 3000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });
    }

    // Function to add new item in list
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_list_option_iconpack, container, false);

            TextView name = list.findViewById(R.id.list_title_iconpack);
            name.setText((String) pack.get(i)[0]);

            TextView description = list.findViewById(R.id.list_desc_iconpack);
            description.setText((String) pack.get(i)[1]);

            ImageView ic1 = list.findViewById(R.id.list_preview1_iconpack);
            ic1.setImageResource((int) pack.get(i)[2]);

            ImageView ic2 = list.findViewById(R.id.list_preview2_iconpack);
            ic2.setImageResource((int) pack.get(i)[3]);

            ImageView ic3 = list.findViewById(R.id.list_preview3_iconpack);
            ic3.setImageResource((int) pack.get(i)[4]);

            ImageView ic4 = list.findViewById(R.id.list_preview4_iconpack);
            ic4.setImageResource((int) pack.get(i)[5]);

            container.addView(list);
        }
    }

    // Function to add new item in list
    private void addActivityItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_list_menu, container_activity, false);

            TextView title = list.findViewById(R.id.list_title);
            title.setText((String) pack.get(i)[1]);

            TextView desc = list.findViewById(R.id.list_desc);
            desc.setText((String) pack.get(i)[2]);

            ImageView preview = list.findViewById(R.id.list_preview);
            preview.setImageResource((int) pack.get(i)[3]);

            container_activity.addView(list);
        }
    }

    private void fixViewGroup(ViewGroup viewGroup) {
        ((ViewGroup.MarginLayoutParams) viewGroup.getChildAt(viewGroup.getChildCount() - 1).getLayoutParams()).setMargins(0, 0, 0, 0);
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}