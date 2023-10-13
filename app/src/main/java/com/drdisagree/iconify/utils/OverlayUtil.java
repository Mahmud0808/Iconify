package com.drdisagree.iconify.utils;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.config.Prefs;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    public static boolean isOverlayEnabled(String pkgName) {
        try {
            return Iconify.mRootServiceProvider.isOverlayEnabled(pkgName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isOverlayDisabled(String pkgName) {
        return !isOverlayEnabled(pkgName);
    }

    static boolean isOverlayInstalled(List<String> enabledOverlays, String pkgName) {
        for (String line : enabledOverlays) {
            if (line.equals(pkgName)) return true;
        }
        return false;
    }

    public static void enableOverlay(String pkgName) {
        try {
            Prefs.putBoolean(pkgName, true);
            Iconify.mRootServiceProvider.enableOverlay(Collections.singletonList(pkgName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disableOverlay(String pkgName) {
        try {
            Prefs.putBoolean(pkgName, false);
            Iconify.mRootServiceProvider.disableOverlay(Collections.singletonList(pkgName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void enableOverlays(String... pkgNames) {
        StringBuilder command = new StringBuilder();

        for (String pkgName : pkgNames) {
            Prefs.putBoolean(pkgName, true);
            command.append("cmd overlay enable --user current ").append(pkgName).append("; cmd overlay set-priority ").append(pkgName).append(" highest; ");
        }

        Shell.cmd(command.toString().trim()).submit();
    }

    public static void disableOverlays(String... pkgNames) {
        StringBuilder command = new StringBuilder();

        for (String pkgName : pkgNames) {
            Prefs.putBoolean(pkgName, false);
            command.append("cmd overlay disable --user current ").append(pkgName).append("; ");
        }

        Shell.cmd(command.toString().trim()).submit();
    }

    public static void changeOverlayState(Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Number of arguments must be even.");
        }

        StringBuilder command = new StringBuilder();

        for (int i = 0; i < args.length; i += 2) {
            String pkgName = (String) args[i];
            boolean state = (boolean) args[i + 1];

            Prefs.putBoolean(pkgName, state);

            if (state) {
                command.append("cmd overlay enable --user current ").append(pkgName).append("; cmd overlay set-priority ").append(pkgName).append(" highest; ");
            } else {
                command.append("cmd overlay disable --user current ").append(pkgName).append("; ");
            }
        }

        Shell.cmd(command.toString().trim()).submit();
    }

    public static boolean overlayExists() {
        List<String> list = Shell.cmd("[ -f /system/product/overlay/IconifyComponentAMGC.apk ] && echo \"found\" || echo \"not found\"").exec().getOut();
        return Objects.equals(list.get(0), "found");
    }

    public static boolean matchOverlayAgainstAssets() {
        try {
            String[] packages = Iconify.getAppContext().getAssets().list("Overlays");
            int numberOfOverlaysInAssets = 0;

            assert packages != null;
            for (String overlay : packages) {
                numberOfOverlaysInAssets += Objects.requireNonNull(Iconify.getAppContext().getAssets().list("Overlays/" + overlay)).length;
            }

            int numberOfOverlaysInstalled = Integer.parseInt(Shell.cmd("find /" + Resources.OVERLAY_DIR + "/ -maxdepth 1 -type f -print| wc -l").exec().getOut().get(0));
            return numberOfOverlaysInAssets <= numberOfOverlaysInstalled;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
