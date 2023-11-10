package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
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

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentQsTileSizeBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.dialogs.LoadingDialog;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceEntry;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager;
import com.google.android.material.slider.Slider;

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
        int[] portNonExpandedHeight = new int[]{Prefs.getInt(PORT_QSTILE_NONEXPANDED_HEIGHT, 60)};
        binding.portNonexpandedHeight.setSliderValue(portNonExpandedHeight[0]);
        binding.portNonexpandedHeight.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                portNonExpandedHeight[0] = (int) slider.getValue();
            }
        });

        // Reset button
        binding.portNonexpandedHeight.setResetClickListener(v -> {
            portNonExpandedHeight[0] = 60;
            return true;
        });

        // Portrait Expanded height
        int[] portExpandedHeight = new int[]{Prefs.getInt(PORT_QSTILE_EXPANDED_HEIGHT, 80)};
        binding.portExpandedHeight.setSliderValue(portExpandedHeight[0]);
        binding.portExpandedHeight.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                portExpandedHeight[0] = (int) slider.getValue();

            }
        });

        // Reset button
        binding.portExpandedHeight.setResetClickListener(v -> {
            portExpandedHeight[0] = 80;
            return true;
        });

        // Landscape Nonexpanded height
        int[] landNonExpandedHeight = new int[]{Prefs.getInt(LAND_QSTILE_NONEXPANDED_HEIGHT, 60)};
        binding.landNonexpandedHeight.setSliderValue(landNonExpandedHeight[0]);
        binding.landNonexpandedHeight.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                landNonExpandedHeight[0] = (int) slider.getValue();
            }
        });

        // Reset button
        binding.landNonexpandedHeight.setResetClickListener(v -> {
            landNonExpandedHeight[0] = 60;
            return true;
        });

        // Landscape Expanded height
        int[] landExpandedHeight = new int[]{Prefs.getInt(LAND_QSTILE_EXPANDED_HEIGHT, 80)};
        binding.landExpandedHeight.setSliderValue(landExpandedHeight[0]);
        binding.landExpandedHeight.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                landExpandedHeight[0] = (int) slider.getValue();
            }
        });

        // Reset button
        binding.landExpandedHeight.setResetClickListener(v -> {
            landExpandedHeight[0] = 80;
            return true;
        });

        // Apply and reset button
        if (isQsTileHeightEnabled()) {
            binding.qsTileHeightReset.setVisibility(View.VISIBLE);
        }

        binding.qsTileHeightApply.setOnClickListener(v -> {
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                // Show loading dialog
                loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

                ResourceEntry qsTileNonExpandedPort = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_quick_tile_size", portNonExpandedHeight[0] + "dp");
                qsTileNonExpandedPort.setPortrait(true);
                ResourceEntry qsTileExpandedPort = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_tile_height", portExpandedHeight[0] + "dp");
                qsTileExpandedPort.setPortrait(true);
                ResourceEntry qsTileNonExpandedLand = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_quick_tile_size", landNonExpandedHeight[0] + "dp");
                qsTileNonExpandedLand.setLandscape(true);
                ResourceEntry qsTileExpandedLand = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_tile_height", landExpandedHeight[0] + "dp");
                qsTileExpandedLand.setLandscape(true);

                new Handler(Looper.getMainLooper()).post(() -> {
                    AtomicBoolean hasErroredOut = new AtomicBoolean(ResourceManager.buildOverlayWithResource(
                            qsTileNonExpandedPort,
                            qsTileExpandedPort,
                            qsTileNonExpandedLand,
                            qsTileExpandedLand
                    ));

                    if (!hasErroredOut.get()) {
                        Prefs.putInt(PORT_QSTILE_NONEXPANDED_HEIGHT, portNonExpandedHeight[0]);
                        Prefs.putInt(PORT_QSTILE_EXPANDED_HEIGHT, portExpandedHeight[0]);
                        Prefs.putInt(LAND_QSTILE_NONEXPANDED_HEIGHT, landNonExpandedHeight[0]);
                        Prefs.putInt(LAND_QSTILE_EXPANDED_HEIGHT, landExpandedHeight[0]);
                    }

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        if (!hasErroredOut.get()) {
                            binding.qsTileHeightReset.setVisibility(View.VISIBLE);
                        }
                    }, 2000);
                });
            }
        });

        binding.qsTileHeightReset.setOnClickListener(v -> {
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                // Show loading dialog
                loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

                ResourceEntry qsTileNonExpandedPort = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_quick_tile_size");
                qsTileNonExpandedPort.setPortrait(true);
                ResourceEntry qsTileExpandedPort = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_tile_height");
                qsTileExpandedPort.setPortrait(true);
                ResourceEntry qsTileNonExpandedLand = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_quick_tile_size");
                qsTileNonExpandedLand.setLandscape(true);
                ResourceEntry qsTileExpandedLand = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_tile_height");
                qsTileExpandedLand.setLandscape(true);

                new Handler(Looper.getMainLooper()).post(() -> {
                    AtomicBoolean hasErroredOut = new AtomicBoolean(ResourceManager.removeResourceFromOverlay(
                            qsTileNonExpandedPort,
                            qsTileExpandedPort,
                            qsTileNonExpandedLand,
                            qsTileExpandedLand
                    ));

                    if (!hasErroredOut.get()) {
                        Prefs.clearPrefs(
                                PORT_QSTILE_NONEXPANDED_HEIGHT,
                                PORT_QSTILE_EXPANDED_HEIGHT,
                                LAND_QSTILE_NONEXPANDED_HEIGHT,
                                LAND_QSTILE_EXPANDED_HEIGHT
                        );

                        portNonExpandedHeight[0] = 60;
                        portExpandedHeight[0] = 80;
                        landNonExpandedHeight[0] = 60;
                        landExpandedHeight[0] = 80;

                        binding.portNonexpandedHeight.resetSlider();
                        binding.portExpandedHeight.resetSlider();
                        binding.landNonexpandedHeight.resetSlider();
                        binding.landExpandedHeight.resetSlider();
                    }

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        if (!hasErroredOut.get()) {
                            binding.qsTileHeightReset.setVisibility(View.GONE);
                        }
                    }, 2000);
                });
            }
        });

        return view;
    }

    private boolean isQsTileHeightEnabled() {
        return Prefs.getInt(PORT_QSTILE_NONEXPANDED_HEIGHT, 60) != 60 ||
                Prefs.getInt(PORT_QSTILE_EXPANDED_HEIGHT, 80) != 80 ||
                Prefs.getInt(LAND_QSTILE_NONEXPANDED_HEIGHT, 60) != 60 ||
                Prefs.getInt(LAND_QSTILE_EXPANDED_HEIGHT, 80) != 80;
    }

    @Override
    public void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
}