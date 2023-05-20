package com.drdisagree.iconify.ui.activities;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.adapters.SwitchAdapter;
import com.drdisagree.iconify.ui.models.SwitchModel;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.ui.views.LoadingDialog;

import java.util.ArrayList;

public class Switch extends BaseActivity {

    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_switch);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // RecyclerView
        RecyclerView container = findViewById(R.id.switch_container);
        container.setLayoutManager(new LinearLayoutManager(this));
        container.setAdapter(initSwitchItems());
        container.setHasFixedSize(true);
    }

    private SwitchAdapter initSwitchItems() {
        ArrayList<SwitchModel> switch_list = new ArrayList<>();

        switch_list.add(new SwitchModel("Minimal Switch", R.drawable.switch_minimal_track, R.drawable.switch_minimal_thumb));
        switch_list.add(new SwitchModel("Material Switch", R.drawable.switch_material_track, R.drawable.switch_material_thumb));
        switch_list.add(new SwitchModel("Realme Switch", R.drawable.switch_realme_track, R.drawable.switch_realme_thumb));
        switch_list.add(new SwitchModel("iOS Switch", R.drawable.switch_ios_track, R.drawable.switch_ios_thumb));
        switch_list.add(new SwitchModel("Outline Switch", R.drawable.switch_outline_track, R.drawable.switch_outline_thumb));
        switch_list.add(new SwitchModel("Neumorph Switch", R.drawable.switch_neumorph_track, R.drawable.switch_neumorph_thumb));
        switch_list.add(new SwitchModel("Emoji Switch", R.drawable.switch_emoji_track, R.drawable.switch_emoji_thumb));
        switch_list.add(new SwitchModel("Tiny Switch", R.drawable.switch_tiny_track, R.drawable.switch_tiny_thumb));
        switch_list.add(new SwitchModel("Shaded Switch", R.drawable.switch_shaded_track, R.drawable.switch_shaded_thumb));
        switch_list.add(new SwitchModel("Foggy Switch", R.drawable.switch_foggy_track, R.drawable.switch_foggy_thumb));
        switch_list.add(new SwitchModel("Checkmark Switch", R.drawable.switch_checkmark_track, R.drawable.switch_checkmark_thumb));

        return new SwitchAdapter(this, switch_list, loadingDialog);
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}