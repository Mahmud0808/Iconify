package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.FRAGMENT_BACK_BUTTON_DELAY;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.adapters.SwitchAdapter;
import com.drdisagree.iconify.ui.models.SwitchModel;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class Switch extends Fragment {

    LoadingDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_switch, container, false);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = view.findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_switch));
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(view1 -> new Handler().postDelayed(() -> {
            getParentFragmentManager().popBackStack();
        }, FRAGMENT_BACK_BUTTON_DELAY));

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireActivity());

        // RecyclerView
        RecyclerView listView = view.findViewById(R.id.switch_container);
        listView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        listView.setAdapter(initSwitchItems());
        listView.setHasFixedSize(true);

        return view;
    }

    private SwitchAdapter initSwitchItems() {
        ArrayList<SwitchModel> switch_list = new ArrayList<>();

        switch_list.add(new SwitchModel("Minimal Switch", R.drawable.switch_minimal_track, R.drawable.switch_minimal_thumb));
        switch_list.add(new SwitchModel("Material Switch", R.drawable.switch_material_track, R.drawable.switch_material_thumb));
        switch_list.add(new SwitchModel("Realme Switch", R.drawable.switch_realme_track, R.drawable.switch_realme_thumb));
        switch_list.add(new SwitchModel("iOS Switch", R.drawable.switch_ios_track, R.drawable.switch_ios_thumb));
        switch_list.add(new SwitchModel("Outline Switch", R.drawable.switch_outline_track, R.drawable.switch_outline_thumb));
        switch_list.add(new SwitchModel("Neumorph Switch", R.drawable.switch_neumorph_track, R.drawable.switch_neumorph_thumb));

        return new SwitchAdapter(requireActivity(), switch_list, loadingDialog);
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}