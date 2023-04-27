package com.drdisagree.iconify.ui.utils;

import static com.drdisagree.iconify.common.Preferences.APP_THEME;

import androidx.appcompat.app.AppCompatDelegate;

import com.drdisagree.iconify.config.Prefs;

public class ThemeHelper {

    public static int getTheme() {
        int theme = Prefs.getInt(APP_THEME, 2);

        switch (theme) {
            case 0:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case 1:
                return AppCompatDelegate.MODE_NIGHT_YES;
            case 2:
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }
}
