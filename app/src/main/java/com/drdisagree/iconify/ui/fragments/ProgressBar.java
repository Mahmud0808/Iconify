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
import com.drdisagree.iconify.ui.adapters.ProgressBarAdapter;
import com.drdisagree.iconify.ui.models.ProgressBarModel;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class ProgressBar extends Fragment {

    LoadingDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress_bar, container, false);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = view.findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_progress_bar));
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
        RecyclerView listView = view.findViewById(R.id.progressbar_container);
        listView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        listView.setAdapter(initProgressBarItems());
        listView.setHasFixedSize(true);

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

        return new ProgressBarAdapter(requireActivity(), pgb_list, loadingDialog);
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}