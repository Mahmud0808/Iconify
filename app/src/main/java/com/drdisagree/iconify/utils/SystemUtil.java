package com.drdisagree.iconify.utils;

import android.content.res.Configuration;

import com.drdisagree.iconify.Iconify;

public class SystemUtil {
    public static boolean isDarkMode() {
        return (Iconify.getAppContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;
    }
}
