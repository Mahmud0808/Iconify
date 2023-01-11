package com.drdisagree.iconify.ui.activity;

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
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.overlaymanager.BrightnessPixelManager;
import com.drdisagree.iconify.ui.fragment.LoadingDialog;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class BrightnessBarsPixel extends AppCompatActivity {

    ArrayList<String> BRIGHTNESSBAR_KEY = new ArrayList<>();

    LoadingDialog loadingDialog;
    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brightness_bars_pixel);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_brightness_bar));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // Brightness Bar list items
        container = (ViewGroup) findViewById(R.id.brightness_bars_list_pixel);
        ArrayList<Object[]> bb_list = new ArrayList<>();

        // Brightness Bar add items in list
        bb_list.add(new Object[]{"Rounded Clip", R.drawable.bb_roundedclip_pixel, R.drawable.auto_bb_roundedclip_pixel});
        bb_list.add(new Object[]{"Rounded Bar", R.drawable.bb_rounded_pixel, R.drawable.auto_bb_rounded_pixel});
        bb_list.add(new Object[]{"Double Layer", R.drawable.bb_double_layer_pixel, R.drawable.auto_bb_double_layer_pixel});
        bb_list.add(new Object[]{"Shaded Layer", R.drawable.bb_shaded_layer_pixel, R.drawable.auto_bb_shaded_layer_pixel});
        bb_list.add(new Object[]{"Outline", R.drawable.bb_outline_pixel, R.drawable.auto_bb_outline_pixel});
        bb_list.add(new Object[]{"Leafy Outline", R.drawable.bb_leafy_outline_pixel, R.drawable.auto_bb_leafy_outline_pixel});
        bb_list.add(new Object[]{"Neumorph", R.drawable.bb_neumorph_pixel, R.drawable.auto_bb_neumorph_pixel});
        bb_list.add(new Object[]{"Neumorph Outline", R.drawable.bb_neumorph_outline_pixel, R.drawable.auto_bb_neumorph_outline_pixel});
        bb_list.add(new Object[]{"Inline", R.drawable.bb_inline_pixel, R.drawable.auto_bb_rounded_pixel});
        bb_list.add(new Object[]{"Neumorph Thumb", R.drawable.bb_neumorph_thumb_pixel, R.drawable.auto_bb_neumorph_thumb_pixel});
        bb_list.add(new Object[]{"Blocky Thumb", R.drawable.bb_blocky_thumb_pixel, R.drawable.auto_bb_blocky_thumb_pixel});
        bb_list.add(new Object[]{"Comet Thumb", R.drawable.bb_comet_thumb_pixel, R.drawable.auto_bb_comet_thumb_pixel});
        bb_list.add(new Object[]{"Minimal Thumb", R.drawable.bb_minimal_thumb_pixel, R.drawable.auto_bb_minimal_thumb_pixel});
        bb_list.add(new Object[]{"Old School Thumb", R.drawable.bb_oldschool_thumb_pixel, R.drawable.auto_bb_oldschool_thumb_pixel});
        bb_list.add(new Object[]{"Gradient Thumb", R.drawable.bb_gradient_thumb_pixel, R.drawable.auto_bb_gradient_thumb_pixel});
        bb_list.add(new Object[]{"Lighty", R.drawable.bb_lighty_pixel, R.drawable.auto_bb_lighty_pixel});

        addItem(bb_list);

        // Generate keys for preference
        for (int i = 0; i < container.getChildCount(); i++) {
            BRIGHTNESSBAR_KEY.add("IconifyComponentBBP" + (i + 1) + ".overlay");
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
            if (PrefConfig.loadPrefBool(Iconify.getAppContext(), BRIGHTNESSBAR_KEY.get(i))) {
                child.setBackground(ContextCompat.getDrawable(BrightnessBarsPixel.this, R.drawable.container_selected));
            } else {
                child.setBackground(ContextCompat.getDrawable(BrightnessBarsPixel.this, R.drawable.container));
            }
        }
    }

    // Function to disable other packs if one is applied
    private void disable_others(String pack) {
        for (int i = 0; i < References.TOTAL_BRIGHTNESSBARSPIXEL; i++)
            PrefConfig.savePrefBool(Iconify.getAppContext(), BRIGHTNESSBAR_KEY.get(i), pack.equals(BRIGHTNESSBAR_KEY.get(i)));
    }

    // Function for onClick events
    private void enableOnClickListener(LinearLayout layout, Button enable, Button disable, String key, int index) {
        // Set onClick operation for options in list
        layout.setOnClickListener(v -> {
            refreshLayout(layout);
            if (!PrefConfig.loadPrefBool(Iconify.getAppContext(), key)) {
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
                BrightnessPixelManager.install_pack(index + 1);

                runOnUiThread(() -> {
                    PrefConfig.savePrefBool(Iconify.getAppContext(), key, true);

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
                BrightnessPixelManager.disable_pack(index + 1);

                runOnUiThread(() -> {
                    PrefConfig.savePrefBool(Iconify.getAppContext(), key, false);

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
            View list = LayoutInflater.from(this).inflate(R.layout.list_option_brightnessbar_pixel, container, false);

            TextView name = list.findViewById(R.id.list_title_brightnessbar);
            name.setText((String) pack.get(i)[0]);

            ImageView brightnessbar = list.findViewById(R.id.brightness_bar);
            brightnessbar.setBackground(ContextCompat.getDrawable(BrightnessBarsPixel.this, (int) pack.get(i)[1]));

            ImageView auto_brightness_icon = list.findViewById(R.id.auto_brightness_icon);
            auto_brightness_icon.setBackground(ContextCompat.getDrawable(BrightnessBarsPixel.this, (int) pack.get(i)[2]));

            container.addView(list);
        }
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}