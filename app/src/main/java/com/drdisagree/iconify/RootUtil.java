package com.drdisagree.iconify;

import com.topjohnwu.superuser.Shell;
import java.io.*;

public class RootUtil {

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
        Shell.Result magisk = Shell.cmd("[ -d /data/adb/magisk ]").exec();
        return magisk.isSuccess();
    }
}