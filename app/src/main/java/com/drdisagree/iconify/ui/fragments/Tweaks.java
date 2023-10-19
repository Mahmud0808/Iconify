package com.drdisagree.iconify.ui.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.databinding.FragmentTweaksBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.AppUtil;

import java.util.ArrayList;
import java.util.Objects;

public class Tweaks extends BaseFragment {

    private FragmentTweaksBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTweaksBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.navbar_tweaks);

        ArrayList<Object[]> tweaks_list = new ArrayList<>();

        tweaks_list.add(new Object[]{R.id.action_tweaks_to_colorEngine, Iconify.getAppContext().getResources().getString(R.string.activity_title_color_engine), Iconify.getAppContext().getResources().getString(R.string.activity_desc_color_engine), R.drawable.ic_tweaks_color});
        tweaks_list.add(new Object[]{R.id.action_tweaks_to_uiRoundness, Iconify.getAppContext().getResources().getString(R.string.activity_title_ui_roundness), Iconify.getAppContext().getResources().getString(R.string.activity_desc_ui_roundness), R.drawable.ic_tweaks_roundness});
        tweaks_list.add(new Object[]{R.id.action_tweaks_to_qsRowColumn, Iconify.getAppContext().getResources().getString(R.string.activity_title_qs_row_column), Iconify.getAppContext().getResources().getString(R.string.activity_desc_qs_row_column), R.drawable.ic_qs_row_column});
        tweaks_list.add(new Object[]{R.id.action_tweaks_to_qsIconLabel, Iconify.getAppContext().getResources().getString(R.string.activity_title_qs_icon_label), Iconify.getAppContext().getResources().getString(R.string.activity_desc_qs_icon_label), R.drawable.ic_qs_icon_and_label});
        tweaks_list.add(new Object[]{R.id.action_tweaks_to_qsTileSize, Iconify.getAppContext().getResources().getString(R.string.activity_title_qs_tile_size), Iconify.getAppContext().getResources().getString(R.string.activity_desc_qs_tile_size), R.drawable.ic_qs_tile_size});
        tweaks_list.add(new Object[]{R.id.action_tweaks_to_qsPanelMargin, Iconify.getAppContext().getResources().getString(R.string.activity_title_qs_panel_margin), Iconify.getAppContext().getResources().getString(R.string.activity_desc_qs_panel_margin), R.drawable.ic_qs_top_margin});
        tweaks_list.add(new Object[]{R.id.action_tweaks_to_statusbar, Iconify.getAppContext().getResources().getString(R.string.activity_title_statusbar), Iconify.getAppContext().getResources().getString(R.string.activity_desc_statusbar), R.drawable.ic_tweaks_statusbar});
        tweaks_list.add(new Object[]{R.id.action_tweaks_to_navigationBar, Iconify.getAppContext().getResources().getString(R.string.activity_title_navigation_bar), Iconify.getAppContext().getResources().getString(R.string.activity_desc_navigation_bar), R.drawable.ic_tweaks_navbar});
        tweaks_list.add(new Object[]{R.id.action_tweaks_to_mediaPlayer, Iconify.getAppContext().getResources().getString(R.string.activity_title_media_player), Iconify.getAppContext().getResources().getString(R.string.activity_desc_media_player), R.drawable.ic_tweaks_media});
        tweaks_list.add(new Object[]{R.id.action_tweaks_to_volumePanel, Iconify.getAppContext().getResources().getString(R.string.activity_title_volume_panel), Iconify.getAppContext().getResources().getString(R.string.activity_desc_volume_panel), R.drawable.ic_tweaks_volume});
        tweaks_list.add(new Object[]{R.id.action_tweaks_to_miscellaneous, Iconify.getAppContext().getResources().getString(R.string.activity_title_miscellaneous), Iconify.getAppContext().getResources().getString(R.string.activity_desc_miscellaneous), R.drawable.ic_tweaks_miscellaneous});
        tweaks_list.add(new Object[]{(View.OnClickListener) v -> {
            // Check if LSPosed is installed or not
            if (!AppUtil.isLsposedInstalled()) {
                Toast.makeText(Iconify.getAppContext(), Iconify.getAppContext().getResources().getString(R.string.toast_lsposed_not_found), Toast.LENGTH_SHORT).show();
                return;
            }

            Navigation.findNavController(view).navigate(R.id.action_tweaks_to_nav_xposed_menu);
        }, Iconify.getAppContext().getResources().getString(R.string.activity_title_xposed_menu), Iconify.getAppContext().getResources().getString(R.string.activity_desc_xposed_menu), R.drawable.ic_tweaks_xposed_menu});

        addItem(tweaks_list);

        return view;
    }

    // Function to add new item in list
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(requireActivity()).inflate(R.layout.view_list_menu, binding.tweaksList, false);

            TextView title = list.findViewById(R.id.list_title);
            title.setText((String) pack.get(i)[1]);

            TextView desc = list.findViewById(R.id.list_desc);
            desc.setText((String) pack.get(i)[2]);

            ImageView preview = list.findViewById(R.id.list_icon);
            preview.setImageResource((int) pack.get(i)[3]);

            if (pack.get(i)[0] instanceof View.OnClickListener) {
                list.setOnClickListener((View.OnClickListener) pack.get(i)[0]);
            } else if (pack.get(i)[0] instanceof Integer) {
                int finalI = i;
                list.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).navigate((Integer) pack.get(finalI)[0]));
            }

            if (Objects.equals(pack.get(i)[1], Iconify.getAppContext().getResources().getString(R.string.activity_title_media_player)) && Build.VERSION.SDK_INT >= 33)
                list.setVisibility(View.GONE);

            binding.tweaksList.addView(list);
        }
    }
}