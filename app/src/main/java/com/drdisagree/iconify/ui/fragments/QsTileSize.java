package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Preferences.LAND_QSTILE_EXPANDED_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.LAND_QSTILE_NONEXPANDED_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.PORT_QSTILE_EXPANDED_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.PORT_QSTILE_NONEXPANDED_HEIGHT;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentQsTileSizeBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.dialogs.LoadingDialog;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.drdisagree.iconify.utils.overlay.manager.QsTileHeightManager;
import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class QsTileSize extends BaseFragment {

    private FragmentQsTileSizeBinding binding;
    private LoadingDialog loadingDialog;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQsTileSizeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_qs_tile_size);

        // Show loading dialog
        loadingDialog = new LoadingDialog(requireContext());

        // Portrait Nonexpanded height
        binding.portNonexpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(PORT_QSTILE_NONEXPANDED_HEIGHT, 60) + "dp");
        binding.portNonexpandedHeightSeekbar.setValue(Prefs.getInt(PORT_QSTILE_NONEXPANDED_HEIGHT, 60));

        // Reset button
        binding.resetPortNonexpandedHeight.setVisibility(Prefs.getInt(PORT_QSTILE_NONEXPANDED_HEIGHT, 60) == 60 ? View.INVISIBLE : View.VISIBLE);
        int[] portNonExpandedHeight = new int[]{Prefs.getInt(PORT_QSTILE_NONEXPANDED_HEIGHT, 60)};

        binding.resetPortNonexpandedHeight.setOnLongClickListener(v -> {
            portNonExpandedHeight[0] = 60;
            binding.portNonexpandedHeightSeekbar.setValue(60);
            binding.portNonexpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + portNonExpandedHeight[0] + "dp");
            binding.resetPortNonexpandedHeight.setVisibility(View.INVISIBLE);
            return true;
        });

        binding.portNonexpandedHeightSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                portNonExpandedHeight[0] = (int) slider.getValue();
                if (portNonExpandedHeight[0] == 60)
                    binding.resetPortNonexpandedHeight.setVisibility(View.INVISIBLE);
                binding.portNonexpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + portNonExpandedHeight[0] + "dp");
                binding.resetPortNonexpandedHeight.setVisibility(portNonExpandedHeight[0] == 60 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Portrait Expanded height
        binding.portExpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(PORT_QSTILE_EXPANDED_HEIGHT, 80) + "dp");
        binding.portExpandedHeightSeekbar.setValue(Prefs.getInt(PORT_QSTILE_EXPANDED_HEIGHT, 80));

        // Reset button
        binding.resetPortExpandedHeight.setVisibility(Prefs.getInt(PORT_QSTILE_EXPANDED_HEIGHT, 80) == 80 ? View.INVISIBLE : View.VISIBLE);
        int[] portExpandedHeight = new int[]{Prefs.getInt(PORT_QSTILE_EXPANDED_HEIGHT, 80)};

        binding.resetPortExpandedHeight.setOnLongClickListener(v -> {
            portExpandedHeight[0] = 80;
            binding.portExpandedHeightSeekbar.setValue(80);
            binding.portExpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + portExpandedHeight[0] + "dp");
            binding.resetPortExpandedHeight.setVisibility(View.INVISIBLE);
            return true;
        });

        binding.portExpandedHeightSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                portExpandedHeight[0] = (int) slider.getValue();
                if (portExpandedHeight[0] == 80)
                    binding.resetPortExpandedHeight.setVisibility(View.INVISIBLE);
                binding.portExpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + portExpandedHeight[0] + "dp");
                binding.resetPortExpandedHeight.setVisibility(portExpandedHeight[0] == 80 ? View.INVISIBLE : View.VISIBLE);

            }
        });

        // Landscape Nonexpanded height
        binding.landNonexpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(LAND_QSTILE_NONEXPANDED_HEIGHT, 60) + "dp");
        binding.landNonexpandedHeightSeekbar.setValue(Prefs.getInt(LAND_QSTILE_NONEXPANDED_HEIGHT, 60));

        // Reset button
        binding.resetLandNonexpandedHeight.setVisibility(Prefs.getInt(LAND_QSTILE_NONEXPANDED_HEIGHT, 60) == 60 ? View.INVISIBLE : View.VISIBLE);
        int[] landNonExpandedHeight = new int[]{Prefs.getInt(LAND_QSTILE_NONEXPANDED_HEIGHT, 60)};

        binding.resetLandNonexpandedHeight.setOnLongClickListener(v -> {
            landNonExpandedHeight[0] = 60;
            binding.landNonexpandedHeightSeekbar.setValue(60);
            binding.landNonexpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + landNonExpandedHeight[0] + "dp");
            binding.resetLandNonexpandedHeight.setVisibility(View.INVISIBLE);
            return true;
        });

        binding.landNonexpandedHeightSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                landNonExpandedHeight[0] = (int) slider.getValue();
                if (landNonExpandedHeight[0] == 60)
                    binding.resetLandNonexpandedHeight.setVisibility(View.INVISIBLE);
                binding.landNonexpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + landNonExpandedHeight[0] + "dp");
                binding.resetLandNonexpandedHeight.setVisibility(landNonExpandedHeight[0] == 60 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Landscape Expanded height
        binding.landExpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(LAND_QSTILE_EXPANDED_HEIGHT, 80) + "dp");
        binding.landExpandedHeightSeekbar.setValue(Prefs.getInt(LAND_QSTILE_EXPANDED_HEIGHT, 80));

        // Reset button
        binding.resetLandExpandedHeight.setVisibility(Prefs.getInt(LAND_QSTILE_EXPANDED_HEIGHT, 80) == 80 ? View.INVISIBLE : View.VISIBLE);
        int[] landExpandedHeight = new int[]{Prefs.getInt(LAND_QSTILE_EXPANDED_HEIGHT, 80)};

        binding.resetLandExpandedHeight.setOnLongClickListener(v -> {
            landExpandedHeight[0] = 80;
            binding.landExpandedHeightSeekbar.setValue(80);
            binding.landExpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + landExpandedHeight[0] + "dp");
            binding.resetLandExpandedHeight.setVisibility(View.INVISIBLE);
            return true;
        });

        binding.landExpandedHeightSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                landExpandedHeight[0] = (int) slider.getValue();
                if (landExpandedHeight[0] == 80)
                    binding.resetLandExpandedHeight.setVisibility(View.INVISIBLE);
                binding.landExpandedHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + landExpandedHeight[0] + "dp");
                binding.resetLandExpandedHeight.setVisibility(landExpandedHeight[0] == 80 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Apply and reset button
        if (Prefs.getBoolean("IconifyComponentQSTH.overlay"))
            binding.qsTileHeightReset.setVisibility(View.VISIBLE);

        binding.qsTileHeightApply.setOnClickListener(v -> {
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                // Show loading dialog
                loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));
                AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                new Thread(() -> {
                    try {
                        hasErroredOut.set(QsTileHeightManager.buildOverlay(portNonExpandedHeight[0], portExpandedHeight[0], landNonExpandedHeight[0], landExpandedHeight[0], true));
                    } catch (IOException e) {
                        hasErroredOut.set(true);
                    }

                    new Handler(Looper.getMainLooper()).post(() -> {
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
                                Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                        }, 2000);
                    });
                }).start();
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

            binding.portNonexpandedHeightSeekbar.setValue(60);
            binding.portExpandedHeightSeekbar.setValue(80);
            binding.landNonexpandedHeightSeekbar.setValue(60);
            binding.landExpandedHeightSeekbar.setValue(80);

            binding.resetPortNonexpandedHeight.setVisibility(View.INVISIBLE);
            binding.resetPortExpandedHeight.setVisibility(View.INVISIBLE);
            binding.resetLandNonexpandedHeight.setVisibility(View.INVISIBLE);
            binding.resetLandExpandedHeight.setVisibility(View.INVISIBLE);

            binding.qsTileHeightReset.setVisibility(View.GONE);

            OverlayUtil.disableOverlay("IconifyComponentQSTH.overlay");
        });

        return view;
    }

    @Override
    public void onDestroy() {
        loadingDialog.dismiss();
        super.onDestroy();
    }
}