package com.drdisagree.iconify.utils.overlay.manager

import com.drdisagree.iconify.common.Dynamic.TOTAL_ICONPACKS
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.utils.overlay.OverlayUtil.disableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtil.enableOverlaysExclusiveInCategory

object IconPackManager {
    fun enableOverlay(n: Int) {
        disableOthers(n)

        enableOverlaysExclusiveInCategory(
            "IconifyComponentIPAS$n.overlay",
            "IconifyComponentIPSUI$n.overlay"
        )
    }

    fun disableOverlay(n: Int) {
        disableOverlays("IconifyComponentIPAS$n.overlay", "IconifyComponentIPSUI$n.overlay")
    }

    private fun disableOthers(n: Int) {
        for (i in 1..TOTAL_ICONPACKS) {
            putBoolean("IconifyComponentIPAS$i.overlay", i == n)
            putBoolean("IconifyComponentIPSUI$i.overlay", i == n)
        }
    }
}