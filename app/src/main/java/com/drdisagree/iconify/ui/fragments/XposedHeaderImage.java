package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_ALPHA;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_BOTTOM_FADE_AMOUNT;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_LANDSCAPE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_ZOOMTOFIT;
import static com.drdisagree.iconify.common.Resources.HEADER_IMAGE_DIR;
import static com.drdisagree.iconify.utils.FileUtil.getRealPath;
import static com.drdisagree.iconify.utils.FileUtil.moveToIconifyHiddenDir;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentXposedHeaderImageBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.google.android.material.slider.Slider;

public class XposedHeaderImage extends BaseFragment {

    private FragmentXposedHeaderImageBinding binding;
    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    if (path != null && moveToIconifyHiddenDir(path, HEADER_IMAGE_DIR)) {
                        binding.headerImage.setEnableButtonVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(Iconify.Companion.getAppContext(), getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXposedHeaderImageBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_header_image);

        // Header image picker
        binding.headerImage.setActivityResultLauncher(startActivityIntent);

        binding.headerImage.setEnableButtonOnClickListener(v -> {
            RPrefs.putBoolean(HEADER_IMAGE_SWITCH, false);
            RPrefs.putBoolean(HEADER_IMAGE_SWITCH, true);
            binding.headerImage.setEnableButtonVisibility(View.GONE);
            binding.headerImage.setDisableButtonVisibility(View.VISIBLE);
            updateEnabledState();
        });

        binding.headerImage.setDisableButtonVisibility(RPrefs.getBoolean(HEADER_IMAGE_SWITCH, false) ? View.VISIBLE : View.GONE);
        binding.headerImage.setDisableButtonOnClickListener(v -> {
            RPrefs.putBoolean(HEADER_IMAGE_SWITCH, false);
            binding.headerImage.setDisableButtonVisibility(View.GONE);
            updateEnabledState();
        });

        // Image height
        binding.headerImageHeight.setSliderValue(RPrefs.getInt(HEADER_IMAGE_HEIGHT, 140));
        binding.headerImageHeight.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                RPrefs.putInt(HEADER_IMAGE_HEIGHT, (int) slider.getValue());
            }
        });

        // Image alpha
        binding.headerImageAlpha.setSliderValue(RPrefs.getInt(HEADER_IMAGE_ALPHA, 100));
        binding.headerImageAlpha.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                RPrefs.putInt(HEADER_IMAGE_ALPHA, (int) slider.getValue());
            }
        });

        // Image bottom fade amount
        binding.headerImageBottomFade.setSliderValue(RPrefs.getInt(HEADER_IMAGE_BOTTOM_FADE_AMOUNT, 40));
        binding.headerImageBottomFade.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                RPrefs.putInt(HEADER_IMAGE_BOTTOM_FADE_AMOUNT, (int) slider.getValue());
            }
        });

        // Header image zoom to fit
        binding.zoomToFit.setSwitchChecked(RPrefs.getBoolean(HEADER_IMAGE_ZOOMTOFIT, false));
        binding.zoomToFit.setSwitchChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(HEADER_IMAGE_ZOOMTOFIT, isChecked));

        // Header image hide in landscape
        binding.hideInLandscape.setSwitchChecked(RPrefs.getBoolean(HEADER_IMAGE_LANDSCAPE_SWITCH, true));
        binding.hideInLandscape.setSwitchChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(HEADER_IMAGE_LANDSCAPE_SWITCH, isChecked));

        updateEnabledState();

        return view;
    }

    private void updateEnabledState() {
        boolean enabled = RPrefs.getBoolean(HEADER_IMAGE_SWITCH, false);

        binding.headerImageHeight.setEnabled(enabled);
        binding.headerImageAlpha.setEnabled(enabled);
        binding.zoomToFit.setEnabled(enabled);
        binding.hideInLandscape.setEnabled(enabled);
    }
}