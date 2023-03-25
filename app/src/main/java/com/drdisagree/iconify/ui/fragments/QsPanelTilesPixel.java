package com.drdisagree.iconify.ui.fragments;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.adapters.QsShapeAdapter;
import com.drdisagree.iconify.ui.models.QsShapeModel;
import com.drdisagree.iconify.ui.utils.FragmentHelper;
import com.drdisagree.iconify.ui.views.LoadingDialog;

import java.util.ArrayList;

public class QsPanelTilesPixel extends Fragment {

    LoadingDialog loadingDialog;
    RecyclerView listView;
    QsShapeAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qs_panel_tiles_pixel, container, false);

        // Header
        FragmentHelper.initHeader((AppCompatActivity) requireActivity(), view, R.string.activity_title_qs_shape, getParentFragmentManager());

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireActivity());

        // RecyclerView
        listView = view.findViewById(R.id.qs_shapes_pixel_container);
        listView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = initQsShapeItems();
        listView.setAdapter(adapter);
        listView.setHasFixedSize(true);

        return view;
    }

    private QsShapeAdapter initQsShapeItems() {
        ArrayList<QsShapeModel> qsshape_list = new ArrayList<>();

        qsshape_list.add(new QsShapeModel("Default", R.drawable.qs_shape_default_enabled_pixel, R.drawable.qs_shape_default_disabled_pixel, false));
        qsshape_list.add(new QsShapeModel("Double Layer", R.drawable.qs_shape_doublelayer_enabled_pixel, R.drawable.qs_shape_doublelayer_disabled_pixel, false));
        qsshape_list.add(new QsShapeModel("Shaded Layer", R.drawable.qs_shape_shadedlayer_enabled_pixel, R.drawable.qs_shape_shadedlayer_disabled_pixel, false));
        qsshape_list.add(new QsShapeModel("Outline", R.drawable.qs_shape_outline_enabled_pixel, R.drawable.qs_shape_outline_disabled_pixel, true));
        qsshape_list.add(new QsShapeModel("Leafy Outline", R.drawable.qs_shape_leafy_outline_enabled_pixel, R.drawable.qs_shape_leafy_outline_disabled_pixel, true));
        qsshape_list.add(new QsShapeModel("Neumorph", R.drawable.qs_shape_neumorph_enabled_pixel, R.drawable.qs_shape_neumorph_disabled_pixel, false));
        qsshape_list.add(new QsShapeModel("Surround", R.drawable.qs_shape_surround_enabled_pixel, R.drawable.qs_shape_surround_disabled_pixel, false, 4, 22));
        qsshape_list.add(new QsShapeModel("Bookmark", R.drawable.qs_shape_bookmark_enabled_pixel, R.drawable.qs_shape_bookmark_disabled_pixel, false, 4, 26));
        qsshape_list.add(new QsShapeModel("Neumorph Outline", R.drawable.qs_shape_neumorph_outline_enabled_pixel, R.drawable.qs_shape_neumorph_outline_disabled_pixel, true));
        qsshape_list.add(new QsShapeModel("Reflected", R.drawable.qs_shape_reflected_enabled_pixel, R.drawable.qs_shape_reflected_disabled_pixel, true));
        qsshape_list.add(new QsShapeModel("Reflected Fill", R.drawable.qs_shape_reflected_fill_enabled_pixel, R.drawable.qs_shape_reflected_fill_disabled_pixel, false));
        qsshape_list.add(new QsShapeModel("Divided", R.drawable.qs_shape_divided_enabled_pixel, R.drawable.qs_shape_divided_disabled_pixel, false, 4, 22));
        qsshape_list.add(new QsShapeModel("Lighty", R.drawable.qs_shape_lighty_enabled_pixel, R.drawable.qs_shape_lighty_disabled_pixel, true));
        qsshape_list.add(new QsShapeModel("Bottom Outline", R.drawable.qs_shape_bottom_outline_enabled_pixel, R.drawable.qs_shape_bottom_outline_disabled_pixel, false));
        qsshape_list.add(new QsShapeModel("Cyberponk", R.drawable.qs_shape_cyberponk_enabled_pixel, R.drawable.qs_shape_cyberponk_disabled_pixel, false));
        qsshape_list.add(new QsShapeModel("Cyberponk v2", R.drawable.qs_shape_cyberponk_v2_enabled_pixel, R.drawable.qs_shape_cyberponk_v2_disabled_pixel, true));
        qsshape_list.add(new QsShapeModel("Semi Transparent", R.drawable.qs_shape_semi_transparent_enabled_pixel, R.drawable.qs_shape_semi_transparent_disabled_pixel, false));
        qsshape_list.add(new QsShapeModel("Thin Outline", R.drawable.qs_shape_thin_outline_enabled_pixel, R.drawable.qs_shape_thin_outline_disabled_pixel, true));
        qsshape_list.add(new QsShapeModel("Purfect", R.drawable.qs_shape_purfect_enabled_pixel, R.drawable.qs_shape_purfect_disabled_pixel, false));

        return new QsShapeAdapter(requireActivity(), qsshape_list, loadingDialog, "QSSP");
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