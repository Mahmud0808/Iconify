package com.drdisagree.iconify.core.utils.overlay.managers

import com.drdisagree.iconify.data.config.RPrefs.putBoolean
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils.disableOverlay
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils.enableOverlayExclusiveInCategory

object MediaPlayerIconManager {

    fun enableOverlay(m: Int, n: Int) {
        disableOthers(m, n)
        enableOverlayExclusiveInCategory("IconifyComponentMPIP$m$n.overlay")
    }

    fun disableOverlay(m: Int, n: Int) {
        disableOverlay("IconifyComponentMPIP$m$n.overlay")
    }

    private fun disableOthers(m: Int, n: Int) {
        for (i in 1..3) {
            putBoolean("IconifyComponentMPIP$m$i.overlay", i == n)
        }
    }
}