package com.drdisagree.iconify.utils;

import com.drdisagree.iconify.common.References;
import com.topjohnwu.superuser.Shell;

public class HelperUtil {

    public static void backupFiles() {
        // Create backup directory
        Shell.cmd("rm -rf " + References.BACKUP_DIR, "mkdir -p " + References.BACKUP_DIR).exec();

        backupFile(References.MODULE_DIR + "/common/system.prop");
        backupFile(References.OVERLAY_DIR + "/IconifyComponentME.apk");
    }

    public static void restoreFiles() {
        restoreFile("IconifyComponentME.apk", References.OVERLAY_DIR);
        restoreFile("system.prop", References.MODULE_DIR + "/common");

        // Remove backup directory
        Shell.cmd("rm -rf " + References.BACKUP_DIR).exec();
    }

    public static boolean backupExists(String fileName) {
        return RootUtil.fileExists(References.BACKUP_DIR + "/" + fileName);
    }

    public static void backupFile(String source) {
        if (RootUtil.fileExists(source))
            Shell.cmd("cp -rf " + source + " " + References.BACKUP_DIR + "/").exec();
    }

    public static void restoreFile(String fileName, String dest) {
        if (HelperUtil.backupExists(fileName)) {
            Shell.cmd("rm -rf " + dest + "/" + fileName).exec();
            Shell.cmd("cp -rf " + References.BACKUP_DIR + "/" + fileName + " " + dest + "/").exec();
            RootUtil.setPermissions(644, dest + "/" + fileName);
        }
    }
}
