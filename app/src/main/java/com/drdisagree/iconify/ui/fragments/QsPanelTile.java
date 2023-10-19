package com.drdisagree.iconify.ui.fragments;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.databinding.FragmentQsPanelTileBinding;
import com.drdisagree.iconify.ui.adapters.MenuAdapter;
import com.drdisagree.iconify.ui.adapters.QsShapeAdapter;
import com.drdisagree.iconify.ui.adapters.SectionTitleAdapter;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.dialogs.LoadingDialog;
import com.drdisagree.iconify.ui.models.MenuModel;
import com.drdisagree.iconify.ui.models.QsShapeModel;
import com.drdisagree.iconify.ui.utils.ViewHelper;

import java.util.ArrayList;

public class QsPanelTile extends BaseFragment {

    private FragmentQsPanelTileBinding binding;
    private LoadingDialog loadingDialog;
    private ConcatAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQsPanelTileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_qs_shape);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        // RecyclerView
        binding.qsShapesContainer.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ConcatAdapter(initActivityItems(), new SectionTitleAdapter(requireContext(), R.layout.view_section_title, R.string.qspanel_tile_styles), initQsShapeItems());
        binding.qsShapesContainer.setAdapter(adapter);
        binding.qsShapesContainer.setHasFixedSize(true);

        return view;
    }

    private MenuAdapter initActivityItems() {
        ArrayList<MenuModel> qsshape_activity_list = new ArrayList<>();

        qsshape_activity_list.add(new MenuModel(R.id.action_qsPanelTile_to_qsPanelTilePixel, getResources().getString(R.string.activity_title_pixel_variant), getResources().getString(R.string.activity_desc_pixel_variant), R.drawable.ic_pixel_device));

        return new MenuAdapter(requireContext(), qsshape_activity_list);
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
        qsshape_list.add(new QsShapeModel("Translucent Outline", R.drawable.qs_shape_translucent_outline_enabled, R.drawable.qs_shape_translucent_outline_disabled, true));

        return new QsShapeAdapter(requireContext(), qsshape_list, loadingDialog, "QSSN");
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
        loadingDialog.dismiss();
        super.onDestroy();
    }
}