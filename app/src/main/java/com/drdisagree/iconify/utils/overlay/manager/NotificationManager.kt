package com.drdisagree.iconify.utils.overlay.manager

import com.drdisagree.iconify.common.Dynamic.TOTAL_NOTIFICATIONS
import com.drdisagree.iconify.config.Prefs.putBoolean
import com.drdisagree.iconify.utils.overlay.OverlayUtil.disableOverlay
import com.drdisagree.iconify.utils.overlay.OverlayUtil.enableOverlayExclusiveInCategory
import com.drdisagree.iconify.utils.overlay.OverlayUtil.enableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtil.isOverlayEnabled

object NotificationManager {

    fun enableOverlay(n: Int) {
        disableOthers(n)
        enableOverlayExclusiveInCategory("IconifyComponentNFN$n.overlay")

        if (!isOverlayEnabled("IconifyComponentCR1.overlay") || !isOverlayEnabled("IconifyComponentCR2.overlay")) {
            enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay")
        }
    }

    fun disableOverlay(n: Int) {
        disableOverlay("IconifyComponentNFN$n.overlay")
    }

    private fun disableOthers(n: Int) {
        for (i in 1..TOTAL_NOTIFICATIONS) {
            putBoolean("IconifyComponentNFN$i.overlay", i == n)
            putBoolean("IconifyComponentNFP$i.overlay", false)
        }
    }
}