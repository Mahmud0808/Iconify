package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BrightnessBars extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.brightness_bars);

        // Header
        TextView header = findViewById(R.id.header);
        header.setText("Brightness Bar");

        LinearLayout bb = findViewById(R.id.list_option_brightnessbar);
        if (OverlayUtils.isOverlayEnabled("IconifyComponentIPAS1.overlay"))
            bb.setVisibility(View.VISIBLE);
        else
            bb.setVisibility(View.GONE);
    }
}