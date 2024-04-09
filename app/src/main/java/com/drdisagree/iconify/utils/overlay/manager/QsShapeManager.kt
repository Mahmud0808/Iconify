package com.drdisagree.iconify.utils.overlay.manager

import com.drdisagree.iconify.common.Dynamic.TOTAL_QSSHAPES
import com.drdisagree.iconify.config.Prefs.putBoolean
import com.drdisagree.iconify.utils.overlay.OverlayUtil.changeOverlayState
import com.drdisagree.iconify.utils.overlay.OverlayUtil.disableOverlay
import com.drdisagree.iconify.utils.overlay.OverlayUtil.enableOverlayExclusiveInCategory
import com.drdisagree.iconify.utils.overlay.OverlayUtil.enableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtil.isOverlayEnabled

object QsShapeManager {

    fun enableOverlay(n: Int) {
        disableOthers(n)
        enableOverlayExclusiveInCategory("IconifyComponentQSSN$n.overlay")

        if (!isOverlayEnabled("IconifyComponentCR1.overlay") || !isOverlayEnabled("IconifyComponentCR2.overlay")) {
            enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay")
        }

        changeOverlayState(
            "IconifyComponentQSNT1.overlay",
            !isOverlayEnabled("IconifyComponentQSNT1.overlay"),
            "IconifyComponentQSNT1.overlay",
            isOverlayEnabled("IconifyComponentQSNT1.overlay"),
            "IconifyComponentQSNT2.overlay",
            !isOverlayEnabled("IconifyComponentQSNT2.overlay"),
            "IconifyComponentQSNT2.overlay",
            isOverlayEnabled("IconifyComponentQSNT2.overlay"),
            "IconifyComponentQSNT3.overlay",
            !isOverlayEnabled("IconifyComponentQSNT3.overlay"),
            "IconifyComponentQSNT3.overlay",
            isOverlayEnabled("IconifyComponentQSNT3.overlay"),
            "IconifyComponentQSNT4.overlay",
            !isOverlayEnabled("IconifyComponentQSNT4.overlay"),
            "IconifyComponentQSNT4.overlay",
            isOverlayEnabled("IconifyComponentQSNT4.overlay")
        )
    }

    fun disableOverlay(n: Int) {
        disableOverlay("IconifyComponentQSSN$n.overlay")
    }

    private fun disableOthers(n: Int) {
        for (i in 1..TOTAL_QSSHAPES) {
            putBoolean("IconifyComponentQSSN$i.overlay", i == n)
            putBoolean("IconifyComponentQSSP$i.overlay", false)
        }
    }
}