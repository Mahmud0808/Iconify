package com.drdisagree.iconify.ui.activity;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;
import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.fragment.LoadingDialog;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.VolumeCompilerUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class VolumePanel extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volume_panel);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_volume_panel));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        LoadingDialog loadingDialog = new LoadingDialog(this);

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch thin_bg = findViewById(R.id.thin_bg);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch thick_bg = findViewById(R.id.thick_bg);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch no_bg = findViewById(R.id.no_bg);

        thin_bg.setChecked(Prefs.getBoolean("IconifyComponentVPBG1.overlay"));
        thin_bg.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                thick_bg.setChecked(false);
                no_bg.setChecked(false);
                OverlayUtil.disableOverlay("IconifyComponentVPBG2.overlay");
                OverlayUtil.disableOverlay("IconifyComponentVPBG3.overlay");
                OverlayUtil.enableOverlay("IconifyComponentVPBG1.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentVPBG1.overlay");
            }
        });

        thick_bg.setChecked(Prefs.getBoolean("IconifyComponentVPBG2.overlay"));
        thick_bg.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                thin_bg.setChecked(false);
                no_bg.setChecked(false);
                OverlayUtil.disableOverlay("IconifyComponentVPBG1.overlay");
                OverlayUtil.disableOverlay("IconifyComponentVPBG3.overlay");
                OverlayUtil.enableOverlay("IconifyComponentVPBG2.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentVPBG2.overlay");
            }
        });

        no_bg.setChecked(Prefs.getBoolean("IconifyComponentVPBG3.overlay"));
        no_bg.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                thin_bg.setChecked(false);
                thick_bg.setChecked(false);
                OverlayUtil.disableOverlay("IconifyComponentVPBG1.overlay");
                OverlayUtil.disableOverlay("IconifyComponentVPBG2.overlay");
                OverlayUtil.enableOverlay("IconifyComponentVPBG3.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentVPBG3.overlay");
            }
        });

        Button gradient_volume = findViewById(R.id.gradient_volume);
        gradient_volume.setOnClickListener(v -> {
            AtomicBoolean hasErroredOut = new AtomicBoolean(false);

            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent();
                intent.setAction(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", Iconify.getAppContext().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } else {
                loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

                Runnable runnable = () -> {
                    try {
                        hasErroredOut.set(VolumeCompilerUtil.buildOverlay("VolumeGradient", SYSTEM_UI_PACKAGE));
                    } catch (IOException e) {
                        hasErroredOut.set(true);
                        e.printStackTrace();
                    }
                    runOnUiThread(() -> new Handler().postDelayed(() -> {
                        loadingDialog.hide();

                        if (hasErroredOut.get()) {
                            Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_module_generated), Toast.LENGTH_SHORT).show();
                        }
                    }, 2000));
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}