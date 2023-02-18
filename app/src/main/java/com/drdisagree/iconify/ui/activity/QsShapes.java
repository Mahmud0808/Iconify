package com.drdisagree.iconify.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.overlaymanager.QsShapeManager;
import com.drdisagree.iconify.ui.view.LoadingDialog;
import com.drdisagree.iconify.utils.DisplayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class QsShapes extends AppCompatActivity {

    ArrayList<String> QSSHAPE_KEY = new ArrayList<>();

    LoadingDialog loadingDialog;
    ViewGroup.MarginLayoutParams marginParams;
    LinearLayout.LayoutParams layoutParams;
    private ViewGroup container, container_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qs_shapes);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_qs_shape));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // Activities list
        container_activity = findViewById(R.id.qs_shape_list_activity);
        ArrayList<Object[]> qs_shape_list_activity = new ArrayList<>();

        // Activities add items in list
        qs_shape_list_activity.add(new Object[]{QsRowColumn.class, getResources().getString(R.string.row_and_column_title), getResources().getString(R.string.row_and_column_desc), R.drawable.ic_qs_row_column});
        qs_shape_list_activity.add(new Object[]{QsIconLabel.class, getResources().getString(R.string.icon_and_label_title), getResources().getString(R.string.icon_and_label_desc), R.drawable.ic_qs_icon_and_label});
        qs_shape_list_activity.add(new Object[]{QsTileSize.class, getResources().getString(R.string.activity_title_qs_tile_size), getResources().getString(R.string.activity_desc_qs_tile_size), R.drawable.ic_qs_tile_size});
        qs_shape_list_activity.add(new Object[]{QsShapesPixel.class, getResources().getString(R.string.activity_title_pixel_variant), getResources().getString(R.string.activity_desc_pixel_variant), R.drawable.ic_pixel_device});

        addActivityItem(qs_shape_list_activity);
        fixViewGroup(container_activity);

        // Enable onClick event
        for (int i = 0; i < qs_shape_list_activity.size(); i++) {
            LinearLayout child = container_activity.getChildAt(i).findViewById(R.id.list_item);
            int finalI = i;
            child.setOnClickListener(v -> {
                Intent intent = new Intent(QsShapes.this, (Class<?>) qs_shape_list_activity.get(finalI)[0]);
                startActivity(intent);
            });
        }

        // Qs Shapes list items
        container = findViewById(R.id.qs_shape_list);
        ArrayList<Object[]> qsshape_list = new ArrayList<>();

        // Qs Shape add items in list
        qsshape_list.add(new Object[]{"Default", R.drawable.qs_shape_default_enabled, R.drawable.qs_shape_default_disabled});
        qsshape_list.add(new Object[]{"Double Layer", R.drawable.qs_shape_doublelayer_enabled, R.drawable.qs_shape_doublelayer_disabled});
        qsshape_list.add(new Object[]{"Shaded Layer", R.drawable.qs_shape_shadedlayer_enabled, R.drawable.qs_shape_shadedlayer_disabled});
        qsshape_list.add(new Object[]{"Outline", R.drawable.qs_shape_outline_enabled, R.drawable.qs_shape_outline_disabled});
        qsshape_list.add(new Object[]{"Leafy Outline", R.drawable.qs_shape_leafy_outline_enabled, R.drawable.qs_shape_leafy_outline_disabled});
        qsshape_list.add(new Object[]{"Neumorph", R.drawable.qs_shape_neumorph_enabled, R.drawable.qs_shape_neumorph_disabled});
        qsshape_list.add(new Object[]{"Surround", R.drawable.qs_shape_surround_enabled, R.drawable.qs_shape_surround_disabled});
        qsshape_list.add(new Object[]{"Bookmark", R.drawable.qs_shape_bookmark_enabled, R.drawable.qs_shape_bookmark_disabled});
        qsshape_list.add(new Object[]{"Neumorph Outline", R.drawable.qs_shape_neumorph_outline_enabled, R.drawable.qs_shape_neumorph_outline_disabled});
        qsshape_list.add(new Object[]{"Reflected", R.drawable.qs_shape_reflected_enabled, R.drawable.qs_shape_reflected_disabled});
        qsshape_list.add(new Object[]{"Reflected Fill", R.drawable.qs_shape_reflected_fill_enabled, R.drawable.qs_shape_reflected_fill_disabled});
        qsshape_list.add(new Object[]{"Divided", R.drawable.qs_shape_divided_enabled, R.drawable.qs_shape_divided_disabled});
        qsshape_list.add(new Object[]{"Lighty", R.drawable.qs_shape_lighty_enabled, R.drawable.qs_shape_lighty_disabled});
        qsshape_list.add(new Object[]{"Bottom Outline", R.drawable.qs_shape_bottom_outline_enabled, R.drawable.qs_shape_bottom_outline_disabled});
        qsshape_list.add(new Object[]{"Cyberponk", R.drawable.qs_shape_cyberponk_enabled, R.drawable.qs_shape_cyberponk_disabled});
        qsshape_list.add(new Object[]{"Cyberponk v2", R.drawable.qs_shape_cyberponk_v2_enabled, R.drawable.qs_shape_cyberponk_v2_disabled});
        qsshape_list.add(new Object[]{"Semi Transparent", R.drawable.qs_shape_semi_transparent_enabled, R.drawable.qs_shape_semi_transparent_disabled});
        qsshape_list.add(new Object[]{"Thin Outline", R.drawable.qs_shape_thin_outline_enabled, R.drawable.qs_shape_thin_outline_disabled});
        qsshape_list.add(new Object[]{"Purfect", R.drawable.qs_shape_purfect_enabled, R.drawable.qs_shape_purfect_disabled});

        addItem(qsshape_list);

        // Generate keys for preference
        for (int i = 0; i < container.getChildCount(); i++) {
            QSSHAPE_KEY.add("IconifyComponentQSSN" + (i + 1) + ".overlay");
        }

        // Custom margins for Surround
        setMargin(6, 4, 22);

        // Custom margins for Bookmark
        setMargin(7, 4, 26);

        // Custom margins for Divided
        setMargin(11, 4, 22);

        // Enable onClick event
        for (int i = 0; i < container.getChildCount(); i++) {
            enableOnClickListener(container.getChildAt(i).findViewById(R.id.qsshape_child),
                    container.getChildAt(i).findViewById(R.id.list_button_enable_qsshape),
                    container.getChildAt(i).findViewById(R.id.list_button_disable_qsshape),
                    QSSHAPE_KEY.get(i), i);
        }

        refreshBackground();

        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            for (int i = 0; i < container.getChildCount(); i++) {
                LinearLayout child = container.getChildAt(i).findViewById(R.id.qsshape_child);
                LinearLayout qstile_orientation = child.findViewById(R.id.qs_tile_orientation);
                qstile_orientation.setOrientation(LinearLayout.HORIZONTAL);
            }
        } else {
            for (int i = 0; i < container.getChildCount(); i++) {
                LinearLayout child = container.getChildAt(i).findViewById(R.id.qsshape_child);
                LinearLayout qstile_orientation = child.findViewById(R.id.qs_tile_orientation);
                qstile_orientation.setOrientation(LinearLayout.VERTICAL);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Change orientation in landscape / portrait mode
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            for (int i = 0; i < container.getChildCount(); i++) {
                LinearLayout child = container.getChildAt(i).findViewById(R.id.qsshape_child);
                LinearLayout qstile_orientation = child.findViewById(R.id.qs_tile_orientation);
                qstile_orientation.setOrientation(LinearLayout.HORIZONTAL);
            }
        } else {
            for (int i = 0; i < container.getChildCount(); i++) {
                LinearLayout child = container.getChildAt(i).findViewById(R.id.qsshape_child);
                LinearLayout qstile_orientation = child.findViewById(R.id.qs_tile_orientation);
                qstile_orientation.setOrientation(LinearLayout.VERTICAL);
            }
        }
    }


    // Function to check for layout changes
    private void refreshLayout(LinearLayout layout) {
        for (int i = 0; i < container.getChildCount(); i++) {
            LinearLayout child = container.getChildAt(i).findViewById(R.id.qsshape_child);
            if (!(child == layout)) {
                container.getChildAt(i).findViewById(R.id.list_button_enable_qsshape).setVisibility(View.GONE);
                container.getChildAt(i).findViewById(R.id.list_button_disable_qsshape).setVisibility(View.GONE);
            }
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        for (int i = 0; i < container.getChildCount(); i++) {
            LinearLayout child = container.getChildAt(i).findViewById(R.id.qsshape_child);
            if (Prefs.getBoolean(QSSHAPE_KEY.get(i))) {
                child.setBackground(ContextCompat.getDrawable(QsShapes.this, R.drawable.container_selected));
            } else {
                child.setBackground(ContextCompat.getDrawable(QsShapes.this, R.drawable.container));
            }
        }
    }

    // Function for onClick events
    private void enableOnClickListener(LinearLayout layout, Button enable, Button
            disable, String key, int index) {
        // Set onClick operation for options in list
        layout.setOnClickListener(v -> {
            refreshLayout(layout);
            if (!Prefs.getBoolean(key)) {
                disable.setVisibility(View.GONE);
                if (enable.getVisibility() == View.VISIBLE)
                    enable.setVisibility(View.GONE);
                else
                    enable.setVisibility(View.VISIBLE);
            } else {
                enable.setVisibility(View.GONE);
                if (disable.getVisibility() == View.VISIBLE)
                    disable.setVisibility(View.GONE);
                else
                    disable.setVisibility(View.VISIBLE);
            }
        });

        // Set onClick operation for Enable button
        enable.setOnClickListener(v -> {
            refreshLayout(layout);
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                QsShapeManager.install_pack(index + 1);

                runOnUiThread(() -> {
                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Change button visibility
                        enable.setVisibility(View.GONE);
                        disable.setVisibility(View.VISIBLE);
                        refreshBackground();

                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    }, 2000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });

        // Set onClick operation for Disable button
        disable.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                QsShapeManager.disable_pack(index + 1);

                runOnUiThread(() -> {
                    new Handler().postDelayed(() -> {
                        loadingDialog.hide();

                        // Change button visibility
                        disable.setVisibility(View.GONE);
                        enable.setVisibility(View.VISIBLE);
                        refreshBackground();

                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                    }, 2000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });
    }

    private void setMargin(int childIndex, int iconMarginLeft, int iconMarginRight) {
        marginParams = new ViewGroup.MarginLayoutParams(container.getChildAt(childIndex).findViewById(R.id.qs_icon1).getLayoutParams());
        marginParams.setMargins(DisplayUtil.IntToDp(iconMarginLeft), 0, DisplayUtil.IntToDp(iconMarginRight), 0);
        layoutParams = new LinearLayout.LayoutParams(marginParams);
        container.getChildAt(childIndex).findViewById(R.id.qs_icon1).setLayoutParams(layoutParams);

        marginParams = new ViewGroup.MarginLayoutParams(container.getChildAt(childIndex).findViewById(R.id.qs_icon2).getLayoutParams());
        marginParams.setMargins(DisplayUtil.IntToDp(iconMarginLeft), 0, DisplayUtil.IntToDp(iconMarginRight), 0);
        layoutParams = new LinearLayout.LayoutParams(marginParams);
        container.getChildAt(childIndex).findViewById(R.id.qs_icon2).setLayoutParams(layoutParams);

        marginParams = new ViewGroup.MarginLayoutParams(container.getChildAt(childIndex).findViewById(R.id.qs_icon3).getLayoutParams());
        marginParams.setMargins(DisplayUtil.IntToDp(iconMarginLeft), 0, DisplayUtil.IntToDp(iconMarginRight), 0);
        layoutParams = new LinearLayout.LayoutParams(marginParams);
        container.getChildAt(childIndex).findViewById(R.id.qs_icon3).setLayoutParams(layoutParams);

        marginParams = new ViewGroup.MarginLayoutParams(container.getChildAt(childIndex).findViewById(R.id.qs_icon4).getLayoutParams());
        marginParams.setMargins(DisplayUtil.IntToDp(iconMarginLeft), 0, DisplayUtil.IntToDp(iconMarginRight), 0);
        layoutParams = new LinearLayout.LayoutParams(marginParams);
        container.getChildAt(childIndex).findViewById(R.id.qs_icon4).setLayoutParams(layoutParams);
    }

    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_list_option_qsshape, container, false);

            TextView name = list.findViewById(R.id.list_title_qsshape);
            name.setText((String) pack.get(i)[0]);

            list.findViewById(R.id.qs_tile1).setBackground(ContextCompat.getDrawable(QsShapes.this, (int) pack.get(i)[1]));
            list.findViewById(R.id.qs_tile2).setBackground(ContextCompat.getDrawable(QsShapes.this, (int) pack.get(i)[2]));
            list.findViewById(R.id.qs_tile3).setBackground(ContextCompat.getDrawable(QsShapes.this, (int) pack.get(i)[2]));
            list.findViewById(R.id.qs_tile4).setBackground(ContextCompat.getDrawable(QsShapes.this, (int) pack.get(i)[1]));

            container.addView(list);
        }
    }

    // Function to add new item in list
    private void addActivityItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_list_menu, container_activity, false);

            TextView title = list.findViewById(R.id.list_title);
            title.setText((String) pack.get(i)[1]);

            TextView desc = list.findViewById(R.id.list_desc);
            desc.setText((String) pack.get(i)[2]);

            ImageView preview = list.findViewById(R.id.list_preview);
            preview.setImageResource((int) pack.get(i)[3]);

            container_activity.addView(list);
        }
    }

    private void fixViewGroup(ViewGroup viewGroup) {
        ((ViewGroup.MarginLayoutParams) viewGroup.getChildAt(viewGroup.getChildCount() - 1).getLayoutParams()).setMargins(0, 0, 0, 0);
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}