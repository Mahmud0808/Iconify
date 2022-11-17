package com.drdisagree.iconify.common;

import com.topjohnwu.superuser.Shell;

public class References {
    public static final int TOTAL_BRIGHTNESSBARS = (Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentBB' | sed -E 's/^....//'").exec().getOut()).size();
    public static final int TOTAL_ICONPACKS = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentIPAS' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_NOTIFICATIONS = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentNF' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_QSSHAPES = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentQSS' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_RADIUS = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentCR' | sed -E 's/^....//'").exec().getOut().size();
}
