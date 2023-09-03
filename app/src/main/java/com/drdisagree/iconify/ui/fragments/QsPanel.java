package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.FRAGMENT_BACK_BUTTON_DELAY;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.databinding.FragmentQsPanelBinding;
import com.drdisagree.iconify.ui.activities.QsIconLabel;
import com.drdisagree.iconify.ui.activities.QsPanelMargin;
import com.drdisagree.iconify.ui.activities.QsRowColumn;
import com.drdisagree.iconify.ui.activities.QsTileSize;
import com.drdisagree.iconify.ui.adapters.MenuAdapter;
import com.drdisagree.iconify.ui.models.MenuModel;

import java.util.ArrayList;
import java.util.Objects;

public class QsPanel extends BaseFragment {

    private FragmentQsPanelBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQsPanelBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        binding.header.toolbar.setTitle(getResources().getString(R.string.activity_title_qs_panel));
        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayShowHomeEnabled(true);
        binding.header.toolbar.setNavigationOnClickListener(view1 -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            getParentFragmentManager().popBackStack();
        }, FRAGMENT_BACK_BUTTON_DELAY));

        // RecyclerView
        binding.qsPanelContainer.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.qsPanelContainer.setAdapter(initActivityItems());
        binding.qsPanelContainer.setHasFixedSize(true);

        return view;
    }

    private MenuAdapter initActivityItems() {
        ArrayList<MenuModel> qspanel_activity_list = new ArrayList<>();

        qspanel_activity_list.add(new MenuModel(QsRowColumn.class, getResources().getString(R.string.row_and_column_title), getResources().getString(R.string.row_and_column_desc), R.drawable.ic_qs_row_column));
        qspanel_activity_list.add(new MenuModel(QsIconLabel.class, getResources().getString(R.string.icon_and_label_title), getResources().getString(R.string.icon_and_label_desc), R.drawable.ic_qs_icon_and_label));
        qspanel_activity_list.add(new MenuModel(QsTileSize.class, getResources().getString(R.string.activity_title_qs_tile_size), getResources().getString(R.string.activity_desc_qs_tile_size), R.drawable.ic_qs_tile_size));
        qspanel_activity_list.add(new MenuModel(QsPanelMargin.class, getResources().getString(R.string.activity_title_qs_panel_margin), getResources().getString(R.string.activity_desc_qs_panel_margin), R.drawable.ic_qs_top_margin));

        return new MenuAdapter(requireActivity(), qspanel_activity_list);
    }
}