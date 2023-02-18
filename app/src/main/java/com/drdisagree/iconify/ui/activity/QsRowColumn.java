package com.drdisagree.iconify.ui.activity;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;
import static com.drdisagree.iconify.common.References.LAND_QQS_ROW;
import static com.drdisagree.iconify.common.References.LAND_QS_COLUMN;
import static com.drdisagree.iconify.common.References.LAND_QS_ROW;
import static com.drdisagree.iconify.common.References.PORT_QQS_ROW;
import static com.drdisagree.iconify.common.References.PORT_QS_COLUMN;
import static com.drdisagree.iconify.common.References.PORT_QS_ROW;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.overlaymanager.QsRowColumnManager;
import com.drdisagree.iconify.ui.view.LoadingDialog;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class QsRowColumn extends AppCompatActivity {

    LoadingDialog loadingDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qs_row_column);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_qs_row_column));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // Portrait Quick QsPanel Row
        SeekBar port_qqs_row_seekbar = findViewById(R.id.port_qqs_row_seekbar);
        TextView port_qqs_row_output = findViewById(R.id.port_qqs_row_output);

        port_qqs_row_seekbar.setPadding(0, 0, 0, 0);
        final int[] portQqsRow = {Prefs.getInt(PORT_QQS_ROW, 2)};
        port_qqs_row_output.setText(getResources().getString(R.string.opt_selected) + ' ' + portQqsRow[0]);
        port_qqs_row_seekbar.setProgress(portQqsRow[0]);

        port_qqs_row_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                portQqsRow[0] = progress;
                port_qqs_row_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Portrait QsPanel Row
        SeekBar port_qs_row_seekbar = findViewById(R.id.port_qs_row_seekbar);
        TextView port_qs_row_output = findViewById(R.id.port_qs_row_output);

        port_qs_row_seekbar.setPadding(0, 0, 0, 0);
        final int[] portQsRow = {Prefs.getInt(PORT_QS_ROW, 4)};
        port_qs_row_output.setText(getResources().getString(R.string.opt_selected) + ' ' + portQsRow[0]);
        port_qs_row_seekbar.setProgress(portQsRow[0]);

        port_qs_row_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                portQsRow[0] = progress;
                port_qs_row_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Portrait QsPanel Column
        SeekBar port_qs_column_seekbar = findViewById(R.id.port_qs_column_seekbar);
        TextView port_qs_column_output = findViewById(R.id.port_qs_column_output);

        port_qs_column_seekbar.setPadding(0, 0, 0, 0);
        final int[] portQsColumn = {Prefs.getInt(PORT_QS_COLUMN, 2)};
        port_qs_column_output.setText(getResources().getString(R.string.opt_selected) + ' ' + portQsColumn[0]);
        port_qs_column_seekbar.setProgress(portQsColumn[0]);

        port_qs_column_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                portQsColumn[0] = progress;
                port_qs_column_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Landscape Quick QsPanel Row
        SeekBar land_qqs_row_seekbar = findViewById(R.id.land_qqs_row_seekbar);
        TextView land_qqs_row_output = findViewById(R.id.land_qqs_row_output);

        land_qqs_row_seekbar.setPadding(0, 0, 0, 0);
        final int[] landQqsRow = {Prefs.getInt(LAND_QQS_ROW, 2)};
        land_qqs_row_output.setText(getResources().getString(R.string.opt_selected) + ' ' + landQqsRow[0]);
        land_qqs_row_seekbar.setProgress(landQqsRow[0]);

        land_qqs_row_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                landQqsRow[0] = progress;
                land_qqs_row_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Landscape QsPanel Row
        SeekBar land_qs_row_seekbar = findViewById(R.id.land_qs_row_seekbar);
        TextView land_qs_row_output = findViewById(R.id.land_qs_row_output);

        land_qs_row_seekbar.setPadding(0, 0, 0, 0);
        final int[] landQsRow = {Prefs.getInt(LAND_QS_ROW, 4)};
        land_qs_row_output.setText(getResources().getString(R.string.opt_selected) + ' ' + landQsRow[0]);
        land_qs_row_seekbar.setProgress(landQsRow[0]);

        land_qs_row_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                landQsRow[0] = progress;
                land_qs_row_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Landscape QsPanel Column
        SeekBar land_qs_column_seekbar = findViewById(R.id.land_qs_column_seekbar);
        TextView land_qs_column_output = findViewById(R.id.land_qs_column_output);

        land_qs_column_seekbar.setPadding(0, 0, 0, 0);
        final int[] landQsColumn = {Prefs.getInt(LAND_QS_COLUMN, 2)};
        land_qs_column_output.setText(getResources().getString(R.string.opt_selected) + ' ' + landQsColumn[0]);
        land_qs_column_seekbar.setProgress(landQsColumn[0]);

        land_qs_column_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                landQsColumn[0] = progress;
                land_qs_column_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Apply and reset button
        Button qs_row_column_apply = findViewById(R.id.qs_row_column_apply);
        Button qs_row_column_reset = findViewById(R.id.qs_row_column_reset);

        if (Prefs.getBoolean("IconifyComponentQSRC.overlay"))
            qs_row_column_reset.setVisibility(View.VISIBLE);

        qs_row_column_apply.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent();
                intent.setAction(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } else {
                // Show loading dialog
                loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));
                AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                Runnable runnable = () -> {
                    try {
                        hasErroredOut.set(QsRowColumnManager.buildOverlay(portQqsRow[0], portQsRow[0], portQsColumn[0], landQqsRow[0], landQsRow[0], landQsColumn[0]));
                    } catch (IOException e) {
                        hasErroredOut.set(true);
                    }

                    runOnUiThread(() -> {
                        if (!hasErroredOut.get()) {
                            Prefs.putInt(PORT_QQS_ROW, portQqsRow[0]);
                            Prefs.putInt(PORT_QS_ROW, portQsRow[0]);
                            Prefs.putInt(PORT_QS_COLUMN, portQsColumn[0]);
                            Prefs.putInt(LAND_QQS_ROW, landQqsRow[0]);
                            Prefs.putInt(LAND_QS_ROW, landQsRow[0]);
                            Prefs.putInt(LAND_QS_COLUMN, landQsColumn[0]);

                            qs_row_column_reset.setVisibility(View.VISIBLE);
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

        qs_row_column_reset.setOnClickListener(v -> {
            Prefs.clearPref(PORT_QQS_ROW);
            Prefs.clearPref(PORT_QS_ROW);
            Prefs.clearPref(PORT_QS_COLUMN);
            Prefs.clearPref(LAND_QQS_ROW);
            Prefs.clearPref(LAND_QS_ROW);
            Prefs.clearPref(LAND_QS_COLUMN);

            portQqsRow[0] = 2;
            portQsRow[0] = 4;
            portQsColumn[0] = 2;
            landQqsRow[0] = 2;
            landQsRow[0] = 4;
            landQsColumn[0] = 2;

            port_qqs_row_output.setText(getResources().getString(R.string.opt_selected) + "2");
            port_qs_row_output.setText(getResources().getString(R.string.opt_selected) + "4");
            port_qs_column_output.setText(getResources().getString(R.string.opt_selected) + "2");
            land_qqs_row_output.setText(getResources().getString(R.string.opt_selected) + "2");
            land_qs_row_output.setText(getResources().getString(R.string.opt_selected) + "4");
            land_qs_column_output.setText(getResources().getString(R.string.opt_selected) + "2");

            port_qqs_row_seekbar.setProgress(2);
            port_qs_row_seekbar.setProgress(4);
            port_qs_column_seekbar.setProgress(2);
            land_qqs_row_seekbar.setProgress(2);
            land_qs_row_seekbar.setProgress(4);
            land_qs_column_seekbar.setProgress(2);

            qs_row_column_reset.setVisibility(View.GONE);

            OverlayUtil.disableOverlay("IconifyComponentQSRC.overlay");
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}