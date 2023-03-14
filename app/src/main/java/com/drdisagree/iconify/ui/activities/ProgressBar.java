package com.drdisagree.iconify.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.adapters.ProgressBarAdapter;
import com.drdisagree.iconify.ui.models.ProgressBarModel;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class ProgressBar extends AppCompatActivity {

    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_bar);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_progress_bar));
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.activity_title_progress_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home_as_up_indicator);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // RecyclerView
        RecyclerView container = findViewById(R.id.progressbar_container);
        container.setLayoutManager(new LinearLayoutManager(this));
        container.setAdapter(initProgressBarItems());
        container.setHasFixedSize(true);
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

        return new ProgressBarAdapter(this, pgb_list, loadingDialog);
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