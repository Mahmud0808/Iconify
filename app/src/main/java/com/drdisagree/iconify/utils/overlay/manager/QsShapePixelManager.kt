package com.drdisagree.iconify.utils.overlay.manager

import com.drdisagree.iconify.common.Dynamic.TOTAL_QSSHAPESPIXEL
import com.drdisagree.iconify.common.Dynamic.isAtleastA14
import com.drdisagree.iconify.common.Preferences.FIX_QS_TILE_COLOR
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.utils.SystemUtils
import com.drdisagree.iconify.utils.overlay.OverlayUtils.changeOverlayState
import com.drdisagree.iconify.utils.overlay.OverlayUtils.disableOverlay
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlayExclusiveInCategory
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtils.isOverlayEnabled

object QsShapePixelManager {
    fun enableOverlay(n: Int) {
        disableOthers(n)
        enableOverlayExclusiveInCategory("IconifyComponentQSSP$n.overlay")

        if (!isOverlayEnabled("IconifyComponentCR1.overlay") || !isOverlayEnabled("IconifyComponentCR2.overlay")) {
            enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay")
        }

        changeOverlayState(
            "IconifyComponentQSPT1.overlay",
            !isOverlayEnabled("IconifyComponentQSPT1.overlay"),
            "IconifyComponentQSPT1.overlay",
            isOverlayEnabled("IconifyComponentQSPT1.overlay"),
            "IconifyComponentQSPT2.overlay",
            !isOverlayEnabled("IconifyComponentQSPT2.overlay"),
            "IconifyComponentQSPT2.overlay",
            isOverlayEnabled("IconifyComponentQSPT2.overlay"),
            "IconifyComponentQSPT3.overlay",
            !isOverlayEnabled("IconifyComponentQSPT3.overlay"),
            "IconifyComponentQSPT3.overlay",
            isOverlayEnabled("IconifyComponentQSPT3.overlay"),
            "IconifyComponentQSPT4.overlay",
            !isOverlayEnabled("IconifyComponentQSPT4.overlay"),
            "IconifyComponentQSPT4.overlay",
            isOverlayEnabled("IconifyComponentQSPT4.overlay")
        )

        if (isAtleastA14 && !RPrefs.getBoolean(FIX_QS_TILE_COLOR, false)) {
            RPrefs.putBoolean(FIX_QS_TILE_COLOR, true)
            SystemUtils.restartSystemUI()
        }
    }

    fun disableOverlay(n: Int) {
        disableOverlay("IconifyComponentQSSP$n.overlay")

        if (isAtleastA14 && RPrefs.getBoolean(FIX_QS_TILE_COLOR, false)) {
            RPrefs.putBoolean(FIX_QS_TILE_COLOR, false)
            SystemUtils.restartSystemUI()
        }
    }

    private fun disableOthers(n: Int) {
        for (i in 1..TOTAL_QSSHAPESPIXEL) {
            RPrefs.putBoolean("IconifyComponentQSSP$i.overlay", i == n)
            RPrefs.putBoolean("IconifyComponentQSSN$i.overlay", false)
        }
    }
}