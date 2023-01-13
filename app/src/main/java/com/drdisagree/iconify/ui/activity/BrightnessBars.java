package com.drdisagree.iconify.ui.activity;

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
import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.overlaymanager.BrightnessManager;
import com.drdisagree.iconify.ui.fragment.LoadingDialog;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class BrightnessBars extends AppCompatActivity {

    ArrayList<String> BRIGHTNESSBAR_KEY = new ArrayList<>();

    LoadingDialog loadingDialog;
    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brightness_bars);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_brightness_bar));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Brightnessbar pixel item on click
        LinearLayout brightness_bar_pixel = findViewById(R.id.brightness_bar_pixel);
        brightness_bar_pixel.setOnClickListener(v -> {
            Intent intent = new Intent(BrightnessBars.this, BrightnessBarsPixel.class);
            startActivity(intent);
        });

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // Brightness Bar list items
        container = (ViewGroup) findViewById(R.id.brightness_bars_list);
        ArrayList<Object[]> bb_list = new ArrayList<>();

        // Brightness Bar add items in list
        bb_list.add(new Object[]{"Rounded Clip", R.drawable.bb_roundedclip, R.drawable.auto_bb_roundedclip});
        bb_list.add(new Object[]{"Rounded Bar", R.drawable.bb_rounded, R.drawable.auto_bb_rounded});
        bb_list.add(new Object[]{"Double Layer", R.drawable.bb_double_layer, R.drawable.auto_bb_double_layer});
        bb_list.add(new Object[]{"Shaded Layer", R.drawable.bb_shaded_layer, R.drawable.auto_bb_shaded_layer});
        bb_list.add(new Object[]{"Outline", R.drawable.bb_outline, R.drawable.auto_bb_outline});
        bb_list.add(new Object[]{"Leafy Outline", R.drawable.bb_leafy_outline, R.drawable.auto_bb_leafy_outline});
        bb_list.add(new Object[]{"Neumorph", R.drawable.bb_neumorph, R.drawable.auto_bb_neumorph});
        bb_list.add(new Object[]{"Neumorph Outline", R.drawable.bb_neumorph_outline, R.drawable.auto_bb_neumorph_outline});
        bb_list.add(new Object[]{"Inline", R.drawable.bb_inline, R.drawable.auto_bb_rounded});
        bb_list.add(new Object[]{"Neumorph Thumb", R.drawable.bb_neumorph_thumb, R.drawable.auto_bb_neumorph_thumb});
        bb_list.add(new Object[]{"Blocky Thumb", R.drawable.bb_blocky_thumb, R.drawable.auto_bb_blocky_thumb});
        bb_list.add(new Object[]{"Comet Thumb", R.drawable.bb_comet_thumb, R.drawable.auto_bb_comet_thumb});
        bb_list.add(new Object[]{"Minimal Thumb", R.drawable.bb_minimal_thumb, R.drawable.auto_bb_minimal_thumb});
        bb_list.add(new Object[]{"Old School Thumb", R.drawable.bb_oldschool_thumb, R.drawable.auto_bb_oldschool_thumb});
        bb_list.add(new Object[]{"Gradient Thumb", R.drawable.bb_gradient_thumb, R.drawable.auto_bb_gradient_thumb});
        bb_list.add(new Object[]{"Lighty", R.drawable.bb_lighty, R.drawable.auto_bb_lighty});

        addItem(bb_list);

        // Generate keys for preference
        for (int i = 0; i < container.getChildCount(); i++) {
            BRIGHTNESSBAR_KEY.add("IconifyComponentBBN" + (i + 1) + ".overlay");
        }

        // Enable onClick event
        for (int i = 0; i < container.getChildCount(); i++) {
            enableOnClickListener(container.getChildAt(i).findViewById(R.id.brightness_bar_child),
                    container.getChildAt(i).findViewById(R.id.list_button_enable_brightnessbar),
                    container.getChildAt(i).findViewById(R.id.list_button_disable_brightnessbar),
                    BRIGHTNESSBAR_KEY.get(i), i);
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
            LinearLayout child = container.getChildAt(i).findViewById(R.id.brightness_bar_child);
            if (!(child == layout)) {
                container.getChildAt(i).findViewById(R.id.list_button_enable_brightnessbar).setVisibility(View.GONE);
                container.getChildAt(i).findViewById(R.id.list_button_disable_brightnessbar).setVisibility(View.GONE);
            }
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        for (int i = 0; i < container.getChildCount(); i++) {
            LinearLayout child = container.getChildAt(i).findViewById(R.id.brightness_bar_child);
            if (Prefs.getBoolean(BRIGHTNESSBAR_KEY.get(i))) {
                child.setBackground(ContextCompat.getDrawable(BrightnessBars.this, R.drawable.container_selected));
            } else {
                child.setBackground(ContextCompat.getDrawable(BrightnessBars.this, R.drawable.container));
            }
        }
    }

    // Function to disable other packs if one is applied
    private void disable_others(String pack) {
        for (int i = 0; i < References.TOTAL_BRIGHTNESSBARS; i++)
            Prefs.putBoolean(BRIGHTNESSBAR_KEY.get(i), pack.equals(BRIGHTNESSBAR_KEY.get(i)));
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
                disable_others(key);
                BrightnessManager.install_pack(index + 1);

                runOnUiThread(() -> {
                    Prefs.putBoolean(key, true);

                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Change button visibility
                        enable.setVisibility(View.GONE);
                        disable.setVisibility(View.VISIBLE);
                        refreshBackground();

                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    }, 2000);
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
                BrightnessManager.disable_pack(index + 1);

                runOnUiThread(() -> {
                    Prefs.putBoolean(key, false);

                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Change button visibility
                        disable.setVisibility(View.GONE);
                        enable.setVisibility(View.VISIBLE);
                        refreshBackground();

                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                    }, 2000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });
    }

    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.list_option_brightnessbar, container, false);

            TextView name = list.findViewById(R.id.list_title_brightnessbar);
            name.setText((String) pack.get(i)[0]);

            ImageView brightnessbar = list.findViewById(R.id.brightness_bar);
            brightnessbar.setBackground(ContextCompat.getDrawable(BrightnessBars.this, (int) pack.get(i)[1]));

            ImageView auto_brightness_icon = list.findViewById(R.id.auto_brightness_icon);
            auto_brightness_icon.setBackground(ContextCompat.getDrawable(BrightnessBars.this, (int) pack.get(i)[2]));

            container.addView(list);
        }
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}