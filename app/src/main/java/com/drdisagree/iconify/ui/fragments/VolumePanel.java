package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.VOLUME_PANEL_BACKGROUND_WIDTH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentVolumePanelBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.dialogs.InfoDialog;
import com.drdisagree.iconify.ui.dialogs.LoadingDialog;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.compiler.VolumeCompiler;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceEntry;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager;

import java.util.concurrent.atomic.AtomicBoolean;

public class VolumePanel extends BaseFragment {

    private FragmentVolumePanelBinding binding;
    private LoadingDialog loadingDialog;
    private InfoDialog infoDialog;
    private int realCheckedId = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentVolumePanelBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_volume_panel);

        binding.thinBg.setChecked(Prefs.getInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0) == 1);
        binding.thinBg.addOnCheckedChangeListener((button, isChecked) -> {
            if (button.isPressed()) {
                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.toggleButtonGroup.uncheck(binding.thinBg.getId());
                } else {
                    if (isChecked) {
                        binding.toggleButtonGroup.uncheck(binding.thickBg.getId());
                        binding.toggleButtonGroup.uncheck(binding.noBg.getId());

                        Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 1);
                        ResourceManager.buildOverlayWithResource(
                                requireContext(),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_slider_width", "42dp"),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_track_width", "4dp"),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "rounded_slider_track_inset", "22dp")
                        );
                    } else {
                        Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0);
                        ResourceManager.removeResourceFromOverlay(
                                requireContext(),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_slider_width"),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_track_width"),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "rounded_slider_track_inset")
                        );
                    }
                }
            }
        });

        binding.thickBg.setChecked(Prefs.getInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0) == 2);
        binding.thickBg.addOnCheckedChangeListener((button, isChecked) -> {
            if (button.isPressed()) {
                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.toggleButtonGroup.uncheck(binding.thickBg.getId());
                } else {
                    if (isChecked) {
                        binding.toggleButtonGroup.uncheck(binding.thinBg.getId());
                        binding.toggleButtonGroup.uncheck(binding.noBg.getId());

                        Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 2);
                        ResourceManager.buildOverlayWithResource(
                                requireContext(),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_slider_width", "42dp"),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_track_width", "42dp"),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "rounded_slider_track_inset", "0dp")
                        );
                    } else {
                        Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0);
                        ResourceManager.removeResourceFromOverlay(
                                requireContext(),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_slider_width"),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_track_width"),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "rounded_slider_track_inset")
                        );
                    }
                }
            }
        });

        binding.noBg.setChecked(Prefs.getInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0) == 3);
        binding.noBg.addOnCheckedChangeListener((button, isChecked) -> {
            if (button.isPressed()) {
                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.toggleButtonGroup.uncheck(binding.noBg.getId());
                } else {
                    if (isChecked) {
                        binding.toggleButtonGroup.uncheck(binding.thinBg.getId());
                        binding.toggleButtonGroup.uncheck(binding.thickBg.getId());

                        Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 3);
                        ResourceManager.buildOverlayWithResource(
                                requireContext(),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_slider_width", "42dp"),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_track_width", "0dp"),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "rounded_slider_track_inset", "24dp")
                        );
                    } else {
                        Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0);
                        ResourceManager.removeResourceFromOverlay(
                                requireContext(),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_slider_width"),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_track_width"),
                                new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "rounded_slider_track_inset")
                        );
                    }
                }
            }
        });

        // Loading dialog while creating modules
        loadingDialog = new LoadingDialog(requireContext());

        // Credits dialog for volume style modules
        infoDialog = new InfoDialog(requireContext());

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
            if ((RootUtil.isKSUInstalled() || RootUtil.isApatchInstalled()) && !RootUtil.isMagiskInstalled()) {
                Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_only_magisk_supported), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                if (realCheckedId == -1) {
                    Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_select_style), Toast.LENGTH_SHORT).show();
                } else {
                    installVolumeModule(realCheckedId);
                }
            }
        });

        return view;
    }

    @SuppressLint({"NonConstantResourceId"})
    private void installVolumeModule(int volume) {
        loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

        AtomicBoolean hasErroredOut = new AtomicBoolean(false);
        String selectedStyle = switch (volume) {
            case R.id.gradient_style -> "VolumeGradient";
            case R.id.doublelayer_style -> "VolumeDoubleLayer";
            case R.id.shadedlayer_style -> "VolumeShadedLayer";
            case R.id.neumorph_style -> "VolumeNeumorph";
            case R.id.outline_style -> "VolumeOutline";
            case R.id.neumorphoutline_style -> "VolumeNeumorphOutline";
            default -> null;
        };

        new Thread(() -> {
            try {
                hasErroredOut.set(VolumeCompiler.buildModule(selectedStyle, SYSTEMUI_PACKAGE));
            } catch (Exception e) {
                hasErroredOut.set(true);
                Log.e("VolumePanel", e.toString());
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                loadingDialog.hide();

                if (hasErroredOut.get()) {
                    Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_module_created), Toast.LENGTH_SHORT).show();
                }
            }, 2000);
        }).start();
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

    @SuppressWarnings("SameParameterValue")
    private void setVolumeDrawable(int ringerDrawable, int progressDrawable, boolean ringerInverse, boolean progressInverse) {
        binding.volumeThinBg.volumeRingerBg.setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), ringerDrawable));
        binding.volumeThinBg.volumeProgressDrawable.setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), progressDrawable));
        binding.volumeThickBg.volumeRingerBg.setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), ringerDrawable));
        binding.volumeThickBg.volumeProgressDrawable.setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), progressDrawable));
        binding.volumeNoBg.volumeRingerBg.setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), ringerDrawable));
        binding.volumeNoBg.volumeProgressDrawable.setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), progressDrawable));

        if (ringerInverse) {
            binding.volumeThinBg.volumeRingerIcon.setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
            binding.volumeThickBg.volumeRingerIcon.setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
            binding.volumeNoBg.volumeRingerIcon.setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
        } else {
            binding.volumeThinBg.volumeRingerIcon.setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
            binding.volumeThickBg.volumeRingerIcon.setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
            binding.volumeNoBg.volumeRingerIcon.setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
        }

        if (progressInverse) {
            binding.volumeThinBg.volumeProgressIcon.setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
            binding.volumeThickBg.volumeProgressIcon.setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
            binding.volumeNoBg.volumeProgressIcon.setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
        } else {
            binding.volumeThinBg.volumeProgressIcon.setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
            binding.volumeThickBg.volumeProgressIcon.setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
            binding.volumeNoBg.volumeProgressIcon.setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
        }
    }

    @Override
    public void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
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