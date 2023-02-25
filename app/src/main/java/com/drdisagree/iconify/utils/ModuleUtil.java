package com.drdisagree.iconify.utils;

import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_PIXEL_DARK_BG;
import static com.drdisagree.iconify.common.Resources.BIN_DIR;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.helpers.BackupRestore;
import com.drdisagree.iconify.utils.helpers.BinaryInstaller;
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
        Shell.cmd("printf 'id=Iconify\n" + "name=Iconify\nversion=" + BuildConfig.VERSION_NAME + "\n" + "versionCode=" + BuildConfig.VERSION_CODE + "\n" + "" + "author=@DrDisagree\n" + "description=Systemless module for Iconify.\n' > " + Resources.MODULE_DIR + "/module.prop").exec();
        Shell.cmd("mkdir -p " + Resources.MODULE_DIR + "/common").exec();
        Shell.cmd("printf 'MODDIR=${0%%/*}\n' > " + Resources.MODULE_DIR + "/post-fs-data.sh").exec();

        SharedPreferences prefs = Iconify.getAppContext().getSharedPreferences(Iconify.getAppContext().getPackageName(), Context.MODE_PRIVATE);
        Map<String, ?> map = prefs.getAll();

        boolean primaryColorEnabled = false;
        boolean secondaryColorEnabled = false;
        StringBuilder fabricated_cmd = new StringBuilder();
        for (Map.Entry<String, ?> item : map.entrySet()) {
            if (item.getValue() instanceof Boolean && ((Boolean) item.getValue()) && item.getKey().contains("fabricated") && !item.getKey().contains("quickQsOffsetHeight")) {
                fabricated_cmd.append(FabricatedUtil.buildCommand(Prefs.getString("FOCMDtarget" + item.getKey().replace("fabricated", "")), Prefs.getString("FOCMDname" + item.getKey().replace("fabricated", "")), Prefs.getString("FOCMDtype" + item.getKey().replace("fabricated", "")), Prefs.getString("FOCMDresourceName" + item.getKey().replace("fabricated", "")), Prefs.getString("FOCMDval" + item.getKey().replace("fabricated", ""))));
                if (item.getKey().contains(COLOR_ACCENT_PRIMARY)) primaryColorEnabled = true;
                else if (item.getKey().contains(COLOR_ACCENT_SECONDARY))
                    secondaryColorEnabled = true;
            }
        }

        if (!primaryColorEnabled && shouldUseDefaultColors()) {
            fabricated_cmd.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary android:color/holo_blue_light 0x1c " + ICONIFY_COLOR_ACCENT_PRIMARY + "\n");
            fabricated_cmd.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary\n");
            fabricated_cmd.append("cmd overlay fabricate --target android --name IconifyComponentcolorPixelBackgroundDark android:color/holo_blue_dark 0x1c " + ICONIFY_COLOR_PIXEL_DARK_BG + "\n");
            fabricated_cmd.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorPixelBackgroundDark\n");
        }

        if (!secondaryColorEnabled && shouldUseDefaultColors()) {
            fabricated_cmd.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondary android:color/holo_green_light 0x1c " + ICONIFY_COLOR_ACCENT_SECONDARY + "\n");
            fabricated_cmd.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentSecondary\n");
        }

        String service_sh = "MODDIR=${0%%/*}\n\n" + "while [ \"$(getprop sys.boot_completed | tr -d '\\r')\" != \"1\" ]\n" + "do\n" + " sleep 1\n" + "done\n" + "sleep 10\n\n" + "qspb=$(cmd overlay list |  grep -E '^.x..IconifyComponentQSPB.overlay' | sed -E 's/^.x..//')\n" + "dm=$(cmd overlay list |  grep -E '^.x..IconifyComponentDM.overlay' | sed -E 's/^.x..//')\n" + "if ([ ! -z \"$qspb\" ] && [ -z \"$dm\" ])\n" + "then\n" + " cmd overlay disable --user current IconifyComponentQSPB.overlay\n" + " cmd overlay enable --user current IconifyComponentQSPB.overlay\n" + " cmd overlay set-priority IconifyComponentQSPB.overlay highest\n" + "fi\n\n";

        service_sh += fabricated_cmd;
        service_sh += "\n";

        Shell.cmd("printf '" + service_sh + "' > " + Resources.MODULE_DIR + "/service.sh").exec();
        Shell.cmd("touch " + Resources.MODULE_DIR + "/common/system.prop").exec();
        Shell.cmd("touch " + Resources.MODULE_DIR + "/auto_mount").exec();
        Shell.cmd("mkdir -p " + Resources.MODULE_DIR + "/tools").exec();
        Shell.cmd("mkdir -p " + Resources.MODULE_DIR + "/system").exec();
        Shell.cmd("mkdir -p " + Resources.MODULE_DIR + "/system/product").exec();
        Shell.cmd("mkdir -p " + Resources.MODULE_DIR + "/system/product/overlay").exec();
        Shell.cmd("mkdir -p " + BIN_DIR).exec();

        extractTools();
        BinaryInstaller.symLinkBinaries();

        Log.i(TAG, "Magisk module successfully created.");
    }

    private static boolean shouldUseDefaultColors() {
        return OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMAC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMACL.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMGC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMGCL.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentME.overlay");
    }

    public static boolean moduleExists() {
        return RootUtil.folderExists(Resources.MODULE_DIR);
    }

    public static void extractTools() {
        Log.d(TAG, "Extracting tools...");
        try {
            FileUtil.copyAssets("Tools");
            Shell.cmd("cp -a " + Resources.DATA_DIR + "/Tools/. " + Resources.MODULE_DIR + "/tools").exec();
            FileUtil.cleanDir("Tools");
            RootUtil.setPermissionsRecursively(755, Resources.MODULE_DIR + "/tools");
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract tools.\n" + e);
        }
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
            e.printStackTrace();
        }
    }
}
