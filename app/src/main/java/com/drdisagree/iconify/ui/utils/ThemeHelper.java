package com.drdisagree.iconify.ui.utils;

import static com.drdisagree.iconify.common.Preferences.APP_THEME;

import androidx.appcompat.app.AppCompatDelegate;

import com.drdisagree.iconify.config.Prefs;

public class ThemeHelper {

    public static int getTheme() {
        int theme = Prefs.getInt(APP_THEME, 2);

        return switch (theme) {
            case 0 -> AppCompatDelegate.MODE_NIGHT_NO;
            case 1 -> AppCompatDelegate.MODE_NIGHT_YES;
            default -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        };
    }
}
