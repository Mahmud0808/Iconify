package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.HIDE_QSLABEL_SWITCH;
import static com.drdisagree.iconify.common.References.QSALPHA_LEVEL;
import static com.drdisagree.iconify.common.References.QSTRANSPARENCY_SWITCH;
import static com.drdisagree.iconify.common.References.STATUSBAR_CLOCKBG;
import static com.drdisagree.iconify.common.References.VERTICAL_QSTILE_SWITCH;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RemotePrefs;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class XPosedMenu extends AppCompatActivity {

    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_menu);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_xposed_menu));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Xposed menu list items
        container = findViewById(R.id.xposed_list);
        ArrayList<Object[]> xposed_menu = new ArrayList<>();

        xposed_menu.add(new Object[]{QuickSettings.class, getResources().getString(R.string.activity_title_quicksettings), getResources().getString(R.string.activity_desc_quicksettings), R.drawable.ic_xposed_quicksettings});
        xposed_menu.add(new Object[]{Lockscreen.class, getResources().getString(R.string.activity_title_lockscreen), getResources().getString(R.string.activity_desc_lockscreen), R.drawable.ic_xposed_lockscreen});

        addItem(xposed_menu);

        // Enable onClick event
        for (int i = 0; i < xposed_menu.size(); i++) {
            LinearLayout child = container.getChildAt(i).findViewById(R.id.list_item);
            int finalI = i;
            child.setOnClickListener(v -> {
                Intent intent = new Intent(XPosedMenu.this, (Class<?>) xposed_menu.get(finalI)[0]);
                startActivity(intent);
            });
        }
    }

    // Function to add new item in list
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.list_view, container, false);

            TextView title = list.findViewById(R.id.list_title);
            title.setText((String) pack.get(i)[1]);

            TextView desc = list.findViewById(R.id.list_desc);
            desc.setText((String) pack.get(i)[2]);

            ImageView preview = list.findViewById(R.id.list_preview);
            preview.setImageResource((int) pack.get(i)[3]);

            container.addView(list);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}