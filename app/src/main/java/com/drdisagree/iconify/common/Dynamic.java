package com.drdisagree.iconify.common;

import static com.drdisagree.iconify.common.Resources.BIN_DIR;

import com.drdisagree.iconify.Iconify;
import com.topjohnwu.superuser.Shell;

import java.io.File;

public class Dynamic {

    // Grab number of overlays dynamically for each variant
    public static final int TOTAL_BRIGHTNESSBARS = (Shell.cmd("cmd overlay list | grep '....IconifyComponentBBN'").exec().getOut()).size();
    public static final int TOTAL_BRIGHTNESSBARSPIXEL = (Shell.cmd("cmd overlay list | grep '....IconifyComponentBBP'").exec().getOut()).size();
    public static final int TOTAL_ICONPACKS = Shell.cmd("cmd overlay list | grep '....IconifyComponentIPAS'").exec().getOut().size();
    public static final int TOTAL_NOTIFICATIONS = Shell.cmd("cmd overlay list | grep '....IconifyComponentNFN'").exec().getOut().size();
    public static final int TOTAL_NOTIFICATIONSPIXEL = Shell.cmd("cmd overlay list | grep '....IconifyComponentNFP'").exec().getOut().size();
    public static final int TOTAL_QSSHAPES = Shell.cmd("cmd overlay list | grep '....IconifyComponentQSSN'").exec().getOut().size();
    public static final int TOTAL_QSSHAPESPIXEL = Shell.cmd("cmd overlay list | grep '....IconifyComponentQSSP'").exec().getOut().size();

    // Overlay compiler tools
    public static final String NATIVE_LIBRARY_DIR = Iconify.getAppContext().getApplicationInfo().nativeLibraryDir;
    public static final File AAPTLIB = new File(NATIVE_LIBRARY_DIR, "libaapt.so");
    public static final File AAPT = new File(BIN_DIR, "aapt");
    public static final File ZIPALIGNLIB = new File(NATIVE_LIBRARY_DIR, "libzipalign.so");
    public static final File ZIPALIGN = new File(BIN_DIR, "zipalign");
    public static final File ZIP = new File(BIN_DIR, "zip");

    // Onboarding overlay installation
    public static boolean skippedInstallation = false;
}
