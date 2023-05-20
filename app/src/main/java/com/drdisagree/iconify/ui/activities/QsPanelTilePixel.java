package com.drdisagree.iconify.ui.activities;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.adapters.QsShapeAdapter;
import com.drdisagree.iconify.ui.models.QsShapeModel;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.ui.views.LoadingDialog;

import java.util.ArrayList;

public class QsPanelTilePixel extends BaseActivity {

    LoadingDialog loadingDialog;
    RecyclerView container;
    QsShapeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qs_panel_tile_pixel);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_qs_shape);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // RecyclerView
        container = findViewById(R.id.qs_shapes_pixel_container);
        container.setLayoutManager(new LinearLayoutManager(this));
        adapter = initQsShapeItems();
        container.setAdapter(adapter);
        container.setHasFixedSize(true);
    }

    private QsShapeAdapter initQsShapeItems() {
        ArrayList<QsShapeModel> qsshape_list = new ArrayList<>();

        qsshape_list.add(new QsShapeModel("Default", R.drawable.qs_shape_default_enabled_pixel, R.drawable.qs_shape_default_disabled_pixel));
        qsshape_list.add(new QsShapeModel("Double Layer", R.drawable.qs_shape_doublelayer_enabled_pixel, R.drawable.qs_shape_doublelayer_disabled_pixel));
        qsshape_list.add(new QsShapeModel("Shaded Layer", R.drawable.qs_shape_shadedlayer_enabled_pixel, R.drawable.qs_shape_shadedlayer_disabled_pixel));
        qsshape_list.add(new QsShapeModel("Outline", R.drawable.qs_shape_outline_enabled_pixel, R.drawable.qs_shape_outline_disabled_pixel, true));
        qsshape_list.add(new QsShapeModel("Leafy Outline", R.drawable.qs_shape_leafy_outline_enabled_pixel, R.drawable.qs_shape_leafy_outline_disabled_pixel, true));
        qsshape_list.add(new QsShapeModel("Neumorph", R.drawable.qs_shape_neumorph_enabled_pixel, R.drawable.qs_shape_neumorph_disabled_pixel));
        qsshape_list.add(new QsShapeModel("Surround", R.drawable.qs_shape_surround_enabled_pixel, R.drawable.qs_shape_surround_disabled_pixel, 4, 22));
        qsshape_list.add(new QsShapeModel("Bookmark", R.drawable.qs_shape_bookmark_enabled_pixel, R.drawable.qs_shape_bookmark_disabled_pixel, 4, 26));
        qsshape_list.add(new QsShapeModel("Neumorph Outline", R.drawable.qs_shape_neumorph_outline_enabled_pixel, R.drawable.qs_shape_neumorph_outline_disabled_pixel, true));
        qsshape_list.add(new QsShapeModel("Reflected", R.drawable.qs_shape_reflected_enabled_pixel, R.drawable.qs_shape_reflected_disabled_pixel, true));
        qsshape_list.add(new QsShapeModel("Reflected Fill", R.drawable.qs_shape_reflected_fill_enabled_pixel, R.drawable.qs_shape_reflected_fill_disabled_pixel));
        qsshape_list.add(new QsShapeModel("Divided", R.drawable.qs_shape_divided_enabled_pixel, R.drawable.qs_shape_divided_disabled_pixel, 4, 22));
        qsshape_list.add(new QsShapeModel("Lighty", R.drawable.qs_shape_lighty_enabled_pixel, R.drawable.qs_shape_lighty_disabled_pixel, true));
        qsshape_list.add(new QsShapeModel("Bottom Outline", R.drawable.qs_shape_bottom_outline_enabled_pixel, R.drawable.qs_shape_bottom_outline_disabled_pixel));
        qsshape_list.add(new QsShapeModel("Cyberponk", R.drawable.qs_shape_cyberponk_enabled_pixel, R.drawable.qs_shape_cyberponk_disabled_pixel));
        qsshape_list.add(new QsShapeModel("Cyberponk v2", R.drawable.qs_shape_cyberponk_v2_enabled_pixel, R.drawable.qs_shape_cyberponk_v2_disabled_pixel, true));
        qsshape_list.add(new QsShapeModel("Semi Transparent", R.drawable.qs_shape_semi_transparent_enabled_pixel, R.drawable.qs_shape_semi_transparent_disabled_pixel, true));
        qsshape_list.add(new QsShapeModel("Thin Outline", R.drawable.qs_shape_thin_outline_enabled_pixel, R.drawable.qs_shape_thin_outline_disabled_pixel, true));
        qsshape_list.add(new QsShapeModel("Purfect", R.drawable.qs_shape_purfect_enabled_pixel, R.drawable.qs_shape_purfect_disabled_pixel));
        qsshape_list.add(new QsShapeModel("Translucent Outline", R.drawable.qs_shape_translucent_outline_enabled_pixel, R.drawable.qs_shape_translucent_outline_disabled_pixel, true));

        return new QsShapeAdapter(this, qsshape_list, loadingDialog, "QSSP");
    }

    // Change orientation in landscape / portrait mode
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}