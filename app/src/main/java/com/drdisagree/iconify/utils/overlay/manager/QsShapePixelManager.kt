package com.drdisagree.iconify.utils.overlay.manager

import com.drdisagree.iconify.common.Dynamic.TOTAL_QSSHAPESPIXEL
import com.drdisagree.iconify.config.Prefs.putBoolean
import com.drdisagree.iconify.utils.overlay.OverlayUtil.changeOverlayState
import com.drdisagree.iconify.utils.overlay.OverlayUtil.disableOverlay
import com.drdisagree.iconify.utils.overlay.OverlayUtil.enableOverlayExclusiveInCategory
import com.drdisagree.iconify.utils.overlay.OverlayUtil.enableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtil.isOverlayEnabled

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
    }

    fun disableOverlay(n: Int) {
        disableOverlay("IconifyComponentQSSP$n.overlay")
    }

    private fun disableOthers(n: Int) {
        for (i in 1..TOTAL_QSSHAPESPIXEL) {
            putBoolean("IconifyComponentQSSP$i.overlay", i == n)
            putBoolean("IconifyComponentQSSN$i.overlay", false)
        }
    }
}