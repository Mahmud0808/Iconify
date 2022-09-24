package com.drdisagree.iconify.utils;

import android.util.Log;

import com.jaredrummler.android.shell.Shell;

import java.io.DataOutputStream;
import java.io.IOException;

public class RootUtil {

    private static String magiskDir = null;

    public static boolean isDeviceRooted() {
        if (Boolean.TRUE.equals(com.topjohnwu.superuser.Shell.isAppGrantedRoot())) {
            return true;
        } else {
            Process process = null;
            try {
                // Check for root permission
                process = Runtime.getRuntime().exec("su");
                // Try to write on terminal and exit
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("echo \"Checking for root permission.\" >/system/sd/temporary.txt\n");
                os.writeBytes("exit\n");
                os.flush();

                try {
                    process.waitFor();
                    return process.exitValue() == 1;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (process != null) process.destroy();
            }
        }
    }

    public static boolean isMagiskInstalled() {
        return Shell.run("magisk").getStderr().contains("su");
    }

    static String getMagiskDirectory() {
        int magiskVer = 0;
        try {
            magiskVer = Integer.parseInt(String.valueOf(Shell.run("su -V")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (magiskVer < 20000) {
            Log.e("MagiskCheck", "Magisk version cannot be lesser than 20.0");
        }
        magiskDir = "/data/adb/modules/Iconify";
        Log.e("MagiskCheck", "Detected directory " + magiskDir + " for version " + magiskVer);
        return magiskDir;
    }

    public static void setPermissions(final int permission, final String foldername) {
        com.topjohnwu.superuser.Shell.cmd("chmod " + permission + ' ' + foldername).exec();
    }

    public static void setPermissionsRecursively(final int permission, final String foldername) {
        com.topjohnwu.superuser.Shell.cmd("chmod -R " + permission + ' ' + foldername).exec();
    }
}