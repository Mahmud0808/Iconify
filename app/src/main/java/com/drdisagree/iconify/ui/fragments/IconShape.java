package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.SELECTED_ICON_SHAPE;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentIconShapeBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.dialogs.LoadingDialog;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.drdisagree.iconify.utils.overlay.compiler.OnDemandCompiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class IconShape extends BaseFragment {

    private FragmentIconShapeBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentIconShapeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_icon_shape);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        // Icon masking shape list
        ArrayList<Object[]> icon_shape_preview_styles = new ArrayList<>();

        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_none, R.string.icon_mask_style_none});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_pebble, R.string.icon_mask_style_pebble});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_rounded_hexagon, R.string.icon_mask_style_hexagon});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_samsung, R.string.icon_mask_style_samsung});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_scroll, R.string.icon_mask_style_scroll});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_teardrops, R.string.icon_mask_style_teardrop});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_square, R.string.icon_mask_style_square});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_rounded_rectangle, R.string.icon_mask_style_rounded_rectangle});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_ios, R.string.icon_mask_style_ios});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_cloudy, R.string.icon_mask_style_cloudy});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_cylinder, R.string.icon_mask_style_cylinder});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_flower, R.string.icon_mask_style_flower});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_heart, R.string.icon_mask_style_heart});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_leaf, R.string.icon_mask_style_leaf});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_stretched, R.string.icon_mask_style_stretched});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_tapered_rectangle, R.string.icon_mask_style_tapered_rectangle});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_vessel, R.string.icon_mask_style_vessel});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_rohie_meow, R.string.icon_mask_style_rice_rohie_meow});
        icon_shape_preview_styles.add(new Object[]{R.drawable.icon_shape_force_round, R.string.icon_mask_style_force_round});

        addItem(icon_shape_preview_styles);

        refreshBackground();

        return view;
    }

    // Function to add new item in list
    @SuppressLint("UseCompatLoadingForDrawables")
    private void addItem(ArrayList<Object[]> pack) {
        @ColorInt int colorBackground = Iconify.getAppContextLocale().getResources().getColor(R.color.colorBackground, Iconify.getAppContext().getTheme());

        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(requireContext()).inflate(R.layout.view_icon_shape, binding.iconShapePreviewContainer, false);

            LinearLayout icon_container_bg = list.findViewById(R.id.mask_shape_bg);
            LinearLayout icon_container_fg = list.findViewById(R.id.mask_shape_fg);

            icon_container_bg.setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), (int) pack.get(i)[0]));
            icon_container_fg.setBackground(ContextCompat.getDrawable(Iconify.getAppContext(), (int) pack.get(i)[0]));
            icon_container_fg.setBackgroundTintList(ColorStateList.valueOf(colorBackground));

            TextView style_name = list.findViewById(R.id.shape_name);
            style_name.setText(getResources().getString((int) pack.get(i)[1]));

            int finalI = i;
            list.setOnClickListener(v -> {
                if (finalI == 0) {
                    Prefs.putInt(SELECTED_ICON_SHAPE, finalI);
                    OverlayUtil.disableOverlay("IconifyComponentSIS.overlay");
                    Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();

                    refreshBackground();
                } else {
                    if (!SystemUtil.hasStoragePermission()) {
                        SystemUtil.requestStoragePermission(requireContext());
                    } else {
                        // Show loading dialog
                        loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

                        new Thread(() -> {
                            AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                            try {
                                hasErroredOut.set(OnDemandCompiler.buildOverlay("SIS", finalI, FRAMEWORK_PACKAGE, true));
                            } catch (IOException e) {
                                hasErroredOut.set(true);
                                Log.e("IconShape", e.toString());
                            }

                            if (!hasErroredOut.get()) {
                                Prefs.putInt(SELECTED_ICON_SHAPE, finalI);
                                refreshBackground();
                            }

                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                // Hide loading dialog
                                loadingDialog.hide();

                                if (!hasErroredOut.get()) {
                                    Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                }
                            }, 3000);
                        }).start();
                    }
                }
            });

            binding.iconShapePreviewContainer.addView(list);
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        @ColorInt int colorSuccess = Iconify.getAppContextLocale().getResources().getColor(R.color.colorSuccess, Iconify.getAppContext().getTheme());
        @ColorInt int textColorSecondary = Iconify.getAppContextLocale().getResources().getColor(R.color.textColorSecondary, Iconify.getAppContext().getTheme());

        for (int i = 0; i < binding.iconShapePreviewContainer.getChildCount(); i++) {
            LinearLayout child = binding.iconShapePreviewContainer.getChildAt(i).findViewById(R.id.list_item_shape);
            TextView title = child.findViewById(R.id.shape_name);
            LinearLayout icon_container_bg = child.findViewById(R.id.mask_shape_bg);

            if (i == Prefs.getInt(SELECTED_ICON_SHAPE, 0)) {
                icon_container_bg.setBackgroundTintList(ColorStateList.valueOf(colorSuccess));
                title.setTextColor(colorSuccess);
            } else {
                icon_container_bg.setBackgroundTintList(ColorStateList.valueOf(textColorSecondary));
                title.setTextColor(textColorSecondary);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
}