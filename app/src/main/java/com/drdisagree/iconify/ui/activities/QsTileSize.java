package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.LAND_QSTILE_EXPANDED_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.LAND_QSTILE_NONEXPANDED_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.PORT_QSTILE_EXPANDED_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.PORT_QSTILE_NONEXPANDED_HEIGHT;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.overlaymanager.QsTileHeightManager;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class QsTileSize extends AppCompatActivity {

    LoadingDialog loadingDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qs_tile_size);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_qs_tile_size));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Show loading dialog
        loadingDialog = new LoadingDialog(this);

        // Portrait Nonexpanded height
        SeekBar port_nonexpanded_height_seekbar = findViewById(R.id.port_nonexpanded_height_seekbar);
        TextView port_nonexpanded_height_output = findViewById(R.id.port_nonexpanded_height_output);
        port_nonexpanded_height_output.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(PORT_QSTILE_NONEXPANDED_HEIGHT, 60) + "dp");
        port_nonexpanded_height_seekbar.setProgress(Prefs.getInt(PORT_QSTILE_NONEXPANDED_HEIGHT, 60));

        // Reset button
        ImageView reset_port_nonexpanded_height = findViewById(R.id.reset_port_nonexpanded_height);
        reset_port_nonexpanded_height.setVisibility(Prefs.getInt(PORT_QSTILE_NONEXPANDED_HEIGHT, 60) == 60 ? View.INVISIBLE : View.VISIBLE);
        int[] portNonExpandedHeight = new int[]{Prefs.getInt(PORT_QSTILE_NONEXPANDED_HEIGHT, 60)};

        reset_port_nonexpanded_height.setOnLongClickListener(v -> {
            portNonExpandedHeight[0] = 60;
            port_nonexpanded_height_seekbar.setProgress(60);
            reset_port_nonexpanded_height.setVisibility(View.INVISIBLE);
            return true;
        });

        port_nonexpanded_height_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                portNonExpandedHeight[0] = progress;
                if (progress == 60) reset_port_nonexpanded_height.setVisibility(View.INVISIBLE);
                port_nonexpanded_height_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                reset_port_nonexpanded_height.setVisibility(portNonExpandedHeight[0] == 60 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Portrait Expanded height
        SeekBar port_expanded_height_seekbar = findViewById(R.id.port_expanded_height_seekbar);
        TextView port_expanded_height_output = findViewById(R.id.port_expanded_height_output);
        port_expanded_height_output.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(PORT_QSTILE_EXPANDED_HEIGHT, 80) + "dp");
        port_expanded_height_seekbar.setProgress(Prefs.getInt(PORT_QSTILE_EXPANDED_HEIGHT, 80));

        // Reset button
        ImageView reset_port_expanded_height = findViewById(R.id.reset_port_expanded_height);
        reset_port_expanded_height.setVisibility(Prefs.getInt(PORT_QSTILE_EXPANDED_HEIGHT, 80) == 80 ? View.INVISIBLE : View.VISIBLE);
        int[] portExpandedHeight = new int[]{Prefs.getInt(PORT_QSTILE_EXPANDED_HEIGHT, 80)};

        reset_port_expanded_height.setOnLongClickListener(v -> {
            portExpandedHeight[0] = 80;
            port_expanded_height_seekbar.setProgress(80);
            reset_port_expanded_height.setVisibility(View.INVISIBLE);
            return true;
        });

        port_expanded_height_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                portExpandedHeight[0] = progress;
                if (progress == 80) reset_port_expanded_height.setVisibility(View.INVISIBLE);
                port_expanded_height_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                reset_port_expanded_height.setVisibility(portExpandedHeight[0] == 80 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Landscape Nonexpanded height
        SeekBar land_nonexpanded_height_seekbar = findViewById(R.id.land_nonexpanded_height_seekbar);
        TextView land_nonexpanded_height_output = findViewById(R.id.land_nonexpanded_height_output);
        land_nonexpanded_height_output.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(LAND_QSTILE_NONEXPANDED_HEIGHT, 60) + "dp");
        land_nonexpanded_height_seekbar.setProgress(Prefs.getInt(LAND_QSTILE_NONEXPANDED_HEIGHT, 60));

        // Reset button
        ImageView reset_land_nonexpanded_height = findViewById(R.id.reset_land_nonexpanded_height);
        reset_land_nonexpanded_height.setVisibility(Prefs.getInt(LAND_QSTILE_NONEXPANDED_HEIGHT, 60) == 60 ? View.INVISIBLE : View.VISIBLE);
        int[] landNonExpandedHeight = new int[]{Prefs.getInt(LAND_QSTILE_NONEXPANDED_HEIGHT, 60)};

        reset_land_nonexpanded_height.setOnLongClickListener(v -> {
            landNonExpandedHeight[0] = 60;
            land_nonexpanded_height_seekbar.setProgress(60);
            reset_land_nonexpanded_height.setVisibility(View.INVISIBLE);
            return true;
        });

        land_nonexpanded_height_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                landNonExpandedHeight[0] = progress;
                if (progress == 60) reset_land_nonexpanded_height.setVisibility(View.INVISIBLE);
                land_nonexpanded_height_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                reset_land_nonexpanded_height.setVisibility(landNonExpandedHeight[0] == 60 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Landscape Expanded height
        SeekBar land_expanded_height_seekbar = findViewById(R.id.land_expanded_height_seekbar);
        TextView land_expanded_height_output = findViewById(R.id.land_expanded_height_output);
        land_expanded_height_output.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(LAND_QSTILE_EXPANDED_HEIGHT, 80) + "dp");
        land_expanded_height_seekbar.setProgress(Prefs.getInt(LAND_QSTILE_EXPANDED_HEIGHT, 80));

        // Reset button
        ImageView reset_land_expanded_height = findViewById(R.id.reset_land_expanded_height);
        reset_land_expanded_height.setVisibility(Prefs.getInt(LAND_QSTILE_EXPANDED_HEIGHT, 80) == 80 ? View.INVISIBLE : View.VISIBLE);
        int[] landExpandedHeight = new int[]{Prefs.getInt(LAND_QSTILE_EXPANDED_HEIGHT, 80)};

        reset_land_expanded_height.setOnLongClickListener(v -> {
            landExpandedHeight[0] = 80;
            land_expanded_height_seekbar.setProgress(80);
            reset_land_expanded_height.setVisibility(View.INVISIBLE);
            return true;
        });

        land_expanded_height_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                landExpandedHeight[0] = progress;
                if (progress == 80) reset_land_expanded_height.setVisibility(View.INVISIBLE);
                land_expanded_height_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                reset_land_expanded_height.setVisibility(landExpandedHeight[0] == 80 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Apply and reset button
        Button qs_tile_height_apply = findViewById(R.id.qs_tile_height_apply);
        Button qs_tile_height_reset = findViewById(R.id.qs_tile_height_reset);

        if (Prefs.getBoolean("IconifyComponentQSTH.overlay"))
            qs_tile_height_reset.setVisibility(View.VISIBLE);

        qs_tile_height_apply.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                SystemUtil.getStoragePermission(this);
            } else {
                // Show loading dialog
                loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));
                AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                Runnable runnable = () -> {
                    try {
                        hasErroredOut.set(QsTileHeightManager.enableOverlay(portNonExpandedHeight[0], portExpandedHeight[0], landNonExpandedHeight[0], landExpandedHeight[0]));
                    } catch (IOException e) {
                        hasErroredOut.set(true);
                    }

                    runOnUiThread(() -> {
                        if (!hasErroredOut.get()) {
                            Prefs.putInt(PORT_QSTILE_NONEXPANDED_HEIGHT, portNonExpandedHeight[0]);
                            Prefs.putInt(PORT_QSTILE_EXPANDED_HEIGHT, portExpandedHeight[0]);
                            Prefs.putInt(LAND_QSTILE_NONEXPANDED_HEIGHT, landNonExpandedHeight[0]);
                            Prefs.putInt(LAND_QSTILE_EXPANDED_HEIGHT, landExpandedHeight[0]);

                            qs_tile_height_reset.setVisibility(View.VISIBLE);
                        }

                        new Handler().postDelayed(() -> {
                            // Hide loading dialog
                            loadingDialog.hide();

                            if (hasErroredOut.get())
                                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                        }, 2000);
                    });
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });

        qs_tile_height_reset.setOnClickListener(v -> {
            Prefs.clearPref(PORT_QSTILE_NONEXPANDED_HEIGHT);
            Prefs.clearPref(PORT_QSTILE_EXPANDED_HEIGHT);
            Prefs.clearPref(LAND_QSTILE_NONEXPANDED_HEIGHT);
            Prefs.clearPref(LAND_QSTILE_EXPANDED_HEIGHT);

            portNonExpandedHeight[0] = 60;
            portExpandedHeight[0] = 80;
            landNonExpandedHeight[0] = 60;
            landExpandedHeight[0] = 80;

            port_nonexpanded_height_output.setText(getResources().getString(R.string.opt_selected) + "60dp");
            port_expanded_height_output.setText(getResources().getString(R.string.opt_selected) + "80dp");
            land_nonexpanded_height_output.setText(getResources().getString(R.string.opt_selected) + "60dp");
            land_expanded_height_output.setText(getResources().getString(R.string.opt_selected) + "80dp");

            port_nonexpanded_height_seekbar.setProgress(60);
            port_expanded_height_seekbar.setProgress(80);
            land_nonexpanded_height_seekbar.setProgress(60);
            land_expanded_height_seekbar.setProgress(80);

            reset_port_nonexpanded_height.setVisibility(View.INVISIBLE);
            reset_port_expanded_height.setVisibility(View.INVISIBLE);
            reset_land_nonexpanded_height.setVisibility(View.INVISIBLE);
            reset_land_expanded_height.setVisibility(View.INVISIBLE);

            qs_tile_height_reset.setVisibility(View.GONE);

            OverlayUtil.disableOverlay("IconifyComponentQSTH.overlay");
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}