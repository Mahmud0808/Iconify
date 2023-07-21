package com.drdisagree.iconify.utils;

import android.util.TypedValue;

import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.helpers.TypedValueUtil;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.List;

public class FabricatedUtil {

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
        if (target == null || name == null || type == null || resourceName == null || val == null) {
            throw new IllegalArgumentException("One or more arguments are null" + "\n" + "target: " + target + "\n" + "name: " + name + "\n" + "type: " + type + "\n" + "resourceName: " + resourceName + "\n" + "val: " + val);
        }

        List<String> commands = buildCommands(target, name, type, resourceName, val);

        Prefs.putBoolean("fabricated" + name, true);
        Prefs.putString("FOCMDtarget" + name, target);
        Prefs.putString("FOCMDname" + name, name);
        Prefs.putString("FOCMDtype" + name, type);
        Prefs.putString("FOCMDresourceName" + name, resourceName);
        Prefs.putString("FOCMDval" + name, val);

        Shell.cmd("mv " + Resources.MODULE_DIR + "/post-exec.sh " + Resources.MODULE_DIR + "/post-exec.txt; grep -v \"IconifyComponent" + name + "\" " + Resources.MODULE_DIR + "/post-exec.txt > " + Resources.MODULE_DIR + "/post-exec.txt.tmp && mv " + Resources.MODULE_DIR + "/post-exec.txt.tmp " + Resources.MODULE_DIR + "/post-exec.sh; rm -rf " + Resources.MODULE_DIR + "/post-exec.txt; rm -rf " + Resources.MODULE_DIR + "/post-exec.txt.tmp").submit();
        Shell.cmd("echo -e \"" + commands.get(0) + "\n" + commands.get(1) + "\" >> " + Resources.MODULE_DIR + "/post-exec.sh").submit();

        Shell.cmd(commands.get(0), commands.get(1)).submit();
    }

    public static List<String> buildCommands(String target, String name, String type, String resourceName, String val) {
        String resourceType = "0x1c";

        if (target.equals("systemui") || target.equals("sysui")) target = "com.android.systemui";

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

        if (type.equals("dimen")) {
            int valType = -1;

            if (val.contains("dp") || val.contains("dip")) {
                valType = TypedValue.COMPLEX_UNIT_DIP;
                val = val.replace("dp", "").replace("dip", "");
            } else if (val.contains("sp")) {
                valType = TypedValue.COMPLEX_UNIT_SP;
                val = val.replace("sp", "");
            } else if (val.contains("px")) {
                valType = TypedValue.COMPLEX_UNIT_PX;
                val = val.replace("px", "");
            } else if (val.contains("in")) {
                valType = TypedValue.COMPLEX_UNIT_IN;
                val = val.replace("in", "");
            } else if (val.contains("pt")) {
                valType = TypedValue.COMPLEX_UNIT_PT;
                val = val.replace("pt", "");
            } else if (val.contains("mm")) {
                valType = TypedValue.COMPLEX_UNIT_MM;
                val = val.replace("mm", "");
            }

            val = String.valueOf(TypedValueUtil.createComplexDimension(Integer.parseInt(val), valType));
        }

        List<String> commands = new ArrayList<>();
        commands.add("cmd overlay fabricate --target " + target + " --name IconifyComponent" + name + " " + target + ":" + type + "/" + resourceName + " " + resourceType + " " + val);
        commands.add("cmd overlay enable --user current com.android.shell:IconifyComponent" + name);

        return commands;
    }

    public static void disableOverlay(String name) {
        Prefs.putBoolean("fabricated" + name, false);
        Prefs.clearPref("FOCMDtarget" + name);
        Prefs.clearPref("FOCMDname" + name);
        Prefs.clearPref("FOCMDtype" + name);
        Prefs.clearPref("FOCMDresourceName" + name);
        Prefs.clearPref("FOCMDval" + name);

        String disable_cmd = "cmd overlay disable --user current com.android.shell:IconifyComponent" + name;

        Shell.cmd("mv " + Resources.MODULE_DIR + "/post-exec.sh " + Resources.MODULE_DIR + "/post-exec.txt; grep -v \"IconifyComponent" + name + "\" " + Resources.MODULE_DIR + "/post-exec.txt > " + Resources.MODULE_DIR + "/post-exec.txt.tmp && mv " + Resources.MODULE_DIR + "/post-exec.txt.tmp " + Resources.MODULE_DIR + "/post-exec.sh; rm -rf " + Resources.MODULE_DIR + "/post-exec.txt; rm -rf " + Resources.MODULE_DIR + "/post-exec.txt.tmp").submit();

        Shell.cmd(disable_cmd).submit();
    }

    public static void disableOverlays(String... names) {
        StringBuilder command = new StringBuilder();

        for (String name : names) {
            Prefs.putBoolean("fabricated" + name, false);
            Prefs.clearPref("FOCMDtarget" + name);
            Prefs.clearPref("FOCMDname" + name);
            Prefs.clearPref("FOCMDtype" + name);
            Prefs.clearPref("FOCMDresourceName" + name);
            Prefs.clearPref("FOCMDval" + name);

            command.append("cmd overlay disable --user current com.android.shell:IconifyComponent").append(name).append("; ");

            Shell.cmd("mv " + Resources.MODULE_DIR + "/post-exec.sh " + Resources.MODULE_DIR + "/post-exec.txt; grep -v \"IconifyComponent" + name + "\" " + Resources.MODULE_DIR + "/post-exec.txt > " + Resources.MODULE_DIR + "/post-exec.txt.tmp && mv " + Resources.MODULE_DIR + "/post-exec.txt.tmp " + Resources.MODULE_DIR + "/post-exec.sh; rm -rf " + Resources.MODULE_DIR + "/post-exec.txt; rm -rf " + Resources.MODULE_DIR + "/post-exec.txt.tmp").submit();
        }

        Shell.cmd(command.toString().trim()).submit();
    }

    public static boolean isOverlayEnabled(List<String> overlays, String name) {
        for (String overlay : overlays) {
            if (overlay.equals("IconifyComponent" + name)) return true;
        }
        return false;
    }

    public static boolean isOverlayDisabled(List<String> overlays, String name) {
        for (String overlay : overlays) {
            if (overlay.equals("IconifyComponent" + name)) return false;
        }
        return true;
    }
}
