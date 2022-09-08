package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.topjohnwu.superuser.Shell;

import java.util.List;

public class MediaPlayer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_player);

        // Header
        TextView header = findViewById(R.id.header);
        header.setText("Media Player");

        List<String> overlay = OverlayUtils.getOverlayList();

        Switch mp_accent = findViewById(R.id.mp_accent);
        Switch mp_system = findViewById(R.id.mp_system);
        Switch mp_pitch_black = findViewById(R.id.mp_pitch_black);

        if (OverlayUtils.isOverlayEnabled(overlay, "IconifyComponentMPA.overlay"))
            mp_accent.setChecked(true);
        else
            mp_accent.setChecked(false);

        mp_accent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    OverlayUtils.enableOverlay(overlay, "IconifyComponentMPA.overlay");
                    OverlayUtils.disableOverlay("IconifyComponentMPS.overlay");
                    OverlayUtils.disableOverlay("IconifyComponentMPB.overlay");
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentMPA.overlay");
                }
            }
        });

        if (OverlayUtils.isOverlayEnabled(overlay, "IconifyComponentMPS.overlay"))
            mp_system.setChecked(true);
        else
            mp_system.setChecked(false);

        mp_system.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    OverlayUtils.enableOverlay(overlay, "IconifyComponentMPS.overlay");
                    OverlayUtils.disableOverlay("IconifyComponentMPA.overlay");
                    OverlayUtils.disableOverlay("IconifyComponentMPB.overlay");
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentMPS.overlay");
                }
            }
        });

        if (OverlayUtils.isOverlayEnabled(overlay, "IconifyComponentMPB.overlay"))
            mp_pitch_black.setChecked(true);
        else
            mp_pitch_black.setChecked(false);

        mp_pitch_black.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    OverlayUtils.enableOverlay(overlay, "IconifyComponentMPB.overlay");
                    OverlayUtils.disableOverlay("IconifyComponentMPA.overlay");
                    OverlayUtils.disableOverlay("IconifyComponentMPS.overlay");
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentMPB.overlay");
                }
            }
        });
    }
}