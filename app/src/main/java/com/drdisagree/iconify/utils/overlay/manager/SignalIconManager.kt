package com.drdisagree.iconify.utils.overlay.manager

import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.utils.overlay.OverlayUtils
import com.drdisagree.iconify.utils.overlay.OverlayUtils.checkEnabledOverlay
import com.drdisagree.iconify.utils.overlay.OverlayUtils.disableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlaysExclusiveInCategory
import com.topjohnwu.superuser.Shell

object SignalIconManager {
    private fun getTotalIconPacks(category: String): Int {
        return Shell.cmd("cmd overlay list | grep '....IconifyComponent$category'").exec().out.size
    }

    fun enableOverlay(n: Int, category: String) {
        disableOthers(n, category)

        val iconPackPkgName = checkEnabledOverlay("IPAS")
        if (iconPackPkgName.isNotEmpty()) {
            OverlayUtils.disableOverlay(iconPackPkgName)
        }

        enableOverlaysExclusiveInCategory("IconifyComponent$category$n.overlay")

        val otherSignalPack = checkEnabledOverlay(if (category == "WIFI") "SGIC" else "WIFI")
        if (otherSignalPack.isNotEmpty()) {
            enableOverlaysExclusiveInCategory(otherSignalPack)
        }

        if (iconPackPkgName.isNotEmpty()) {
            OverlayUtils.enableOverlay(iconPackPkgName, "high")
        }

        enableOverlaysExclusiveInCategory("IconifyComponent$category$n.overlay")

        if (otherSignalPack.isNotEmpty()) {
            enableOverlaysExclusiveInCategory(otherSignalPack)
        }
    }

    fun disableOverlay(n: Int, category: String) {
        disableOverlays("IconifyComponent$category$n.overlay")
    }

    private fun disableOthers(n: Int, category: String) {
        val totalIconPacks = getTotalIconPacks(category)

        for (i in 1..totalIconPacks) {
            putBoolean("IconifyComponent$category$i.overlay", i == n)
        }
    }
}
