package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.LAND_QQS_TOP_MARGIN;
import static com.drdisagree.iconify.common.Preferences.LAND_QS_TOP_MARGIN;
import static com.drdisagree.iconify.common.Preferences.PORT_QQS_TOP_MARGIN;
import static com.drdisagree.iconify.common.Preferences.PORT_QS_TOP_MARGIN;

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
import com.drdisagree.iconify.databinding.FragmentQsPanelMarginBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.dialogs.LoadingDialog;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceEntry;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager;
import com.google.android.material.slider.Slider;

import java.util.concurrent.atomic.AtomicBoolean;

public class QsPanelMargin extends BaseFragment {

    private FragmentQsPanelMarginBinding binding;
    private LoadingDialog loadingDialog;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQsPanelMarginBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_qs_panel_margin);

        // Show loading dialog
        loadingDialog = new LoadingDialog(requireContext());

        // Portrait qqs margin
        binding.portQqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(PORT_QQS_TOP_MARGIN, 100) + "dp");
        binding.portQqsTopMarginSeekbar.setValue(Prefs.getInt(PORT_QQS_TOP_MARGIN, 100));
        int[] portQqsMargin = new int[]{Prefs.getInt(PORT_QQS_TOP_MARGIN, 100)};

        binding.portQqsTopMarginSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                portQqsMargin[0] = (int) slider.getValue();
                binding.portQqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + portQqsMargin[0] + "dp");
            }
        });

        // Portrait qs margin
        binding.portQsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(PORT_QS_TOP_MARGIN, 100) + "dp");
        binding.portQsTopMarginSeekbar.setValue(Prefs.getInt(PORT_QS_TOP_MARGIN, 100));
        int[] portQsMargin = new int[]{Prefs.getInt(PORT_QS_TOP_MARGIN, 100)};

        binding.portQsTopMarginSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                portQsMargin[0] = (int) slider.getValue();
                binding.portQsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + portQsMargin[0] + "dp");
            }
        });

        // Landscape qqs margin
        binding.landQqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(LAND_QQS_TOP_MARGIN, 100) + "dp");
        binding.landQqsTopMarginSeekbar.setValue(Prefs.getInt(LAND_QQS_TOP_MARGIN, 100));
        int[] landQqsMargin = new int[]{Prefs.getInt(LAND_QQS_TOP_MARGIN, 100)};

        binding.landQqsTopMarginSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                landQqsMargin[0] = (int) slider.getValue();
                binding.landQqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + landQqsMargin[0] + "dp");
            }
        });

        // Landscape qs margin
        binding.landQsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(LAND_QS_TOP_MARGIN, 100) + "dp");
        binding.landQsTopMarginSeekbar.setValue(Prefs.getInt(LAND_QS_TOP_MARGIN, 100));
        int[] landQsMargin = new int[]{Prefs.getInt(LAND_QS_TOP_MARGIN, 100)};

        binding.landQsTopMarginSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                landQsMargin[0] = (int) slider.getValue();
                binding.landQsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + landQsMargin[0] + "dp");
            }
        });

        // Apply and reset button
        if (isQsMarginEnabled()) {
            binding.qsMarginReset.setVisibility(View.VISIBLE);
        }

        binding.qsMarginApply.setOnClickListener(v -> {
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                // Show loading dialog
                loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

                // Framework portrait
                ResourceEntry qqsMarginPortF = new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "quick_qs_offset_height", portQqsMargin[0] + "dp");
                qqsMarginPortF.setPortrait(true);
                ResourceEntry qsMarginPortF = new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "quick_qs_total_height", portQsMargin[0] + "dp");
                qsMarginPortF.setPortrait(true);

                // Framework landscape
                ResourceEntry qqsMarginLandF = new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "quick_qs_offset_height", landQqsMargin[0] + "dp");
                qqsMarginLandF.setLandscape(true);
                ResourceEntry qsMarginLandF = new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "quick_qs_total_height", landQsMargin[0] + "dp");
                qsMarginLandF.setLandscape(true);

                // SystemUI portrait
                ResourceEntry qqsMarginPortS1 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qqs_layout_margin_top", portQqsMargin[0] + "dp");
                qqsMarginPortS1.setPortrait(true);
                ResourceEntry qqsMarginPortS2 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_header_row_min_height", portQqsMargin[0] + "dp");
                qqsMarginPortS2.setPortrait(true);
                ResourceEntry qsMarginPortS1 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_panel_padding_top", portQsMargin[0] + "dp");
                qsMarginPortS1.setPortrait(true);
                ResourceEntry qsMarginPortS2 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_panel_padding_top_combined_headers", portQsMargin[0] + "dp");
                qsMarginPortS2.setPortrait(true);

                // SystemUI landscape
                ResourceEntry qqsMarginLandS1 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qqs_layout_margin_top", landQqsMargin[0] + "dp");
                qqsMarginLandS1.setLandscape(true);
                ResourceEntry qqsMarginLandS2 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_header_row_min_height", landQqsMargin[0] + "dp");
                qqsMarginLandS2.setLandscape(true);
                ResourceEntry qsMarginLandS1 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_panel_padding_top", landQsMargin[0] + "dp");
                qsMarginLandS1.setLandscape(true);
                ResourceEntry qsMarginLandS2 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_panel_padding_top_combined_headers", landQsMargin[0] + "dp");
                qsMarginLandS2.setLandscape(true);

                new Handler(Looper.getMainLooper()).post(() -> {
                    AtomicBoolean hasErroredOut = new AtomicBoolean(ResourceManager.buildOverlayWithResource(
                            qqsMarginPortF, qsMarginPortF, qqsMarginLandF, qsMarginLandF,
                            qqsMarginPortS1, qqsMarginPortS2, qsMarginPortS1, qsMarginPortS2,
                            qqsMarginLandS1, qqsMarginLandS2, qsMarginLandS1, qsMarginLandS2
                    ));

                    if (!hasErroredOut.get()) {
                        Prefs.putInt(PORT_QQS_TOP_MARGIN, portQqsMargin[0]);
                        Prefs.putInt(PORT_QS_TOP_MARGIN, portQsMargin[0]);
                        Prefs.putInt(LAND_QQS_TOP_MARGIN, landQqsMargin[0]);
                        Prefs.putInt(LAND_QS_TOP_MARGIN, landQsMargin[0]);
                    }

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        if (!hasErroredOut.get()) {
                            binding.qsMarginReset.setVisibility(View.VISIBLE);
                        }
                    }, 2000);
                });
            }
        });

        binding.qsMarginReset.setOnClickListener(v -> {
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                // Show loading dialog
                loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

                // Framework portrait
                ResourceEntry qqsMarginPortF = new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "quick_qs_offset_height");
                qqsMarginPortF.setPortrait(true);
                ResourceEntry qsMarginPortF = new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "quick_qs_total_height");
                qsMarginPortF.setPortrait(true);

                // Framework landscape
                ResourceEntry qqsMarginLandF = new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "quick_qs_offset_height");
                qqsMarginLandF.setLandscape(true);
                ResourceEntry qsMarginLandF = new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "quick_qs_total_height");
                qsMarginLandF.setLandscape(true);

                // SystemUI portrait
                ResourceEntry qqsMarginPortS1 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qqs_layout_margin_top");
                qqsMarginPortS1.setPortrait(true);
                ResourceEntry qqsMarginPortS2 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_header_row_min_height");
                qqsMarginPortS2.setPortrait(true);
                ResourceEntry qsMarginPortS1 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_panel_padding_top");
                qsMarginPortS1.setPortrait(true);
                ResourceEntry qsMarginPortS2 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_panel_padding_top_combined_headers");
                qsMarginPortS2.setPortrait(true);

                // SystemUI landscape
                ResourceEntry qqsMarginLandS1 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qqs_layout_margin_top");
                qqsMarginLandS1.setLandscape(true);
                ResourceEntry qqsMarginLandS2 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_header_row_min_height");
                qqsMarginLandS2.setLandscape(true);
                ResourceEntry qsMarginLandS1 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_panel_padding_top");
                qsMarginLandS1.setLandscape(true);
                ResourceEntry qsMarginLandS2 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_panel_padding_top_combined_headers");
                qsMarginLandS2.setLandscape(true);

                new Handler(Looper.getMainLooper()).post(() -> {
                    AtomicBoolean hasErroredOut = new AtomicBoolean(ResourceManager.removeResourceFromOverlay(
                            qqsMarginPortF, qsMarginPortF, qqsMarginLandF, qsMarginLandF,
                            qqsMarginPortS1, qqsMarginPortS2, qsMarginPortS1, qsMarginPortS2,
                            qqsMarginLandS1, qqsMarginLandS2, qsMarginLandS1, qsMarginLandS2
                    ));

                    if (!hasErroredOut.get()) {
                        Prefs.clearPrefs(
                                PORT_QQS_TOP_MARGIN,
                                PORT_QS_TOP_MARGIN,
                                LAND_QQS_TOP_MARGIN,
                                LAND_QS_TOP_MARGIN
                        );

                        portQqsMargin[0] = 100;
                        portQsMargin[0] = 100;
                        landQqsMargin[0] = 100;
                        landQsMargin[0] = 100;

                        binding.portQqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + "100dp");
                        binding.portQsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + "100dp");
                        binding.landQqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + "100dp");
                        binding.landQsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + "100dp");

                        binding.portQqsTopMarginSeekbar.setValue(100);
                        binding.portQsTopMarginSeekbar.setValue(100);
                        binding.landQqsTopMarginSeekbar.setValue(100);
                        binding.landQsTopMarginSeekbar.setValue(100);
                    }

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        if (!hasErroredOut.get()) {
                            binding.qsMarginReset.setVisibility(View.GONE);
                        }
                    }, 2000);
                });
            }
        });
        return view;
    }

    private boolean isQsMarginEnabled() {
        return Prefs.getInt(PORT_QQS_TOP_MARGIN, 100) != 100 ||
                Prefs.getInt(PORT_QS_TOP_MARGIN, 100) != 100 ||
                Prefs.getInt(LAND_QQS_TOP_MARGIN, 100) != 100 ||
                Prefs.getInt(LAND_QS_TOP_MARGIN, 100) != 100;
    }

    @Override
    public void onDestroy() {
        loadingDialog.dismiss();
        super.onDestroy();
    }
}