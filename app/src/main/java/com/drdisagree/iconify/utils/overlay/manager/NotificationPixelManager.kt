package com.drdisagree.iconify.utils.overlay.manager

import com.drdisagree.iconify.common.Dynamic.TOTAL_NOTIFICATIONSPIXEL
import com.drdisagree.iconify.config.Prefs.putBoolean
import com.drdisagree.iconify.utils.overlay.OverlayUtil.disableOverlay
import com.drdisagree.iconify.utils.overlay.OverlayUtil.enableOverlayExclusiveInCategory
import com.drdisagree.iconify.utils.overlay.OverlayUtil.enableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtil.isOverlayEnabled

object NotificationPixelManager {

    fun enableOverlay(n: Int) {
        disableOthers(n)
        enableOverlayExclusiveInCategory("IconifyComponentNFP$n.overlay")

        if (!isOverlayEnabled("IconifyComponentCR1.overlay") || !isOverlayEnabled("IconifyComponentCR2.overlay")) {
            enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay")
        }
    }

    fun disableOverlay(n: Int) {
        disableOverlay("IconifyComponentNFP$n.overlay")
    }

    private fun disableOthers(n: Int) {
        for (i in 1..TOTAL_NOTIFICATIONSPIXEL) {
            putBoolean("IconifyComponentNFP$i.overlay", i == n)
            putBoolean("IconifyComponentNFN$i.overlay", false)
        }
    }
}