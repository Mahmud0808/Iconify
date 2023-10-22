package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_ALPHA;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_LANDSCAPE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_ZOOMTOFIT;
import static com.drdisagree.iconify.common.Resources.HEADER_IMAGE_DIR;
import static com.drdisagree.iconify.utils.FileUtil.copyToIconifyHiddenDir;
import static com.drdisagree.iconify.utils.FileUtil.getRealPath;

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
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.slider.Slider;

public class XposedHeaderImage extends BaseFragment {

    private FragmentXposedHeaderImageBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXposedHeaderImageBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_header_image);

        // Header image picker
        binding.pickHeaderImage.setOnClickListener(v -> {
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                browseHeaderImage();
            }
        });

        binding.disableHeaderImage.setVisibility(RPrefs.getBoolean(HEADER_IMAGE_SWITCH, false) ? View.VISIBLE : View.GONE);

        binding.enableHeaderImage.setOnClickListener(v -> {
            RPrefs.putBoolean(HEADER_IMAGE_SWITCH, false);
            RPrefs.putBoolean(HEADER_IMAGE_SWITCH, true);
            binding.enableHeaderImage.setVisibility(View.GONE);
            binding.disableHeaderImage.setVisibility(View.VISIBLE);
        });

        binding.disableHeaderImage.setOnClickListener(v -> {
            RPrefs.putBoolean(HEADER_IMAGE_SWITCH, false);
            binding.disableHeaderImage.setVisibility(View.GONE);
        });

        // Image height
        binding.headerImageHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(HEADER_IMAGE_HEIGHT, 140) + "dp");
        binding.headerImageHeightSeekbar.setValue(RPrefs.getInt(HEADER_IMAGE_HEIGHT, 140));
        final int[] imageHeight = {RPrefs.getInt(HEADER_IMAGE_HEIGHT, 140)};
        binding.headerImageHeightSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                imageHeight[0] = (int) slider.getValue();
                binding.headerImageHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + imageHeight[0] + "dp");
                RPrefs.putInt(HEADER_IMAGE_HEIGHT, imageHeight[0]);
            }
        });

        // Image alpha
        binding.imageAlphaOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(HEADER_IMAGE_ALPHA, 100) + "%");
        binding.imageAlphaSeekbar.setValue(RPrefs.getInt(HEADER_IMAGE_ALPHA, 100));
        final int[] imageAlpha = {RPrefs.getInt(HEADER_IMAGE_ALPHA, 100)};
        binding.imageAlphaSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                imageAlpha[0] = (int) slider.getValue();
                binding.imageAlphaOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + imageAlpha[0] + "%");
                RPrefs.putInt(HEADER_IMAGE_ALPHA, imageAlpha[0]);
            }
        });

        // Header image zoom to fit
        binding.enableZoomToFit.setChecked(RPrefs.getBoolean(HEADER_IMAGE_ZOOMTOFIT, false));
        binding.enableZoomToFit.setOnCheckedChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(HEADER_IMAGE_ZOOMTOFIT, isChecked));
        binding.enableZoomToFitContainer.setOnClickListener(v -> binding.enableZoomToFit.toggle());

        // Header image hide in landscape
        binding.enableHideImageLandscape.setChecked(RPrefs.getBoolean(HEADER_IMAGE_LANDSCAPE_SWITCH, true));
        binding.enableHideImageLandscape.setOnCheckedChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(HEADER_IMAGE_LANDSCAPE_SWITCH, isChecked));
        binding.enableHideImageLandscapeContainer.setOnClickListener(v -> binding.enableHideImageLandscape.toggle());

        return view;
    }

    public void browseHeaderImage() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("image/*");
        startActivityIntent.launch(chooseFile);
    }

    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    if (path != null && copyToIconifyHiddenDir(path, HEADER_IMAGE_DIR)) {
                        binding.enableHeaderImage.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });
}