package com.drdisagree.iconify.ui.utils;

import static com.drdisagree.iconify.common.Const.FRAGMENT_BACK_BUTTON_DELAY;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.References.FRAGMENT_STYLES;
import static com.drdisagree.iconify.common.References.FRAGMENT_SETTINGS;
import static com.drdisagree.iconify.common.References.FRAGMENT_TWEAKS;

import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.fragments.Styles;
import com.drdisagree.iconify.ui.fragments.Settings;
import com.drdisagree.iconify.ui.fragments.Tweaks;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class FragmentHelper {

    public static String getTopFragment(FragmentManager fragmentManager) {
        String[] fragment = {STR_NULL};

        int last = fragmentManager.getFragments().size() - 1;

        if (last >= 0) {
            Fragment topFragment = fragmentManager.getFragments().get(last);

            if (topFragment instanceof Styles) fragment[0] = FRAGMENT_STYLES;
            else if (topFragment instanceof Tweaks) fragment[0] = FRAGMENT_TWEAKS;
            else if (topFragment instanceof Settings) fragment[0] = FRAGMENT_SETTINGS;
        }

        return fragment[0];
    }

    public static void initHeader(AppCompatActivity appCompatActivity, View view, int title, FragmentManager fragmentManager) {
        CollapsingToolbarLayout collapsing_toolbar = view.findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(appCompatActivity.getResources().getString(title));
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        appCompatActivity.setSupportActionBar(toolbar);
        Objects.requireNonNull(appCompatActivity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(appCompatActivity.getSupportActionBar()).setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(view1 -> new Handler().postDelayed(fragmentManager::popBackStack, FRAGMENT_BACK_BUTTON_DELAY));
    }
}
