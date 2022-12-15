package com.drdisagree.iconify.common;

import com.topjohnwu.superuser.Shell;

public class References {
    public static boolean isNotificationServiceRunning = false;
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
}
