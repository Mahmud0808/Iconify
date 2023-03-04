package com.drdisagree.iconify.ui.activities;

import android.annotation.SuppressLint;
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

public class Notifications extends AppCompatActivity {

    LoadingDialog loadingDialog;
    private ViewGroup container_activity;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SystemUtil.isDarkMode()) {
            getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(Iconify.getAppContext(), R.color.offStateColor4));
            getWindow().setStatusBarColor(ContextCompat.getColor(Iconify.getAppContext(), R.color.offStateColor4));
            getWindow().setNavigationBarColor(ContextCompat.getColor(Iconify.getAppContext(), R.color.offStateColor4));
        }
        setContentView(R.layout.activity_notifications);
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

        // Activities list
        container_activity = findViewById(R.id.notification_list_activity);
        ArrayList<Object[]> notification_list_activity = new ArrayList<>();

        // Activities add items in list
        notification_list_activity.add(new Object[]{NotificationsPixel.class, getResources().getString(R.string.activity_title_pixel_variant), getResources().getString(R.string.activity_desc_pixel_variant), R.drawable.ic_pixel_device});

        addActivityItem(notification_list_activity);
        fixViewGroup(container_activity);

        // Enable onClick event
        for (int i = 0; i < notification_list_activity.size(); i++) {
            LinearLayout child = container_activity.getChildAt(i).findViewById(R.id.list_item);
            int finalI = i;
            child.setOnClickListener(v -> {
                Intent intent = new Intent(Notifications.this, (Class<?>) notification_list_activity.get(finalI)[0]);
                startActivity(intent);
            });
        }

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

        NotificationAdapter notificationAdapter = new NotificationAdapter(this, container_notif, notif_list, loadingDialog, "NFN");
        container_notif.setAdapter(notificationAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Function to add new item in list
    private void addActivityItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_list_menu, container_activity, false);

            TextView title = list.findViewById(R.id.list_title);
            title.setText((String) pack.get(i)[1]);

            TextView desc = list.findViewById(R.id.list_desc);
            desc.setText((String) pack.get(i)[2]);

            ImageView preview = list.findViewById(R.id.list_preview);
            preview.setImageResource((int) pack.get(i)[3]);

            container_activity.addView(list);
        }
    }

    private void fixViewGroup(ViewGroup viewGroup) {
        ((ViewGroup.MarginLayoutParams) viewGroup.getChildAt(viewGroup.getChildCount() - 1).getLayoutParams()).setMargins(0, 0, 0, 0);
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}