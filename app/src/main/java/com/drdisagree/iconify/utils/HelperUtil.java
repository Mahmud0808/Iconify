package com.drdisagree.iconify.utils;

import com.drdisagree.iconify.common.References;
import com.topjohnwu.superuser.Shell;

public class HelperUtil {

    public static boolean monetBackupExists() {
        return RootUtil.fileExists(References.BACKUP_DIR + "/IconifyComponentME.apk");
    }

    public static boolean propBackupExists() {
        return RootUtil.fileExists(References.BACKUP_DIR + "/system.prop");
    }

    public static void backupFiles() {
        if (RootUtil.fileExists(References.MODULE_DIR + "/common/system.prop"))
            Shell.cmd("cp -rf " + References.MODULE_DIR + "/common/system.prop " + References.BACKUP_DIR + "/").exec();

        if (RootUtil.fileExists(References.OVERLAY_DIR + "/IconifyComponentME.apk"))
            Shell.cmd("cp -rf " + References.OVERLAY_DIR + "/IconifyComponentME.apk " + References.BACKUP_DIR + "/").exec();
    }

    public static void restoreFiles() {
        if (HelperUtil.monetBackupExists()) {
            Shell.cmd("rm -rf " + References.OVERLAY_DIR + "/IconifyComponentME.apk").exec();
            Shell.cmd("cp -rf " + References.BACKUP_DIR + "/IconifyComponentME.apk " + References.OVERLAY_DIR + "/").exec();
            RootUtil.setPermissions(644, References.OVERLAY_DIR + "/IconifyComponentME.apk");
        }

        if (HelperUtil.propBackupExists()) {
            Shell.cmd("rm -rf " + References.MODULE_DIR + "/common/system.prop").exec();
            Shell.cmd("cp -rf " + References.BACKUP_DIR + "/system.prop " + References.MODULE_DIR + "/common/").exec();
            RootUtil.setPermissions(644, References.MODULE_DIR + "/common/system.prop");
        }
    }
}
