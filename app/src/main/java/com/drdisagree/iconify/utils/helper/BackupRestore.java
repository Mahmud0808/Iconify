package com.drdisagree.iconify.utils.helper;

import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.RootUtil;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Objects;

public class BackupRestore {

    public static void backupFiles() {
        // Create backup directory
        Shell.cmd("rm -rf " + Resources.BACKUP_DIR, "mkdir -p " + Resources.BACKUP_DIR).exec();

        backupFile(Resources.MODULE_DIR + "/system.prop");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentME.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentCR1.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentCR2.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentSIS.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentSIP1.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentSIP2.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentSIP3.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentPGB.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentSWITCH1.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentSWITCH2.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentDynamic1.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentDynamic2.apk");
    }

    public static void restoreFiles() {
        restoreFile("system.prop", Resources.TEMP_MODULE_DIR);
        restoreFile("IconifyComponentME.apk", Resources.TEMP_MODULE_OVERLAY_DIR);
        restoreFile("IconifyComponentCR1.apk", Resources.TEMP_MODULE_OVERLAY_DIR);
        restoreFile("IconifyComponentCR2.apk", Resources.TEMP_MODULE_OVERLAY_DIR);
        restoreFile("IconifyComponentSIS.apk", Resources.TEMP_MODULE_OVERLAY_DIR);
        restoreFile("IconifyComponentSIP1.apk", Resources.TEMP_MODULE_OVERLAY_DIR);
        restoreFile("IconifyComponentSIP2.apk", Resources.TEMP_MODULE_OVERLAY_DIR);
        restoreFile("IconifyComponentSIP3.apk", Resources.TEMP_MODULE_OVERLAY_DIR);
        restoreFile("IconifyComponentPGB.apk", Resources.TEMP_MODULE_OVERLAY_DIR);
        restoreFile("IconifyComponentSWITCH1.apk", Resources.TEMP_MODULE_OVERLAY_DIR);
        restoreFile("IconifyComponentSWITCH2.apk", Resources.TEMP_MODULE_OVERLAY_DIR);
        restoreFile("IconifyComponentDynamic1.apk", Resources.TEMP_MODULE_OVERLAY_DIR);
        restoreFile("IconifyComponentDynamic2.apk", Resources.TEMP_MODULE_OVERLAY_DIR);
        restoreBlurSettings();

        // Remove backup directory
        Shell.cmd("rm -rf " + Resources.BACKUP_DIR).exec();
    }

    private static boolean backupExists(String fileName) {
        return RootUtil.fileExists(Resources.BACKUP_DIR + "/" + fileName);
    }

    private static void backupFile(String source) {
        if (RootUtil.fileExists(source))
            Shell.cmd("cp -rf " + source + " " + Resources.BACKUP_DIR + "/").exec();
    }

    private static void restoreFile(String fileName, String dest) {
        if (backupExists(fileName)) {
            Shell.cmd("rm -rf " + dest + "/" + fileName).exec();
            Shell.cmd("cp -rf " + Resources.BACKUP_DIR + "/" + fileName + " " + dest + "/").exec();
        }
    }

    private static void restoreBlurSettings() {
        if (isBlurEnabled()) {
            enableBlur();
        }
    }

    public static boolean isBlurEnabled() {
        List<String> outs = Shell.cmd("if grep -q \"ro.surface_flinger.supports_background_blur=1\" " + Resources.TEMP_MODULE_DIR + "/system.prop; then echo yes; else echo no; fi").exec().getOut();
        return Objects.equals(outs.get(0), "yes");
    }

    public static void disableBlur() {
        Shell.cmd("mv " + Resources.TEMP_MODULE_DIR + "/system.prop " + Resources.TEMP_MODULE_DIR + "/system.txt; grep -v \"ro.surface_flinger.supports_background_blur\" " + Resources.TEMP_MODULE_DIR + "/system.txt > " + Resources.TEMP_MODULE_DIR + "/system.txt.tmp; rm -rf " + Resources.TEMP_MODULE_DIR + "/system.prop; mv " + Resources.TEMP_MODULE_DIR + "/system.txt.tmp " + Resources.TEMP_MODULE_DIR + "/system.prop; rm -rf " + Resources.TEMP_MODULE_DIR + "/system.txt; rm -rf " + Resources.TEMP_MODULE_DIR + "/system.txt.tmp").exec();
        Shell.cmd("grep -v \"ro.surface_flinger.supports_background_blur\" " + Resources.TEMP_MODULE_DIR + "/service.sh > " + Resources.TEMP_MODULE_DIR + "/service.sh.tmp && mv " + Resources.TEMP_MODULE_DIR + "/service.sh.tmp " + Resources.TEMP_MODULE_DIR + "/service.sh").exec();
    }

    public static void enableBlur() {
        disableBlur();

        String blur_cmd1 = "ro.surface_flinger.supports_background_blur=1";
        String blur_cmd2 = "resetprop ro.surface_flinger.supports_background_blur 1 && killall surfaceflinger";

        Shell.cmd("echo \"" + blur_cmd1 + "\" >> " + Resources.TEMP_MODULE_DIR + "/system.prop").exec();
        Shell.cmd("sed '/*}/a " + blur_cmd2 + "' " + Resources.TEMP_MODULE_DIR + "/service.sh > " + Resources.TEMP_MODULE_DIR + "/service.sh.tmp && mv " + Resources.TEMP_MODULE_DIR + "/service.sh.tmp " + Resources.TEMP_MODULE_DIR + "/service.sh").exec();
    }
}
