package com.drdisagree.iconify.ui.activities;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.adapters.MenuAdapter;
import com.drdisagree.iconify.ui.adapters.QsShapeAdapter;
import com.drdisagree.iconify.ui.adapters.ViewAdapter;
import com.drdisagree.iconify.ui.models.MenuModel;
import com.drdisagree.iconify.ui.models.QsShapeModel;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class QsPanelTile extends AppCompatActivity {

    LoadingDialog loadingDialog;
    RecyclerView container;
    ConcatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qs_panel_tile);

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

        // RecyclerView
        container = findViewById(R.id.qs_shapes_container);
        container.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConcatAdapter(initActivityItems(), new ViewAdapter(this, R.layout.view_divider), initQsShapeItems());
        container.setAdapter(adapter);
        container.setHasFixedSize(true);
    }

    private MenuAdapter initActivityItems() {
        ArrayList<MenuModel> qsshape_activity_list = new ArrayList<>();

        qsshape_activity_list.add(new MenuModel(QsRowColumn.class, getResources().getString(R.string.row_and_column_title), getResources().getString(R.string.row_and_column_desc), R.drawable.ic_qs_row_column));
        qsshape_activity_list.add(new MenuModel(QsIconLabel.class, getResources().getString(R.string.icon_and_label_title), getResources().getString(R.string.icon_and_label_desc), R.drawable.ic_qs_icon_and_label));
        qsshape_activity_list.add(new MenuModel(QsTileSize.class, getResources().getString(R.string.activity_title_qs_tile_size), getResources().getString(R.string.activity_desc_qs_tile_size), R.drawable.ic_qs_tile_size));
        qsshape_activity_list.add(new MenuModel(QsPanelTilePixel.class, getResources().getString(R.string.activity_title_pixel_variant), getResources().getString(R.string.activity_desc_pixel_variant), R.drawable.ic_pixel_device));

        return new MenuAdapter(this, qsshape_activity_list);
    }

    private QsShapeAdapter initQsShapeItems() {
        ArrayList<QsShapeModel> qsshape_list = new ArrayList<>();

        qsshape_list.add(new QsShapeModel("Default", R.drawable.qs_shape_default_enabled, R.drawable.qs_shape_default_disabled, false));
        qsshape_list.add(new QsShapeModel("Double Layer", R.drawable.qs_shape_doublelayer_enabled, R.drawable.qs_shape_doublelayer_disabled, false));
        qsshape_list.add(new QsShapeModel("Shaded Layer", R.drawable.qs_shape_shadedlayer_enabled, R.drawable.qs_shape_shadedlayer_disabled, false));
        qsshape_list.add(new QsShapeModel("Outline", R.drawable.qs_shape_outline_enabled, R.drawable.qs_shape_outline_disabled, true));
        qsshape_list.add(new QsShapeModel("Leafy Outline", R.drawable.qs_shape_leafy_outline_enabled, R.drawable.qs_shape_leafy_outline_disabled, true));
        qsshape_list.add(new QsShapeModel("Neumorph", R.drawable.qs_shape_neumorph_enabled, R.drawable.qs_shape_neumorph_disabled, false));
        qsshape_list.add(new QsShapeModel("Surround", R.drawable.qs_shape_surround_enabled, R.drawable.qs_shape_surround_disabled, false, 4, 22));
        qsshape_list.add(new QsShapeModel("Bookmark", R.drawable.qs_shape_bookmark_enabled, R.drawable.qs_shape_bookmark_disabled, false, 4, 26));
        qsshape_list.add(new QsShapeModel("Neumorph Outline", R.drawable.qs_shape_neumorph_outline_enabled, R.drawable.qs_shape_neumorph_outline_disabled, true));
        qsshape_list.add(new QsShapeModel("Reflected", R.drawable.qs_shape_reflected_enabled, R.drawable.qs_shape_reflected_disabled, true));
        qsshape_list.add(new QsShapeModel("Reflected Fill", R.drawable.qs_shape_reflected_fill_enabled, R.drawable.qs_shape_reflected_fill_disabled, false));
        qsshape_list.add(new QsShapeModel("Divided", R.drawable.qs_shape_divided_enabled, R.drawable.qs_shape_divided_disabled, false, 4, 22));
        qsshape_list.add(new QsShapeModel("Lighty", R.drawable.qs_shape_lighty_enabled, R.drawable.qs_shape_lighty_disabled, true));
        qsshape_list.add(new QsShapeModel("Bottom Outline", R.drawable.qs_shape_bottom_outline_enabled, R.drawable.qs_shape_bottom_outline_disabled, false));
        qsshape_list.add(new QsShapeModel("Cyberponk", R.drawable.qs_shape_cyberponk_enabled, R.drawable.qs_shape_cyberponk_disabled, false));
        qsshape_list.add(new QsShapeModel("Cyberponk v2", R.drawable.qs_shape_cyberponk_v2_enabled, R.drawable.qs_shape_cyberponk_v2_disabled, true));
        qsshape_list.add(new QsShapeModel("Semi Transparent", R.drawable.qs_shape_semi_transparent_enabled, R.drawable.qs_shape_semi_transparent_disabled, false));
        qsshape_list.add(new QsShapeModel("Thin Outline", R.drawable.qs_shape_thin_outline_enabled, R.drawable.qs_shape_thin_outline_disabled, true));
        qsshape_list.add(new QsShapeModel("Purfect", R.drawable.qs_shape_purfect_enabled, R.drawable.qs_shape_purfect_disabled, false));

        return new QsShapeAdapter(this, qsshape_list, loadingDialog, "QSSN");
    }

    // Change orientation in landscape / portrait mode
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}