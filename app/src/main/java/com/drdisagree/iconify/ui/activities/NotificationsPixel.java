package com.drdisagree.iconify.ui.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.adapters.NotificationAdapter;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class NotificationsPixel extends AppCompatActivity {

    LoadingDialog loadingDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SystemUtil.isDarkMode()) {
            getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(Iconify.getAppContext(), R.color.offStateColor4));
            getWindow().setStatusBarColor(ContextCompat.getColor(Iconify.getAppContext(), R.color.offStateColor4));
            getWindow().setNavigationBarColor(ContextCompat.getColor(Iconify.getAppContext(), R.color.offStateColor4));
        }
        setContentView(R.layout.activity_notifications_pixel);
        if (!SystemUtil.isDarkMode())
            findViewById(R.id.collapsing_toolbar).setBackgroundColor(ContextCompat.getColor(Iconify.getAppContext(), R.color.offStateColor4));

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_notification));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // Notifications list holder
        RecyclerView container_notif = findViewById(R.id.notification_list);
        container_notif.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<Object[]> notif_list = new ArrayList<>();

        // Notifications items
        notif_list.add(new Object[]{"Default", R.drawable.notif_default});
        notif_list.add(new Object[]{"Layers", R.drawable.notif_layers});
        notif_list.add(new Object[]{"Thin Outline", R.drawable.notif_thin_outline});
        notif_list.add(new Object[]{"Bottom Outline", R.drawable.notif_bottom_outline});
        notif_list.add(new Object[]{"Neumorph", R.drawable.notif_neumorph});
        notif_list.add(new Object[]{"Stack", R.drawable.notif_stack});
        notif_list.add(new Object[]{"Side Stack", R.drawable.notif_side_stack});
        notif_list.add(new Object[]{"Outline", R.drawable.notif_outline});
        notif_list.add(new Object[]{"Leafy Outline", R.drawable.notif_leafy_outline});
        notif_list.add(new Object[]{"Lighty", R.drawable.notif_lighty});
        notif_list.add(new Object[]{"Neumorph Outline", R.drawable.notif_neumorph_outline});
        notif_list.add(new Object[]{"Cyberponk", R.drawable.notif_cyberponk});
        notif_list.add(new Object[]{"Cyberponk v2", R.drawable.notif_cyberponk_v2});
        notif_list.add(new Object[]{"Thread Line", R.drawable.notif_thread_line});
        notif_list.add(new Object[]{"Faded", R.drawable.notif_faded});
        notif_list.add(new Object[]{"Dumbbell", R.drawable.notif_dumbbell});
        notif_list.add(new Object[]{"Semi Transparent", R.drawable.notif_semi_transparent});
        notif_list.add(new Object[]{"Pitch Black", R.drawable.notif_pitch_black});
        notif_list.add(new Object[]{"Duoline", R.drawable.notif_duoline});

        NotificationAdapter notificationAdapter = new NotificationAdapter(this, notif_list, loadingDialog, "NFP");
        container_notif.setAdapter(notificationAdapter);
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