package com.drdisagree.iconify.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.databinding.FragmentSwitchBinding;
import com.drdisagree.iconify.ui.adapters.SwitchAdapter;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.dialogs.LoadingDialog;
import com.drdisagree.iconify.ui.models.SwitchModel;
import com.drdisagree.iconify.ui.utils.ViewHelper;

import java.util.ArrayList;

public class Switch extends BaseFragment {

    private FragmentSwitchBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSwitchBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_switch);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        // RecyclerView
        binding.switchContainer.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.switchContainer.setAdapter(initSwitchItems());
        binding.switchContainer.setHasFixedSize(true);

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
        switch_list.add(new SwitchModel("Emoji Switch", R.drawable.switch_emoji_track, R.drawable.switch_emoji_thumb));
        switch_list.add(new SwitchModel("Tiny Switch", R.drawable.switch_tiny_track, R.drawable.switch_tiny_thumb));
        switch_list.add(new SwitchModel("Shaded Switch", R.drawable.switch_shaded_track, R.drawable.switch_shaded_thumb));
        switch_list.add(new SwitchModel("Foggy Switch", R.drawable.switch_foggy_track, R.drawable.switch_foggy_thumb));
        switch_list.add(new SwitchModel("Checkmark Switch", R.drawable.switch_checkmark_track, R.drawable.switch_checkmark_thumb));

        return new SwitchAdapter(requireContext(), switch_list, loadingDialog);
    }

    @Override
    public void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
}