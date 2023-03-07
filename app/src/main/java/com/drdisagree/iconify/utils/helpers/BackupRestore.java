package com.drdisagree.iconify.utils.helpers;

import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.topjohnwu.superuser.Shell;

public class BackupRestore {

    public static void backupFiles() {
        // Create backup directory
        Shell.cmd("rm -rf " + Resources.BACKUP_DIR, "mkdir -p " + Resources.BACKUP_DIR).exec();

        backupFile(Resources.MODULE_DIR + "/common/system.prop");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentME.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentCR1.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentCR2.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentQSTH.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentSIS.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentSIP1.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentSIP2.apk");
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentSIP3.apk");
    }

    public static void restoreFiles() {
        restoreFile("system.prop", Resources.MODULE_DIR + "/common");
        restoreFile("IconifyComponentME.apk", Resources.OVERLAY_DIR);
        restoreFile("IconifyComponentCR1.apk", Resources.OVERLAY_DIR);
        restoreFile("IconifyComponentCR2.apk", Resources.OVERLAY_DIR);
        restoreFile("IconifyComponentQSTH.apk", Resources.OVERLAY_DIR);
        restoreFile("IconifyComponentSIS.apk", Resources.OVERLAY_DIR);
        restoreFile("IconifyComponentSIP1.apk", Resources.OVERLAY_DIR);
        restoreFile("IconifyComponentSIP2.apk", Resources.OVERLAY_DIR);
        restoreFile("IconifyComponentSIP3.apk", Resources.OVERLAY_DIR);
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
            RootUtil.setPermissions(644, dest + "/" + fileName);
        }
    }

    private static void restoreBlurSettings() {
        if (SystemUtil.isBlurEnabled()) {
            SystemUtil.enableBlur();
        }
    }
}
