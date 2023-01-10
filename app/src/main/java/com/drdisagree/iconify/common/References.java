package com.drdisagree.iconify.common;

import android.os.Environment;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.topjohnwu.superuser.Shell;

import java.util.List;

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

    // List of overlays enabled
    public static List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
    public static List<String> FabricatedEnabledOverlays = FabricatedOverlayUtil.getEnabledOverlayList();
}
