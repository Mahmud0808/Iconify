package com.drdisagree.iconify;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.*;

import com.jaredrummler.android.shell.BuildConfig;
import com.jaredrummler.android.shell.Shell;

public class RootUtil {

    private static String magiskDir = null;

    static boolean isDeviceRooted() {
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

    static boolean isMagiskInstalled() {
        return Shell.run("magisk").getStderr().contains("su");
    }

    static String getMagiskDirectory() {
        final int magiskVer = Integer.parseInt(String.valueOf(Shell.run("su -V")));
        if (magiskDir != null)
            return magiskDir;
        if (magiskVer >= 20000) {
            magiskDir = "/data/adb/modules/Iconify";
        } else {
            Log.e("MagiskCheck", "Magisk version cannot be lesser than 20.0");
            magiskDir = "/";
        }
        Log.e("MagiskCheck", String.format("Detected directory %s for version %d", magiskDir, magiskVer));
        return magiskDir;
    }

    public static void setPermissions(final int permission, final String foldername) {
        com.topjohnwu.superuser.Shell.cmd("chmod " + permission + ' ' + foldername).exec();
    }

    public static void setPermissionsRecursively(final int permission, final String foldername) {
        com.topjohnwu.superuser.Shell.cmd("chmod -R " + permission + ' ' + foldername).exec();
    }
}