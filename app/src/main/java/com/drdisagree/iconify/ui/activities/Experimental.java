package com.drdisagree.iconify.ui.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;

public class Experimental extends BaseActivity {

    @SuppressLint({"SetTextI18n", "CommitPrefEdits"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experimental);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_experimental);
    }
}