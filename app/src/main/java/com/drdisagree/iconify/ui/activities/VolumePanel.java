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
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityVolumePanelBinding;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.ui.views.InfoDialog;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.ui.views.RadioDialog;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.compiler.VolumeCompiler;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class VolumePanel extends BaseActivity implements RadioDialog.RadioDialogListener {

    private ActivityVolumePanelBinding binding;
    private LoadingDialog loadingDialog;
    private InfoDialog infoDialog;
    private RadioDialog rd_volume_style;
    private int selectedStyle = 0;

    @SuppressLint("SetTextI18n")
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

        // Info dialog for volume style modules
        infoDialog = new InfoDialog(this);

        // Volume style
        binding.volumeStyle.volumeStyleInfo.setOnClickListener(v -> infoDialog.show(R.string.read_carefully, R.string.volume_module_installation_guide));

        rd_volume_style = new RadioDialog(this, 0, selectedStyle);
        rd_volume_style.setRadioDialogListener(this);
        binding.volumeStyle.volumeStyleModule.setOnClickListener(v -> rd_volume_style.show(R.string.volume_style, R.array.volume_style, binding.volumeStyle.selectedVolumeStyle));
        binding.volumeStyle.selectedVolumeStyle.setText(getResources().getString(R.string.opt_selected) + " " + Arrays.asList(getResources().getStringArray(R.array.volume_style)).get(rd_volume_style.getSelectedIndex()));

        binding.volumeStyle.volumeStyleCreateModule.setOnClickListener(v -> {
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(this);
            } else {
                if (selectedStyle == 0) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_select_style), Toast.LENGTH_SHORT).show();
                } else {
                    installVolumeModule(selectedStyle);
                }
            }
        });
    }

    @SuppressLint({"NonConstantResourceId"})
    private void installVolumeModule(int volumeStyle) {
        loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

        AtomicBoolean hasErroredOut = new AtomicBoolean(false);
        String selectedStyle = null;

        switch (volumeStyle) {
            case 1:
                selectedStyle = "VolumeGradient";
                break;
            case 2:
                selectedStyle = "VolumeDoubleLayer";
                break;
            case 3:
                selectedStyle = "VolumeShadedLayer";
                break;
            case 4:
                selectedStyle = "VolumeNeumorph";
                break;
            case 5:
                selectedStyle = "VolumeOutline";
                break;
            case 6:
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

    private void updateVolumePreview(int idx) {
        if (idx == 0)
            setVolumeDrawable(R.drawable.media_player_bg_accent, R.drawable.media_player_bg_accent, false, false);
        else if (idx == 1)
            setVolumeDrawable(R.drawable.volume_gradient, R.drawable.volume_gradient, false, false);
        else if (idx == 2)
            setVolumeDrawable(R.drawable.volume_double_layer, R.drawable.volume_double_layer, false, false);
        else if (idx == 3)
            setVolumeDrawable(R.drawable.volume_shaded_layer, R.drawable.volume_shaded_layer, false, false);
        else if (idx == 4)
            setVolumeDrawable(R.drawable.volume_neumorph, R.drawable.volume_neumorph, false, false);
        else if (idx == 5)
            setVolumeDrawable(R.drawable.volume_outline_ringer, R.drawable.volume_outline, true, false);
        else if (idx == 6)
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onItemSelected(int dialogId, int selectedIndex) {
        selectedStyle = selectedIndex;
        updateVolumePreview(selectedIndex);
        binding.volumeStyle.selectedVolumeStyle.setText(getResources().getString(R.string.opt_selected) + " " + Arrays.asList(getResources().getStringArray(R.array.volume_style)).get(selectedStyle));
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        if (rd_volume_style != null)
            rd_volume_style.dismiss();
        super.onDestroy();
    }
}