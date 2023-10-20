package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Preferences.HEADER_QQS_TOPMARGIN;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentQsPanelMarginBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.dialogs.LoadingDialog;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.drdisagree.iconify.utils.overlay.manager.QsMarginManager;
import com.google.android.material.slider.Slider;

import java.io.IOException;
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
        if (Prefs.getBoolean("IconifyComponentHSIZE1.overlay") || Prefs.getBoolean("IconifyComponentHSIZE2.overlay"))
            binding.qsMarginReset.setVisibility(View.VISIBLE);

        binding.qsMarginApply.setOnClickListener(v -> {
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                // Show loading dialog
                loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));
                AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                new Thread(() -> {
                    try {
                        hasErroredOut.set(QsMarginManager.buildOverlay(portQqsMargin[0], portQsMargin[0], landQqsMargin[0], landQsMargin[0], true));
                    } catch (IOException e) {
                        hasErroredOut.set(true);
                    }

                    new Handler(Looper.getMainLooper()).post(() -> {
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
                                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                        }, 2000);
                    });
                }).start();
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

            binding.portQqsTopMarginSeekbar.setValue(100);
            binding.portQsTopMarginSeekbar.setValue(100);
            binding.landQqsTopMarginSeekbar.setValue(100);
            binding.landQsTopMarginSeekbar.setValue(100);

            binding.qsMarginReset.setVisibility(View.GONE);

            OverlayUtil.disableOverlays("IconifyComponentHSIZE1.overlay", "IconifyComponentHSIZE2.overlay");
        });

        return view;
    }

    @Override
    public void onDestroy() {
        loadingDialog.dismiss();
        super.onDestroy();
    }
}