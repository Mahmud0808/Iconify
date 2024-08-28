package com.drdisagree.iconify.utils.overlay.manager

import com.drdisagree.iconify.common.Dynamic.TOTAL_NOTIFICATIONSPIXEL
import com.drdisagree.iconify.common.Dynamic.isAndroid14
import com.drdisagree.iconify.common.Dynamic.isAtleastA14
import com.drdisagree.iconify.common.Preferences.FIX_NOTIFICATION_COLOR
import com.drdisagree.iconify.common.Preferences.FIX_NOTIFICATION_FOOTER_BUTTON_COLOR
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.utils.SystemUtils
import com.drdisagree.iconify.utils.overlay.OverlayUtils.disableOverlay
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlayExclusiveInCategory
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtils.isOverlayEnabled
import com.drdisagree.iconify.xposed.modules.utils.SystemUtils.isSecurityPatchBeforeJune2024

object NotificationPixelManager {

    fun enableOverlay(n: Int) {
        disableOthers(n)
        enableOverlayExclusiveInCategory("IconifyComponentNFP$n.overlay")

        if (!isOverlayEnabled("IconifyComponentCR1.overlay") || !isOverlayEnabled("IconifyComponentCR2.overlay")) {
            enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay")
        }

        if (isAtleastA14) {
            var requireReload = false

            if (!RPrefs.getBoolean(FIX_NOTIFICATION_COLOR, false) &&
                isAndroid14 && isSecurityPatchBeforeJune2024()
            ) {
                RPrefs.putBoolean(FIX_NOTIFICATION_COLOR, true)
                requireReload = true
            }

            if (!RPrefs.getBoolean(FIX_NOTIFICATION_FOOTER_BUTTON_COLOR, false)) {
                RPrefs.putBoolean(FIX_NOTIFICATION_FOOTER_BUTTON_COLOR, true)
                requireReload = true
            }

            if (requireReload) {
                SystemUtils.restartSystemUI()
            }
        }
    }

    fun disableOverlay(n: Int) {
        disableOverlay("IconifyComponentNFP$n.overlay")

        if (isAtleastA14) {
            var requireReload = false

            if (RPrefs.getBoolean(FIX_NOTIFICATION_COLOR, false) &&
                isAndroid14 && isSecurityPatchBeforeJune2024()
            ) {
                RPrefs.putBoolean(FIX_NOTIFICATION_COLOR, false)
                requireReload = true
            }

            if (RPrefs.getBoolean(FIX_NOTIFICATION_FOOTER_BUTTON_COLOR, false)) {
                RPrefs.putBoolean(FIX_NOTIFICATION_FOOTER_BUTTON_COLOR, false)
                requireReload = true
            }

            if (requireReload) {
                SystemUtils.restartSystemUI()
            }
        }
    }

    private fun disableOthers(n: Int) {
        for (i in 1..TOTAL_NOTIFICATIONSPIXEL) {
            RPrefs.putBoolean("IconifyComponentNFP$i.overlay", i == n)
            RPrefs.putBoolean("IconifyComponentNFN$i.overlay", false)
        }
    }
}