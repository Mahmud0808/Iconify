package com.drdisagree.iconify.utils.overlay.manager

import com.drdisagree.iconify.common.Dynamic.TOTAL_NOTIFICATIONSPIXEL
import com.drdisagree.iconify.common.Dynamic.isAtleastA14
import com.drdisagree.iconify.common.Dynamic.isSecurityPatchBeforeJune2024
import com.drdisagree.iconify.common.Preferences.FIX_NOTIFICATION_COLOR
import com.drdisagree.iconify.config.Prefs
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.utils.SystemUtil
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

        if (isAtleastA14 &&
            !RPrefs.getBoolean(FIX_NOTIFICATION_COLOR, false) &&
            isSecurityPatchBeforeJune2024()
        ) {
            RPrefs.putBoolean(FIX_NOTIFICATION_COLOR, true)
            SystemUtil.restartSystemUI()
        }
    }

    fun disableOverlay(n: Int) {
        disableOverlay("IconifyComponentNFP$n.overlay")

        if (isAtleastA14 &&
            RPrefs.getBoolean(FIX_NOTIFICATION_COLOR, false) &&
            isSecurityPatchBeforeJune2024()
        ) {
            RPrefs.putBoolean(FIX_NOTIFICATION_COLOR, false)
            SystemUtil.restartSystemUI()
        }
    }

    private fun disableOthers(n: Int) {
        for (i in 1..TOTAL_NOTIFICATIONSPIXEL) {
            Prefs.putBoolean("IconifyComponentNFP$i.overlay", i == n)
            Prefs.putBoolean("IconifyComponentNFN$i.overlay", false)
        }
    }
}