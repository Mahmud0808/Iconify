package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.SELECTED_ICON_SHAPE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityIconShapeBinding;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.compiler.OnDemandCompiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class IconShape extends BaseActivity {

    private ActivityIconShapeBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIconShapeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewHelper.setHeader(this, binding.header.toolbar, R.string.activity_title_icon_shape);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

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

        addItem(icon_shape_preview_styles);

        refreshBackground();
    }

    // Function to add new item in list
    @SuppressLint("UseCompatLoadingForDrawables")
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_icon_shape, binding.iconShapePreviewContainer, false);

            LinearLayout icon_container = list.findViewById(R.id.mask_shape);
            icon_container.setBackground(ContextCompat.getDrawable(getApplicationContext(), (int) pack.get(i)[0]));

            TextView style_name = list.findViewById(R.id.shape_name);
            style_name.setText(getResources().getString((int) pack.get(i)[1]));

            int finalI = i;
            list.setOnClickListener(v -> {
                if (finalI == 0) {
                    Prefs.putInt(SELECTED_ICON_SHAPE, finalI);
                    OverlayUtil.disableOverlay("IconifyComponentSIS.overlay");
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();

                    refreshBackground();
                } else {
                    if (!SystemUtil.hasStoragePermission()) {
                        SystemUtil.requestStoragePermission(this);
                    } else {
                        // Show loading dialog
                        loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

                        Runnable runnable = () -> {
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

                            runOnUiThread(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                // Hide loading dialog
                                loadingDialog.hide();

                                if (!hasErroredOut.get()) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                }
                            }, 3000));
                        };
                        Thread thread = new Thread(runnable);
                        thread.start();
                    }
                }
            });

            binding.iconShapePreviewContainer.addView(list);
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        for (int i = 0; i < binding.iconShapePreviewContainer.getChildCount(); i++) {
            LinearLayout child = binding.iconShapePreviewContainer.getChildAt(i).findViewById(R.id.list_item_shape);
            TextView title = child.findViewById(R.id.shape_name);
            if (i == Prefs.getInt(SELECTED_ICON_SHAPE, 0)) {
                title.setTextColor(getResources().getColor(R.color.colorSuccess, getTheme()));
            } else {
                title.setTextColor(getResources().getColor(R.color.textColorSecondary, getTheme()));
            }
        }
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}