package com.drdisagree.iconify.ui.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.databinding.ActivityExperimentalBinding;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;

public class Experimental extends BaseActivity {

    private ActivityExperimentalBinding binding;

    @SuppressLint({"SetTextI18n", "CommitPrefEdits"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExperimentalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.collapsingToolbar, binding.header.toolbar, R.string.activity_title_experimental);
    }
}