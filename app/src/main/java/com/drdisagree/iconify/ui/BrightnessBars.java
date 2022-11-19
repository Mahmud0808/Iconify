package com.drdisagree.iconify.ui;

import android.content.Intent;
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

public class BrightnessBars extends AppCompatActivity {

    private static final String ROUNDED_CLIP_KEY = "IconifyComponentBBN1.overlay";
    private static final String ROUNDED_BAR_KEY = "IconifyComponentBBN2.overlay";
    private static final String DOUBLE_LAYER_KEY = "IconifyComponentBBN3.overlay";
    private static final String SHADED_LAYER_KEY = "IconifyComponentBBN4.overlay";
    private static final String OUTLINE_KEY = "IconifyComponentBBN5.overlay";
    private static final String LEAFY_OUTLINE_KEY = "IconifyComponentBBN6.overlay";
    private static final String NEUMORPH_KEY = "IconifyComponentBBN7.overlay";
    private static final String INLINE_KEY = "IconifyComponentBBN8.overlay";
    private static final String NEUMORPH_OUTLINE_KEY = "IconifyComponentBBN9.overlay";

    LinearLayout[] Container;
    LinearLayout RoundedClipContainer, RoundedContainer, DoubleLayerContainer, ShadedLayerContainer, OutlineContainer, LeafyOutlineContainer, NeumorphContainer, InlineContainer, NeumorphOutlineContainer;
    Button RoundedClip_Enable, RoundedClip_Disable, Rounded_Enable, Rounded_Disable, DoubleLayer_Enable, DoubleLayer_Disable, ShadedLayer_Enable, ShadedLayer_Disable, Outline_Enable, Outline_Disable, LeafyOutline_Enable, LeafyOutline_Disable, Neumorph_Enable, Neumorph_Disable, Inline_Enable, Inline_Disable, NeumorphOutline_Enable, NeumorphOutline_Disable;
    ImageView RoundedClip_Auto_Bb, Rounded_Auto_Bb, DoubleLayer_Auto_Bb, ShadedLayer_Auto_Bb, Outline_Auto_Bb, LeafyOutline_Auto_Bb, Neumorph_Auto_Bb, Inline_Auto_Bb, NeumorphOutline_Auto_Bb;
    ImageView RoundedClip_Bb, Rounded_Bb, DoubleLayer_Bb, ShadedLayer_Bb, Outline_Bb, LeafyOutline_Bb, Neumorph_Bb, Inline_Bb, NeumorphOutline_Bb;
    private ViewGroup container;
    private LinearLayout spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.brightness_bars);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("Brightness Bar");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Brightnessbar pixel item on click
        LinearLayout brightness_bar_pixel = findViewById(R.id.brightness_bar_pixel);
        brightness_bar_pixel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BrightnessBars.this, BrightnessBarsPixel.class);
                startActivity(intent);
            }
        });

        // Progressbar while enabling or disabling pack
        spinner = findViewById(R.id.progressBar_BrightnessBar);

        // Don't show progressbar on opening page
        spinner.setVisibility(View.GONE);

        // Brightness Bar list items
        container = (ViewGroup) findViewById(R.id.brightness_bars_list);

        // Brightness Bar add items in list
        addItem(R.id.brightnessBar_roundedClip_container, R.id.brightnessBar_roundedClip_bb, R.id.brightnessBar_roundedClip_auto_bb, "Rounded Clip", R.id.brightnessBar_roundedClip_enable, R.id.brightnessBar_roundedClip_disable);
        addItem(R.id.brightnessBar_rounded_container, R.id.brightnessBar_rounded_bb, R.id.brightnessBar_rounded_auto_bb, "Rounded Bar", R.id.brightnessBar_rounded_enable, R.id.brightnessBar_rounded_disable);
        addItem(R.id.brightnessBar_doubleLayer_container, R.id.brightnessBar_doubleLayer_bb, R.id.brightnessBar_doubleLayer_auto_bb, "Double Layer", R.id.brightnessBar_doubleLayer_enable, R.id.brightnessBar_doubleLayer_disable);
        addItem(R.id.brightnessBar_shadedLayer_container, R.id.brightnessBar_shadedLayer_bb, R.id.brightnessBar_shadedLayer_auto_bb, "Shaded Layer", R.id.brightnessBar_shadedLayer_enable, R.id.brightnessBar_shadedLayer_disable);
        addItem(R.id.brightnessBar_outline_container, R.id.brightnessBar_outline_bb, R.id.brightnessBar_outline_auto_bb, "Outline", R.id.brightnessBar_outline_enable, R.id.brightnessBar_outline_disable);
        addItem(R.id.brightnessBar_leafy_outline_container, R.id.brightnessBar_leafy_outline_bb, R.id.brightnessBar_leafy_outline_auto_bb, "Leafy Outline", R.id.brightnessBar_leafy_outline_enable, R.id.brightnessBar_leafy_outline_disable);
        addItem(R.id.brightnessBar_neumorph_container, R.id.brightnessBar_neumorph_bb, R.id.brightnessBar_neumorph_auto_bb, "Neumorph", R.id.brightnessBar_neumorph_enable, R.id.brightnessBar_neumorph_disable);
        addItem(R.id.brightnessBar_neumorph_outline_container, R.id.brightnessBar_neumorph_outline_bb, R.id.brightnessBar_neumorph_outline_auto_bb, "Neumorph Outline", R.id.brightnessBar_neumorph_outline_enable, R.id.brightnessBar_neumorph_outline_disable);
        addItem(R.id.brightnessBar_inline_container, R.id.brightnessBar_inline_bb, R.id.brightnessBar_inline_auto_bb, "Inline", R.id.brightnessBar_inline_enable, R.id.brightnessBar_inline_disable);

        // Rounded Clip
        RoundedClipContainer = findViewById(R.id.brightnessBar_roundedClip_container);
        RoundedClip_Enable = findViewById(R.id.brightnessBar_roundedClip_enable);
        RoundedClip_Disable = findViewById(R.id.brightnessBar_roundedClip_disable);
        RoundedClip_Bb = findViewById(R.id.brightnessBar_roundedClip_bb);
        RoundedClip_Bb.setBackground(ContextCompat.getDrawable(BrightnessBars.this, R.drawable.bb_roundedclip));
        RoundedClip_Auto_Bb = findViewById(R.id.brightnessBar_roundedClip_auto_bb);
        RoundedClip_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_roundedclip));

        // Rounded Bar
        RoundedContainer = findViewById(R.id.brightnessBar_rounded_container);
        Rounded_Enable = findViewById(R.id.brightnessBar_rounded_enable);
        Rounded_Disable = findViewById(R.id.brightnessBar_rounded_disable);
        Rounded_Bb = findViewById(R.id.brightnessBar_rounded_bb);
        Rounded_Bb.setBackground(ContextCompat.getDrawable(BrightnessBars.this, R.drawable.bb_rounded));
        Rounded_Auto_Bb = findViewById(R.id.brightnessBar_rounded_auto_bb);
        Rounded_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_rounded));

        // Double Layer
        DoubleLayerContainer = findViewById(R.id.brightnessBar_doubleLayer_container);
        DoubleLayer_Enable = findViewById(R.id.brightnessBar_doubleLayer_enable);
        DoubleLayer_Disable = findViewById(R.id.brightnessBar_doubleLayer_disable);
        DoubleLayer_Bb = findViewById(R.id.brightnessBar_doubleLayer_bb);
        DoubleLayer_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_double_layer));
        DoubleLayer_Auto_Bb = findViewById(R.id.brightnessBar_doubleLayer_auto_bb);
        DoubleLayer_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_double_layer));

        // Shaded Layer
        ShadedLayerContainer = findViewById(R.id.brightnessBar_shadedLayer_container);
        ShadedLayer_Enable = findViewById(R.id.brightnessBar_shadedLayer_enable);
        ShadedLayer_Disable = findViewById(R.id.brightnessBar_shadedLayer_disable);
        ShadedLayer_Bb = findViewById(R.id.brightnessBar_shadedLayer_bb);
        ShadedLayer_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_shaded_layer));
        ShadedLayer_Auto_Bb = findViewById(R.id.brightnessBar_shadedLayer_auto_bb);
        ShadedLayer_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_shaded_layer));

        // Outline
        OutlineContainer = findViewById(R.id.brightnessBar_outline_container);
        Outline_Enable = findViewById(R.id.brightnessBar_outline_enable);
        Outline_Disable = findViewById(R.id.brightnessBar_outline_disable);
        Outline_Bb = findViewById(R.id.brightnessBar_outline_bb);
        Outline_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_outline));
        Outline_Auto_Bb = findViewById(R.id.brightnessBar_outline_auto_bb);
        Outline_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_outline));

        // Leafy Outline
        LeafyOutlineContainer = findViewById(R.id.brightnessBar_leafy_outline_container);
        LeafyOutline_Enable = findViewById(R.id.brightnessBar_leafy_outline_enable);
        LeafyOutline_Disable = findViewById(R.id.brightnessBar_leafy_outline_disable);
        LeafyOutline_Bb = findViewById(R.id.brightnessBar_leafy_outline_bb);
        LeafyOutline_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_leafy_outline));
        LeafyOutline_Auto_Bb = findViewById(R.id.brightnessBar_leafy_outline_auto_bb);
        LeafyOutline_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_leafy_outline));

        // Neumorph
        NeumorphContainer = findViewById(R.id.brightnessBar_neumorph_container);
        Neumorph_Enable = findViewById(R.id.brightnessBar_neumorph_enable);
        Neumorph_Disable = findViewById(R.id.brightnessBar_neumorph_disable);
        Neumorph_Bb = findViewById(R.id.brightnessBar_neumorph_bb);
        Neumorph_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_neumorph));
        Neumorph_Auto_Bb = findViewById(R.id.brightnessBar_neumorph_auto_bb);
        Neumorph_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_neumorph));

        // Neumorph Outline
        NeumorphOutlineContainer = findViewById(R.id.brightnessBar_neumorph_outline_container);
        NeumorphOutline_Enable = findViewById(R.id.brightnessBar_neumorph_outline_enable);
        NeumorphOutline_Disable = findViewById(R.id.brightnessBar_neumorph_outline_disable);
        NeumorphOutline_Bb = findViewById(R.id.brightnessBar_neumorph_outline_bb);
        NeumorphOutline_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_neumorph_outline));
        NeumorphOutline_Auto_Bb = findViewById(R.id.brightnessBar_neumorph_outline_auto_bb);
        NeumorphOutline_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_neumorph_outline));

        // Inline
        InlineContainer = findViewById(R.id.brightnessBar_inline_container);
        Inline_Enable = findViewById(R.id.brightnessBar_inline_enable);
        Inline_Disable = findViewById(R.id.brightnessBar_inline_disable);
        Inline_Bb = findViewById(R.id.brightnessBar_inline_bb);
        Inline_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_inline));
        Inline_Auto_Bb = findViewById(R.id.brightnessBar_inline_auto_bb);
        Inline_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_rounded));

        // List of Brightness Bar
        Container = new LinearLayout[]{RoundedClipContainer, RoundedContainer, DoubleLayerContainer, ShadedLayerContainer, OutlineContainer, LeafyOutlineContainer, NeumorphContainer, NeumorphOutlineContainer, InlineContainer};

        // Enable onClick event
        enableOnClickListener(RoundedClipContainer, RoundedClip_Enable, RoundedClip_Disable, ROUNDED_CLIP_KEY, 1);
        enableOnClickListener(RoundedContainer, Rounded_Enable, Rounded_Disable, ROUNDED_BAR_KEY, 2);
        enableOnClickListener(DoubleLayerContainer, DoubleLayer_Enable, DoubleLayer_Disable, DOUBLE_LAYER_KEY, 3);
        enableOnClickListener(ShadedLayerContainer, ShadedLayer_Enable, ShadedLayer_Disable, SHADED_LAYER_KEY, 4);
        enableOnClickListener(OutlineContainer, Outline_Enable, Outline_Disable, OUTLINE_KEY, 5);
        enableOnClickListener(LeafyOutlineContainer, LeafyOutline_Enable, LeafyOutline_Disable, LEAFY_OUTLINE_KEY, 6);
        enableOnClickListener(NeumorphContainer, Neumorph_Enable, Neumorph_Disable, NEUMORPH_KEY, 7);
        enableOnClickListener(InlineContainer, Inline_Enable, Inline_Disable, INLINE_KEY, 8);
        enableOnClickListener(NeumorphOutlineContainer, NeumorphOutline_Enable, NeumorphOutline_Disable, NEUMORPH_OUTLINE_KEY, 9);

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
                if (linearLayout == RoundedClipContainer) {
                    RoundedClip_Enable.setVisibility(View.GONE);
                    RoundedClip_Disable.setVisibility(View.GONE);
                } else if (linearLayout == RoundedContainer) {
                    Rounded_Enable.setVisibility(View.GONE);
                    Rounded_Disable.setVisibility(View.GONE);
                } else if (linearLayout == DoubleLayerContainer) {
                    DoubleLayer_Enable.setVisibility(View.GONE);
                    DoubleLayer_Disable.setVisibility(View.GONE);
                } else if (linearLayout == ShadedLayerContainer) {
                    ShadedLayer_Enable.setVisibility(View.GONE);
                    ShadedLayer_Disable.setVisibility(View.GONE);
                } else if (linearLayout == OutlineContainer) {
                    Outline_Enable.setVisibility(View.GONE);
                    Outline_Disable.setVisibility(View.GONE);
                } else if (linearLayout == LeafyOutlineContainer) {
                    LeafyOutline_Enable.setVisibility(View.GONE);
                    LeafyOutline_Disable.setVisibility(View.GONE);
                } else if (linearLayout == NeumorphContainer) {
                    Neumorph_Enable.setVisibility(View.GONE);
                    Neumorph_Disable.setVisibility(View.GONE);
                } else if (linearLayout == InlineContainer) {
                    Inline_Enable.setVisibility(View.GONE);
                    Inline_Disable.setVisibility(View.GONE);
                } else if (linearLayout == NeumorphOutlineContainer) {
                    NeumorphOutline_Enable.setVisibility(View.GONE);
                    NeumorphOutline_Disable.setVisibility(View.GONE);
                }
            }
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        checkIfApplied(RoundedClipContainer, 1);
        checkIfApplied(RoundedContainer, 2);
        checkIfApplied(DoubleLayerContainer, 3);
        checkIfApplied(ShadedLayerContainer, 4);
        checkIfApplied(OutlineContainer, 5);
        checkIfApplied(LeafyOutlineContainer, 6);
        checkIfApplied(NeumorphContainer, 7);
        checkIfApplied(InlineContainer, 8);
        checkIfApplied(NeumorphOutlineContainer, 9);
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
            }, 1000);
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
            }, 1000);
        });
    }

    // Function to disable other packs if one is applied
    private void disable_others(String pack) {
        PrefConfig.savePrefBool(Iconify.getAppContext(), ROUNDED_CLIP_KEY, pack.equals(ROUNDED_CLIP_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), ROUNDED_BAR_KEY, pack.equals(ROUNDED_BAR_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), DOUBLE_LAYER_KEY, pack.equals(DOUBLE_LAYER_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), SHADED_LAYER_KEY, pack.equals(SHADED_LAYER_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), OUTLINE_KEY, pack.equals(OUTLINE_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), LEAFY_OUTLINE_KEY, pack.equals(LEAFY_OUTLINE_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), NEUMORPH_KEY, pack.equals(NEUMORPH_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), INLINE_KEY, pack.equals(INLINE_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), NEUMORPH_OUTLINE_KEY, pack.equals(NEUMORPH_OUTLINE_KEY));
    }

    // Function to change applied pack's bg
    private void checkIfApplied(LinearLayout layout, int brightness) {
        if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentBBN" + brightness + ".overlay")) {
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
        View list = LayoutInflater.from(this).inflate(R.layout.list_option_brightnessbar, container, false);

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