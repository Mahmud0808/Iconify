package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_TEXT_SCALING;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SIDEMARGIN;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_STYLE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_TEXT_WHITE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_TOPMARGIN;
import static com.drdisagree.iconify.ui.utils.ViewBindingHelpers.disableNestedScrolling;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.ui.adapters.ClockPreviewAdapter;
import com.drdisagree.iconify.ui.models.ClockModel;
import com.drdisagree.iconify.ui.views.HeaderClockStyles;
import com.drdisagree.iconify.utils.HelperUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

import me.relex.circleindicator.CircleIndicator3;

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
            if (!isChecked) RPrefs.putInt(HEADER_CLOCK_STYLE, 1);
            new Handler().postDelayed(HelperUtil::forceApply, 200);
        });

        // Header clock style
        ViewPager2 container = findViewById(R.id.header_clock_preview);
        container.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        ClockPreviewAdapter adapter = initHeaderClockStyles();
        container.setAdapter(adapter);
        disableNestedScrolling(container);

        CircleIndicator3 indicator = findViewById(R.id.header_clock_preview_indicator);
        container.setCurrentItem(RPrefs.getInt(HEADER_CLOCK_STYLE, 1) - 1);
        indicator.setViewPager(container);
        indicator.tintIndicator(getResources().getColor(R.color.textColorSecondary));

        // Text Scaling
        SeekBar header_clock_textscaling_seekbar = findViewById(R.id.header_clock_textscaling_seekbar);
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
                    new Handler().postDelayed(HelperUtil::forceApply, 200);
                }
            }
        });

        // Header clock side margin
        SeekBar header_clock_side_margin_seekbar = findViewById(R.id.header_clock_side_margin_seekbar);
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
                    new Handler().postDelayed(HelperUtil::forceApply, 200);
                }
            }
        });

        // Header clock top margin
        SeekBar header_clock_top_margin_seekbar = findViewById(R.id.header_clock_top_margin_seekbar);
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
                    new Handler().postDelayed(HelperUtil::forceApply, 200);
                }
            }
        });

        // Force white text
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_force_white_text = findViewById(R.id.enable_force_white_text);
        enable_force_white_text.setChecked(RPrefs.getBoolean(HEADER_CLOCK_TEXT_WHITE, false));
        enable_force_white_text.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_CLOCK_TEXT_WHITE, isChecked);
            if (RPrefs.getBoolean(HEADER_CLOCK_SWITCH, false)) {
                new Handler().postDelayed(HelperUtil::forceApply, 200);
            }
        });
    }

    private ClockPreviewAdapter initHeaderClockStyles() {
        ArrayList<ClockModel> header_clock = new ArrayList<>();

        for (int i = 1; i <= 5; i++)
            header_clock.add(new ClockModel(HeaderClockStyles.initHeaderClockStyle(this, i)));

        return new ClockPreviewAdapter(this, header_clock, HEADER_CLOCK_SWITCH, HEADER_CLOCK_STYLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}