package com.drdisagree.iconify.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.installer.QsShapeInstaller;
import com.drdisagree.iconify.utils.DisplayUtil;
import com.drdisagree.iconify.utils.OverlayUtils;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class QsShapesPixel extends AppCompatActivity {

    private static final String OUTLINE_PIXEL_KEY = "IconifyComponentQSSP4.overlay";
    private static final String LEAFY_OUTLINE_PIXEL_KEY = "IconifyComponentQSSP5.overlay";

    LinearLayout[] Container;
    LinearLayout OutlinePixelContainer, LeafyOutlinePixelContainer;
    Button OutlinePixel_Enable, OutlinePixel_Disable, LeafyOutlinePixel_Enable, LeafyOutlinePixel_Disable;
    LinearLayout[] qstile_orientation_list;
    private ViewGroup container;
    private LinearLayout spinner;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qs_shapes_pixel);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("QS Panel Tiles");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Progressbar while enabling or disabling pack
        spinner = findViewById(R.id.progressBar_QsShapePixel);

        // Don't show progressbar on opening page
        spinner.setVisibility(View.GONE);

        // Qs Shapes list items
        container = (ViewGroup) findViewById(R.id.qs_tiles_list_pixel);

        ViewGroup.MarginLayoutParams marginParams;
        LinearLayout.LayoutParams layoutParams;

        // Qs Shape add items in list
        addItem(R.id.outline_pixel_container, R.id.outline_pixel_qstile1, R.id.outline_pixel_qstile2, R.id.outline_pixel_qstile3, R.id.outline_pixel_qstile4, "Outline", R.id.outline_pixel_enable, R.id.outline_pixel_disable, R.id.outline_pixel_qstile_orientation);
        addItem(R.id.leafy_outline_pixel_container, R.id.leafy_outline_pixel_qstile1, R.id.leafy_outline_pixel_qstile2, R.id.leafy_outline_pixel_qstile3, R.id.leafy_outline_pixel_qstile4, "Leafy Outline", R.id.leafy_outline_pixel_enable, R.id.leafy_outline_pixel_disable, R.id.leafy_pixel_qstile_orientation);

        // Outline
        OutlinePixelContainer = findViewById(R.id.outline_pixel_container);
        OutlinePixel_Enable = findViewById(R.id.outline_pixel_enable);
        OutlinePixel_Disable = findViewById(R.id.outline_pixel_disable);
        LinearLayout OutlinePixel_QsTile1 = findViewById(R.id.outline_pixel_qstile1);
        LinearLayout OutlinePixel_QsTile2 = findViewById(R.id.outline_pixel_qstile2);
        LinearLayout OutlinePixel_QsTile3 = findViewById(R.id.outline_pixel_qstile3);
        LinearLayout OutlinePixel_QsTile4 = findViewById(R.id.outline_pixel_qstile4);
        OutlinePixel_QsTile1.setBackground(ContextCompat.getDrawable(QsShapesPixel.this, R.drawable.qs_shape_outline_pixel_enabled));
        OutlinePixel_QsTile2.setBackground(ContextCompat.getDrawable(QsShapesPixel.this, R.drawable.qs_shape_outline_pixel_disabled));
        OutlinePixel_QsTile3.setBackground(ContextCompat.getDrawable(QsShapesPixel.this, R.drawable.qs_shape_outline_pixel_disabled));
        OutlinePixel_QsTile4.setBackground(ContextCompat.getDrawable(QsShapesPixel.this, R.drawable.qs_shape_outline_pixel_enabled));

        // Leafy Outline
        LeafyOutlinePixelContainer = findViewById(R.id.leafy_outline_pixel_container);
        LeafyOutlinePixel_Enable = findViewById(R.id.leafy_outline_pixel_enable);
        LeafyOutlinePixel_Disable = findViewById(R.id.leafy_outline_pixel_disable);
        LinearLayout LeafyOutlinePixel_QsTile1 = findViewById(R.id.leafy_outline_pixel_qstile1);
        LinearLayout LeafyOutlinePixel_QsTile2 = findViewById(R.id.leafy_outline_pixel_qstile2);
        LinearLayout LeafyOutlinePixel_QsTile3 = findViewById(R.id.leafy_outline_pixel_qstile3);
        LinearLayout LeafyOutlinePixel_QsTile4 = findViewById(R.id.leafy_outline_pixel_qstile4);
        LeafyOutlinePixel_QsTile1.setBackground(ContextCompat.getDrawable(QsShapesPixel.this, R.drawable.qs_shape_leafy_outline_pixel_enabled));
        LeafyOutlinePixel_QsTile2.setBackground(ContextCompat.getDrawable(QsShapesPixel.this, R.drawable.qs_shape_leafy_outline_pixel_disabled));
        LeafyOutlinePixel_QsTile3.setBackground(ContextCompat.getDrawable(QsShapesPixel.this, R.drawable.qs_shape_leafy_outline_pixel_disabled));
        LeafyOutlinePixel_QsTile4.setBackground(ContextCompat.getDrawable(QsShapesPixel.this, R.drawable.qs_shape_leafy_outline_pixel_enabled));

        // List of QsShape
        Container = new LinearLayout[]{OutlinePixelContainer, LeafyOutlinePixelContainer};

        // Enable onClick event
        enableOnClickListener(OutlinePixelContainer, OutlinePixel_Enable, OutlinePixel_Disable, OUTLINE_PIXEL_KEY, 4, false);
        enableOnClickListener(LeafyOutlinePixelContainer, LeafyOutlinePixel_Enable, LeafyOutlinePixel_Disable, LEAFY_OUTLINE_PIXEL_KEY, 5, false);

        // List of orientation
        qstile_orientation_list = new LinearLayout[]{findViewById(R.id.outline_pixel_qstile_orientation), findViewById(R.id.leafy_pixel_qstile_orientation)};

        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            for (LinearLayout qstile_orientation : qstile_orientation_list)
                qstile_orientation.setOrientation(LinearLayout.VERTICAL);
        } else {
            for (LinearLayout qstile_orientation : qstile_orientation_list)
                qstile_orientation.setOrientation(LinearLayout.HORIZONTAL);
        }

        refreshBackground();
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
        // List of orientation
        qstile_orientation_list = new LinearLayout[]{findViewById(R.id.outline_pixel_qstile_orientation), findViewById(R.id.leafy_pixel_qstile_orientation)};
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            for (LinearLayout qstile_orientation : qstile_orientation_list)
                qstile_orientation.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            for (LinearLayout qstile_orientation : qstile_orientation_list)
                qstile_orientation.setOrientation(LinearLayout.VERTICAL);
        }
    }

    // Function to check for layout changes
    private void refreshLayout(LinearLayout layout) {
        for (LinearLayout linearLayout : Container) {
            if (!(linearLayout == layout)) {
                if (linearLayout == OutlinePixelContainer) {
                    OutlinePixel_Enable.setVisibility(View.GONE);
                    OutlinePixel_Disable.setVisibility(View.GONE);
                } else if (linearLayout == LeafyOutlinePixelContainer) {
                    LeafyOutlinePixel_Enable.setVisibility(View.GONE);
                    LeafyOutlinePixel_Disable.setVisibility(View.GONE);
                }
            }
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        checkIfApplied(OutlinePixelContainer, 4);
        checkIfApplied(LeafyOutlinePixelContainer, 5);
    }

    // Function for onClick events
    private void enableOnClickListener(LinearLayout layout, Button enable, Button disable, String key, int index, boolean hidelabel) {

        // Set onClick operation for options in list
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLayout(layout);
                if (!PrefConfig.loadPrefBool(Iconify.getAppContext(), key)) {
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
            }
        });

        // Set onClick operation for Enable button
        enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLayout(layout);
                // Show spinner
                spinner.setVisibility(View.VISIBLE);
                // Block touch
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        disable_others(key);
                        QsShapeInstaller.install_pack(index);
                        if (hidelabel) {
                            OverlayUtils.enableOverlay("IconifyComponentQSHL.overlay");
                            PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentQSHL.overlay", true);
                        } else {
                            OverlayUtils.disableOverlay("IconifyComponentQSHL.overlay");
                            PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentQSHL.overlay", false);
                        }
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
                PrefConfig.savePrefBool(Iconify.getAppContext(), key, true);
                // Wait 1 second
                spinner.postDelayed(new Runnable() {
                    public void run() {
                        // Hide spinner
                        spinner.setVisibility(View.GONE);
                        // Unblock touch
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        // Change background to selected
                        background(layout.getId(), R.drawable.container_selected);
                        // Change button visibility
                        enable.setVisibility(View.GONE);
                        disable.setVisibility(View.VISIBLE);
                        refreshBackground();
                        Toast.makeText(Iconify.getAppContext(), "Applied", Toast.LENGTH_SHORT).show();
                    }
                }, 400);
            }
        });

        // Set onClick operation for Disable button
        disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show spinner
                spinner.setVisibility(View.VISIBLE);
                // Block touch
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        QsShapeInstaller.disable_pack(index);
                        if (hidelabel) {
                            OverlayUtils.disableOverlay("IconifyComponentQSHL.overlay");
                            PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentQSHL.overlay", false);
                        }
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
                PrefConfig.savePrefBool(Iconify.getAppContext(), key, false);
                // Wait 1 second
                spinner.postDelayed(new Runnable() {
                    public void run() {
                        // Hide spinner
                        spinner.setVisibility(View.GONE);
                        // Unblock touch
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        // Change background to selected
                        background(layout.getId(), R.drawable.container);
                        // Change button visibility
                        disable.setVisibility(View.GONE);
                        enable.setVisibility(View.VISIBLE);
                        refreshBackground();
                        Toast.makeText(Iconify.getAppContext(), "Disabled", Toast.LENGTH_SHORT).show();
                    }
                }, 400);
            }
        });
    }

    // Function to disable other packs if one is applied
    private void disable_others(String pack) {
        PrefConfig.savePrefBool(Iconify.getAppContext(), OUTLINE_PIXEL_KEY, pack.equals(OUTLINE_PIXEL_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), LEAFY_OUTLINE_PIXEL_KEY, pack.equals(LEAFY_OUTLINE_PIXEL_KEY));
    }

    // Function to change applied pack's bg
    private void checkIfApplied(LinearLayout layout, int qsshape) {
        if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentQSSP" + qsshape + ".overlay")) {
            background(layout.getId(), R.drawable.container_selected);
        } else {
            background(layout.getId(), R.drawable.container);
        }
    }

    // Function to add border for installed pack
    private void background(int id, int drawable) {
        LinearLayout layout = findViewById(id);
        layout.setBackground(ContextCompat.getDrawable(this, drawable));
    }

    private void addItem(int id, int qstile1id, int qstile2id, int qstile3id, int qstile4id, String title, int enableid, int disableid, int orientation) {
        View list = LayoutInflater.from(this).inflate(R.layout.list_option_qstile_pixel, container, false);

        TextView name = list.findViewById(R.id.list_title_qstile);
        Button enable = list.findViewById(R.id.list_button_enable_qstile);
        Button disable = list.findViewById(R.id.list_button_disable_qstile);
        LinearLayout qstile1 = list.findViewById(R.id.qs_tile1);
        LinearLayout qstile2 = list.findViewById(R.id.qs_tile2);
        LinearLayout qstile3 = list.findViewById(R.id.qs_tile3);
        LinearLayout qstile4 = list.findViewById(R.id.qs_tile4);
        LinearLayout qs_tile_orientation = list.findViewById(R.id.qs_tile_orientation);

        list.setId(id);
        name.setText(title);

        enable.setId(enableid);
        disable.setId(disableid);

        qstile1.setId(qstile1id);
        qstile2.setId(qstile2id);
        qstile3.setId(qstile3id);
        qstile4.setId(qstile4id);

        qs_tile_orientation.setId(orientation);

        container.addView(list);
    }
}