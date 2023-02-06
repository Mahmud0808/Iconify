package com.drdisagree.iconify.utils;

import com.drdisagree.iconify.common.References;
import com.topjohnwu.superuser.Shell;

import java.util.List;

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

    public static boolean fileExists(String dir) {
        List<String> lines = Shell.cmd("test -f " + dir + " && echo '1'").exec().getOut();
        for (String line : lines) {
            if (line.contains("1"))
                return true;
        }
        return false;
    }

    public static boolean folderExists(String dir) {
        List<String> lines = Shell.cmd("test -d " + dir + " && echo '1'").exec().getOut();
        for (String line : lines) {
            if (line.contains("1"))
                return true;
        }
        return false;
    }
}