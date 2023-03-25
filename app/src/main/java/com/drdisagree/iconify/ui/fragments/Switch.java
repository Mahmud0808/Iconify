package com.drdisagree.iconify.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.adapters.SwitchAdapter;
import com.drdisagree.iconify.ui.models.SwitchModel;
import com.drdisagree.iconify.ui.utils.FragmentHelper;
import com.drdisagree.iconify.ui.views.LoadingDialog;

import java.util.ArrayList;

public class Switch extends Fragment {

    LoadingDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_switch, container, false);

        // Header
        FragmentHelper.initHeader((AppCompatActivity) requireActivity(), view, R.string.activity_title_switch, getParentFragmentManager());

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