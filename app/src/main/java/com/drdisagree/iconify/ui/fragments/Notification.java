package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.References.FRAGMENT_NOTIFICATIONPIXEL;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.adapters.MenuFragmentAdapter;
import com.drdisagree.iconify.ui.adapters.NotificationAdapter;
import com.drdisagree.iconify.ui.adapters.ViewAdapter;
import com.drdisagree.iconify.ui.models.MenuFragmentModel;
import com.drdisagree.iconify.ui.models.NotificationModel;
import com.drdisagree.iconify.ui.utils.FragmentHelper;
import com.drdisagree.iconify.ui.views.LoadingDialog;

import java.util.ArrayList;

public class Notification extends Fragment {

    LoadingDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        // Header
        FragmentHelper.initHeader((AppCompatActivity) requireActivity(), view, R.string.activity_title_notification, getParentFragmentManager());

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireActivity());

        // RecyclerView
        RecyclerView listView = view.findViewById(R.id.notifications_container);
        listView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        ConcatAdapter adapter = new ConcatAdapter(initActivityItems(), new ViewAdapter(requireActivity(), R.layout.view_divider), initNotifItems());
        listView.setAdapter(adapter);
        listView.setHasFixedSize(true);

        return view;
    }

    private MenuFragmentAdapter initActivityItems() {
        ArrayList<MenuFragmentModel> notif_activity_list = new ArrayList<>();

        notif_activity_list.add(new MenuFragmentModel(new NotificationPixel(), FRAGMENT_NOTIFICATIONPIXEL, getResources().getString(R.string.activity_title_pixel_variant), getResources().getString(R.string.activity_desc_pixel_variant), R.drawable.ic_pixel_device));

        return new MenuFragmentAdapter(requireActivity(), notif_activity_list, getParentFragmentManager());
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

        return new NotificationAdapter(requireActivity(), notif_list, loadingDialog, "NFN");
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}