package com.drdisagree.iconify.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.installer.BrightnessInstaller;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class BrightnessBarsPixel extends AppCompatActivity {

    private static final String OUTLINE_PIXEL_KEY = "IconifyComponentBBP5.overlay";
    private static final String LEAFY_OUTLINE_PIXEL_KEY = "IconifyComponentBBP6.overlay";

    LinearLayout[] Container;
    LinearLayout OutlinePixelContainer, LeafyOutlinePixelContainer;
    Button OutlinePixel_Enable, OutlinePixel_Disable, LeafyOutlinePixel_Enable, LeafyOutlinePixel_Disable;
    ImageView OutlinePixel_Auto_Bb, LeafyOutlinePixel_Auto_Bb;
    ImageView OutlinePixel_Bb, LeafyOutlinePixel_Bb;
    private ViewGroup container;
    private LinearLayout spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.brightness_bars_pixel);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("Brightness Bar");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Progressbar while enabling or disabling pack
        spinner = findViewById(R.id.progressBar_BrightnessBarPixel);

        // Don't show progressbar on opening page
        spinner.setVisibility(View.GONE);

        // Brightness Bar list items
        container = (ViewGroup) findViewById(R.id.brightness_bars_list_pixel);

        // Brightness Bar add items in list
        addItem(R.id.brightnessBar_outline_pixel_container, R.id.brightnessBar_outline_pixel_bb, R.id.brightnessBar_outline_pixel_auto_bb, "Outline", R.id.brightnessBar_outline_pixel_enable, R.id.brightnessBar_outline_pixel_disable);
        addItem(R.id.brightnessBar_leafy_outline_pixel_container, R.id.brightnessBar_leafy_outline_pixel_bb, R.id.brightnessBar_leafy_outline_pixel_auto_bb, "Leafy Outline", R.id.brightnessBar_leafy_outline_pixel_enable, R.id.brightnessBar_leafy_outline_pixel_disable);

        // Outline [Pixel]
        OutlinePixelContainer = findViewById(R.id.brightnessBar_outline_pixel_container);
        OutlinePixel_Enable = findViewById(R.id.brightnessBar_outline_pixel_enable);
        OutlinePixel_Disable = findViewById(R.id.brightnessBar_outline_pixel_disable);
        OutlinePixel_Bb = findViewById(R.id.brightnessBar_outline_pixel_bb);
        OutlinePixel_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_outline_pixel));
        OutlinePixel_Auto_Bb = findViewById(R.id.brightnessBar_outline_pixel_auto_bb);
        OutlinePixel_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_outline_pixel));
        OutlinePixel_Auto_Bb.setImageResource(R.drawable.ic_brightness_off_white);

        // Leafy Outline [Pixel]
        LeafyOutlinePixelContainer = findViewById(R.id.brightnessBar_leafy_outline_pixel_container);
        LeafyOutlinePixel_Enable = findViewById(R.id.brightnessBar_leafy_outline_pixel_enable);
        LeafyOutlinePixel_Disable = findViewById(R.id.brightnessBar_leafy_outline_pixel_disable);
        LeafyOutlinePixel_Bb = findViewById(R.id.brightnessBar_leafy_outline_pixel_bb);
        LeafyOutlinePixel_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_leafy_outline_pixel));
        LeafyOutlinePixel_Auto_Bb = findViewById(R.id.brightnessBar_leafy_outline_pixel_auto_bb);
        LeafyOutlinePixel_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_leafy_outline_pixel));
        LeafyOutlinePixel_Auto_Bb.setImageResource(R.drawable.ic_brightness_off_white);

        // List of Brightness Bar
        Container = new LinearLayout[]{OutlinePixelContainer, LeafyOutlinePixelContainer};

        // Enable onClick event
        enableOnClickListener(OutlinePixelContainer, OutlinePixel_Enable, OutlinePixel_Disable, OUTLINE_PIXEL_KEY, 5);
        enableOnClickListener(LeafyOutlinePixelContainer, LeafyOutlinePixel_Enable, LeafyOutlinePixel_Disable, LEAFY_OUTLINE_PIXEL_KEY, 6);

        refreshBackground();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Function to check for layout changes
    private void refreshLayout(LinearLayout layout) {
        for (LinearLayout linearLayout : Container) {
            if (!(linearLayout == layout)) {
                if (linearLayout == OutlinePixelContainer) {
                    OutlinePixel_Enable.setVisibility(View.GONE);
                    OutlinePixel_Disable.setVisibility(View.GONE);
                } else if (linearLayout == LeafyOutlinePixelContainer) {
                    LeafyOutlinePixel_Enable.setVisibility(View.GONE);
                    LeafyOutlinePixel_Disable.setVisibility(View.GONE);
                }
            }
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        checkIfApplied(OutlinePixelContainer, 5);
        checkIfApplied(LeafyOutlinePixelContainer, 6);
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
            // Show spinner
            spinner.setVisibility(View.VISIBLE);
            // Block touch
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            Runnable runnable = () -> {
                disable_others(key);
                BrightnessInstaller.install_pack(index);
            };
            Thread thread = new Thread(runnable);
            thread.start();
            PrefConfig.savePrefBool(Iconify.getAppContext(), key, true);
            // Wait 1 second
            spinner.postDelayed(() -> {
                // Hide spinner
                spinner.setVisibility(View.GONE);
                // Unblock touch
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                // Change background to selected
                background(layout.getId(), R.drawable.container_selected);
                // Change button visibility
                enable.setVisibility(View.GONE);
                disable.setVisibility(View.VISIBLE);
                refreshBackground();
                Toast.makeText(Iconify.getAppContext(), "Applied", Toast.LENGTH_SHORT).show();
            }, 500);
        });

        // Set onClick operation for Disable button
        disable.setOnClickListener(v -> {
            // Show spinner
            spinner.setVisibility(View.VISIBLE);
            // Block touch
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            Runnable runnable = () -> BrightnessInstaller.disable_pack(index);
            Thread thread = new Thread(runnable);
            thread.start();
            PrefConfig.savePrefBool(Iconify.getAppContext(), key, false);
            // Wait 1 second
            spinner.postDelayed(() -> {
                // Hide spinner
                spinner.setVisibility(View.GONE);
                // Unblock touch
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                // Change background to selected
                background(layout.getId(), R.drawable.container);
                // Change button visibility
                disable.setVisibility(View.GONE);
                enable.setVisibility(View.VISIBLE);
                refreshBackground();
                Toast.makeText(Iconify.getAppContext(), "Disabled", Toast.LENGTH_SHORT).show();
            }, 500);
        });
    }

    // Function to disable other packs if one is applied
    private void disable_others(String pack) {
        PrefConfig.savePrefBool(Iconify.getAppContext(), OUTLINE_PIXEL_KEY, pack.equals(OUTLINE_PIXEL_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), LEAFY_OUTLINE_PIXEL_KEY, pack.equals(LEAFY_OUTLINE_PIXEL_KEY));
    }

    // Function to change applied pack's bg
    private void checkIfApplied(LinearLayout layout, int brightness) {
        if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentBBP" + brightness + ".overlay")) {
            background(layout.getId(), R.drawable.container_selected);
        } else {
            background(layout.getId(), R.drawable.container);
        }
    }

    // Function to add border for installed pack
    private void background(int id, int drawable) {
        LinearLayout layout = findViewById(id);
        layout.setBackground(ContextCompat.getDrawable(this, drawable));
    }

    private void addItem(int id, int bb_id, int auto_bb_id, String title, int enableid, int disableid) {
        View list = LayoutInflater.from(this).inflate(R.layout.list_option_brightnessbar_pixel, container, false);

        TextView name = list.findViewById(R.id.list_title_brightnessbar);
        Button enable = list.findViewById(R.id.list_button_enable_brightnessbar);
        Button disable = list.findViewById(R.id.list_button_disable_brightnessbar);
        ImageView bb = list.findViewById(R.id.brightness_bar);
        ImageView auto_bb = list.findViewById(R.id.auto_brightness_icon);

        list.setId(id);
        name.setText(title);

        enable.setId(enableid);
        disable.setId(disableid);

        bb.setId(bb_id);
        auto_bb.setId(auto_bb_id);

        container.addView(list);
    }
}