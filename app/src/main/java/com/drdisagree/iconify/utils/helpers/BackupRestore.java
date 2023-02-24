package com.drdisagree.iconify.utils.helpers;

import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.topjohnwu.superuser.Shell;

public class BackupRestore {

    public static void backupFiles() {
        // Create backup directory
        Shell.cmd("rm -rf " + References.BACKUP_DIR, "mkdir -p " + References.BACKUP_DIR).exec();

        backupFile(References.MODULE_DIR + "/common/system.prop");
        backupFile(References.OVERLAY_DIR + "/IconifyComponentME.apk");
        backupFile(References.OVERLAY_DIR + "/IconifyComponentCR.apk");
        backupFile(References.OVERLAY_DIR + "/IconifyComponentQSTH.apk");
        backupFile(References.OVERLAY_DIR + "/IconifyComponentSIP1.apk");
        backupFile(References.OVERLAY_DIR + "/IconifyComponentSIP2.apk");
        backupFile(References.OVERLAY_DIR + "/IconifyComponentSIP3.apk");
    }

    public static void restoreFiles() {
        restoreFile("system.prop", References.MODULE_DIR + "/common");
        restoreFile("IconifyComponentME.apk", References.OVERLAY_DIR);
        restoreFile("IconifyComponentCR.apk", References.OVERLAY_DIR);
        restoreFile("IconifyComponentQSTH.apk", References.OVERLAY_DIR);
        restoreFile("IconifyComponentSIP1.apk", References.OVERLAY_DIR);
        restoreFile("IconifyComponentSIP2.apk", References.OVERLAY_DIR);
        restoreFile("IconifyComponentSIP3.apk", References.OVERLAY_DIR);
        restoreBlurSettings();

        // Remove backup directory
        Shell.cmd("rm -rf " + References.BACKUP_DIR).exec();
    }

    private static boolean backupExists(String fileName) {
        return RootUtil.fileExists(References.BACKUP_DIR + "/" + fileName);
    }

    private static void backupFile(String source) {
        if (RootUtil.fileExists(source))
            Shell.cmd("cp -rf " + source + " " + References.BACKUP_DIR + "/").exec();
    }

    private static void restoreFile(String fileName, String dest) {
        if (backupExists(fileName)) {
            Shell.cmd("rm -rf " + dest + "/" + fileName).exec();
            Shell.cmd("cp -rf " + References.BACKUP_DIR + "/" + fileName + " " + dest + "/").exec();
            RootUtil.setPermissions(644, dest + "/" + fileName);
        }
    }

    private static void restoreBlurSettings() {
        if (SystemUtil.isBlurEnabled()) {
            SystemUtil.enableBlur();
        }
    }
}
