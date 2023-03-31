package com.drdisagree.iconify.utils;

import android.util.Log;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.config.Prefs;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Objects;

public class OverlayUtil {

    public static List<String> getOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponent' | sed -E 's/^....//'").exec().getOut();
    }

    public static List<String> getEnabledOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^.x..IconifyComponent' | sed -E 's/^.x..//'").exec().getOut();
    }

    public static List<String> getDisabledOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^. ..IconifyComponent' | sed -E 's/^. ..//'").exec().getOut();
    }

    public static boolean isOverlayEnabled(List<String> overlays, String pkgName) {
        for (String overlay : overlays) {
            if (overlay.equals(pkgName)) return true;
        }
        return false;
    }

    public static boolean isOverlayDisabled(List<String> overlays, String pkgName) {
        for (String overlay : overlays) {
            if (overlay.equals(pkgName)) return false;
        }
        return true;
    }

    static boolean isOverlayInstalled(List<String> enabledOverlays, String pkgName) {
        for (String line : enabledOverlays) {
            if (line.equals(pkgName)) return true;
        }
        return false;
    }

    public static void enableOverlay(String pkgName) {
        Prefs.putBoolean(pkgName, true);
        Shell.cmd("cmd overlay enable --user current " + pkgName, "cmd overlay set-priority " + pkgName + " highest").exec();
    }

    public static void disableOverlay(String pkgName) {
        Prefs.putBoolean(pkgName, false);
        Shell.cmd("cmd overlay disable --user current " + pkgName).exec();
    }

    public static boolean overlayExists() {
        List<String> list = Shell.cmd("[ -f /system/product/overlay/IconifyComponentAMGC.apk ] && echo \"found\" || echo \"not found\"").exec().getOut();
        return Objects.equals(list.get(0), "found");
    }

    public static boolean matchOverlayAgainstAssets() {
        try {
            String[] packages = Iconify.getAppContext().getAssets().list("Overlays");
            int numberOfOverlaysInAssets = 0;

            for (String overlay : packages) {
                numberOfOverlaysInAssets += Iconify.getAppContext().getAssets().list("Overlays/" + overlay).length;
            }

            int numberOfOverlaysInstalled = Integer.parseInt(Shell.cmd("find /" + Resources.OVERLAY_DIR + "/ -maxdepth 1 -type f -print| wc -l").exec().getOut().get(0));
            Log.e("OverlayExists", String.valueOf(numberOfOverlaysInAssets) + ' ' + numberOfOverlaysInstalled);
            return numberOfOverlaysInAssets <= numberOfOverlaysInstalled;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
