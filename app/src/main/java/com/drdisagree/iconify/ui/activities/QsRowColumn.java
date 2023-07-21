package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.QS_ROW_COLUMN_SWITCH;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.References.FABRICATED_QQS_ROW;
import static com.drdisagree.iconify.common.References.FABRICATED_QQS_TILE;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_COLUMN;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_ROW;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_TILE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityQsRowColumnBinding;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.FabricatedUtil;

public class QsRowColumn extends BaseActivity {

    private ActivityQsRowColumnBinding binding;
    private LoadingDialog loadingDialog;

    public static void applyRowColumn() {
        FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_QQS_ROW, "integer", "quick_qs_panel_max_rows", String.valueOf(Integer.parseInt(Prefs.getString(FABRICATED_QQS_ROW)) + 1));
        FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_QS_ROW, "integer", "quick_settings_max_rows", String.valueOf(Integer.parseInt(Prefs.getString(FABRICATED_QS_ROW)) + 1));
        FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_QS_COLUMN, "integer", "quick_settings_num_columns", String.valueOf(Integer.parseInt(Prefs.getString(FABRICATED_QS_COLUMN)) + 1));
        FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_QQS_TILE, "integer", "quick_qs_panel_max_tiles", String.valueOf((Integer.parseInt(Prefs.getString(FABRICATED_QQS_ROW)) + 1) * (Integer.parseInt(Prefs.getString(FABRICATED_QS_COLUMN)) + 1)));
        FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_QS_TILE, "integer", "quick_settings_min_num_tiles", String.valueOf((Integer.parseInt(Prefs.getString(FABRICATED_QS_COLUMN)) + 1) * (Integer.parseInt(Prefs.getString(FABRICATED_QS_ROW)) + 1)));
    }

    public static void resetRowColumn() {
        FabricatedUtil.disableOverlay(FABRICATED_QQS_ROW);
        FabricatedUtil.disableOverlay(FABRICATED_QS_ROW);
        FabricatedUtil.disableOverlay(FABRICATED_QS_COLUMN);
        FabricatedUtil.disableOverlay(FABRICATED_QQS_TILE);
        FabricatedUtil.disableOverlay(FABRICATED_QS_TILE);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQsRowColumnBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.collapsingToolbar, binding.header.toolbar, R.string.activity_title_qs_row_column);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // Quick QsPanel Row
        final int[] finalQqsRow = {1};

        if (!Prefs.getString(FABRICATED_QQS_ROW).equals(STR_NULL)) {
            binding.qqsRowOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (Integer.parseInt(Prefs.getString(FABRICATED_QQS_ROW)) + 1));
            finalQqsRow[0] = Integer.parseInt(Prefs.getString(FABRICATED_QQS_ROW));
            binding.qqsRowSeekbar.setProgress(finalQqsRow[0]);
        } else binding.qqsRowOutput.setText(getResources().getString(R.string.opt_selected) + " 2");

        binding.qqsRowSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                finalQqsRow[0] = progress;
                binding.qqsRowOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // QsPanel Row
        final int[] finalQsRow = {3};

        if (!Prefs.getString(FABRICATED_QS_ROW).equals(STR_NULL)) {
            binding.qsRowOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (Integer.parseInt(Prefs.getString(FABRICATED_QS_ROW)) + 1));
            finalQsRow[0] = Integer.parseInt(Prefs.getString(FABRICATED_QS_ROW));
            binding.qsRowSeekbar.setProgress(finalQsRow[0]);
        } else binding.qsRowOutput.setText(getResources().getString(R.string.opt_selected) + " 4");

        binding.qsRowSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                finalQsRow[0] = progress;
                binding.qsRowOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // QsPanel Column
        final int[] finalQsColumn = {1};

        if (!Prefs.getString(FABRICATED_QS_COLUMN).equals(STR_NULL)) {
            binding.qsColumnOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (Integer.parseInt(Prefs.getString(FABRICATED_QS_COLUMN)) + 1));
            finalQsColumn[0] = Integer.parseInt(Prefs.getString(FABRICATED_QS_COLUMN));
            binding.qsColumnSeekbar.setProgress(finalQsColumn[0]);
        } else binding.qsColumnOutput.setText(getResources().getString(R.string.opt_selected) + " 2");

        binding.qsColumnSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                finalQsColumn[0] = progress;
                binding.qsColumnOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        
        // Apply button
        binding.qsRowColumnApply.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                Prefs.putBoolean(QS_ROW_COLUMN_SWITCH, true);

                Prefs.putString(FABRICATED_QQS_ROW, String.valueOf(finalQqsRow[0]));
                Prefs.putString(FABRICATED_QS_ROW, String.valueOf(finalQsRow[0]));
                Prefs.putString(FABRICATED_QS_COLUMN, String.valueOf(finalQsColumn[0]));
                Prefs.putString(FABRICATED_QQS_TILE, String.valueOf((finalQqsRow[0] + 1) * (finalQsColumn[0] + 1)));
                Prefs.putString(FABRICATED_QS_TILE, String.valueOf((finalQsColumn[0] + 1) * (finalQsRow[0] + 1)));

                applyRowColumn();

                runOnUiThread(() -> {

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Reset button visibility
                        binding.qsRowColumnReset.setVisibility(View.VISIBLE);

                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    }, 2000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });

        // Reset button
        if (Prefs.getBoolean(QS_ROW_COLUMN_SWITCH)) binding.qsRowColumnReset.setVisibility(View.VISIBLE);
        else binding.qsRowColumnReset.setVisibility(View.GONE);

        binding.qsRowColumnReset.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                resetRowColumn();

                runOnUiThread(() -> {
                    Prefs.putBoolean(QS_ROW_COLUMN_SWITCH, false);

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Reset button visibility
                        binding.qsRowColumnReset.setVisibility(View.GONE);

                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_reset), Toast.LENGTH_SHORT).show();
                    }, 2000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}