package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.LAND_QSTILE_EXPANDED_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.LAND_QSTILE_NONEXPANDED_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.PORT_QSTILE_EXPANDED_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.PORT_QSTILE_NONEXPANDED_HEIGHT;

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
import com.drdisagree.iconify.databinding.ActivityQsTileSizeBinding;
import com.drdisagree.iconify.overlaymanager.QsTileHeightManager;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class QsTileSize extends BaseActivity {

    private ActivityQsTileSizeBinding binding;
    private LoadingDialog loadingDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQsTileSizeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.collapsingToolbar, binding.header.toolbar, R.string.activity_title_qs_tile_size);

        // Show loading dialog
        loadingDialog = new LoadingDialog(this);

        // Portrait Nonexpanded height
        binding.portNonexpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(PORT_QSTILE_NONEXPANDED_HEIGHT, 60) + "dp");
        binding.portNonexpandedHeightSeekbar.setProgress(Prefs.getInt(PORT_QSTILE_NONEXPANDED_HEIGHT, 60));

        // Reset button
        binding.resetPortNonexpandedHeight.setVisibility(Prefs.getInt(PORT_QSTILE_NONEXPANDED_HEIGHT, 60) == 60 ? View.INVISIBLE : View.VISIBLE);
        int[] portNonExpandedHeight = new int[]{Prefs.getInt(PORT_QSTILE_NONEXPANDED_HEIGHT, 60)};

        binding.resetPortNonexpandedHeight.setOnLongClickListener(v -> {
            portNonExpandedHeight[0] = 60;
            binding.portNonexpandedHeightSeekbar.setProgress(60);
            binding.resetPortNonexpandedHeight.setVisibility(View.INVISIBLE);
            return true;
        });

        binding.portNonexpandedHeightSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                portNonExpandedHeight[0] = progress;
                if (progress == 60)
                    binding.resetPortNonexpandedHeight.setVisibility(View.INVISIBLE);
                binding.portNonexpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.resetPortNonexpandedHeight.setVisibility(portNonExpandedHeight[0] == 60 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Portrait Expanded height
        binding.portExpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(PORT_QSTILE_EXPANDED_HEIGHT, 80) + "dp");
        binding.portExpandedHeightSeekbar.setProgress(Prefs.getInt(PORT_QSTILE_EXPANDED_HEIGHT, 80));

        // Reset button
        binding.resetPortExpandedHeight.setVisibility(Prefs.getInt(PORT_QSTILE_EXPANDED_HEIGHT, 80) == 80 ? View.INVISIBLE : View.VISIBLE);
        int[] portExpandedHeight = new int[]{Prefs.getInt(PORT_QSTILE_EXPANDED_HEIGHT, 80)};

        binding.resetPortExpandedHeight.setOnLongClickListener(v -> {
            portExpandedHeight[0] = 80;
            binding.portExpandedHeightSeekbar.setProgress(80);
            binding.resetPortExpandedHeight.setVisibility(View.INVISIBLE);
            return true;
        });

        binding.portExpandedHeightSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                portExpandedHeight[0] = progress;
                if (progress == 80) binding.resetPortExpandedHeight.setVisibility(View.INVISIBLE);
                binding.portExpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.resetPortExpandedHeight.setVisibility(portExpandedHeight[0] == 80 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Landscape Nonexpanded height
        binding.landNonexpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(LAND_QSTILE_NONEXPANDED_HEIGHT, 60) + "dp");
        binding.landNonexpandedHeightSeekbar.setProgress(Prefs.getInt(LAND_QSTILE_NONEXPANDED_HEIGHT, 60));

        // Reset button
        binding.resetLandNonexpandedHeight.setVisibility(Prefs.getInt(LAND_QSTILE_NONEXPANDED_HEIGHT, 60) == 60 ? View.INVISIBLE : View.VISIBLE);
        int[] landNonExpandedHeight = new int[]{Prefs.getInt(LAND_QSTILE_NONEXPANDED_HEIGHT, 60)};

        binding.resetLandNonexpandedHeight.setOnLongClickListener(v -> {
            landNonExpandedHeight[0] = 60;
            binding.landNonexpandedHeightSeekbar.setProgress(60);
            binding.resetLandNonexpandedHeight.setVisibility(View.INVISIBLE);
            return true;
        });

        binding.landNonexpandedHeightSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                landNonExpandedHeight[0] = progress;
                if (progress == 60)
                    binding.resetLandNonexpandedHeight.setVisibility(View.INVISIBLE);
                binding.landNonexpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.resetLandNonexpandedHeight.setVisibility(landNonExpandedHeight[0] == 60 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Landscape Expanded height
        binding.landExpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(LAND_QSTILE_EXPANDED_HEIGHT, 80) + "dp");
        binding.landExpandedHeightSeekbar.setProgress(Prefs.getInt(LAND_QSTILE_EXPANDED_HEIGHT, 80));

        // Reset button
        binding.resetLandExpandedHeight.setVisibility(Prefs.getInt(LAND_QSTILE_EXPANDED_HEIGHT, 80) == 80 ? View.INVISIBLE : View.VISIBLE);
        int[] landExpandedHeight = new int[]{Prefs.getInt(LAND_QSTILE_EXPANDED_HEIGHT, 80)};

        binding.resetLandExpandedHeight.setOnLongClickListener(v -> {
            landExpandedHeight[0] = 80;
            binding.landExpandedHeightSeekbar.setProgress(80);
            binding.resetLandExpandedHeight.setVisibility(View.INVISIBLE);
            return true;
        });

        binding.landExpandedHeightSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                landExpandedHeight[0] = progress;
                if (progress == 80) binding.resetLandExpandedHeight.setVisibility(View.INVISIBLE);
                binding.landExpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.resetLandExpandedHeight.setVisibility(landExpandedHeight[0] == 80 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Apply and reset button
        if (Prefs.getBoolean("IconifyComponentQSTH.overlay"))
            binding.qsTileHeightApply.setVisibility(View.VISIBLE);

        binding.qsTileHeightApply.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                SystemUtil.getStoragePermission(this);
            } else {
                // Show loading dialog
                loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));
                AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                Runnable runnable = () -> {
                    try {
                        hasErroredOut.set(QsTileHeightManager.enableOverlay(portNonExpandedHeight[0], portExpandedHeight[0], landNonExpandedHeight[0], landExpandedHeight[0], true));
                    } catch (IOException e) {
                        hasErroredOut.set(true);
                    }

                    runOnUiThread(() -> {
                        if (!hasErroredOut.get()) {
                            Prefs.putInt(PORT_QSTILE_NONEXPANDED_HEIGHT, portNonExpandedHeight[0]);
                            Prefs.putInt(PORT_QSTILE_EXPANDED_HEIGHT, portExpandedHeight[0]);
                            Prefs.putInt(LAND_QSTILE_NONEXPANDED_HEIGHT, landNonExpandedHeight[0]);
                            Prefs.putInt(LAND_QSTILE_EXPANDED_HEIGHT, landExpandedHeight[0]);

                            binding.qsTileHeightReset.setVisibility(View.VISIBLE);
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

        binding.qsTileHeightReset.setOnClickListener(v -> {
            Prefs.clearPrefs(PORT_QSTILE_NONEXPANDED_HEIGHT, PORT_QSTILE_EXPANDED_HEIGHT, LAND_QSTILE_NONEXPANDED_HEIGHT, LAND_QSTILE_EXPANDED_HEIGHT);

            portNonExpandedHeight[0] = 60;
            portExpandedHeight[0] = 80;
            landNonExpandedHeight[0] = 60;
            landExpandedHeight[0] = 80;

            binding.portNonexpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + "60dp");
            binding.portExpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + "80dp");
            binding.landNonexpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + "60dp");
            binding.landExpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + "80dp");

            binding.portNonexpandedHeightSeekbar.setProgress(60);
            binding.portExpandedHeightSeekbar.setProgress(80);
            binding.landNonexpandedHeightSeekbar.setProgress(60);
            binding.landExpandedHeightSeekbar.setProgress(80);

            binding.resetPortNonexpandedHeight.setVisibility(View.INVISIBLE);
            binding.resetPortExpandedHeight.setVisibility(View.INVISIBLE);
            binding.resetLandNonexpandedHeight.setVisibility(View.INVISIBLE);
            binding.resetLandExpandedHeight.setVisibility(View.INVISIBLE);

            binding.qsTileHeightReset.setVisibility(View.GONE);

            OverlayUtil.disableOverlay("IconifyComponentQSTH.overlay");
        });
    }
}