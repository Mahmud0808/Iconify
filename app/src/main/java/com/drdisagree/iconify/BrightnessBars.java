package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class BrightnessBars extends AppCompatActivity {

    private static final BigDecimal MAX = BigDecimal.valueOf(10000);

    private ViewGroup container;
    private LinearLayout spinner;
    LinearLayout[] Container;
    LinearLayout RoundedClipContainer, LessRoundedClipContainer, RoundedContainer, LessRoundedContainer;
    Button RoundedClip_Enable, RoundedClip_Disable, LessRoundedClip_Enable, LessRoundedClip_Disable, Rounded_Enable, Rounded_Disable, LessRounded_Enable, LessRounded_Disable;
    ImageButton RoundedClip_Auto_Bb, LessRoundedClip_Auto_Bb, Rounded_Auto_Bb, LessRounded_Auto_Bb;
    ImageView RoundedClip_Bb, LessRoundedClip_Bb, Rounded_Bb, LessRounded_Bb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.brightness_bars);

        // Header
        TextView header = findViewById(R.id.header);
        header.setText("Brightness Bar");

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

        // List of Brightness Bar
        Container = new LinearLayout[]{RoundedClipContainer, LessRoundedClipContainer, RoundedContainer, LessRoundedContainer};

        // Enable onClick event
        enableOnClickListener(RoundedClipContainer, RoundedClip_Enable, RoundedClip_Disable, "roundedclip", 1);
        enableOnClickListener(LessRoundedClipContainer, LessRoundedClip_Enable, LessRoundedClip_Disable, "lessroundedclip", 2);
        enableOnClickListener(RoundedContainer, Rounded_Enable, Rounded_Disable, "rounded", 3);
        enableOnClickListener(LessRoundedContainer, LessRounded_Enable, LessRounded_Disable, "lessrounded", 4);

        refreshBackground();
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
                }
            }
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        List<String> overlays = OverlayUtils.getOverlayList();
        checkIfApplied(RoundedClipContainer, 1, overlays);
        checkIfApplied(LessRoundedClipContainer, 2, overlays);
        checkIfApplied(RoundedContainer, 3, overlays);
        checkIfApplied(LessRoundedContainer, 4, overlays);
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
                disable_others(key);
                BrightnessInstaller.install_pack(index);
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
                        refreshBackground();
                        // Change button visibility
                        enable.setVisibility(View.GONE);
                        disable.setVisibility(View.VISIBLE);
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
                BrightnessInstaller.disable_pack(index);
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
                    }
                }, 1000);
            }
        });
    }

    // Function to disable other packs if one is applied
    private void disable_others(String pack) {
        if (Objects.equals(pack, "roundedclip")) {
            PrefConfig.savePrefBool(getApplicationContext(), "lessroundedclip", false);
            PrefConfig.savePrefBool(getApplicationContext(), "rounded", false);
            PrefConfig.savePrefBool(getApplicationContext(), "lessrounded", false);
        } else if (Objects.equals(pack, "lessroundedclip")) {
            PrefConfig.savePrefBool(getApplicationContext(), "roundedclip", false);
            PrefConfig.savePrefBool(getApplicationContext(), "rounded", false);
            PrefConfig.savePrefBool(getApplicationContext(), "lessrounded", false);
        } else if (Objects.equals(pack, "rounded")) {
            PrefConfig.savePrefBool(getApplicationContext(), "roundedclip", false);
            PrefConfig.savePrefBool(getApplicationContext(), "lessroundedclip", false);
            PrefConfig.savePrefBool(getApplicationContext(), "lessrounded", false);
        } else if (Objects.equals(pack, "lessrounded")) {
            PrefConfig.savePrefBool(getApplicationContext(), "rounded", false);
            PrefConfig.savePrefBool(getApplicationContext(), "roundedclip", false);
            PrefConfig.savePrefBool(getApplicationContext(), "lessroundedclip", false);
        }
    }

    // Function to change applied pack's bg
    private void checkIfApplied(LinearLayout layout, int brightnessbar, List<String> overlays) {
        if (OverlayUtils.isOverlayEnabled(overlays, "IconifyComponentBB" + brightnessbar + ".overlay")) {
            if (brightnessbar == 1)
                PrefConfig.savePrefBool(this, "roundedclip", true);
            else if (brightnessbar == 2)
                PrefConfig.savePrefBool(this, "lessroundedclip", true);
            else if (brightnessbar == 3)
                PrefConfig.savePrefBool(this, "rounded", true);
            else if (brightnessbar == 4)
                PrefConfig.savePrefBool(this, "lessrounded", true);
            background(layout.getId(), R.drawable.container_selected);
        } else {
            if (brightnessbar == 1)
                PrefConfig.savePrefBool(this, "roundedclip", false);
            else if (brightnessbar == 2)
                PrefConfig.savePrefBool(this, "lessroundedclip", false);
            else if (brightnessbar == 3)
                PrefConfig.savePrefBool(this, "rounded", false);
            else if (brightnessbar == 4)
                PrefConfig.savePrefBool(this, "lessrounded", false);
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
        ImageButton auto_bb = list.findViewById(R.id.auto_brightness_icon);

        list.setId(id);
        name.setText(title);

        enable.setId(enableid);
        disable.setId(disableid);

        bb.setId(bb_id);
        auto_bb.setId(auto_bb_id);

        container.addView(list);
    }
}