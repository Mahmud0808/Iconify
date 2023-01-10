package com.drdisagree.iconify.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class Extras extends AppCompatActivity {

    LinearLayout extras_ui_roundness, extras_navbar;
    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extras);

        container = (ViewGroup) findViewById(R.id.extras_list);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_extras));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Extras page list items

        addItem(R.id.extras_ui_roundness, getResources().getString(R.string.activity_title_ui_roundness), getResources().getString(R.string.activity_desc_ui_roundness), R.drawable.ic_extras_roundness);
        addItem(R.id.extras_navbar, getResources().getString(R.string.activity_title_navigation_bar), getResources().getString(R.string.activity_desc_navigation_bar), R.drawable.ic_extras_navbar);

        // UI Roundness item onClick
        extras_ui_roundness = findViewById(R.id.extras_ui_roundness);
        extras_ui_roundness.setOnClickListener(v -> {
            Intent intent = new Intent(Extras.this, UiRoundness.class);
            startActivity(intent);
        });

        // Navigation Bar item onClick
        extras_navbar = findViewById(R.id.extras_navbar);
        extras_navbar.setOnClickListener(v -> {
            Intent intent = new Intent(Extras.this, NavigationBar.class);
            startActivity(intent);
        });
    }

    // Function to add new item in list
    private void addItem(int id, String title, String desc, int preview) {
        View list_view = LayoutInflater.from(this).inflate(R.layout.list_view, container, false);

        TextView list_title = (TextView) list_view.findViewById(R.id.list_title);
        TextView list_desc = (TextView) list_view.findViewById(R.id.list_desc);
        ImageView list_preview = (ImageView) list_view.findViewById(R.id.list_preview);

        list_view.setId(id);
        list_title.setText(title);
        list_desc.setText(desc);
        list_preview.setImageResource(preview);

        container.addView(list_view);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}