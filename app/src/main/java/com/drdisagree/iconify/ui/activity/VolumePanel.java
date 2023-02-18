package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.SYSTEMUI_PACKAGE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.view.InfoDialog;
import com.drdisagree.iconify.ui.view.LoadingDialog;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.compilerutil.VolumeCompilerUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class VolumePanel extends AppCompatActivity {

    LoadingDialog loadingDialog;
    InfoDialog infoDialog;
    RadioGroup rg1, rg2;
    private int checkedId1 = -1, checkedId2 = -1, realCheckedId = -1;

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

        // Loading dialog while creating modules
        loadingDialog = new LoadingDialog(this);

        // Info dialog for volume style modules
        infoDialog = new InfoDialog(this);

        // Volume style
        LinearLayout volume_style = findViewById(R.id.volume_style);

        ImageView info_img = volume_style.findViewById(R.id.volume_style_info);
        info_img.setOnClickListener(v -> infoDialog.show(R.string.read_carefully, R.string.volume_module_installation_guide));

        rg1 = volume_style.findViewById(R.id.volume_style1);
        rg2 = volume_style.findViewById(R.id.volume_style2);
        rg1.clearCheck();
        rg2.clearCheck();
        rg1.setOnCheckedChangeListener(listener1);
        rg2.setOnCheckedChangeListener(listener2);
        checkedId1 = rg1.getCheckedRadioButtonId();
        checkedId2 = rg2.getCheckedRadioButtonId();
        realCheckedId = checkedId1 == -1 ? checkedId2 : checkedId1;

        Button create_module = volume_style.findViewById(R.id.volume_style_create_module);
        create_module.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                SystemUtil.getStoragePermission(this);
            } else {
                if (realCheckedId == -1) {
                    Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_select_style), Toast.LENGTH_SHORT).show();
                } else {
                    installVolumeModule(realCheckedId);
                }
            }
        });
    }

    public void enableColors(View view) {
        OverlayUtil.enableOverlay("IconifyComponentIXCC.overlay");
    }

    @SuppressLint({"NonConstantResourceId"})
    private void installVolumeModule(int volume) {
        loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

        AtomicBoolean hasErroredOut = new AtomicBoolean(false);
        String selectedStyle = null;

        switch (volume) {
            case R.id.gradient_style:
                selectedStyle = "VolumeGradient";
                break;
            case R.id.doublelayer_style:
                selectedStyle = "VolumeDoubleLayer";
                break;
            case R.id.shadedlayer_style:
                selectedStyle = "VolumeShadedLayer";
                break;
            case R.id.neumorph_style:
                selectedStyle = "VolumeNeumorph";
                break;
            case R.id.outline_style:
                selectedStyle = "VolumeOutline";
                break;
            case R.id.neumorphoutline_style:
                selectedStyle = "VolumeNeumorphOutline";
                break;
        }

        String finalSelectedStyle = selectedStyle;
        Runnable runnable = () -> {
            try {
                hasErroredOut.set(VolumeCompilerUtil.buildModule(finalSelectedStyle, SYSTEMUI_PACKAGE));
            } catch (IOException e) {
                hasErroredOut.set(true);
                e.printStackTrace();
            }
            runOnUiThread(() -> new Handler().postDelayed(() -> {
                loadingDialog.hide();

                if (hasErroredOut.get()) {
                    Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_module_created), Toast.LENGTH_SHORT).show();
                }
            }, 2000));
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void updateVolumePreview(int id) {
        if (id == R.id.gradient_style)
            setVolumeDrawable(R.drawable.volume_gradient, R.drawable.volume_gradient, false, false);
        else if (id == R.id.doublelayer_style)
            setVolumeDrawable(R.drawable.volume_double_layer, R.drawable.volume_double_layer, false, false);
        else if (id == R.id.shadedlayer_style)
            setVolumeDrawable(R.drawable.volume_shaded_layer, R.drawable.volume_shaded_layer, false, false);
        else if (id == R.id.neumorph_style)
            setVolumeDrawable(R.drawable.volume_neumorph, R.drawable.volume_neumorph, false, false);
        else if (id == R.id.outline_style)
            setVolumeDrawable(R.drawable.volume_outline_ringer, R.drawable.volume_outline, true, false);
        else if (id == R.id.neumorphoutline_style)
            setVolumeDrawable(R.drawable.volume_neumorph_outline_ringer, R.drawable.volume_neumorph_outline, true, false);
    }

    private void setVolumeDrawable(int ringerDrawable, int progressDrawable, boolean ringerInverse, boolean progressInverse) {
        findViewById(R.id.volume_thin_bg).findViewById(R.id.volume_ringer_bg).setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), ringerDrawable));
        findViewById(R.id.volume_thin_bg).findViewById(R.id.volume_progress_drawable).setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), progressDrawable));
        findViewById(R.id.volume_thick_bg).findViewById(R.id.volume_ringer_bg).setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), ringerDrawable));
        findViewById(R.id.volume_thick_bg).findViewById(R.id.volume_progress_drawable).setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), progressDrawable));
        findViewById(R.id.volume_no_bg).findViewById(R.id.volume_ringer_bg).setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), ringerDrawable));
        findViewById(R.id.volume_no_bg).findViewById(R.id.volume_progress_drawable).setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), progressDrawable));

        if (ringerInverse) {
            findViewById(R.id.volume_thin_bg).findViewById(R.id.volume_ringer_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
            findViewById(R.id.volume_thick_bg).findViewById(R.id.volume_ringer_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
            findViewById(R.id.volume_no_bg).findViewById(R.id.volume_ringer_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
        } else {
            findViewById(R.id.volume_thin_bg).findViewById(R.id.volume_ringer_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
            findViewById(R.id.volume_thick_bg).findViewById(R.id.volume_ringer_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
            findViewById(R.id.volume_no_bg).findViewById(R.id.volume_ringer_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
        }

        if (progressInverse) {
            findViewById(R.id.volume_thin_bg).findViewById(R.id.volume_progress_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
            findViewById(R.id.volume_thick_bg).findViewById(R.id.volume_progress_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
            findViewById(R.id.volume_no_bg).findViewById(R.id.volume_progress_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
        } else {
            findViewById(R.id.volume_thin_bg).findViewById(R.id.volume_progress_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
            findViewById(R.id.volume_thick_bg).findViewById(R.id.volume_progress_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
            findViewById(R.id.volume_no_bg).findViewById(R.id.volume_progress_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
        }
    }

    private final RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                rg2.setOnCheckedChangeListener(null);
                rg2.clearCheck();
                rg2.setOnCheckedChangeListener(listener2);
                realCheckedId = checkedId;
            }
            updateVolumePreview(checkedId);
        }
    };

    private final RadioGroup.OnCheckedChangeListener listener2 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                rg1.setOnCheckedChangeListener(null);
                rg1.clearCheck();
                rg1.setOnCheckedChangeListener(listener1);
                realCheckedId = checkedId;
            }
            updateVolumePreview(checkedId);
        }
    };

}