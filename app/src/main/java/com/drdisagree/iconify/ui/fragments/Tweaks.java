package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.FRAGMENT_TRANSITION_DELAY;
import static com.drdisagree.iconify.common.References.FRAGMENT_COLORENGINE;
import static com.drdisagree.iconify.common.References.FRAGMENT_MEDIAPLAYER;
import static com.drdisagree.iconify.common.References.FRAGMENT_NAVIGATIONBAR;
import static com.drdisagree.iconify.common.References.FRAGMENT_STATUSBAR;
import static com.drdisagree.iconify.common.References.FRAGMENT_STYLES;
import static com.drdisagree.iconify.common.References.FRAGMENT_UIROUNDNESS;
import static com.drdisagree.iconify.common.References.FRAGMENT_VOLUMEPANEL;
import static com.drdisagree.iconify.common.References.FRAGMENT_XPOSEDMENU;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.utils.AppUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;

public class Tweaks extends Fragment {

    private ViewGroup listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tweaks, container, false);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = view.findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.navbar_tweaks));
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        listView = view.findViewById(R.id.tweaks_list);

        ArrayList<Object[]> tweaks_list = new ArrayList<>();

        tweaks_list.add(new Object[]{new ColorEngine(), FRAGMENT_COLORENGINE, getResources().getString(R.string.activity_title_color_engine), getResources().getString(R.string.activity_desc_color_engine), R.drawable.ic_home_color});
        tweaks_list.add(new Object[]{new UIRoundness(), FRAGMENT_UIROUNDNESS, getResources().getString(R.string.activity_title_ui_roundness), getResources().getString(R.string.activity_desc_ui_roundness), R.drawable.ic_home_ui_roundness});
        tweaks_list.add(new Object[]{new StatusBar(), FRAGMENT_STATUSBAR, getResources().getString(R.string.activity_title_statusbar), getResources().getString(R.string.activity_desc_statusbar), R.drawable.ic_extras_statusbar});
        tweaks_list.add(new Object[]{new NavigationBar(), FRAGMENT_NAVIGATIONBAR, getResources().getString(R.string.activity_title_navigation_bar), getResources().getString(R.string.activity_desc_navigation_bar), R.drawable.ic_extras_navbar});
        tweaks_list.add(new Object[]{new MediaPlayer(), FRAGMENT_MEDIAPLAYER, getResources().getString(R.string.activity_title_media_player), getResources().getString(R.string.activity_desc_media_player), R.drawable.ic_home_media});
        tweaks_list.add(new Object[]{new VolumePanel(), FRAGMENT_VOLUMEPANEL, getResources().getString(R.string.activity_title_volume_panel), getResources().getString(R.string.activity_desc_volume_panel), R.drawable.ic_home_volume});
        tweaks_list.add(new Object[]{null, null, getResources().getString(R.string.activity_title_xposed_menu), getResources().getString(R.string.activity_desc_xposed_menu), R.drawable.ic_extras_xposed_menu});

        addItem(tweaks_list);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out);

        // Xposed Menu fragment
        listView.getChildAt(tweaks_list.size() - 1).findViewById(R.id.list_info_item).setOnClickListener(view1 -> {
            new Handler().postDelayed(() -> {
                // Check if LSPosed is installed or not
                if (!AppUtil.isLsposedInstalled()) {
                    Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_lsposed_not_found), Toast.LENGTH_SHORT).show();
                    return;
                }

                fragmentTransaction.replace(R.id.main_fragment, new XposedMenu(), FRAGMENT_XPOSEDMENU);
                fragmentManager.popBackStack(FRAGMENT_STYLES, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentTransaction.addToBackStack(FRAGMENT_XPOSEDMENU);
                fragmentTransaction.commit();
            }, 100);
        });

        return view;
    }

    // Function to add new item in list
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(requireActivity()).inflate(R.layout.view_list_menu, listView, false);

            TextView title = list.findViewById(R.id.list_title);
            title.setText((String) pack.get(i)[2]);

            TextView desc = list.findViewById(R.id.list_desc);
            desc.setText((String) pack.get(i)[3]);

            ImageView preview = list.findViewById(R.id.list_preview);
            preview.setImageResource((int) pack.get(i)[4]);

            if (pack.get(i)[0] != null) {
                int finalI = i;
                list.setOnClickListener(view -> {
                    FragmentManager fragmentManager = getParentFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out);

                    new Handler().postDelayed(() -> {
                        fragmentTransaction.replace(R.id.main_fragment, (Fragment) pack.get(finalI)[0], (String) pack.get(finalI)[1]);
                        fragmentManager.popBackStack(FRAGMENT_STYLES, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        fragmentTransaction.addToBackStack((String) pack.get(finalI)[1]);
                        fragmentTransaction.commit();
                    }, FRAGMENT_TRANSITION_DELAY);
                });
            }

            listView.addView(list);
        }
    }
}