package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.References.FRAGMENT_QSICONLABEL;
import static com.drdisagree.iconify.common.References.FRAGMENT_QSPANELTILESPIXEL;
import static com.drdisagree.iconify.common.References.FRAGMENT_QSROWCOLUMN;
import static com.drdisagree.iconify.common.References.FRAGMENT_QSTILESIZE;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.adapters.MenuFragmentAdapter;
import com.drdisagree.iconify.ui.adapters.QsShapeAdapter;
import com.drdisagree.iconify.ui.adapters.ViewAdapter;
import com.drdisagree.iconify.ui.models.MenuFragmentModel;
import com.drdisagree.iconify.ui.models.QsShapeModel;
import com.drdisagree.iconify.ui.utils.FragmentHelper;
import com.drdisagree.iconify.ui.views.LoadingDialog;

import java.util.ArrayList;

public class QsPanelTiles extends Fragment {

    LoadingDialog loadingDialog;
    RecyclerView listView;
    ConcatAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qs_panel_tiles, container, false);

        // Header
        FragmentHelper.initHeader((AppCompatActivity) requireActivity(), view, R.string.activity_title_qs_shape, getParentFragmentManager());

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireActivity());

        // RecyclerView
        listView = view.findViewById(R.id.qs_shapes_container);
        listView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = new ConcatAdapter(initActivityItems(), new ViewAdapter(requireActivity(), R.layout.view_divider), initQsShapeItems());
        listView.setAdapter(adapter);
        listView.setHasFixedSize(true);

        return view;
    }

    private MenuFragmentAdapter initActivityItems() {
        ArrayList<MenuFragmentModel> qsshape_activity_list = new ArrayList<>();

        qsshape_activity_list.add(new MenuFragmentModel(new QsRowColumn(), FRAGMENT_QSROWCOLUMN, getResources().getString(R.string.row_and_column_title), getResources().getString(R.string.row_and_column_desc), R.drawable.ic_qs_row_column));
        qsshape_activity_list.add(new MenuFragmentModel(new QsIconLabel(), FRAGMENT_QSICONLABEL, getResources().getString(R.string.icon_and_label_title), getResources().getString(R.string.icon_and_label_desc), R.drawable.ic_qs_icon_and_label));
        qsshape_activity_list.add(new MenuFragmentModel(new QsTileSize(), FRAGMENT_QSTILESIZE, getResources().getString(R.string.activity_title_qs_tile_size), getResources().getString(R.string.activity_desc_qs_tile_size), R.drawable.ic_qs_tile_size));
        qsshape_activity_list.add(new MenuFragmentModel(new QsPanelTilesPixel(), FRAGMENT_QSPANELTILESPIXEL, getResources().getString(R.string.activity_title_pixel_variant), getResources().getString(R.string.activity_desc_pixel_variant), R.drawable.ic_pixel_device));

        return new MenuFragmentAdapter(requireActivity(), qsshape_activity_list, getParentFragmentManager());
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

        return new QsShapeAdapter(requireActivity(), qsshape_list, loadingDialog, "QSSN");
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