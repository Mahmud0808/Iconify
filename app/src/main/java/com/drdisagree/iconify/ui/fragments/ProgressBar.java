package com.drdisagree.iconify.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.databinding.FragmentProgressBarBinding;
import com.drdisagree.iconify.ui.adapters.ProgressBarAdapter;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.dialogs.LoadingDialog;
import com.drdisagree.iconify.ui.models.ProgressBarModel;
import com.drdisagree.iconify.ui.utils.ViewHelper;

import java.util.ArrayList;

public class ProgressBar extends BaseFragment {

    private FragmentProgressBarBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProgressBarBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_progress_bar);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        // RecyclerView
        binding.progressbarContainer.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.progressbarContainer.setAdapter(initProgressBarItems());
        binding.progressbarContainer.setHasFixedSize(true);

        return view;
    }

    private ProgressBarAdapter initProgressBarItems() {
        ArrayList<ProgressBarModel> pgb_list = new ArrayList<>();

        pgb_list.add(new ProgressBarModel("Default", R.drawable.preview_seekbar_default));
        pgb_list.add(new ProgressBarModel("Divided", R.drawable.preview_seekbar_divided));
        pgb_list.add(new ProgressBarModel("Gradient Thumb", R.drawable.preview_seekbar_gradient_thumb));
        pgb_list.add(new ProgressBarModel("Minimal Thumb", R.drawable.preview_seekbar_minimal_thumb));
        pgb_list.add(new ProgressBarModel("Blocky Thumb", R.drawable.preview_seekbar_blocky_thumb));
        pgb_list.add(new ProgressBarModel("Outline Thumb", R.drawable.preview_seekbar_outline_thumb));
        pgb_list.add(new ProgressBarModel("Oldschool Thumb", R.drawable.preview_seekbar_oldschool_thumb));
        pgb_list.add(new ProgressBarModel("No Thumb", R.drawable.preview_seekbar_no_thumb));
        pgb_list.add(new ProgressBarModel("Thin Track", R.drawable.preview_seekbar_thin_track));
        pgb_list.add(new ProgressBarModel("Inline", R.drawable.preview_seekbar_inline));
        pgb_list.add(new ProgressBarModel("Lighty", R.drawable.preview_seekbar_lighty));

        return new ProgressBarAdapter(requireContext(), pgb_list, loadingDialog);
    }

    @Override
    public void onDestroy() {
        loadingDialog.dismiss();
        super.onDestroy();
    }
}