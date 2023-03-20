package com.drdisagree.iconify.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.activities.ColorEngine;
import com.drdisagree.iconify.ui.activities.MediaPlayer;
import com.drdisagree.iconify.ui.activities.NavigationBar;
import com.drdisagree.iconify.ui.activities.Statusbar;
import com.drdisagree.iconify.ui.activities.UiRoundness;
import com.drdisagree.iconify.ui.activities.VolumePanel;
import com.drdisagree.iconify.ui.activities.XPosedMenu;

import java.util.ArrayList;

public class Tweaks extends Fragment {

    private ViewGroup listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tweaks, container, false);
        listView = view.findViewById(R.id.tweaks_list);

        ArrayList<Object[]> tweaks_list = new ArrayList<>();

        tweaks_list.add(new Object[]{ColorEngine.class, getResources().getString(R.string.activity_title_color_engine), getResources().getString(R.string.activity_desc_color_engine), R.drawable.ic_home_color});
        tweaks_list.add(new Object[]{UiRoundness.class, getResources().getString(R.string.activity_title_ui_roundness), getResources().getString(R.string.activity_desc_ui_roundness), R.drawable.ic_home_ui_roundness});
        tweaks_list.add(new Object[]{Statusbar.class, getResources().getString(R.string.activity_title_statusbar), getResources().getString(R.string.activity_desc_statusbar), R.drawable.ic_extras_statusbar});
        tweaks_list.add(new Object[]{NavigationBar.class, getResources().getString(R.string.activity_title_navigation_bar), getResources().getString(R.string.activity_desc_navigation_bar), R.drawable.ic_extras_navbar});
        tweaks_list.add(new Object[]{MediaPlayer.class, getResources().getString(R.string.activity_title_media_player), getResources().getString(R.string.activity_desc_media_player), R.drawable.ic_home_media});
        tweaks_list.add(new Object[]{VolumePanel.class, getResources().getString(R.string.activity_title_volume_panel), getResources().getString(R.string.activity_desc_volume_panel), R.drawable.ic_home_volume});
        tweaks_list.add(new Object[]{XPosedMenu.class, getResources().getString(R.string.activity_title_xposed_menu), getResources().getString(R.string.activity_desc_xposed_menu), R.drawable.ic_extras_xposed_menu});

        addItem(tweaks_list);

        // Enable onClick event
        for (int i = 0; i < tweaks_list.size(); i++) {
            LinearLayout child = listView.getChildAt(i).findViewById(R.id.list_info_item);
            int finalI = i;
            child.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), (Class<?>) tweaks_list.get(finalI)[0]);
                startActivity(intent);
            });
        }

        return view;
    }

    // Function to add new item in list
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(requireActivity()).inflate(R.layout.view_list_menu, listView, false);

            TextView title = list.findViewById(R.id.list_title);
            title.setText((String) pack.get(i)[1]);

            TextView desc = list.findViewById(R.id.list_desc);
            desc.setText((String) pack.get(i)[2]);

            ImageView preview = list.findViewById(R.id.list_preview);
            preview.setImageResource((int) pack.get(i)[3]);

            listView.addView(list);
        }
    }
}