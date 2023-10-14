package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.VOLUME_PANEL_BACKGROUND_WIDTH;
import static com.drdisagree.iconify.common.References.FABRICATED_ROUNDED_SLIDER_TRACK_INSET;
import static com.drdisagree.iconify.common.References.FABRICATED_VOLUME_DIALOG_SLIDER_WIDTH;
import static com.drdisagree.iconify.common.References.FABRICATED_VOLUME_DIALOG_TRACK_WIDTH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityVolumePanelBinding;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.ui.views.InfoDialog;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.overlay.FabricatedUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.compiler.VolumeCompiler;

import java.util.concurrent.atomic.AtomicBoolean;

public class VolumePanel extends BaseActivity {

    private ActivityVolumePanelBinding binding;
    private LoadingDialog loadingDialog;
    private InfoDialog infoDialog;
    private int realCheckedId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVolumePanelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewHelper.setHeader(this, binding.header.toolbar, R.string.activity_title_volume_panel);

        binding.thinBg.setChecked(Prefs.getInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0) == 1);
        binding.thinBg.addOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                binding.toggleButtonGroup.uncheck(binding.thickBg.getId());
                binding.toggleButtonGroup.uncheck(binding.noBg.getId());

                Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 1);
                FabricatedUtil.buildAndEnableOverlays(
                        new Object[]{SYSTEMUI_PACKAGE, FABRICATED_VOLUME_DIALOG_SLIDER_WIDTH, "dimen", "volume_dialog_slider_width", "42dp"},
                        new Object[]{SYSTEMUI_PACKAGE, FABRICATED_VOLUME_DIALOG_TRACK_WIDTH, "dimen", "volume_dialog_track_width", "4dp"},
                        new Object[]{SYSTEMUI_PACKAGE, FABRICATED_ROUNDED_SLIDER_TRACK_INSET, "dimen", "rounded_slider_track_inset", "22dp"}
                );
                FabricatedUtil.disableOverlay(FABRICATED_ROUNDED_SLIDER_TRACK_INSET);
            } else {
                Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0);
                FabricatedUtil.disableOverlays(FABRICATED_VOLUME_DIALOG_SLIDER_WIDTH, FABRICATED_VOLUME_DIALOG_TRACK_WIDTH, FABRICATED_ROUNDED_SLIDER_TRACK_INSET);
            }
        });

        binding.thickBg.setChecked(Prefs.getInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0) == 2);
        binding.thickBg.addOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                binding.toggleButtonGroup.uncheck(binding.thinBg.getId());
                binding.toggleButtonGroup.uncheck(binding.noBg.getId());

                Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 2);
                FabricatedUtil.buildAndEnableOverlays(
                        new Object[]{SYSTEMUI_PACKAGE, FABRICATED_VOLUME_DIALOG_SLIDER_WIDTH, "dimen", "volume_dialog_slider_width", "42dp"},
                        new Object[]{SYSTEMUI_PACKAGE, FABRICATED_VOLUME_DIALOG_TRACK_WIDTH, "dimen", "volume_dialog_track_width", "42dp"},
                        new Object[]{SYSTEMUI_PACKAGE, FABRICATED_ROUNDED_SLIDER_TRACK_INSET, "dimen", "rounded_slider_track_inset", "0dp"}
                );
            } else {
                Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0);
                FabricatedUtil.disableOverlays(FABRICATED_VOLUME_DIALOG_SLIDER_WIDTH, FABRICATED_VOLUME_DIALOG_TRACK_WIDTH, FABRICATED_ROUNDED_SLIDER_TRACK_INSET);
            }
        });

        binding.noBg.setChecked(Prefs.getInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0) == 3);
        binding.noBg.addOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                binding.toggleButtonGroup.uncheck(binding.thinBg.getId());
                binding.toggleButtonGroup.uncheck(binding.thickBg.getId());

                Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 3);
                FabricatedUtil.buildAndEnableOverlays(
                        new Object[]{SYSTEMUI_PACKAGE, FABRICATED_VOLUME_DIALOG_SLIDER_WIDTH, "dimen", "volume_dialog_slider_width", "42dp"},
                        new Object[]{SYSTEMUI_PACKAGE, FABRICATED_VOLUME_DIALOG_TRACK_WIDTH, "dimen", "volume_dialog_track_width", "0dp"},
                        new Object[]{SYSTEMUI_PACKAGE, FABRICATED_ROUNDED_SLIDER_TRACK_INSET, "dimen", "rounded_slider_track_inset", "24dp"}
                );
                FabricatedUtil.disableOverlay(FABRICATED_ROUNDED_SLIDER_TRACK_INSET);
            } else {
                Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0);
                FabricatedUtil.disableOverlays(FABRICATED_VOLUME_DIALOG_SLIDER_WIDTH, FABRICATED_VOLUME_DIALOG_TRACK_WIDTH, FABRICATED_ROUNDED_SLIDER_TRACK_INSET);
            }
        });

        // Loading dialog while creating modules
        loadingDialog = new LoadingDialog(this);

        // Credits dialog for volume style modules
        infoDialog = new InfoDialog(this);

        // Volume style
        binding.volumeStyle.volumeStyleInfo.setOnClickListener(v -> infoDialog.show(R.string.read_carefully, R.string.volume_module_installation_guide));

        binding.volumeStyle.volumeStyle1.clearCheck();
        binding.volumeStyle.volumeStyle2.clearCheck();
        binding.volumeStyle.volumeStyle1.setOnCheckedChangeListener(listener1);
        binding.volumeStyle.volumeStyle2.setOnCheckedChangeListener(listener2);
        int checkedId1 = binding.volumeStyle.volumeStyle1.getCheckedRadioButtonId();
        int checkedId2 = binding.volumeStyle.volumeStyle2.getCheckedRadioButtonId();
        realCheckedId = checkedId1 == -1 ? checkedId2 : checkedId1;

        binding.volumeStyle.volumeStyleCreateModule.setOnClickListener(v -> {
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(this);
            } else {
                if (realCheckedId == -1) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_select_style), Toast.LENGTH_SHORT).show();
                } else {
                    installVolumeModule(realCheckedId);
                }
            }
        });
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
                hasErroredOut.set(VolumeCompiler.buildModule(finalSelectedStyle, SYSTEMUI_PACKAGE));
            } catch (Exception e) {
                hasErroredOut.set(true);
                Log.e("VolumePanel", e.toString());
            }
            runOnUiThread(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
                loadingDialog.hide();

                if (hasErroredOut.get()) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_module_created), Toast.LENGTH_SHORT).show();
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
        binding.volumeThinBg.volumeRingerBg.setBackground(ContextCompat.getDrawable(getApplicationContext(), ringerDrawable));
        binding.volumeThinBg.volumeProgressDrawable.setBackground(ContextCompat.getDrawable(getApplicationContext(), progressDrawable));
        binding.volumeThickBg.volumeRingerBg.setBackground(ContextCompat.getDrawable(getApplicationContext(), ringerDrawable));
        binding.volumeThickBg.volumeProgressDrawable.setBackground(ContextCompat.getDrawable(getApplicationContext(), progressDrawable));
        binding.volumeNoBg.volumeRingerBg.setBackground(ContextCompat.getDrawable(getApplicationContext(), ringerDrawable));
        binding.volumeNoBg.volumeProgressDrawable.setBackground(ContextCompat.getDrawable(getApplicationContext(), progressDrawable));

        if (ringerInverse) {
            binding.volumeThinBg.volumeRingerIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.textColorPrimary));
            binding.volumeThickBg.volumeRingerIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.textColorPrimary));
            binding.volumeNoBg.volumeRingerIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.textColorPrimary));
        } else {
            binding.volumeThinBg.volumeRingerIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.textColorPrimaryInverse));
            binding.volumeThickBg.volumeRingerIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.textColorPrimaryInverse));
            binding.volumeNoBg.volumeRingerIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.textColorPrimaryInverse));
        }

        if (progressInverse) {
            binding.volumeThinBg.volumeProgressIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.textColorPrimary));
            binding.volumeThickBg.volumeProgressIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.textColorPrimary));
            binding.volumeNoBg.volumeProgressIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.textColorPrimary));
        } else {
            binding.volumeThinBg.volumeProgressIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.textColorPrimaryInverse));
            binding.volumeThickBg.volumeProgressIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.textColorPrimaryInverse));
            binding.volumeNoBg.volumeProgressIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.textColorPrimaryInverse));
        }
    }

    private final RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                binding.volumeStyle.volumeStyle2.setOnCheckedChangeListener(null);
                binding.volumeStyle.volumeStyle2.clearCheck();
                binding.volumeStyle.volumeStyle2.setOnCheckedChangeListener(listener2);
                realCheckedId = checkedId;
            }
            updateVolumePreview(checkedId);
        }
    };

    private final RadioGroup.OnCheckedChangeListener listener2 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                binding.volumeStyle.volumeStyle1.setOnCheckedChangeListener(null);
                binding.volumeStyle.volumeStyle1.clearCheck();
                binding.volumeStyle.volumeStyle1.setOnCheckedChangeListener(listener1);
                realCheckedId = checkedId;
            }
            updateVolumePreview(checkedId);
        }
    };

}