package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_ALPHA;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_ZOOMTOFIT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.utils.HelperUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

public class XposedHeaderImage extends AppCompatActivity {

    private static final int PICKFILE_RESULT_CODE = 100;
    private Button enable_header_image;

    private static String getRealPathFromURI(Uri uri) {
        File file = null;
        try {
            @SuppressLint("Recycle") Cursor returnCursor = Iconify.getAppContext().getContentResolver().query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String name = returnCursor.getString(nameIndex);
            file = new File(Iconify.getAppContext().getFilesDir(), name);
            @SuppressLint("Recycle") InputStream inputStream = Iconify.getAppContext().getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1024 * 1024;
            int bytesAvailable = inputStream.available();
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file.getPath();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_header_image);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_header_image));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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
            new Handler().postDelayed(HelperUtil::forceApply, 200);
            enable_header_image.setVisibility(View.GONE);
            disable_header_image.setVisibility(View.VISIBLE);
        });

        disable_header_image.setOnClickListener(v -> {
            RPrefs.putBoolean(HEADER_IMAGE_SWITCH, false);
            new Handler().postDelayed(HelperUtil::forceApply, 200);
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
                if (RPrefs.getBoolean(HEADER_IMAGE_SWITCH, false)) {
                    new Handler().postDelayed(HelperUtil::forceApply, 200);
                }
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
                if (RPrefs.getBoolean(HEADER_IMAGE_SWITCH, false)) {
                    new Handler().postDelayed(HelperUtil::forceApply, 200);
                }
            }
        });

        // Header image zoom to fit
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_zoom_to_fit = findViewById(R.id.enable_zoom_to_fit);
        enable_zoom_to_fit.setChecked(RPrefs.getBoolean(HEADER_IMAGE_ZOOMTOFIT, false));
        enable_zoom_to_fit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HEADER_IMAGE_ZOOMTOFIT, isChecked);
            if (RPrefs.getBoolean(HEADER_IMAGE_SWITCH, false)) {
                new Handler().postDelayed(HelperUtil::forceApply, 200);
            }
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

        if (data == null) return;

        if (requestCode == PICKFILE_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String source = getRealPathFromURI(uri);
            if (source == null) {
                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                return;
            }

            String destination = Resources.XPOSED_RESOURCE_TEMP_DIR + "/header_image.png";

            Shell.cmd("mkdir -p " + Resources.XPOSED_RESOURCE_TEMP_DIR).exec();

            if (Shell.cmd("cp " + source + ' ' + destination).exec().isSuccess())
                enable_header_image.setVisibility(View.VISIBLE);
            else
                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}