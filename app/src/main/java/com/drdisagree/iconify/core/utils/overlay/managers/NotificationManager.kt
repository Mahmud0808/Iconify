package com.drdisagree.iconify.core.utils.overlay.managers

import com.drdisagree.iconify.core.utils.SystemUtils
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils.disableOverlay
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils.enableOverlayExclusiveInCategory
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils.enableOverlays
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils.isOverlayEnabled
import com.drdisagree.iconify.data.common.Dynamic.TOTAL_NOTIFICATIONS
import com.drdisagree.iconify.data.common.Dynamic.isAtleastA14
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.data.keys.XposedKey

object NotificationManager {

    fun enableOverlay(n: Int) {
        disableOthers(n)
        enableOverlayExclusiveInCategory("IconifyComponentNFN$n.overlay")

        if (!isOverlayEnabled("IconifyComponentCR1.overlay") || !isOverlayEnabled("IconifyComponentCR2.overlay")) {
            enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay")
        }

        if (isAtleastA14) {
            var requireReload = false

            if (!RPrefs.getBoolean(XposedKey.FIX_NOTIFICATION_COLOR)) {
                RPrefs.putBoolean(XposedKey.FIX_NOTIFICATION_COLOR, true)
                requireReload = true
            }

            if (!RPrefs.getBoolean(XposedKey.FIX_NOTIFICATION_FOOTER_BUTTON_COLOR)) {
                RPrefs.putBoolean(XposedKey.FIX_NOTIFICATION_FOOTER_BUTTON_COLOR, true)
                requireReload = true
            }

            if (requireReload) {
                SystemUtils.restartSystemUI()
            }
        }
    }

    fun disableOverlay(n: Int) {
        disableOverlay("IconifyComponentNFN$n.overlay")

        if (isAtleastA14) {
            var requireReload = false

            if (RPrefs.getBoolean(XposedKey.FIX_NOTIFICATION_COLOR)) {
                RPrefs.putBoolean(XposedKey.FIX_NOTIFICATION_COLOR, false)
                requireReload = true
            }

            if (RPrefs.getBoolean(XposedKey.FIX_NOTIFICATION_FOOTER_BUTTON_COLOR)) {
                RPrefs.putBoolean(XposedKey.FIX_NOTIFICATION_FOOTER_BUTTON_COLOR, false)
                requireReload = true
            }

            if (requireReload) {
                SystemUtils.restartSystemUI()
            }
        }
    }

    private fun disableOthers(n: Int) {
        for (i in 1..TOTAL_NOTIFICATIONS) {
            RPrefs.putBoolean("IconifyComponentNFN$i.overlay", i == n)
        }
    }
}