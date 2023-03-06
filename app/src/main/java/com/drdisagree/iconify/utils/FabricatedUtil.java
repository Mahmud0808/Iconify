package com.drdisagree.iconify.utils;

import android.util.TypedValue;

import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.helpers.TypedValueUtil;
import com.topjohnwu.superuser.Shell;

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
        if (target == null || name == null || type == null || resourceName == null || val == null)
            return;

        Prefs.putBoolean("fabricated" + name, true);
        Prefs.putString("FOCMDtarget" + name, target);
        Prefs.putString("FOCMDname" + name, name);
        Prefs.putString("FOCMDtype" + name, type);
        Prefs.putString("FOCMDresourceName" + name, resourceName);
        Prefs.putString("FOCMDval" + name, val);

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

            Prefs.putString("TypedValue." + name, val);
        }

        String build_cmd = "cmd overlay fabricate --target " + target + " --name IconifyComponent" + name + " " + target + ":" + type + "/" + resourceName + " " + resourceType + " " + val;
        String enable_cmd = "cmd overlay enable --user current com.android.shell:IconifyComponent" + name;

        Shell.cmd("grep -v \"IconifyComponent" + name + "\" " + Resources.MODULE_DIR + "/post-exec.sh > " + Resources.MODULE_DIR + "/post-exec.sh.tmp && mv " + Resources.MODULE_DIR + "/post-exec.sh.tmp " + Resources.MODULE_DIR + "/post-exec.sh").submit();
        Shell.cmd("echo \"" + build_cmd + "\" >> " + Resources.MODULE_DIR + "/post-exec.sh").submit();
        Shell.cmd("echo \"" + enable_cmd + "\" >> " + Resources.MODULE_DIR + "/post-exec.sh").submit();

        Shell.cmd(build_cmd).submit();
        Shell.cmd(enable_cmd).submit();
    }

    public static String buildCommand(String target, String name, String type, String resourceName, String val) {
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
            int valType = 1;

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

        String build_cmd = "cmd overlay fabricate --target " + target + " --name IconifyComponent" + name + " " + target + ":" + type + "/" + resourceName + " " + resourceType + " " + val;
        String enable_cmd = "cmd overlay enable --user current com.android.shell:IconifyComponent" + name;

        String cmd = "";
        cmd += "grep -v \"IconifyComponent" + name + "\" " + Resources.MODULE_DIR + "/post-exec.sh > " + Resources.MODULE_DIR + "/post-exec.sh.tmp && mv " + Resources.MODULE_DIR + "/post-exec.sh.tmp " + Resources.MODULE_DIR + "/post-exec.sh";
        cmd += "\n";
        cmd += "echo \"" + build_cmd + "\" >> " + Resources.MODULE_DIR + "/post-exec.sh";
        cmd += "\n";
        cmd += "echo \"" + enable_cmd + "\" >> " + Resources.MODULE_DIR + "/post-exec.sh";
        cmd += "\n";

        return cmd;
    }

    public static void disableOverlay(String name) {
        Prefs.putBoolean("fabricated" + name, false);
        Prefs.clearPref("FOCMDtarget" + name);
        Prefs.clearPref("FOCMDname" + name);
        Prefs.clearPref("FOCMDtype" + name);
        Prefs.clearPref("FOCMDresourceName" + name);
        Prefs.clearPref("FOCMDval" + name);

        String disable_cmd = "cmd overlay disable --user current com.android.shell:IconifyComponent" + name;

        Shell.cmd("grep -v \"IconifyComponent" + name + "\" " + Resources.MODULE_DIR + "/post-exec.sh > " + Resources.MODULE_DIR + "/post-exec.sh.tmp && mv " + Resources.MODULE_DIR + "/post-exec.sh.tmp " + Resources.MODULE_DIR + "/post-exec.sh").submit();

        Shell.cmd(disable_cmd).submit();
    }

    public static boolean isOverlayEnabled(List<String> overlays, String name) {
        for (String overlay : overlays) {
            if (overlay.equals(name)) return true;
        }
        return false;
    }

    public static boolean isOverlayDisabled(List<String> overlays, String name) {
        for (String overlay : overlays) {
            if (overlay.equals(name)) return false;
        }
        return true;
    }
}
