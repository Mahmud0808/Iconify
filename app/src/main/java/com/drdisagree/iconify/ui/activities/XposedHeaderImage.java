package com.drdisagree.iconify.ui.activities;

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
import android.os.Environment;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.ActivityXposedHeaderImageBinding;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.SystemUtil;

public class XposedHeaderImage extends BaseActivity {

    private ActivityXposedHeaderImageBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXposedHeaderImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.collapsingToolbar, binding.header.toolbar, R.string.activity_title_header_image);

        // Header image picker
        binding.pickHeaderImage.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                SystemUtil.getStoragePermission(this);
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
        binding.headerImageHeightSeekbar.setProgress(RPrefs.getInt(HEADER_IMAGE_HEIGHT, 140));
        final int[] imageHeight = {RPrefs.getInt(HEADER_IMAGE_HEIGHT, 140)};
        binding.headerImageHeightSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                imageHeight[0] = progress;
                binding.headerImageHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(HEADER_IMAGE_HEIGHT, imageHeight[0]);
            }
        });

        // Image alpha
        binding.imageAlphaOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(HEADER_IMAGE_ALPHA, 100) + "%");
        binding.imageAlphaSeekbar.setProgress(RPrefs.getInt(HEADER_IMAGE_ALPHA, 100));
        final int[] imageAlpha = {RPrefs.getInt(HEADER_IMAGE_ALPHA, 100)};
        binding.imageAlphaSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                imageAlpha[0] = progress;
                binding.imageAlphaOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "%");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(HEADER_IMAGE_ALPHA, imageAlpha[0]);
            }
        });

        // Header image zoom to fit
        binding.enableZoomToFit.setChecked(RPrefs.getBoolean(HEADER_IMAGE_ZOOMTOFIT, false));
        binding.enableZoomToFit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_IMAGE_ZOOMTOFIT, isChecked);
        });

        // Header image hide in landscape
        binding.enableHideImageLandscape.setChecked(RPrefs.getBoolean(HEADER_IMAGE_LANDSCAPE_SWITCH, true));
        binding.enableHideImageLandscape.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_IMAGE_LANDSCAPE_SWITCH, isChecked);
        });
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
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    public void browseHeaderImage() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("image/*");
        startActivityIntent.launch(chooseFile);
    }
}