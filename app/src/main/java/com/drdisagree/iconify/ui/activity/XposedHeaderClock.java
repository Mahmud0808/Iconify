package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.HEADER_CLOCK_FONT_TEXT_SCALING;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_SIDEMARGIN;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_STYLE;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_TEXT_WHITE;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_TOPMARGIN;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class XposedHeaderClock extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_header_clock);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_header_clock));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Custom header clock
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_header_clock = findViewById(R.id.enable_header_clock);
        enable_header_clock.setChecked(RPrefs.getBoolean(HEADER_CLOCK_SWITCH, false));
        enable_header_clock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_SWITCH, isChecked);
            new Handler().postDelayed(SystemUtil::doubleToggleDarkTheme, 200);
        });

        // Header clock style
        final Spinner header_clock_style = findViewById(R.id.header_clock_style);
        List<String> hcclock_styles = new ArrayList<>();
        hcclock_styles.add(getResources().getString(R.string.style_0));
        hcclock_styles.add(getResources().getString(R.string.style_1));
        hcclock_styles.add(getResources().getString(R.string.style_2));
        hcclock_styles.add(getResources().getString(R.string.style_3));
        hcclock_styles.add(getResources().getString(R.string.style_4));
        hcclock_styles.add(getResources().getString(R.string.style_5));

        ArrayAdapter<String> hcclock_styles_adapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, hcclock_styles);
        hcclock_styles_adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        header_clock_style.setAdapter(hcclock_styles_adapter);

        final int[] selectedHeaderClock = {RPrefs.getInt(HEADER_CLOCK_STYLE, 0)};
        header_clock_style.setSelection(selectedHeaderClock[0]);
        header_clock_style.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedHeaderClock[0] = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Apply clock
        Button apply_clock = findViewById(R.id.apply_clock);
        apply_clock.setOnClickListener(v -> {
            RPrefs.putInt(HEADER_CLOCK_STYLE, selectedHeaderClock[0]);
            if (RPrefs.getBoolean(HEADER_CLOCK_SWITCH, false)) {
                new Handler().postDelayed(SystemUtil::doubleToggleDarkTheme, 200);
            }
        });

        // Text Scaling
        SeekBar header_clock_textscaling_seekbar = findViewById(R.id.header_clock_textscaling_seekbar);
        header_clock_textscaling_seekbar.setPadding(0, 0, 0, 0);
        TextView header_clock_textscaling_output = findViewById(R.id.header_clock_textscaling_output);
        final int[] textScaling = {RPrefs.getInt(HEADER_CLOCK_FONT_TEXT_SCALING, 10)};
        header_clock_textscaling_output.setText(getResources().getString(R.string.opt_selected) + ' ' + new DecimalFormat("#.#").format(textScaling[0] / 10.0) + "x");
        header_clock_textscaling_seekbar.setProgress(RPrefs.getInt(HEADER_CLOCK_FONT_TEXT_SCALING, 10));
        header_clock_textscaling_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textScaling[0] = progress;
                header_clock_textscaling_output.setText(getResources().getString(R.string.opt_selected) + ' ' + new DecimalFormat("#.#").format(progress / 10.0) + "x");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(HEADER_CLOCK_FONT_TEXT_SCALING, textScaling[0]);
                if (RPrefs.getBoolean(HEADER_CLOCK_SWITCH, false)) {
                    new Handler().postDelayed(SystemUtil::doubleToggleDarkTheme, 200);
                }
            }
        });

        // Header clock side margin
        SeekBar header_clock_side_margin_seekbar = findViewById(R.id.header_clock_side_margin_seekbar);
        header_clock_side_margin_seekbar.setPadding(0, 0, 0, 0);
        TextView header_clock_side_margin_output = findViewById(R.id.header_clock_side_margin_output);
        header_clock_side_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0) + "dp");
        header_clock_side_margin_seekbar.setProgress(RPrefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0));
        final int[] sideMargin = {RPrefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0)};
        header_clock_side_margin_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sideMargin[0] = progress;
                header_clock_side_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(HEADER_CLOCK_SIDEMARGIN, sideMargin[0]);
                if (RPrefs.getBoolean(HEADER_CLOCK_SWITCH, false)) {
                    new Handler().postDelayed(SystemUtil::doubleToggleDarkTheme, 200);
                }
            }
        });

        // Header clock top margin
        SeekBar header_clock_top_margin_seekbar = findViewById(R.id.header_clock_top_margin_seekbar);
        header_clock_top_margin_seekbar.setPadding(0, 0, 0, 0);
        TextView header_clock_top_margin_output = findViewById(R.id.header_clock_top_margin_output);
        header_clock_top_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(HEADER_CLOCK_TOPMARGIN, 8) + "dp");
        header_clock_top_margin_seekbar.setProgress(RPrefs.getInt(HEADER_CLOCK_TOPMARGIN, 8));
        final int[] topMargin = {RPrefs.getInt(HEADER_CLOCK_TOPMARGIN, 8)};
        header_clock_top_margin_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                topMargin[0] = progress;
                header_clock_top_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(HEADER_CLOCK_TOPMARGIN, topMargin[0]);
                if (RPrefs.getBoolean(HEADER_CLOCK_SWITCH, false)) {
                    new Handler().postDelayed(SystemUtil::doubleToggleDarkTheme, 200);
                }
            }
        });

        // Force white text
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_force_white_text = findViewById(R.id.enable_force_white_text);
        enable_force_white_text.setChecked(RPrefs.getBoolean(HEADER_CLOCK_TEXT_WHITE, false));
        enable_force_white_text.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_TEXT_WHITE, isChecked);
            if (RPrefs.getBoolean(HEADER_CLOCK_SWITCH, false)) {
                new Handler().postDelayed(SystemUtil::doubleToggleDarkTheme, 200);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}