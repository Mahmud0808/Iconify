package com.drdisagree.iconify.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.adapters.ViewAdapter;
import com.drdisagree.iconify.ui.adapters.IconPackAdapter;
import com.drdisagree.iconify.ui.adapters.MenuAdapter;
import com.drdisagree.iconify.ui.models.IconPackModel;
import com.drdisagree.iconify.ui.models.MenuModel;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class IconPack extends AppCompatActivity {

    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_pack);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_icon_pack));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // RecyclerView
        RecyclerView container = findViewById(R.id.icon_pack_container);
        container.setLayoutManager(new LinearLayoutManager(this));
        ConcatAdapter adapter = new ConcatAdapter(initActivityItems(), new ViewAdapter(this, R.layout.view_divider), initIconPackItems());
        container.setAdapter(adapter);
        container.setHasFixedSize(true);
    }

    private MenuAdapter initActivityItems() {
        ArrayList<MenuModel> iconpack_activity_list = new ArrayList<>();

        iconpack_activity_list.add(new MenuModel(ColoredBattery.class, getResources().getString(R.string.activity_title_colored_battery), getResources().getString(R.string.activity_desc_colored_battery), R.drawable.ic_colored_battery));
        iconpack_activity_list.add(new MenuModel(SettingsIcons.class, getResources().getString(R.string.activity_title_settings_icons), getResources().getString(R.string.activity_desc_settings_icons), R.drawable.ic_settings_icon_pack));

        return new MenuAdapter(this, iconpack_activity_list);
    }

    private IconPackAdapter initIconPackItems() {
        ArrayList<IconPackModel> iconpack_list = new ArrayList<>();

        iconpack_list.add(new IconPackModel("Aurora", "Dual tone linear icon pack", R.drawable.preview_aurora_wifi, R.drawable.preview_aurora_signal, R.drawable.preview_aurora_airplane, R.drawable.preview_aurora_location));
        iconpack_list.add(new IconPackModel("Gradicon", "Gradient shaded filled icon pack", R.drawable.preview_gradicon_wifi, R.drawable.preview_gradicon_signal, R.drawable.preview_gradicon_airplane, R.drawable.preview_gradicon_location));
        iconpack_list.add(new IconPackModel("Lorn", "Thick linear icon pack", R.drawable.preview_lorn_wifi, R.drawable.preview_lorn_signal, R.drawable.preview_lorn_airplane, R.drawable.preview_lorn_location));
        iconpack_list.add(new IconPackModel("Plumpy", "Dual tone filled icon pack", R.drawable.preview_plumpy_wifi, R.drawable.preview_plumpy_signal, R.drawable.preview_plumpy_airplane, R.drawable.preview_plumpy_location));
        iconpack_list.add(new IconPackModel("Acherus", "Acherus sub icon pack", R.drawable.preview_acherus_wifi, R.drawable.preview_acherus_signal, R.drawable.preview_acherus_airplane, R.drawable.preview_acherus_location));
        iconpack_list.add(new IconPackModel("Circular", "Thin line icon pack", R.drawable.preview_circular_wifi, R.drawable.preview_circular_signal, R.drawable.preview_circular_airplane, R.drawable.preview_circular_location));
        iconpack_list.add(new IconPackModel("Filled", "Dual tone filled icon pack", R.drawable.preview_filled_wifi, R.drawable.preview_filled_signal, R.drawable.preview_filled_airplane, R.drawable.preview_filled_location));
        iconpack_list.add(new IconPackModel("Kai", "Thin line icon pack", R.drawable.preview_kai_wifi, R.drawable.preview_kai_signal, R.drawable.preview_kai_airplane, R.drawable.preview_kai_location));
        iconpack_list.add(new IconPackModel("OOS", "Oxygen OS icon pack", R.drawable.preview_oos_wifi, R.drawable.preview_oos_signal, R.drawable.preview_oos_airplane, R.drawable.preview_oos_location));
        iconpack_list.add(new IconPackModel("Outline", "Thin outline icon pack", R.drawable.preview_outline_wifi, R.drawable.preview_outline_signal, R.drawable.preview_outline_airplane, R.drawable.preview_outline_location));
        iconpack_list.add(new IconPackModel("PUI", "Thick dualtone icon pack", R.drawable.preview_pui_wifi, R.drawable.preview_pui_signal, R.drawable.preview_pui_airplane, R.drawable.preview_pui_location));
        iconpack_list.add(new IconPackModel("Rounded", "Rounded corner icon pack", R.drawable.preview_rounded_wifi, R.drawable.preview_rounded_signal, R.drawable.preview_rounded_airplane, R.drawable.preview_rounded_location));
        iconpack_list.add(new IconPackModel("Sam", "Filled icon pack", R.drawable.preview_sam_wifi, R.drawable.preview_sam_signal, R.drawable.preview_sam_airplane, R.drawable.preview_sam_location));
        iconpack_list.add(new IconPackModel("Victor", "Edgy icon pack", R.drawable.preview_victor_wifi, R.drawable.preview_victor_signal, R.drawable.preview_victor_airplane, R.drawable.preview_victor_location));

        return new IconPackAdapter(this, iconpack_list, loadingDialog);
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