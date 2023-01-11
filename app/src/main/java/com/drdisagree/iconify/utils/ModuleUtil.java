package com.drdisagree.iconify.utils;

import android.graphics.Color;
import android.util.Log;

import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.ui.activity.ColorPicker;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ModuleUtil {

    public static void handleModule() throws IOException {
        if (moduleExists()) {
            Shell.cmd("rm -rf " + References.MODULE_DIR).exec();
        }
        installModule();
    }

    static void installModule() throws IOException {
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

        String primary_colors = "";
        if (!Objects.equals(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentPrimary"), "null")) {
            primary_colors += "cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary android:color/holo_blue_light 0x1c " + ColorPicker.ColorToSpecialHex(Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentPrimary"))) + "\n";
            primary_colors += "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary\n";
            primary_colors += "cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary1 android:color/system_accent1_100 0x1c " + ColorPicker.ColorToSpecialHex(Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentPrimary"))) + "\n";
            primary_colors += "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary1\n";
            primary_colors += "cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary2 android:color/system_accent1_200 0x1c " + ColorPicker.ColorToSpecialHex(Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentPrimary"))) + "\n";
            primary_colors += "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary2\n";
            primary_colors += "cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary3 android:color/system_accent1_300 0x1c " + ColorPicker.ColorToSpecialHex(Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentPrimary"))) + "\n";
            primary_colors += "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary3\n";
            primary_colors += "cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary4 android:color/system_accent2_100 0x1c " + ColorPicker.ColorToSpecialHex(ColorUtils.blendARGB(Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentPrimary")), Color.WHITE, 0.16f)) + "\n";
            primary_colors += "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary4\n";
            primary_colors += "cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary5 android:color/system_accent2_200 0x1c " + ColorPicker.ColorToSpecialHex(ColorUtils.blendARGB(Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentPrimary")), Color.WHITE, 0.16f)) + "\n";
            primary_colors += "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary5\n";
            primary_colors += "cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary6 android:color/system_accent2_300 0x1c " + ColorPicker.ColorToSpecialHex(ColorUtils.blendARGB(Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentPrimary")), Color.WHITE, 0.16f)) + "\n";
            primary_colors += "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary6\n";
            primary_colors += "cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimaryDark android:color/holo_blue_dark 0x1c " + ColorPicker.ColorToSpecialHex(ColorUtils.blendARGB(ColorUtils.blendARGB(Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentPrimary")), Color.BLACK, 0.8f), Color.WHITE, 0.12f)) + "\n";
            primary_colors += "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimaryDark\n";
        } else if (OverlayUtil.isOverlayDisabled(References.EnabledOverlays, "IconifyComponentAMC.overlay")) {
            primary_colors += "cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary android:color/holo_blue_light 0x1c 0xFF50A6D7\n";
            primary_colors += "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary\n";
            primary_colors += "cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimaryDark android:color/holo_blue_dark 0x1c 0xFF122530\n";
            primary_colors += "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimaryDark\n";
        }

        String secondary_colors = "";
        if (!Objects.equals(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentSecondary"), "null")) {
            secondary_colors += "cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondary android:color/holo_green_light 0x1c " + ColorPicker.ColorToSpecialHex(Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentSecondary"))) + "\n";
            secondary_colors += "cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondary android:color/system_accent3_100 0x1c " + ColorPicker.ColorToSpecialHex(Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentSecondary"))) + "\n";
            secondary_colors += "cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondary android:color/system_accent3_200 0x1c " + ColorPicker.ColorToSpecialHex(Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentSecondary"))) + "\n";
            secondary_colors += "cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondary android:color/system_accent3_300 0x1c " + ColorPicker.ColorToSpecialHex(Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentSecondary"))) + "\n";
        } else if (OverlayUtil.isOverlayDisabled(References.EnabledOverlays, "IconifyComponentAMC.overlay")) {
            secondary_colors += "cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondary android:color/holo_green_light 0x1c 0xFF387BFF\n";
            secondary_colors += "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentSecondary\n";
        }

        String service_sh = "MODDIR=${0%%/*}\n\n" +
                "while [ \"$(getprop sys.boot_completed | tr -d '\\r')\" != \"1\" ]\n" +
                "do\n" +
                " sleep 1\n" +
                "done\n" +
                "sleep 5\n\n" +
                "qspb=$(cmd overlay list |  grep -E '^.x..IconifyComponentQSPB.overlay' | sed -E 's/^.x..//')\n" +
                "if [ -z \"$qspb\" ]\n" +
                "then\n" +
                " :\n" +
                "else\n" +
                " cmd overlay disable --user current IconifyComponentQSPB.overlay\n" +
                " cmd overlay enable --user current IconifyComponentQSPB.overlay\n" +
                " cmd overlay set-priority IconifyComponentQSPB.overlay highest\n" +
                "fi\n\n";

        service_sh += primary_colors;
        service_sh += secondary_colors;
        service_sh += "\n";

        Shell.cmd("printf '" + service_sh + "' > " + References.MODULE_DIR + "/service.sh").exec();
        Shell.cmd("touch " + References.MODULE_DIR + "/common/system.prop").exec();
        Shell.cmd("mkdir -p " + References.MODULE_DIR + "/tools").exec();
        Shell.cmd("mkdir -p " + References.MODULE_DIR + "/system").exec();
        Shell.cmd("mkdir -p " + References.MODULE_DIR + "/system/product").exec();
        Shell.cmd("mkdir -p " + References.MODULE_DIR + "/system/product/overlay").exec();
        Log.d("ModuleCheck", "Magisk module successfully created!");

        extractTools();
        CompilerUtil.buildOverlays();
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
        try {
            FileUtil.copyAssets("Tools");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Shell.cmd("cp -a " + References.DATA_DIR + "/Tools/. " + References.MODULE_DIR + "/tools").exec();
            FileUtil.cleanDir("Tools");
            RootUtil.setPermissionsRecursively(755, References.MODULE_DIR + "/tools");
        }
    }
}
