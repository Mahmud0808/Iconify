package com.drdisagree.iconify.utils.overlay.manager;

import com.topjohnwu.superuser.Shell;

public class QsResourceManager {

    public static void removeQuickSettingsStyles(String source, String name) {
        String replaceStart = "<style name=\"Theme.SystemUI.QuickSettings\"";
        String replaceEnd = "<\\/style>";
        String replacement = "<color name=\"dummy_color_iconify\">#00000000<\\/color>";

        String command1 = "sed -i -E '/" + replaceStart + "/,/" + replaceEnd + "/ c\\" +
                replacement + "' '" + source + "/res/values/iconify.xml'";
        String command2 = "sed -i -E '/" + replaceStart + "/,/" + replaceEnd + "/ c\\" +
                replacement + "' '" + source + "/res/values-night/iconify.xml'";

        Shell.cmd(command1, command2).exec();
    }
}
