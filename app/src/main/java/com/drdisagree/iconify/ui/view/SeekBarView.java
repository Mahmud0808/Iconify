package com.drdisagree.iconify.ui.view;

import android.view.HapticFeedbackConstants;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.drdisagree.iconify.R;

public class SeekBarView {

    public static android.widget.SeekBar setCustomSeekbar(LinearLayout container, int min, int max, int def, android.widget.SeekBar.OnSeekBarChangeListener onSeekBarChangeListener) {
        android.widget.SeekBar seekBar = container.findViewById(R.id.seekbar);
        seekBar.setMin(min);
        seekBar.setMax(max);
        seekBar.setProgress(def);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        ImageButton minus = container.findViewById(R.id.minus);
        minus.setOnClickListener(v -> {
            if (seekBar.getMin() < seekBar.getProgress()) {
                seekBar.setProgress(seekBar.getProgress() - 1);
                onSeekBarChangeListener.onStopTrackingTouch(seekBar);
            }
        });

        ImageButton plus = container.findViewById(R.id.plus);
        plus.setOnClickListener(v -> {
            if (seekBar.getMax() > seekBar.getProgress()) {
                seekBar.setProgress(seekBar.getProgress() + 1);
                onSeekBarChangeListener.onStopTrackingTouch(seekBar);
            }
        });

        return seekBar;
    }
}
