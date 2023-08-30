package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.HEADER_QQS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.LAND_QQS_TOP_MARGIN;
import static com.drdisagree.iconify.common.Preferences.LAND_QS_TOP_MARGIN;
import static com.drdisagree.iconify.common.Preferences.PORT_QQS_TOP_MARGIN;
import static com.drdisagree.iconify.common.Preferences.PORT_QS_TOP_MARGIN;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.ActivityQsPanelMarginBinding;
import com.drdisagree.iconify.utils.overlaymanager.QsMarginManager;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class QsPanelMargin extends BaseActivity {

    private ActivityQsPanelMarginBinding binding;
    private LoadingDialog loadingDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQsPanelMarginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.collapsingToolbar, binding.header.toolbar, R.string.activity_title_qs_panel_margin);

        // Show loading dialog
        loadingDialog = new LoadingDialog(this);

        // Portrait qqs margin
        binding.portQqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(PORT_QQS_TOP_MARGIN, 100) + "dp");
        binding.portQqsTopMarginSeekbar.setProgress(Prefs.getInt(PORT_QQS_TOP_MARGIN, 100));
        int[] portQqsMargin = new int[]{Prefs.getInt(PORT_QQS_TOP_MARGIN, 100)};

        binding.portQqsTopMarginSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                portQqsMargin[0] = progress;
                binding.portQqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Portrait qs margin
        binding.portQsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(PORT_QS_TOP_MARGIN, 100) + "dp");
        binding.portQsTopMarginSeekbar.setProgress(Prefs.getInt(PORT_QS_TOP_MARGIN, 100));
        int[] portQsMargin = new int[]{Prefs.getInt(PORT_QS_TOP_MARGIN, 100)};

        binding.portQsTopMarginSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                portQsMargin[0] = progress;
                binding.portQsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Landscape qqs margin
        binding.landQqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(LAND_QQS_TOP_MARGIN, 100) + "dp");
        binding.landQqsTopMarginSeekbar.setProgress(Prefs.getInt(LAND_QQS_TOP_MARGIN, 100));
        int[] landQqsMargin = new int[]{Prefs.getInt(LAND_QQS_TOP_MARGIN, 100)};

        binding.landQqsTopMarginSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                landQqsMargin[0] = progress;
                binding.landQqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Landscape qs margin
        binding.landQsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(LAND_QS_TOP_MARGIN, 100) + "dp");
        binding.landQsTopMarginSeekbar.setProgress(Prefs.getInt(LAND_QS_TOP_MARGIN, 100));
        int[] landQsMargin = new int[]{Prefs.getInt(LAND_QS_TOP_MARGIN, 100)};

        binding.landQsTopMarginSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                landQsMargin[0] = progress;
                binding.landQsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Apply and reset button
        if (Prefs.getBoolean("IconifyComponentHSIZE1.overlay") || Prefs.getBoolean("IconifyComponentHSIZE2.overlay"))
            binding.qsMarginReset.setVisibility(View.VISIBLE);

        binding.qsMarginApply.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                SystemUtil.getStoragePermission(this);
            } else {
                // Show loading dialog
                loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));
                AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                Runnable runnable = () -> {
                    try {
                        hasErroredOut.set(QsMarginManager.buildOverlay(portQqsMargin[0], portQsMargin[0], landQqsMargin[0], landQsMargin[0], true));
                    } catch (IOException e) {
                        hasErroredOut.set(true);
                    }

                    runOnUiThread(() -> {
                        if (!hasErroredOut.get()) {
                            Prefs.putInt(PORT_QQS_TOP_MARGIN, portQqsMargin[0]);
                            Prefs.putInt(PORT_QS_TOP_MARGIN, portQsMargin[0]);
                            Prefs.putInt(LAND_QQS_TOP_MARGIN, landQqsMargin[0]);
                            Prefs.putInt(LAND_QS_TOP_MARGIN, landQsMargin[0]);
                            RPrefs.putInt(HEADER_QQS_TOPMARGIN, portQqsMargin[0]);

                            binding.qsMarginReset.setVisibility(View.VISIBLE);
                        } else {
                            RPrefs.clearPref(HEADER_QQS_TOPMARGIN);
                        }

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            // Hide loading dialog
                            loadingDialog.hide();

                            if (hasErroredOut.get())
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                        }, 2000);
                    });
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });

        binding.qsMarginReset.setOnClickListener(v -> {
            Prefs.clearPrefs(PORT_QQS_TOP_MARGIN, PORT_QS_TOP_MARGIN, LAND_QQS_TOP_MARGIN, LAND_QS_TOP_MARGIN);
            RPrefs.clearPref(HEADER_QQS_TOPMARGIN);

            portQqsMargin[0] = 100;
            portQsMargin[0] = 100;
            landQqsMargin[0] = 100;
            landQsMargin[0] = 100;

            binding.portQqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + "100dp");
            binding.portQsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + "100dp");
            binding.landQqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + "100dp");
            binding.landQsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + "100dp");

            binding.portQqsTopMarginSeekbar.setProgress(100);
            binding.portQsTopMarginSeekbar.setProgress(100);
            binding.landQqsTopMarginSeekbar.setProgress(100);
            binding.landQsTopMarginSeekbar.setProgress(100);

            binding.qsMarginReset.setVisibility(View.GONE);

            OverlayUtil.disableOverlays("IconifyComponentHSIZE1.overlay", "IconifyComponentHSIZE2.overlay");
        });
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}