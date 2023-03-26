package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.SELECTED_ICON_SHAPE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.compiler.OnDemandCompiler;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class IconShape extends AppCompatActivity {

    private FlexboxLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_shape);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_icon_shape));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Icon masking shape list
        container = findViewById(R.id.icon_shape_preview_container);
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
            View list = LayoutInflater.from(this).inflate(R.layout.view_icon_shape, container, false);

            LinearLayout icon_container = list.findViewById(R.id.mask_shape);
            icon_container.setBackground(getResources().getDrawable((int) pack.get(i)[0]));

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
                    if (!Environment.isExternalStorageManager()) {
                        SystemUtil.getStoragePermission(this);
                    } else {
                        AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                        try {
                            hasErroredOut.set(OnDemandCompiler.buildOverlay("SIS", finalI, FRAMEWORK_PACKAGE));
                        } catch (IOException e) {
                            hasErroredOut.set(true);
                            Log.e("IconShape", e.toString());
                        }

                        if (!hasErroredOut.get()) {
                            Prefs.putInt(SELECTED_ICON_SHAPE, finalI);
                            OverlayUtil.enableOverlay("IconifyComponentSIS.overlay");
                            Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();

                        refreshBackground();
                    }
                }
            });

            container.addView(list);
        }
    }

    // Function to check for bg drawable changes
    @SuppressLint("SetTextI18n")
    private void refreshBackground() {
        for (int i = 0; i < container.getChildCount(); i++) {
            LinearLayout child = container.getChildAt(i).findViewById(R.id.list_item_shape);
            TextView title = child.findViewById(R.id.shape_name);
            if (i == Prefs.getInt(SELECTED_ICON_SHAPE, 0)) {
                title.setTextColor(getResources().getColor(R.color.colorSuccess));
            } else {
                title.setTextColor(getResources().getColor(R.color.textColorSecondary));
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}