package com.drdisagree.iconify.utils;

import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.config.Prefs;
import com.topjohnwu.superuser.Shell;

import java.util.List;

public class FabricatedOverlayUtil {

    public static List<String> getOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^....com.android.shell:IconifyComponent' | sed -E 's/^....com.android.shell:IconifyComponent//'").exec().getOut();
    }

    public static List<String> getEnabledOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^.x..com.android.shell:IconifyComponent' | sed -E 's/^.x..com.android.shell:IconifyComponent//'").exec().getOut();
    }

    public static List<String> getDisabledOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^. ..com.android.shell:IconifyComponent' | sed -E 's/^. ..com.android.shell:IconifyComponent//'").exec().getOut();
    }

    public static void buildAndEnableOverlay(String target, String name, String type, String resourceName, String val) {
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

        String build_cmd = "cmd overlay fabricate --target " + target + " --name IconifyComponent" + name + " " + target + ":" + type + "/" + resourceName + " " + resourceType + " " + val;
        String enable_cmd = "cmd overlay enable --user current com.android.shell:IconifyComponent" + name;

        Shell.cmd("grep -v \"IconifyComponent" + name + "\" " + References.MODULE_DIR + "/service.sh > " + References.MODULE_DIR + "/iconify_temp.sh && mv " + References.MODULE_DIR + "/iconify_temp.sh " + References.MODULE_DIR + "/service.sh").exec();
        Shell.cmd("echo \"" + build_cmd + "\" >> " + References.MODULE_DIR + "/service.sh").exec();
        Shell.cmd("echo \"" + enable_cmd + "\" >> " + References.MODULE_DIR + "/service.sh").exec();

        Shell.cmd(build_cmd).exec();
        Shell.cmd(enable_cmd).exec();

        Prefs.putBoolean("fabricated" + name, true);
    }

    public static void disableOverlay(String name) {
        String disable_cmd = "cmd overlay disable --user current com.android.shell:IconifyComponent" + name;

        Shell.cmd("grep -v \"IconifyComponent" + name + "\" " + References.MODULE_DIR + "/service.sh > " + References.MODULE_DIR + "/iconify_temp.sh && mv " + References.MODULE_DIR + "/iconify_temp.sh " + References.MODULE_DIR + "/service.sh").exec();

        Shell.cmd(disable_cmd).exec();

        Prefs.putBoolean("fabricated" + name, false);
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
