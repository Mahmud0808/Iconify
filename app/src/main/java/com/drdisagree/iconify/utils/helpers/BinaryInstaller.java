package com.drdisagree.iconify.utils.helpers;

import static com.drdisagree.iconify.common.Dynamic.AAPT;
import static com.drdisagree.iconify.common.Dynamic.AAPTLIB;
import static com.drdisagree.iconify.common.Dynamic.ZIPALIGN;
import static com.drdisagree.iconify.common.Dynamic.ZIPALIGNLIB;
import static com.drdisagree.iconify.common.Resources.BIN_DIR;

import com.topjohnwu.superuser.Shell;

public class BinaryInstaller {

    public static void symLinkBinaries() {
        Shell.cmd("mkdir -p " + BIN_DIR).exec();

        if (AAPT.exists()) AAPT.delete();
        if (ZIPALIGN.exists()) ZIPALIGN.delete();

        Shell.cmd("ln -sf " + AAPTLIB.getAbsolutePath() + ' ' + AAPT.getAbsolutePath(), "ln -sf " + ZIPALIGNLIB.getAbsolutePath() + ' ' + ZIPALIGN.getAbsolutePath()).exec();
    }
}
