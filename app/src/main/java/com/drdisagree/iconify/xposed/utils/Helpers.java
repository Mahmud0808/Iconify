package com.drdisagree.iconify.xposed.utils;

import com.drdisagree.iconify.config.Prefs;
import com.topjohnwu.superuser.Shell;

public class Helpers {

    public static void enableOverlay(String pkgName) {
        Shell.cmd("cmd overlay enable --user current " + pkgName, "cmd overlay set-priority " + pkgName + " highest").exec();
    }

    public static void disableOverlay(String pkgName) {
        Shell.cmd("cmd overlay disable --user current " + pkgName).exec();
    }
}
