package com.drdisagree.iconify.utils;

import static com.drdisagree.iconify.common.Preferences.FORCE_APPLY_XPOSED_CHOICE;

import com.drdisagree.iconify.config.Prefs;

public class HelperUtil {

    public static void forceApply() {
        if (Prefs.getInt(FORCE_APPLY_XPOSED_CHOICE, 0) == 0) SystemUtil.doubleToggleDarkMode();
        else if (Prefs.getInt(FORCE_APPLY_XPOSED_CHOICE, 0) == 1) SystemUtil.restartSystemUI();
    }
}
