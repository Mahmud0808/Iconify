package com.drdisagree.iconify.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.overlaymanager.IconShapeManager;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class IconShape extends AppCompatActivity {

    private FlexboxLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_shape);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_icon_mask));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Icon masking shape list
        container = findViewById(R.id.icon_masking_preview_container);
        ArrayList<Object[]> icon_masking_preview_styles = new ArrayList<>();

        icon_masking_preview_styles.add(new Object[]{R.drawable.icon_shape_none, R.string.icon_mask_style_none});
        icon_masking_preview_styles.add(new Object[]{R.drawable.icon_shape_pebble, R.string.icon_mask_style_pebble});
        icon_masking_preview_styles.add(new Object[]{R.drawable.icon_shape_rounded_hexagon, R.string.icon_mask_style_hexagon});
        icon_masking_preview_styles.add(new Object[]{R.drawable.icon_shape_samsung, R.string.icon_mask_style_samsung});
        icon_masking_preview_styles.add(new Object[]{R.drawable.icon_shape_scroll, R.string.icon_mask_style_scroll});
        icon_masking_preview_styles.add(new Object[]{R.drawable.icon_shape_square, R.string.icon_mask_style_square});
        icon_masking_preview_styles.add(new Object[]{R.drawable.icon_shape_teardrops, R.string.icon_mask_style_teardrop});
        icon_masking_preview_styles.add(new Object[]{R.drawable.icon_shape_rounded_rectangle, R.string.icon_mask_style_rounded_rectangle});

        addItem(icon_masking_preview_styles);

        refreshBackground();
    }

    // Function to add new item in list
    @SuppressLint("UseCompatLoadingForDrawables")
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_shape_icon_mask, container, false);

            LinearLayout icon_container = list.findViewById(R.id.mask_shape);
            icon_container.setBackground(getResources().getDrawable((int) pack.get(i)[0]));

            TextView style_name = list.findViewById(R.id.mask_name);
            style_name.setText(getResources().getString((int) pack.get(i)[1]));

            int finalI = i;
            list.setOnClickListener(v -> {
                IconShapeManager.install_pack(finalI);
                refreshBackground();
            });

            container.addView(list);
        }
    }

    // Function to check for bg drawable changes
    @SuppressLint("SetTextI18n")
    private void refreshBackground() {
        boolean noneApplied = true;

        for (int i = 1; i < container.getChildCount(); i++) {
            LinearLayout child = container.getChildAt(i).findViewById(R.id.list_item_mask);
            TextView title = child.findViewById(R.id.mask_name);
            if (Prefs.getBoolean("IconifyComponentSIS" + i + ".overlay")) {
                title.setTextColor(getResources().getColor(R.color.colorSuccess));
                noneApplied = false;
            } else {
                title.setTextColor(getResources().getColor(R.color.textColorSecondary));
            }
        }

        if (noneApplied)
            ((TextView) container.getChildAt(0).findViewById(R.id.list_item_mask).findViewById(R.id.mask_name)).setTextColor(getResources().getColor(R.color.colorSuccess));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}