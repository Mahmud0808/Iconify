package com.drdisagree.iconify.utils.overlay.manager

import com.topjohnwu.superuser.Shell

object QsResourceManager {

    fun removeQuickSettingsStyles(source: String) {
        val replaceStart = "<style name=\"Theme.SystemUI.QuickSettings\""
        val replacement = "<color name=\"dummy_color_iconify\">#00000000<\\/color>"
        val replaceEnd = "<\\/style>"

        val command1 = "sed -i -E '/" + replaceStart + "/,/" + replaceEnd + "/ c\\" +
                replacement + "' '" + source + "/res/values/iconify.xml'"
        val command2 = "sed -i -E '/" + replaceStart + "/,/" + replaceEnd + "/ c\\" +
                replacement + "' '" + source + "/res/values-night/iconify.xml'"

        Shell.cmd(command1, command2).exec()
    }
}
