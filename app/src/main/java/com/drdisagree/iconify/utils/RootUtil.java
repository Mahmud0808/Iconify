package com.drdisagree.iconify.utils;

import com.topjohnwu.superuser.Shell;

public class RootUtil {

    public static boolean isDeviceRooted() {
        return Boolean.TRUE.equals(Shell.isAppGrantedRoot());
    }

    public static boolean isMagiskInstalled() {
        return Shell.cmd("[ -d /data/adb/magisk ]").exec().isSuccess();
    }

    public static void setPermissions(final int permission, final String foldername) {
        Shell.cmd("chmod " + permission + ' ' + foldername).exec();
    }

    public static void setPermissionsRecursively(final int permission, final String foldername) {
        Shell.cmd("chmod -R " + permission + ' ' + foldername).exec();
    }
}