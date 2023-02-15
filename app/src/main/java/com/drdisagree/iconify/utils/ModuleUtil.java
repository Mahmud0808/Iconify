package com.drdisagree.iconify.utils;

import static com.drdisagree.iconify.common.References.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.References.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_PIXEL_DARK_BG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.config.Prefs;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ModuleUtil {

    public static List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();

    public static boolean handleModule() throws IOException {
        if (moduleExists()) {
            // Backup necessary files
            HelperUtil.backupFiles();

            Shell.cmd("rm -rf " + References.MODULE_DIR).exec();
        }
        return installModule();
    }

    static boolean installModule() throws IOException {
        Log.e("ModuleCheck", "Magisk module does not exist, creating!");
        // Clean temporary directory
        Shell.cmd("mkdir -p " + References.MODULE_DIR).exec();
        Shell.cmd("printf 'id=Iconify\n" +
                "name=Iconify\nversion=" + BuildConfig.VERSION_NAME + "\n" +
                "versionCode=" + BuildConfig.VERSION_CODE + "\n" + "" +
                "author=@DrDisagree\n" +
                "description=Systemless module for Iconify.\n' > " + References.MODULE_DIR + "/module.prop").exec();
        Shell.cmd("mkdir -p " + References.MODULE_DIR + "/common").exec();
        Shell.cmd("printf 'MODDIR=${0%%/*}\n' > " + References.MODULE_DIR + "/post-fs-data.sh").exec();

        SharedPreferences prefs = Iconify.getAppContext().getSharedPreferences(Iconify.getAppContext().getPackageName(), Context.MODE_PRIVATE);
        Map<String, ?> map = prefs.getAll();

        boolean primaryColorEnabled = false;
        boolean secondaryColorEnabled = false;
        StringBuilder fabricated_cmd = new StringBuilder();
        for (Map.Entry<String, ?> item : map.entrySet()) {
            if (item.getValue() instanceof Boolean && ((Boolean) item.getValue()) && item.getKey().contains("fabricated") && !item.getKey().contains("quickQsOffsetHeight")) {
                fabricated_cmd.append(FabricatedOverlayUtil.buildCommand(
                        Prefs.getString("FOCMDtarget" + item.getKey().replace("fabricated", "")),
                        Prefs.getString("FOCMDname" + item.getKey().replace("fabricated", "")),
                        Prefs.getString("FOCMDtype" + item.getKey().replace("fabricated", "")),
                        Prefs.getString("FOCMDresourceName" + item.getKey().replace("fabricated", "")),
                        Prefs.getString("FOCMDval" + item.getKey().replace("fabricated", ""))));
                if (item.getKey().contains(COLOR_ACCENT_PRIMARY))
                    primaryColorEnabled = true;
                else if (item.getKey().contains(COLOR_ACCENT_SECONDARY))
                    secondaryColorEnabled = true;
            }
        }

        if (!primaryColorEnabled && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMAC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMGC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentME.overlay")) {
            fabricated_cmd.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary android:color/holo_blue_light 0x1c " + ICONIFY_COLOR_ACCENT_PRIMARY + "\n");
            fabricated_cmd.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary\n");
            fabricated_cmd.append("cmd overlay fabricate --target android --name IconifyComponentcolorPixelBackgroundDark android:color/holo_blue_dark 0x1c " + ICONIFY_COLOR_PIXEL_DARK_BG + "\n");
            fabricated_cmd.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorPixelBackgroundDark\n");
        }

        if (!secondaryColorEnabled && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMAC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMGC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentME.overlay")) {
            fabricated_cmd.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondary android:color/holo_green_light 0x1c " + ICONIFY_COLOR_ACCENT_SECONDARY + "\n");
            fabricated_cmd.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentSecondary\n");
        }

        String service_sh = "MODDIR=${0%%/*}\n\n" +
                "while [ \"$(getprop sys.boot_completed | tr -d '\\r')\" != \"1\" ]\n" +
                "do\n" +
                " sleep 1\n" +
                "done\n" +
                "sleep 10\n\n" +
                "qspb=$(cmd overlay list |  grep -E '^.x..IconifyComponentQSPB.overlay' | sed -E 's/^.x..//')\n" +
                "dm=$(cmd overlay list |  grep -E '^.x..IconifyComponentDM.overlay' | sed -E 's/^.x..//')\n" +
                "if ([ ! -z \"$qspb\" ] && [ -z \"$dm\" ])\n" +
                "then\n" +
                " cmd overlay disable --user current IconifyComponentQSPB.overlay\n" +
                " cmd overlay enable --user current IconifyComponentQSPB.overlay\n" +
                " cmd overlay set-priority IconifyComponentQSPB.overlay highest\n" +
                "fi\n\n";

        service_sh += fabricated_cmd;
        service_sh += "\n";

        Shell.cmd("printf '" + service_sh + "' > " + References.MODULE_DIR + "/service.sh").exec();
        Shell.cmd("touch " + References.MODULE_DIR + "/common/system.prop").exec();
        Shell.cmd("mkdir -p " + References.MODULE_DIR + "/tools").exec();
        Shell.cmd("mkdir -p " + References.MODULE_DIR + "/system").exec();
        Shell.cmd("mkdir -p " + References.MODULE_DIR + "/system/product").exec();
        Shell.cmd("mkdir -p " + References.MODULE_DIR + "/system/product/overlay").exec();
        Log.d("ModuleCheck", "Magisk module successfully created!");

        extractTools();
        extractPregeneratedOverlays();
        return OverlayCompilerUtil.buildOverlays();
    }

    public static boolean moduleExists() {
        return RootUtil.folderExists(References.MODULE_DIR);
    }

    static void extractTools() {
        String[] supported_abis = Build.SUPPORTED_ABIS;
        boolean isArm64 = false;
        for (String abi : supported_abis) {
            if (abi.contains("arm64")) {
                isArm64 = true;
                break;
            }
        }

        String folderName;
        if (isArm64)
            folderName = "arm64-v8a";
        else
            folderName = "armeabi-v7a";

        try {
            FileUtil.copyAssets("Tools");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Shell.cmd("cp -a " + References.DATA_DIR + "/Tools/" + folderName + "/. " + References.MODULE_DIR + "/tools").exec();
            Shell.cmd("cp " + References.DATA_DIR + "/Tools/zip " + References.MODULE_DIR + "/tools").exec();
            FileUtil.cleanDir("Tools");
            RootUtil.setPermissionsRecursively(755, References.MODULE_DIR + "/tools");
        }
    }

    static void extractPregeneratedOverlays() {
        try {
            FileUtil.copyAssets("PregeneratedOverlays");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Shell.cmd("cp -a " + References.DATA_DIR + "/PregeneratedOverlays/. " + References.OVERLAY_DIR).exec();
            FileUtil.cleanDir("PregeneratedOverlays");
            RootUtil.setPermissionsRecursively(644, References.OVERLAY_DIR + '/');
        }
    }
}
