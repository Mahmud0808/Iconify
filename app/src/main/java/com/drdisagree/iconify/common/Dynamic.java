package com.drdisagree.iconify.common;

import static com.drdisagree.iconify.common.Resources.BIN_DIR;

import com.drdisagree.iconify.Iconify;
import com.topjohnwu.superuser.Shell;

import java.io.File;

public class Dynamic {

    // Grab number of overlays dynamically for each variant
    public static final int TOTAL_BRIGHTNESSBARS = (Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentBBN' | sed -E 's/^....//'").exec().getOut()).size();
    public static final int TOTAL_BRIGHTNESSBARSPIXEL = (Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentBBP' | sed -E 's/^....//'").exec().getOut()).size();
    public static final int TOTAL_ICONPACKS = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentIPAS' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_NOTIFICATIONS = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentNFN' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_NOTIFICATIONSPIXEL = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentNFP' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_QSSHAPES = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentQSSN' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_QSSHAPESPIXEL = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentQSSP' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_SETTINGSICONPACKS = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentSIP' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_ICONSHAPES = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentSIS' | sed -E 's/^....//'").exec().getOut().size();

    // Overlay compiler tools
    public static final String NATIVE_LIBRARY_DIR = Iconify.getAppContext().getApplicationInfo().nativeLibraryDir;
    public static final File AAPTLIB = new File(NATIVE_LIBRARY_DIR, "libaapt.so");
    public static final File AAPT = new File(BIN_DIR, "aapt");
    public static final File ZIPALIGNLIB = new File(NATIVE_LIBRARY_DIR, "libzipalign.so");
    public static final File ZIPALIGN = new File(BIN_DIR, "zipalign");
    public static final File ZIP = new File(BIN_DIR, "zip");
}
