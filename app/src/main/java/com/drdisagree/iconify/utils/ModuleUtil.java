package com.drdisagree.iconify.utils;

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
            if (item.getValue() instanceof Boolean && ((Boolean) item.getValue()) && item.getKey().contains("fabricated")) {
                fabricated_cmd.append(FabricatedOverlayUtil.buildCommand(
                        Prefs.getString("FOCMDtarget" + item.getKey().replace("fabricated", "")),
                        Prefs.getString("FOCMDname" + item.getKey().replace("fabricated", "")),
                        Prefs.getString("FOCMDtype" + item.getKey().replace("fabricated", "")),
                        Prefs.getString("FOCMDresourceName" + item.getKey().replace("fabricated", "")),
                        Prefs.getString("FOCMDval" + item.getKey().replace("fabricated", ""))));
                if (item.getKey().contains("colorAccentPrimary"))
                    primaryColorEnabled = true;
                else if (item.getKey().contains("colorAccentSecondary"))
                    secondaryColorEnabled = true;
            }
        }

        if (!primaryColorEnabled && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMAC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMGC.overlay")) {
            fabricated_cmd.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary android:color/holo_blue_light 0x1c 0xFF50A6D7\n");
            fabricated_cmd.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary\n");
            fabricated_cmd.append("cmd overlay fabricate --target android --name IconifyComponentcolorPixelBackgroundDark android:color/holo_blue_dark 0x1c 0xFF122530\n");
            fabricated_cmd.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorPixelBackgroundDark\n");
        }

        if (!secondaryColorEnabled && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMAC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMGC.overlay")) {
            fabricated_cmd.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondary android:color/holo_green_light 0x1c 0xFF387BFF\n");
            fabricated_cmd.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentSecondary\n");
        }

        String service_sh = "MODDIR=${0%%/*}\n\n" +
                "while [ \"$(getprop sys.boot_completed | tr -d '\\r')\" != \"1\" ]\n" +
                "do\n" +
                " sleep 1\n" +
                "done\n" +
                "sleep 15\n\n" +
                "qspb=$(cmd overlay list |  grep -E '^.x..IconifyComponentQSPB.overlay' | sed -E 's/^.x..//')\n" +
                "if [ -z \"$qspb\" ]\n" +
                "then\n" +
                " :\n" +
                "else\n" +
                " cmd overlay disable --user current IconifyComponentQSPB.overlay\n" +
                " cmd overlay enable --user current IconifyComponentQSPB.overlay\n" +
                " cmd overlay set-priority IconifyComponentQSPB.overlay highest\n" +
                "fi\n\n" +
                "sleep 5\n\n" +
                "me=$(cmd overlay list |  grep -E '^.x..IconifyComponentME.overlay' | sed -E 's/^.x..//')\n" +
                "if [ -z \"$me\" ]\n" +
                "then\n" +
                " :\n" +
                "else\n" +
                " cmd overlay disable --user current IconifyComponentME.overlay\n" +
                " cmd overlay enable --user current IconifyComponentME.overlay\n" +
                " cmd overlay set-priority IconifyComponentME.overlay highest\n" +
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
        return CompilerUtil.buildOverlays();
    }

    public static boolean moduleExists() {
        List<String> lines = Shell.cmd("test -d " + References.MODULE_DIR + " && echo '1'").exec().getOut();
        for (String line : lines) {
            if (line.contains("1"))
                return true;
        }
        return false;
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
