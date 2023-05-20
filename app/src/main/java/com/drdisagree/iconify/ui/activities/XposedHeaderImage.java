package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_ALPHA;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_LANDSCAPE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_ZOOMTOFIT;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.SystemUtil;

public class XposedHeaderImage extends BaseActivity {

    private static final int PICKFILE_RESULT_CODE = 100;
    private Button enable_header_image;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_header_image);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_header_image);

        // Header image picker
        Button pick_header_image = findViewById(R.id.pick_header_image);
        pick_header_image.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                SystemUtil.getStoragePermission(this);
            } else {
                browseHeaderImage();
            }
        });

        Button disable_header_image = findViewById(R.id.disable_header_image);
        disable_header_image.setVisibility(RPrefs.getBoolean(HEADER_IMAGE_SWITCH, false) ? View.VISIBLE : View.GONE);

        enable_header_image = findViewById(R.id.enable_header_image);
        enable_header_image.setOnClickListener(v -> {
            RPrefs.putBoolean(HEADER_IMAGE_SWITCH, true);
            enable_header_image.setVisibility(View.GONE);
            disable_header_image.setVisibility(View.VISIBLE);
        });

        disable_header_image.setOnClickListener(v -> {
            RPrefs.putBoolean(HEADER_IMAGE_SWITCH, false);
            disable_header_image.setVisibility(View.GONE);
        });

        // Image height
        SeekBar header_image_height = findViewById(R.id.header_image_height_seekbar);
        TextView header_image_height_output = findViewById(R.id.header_image_height_output);
        header_image_height_output.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(HEADER_IMAGE_HEIGHT, 140) + "dp");
        header_image_height.setProgress(RPrefs.getInt(HEADER_IMAGE_HEIGHT, 140));
        final int[] imageHeight = {RPrefs.getInt(HEADER_IMAGE_HEIGHT, 140)};
        header_image_height.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                imageHeight[0] = progress;
                header_image_height_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(HEADER_IMAGE_HEIGHT, imageHeight[0]);
            }
        });

        // Image alpha
        SeekBar image_alpha_seekbar = findViewById(R.id.image_alpha_seekbar);
        TextView image_alpha_output = findViewById(R.id.image_alpha_output);
        image_alpha_output.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(HEADER_IMAGE_ALPHA, 100) + "%");
        image_alpha_seekbar.setProgress(RPrefs.getInt(HEADER_IMAGE_ALPHA, 100));
        final int[] imageAlpha = {RPrefs.getInt(HEADER_IMAGE_ALPHA, 100)};
        image_alpha_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                imageAlpha[0] = progress;
                image_alpha_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "%");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(HEADER_IMAGE_ALPHA, imageAlpha[0]);
            }
        });

        // Header image zoom to fit
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_zoom_to_fit = findViewById(R.id.enable_zoom_to_fit);
        enable_zoom_to_fit.setChecked(RPrefs.getBoolean(HEADER_IMAGE_ZOOMTOFIT, false));
        enable_zoom_to_fit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_IMAGE_ZOOMTOFIT, isChecked);
        });

        // Header image hide in landscape
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_hide_image_landscape = findViewById(R.id.enable_hide_image_landscape);
        enable_hide_image_landscape.setChecked(RPrefs.getBoolean(HEADER_IMAGE_LANDSCAPE_SWITCH, true));
        enable_hide_image_landscape.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_IMAGE_LANDSCAPE_SWITCH, isChecked);
        });
    }

    public void browseHeaderImage() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("image/*");
        startActivityForResult(Intent.createChooser(chooseFile, getResources().getString(R.string.choose_header_image)), PICKFILE_RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FileUtil.copyToIconifyHiddenDir(this, requestCode, resultCode, data, PICKFILE_RESULT_CODE, "header_image.png", enable_header_image);
    }
}