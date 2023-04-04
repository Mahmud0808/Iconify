package com.drdisagree.iconify.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;

public class Miscellaneous extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miscellaneous);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_miscellaneous);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}