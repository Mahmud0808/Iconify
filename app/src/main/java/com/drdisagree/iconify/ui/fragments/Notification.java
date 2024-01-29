package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Dynamic.isAtleastA14;
import static com.drdisagree.iconify.common.Preferences.SHOW_NOTIFICATION_NORMAL_WARN;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentNotificationBinding;
import com.drdisagree.iconify.ui.adapters.MenuAdapter;
import com.drdisagree.iconify.ui.adapters.NotificationAdapter;
import com.drdisagree.iconify.ui.adapters.SectionTitleAdapter;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.dialogs.LoadingDialog;
import com.drdisagree.iconify.ui.models.MenuModel;
import com.drdisagree.iconify.ui.models.NotificationModel;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class Notification extends BaseFragment {

    private FragmentNotificationBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_notification);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        // RecyclerView
        binding.notificationsContainer.setLayoutManager(new LinearLayoutManager(requireContext()));
        ConcatAdapter adapter = new ConcatAdapter(initActivityItems(), new SectionTitleAdapter(requireContext(), R.layout.view_section_title_notif, R.string.notification_styles), initNotifItems());
        binding.notificationsContainer.setAdapter(adapter);
        binding.notificationsContainer.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isAtleastA14 && Prefs.getBoolean(SHOW_NOTIFICATION_NORMAL_WARN, true)) {
            try {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.attention)
                        .setMessage(R.string.requires_lsposed_for_a14)
                        .setPositiveButton(requireContext().getResources().getString(R.string.understood), (dialog, which) -> dialog.dismiss())
                        .setNegativeButton(requireContext().getResources().getString(R.string.dont_show_again), (dialog, which) -> {
                            dialog.dismiss();
                            Prefs.putBoolean(SHOW_NOTIFICATION_NORMAL_WARN, false);
                        })
                        .setCancelable(true)
                        .show();
            } catch (Exception ignored) {
            }
        }
    }

    private MenuAdapter initActivityItems() {
        ArrayList<MenuModel> notif_activity_list = new ArrayList<>();

        notif_activity_list.add(new MenuModel(R.id.action_notification_to_notificationPixel, getResources().getString(R.string.activity_title_pixel_variant), getResources().getString(R.string.activity_desc_pixel_variant), R.drawable.ic_pixel_device));

        return new MenuAdapter(requireContext(), notif_activity_list);
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

        return new NotificationAdapter(requireContext(), notif_list, loadingDialog, "NFN");
    }

    @Override
    public void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
}