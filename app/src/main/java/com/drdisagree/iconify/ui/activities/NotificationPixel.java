package com.drdisagree.iconify.ui.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.adapters.NotificationAdapter;
import com.drdisagree.iconify.ui.models.NotificationModel;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.ui.views.LoadingDialog;

import java.util.ArrayList;

public class NotificationPixel extends BaseActivity {

    LoadingDialog loadingDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_pixel);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_notification);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // RecyclerView
        RecyclerView container = findViewById(R.id.notifications_pixel_container);
        container.setLayoutManager(new LinearLayoutManager(this));
        container.setAdapter(initNotifItems());
        container.setHasFixedSize(true);
    }

    private NotificationAdapter initNotifItems() {
        ArrayList<NotificationModel> notif_list = new ArrayList<>();

        notif_list.add(new NotificationModel("Default", R.drawable.notif_default));
        notif_list.add(new NotificationModel("Layers", R.drawable.notif_layers));
        notif_list.add(new NotificationModel("Thin Outline", R.drawable.notif_thin_outline));
        notif_list.add(new NotificationModel("Bottom Outline", R.drawable.notif_bottom_outline));
        notif_list.add(new NotificationModel("Neumorph", R.drawable.notif_neumorph));
        notif_list.add(new NotificationModel("Stack", R.drawable.notif_stack));
        notif_list.add(new NotificationModel("Side Stack", R.drawable.notif_side_stack));
        notif_list.add(new NotificationModel("Outline", R.drawable.notif_outline));
        notif_list.add(new NotificationModel("Leafy Outline", R.drawable.notif_leafy_outline));
        notif_list.add(new NotificationModel("Lighty", R.drawable.notif_lighty));
        notif_list.add(new NotificationModel("Neumorph Outline", R.drawable.notif_neumorph_outline));
        notif_list.add(new NotificationModel("Cyberponk", R.drawable.notif_cyberponk));
        notif_list.add(new NotificationModel("Cyberponk v2", R.drawable.notif_cyberponk_v2));
        notif_list.add(new NotificationModel("Thread Line", R.drawable.notif_thread_line));
        notif_list.add(new NotificationModel("Faded", R.drawable.notif_faded));
        notif_list.add(new NotificationModel("Dumbbell", R.drawable.notif_dumbbell));
        notif_list.add(new NotificationModel("Semi Transparent", R.drawable.notif_semi_transparent));
        notif_list.add(new NotificationModel("Pitch Black", R.drawable.notif_pitch_black));
        notif_list.add(new NotificationModel("Duoline", R.drawable.notif_duoline));
        notif_list.add(new NotificationModel("iOS", R.drawable.notif_ios));

        return new NotificationAdapter(this, notif_list, loadingDialog, "NFP");
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}