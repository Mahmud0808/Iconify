package com.drdisagree.iconify.common;

import android.os.Environment;

import com.drdisagree.iconify.Iconify;
import com.topjohnwu.superuser.Shell;

public class References {

    // Grab number of overlays dynamically for each variant
    public static final int TOTAL_BRIGHTNESSBARS = (Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentBBN' | sed -E 's/^....//'").exec().getOut()).size();
    public static final int TOTAL_BRIGHTNESSBARSPIXEL = (Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentBBP' | sed -E 's/^....//'").exec().getOut()).size();
    public static final int TOTAL_ICONPACKS = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentIPAS' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_NOTIFICATIONS = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentNF' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_QSSHAPES = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentQSSN' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_QSSHAPESPIXEL = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentQSSP' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_RADIUS = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentCR' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_TEXTSIZE = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentTextSize' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_ICONSIZE = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentIconSize' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_MOVEICON = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentMoveIcon' | sed -E 's/^....//'").exec().getOut().size();

    // Storage location
    public static final String MODULE_DIR = "/data/adb/modules/Iconify";
    public static final String DATA_DIR = Iconify.getAppContext().getFilesDir().toString();
    public static final String OVERLAY_DIR = MODULE_DIR + "/system/product/overlay";
    public static final String TEMP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.iconify";
    public static final String UNSIGNED_UNALIGNED_DIR = TEMP_DIR + "/overlays/unsigned_unaligned";
    public static final String UNSIGNED_DIR = TEMP_DIR + "/overlays/unsigned";
    public static final String SIGNED_DIR = TEMP_DIR + "/overlays/signed";

    // Notification service checker
    public static boolean isNotificationServiceRunning = false;

    // System packages
    public static final String SYSTEM_UI_PACKAGE = "com.android.systemui";
    public static final String FRAMEWORK_PACKAGE = "android";

    // Preference files
    public static final String SharedPref = Iconify.getAppContext().getPackageName();
    public static final String SharedXPref = Iconify.getAppContext().getPackageName() + "_xpreference";

    // Xposed mods
    public static final String QSTRANSPARENCY_SWITCH = "xposed_qstransparency";
    public static final String QSALPHA_LEVEL = "xposed_qsalpha";
    public static final String QSBLUR_SWITCH = "xposed_qsblur";
    public static final String QSBLUR_RADIUS = "xposed_qsblurradius";
    public static final String STATUSBAR_CLOCKBG = "xposed_qsclockbg";
}
