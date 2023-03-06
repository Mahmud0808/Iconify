package com.drdisagree.iconify.ui.activities;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.adapters.MenuAdapter;
import com.drdisagree.iconify.ui.models.MenuModel;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class Extras extends AppCompatActivity {

    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extras);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_extras));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // RecyclerView
        RecyclerView container = findViewById(R.id.extras_container);
        container.setLayoutManager(new LinearLayoutManager(this));
        container.setAdapter(initActivityItems());
        container.setHasFixedSize(true);
    }

    private MenuAdapter initActivityItems() {
        ArrayList<MenuModel> extras_list = new ArrayList<>();

        extras_list.add(new MenuModel(UiRoundness.class, getResources().getString(R.string.activity_title_ui_roundness), getResources().getString(R.string.activity_desc_ui_roundness), R.drawable.ic_extras_roundness));
        extras_list.add(new MenuModel(Statusbar.class, getResources().getString(R.string.activity_title_statusbar), getResources().getString(R.string.activity_desc_statusbar), R.drawable.ic_statusbar));
        extras_list.add(new MenuModel(NavigationBar.class, getResources().getString(R.string.activity_title_navigation_bar), getResources().getString(R.string.activity_desc_navigation_bar), R.drawable.ic_extras_navbar));
        extras_list.add(new MenuModel(ToastFrame.class, getResources().getString(R.string.activity_title_toast_frame), getResources().getString(R.string.activity_desc_toast_frame), R.drawable.ic_toast_frame));
        extras_list.add(new MenuModel(IconShape.class, getResources().getString(R.string.activity_title_icon_shape), getResources().getString(R.string.activity_desc_icon_shape), R.drawable.ic_icon_mask));
        extras_list.add(new MenuModel(XPosedMenu.class, getResources().getString(R.string.activity_title_xposed_menu), getResources().getString(R.string.activity_desc_xposed_menu), R.drawable.ic_extras_xposed_menu));
        //extras_page.add(new MenuModel(Miscellaneous.class, getResources().getString(R.string.activity_title_miscellaneous), getResources().getString(R.string.activity_desc_miscellaneous), R.drawable.ic_extras_miscellaneous));

        return new MenuAdapter(this, extras_list);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}