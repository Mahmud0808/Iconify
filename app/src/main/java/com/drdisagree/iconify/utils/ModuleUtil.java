package com.drdisagree.iconify.utils;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.Preferences.RESTART_SYSUI_AFTER_BOOT;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_SECONDARY;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.TypedValue;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.helpers.BackupRestore;
import com.drdisagree.iconify.utils.helpers.BinaryInstaller;
import com.drdisagree.iconify.utils.helpers.TypedValueUtil;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ModuleUtil {

    private static final String TAG = "ModuleUtil";
    private static final List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();

    public static void handleModule() throws IOException {
        if (moduleExists()) {
            // Backup necessary files
            BackupRestore.backupFiles();

            Shell.cmd("rm -rf " + Resources.MODULE_DIR).exec();
        }
        installModule();
    }

    static void installModule() {
        Log.d(TAG, "Magisk module does not exist, creating...");

        // Clean temporary directory
        Shell.cmd("mkdir -p " + Resources.MODULE_DIR).exec();
        Shell.cmd("printf 'id=Iconify\nname=Iconify\nversion=" + BuildConfig.VERSION_NAME + "\nversionCode=" + BuildConfig.VERSION_CODE + "\nauthor=@DrDisagree\ndescription=Systemless module for Iconify.\n' > " + Resources.MODULE_DIR + "/module.prop").exec();
        Shell.cmd("mkdir -p " + Resources.MODULE_DIR + "/common").exec();
        Shell.cmd("printf 'MODDIR=${0%%/*}\n\n' > " + Resources.MODULE_DIR + "/post-fs-data.sh").exec();
        Shell.cmd("printf 'MODDIR=${0%%/*}\n\nwhile [ \"$(getprop sys.boot_completed | tr -d \"\\r\")\" != \"1\" ]\ndo\n sleep 1\ndone\nsleep 5\n\nsh $MODDIR/post-exec.sh\n\n" + (Prefs.getBoolean(RESTART_SYSUI_AFTER_BOOT, false) ? "killall " + SYSTEMUI_PACKAGE + "\n" : "") + "sleep 6\n\nqspb=$(cmd overlay list |  grep -E \"^.x..IconifyComponentQSPB.overlay\" | sed -E \"s/^.x..//\")\ndm=$(cmd overlay list |  grep -E \"^.x..IconifyComponentDM.overlay\" | sed -E \"s/^.x..//\")\nif ([ ! -z \"$qspb\" ] && [ -z \"$dm\" ])\nthen\n cmd overlay disable --user current IconifyComponentQSPB.overlay\n cmd overlay enable --user current IconifyComponentQSPB.overlay\n cmd overlay set-priority IconifyComponentQSPB.overlay highest\nfi\n\n' > " + Resources.MODULE_DIR + "/service.sh").exec();
        Shell.cmd("touch " + Resources.MODULE_DIR + "/common/system.prop").exec();
        Shell.cmd("touch " + Resources.MODULE_DIR + "/auto_mount").exec();
        Shell.cmd("mkdir -p " + Resources.MODULE_DIR + "/system").exec();
        Shell.cmd("mkdir -p " + Resources.MODULE_DIR + "/system/product").exec();
        Shell.cmd("mkdir -p " + Resources.MODULE_DIR + "/system/product/overlay").exec();

        writePostExec();
        BinaryInstaller.symLinkBinaries();

        Log.i(TAG, "Magisk module successfully created.");
    }


    private static void writePostExec() {
        StringBuilder post_exec = new StringBuilder();
        boolean primaryColorEnabled = false;
        boolean secondaryColorEnabled = false;

        SharedPreferences prefs = Iconify.getAppContext().getSharedPreferences(Iconify.getAppContext().getPackageName(), Context.MODE_PRIVATE);
        Map<String, ?> map = prefs.getAll();
        for (Map.Entry<String, ?> item : map.entrySet()) {
            if (item.getValue() instanceof Boolean && ((Boolean) item.getValue()) && item.getKey().contains("fabricated") && !item.getKey().contains("quickQsOffsetHeight")) {
                post_exec.append(buildFabricatedCommand(Prefs.getString("FOCMDtarget" + item.getKey().replace("fabricated", "")), Prefs.getString("FOCMDname" + item.getKey().replace("fabricated", "")), Prefs.getString("FOCMDtype" + item.getKey().replace("fabricated", "")), Prefs.getString("FOCMDresourceName" + item.getKey().replace("fabricated", "")), Prefs.getString("FOCMDval" + item.getKey().replace("fabricated", "")))).append('\n');
                if (item.getKey().contains(COLOR_ACCENT_PRIMARY)) primaryColorEnabled = true;
                else if (item.getKey().contains(COLOR_ACCENT_SECONDARY))
                    secondaryColorEnabled = true;
            }
        }

        if (!primaryColorEnabled && shouldUseDefaultColors()) {
            post_exec.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary android:color/holo_blue_light 0x1c " + ICONIFY_COLOR_ACCENT_PRIMARY + "\n");
            post_exec.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary\n");
        }

        if (!secondaryColorEnabled && shouldUseDefaultColors()) {
            post_exec.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondary android:color/holo_green_light 0x1c " + ICONIFY_COLOR_ACCENT_SECONDARY + "\n");
            post_exec.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentSecondary\n");
        }

        Shell.cmd("printf '" + post_exec + "' > " + Resources.MODULE_DIR + "/post-exec.sh").exec();
        Shell.cmd("chmod 755 " + Resources.MODULE_DIR + "/post-exec.sh").exec();
    }

    private static boolean shouldUseDefaultColors() {
        return OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMAC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMACL.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMGC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMGCL.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentME.overlay");
    }

    public static boolean moduleExists() {
        return RootUtil.folderExists(Resources.MODULE_DIR);
    }

    public static void extractPremadeOverlays() {
        Log.d(TAG, "Extracting pre-made overlays...");
        try {
            FileUtil.copyAssets("PremadeOverlays");
            Shell.cmd("rm " + Resources.DATA_DIR + "/PremadeOverlays/cheatsheet").exec();
            Shell.cmd("cp -a " + Resources.DATA_DIR + "/PremadeOverlays/. " + Resources.OVERLAY_DIR).exec();
            FileUtil.cleanDir("PremadeOverlays");
            RootUtil.setPermissionsRecursively(644, Resources.OVERLAY_DIR + '/');
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract pre-made overlays.\n" + e);
        }
    }

    public static String buildFabricatedCommand(String target, String name, String type, String resourceName, String val) {
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

        return build_cmd + '\n' + enable_cmd;
    }
}
