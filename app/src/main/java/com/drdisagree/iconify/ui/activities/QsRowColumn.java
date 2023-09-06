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
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityQsRowColumnBinding;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;

public class QsRowColumn extends BaseActivity {

    private ActivityQsRowColumnBinding binding;
    private LoadingDialog loadingDialog;

    public static void applyRowColumn() {
        FabricatedUtil.buildAndEnableOverlays(
                new Object[]{SYSTEMUI_PACKAGE, FABRICATED_QQS_ROW, "integer", "quick_qs_panel_max_rows", String.valueOf(Integer.parseInt(Prefs.getString(FABRICATED_QQS_ROW)) + 1)},
                new Object[]{SYSTEMUI_PACKAGE, FABRICATED_QS_ROW, "integer", "quick_settings_max_rows", String.valueOf(Integer.parseInt(Prefs.getString(FABRICATED_QS_ROW)) + 1)},
                new Object[]{SYSTEMUI_PACKAGE, FABRICATED_QS_COLUMN, "integer", "quick_settings_num_columns", String.valueOf(Integer.parseInt(Prefs.getString(FABRICATED_QS_COLUMN)) + 1)},
                new Object[]{SYSTEMUI_PACKAGE, FABRICATED_QQS_TILE, "integer", "quick_qs_panel_max_tiles", String.valueOf((Integer.parseInt(Prefs.getString(FABRICATED_QQS_ROW)) + 1) * (Integer.parseInt(Prefs.getString(FABRICATED_QS_COLUMN)) + 1))},
                new Object[]{SYSTEMUI_PACKAGE, FABRICATED_QS_TILE, "integer", "quick_settings_min_num_tiles", String.valueOf((Integer.parseInt(Prefs.getString(FABRICATED_QS_COLUMN)) + 1) * (Integer.parseInt(Prefs.getString(FABRICATED_QS_ROW)) + 1))}
        );
    }

    public static void resetRowColumn() {
        FabricatedUtil.disableOverlays(FABRICATED_QQS_ROW, FABRICATED_QS_ROW, FABRICATED_QS_COLUMN, FABRICATED_QQS_TILE, FABRICATED_QS_TILE);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQsRowColumnBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewHelper.setHeader(this, binding.header.toolbar, R.string.activity_title_qs_row_column);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // Quick QsPanel Row
        final int[] finalQqsRow = {1};

        if (!Prefs.getString(FABRICATED_QQS_ROW).equals(STR_NULL)) {
            binding.qqsRowOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (Integer.parseInt(Prefs.getString(FABRICATED_QQS_ROW)) + 1));
            finalQqsRow[0] = Integer.parseInt(Prefs.getString(FABRICATED_QQS_ROW));
            binding.qqsRowSeekbar.setValue(finalQqsRow[0]);
        } else binding.qqsRowOutput.setText(getResources().getString(R.string.opt_selected) + " 2");

        binding.qqsRowSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalQqsRow[0] = (int) slider.getValue();
                binding.qqsRowOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (finalQqsRow[0] + 1));
            }
        });

        // QsPanel Row
        final int[] finalQsRow = {3};

        if (!Prefs.getString(FABRICATED_QS_ROW).equals(STR_NULL)) {
            binding.qsRowOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (Integer.parseInt(Prefs.getString(FABRICATED_QS_ROW)) + 1));
            finalQsRow[0] = Integer.parseInt(Prefs.getString(FABRICATED_QS_ROW));
            binding.qsRowSeekbar.setValue(finalQsRow[0]);
        } else binding.qsRowOutput.setText(getResources().getString(R.string.opt_selected) + " 4");

        binding.qsRowSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalQsRow[0] = (int) slider.getValue();
                binding.qsRowOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (finalQsRow[0] + 1));
            }
        });

        // QsPanel Column
        final int[] finalQsColumn = {1};

        if (!Prefs.getString(FABRICATED_QS_COLUMN).equals(STR_NULL)) {
            binding.qsColumnOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (Integer.parseInt(Prefs.getString(FABRICATED_QS_COLUMN)) + 1));
            finalQsColumn[0] = Integer.parseInt(Prefs.getString(FABRICATED_QS_COLUMN));
            binding.qsColumnSeekbar.setValue(finalQsColumn[0]);
        } else
            binding.qsColumnOutput.setText(getResources().getString(R.string.opt_selected) + " 2");

        binding.qsColumnSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalQsColumn[0] = (int) slider.getValue();
                binding.qsColumnOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (finalQsColumn[0] + 1));
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
        if (Prefs.getBoolean(QS_ROW_COLUMN_SWITCH))
            binding.qsRowColumnReset.setVisibility(View.VISIBLE);
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

        new MaterialAlertDialogBuilder(this, R.style.MaterialComponents_MaterialAlertDialog)
                .setTitle(getResources().getString(R.string.hey_there))
                .setMessage(getResources().getString(R.string.qs_row_column_warn_desc))
                .setPositiveButton(getResources().getString(R.string.understood), (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}