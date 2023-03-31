package com.drdisagree.iconify.utils.helpers;

import static com.drdisagree.iconify.common.Dynamic.AAPT;
import static com.drdisagree.iconify.common.Dynamic.AAPTLIB;
import static com.drdisagree.iconify.common.Dynamic.NATIVE_LIBRARY_DIR;
import static com.drdisagree.iconify.common.Dynamic.ZIPALIGN;
import static com.drdisagree.iconify.common.Dynamic.ZIPALIGNLIB;
import static com.drdisagree.iconify.common.Resources.BIN_DIR;

import android.util.Log;

import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.FileUtil;
import com.topjohnwu.superuser.Shell;

public class BinaryInstaller {

    private static final String TAG = "BinaryInstaller";

    public static void symLinkBinaries() {
        Shell.cmd("mkdir -p " + BIN_DIR).exec();
        extractTools();

        if (AAPT.exists()) AAPT.delete();
        if (ZIPALIGN.exists()) ZIPALIGN.delete();

        Shell.cmd("ln -sf " + AAPTLIB.getAbsolutePath() + ' ' + AAPT.getAbsolutePath()).exec();
        Shell.cmd("ln -sf " + ZIPALIGNLIB.getAbsolutePath() + ' ' + ZIPALIGN.getAbsolutePath()).exec();
    }

    public static void extractTools() {
        Log.d(TAG, "Extracting tools...");
        try {
            FileUtil.copyAssets("Tools");
            Shell.cmd("for fl in " + Resources.DATA_DIR + "/Tools/*; do cp -f \"$fl\" \"" + NATIVE_LIBRARY_DIR + "\"; chmod 755 \"" + NATIVE_LIBRARY_DIR + "/$(basename $fl)\"; ln -sf \"" + NATIVE_LIBRARY_DIR + "/$(basename $fl)\" \"" + BIN_DIR + "/$(basename $fl)\"; done").exec();
            FileUtil.cleanDir("Tools");
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract tools.\n" + e);
        }
    }
}
