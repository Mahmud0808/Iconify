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

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.installer.BrightnessInstaller;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class BrightnessBars extends AppCompatActivity {

    private static final String ROUNDED_CLIP_KEY = "IconifyComponentBB1.overlay";
    private static final String LESS_ROUNDED_CLIP_KEY = "IconifyComponentBB2.overlay";
    private static final String ROUNDED_BAR_KEY = "IconifyComponentBB3.overlay";
    private static final String LESS_ROUNDED_BAR_KEY = "IconifyComponentBB4.overlay";
    private static final String DOUBLE_LAYER_KEY = "IconifyComponentBB5.overlay";
    private static final String SHADED_LAYER_KEY = "IconifyComponentBB6.overlay";

    private ViewGroup container;
    private LinearLayout spinner;
    LinearLayout[] Container;
    LinearLayout RoundedClipContainer, LessRoundedClipContainer, RoundedContainer, LessRoundedContainer, DoubleLayerContainer, ShadedLayerContainer;
    Button RoundedClip_Enable, RoundedClip_Disable, LessRoundedClip_Enable, LessRoundedClip_Disable, Rounded_Enable, Rounded_Disable, LessRounded_Enable, LessRounded_Disable, DoubleLayer_Enable, DoubleLayer_Disable, ShadedLayer_Enable, ShadedLayer_Disable;
    ImageView RoundedClip_Auto_Bb, LessRoundedClip_Auto_Bb, Rounded_Auto_Bb, LessRounded_Auto_Bb, DoubleLayer_Auto_Bb, ShadedLayer_Auto_Bb;
    ImageView RoundedClip_Bb, LessRoundedClip_Bb, Rounded_Bb, LessRounded_Bb, DoubleLayer_Bb, ShadedLayer_Bb;

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

        // Progressbar while enabling or disabling pack
        spinner = findViewById(R.id.progressBar_BrightnessBar);

        // Don't show progressbar on opening page
        spinner.setVisibility(View.GONE);

        // Brightness Bar list items
        container = (ViewGroup) findViewById(R.id.brightness_bars_list);

        // Brightness Bar add items in list
        addItem(R.id.brightnessBar_roundedClip_container, R.id.brightnessBar_roundedClip_bb, R.id.brightnessBar_roundedClip_auto_bb, "Rounded Clip", R.id.brightnessBar_roundedClip_enable, R.id.brightnessBar_roundedClip_disable);
        addItem(R.id.brightnessBar_lessRoundedClip_container, R.id.brightnessBar_lessRoundedClip_bb, R.id.brightnessBar_lessRoundedClip_auto_bb, "Less Rounded Clip", R.id.brightnessBar_lessRoundedClip_enable, R.id.brightnessBar_lessRoundedClip_disable);
        addItem(R.id.brightnessBar_rounded_container, R.id.brightnessBar_rounded_bb, R.id.brightnessBar_rounded_auto_bb, "Rounded Bar", R.id.brightnessBar_rounded_enable, R.id.brightnessBar_rounded_disable);
        addItem(R.id.brightnessBar_lessRounded_container, R.id.brightnessBar_lessRounded_bb, R.id.brightnessBar_lessRounded_auto_bb, "Less Rounded Bar", R.id.brightnessBar_lessRounded_enable, R.id.brightnessBar_lessRounded_disable);
        addItem(R.id.brightnessBar_doubleLayer_container, R.id.brightnessBar_doubleLayer_bb, R.id.brightnessBar_doubleLayer_auto_bb, "Double Layer", R.id.brightnessBar_doubleLayer_enable, R.id.brightnessBar_doubleLayer_disable);
        addItem(R.id.brightnessBar_shadedLayer_container, R.id.brightnessBar_shadedLayer_bb, R.id.brightnessBar_shadedLayer_auto_bb, "Shaded Layer", R.id.brightnessBar_shadedLayer_enable, R.id.brightnessBar_shadedLayer_disable);

        // Rounded Clip
        RoundedClipContainer = findViewById(R.id.brightnessBar_roundedClip_container);
        RoundedClip_Enable = findViewById(R.id.brightnessBar_roundedClip_enable);
        RoundedClip_Disable = findViewById(R.id.brightnessBar_roundedClip_disable);
        RoundedClip_Bb = findViewById(R.id.brightnessBar_roundedClip_bb);
        RoundedClip_Bb.setBackground(ContextCompat.getDrawable(BrightnessBars.this, R.drawable.bb_roundedclip));
        RoundedClip_Auto_Bb = findViewById(R.id.brightnessBar_roundedClip_auto_bb);
        RoundedClip_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_roundedclip));

        // Less Rounded Clip
        LessRoundedClipContainer = findViewById(R.id.brightnessBar_lessRoundedClip_container);
        LessRoundedClip_Enable = findViewById(R.id.brightnessBar_lessRoundedClip_enable);
        LessRoundedClip_Disable = findViewById(R.id.brightnessBar_lessRoundedClip_disable);
        LessRoundedClip_Bb = findViewById(R.id.brightnessBar_lessRoundedClip_bb);
        LessRoundedClip_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_lessroundedclip));
        LessRoundedClip_Auto_Bb = findViewById(R.id.brightnessBar_lessRoundedClip_auto_bb);
        LessRoundedClip_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_lessroundedclip));

        // Rounded Bar
        RoundedContainer = findViewById(R.id.brightnessBar_rounded_container);
        Rounded_Enable = findViewById(R.id.brightnessBar_rounded_enable);
        Rounded_Disable = findViewById(R.id.brightnessBar_rounded_disable);
        Rounded_Bb = findViewById(R.id.brightnessBar_rounded_bb);
        Rounded_Bb.setBackground(ContextCompat.getDrawable(BrightnessBars.this, R.drawable.bb_rounded));
        Rounded_Auto_Bb = findViewById(R.id.brightnessBar_rounded_auto_bb);
        Rounded_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_rounded));

        // Less Rounded Bar
        LessRoundedContainer = findViewById(R.id.brightnessBar_lessRounded_container);
        LessRounded_Enable = findViewById(R.id.brightnessBar_lessRounded_enable);
        LessRounded_Disable = findViewById(R.id.brightnessBar_lessRounded_disable);
        LessRounded_Bb = findViewById(R.id.brightnessBar_lessRounded_bb);
        LessRounded_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_lessrounded));
        LessRounded_Auto_Bb = findViewById(R.id.brightnessBar_lessRounded_auto_bb);
        LessRounded_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_lessrounded));

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

        // List of Brightness Bar
        Container = new LinearLayout[]{RoundedClipContainer, LessRoundedClipContainer, RoundedContainer, LessRoundedContainer, DoubleLayerContainer, ShadedLayerContainer};

        // Enable onClick event
        enableOnClickListener(RoundedClipContainer, RoundedClip_Enable, RoundedClip_Disable, ROUNDED_CLIP_KEY, 1);
        enableOnClickListener(LessRoundedClipContainer, LessRoundedClip_Enable, LessRoundedClip_Disable, LESS_ROUNDED_CLIP_KEY, 2);
        enableOnClickListener(RoundedContainer, Rounded_Enable, Rounded_Disable, ROUNDED_BAR_KEY, 3);
        enableOnClickListener(LessRoundedContainer, LessRounded_Enable, LessRounded_Disable, LESS_ROUNDED_BAR_KEY, 4);
        enableOnClickListener(DoubleLayerContainer, DoubleLayer_Enable, DoubleLayer_Disable, DOUBLE_LAYER_KEY, 5);
        enableOnClickListener(ShadedLayerContainer, ShadedLayer_Enable, ShadedLayer_Disable, SHADED_LAYER_KEY, 6);

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
                } else if (linearLayout == LessRoundedClipContainer) {
                    LessRoundedClip_Enable.setVisibility(View.GONE);
                    LessRoundedClip_Disable.setVisibility(View.GONE);
                } else if (linearLayout == RoundedContainer) {
                    Rounded_Enable.setVisibility(View.GONE);
                    Rounded_Disable.setVisibility(View.GONE);
                } else if (linearLayout == LessRoundedContainer) {
                    LessRounded_Enable.setVisibility(View.GONE);
                    LessRounded_Disable.setVisibility(View.GONE);
                } else if (linearLayout == DoubleLayerContainer) {
                    DoubleLayer_Enable.setVisibility(View.GONE);
                    DoubleLayer_Disable.setVisibility(View.GONE);
                } else if (linearLayout == ShadedLayerContainer) {
                    ShadedLayer_Enable.setVisibility(View.GONE);
                    ShadedLayer_Disable.setVisibility(View.GONE);
                }
            }
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        checkIfApplied(RoundedClipContainer, 1);
        checkIfApplied(LessRoundedClipContainer, 2);
        checkIfApplied(RoundedContainer, 3);
        checkIfApplied(LessRoundedContainer, 4);
        checkIfApplied(DoubleLayerContainer, 5);
        checkIfApplied(ShadedLayerContainer, 6);
    }

    // Function for onClick events
    private void enableOnClickListener(LinearLayout layout, Button enable, Button disable, String key, int index) {

        // Set onClick operation for options in list
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLayout(layout);
                if (!PrefConfig.loadPrefBool(getApplicationContext(), key)) {
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
            }
        });

        // Set onClick operation for Enable button
        enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLayout(layout);
                // Show spinner
                spinner.setVisibility(View.VISIBLE);
                // Block touch
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        disable_others(key);
                        BrightnessInstaller.install_pack(index);
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
                PrefConfig.savePrefBool(getApplicationContext(), key, true);
                // Wait 1 second
                spinner.postDelayed(new Runnable() {
                    public void run() {
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
                        Toast.makeText(getApplicationContext(), "Applied", Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }
        });

        // Set onClick operation for Disable button
        disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show spinner
                spinner.setVisibility(View.VISIBLE);
                // Block touch
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        BrightnessInstaller.disable_pack(index);
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
                PrefConfig.savePrefBool(getApplicationContext(), key, false);
                // Wait 1 second
                spinner.postDelayed(new Runnable() {
                    public void run() {
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
                        Toast.makeText(getApplicationContext(), "Disabled", Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }
        });
    }

    // Function to disable other packs if one is applied
    private void disable_others(String pack) {
        if (Objects.equals(pack, ROUNDED_CLIP_KEY)) {
            PrefConfig.savePrefBool(getApplicationContext(), LESS_ROUNDED_CLIP_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), ROUNDED_BAR_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), LESS_ROUNDED_BAR_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), DOUBLE_LAYER_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), SHADED_LAYER_KEY, false);
        } else if (Objects.equals(pack, LESS_ROUNDED_CLIP_KEY)) {
            PrefConfig.savePrefBool(getApplicationContext(), ROUNDED_CLIP_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), ROUNDED_BAR_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), LESS_ROUNDED_BAR_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), DOUBLE_LAYER_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), SHADED_LAYER_KEY, false);
        } else if (Objects.equals(pack, ROUNDED_BAR_KEY)) {
            PrefConfig.savePrefBool(getApplicationContext(), ROUNDED_CLIP_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), LESS_ROUNDED_CLIP_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), LESS_ROUNDED_BAR_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), DOUBLE_LAYER_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), SHADED_LAYER_KEY, false);
        } else if (Objects.equals(pack, LESS_ROUNDED_BAR_KEY)) {
            PrefConfig.savePrefBool(getApplicationContext(), ROUNDED_BAR_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), ROUNDED_CLIP_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), LESS_ROUNDED_CLIP_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), DOUBLE_LAYER_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), SHADED_LAYER_KEY, false);
        } else if (Objects.equals(pack, DOUBLE_LAYER_KEY)) {
            PrefConfig.savePrefBool(getApplicationContext(), ROUNDED_BAR_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), ROUNDED_CLIP_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), LESS_ROUNDED_CLIP_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), LESS_ROUNDED_BAR_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), SHADED_LAYER_KEY, false);
        } else if (Objects.equals(pack, SHADED_LAYER_KEY)) {
            PrefConfig.savePrefBool(getApplicationContext(), ROUNDED_BAR_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), ROUNDED_CLIP_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), LESS_ROUNDED_CLIP_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), DOUBLE_LAYER_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), LESS_ROUNDED_BAR_KEY, false);
        }
    }

    // Function to change applied pack's bg
    private void checkIfApplied(LinearLayout layout, int brightness) {
        if (PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentBB" + brightness + ".overlay")) {
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