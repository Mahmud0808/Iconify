package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;

public class BrightnessBars extends AppCompatActivity {

    private static final BigDecimal MAX = BigDecimal.valueOf(10000);

    private ViewGroup container;
    private LinearLayout spinner;
    LinearLayout[] Container;
    LinearLayout RoundedClipContainer, LessRoundedClipContainer;
    Button RoundedClip_Enable, RoundedClip_Disable, LessRoundedClip_Enable, LessRoundedClip_Disable;
    ImageButton RoundedClip_Auto_Bb, LessRoundedClip_Auto_Bb;
    ImageView RoundedClip_Bb, LessRoundedClip_Bb;

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

        // Declaration of RoundedClip 3.0
        RoundedClipContainer = findViewById(R.id.brightnessBar_roundedClip_container);
        RoundedClip_Enable = findViewById(R.id.brightnessBar_roundedClip_enable);
        RoundedClip_Disable = findViewById(R.id.brightnessBar_roundedClip_disable);
        RoundedClip_Bb = findViewById(R.id.brightnessBar_roundedClip_bb);
        RoundedClip_Bb.setBackground(ContextCompat.getDrawable(BrightnessBars.this, R.drawable.bb_roundedclip));
        RoundedClip_Auto_Bb = findViewById(R.id.brightnessBar_roundedClip_auto_bb);
        RoundedClip_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_roundedclip));

        // Declaration of MIUI
        LessRoundedClipContainer = findViewById(R.id.brightnessBar_lessRoundedClip_container);
        LessRoundedClip_Enable = findViewById(R.id.brightnessBar_lessRoundedClip_enable);
        LessRoundedClip_Disable = findViewById(R.id.brightnessBar_lessRoundedClip_disable);
        LessRoundedClip_Bb = findViewById(R.id.brightnessBar_lessRoundedClip_bb);
        LessRoundedClip_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.bb_lessroundedclip));
        LessRoundedClip_Auto_Bb = findViewById(R.id.brightnessBar_lessRoundedClip_auto_bb);
        LessRoundedClip_Auto_Bb.setBackground(ContextCompat.getDrawable(this, R.drawable.auto_bb_lessroundedclip));

        // List of Brightness Bar
        Container = new LinearLayout[]{RoundedClipContainer, LessRoundedClipContainer};
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