package com.drdisagree.iconify;

import android.content.Context;
import android.content.pm.PackageManager;
import com.topjohnwu.superuser.Shell;
import java.util.List;

public class OverlayUtils {
    static boolean isOverlayEnabled(String pkgName) {
        List<String> out = Shell.cmd("cmd overlay list").exec().getOut();
        for (String line : out) {
            if (line.startsWith("[x]") && line.contains(pkgName))
                return true;
        }
        return false;
    }
}
