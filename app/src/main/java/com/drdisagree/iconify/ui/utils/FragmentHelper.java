package com.drdisagree.iconify.ui.utils;

import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.References.FRAGMENT_SETTINGS;
import static com.drdisagree.iconify.common.References.FRAGMENT_STYLES;
import static com.drdisagree.iconify.common.References.FRAGMENT_TWEAKS;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.drdisagree.iconify.ui.fragments.Settings;
import com.drdisagree.iconify.ui.fragments.Styles;
import com.drdisagree.iconify.ui.fragments.Tweaks;

public class FragmentHelper {

    public static String getTopFragment(FragmentManager fragmentManager) {
        String[] fragment = {STR_NULL};

        int last = fragmentManager.getFragments().size() - 1;

        if (last >= 0) {
            Fragment topFragment = fragmentManager.getFragments().get(last);

            if (topFragment instanceof Styles)
                fragment[0] = FRAGMENT_STYLES;
            else if (topFragment instanceof Tweaks)
                fragment[0] = FRAGMENT_TWEAKS;
            else if (topFragment instanceof Settings)
                fragment[0] = FRAGMENT_SETTINGS;
        }

        return fragment[0];
    }
}
