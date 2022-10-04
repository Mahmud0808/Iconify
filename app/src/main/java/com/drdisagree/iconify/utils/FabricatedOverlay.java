package com.drdisagree.iconify.utils;

import com.topjohnwu.superuser.Shell;

import java.util.List;

public class FabricatedOverlay {

    public static List<String> getOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^....com.android.shell:IconifyComponent' | sed -E 's/^....com.android.shell:IconifyComponent//'").exec().getOut();
    }

    public static List<String> getEnabledOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^.x..com.android.shell:IconifyComponent' | sed -E 's/^.x..com.android.shell:IconifyComponent//'").exec().getOut();
    }

    public static void buildOverlay(String target, String name, String type, String resourceName, String val) {
        disableOverlay(name);

        String resourceType = "0x1c";

        if (target.equals("systemui"))
            target = "com.android.systemui";

        switch (type) {
            case "color":
                resourceType = "0x1c";
                break;
            case "dimen":
                resourceType = "0x05";
                break;
            case "bool":
                resourceType = "0x12";
                break;
            case "integer":
                resourceType = "0x10";
                break;
        }

        Shell.cmd("cmd overlay fabricate --target " + target + " --name IconifyComponent" + name + " " + target + ":" + type + "/" + resourceName + " " + resourceType + " " + val).exec();
    }

    public static void enableOverlay(String name) {
        Shell.cmd("cmd overlay enable --user current com.android.shell:IconifyComponent" + name).exec();
    }

    public static void disableOverlay(String name) {
        Shell.cmd("cmd overlay disable --user current com.android.shell:IconifyComponent" + name).exec();
    }

    public static boolean isOverlayEnabled(List<String> overlays, String name) {
        for (String overlay : overlays) {
            if (overlay.equals(name))
                return true;
        }
        return false;
    }

    public static boolean isOverlayDisabled(List<String> overlays, String name) {
        for (String overlay : overlays) {
            if (overlay.equals(name))
                return false;
        }
        return true;
    }
}
