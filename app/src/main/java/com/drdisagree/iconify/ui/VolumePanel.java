package com.drdisagree.iconify.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class VolumePanel extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.volume_panel);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("Volume Panel");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}