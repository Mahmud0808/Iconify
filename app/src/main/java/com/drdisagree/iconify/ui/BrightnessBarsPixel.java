package com.drdisagree.iconify.ui;

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
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.installer.BrightnessPixelInstaller;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class BrightnessBarsPixel extends AppCompatActivity {

    private static final String ROUNDED_CLIP_KEY_PIXEL = "IconifyComponentBBP1.overlay";
    private static final String ROUNDED_BAR_KEY_PIXEL = "IconifyComponentBBP2.overlay";
    private static final String DOUBLE_LAYER_KEY_PIXEL = "IconifyComponentBBP3.overlay";
    private static final String SHADED_LAYER_KEY_PIXEL = "IconifyComponentBBP4.overlay";
    private static final String OUTLINE_KEY_PIXEL = "IconifyComponentBBP5.overlay";
    private static final String LEAFY_OUTLINE_KEY_PIXEL = "IconifyComponentBBP6.overlay";
    private static final String NEUMORPH_KEY_PIXEL = "IconifyComponentBBP7.overlay";
    private static final String INLINE_KEY_PIXEL = "IconifyComponentBBP8.overlay";
    private static final String NEUMORPH_OUTLINE_KEY_PIXEL = "IconifyComponentBBP9.overlay";

    LinearLayout[] Container;
    LinearLayout RoundedClipContainer, RoundedContainer, DoubleLayerContainer, ShadedLayerContainer, OutlineContainer, LeafyOutlineContainer, NeumorphContainer, InlineContainer, NeumorphOutlineContainer;
    Button RoundedClip_Enable, RoundedClip_Disable, Rounded_Enable, Rounded_Disable, DoubleLayer_Enable, DoubleLayer_Disable, ShadedLayer_Enable, ShadedLayer_Disable, Outline_Enable, Outline_Disable, LeafyOutline_Enable, LeafyOutline_Disable, Neumorph_Enable, Neumorph_Disable, Inline_Enable, Inline_Disable, NeumorphOutline_Enable, NeumorphOutline_Disable;
    ImageView RoundedClip_Auto_Bb, Rounded_Auto_Bb, DoubleLayer_Auto_Bb, ShadedLayer_Auto_Bb, Outline_Auto_Bb, LeafyOutline_Auto_Bb, Neumorph_Auto_Bb, Inline_Auto_Bb, NeumorphOutline_Auto_Bb;
    ImageView RoundedClip_Bb, Rounded_Bb, DoubleLayer_Bb, ShadedLayer_Bb, Outline_Bb, LeafyOutline_Bb, Neumorph_Bb, Inline_Bb, NeumorphOutline_Bb;
    private ViewGroup container;
    LoadingDialog loadingDialog;

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
        RoundedClip_Bb.setBackground(ContextCompat.getDrawable(BrightnessBarsPixel.this, R.drawable.bb_roundedclip_pixel));
        RoundedClip_Auto_Bb = findViewById(R.id.brightnessBar_roundedClip_auto_bb);
        RoundedClip_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_roundedclip_pixel));

        // Rounded Bar
        RoundedContainer = findViewById(R.id.brightnessBar_rounded_container);
        Rounded_Enable = findViewById(R.id.brightnessBar_rounded_enable);
        Rounded_Disable = findViewById(R.id.brightnessBar_rounded_disable);
        Rounded_Bb = findViewById(R.id.brightnessBar_rounded_bb);
        Rounded_Bb.setBackground(ContextCompat.getDrawable(BrightnessBarsPixel.this, R.drawable.bb_rounded_pixel));
        Rounded_Auto_Bb = findViewById(R.id.brightnessBar_rounded_auto_bb);
        Rounded_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_rounded_pixel));

        // Double Layer
        DoubleLayerContainer = findViewById(R.id.brightnessBar_doubleLayer_container);
        DoubleLayer_Enable = findViewById(R.id.brightnessBar_doubleLayer_enable);
        DoubleLayer_Disable = findViewById(R.id.brightnessBar_doubleLayer_disable);
        DoubleLayer_Bb = findViewById(R.id.brightnessBar_doubleLayer_bb);
        DoubleLayer_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_double_layer_pixel));
        DoubleLayer_Auto_Bb = findViewById(R.id.brightnessBar_doubleLayer_auto_bb);
        DoubleLayer_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_double_layer_pixel));

        // Shaded Layer
        ShadedLayerContainer = findViewById(R.id.brightnessBar_shadedLayer_container);
        ShadedLayer_Enable = findViewById(R.id.brightnessBar_shadedLayer_enable);
        ShadedLayer_Disable = findViewById(R.id.brightnessBar_shadedLayer_disable);
        ShadedLayer_Bb = findViewById(R.id.brightnessBar_shadedLayer_bb);
        ShadedLayer_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_shaded_layer_pixel));
        ShadedLayer_Auto_Bb = findViewById(R.id.brightnessBar_shadedLayer_auto_bb);
        ShadedLayer_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_shaded_layer_pixel));

        // Outline
        OutlineContainer = findViewById(R.id.brightnessBar_outline_container);
        Outline_Enable = findViewById(R.id.brightnessBar_outline_enable);
        Outline_Disable = findViewById(R.id.brightnessBar_outline_disable);
        Outline_Bb = findViewById(R.id.brightnessBar_outline_bb);
        Outline_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_outline_pixel));
        Outline_Auto_Bb = findViewById(R.id.brightnessBar_outline_auto_bb);
        Outline_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_outline_pixel));

        // Leafy Outline
        LeafyOutlineContainer = findViewById(R.id.brightnessBar_leafy_outline_container);
        LeafyOutline_Enable = findViewById(R.id.brightnessBar_leafy_outline_enable);
        LeafyOutline_Disable = findViewById(R.id.brightnessBar_leafy_outline_disable);
        LeafyOutline_Bb = findViewById(R.id.brightnessBar_leafy_outline_bb);
        LeafyOutline_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_leafy_outline_pixel));
        LeafyOutline_Auto_Bb = findViewById(R.id.brightnessBar_leafy_outline_auto_bb);
        LeafyOutline_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_leafy_outline_pixel));

        // Neumorph
        NeumorphContainer = findViewById(R.id.brightnessBar_neumorph_container);
        Neumorph_Enable = findViewById(R.id.brightnessBar_neumorph_enable);
        Neumorph_Disable = findViewById(R.id.brightnessBar_neumorph_disable);
        Neumorph_Bb = findViewById(R.id.brightnessBar_neumorph_bb);
        Neumorph_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_neumorph_pixel));
        Neumorph_Auto_Bb = findViewById(R.id.brightnessBar_neumorph_auto_bb);
        Neumorph_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_neumorph_pixel));

        // Neumorph Outline
        NeumorphOutlineContainer = findViewById(R.id.brightnessBar_neumorph_outline_container);
        NeumorphOutline_Enable = findViewById(R.id.brightnessBar_neumorph_outline_enable);
        NeumorphOutline_Disable = findViewById(R.id.brightnessBar_neumorph_outline_disable);
        NeumorphOutline_Bb = findViewById(R.id.brightnessBar_neumorph_outline_bb);
        NeumorphOutline_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_neumorph_outline_pixel));
        NeumorphOutline_Auto_Bb = findViewById(R.id.brightnessBar_neumorph_outline_auto_bb);
        NeumorphOutline_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_neumorph_outline_pixel));

        // Inline
        InlineContainer = findViewById(R.id.brightnessBar_inline_container);
        Inline_Enable = findViewById(R.id.brightnessBar_inline_enable);
        Inline_Disable = findViewById(R.id.brightnessBar_inline_disable);
        Inline_Bb = findViewById(R.id.brightnessBar_inline_bb);
        Inline_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_inline_pixel));
        Inline_Auto_Bb = findViewById(R.id.brightnessBar_inline_auto_bb);
        Inline_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_rounded_pixel));

        // List of Brightness Bar
        Container = new LinearLayout[]{RoundedClipContainer, RoundedContainer, DoubleLayerContainer, ShadedLayerContainer, OutlineContainer, LeafyOutlineContainer, NeumorphContainer, NeumorphOutlineContainer, InlineContainer};

        // Enable onClick event
        enableOnClickListener(RoundedClipContainer, RoundedClip_Enable, RoundedClip_Disable, ROUNDED_CLIP_KEY_PIXEL, 1);
        enableOnClickListener(RoundedContainer, Rounded_Enable, Rounded_Disable, ROUNDED_BAR_KEY_PIXEL, 2);
        enableOnClickListener(DoubleLayerContainer, DoubleLayer_Enable, DoubleLayer_Disable, DOUBLE_LAYER_KEY_PIXEL, 3);
        enableOnClickListener(ShadedLayerContainer, ShadedLayer_Enable, ShadedLayer_Disable, SHADED_LAYER_KEY_PIXEL, 4);
        enableOnClickListener(OutlineContainer, Outline_Enable, Outline_Disable, OUTLINE_KEY_PIXEL, 5);
        enableOnClickListener(LeafyOutlineContainer, LeafyOutline_Enable, LeafyOutline_Disable, LEAFY_OUTLINE_KEY_PIXEL, 6);
        enableOnClickListener(NeumorphContainer, Neumorph_Enable, Neumorph_Disable, NEUMORPH_KEY_PIXEL, 7);
        enableOnClickListener(InlineContainer, Inline_Enable, Inline_Disable, INLINE_KEY_PIXEL, 8);
        enableOnClickListener(NeumorphOutlineContainer, NeumorphOutline_Enable, NeumorphOutline_Disable, NEUMORPH_OUTLINE_KEY_PIXEL, 9);

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
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                disable_others(key);
                BrightnessPixelInstaller.install_pack(index);

                runOnUiThread(() -> {
                    PrefConfig.savePrefBool(Iconify.getAppContext(), key, true);

                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Change background to selected
                        background(layout.getId(), R.drawable.container_selected);

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
                BrightnessPixelInstaller.disable_pack(index);

                runOnUiThread(() -> {
                    PrefConfig.savePrefBool(Iconify.getAppContext(), key, false);

                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Change background to selected
                        background(layout.getId(), R.drawable.container);

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

    // Function to disable other packs if one is applied
    private void disable_others(String pack) {
        PrefConfig.savePrefBool(Iconify.getAppContext(), ROUNDED_CLIP_KEY_PIXEL, pack.equals(ROUNDED_CLIP_KEY_PIXEL));
        PrefConfig.savePrefBool(Iconify.getAppContext(), ROUNDED_BAR_KEY_PIXEL, pack.equals(ROUNDED_BAR_KEY_PIXEL));
        PrefConfig.savePrefBool(Iconify.getAppContext(), DOUBLE_LAYER_KEY_PIXEL, pack.equals(DOUBLE_LAYER_KEY_PIXEL));
        PrefConfig.savePrefBool(Iconify.getAppContext(), SHADED_LAYER_KEY_PIXEL, pack.equals(SHADED_LAYER_KEY_PIXEL));
        PrefConfig.savePrefBool(Iconify.getAppContext(), OUTLINE_KEY_PIXEL, pack.equals(OUTLINE_KEY_PIXEL));
        PrefConfig.savePrefBool(Iconify.getAppContext(), LEAFY_OUTLINE_KEY_PIXEL, pack.equals(LEAFY_OUTLINE_KEY_PIXEL));
        PrefConfig.savePrefBool(Iconify.getAppContext(), NEUMORPH_KEY_PIXEL, pack.equals(NEUMORPH_KEY_PIXEL));
        PrefConfig.savePrefBool(Iconify.getAppContext(), INLINE_KEY_PIXEL, pack.equals(INLINE_KEY_PIXEL));
        PrefConfig.savePrefBool(Iconify.getAppContext(), NEUMORPH_OUTLINE_KEY_PIXEL, pack.equals(NEUMORPH_OUTLINE_KEY_PIXEL));
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

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}