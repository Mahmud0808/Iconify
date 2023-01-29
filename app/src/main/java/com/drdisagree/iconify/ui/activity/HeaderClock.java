package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.HEADER_CLOCK_SIDEMARGIN;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_STYLE;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_TEXT_WHITE;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_TOPMARGIN;
import static com.drdisagree.iconify.common.References.PANEL_TOPMARGIN_SWITCH;
import static com.drdisagree.iconify.common.References.QS_TOPMARGIN;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RemotePrefs;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HeaderClock extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_header_clock);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_header_clock));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Custom header clock
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_header_clock = findViewById(R.id.enable_header_clock);
        enable_header_clock.setChecked(RemotePrefs.getBoolean(HEADER_CLOCK_SWITCH, false));
        enable_header_clock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RemotePrefs.putBoolean(HEADER_CLOCK_SWITCH, isChecked);
            new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
        });

        // Header clock style
        final Spinner header_clock_style = findViewById(R.id.header_clock_style);
        List<String> hcclock_styles = new ArrayList<>();
        hcclock_styles.add("Style 1");
        hcclock_styles.add("Style 2");
        hcclock_styles.add("Style 3");

        ArrayAdapter<String> hcclock_styles_adapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, hcclock_styles);
        hcclock_styles_adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        header_clock_style.setAdapter(hcclock_styles_adapter);

        header_clock_style.setSelection(RemotePrefs.getInt(HEADER_CLOCK_STYLE, 0));
        header_clock_style.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RemotePrefs.putInt(HEADER_CLOCK_STYLE, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Header clock side margin
        SeekBar header_clock_side_margin_seekbar = findViewById(R.id.header_clock_side_margin_seekbar);
        header_clock_side_margin_seekbar.setPadding(0, 0, 0, 0);
        TextView header_clock_side_margin_output = findViewById(R.id.header_clock_side_margin_output);
        header_clock_side_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + RemotePrefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0) + "dp");
        header_clock_side_margin_seekbar.setProgress(RemotePrefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0));
        final int[] sideMargin = {RemotePrefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0)};
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
                RemotePrefs.putInt(HEADER_CLOCK_SIDEMARGIN, sideMargin[0]);
            }
        });

        // Header clock top margin
        SeekBar header_clock_top_margin_seekbar = findViewById(R.id.header_clock_top_margin_seekbar);
        header_clock_top_margin_seekbar.setPadding(0, 0, 0, 0);
        TextView header_clock_top_margin_output = findViewById(R.id.header_clock_top_margin_output);
        header_clock_top_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + RemotePrefs.getInt(HEADER_CLOCK_TOPMARGIN, 8) + "dp");
        header_clock_top_margin_seekbar.setProgress(RemotePrefs.getInt(HEADER_CLOCK_TOPMARGIN, 8));
        final int[] topMargin = {RemotePrefs.getInt(HEADER_CLOCK_TOPMARGIN, 8)};
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
                RemotePrefs.putInt(HEADER_CLOCK_TOPMARGIN, topMargin[0]);
            }
        });

        // Force white text
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_force_white_text = findViewById(R.id.enable_force_white_text);
        enable_force_white_text.setChecked(RemotePrefs.getBoolean(HEADER_CLOCK_TEXT_WHITE, false));
        enable_force_white_text.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RemotePrefs.putBoolean(HEADER_CLOCK_TEXT_WHITE, isChecked);
        });

        // Enable panel top margin
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_panel_top_margin = findViewById(R.id.enable_panel_top_margin);
        enable_panel_top_margin.setChecked(RemotePrefs.getBoolean(PANEL_TOPMARGIN_SWITCH, false));
        enable_panel_top_margin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RemotePrefs.putBoolean(PANEL_TOPMARGIN_SWITCH, isChecked);
        });

        // QS panel top margin
        SeekBar qs_top_margin_seekbar = findViewById(R.id.qs_top_margin_seekbar);
        qs_top_margin_seekbar.setPadding(0, 0, 0, 0);
        TextView qs_top_margin_output = findViewById(R.id.qs_top_margin_output);
        qs_top_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + RemotePrefs.getInt(QS_TOPMARGIN, 16) + "dp");
        qs_top_margin_seekbar.setProgress(RemotePrefs.getInt(QS_TOPMARGIN, 16));
        final int[] qsTopMargin = {RemotePrefs.getInt(QS_TOPMARGIN, 16)};
        qs_top_margin_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                qsTopMargin[0] = progress;
                qs_top_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RemotePrefs.putInt(QS_TOPMARGIN, qsTopMargin[0]);
            }
        });

        // Restart systemui
        Button restart_sysui = findViewById(R.id.restart_sysui);
        restart_sysui.setOnClickListener(v -> new Handler().postDelayed(SystemUtil::restartSystemUI, 200));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}