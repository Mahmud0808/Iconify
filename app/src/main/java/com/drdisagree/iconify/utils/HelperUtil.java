package com.drdisagree.iconify.utils;

import com.drdisagree.iconify.common.References;
import com.topjohnwu.superuser.Shell;

import java.util.List;

public class HelperUtil {

    public static boolean monetBackupExists() {
        return RootUtil.fileExists(References.BACKUP_DIR + "/IconifyComponentME.apk");
    }

    public static void backupFiles() {
        Shell.cmd("cp -rf " + References.OVERLAY_DIR + "/IconifyComponentME.apk " + References.BACKUP_DIR + "/").exec();
    }

    public static void restoreFiles() {
        if (HelperUtil.monetBackupExists()) {
            Shell.cmd("rm -rf " + References.OVERLAY_DIR + "/IconifyComponentME.apk").exec();
            Shell.cmd("cp -rf " + References.BACKUP_DIR + "/IconifyComponentME.apk " + References.OVERLAY_DIR + "/").exec();
            RootUtil.setPermissions(644, References.OVERLAY_DIR + "/IconifyComponentME.apk");
        }
    }
}
