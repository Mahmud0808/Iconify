package com.drdisagree.iconify.utils.overlay.manager

import com.drdisagree.iconify.common.Dynamic.TOTAL_BRIGHTNESSBARS
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.utils.overlay.OverlayUtils.disableOverlay
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlayExclusiveInCategory
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtils.isOverlayEnabled

object BrightnessBarManager {

    fun enableOverlay(n: Int) {
        disableOthers(n)
        enableOverlayExclusiveInCategory("IconifyComponentBBN$n.overlay")

        if (!isOverlayEnabled("IconifyComponentCR1.overlay") || !isOverlayEnabled("IconifyComponentCR2.overlay")) {
            enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay")
        }
    }

    fun disableOverlay(n: Int) {
        disableOverlay("IconifyComponentBBN$n.overlay")
    }

    private fun disableOthers(n: Int) {
        for (i in 1..TOTAL_BRIGHTNESSBARS) {
            putBoolean("IconifyComponentBBN$i.overlay", i == n)
            putBoolean("IconifyComponentBBP$i.overlay", false)
        }
    }
}