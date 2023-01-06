package com.drdisagree.iconify.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
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
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.utils.FabricatedOverlay;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class QsRowColumn extends AppCompatActivity {

    LoadingDialog loadingDialog;

    public static void applyRowColumn() {
        FabricatedOverlay.buildOverlay("systemui", "qqsRow", "integer", "quick_qs_panel_max_rows", String.valueOf(Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qqsRow")) + 1));
        FabricatedOverlay.buildOverlay("systemui", "qsRow", "integer", "quick_settings_max_rows", String.valueOf(Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsRow")) + 1));
        FabricatedOverlay.buildOverlay("systemui", "qqsColumn", "integer", "quick_qs_panel_max_columns", String.valueOf(Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsColumn")) + 1));
        FabricatedOverlay.buildOverlay("systemui", "qsColumn", "integer", "quick_settings_num_columns", String.valueOf(Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsColumn")) + 1));
        FabricatedOverlay.buildOverlay("systemui", "qqsTile", "integer", "quick_qs_panel_max_tiles", String.valueOf((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qqsRow")) + 1) * (Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsColumn")) + 1)));
        FabricatedOverlay.buildOverlay("systemui", "qsTile", "integer", "quick_settings_min_num_tiles", String.valueOf((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsColumn")) + 1) * (Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsRow")) + 1)));

        FabricatedOverlay.enableOverlay("qqsRow");
        FabricatedOverlay.enableOverlay("qsRow");
        FabricatedOverlay.enableOverlay("qqsColumn");
        FabricatedOverlay.enableOverlay("qsColumn");
        FabricatedOverlay.enableOverlay("qqsTile");
        FabricatedOverlay.enableOverlay("qsTile");
    }

    public static void resetRowColumn() {
        FabricatedOverlay.disableOverlay("qqsRow");
        FabricatedOverlay.disableOverlay("qsRow");
        FabricatedOverlay.disableOverlay("qqsColumn");
        FabricatedOverlay.disableOverlay("qsColumn");
        FabricatedOverlay.disableOverlay("qqsTile");
        FabricatedOverlay.disableOverlay("qsTile");
    }

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

        // Quick QsPanel Row

        SeekBar qqs_row_seekbar = findViewById(R.id.qqs_row_seekbar);
        TextView qqs_row_output = findViewById(R.id.qqs_row_output);

        qqs_row_seekbar.setPadding(0, 0, 0, 0);
        final int[] finalQqsRow = {1};

        if (!PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qqsRow").equals("null")) {
            qqs_row_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qqsRow")) + 1));
            finalQqsRow[0] = Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qqsRow"));
            qqs_row_seekbar.setProgress(finalQqsRow[0]);
        } else
            qqs_row_output.setText(getResources().getString(R.string.opt_selected) + " 2");

        qqs_row_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                finalQqsRow[0] = progress;
                qqs_row_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // QsPanel Row

        SeekBar qs_row_seekbar = findViewById(R.id.qs_row_seekbar);
        TextView qs_row_output = findViewById(R.id.qs_row_output);

        qs_row_seekbar.setPadding(0, 0, 0, 0);
        final int[] finalQsRow = {3};

        if (!PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsRow").equals("null")) {
            qs_row_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsRow")) + 1));
            finalQsRow[0] = Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsRow"));
            qs_row_seekbar.setProgress(finalQsRow[0]);
        } else
            qs_row_output.setText(getResources().getString(R.string.opt_selected) + " 4");

        qs_row_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                finalQsRow[0] = progress;
                qs_row_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // QsPanel Column

        SeekBar qs_column_seekbar = findViewById(R.id.qs_column_seekbar);
        TextView qs_column_output = findViewById(R.id.qs_column_output);

        qs_column_seekbar.setPadding(0, 0, 0, 0);
        final int[] finalQsColumn = {1};

        if (!PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsColumn").equals("null")) {
            qs_column_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsColumn")) + 1));
            finalQsColumn[0] = Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsColumn"));
            qs_column_seekbar.setProgress(finalQsColumn[0]);
        } else
            qs_column_output.setText(getResources().getString(R.string.opt_selected) + " 2");

        qs_column_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                finalQsColumn[0] = progress;
                qs_column_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Apply button declaration
        Button qs_row_column_apply = findViewById(R.id.qs_row_column_apply);

        // Reset button declaration
        Button qs_row_column_reset = findViewById(R.id.qs_row_column_reset);

        // Apply button
        qs_row_column_apply.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                PrefConfig.savePrefBool(Iconify.getAppContext(), "fabricatedqsRowColumn", true);

                PrefConfig.savePrefSettings(Iconify.getAppContext(), "qqsRow", String.valueOf(finalQqsRow[0]));
                PrefConfig.savePrefSettings(Iconify.getAppContext(), "qsRow", String.valueOf(finalQsRow[0]));
                PrefConfig.savePrefSettings(Iconify.getAppContext(), "qqsColumn", String.valueOf(finalQsColumn[0]));
                PrefConfig.savePrefSettings(Iconify.getAppContext(), "qsColumn", String.valueOf(finalQsColumn[0]));
                PrefConfig.savePrefSettings(Iconify.getAppContext(), "qqsTile", String.valueOf((finalQqsRow[0] + 1) * (finalQsColumn[0] + 1)));
                PrefConfig.savePrefSettings(Iconify.getAppContext(), "qsTile", String.valueOf((finalQsColumn[0] + 1) * (finalQsRow[0] + 1)));

                applyRowColumn();

                runOnUiThread(() -> {

                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Reset button visibility
                        qs_row_column_reset.setVisibility(View.VISIBLE);

                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    }, 2000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });

        // Reset button
        if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "fabricatedqsRowColumn"))
            qs_row_column_reset.setVisibility(View.VISIBLE);
        else
            qs_row_column_reset.setVisibility(View.GONE);

        qs_row_column_reset.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                resetRowColumn();

                runOnUiThread(() -> {
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "fabricatedqsRowColumn", false);

                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Reset button visibility
                        qs_row_column_reset.setVisibility(View.GONE);

                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_reset), Toast.LENGTH_SHORT).show();
                    }, 2000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
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