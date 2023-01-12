package com.drdisagree.iconify.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.overlaymanager.QsShapeManager;
import com.drdisagree.iconify.ui.fragment.LoadingDialog;
import com.drdisagree.iconify.utils.DisplayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class QsShapes extends AppCompatActivity {

    ArrayList<String> QSSHAPE_KEY = new ArrayList<>();

    LoadingDialog loadingDialog;
    private ViewGroup container;

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

        // Qs row column item on click
        LinearLayout qs_row_column = findViewById(R.id.qs_row_column);
        qs_row_column.setOnClickListener(v -> {
            Intent intent = new Intent(QsShapes.this, QsRowColumn.class);
            startActivity(intent);
        });

        // Qs text color item on click
        LinearLayout qs_text_color = findViewById(R.id.qs_text_color);
        qs_text_color.setOnClickListener(v -> {
            Intent intent = new Intent(QsShapes.this, QsIconLabel.class);
            startActivity(intent);
        });

        // Pixel variant item on click
        LinearLayout qs_shape_pixel_variant = findViewById(R.id.qs_shape_pixel_variant);
        qs_shape_pixel_variant.setOnClickListener(v -> {
            Intent intent = new Intent(QsShapes.this, QsShapesPixel.class);
            startActivity(intent);
        });

        // Qs Shapes list items
        container = (ViewGroup) findViewById(R.id.qs_shape_list);
        ArrayList<Object[]> qsshape_list = new ArrayList<>();

        // Qs Shape add items in list
        qsshape_list.add(new Object[]{"Default", R.drawable.qs_shape_default_enabled, R.drawable.qs_shape_default_disabled});
        qsshape_list.add(new Object[]{"Double Layer", R.drawable.qs_shape_doublelayer_enabled, R.drawable.qs_shape_doublelayer_disabled});
        qsshape_list.add(new Object[]{"Shaded Layer", R.drawable.qs_shape_shadedlayer_enabled, R.drawable.qs_shape_shadedlayer_disabled});
        qsshape_list.add(new Object[]{"Outline", R.drawable.qs_shape_outline_enabled, R.drawable.qs_shape_outline_disabled});
        qsshape_list.add(new Object[]{"Leafy Outline", R.drawable.qs_shape_leafy_outline_enabled, R.drawable.qs_shape_leafy_outline_disabled});
        qsshape_list.add(new Object[]{"Neumorph", R.drawable.qs_shape_neumorph_enabled, R.drawable.qs_shape_neumorph_disabled});
        qsshape_list.add(new Object[]{"Neumorph Outline", R.drawable.qs_shape_neumorph_outline_enabled, R.drawable.qs_shape_neumorph_outline_disabled});
        qsshape_list.add(new Object[]{"Surround", R.drawable.qs_shape_surround_enabled, R.drawable.qs_shape_surround_disabled});
        qsshape_list.add(new Object[]{"Bookmark", R.drawable.qs_shape_bookmark_enabled, R.drawable.qs_shape_bookmark_disabled});

        addItem(qsshape_list);

        // Generate keys for preference
        for (int i = 0; i < container.getChildCount(); i++) {
            QSSHAPE_KEY.add("IconifyComponentQSSN" + (i + 1) + ".overlay");
        }

        ViewGroup.MarginLayoutParams marginParams;
        LinearLayout.LayoutParams layoutParams;

        // Set custom margins for Surround
        marginParams = new ViewGroup.MarginLayoutParams(container.getChildAt(7).findViewById(R.id.qs_icon1).getLayoutParams());
        marginParams.setMargins(DisplayUtil.IntToDp(4), 0, DisplayUtil.IntToDp(22), 0);
        layoutParams = new LinearLayout.LayoutParams(marginParams);
        container.getChildAt(7).findViewById(R.id.qs_icon1).setLayoutParams(layoutParams);
        marginParams = new ViewGroup.MarginLayoutParams(container.getChildAt(7).findViewById(R.id.qs_icon2).getLayoutParams());
        marginParams.setMargins(DisplayUtil.IntToDp(4), 0, DisplayUtil.IntToDp(22), 0);
        layoutParams = new LinearLayout.LayoutParams(marginParams);
        container.getChildAt(7).findViewById(R.id.qs_icon2).setLayoutParams(layoutParams);
        marginParams = new ViewGroup.MarginLayoutParams(container.getChildAt(7).findViewById(R.id.qs_icon3).getLayoutParams());
        marginParams.setMargins(DisplayUtil.IntToDp(4), 0, DisplayUtil.IntToDp(22), 0);
        layoutParams = new LinearLayout.LayoutParams(marginParams);
        container.getChildAt(7).findViewById(R.id.qs_icon3).setLayoutParams(layoutParams);
        marginParams = new ViewGroup.MarginLayoutParams(container.getChildAt(7).findViewById(R.id.qs_icon4).getLayoutParams());
        marginParams.setMargins(DisplayUtil.IntToDp(4), 0, DisplayUtil.IntToDp(22), 0);
        layoutParams = new LinearLayout.LayoutParams(marginParams);
        container.getChildAt(7).findViewById(R.id.qs_icon4).setLayoutParams(layoutParams);

        // Set custom margins for Bookmark
        marginParams = new ViewGroup.MarginLayoutParams(container.getChildAt(8).findViewById(R.id.qs_icon1).getLayoutParams());
        marginParams.setMargins(DisplayUtil.IntToDp(4), 0, DisplayUtil.IntToDp(26), 0);
        layoutParams = new LinearLayout.LayoutParams(marginParams);
        container.getChildAt(8).findViewById(R.id.qs_icon1).setLayoutParams(layoutParams);
        marginParams = new ViewGroup.MarginLayoutParams(container.getChildAt(8).findViewById(R.id.qs_icon2).getLayoutParams());
        marginParams.setMargins(DisplayUtil.IntToDp(4), 0, DisplayUtil.IntToDp(26), 0);
        layoutParams = new LinearLayout.LayoutParams(marginParams);
        container.getChildAt(8).findViewById(R.id.qs_icon2).setLayoutParams(layoutParams);
        marginParams = new ViewGroup.MarginLayoutParams(container.getChildAt(8).findViewById(R.id.qs_icon3).getLayoutParams());
        marginParams.setMargins(DisplayUtil.IntToDp(4), 0, DisplayUtil.IntToDp(26), 0);
        layoutParams = new LinearLayout.LayoutParams(marginParams);
        container.getChildAt(8).findViewById(R.id.qs_icon3).setLayoutParams(layoutParams);
        marginParams = new ViewGroup.MarginLayoutParams(container.getChildAt(8).findViewById(R.id.qs_icon4).getLayoutParams());
        marginParams.setMargins(DisplayUtil.IntToDp(4), 0, DisplayUtil.IntToDp(26), 0);
        layoutParams = new LinearLayout.LayoutParams(marginParams);
        container.getChildAt(8).findViewById(R.id.qs_icon4).setLayoutParams(layoutParams);

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
            if (PrefConfig.loadPrefBool(QSSHAPE_KEY.get(i))) {
                child.setBackground(ContextCompat.getDrawable(QsShapes.this, R.drawable.container_selected));
            } else {
                child.setBackground(ContextCompat.getDrawable(QsShapes.this, R.drawable.container));
            }
        }
    }

    // Function to disable other packs if one is applied
    private void disable_others(String pack) {
        for (int i = 0; i < References.TOTAL_QSSHAPES; i++)
            PrefConfig.savePrefBool(QSSHAPE_KEY.get(i), pack.equals(QSSHAPE_KEY.get(i)));
    }

    // Function for onClick events
    private void enableOnClickListener(LinearLayout layout, Button enable, Button
            disable, String key, int index) {
        // Set onClick operation for options in list
        layout.setOnClickListener(v -> {
            refreshLayout(layout);
            if (!PrefConfig.loadPrefBool(key)) {
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
                disable_others(key);
                QsShapeManager.install_pack(index + 1);

                runOnUiThread(() -> {
                    PrefConfig.savePrefBool(key, true);

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
                    PrefConfig.savePrefBool(key, false);

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

    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.list_option_qsshape, container, false);

            TextView name = list.findViewById(R.id.list_title_qsshape);
            name.setText((String) pack.get(i)[0]);

            list.findViewById(R.id.qs_tile1).setBackground(ContextCompat.getDrawable(QsShapes.this, (int) pack.get(i)[1]));
            list.findViewById(R.id.qs_tile2).setBackground(ContextCompat.getDrawable(QsShapes.this, (int) pack.get(i)[2]));
            list.findViewById(R.id.qs_tile3).setBackground(ContextCompat.getDrawable(QsShapes.this, (int) pack.get(i)[2]));
            list.findViewById(R.id.qs_tile4).setBackground(ContextCompat.getDrawable(QsShapes.this, (int) pack.get(i)[1]));

            container.addView(list);
        }
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}