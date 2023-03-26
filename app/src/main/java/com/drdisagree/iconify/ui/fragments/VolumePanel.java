package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.VOLUME_PANEL_BACKGROUND_WIDTH;
import static com.drdisagree.iconify.common.References.FABRICATED_ROUNDED_SLIDER_TRACK_INSET;
import static com.drdisagree.iconify.common.References.FABRICATED_VOLUME_DIALOG_SLIDER_WIDTH;
import static com.drdisagree.iconify.common.References.FABRICATED_VOLUME_DIALOG_TRACK_WIDTH;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.utils.FragmentHelper;
import com.drdisagree.iconify.ui.views.InfoDialog;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.compiler.VolumeCompiler;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class VolumePanel extends Fragment {

    private View view;
    LoadingDialog loadingDialog;
    InfoDialog infoDialog;
    RadioGroup rg1, rg2;
    private int checkedId1 = -1, checkedId2 = -1, realCheckedId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_volume_panel, container, false);

        // Header
        FragmentHelper.initHeader((AppCompatActivity) requireActivity(), view, R.string.activity_title_volume_panel, getParentFragmentManager());

        @SuppressLint("UseSwitchCompatOrMaterialCode") android.widget.Switch thin_bg = view.findViewById(R.id.thin_bg);
        @SuppressLint("UseSwitchCompatOrMaterialCode") android.widget.Switch thick_bg = view.findViewById(R.id.thick_bg);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch no_bg = view.findViewById(R.id.no_bg);

        thin_bg.setChecked(Prefs.getInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0) == 1);
        thin_bg.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                thick_bg.setChecked(false);
                no_bg.setChecked(false);
                Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 1);
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_VOLUME_DIALOG_SLIDER_WIDTH, "dimen", "volume_dialog_slider_width", "42dp");
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_VOLUME_DIALOG_TRACK_WIDTH, "dimen", "volume_dialog_track_width", "4dp");
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_ROUNDED_SLIDER_TRACK_INSET, "dimen", "rounded_slider_track_inset", "22dp");
                FabricatedUtil.disableOverlay(FABRICATED_ROUNDED_SLIDER_TRACK_INSET);
            } else {
                Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0);
                FabricatedUtil.disableOverlay(FABRICATED_VOLUME_DIALOG_SLIDER_WIDTH);
                FabricatedUtil.disableOverlay(FABRICATED_VOLUME_DIALOG_TRACK_WIDTH);
                FabricatedUtil.disableOverlay(FABRICATED_ROUNDED_SLIDER_TRACK_INSET);
            }
        });

        thick_bg.setChecked(Prefs.getInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0) == 2);
        thick_bg.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                thin_bg.setChecked(false);
                no_bg.setChecked(false);
                Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 2);
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_VOLUME_DIALOG_SLIDER_WIDTH, "dimen", "volume_dialog_slider_width", "42dp");
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_VOLUME_DIALOG_TRACK_WIDTH, "dimen", "volume_dialog_track_width", "42dp");
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_ROUNDED_SLIDER_TRACK_INSET, "dimen", "rounded_slider_track_inset", "0dp");
            } else {
                Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0);
                FabricatedUtil.disableOverlay(FABRICATED_VOLUME_DIALOG_SLIDER_WIDTH);
                FabricatedUtil.disableOverlay(FABRICATED_VOLUME_DIALOG_TRACK_WIDTH);
                FabricatedUtil.disableOverlay(FABRICATED_ROUNDED_SLIDER_TRACK_INSET);
            }
        });

        no_bg.setChecked(Prefs.getInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0) == 3);
        no_bg.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                thin_bg.setChecked(false);
                thick_bg.setChecked(false);
                Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 3);
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_VOLUME_DIALOG_SLIDER_WIDTH, "dimen", "volume_dialog_slider_width", "42dp");
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_VOLUME_DIALOG_TRACK_WIDTH, "dimen", "volume_dialog_track_width", "0dp");
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_ROUNDED_SLIDER_TRACK_INSET, "dimen", "rounded_slider_track_inset", "24dp");
                FabricatedUtil.disableOverlay(FABRICATED_ROUNDED_SLIDER_TRACK_INSET);
            } else {
                Prefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0);
                FabricatedUtil.disableOverlay(FABRICATED_VOLUME_DIALOG_SLIDER_WIDTH);
                FabricatedUtil.disableOverlay(FABRICATED_VOLUME_DIALOG_TRACK_WIDTH);
                FabricatedUtil.disableOverlay(FABRICATED_ROUNDED_SLIDER_TRACK_INSET);
            }
        });

        // Loading dialog while creating modules
        loadingDialog = new LoadingDialog(requireActivity());

        // Info dialog for volume style modules
        infoDialog = new InfoDialog(requireActivity());

        // Volume style
        LinearLayout volume_style = view.findViewById(R.id.volume_style);

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
                SystemUtil.getStoragePermission(requireActivity());
            } else {
                if (realCheckedId == -1) {
                    Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_select_style), Toast.LENGTH_SHORT).show();
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
            } catch (IOException e) {
                hasErroredOut.set(true);
                Log.e("VolumePanel", e.toString());
            }
            requireActivity().runOnUiThread(() -> new Handler().postDelayed(() -> {
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
        view.findViewById(R.id.volume_thin_bg).findViewById(R.id.volume_ringer_bg).setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), ringerDrawable));
        view.findViewById(R.id.volume_thin_bg).findViewById(R.id.volume_progress_drawable).setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), progressDrawable));
        view.findViewById(R.id.volume_thick_bg).findViewById(R.id.volume_ringer_bg).setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), ringerDrawable));
        view.findViewById(R.id.volume_thick_bg).findViewById(R.id.volume_progress_drawable).setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), progressDrawable));
        view.findViewById(R.id.volume_no_bg).findViewById(R.id.volume_ringer_bg).setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), ringerDrawable));
        view.findViewById(R.id.volume_no_bg).findViewById(R.id.volume_progress_drawable).setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), progressDrawable));

        if (ringerInverse) {
            view.findViewById(R.id.volume_thin_bg).findViewById(R.id.volume_ringer_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
            view.findViewById(R.id.volume_thick_bg).findViewById(R.id.volume_ringer_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
            view.findViewById(R.id.volume_no_bg).findViewById(R.id.volume_ringer_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
        } else {
            view.findViewById(R.id.volume_thin_bg).findViewById(R.id.volume_ringer_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
            view.findViewById(R.id.volume_thick_bg).findViewById(R.id.volume_ringer_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
            view.findViewById(R.id.volume_no_bg).findViewById(R.id.volume_ringer_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
        }

        if (progressInverse) {
            view.findViewById(R.id.volume_thin_bg).findViewById(R.id.volume_progress_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
            view.findViewById(R.id.volume_thick_bg).findViewById(R.id.volume_progress_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
            view.findViewById(R.id.volume_no_bg).findViewById(R.id.volume_progress_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimary));
        } else {
            view.findViewById(R.id.volume_thin_bg).findViewById(R.id.volume_progress_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
            view.findViewById(R.id.volume_thick_bg).findViewById(R.id.volume_progress_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
            view.findViewById(R.id.volume_no_bg).findViewById(R.id.volume_progress_icon).setBackgroundTintList(ContextCompat.getColorStateList(Iconify.getAppContext(), R.color.textColorPrimaryInverse));
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