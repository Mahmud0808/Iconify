package com.drdisagree.iconify;

import android.content.Context;
import android.content.pm.PackageManager;

import com.topjohnwu.superuser.Shell;

import java.util.List;

public class OverlayUtils {
    static boolean isOverlayEnabled(String pkgName) {
        List<String> out = Shell.cmd("cmd overlay list").exec().getOut();
        for (String line : out) {
            if (line.startsWith("[x]") && line.contains(pkgName))
                return true;
        }
        return false;
    }

    static boolean isOverlayDisabled(String pkgName) {
        List<String> out = Shell.cmd("cmd overlay list").exec().getOut();
        for (String line : out) {
            if (line.startsWith("[ ]") && line.contains(pkgName))
                return true;
        }
        return false;
    }

    static boolean isOverlayInstalled(String pkgName) {
        List<String> out = Shell.cmd("cmd overlay list").exec().getOut();
        for (String line : out) {
            if (line.contains(pkgName))
                return true;
        }
        return false;
    }

    static void enableOverlay(String pkgName) {
        if (isOverlayEnabled(pkgName))
            disableOverlay(pkgName);
        Shell.cmd("cmd overlay enable --user current " + pkgName, "cmd overlay set-priority " + pkgName +" highest").exec();
    }

    static void disableOverlay(String pkgName) {
        Shell.cmd("cmd overlay disable --user current " + pkgName).exec();
    }
}
