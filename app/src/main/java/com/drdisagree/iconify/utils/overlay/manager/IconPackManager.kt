package com.drdisagree.iconify.utils.overlay.manager

import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.utils.overlay.OverlayUtils.disableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlaysExclusiveInCategory
import com.topjohnwu.superuser.Shell

object IconPackManager {
    private fun getTotalIconPacks(category: String): Int {
        return Shell.cmd("cmd overlay list | grep '....IconifyComponent$category'").exec().out.size
    }

    fun enableOverlay(index: Int, vararg categories: String) {
        disableOthers(index, *categories)

        enableOverlaysExclusiveInCategory(
            *categories.map { "IconifyComponent$it$index.overlay" }.toTypedArray()
        )
    }

    fun disableOverlay(index: Int, vararg categories: String) {
        disableOverlays(
            *categories.map { "IconifyComponent$it$index.overlay" }.toTypedArray()
        )
    }

    private fun disableOthers(index: Int, vararg categories: String) {
        val totalIconPacks = getTotalIconPacks(categories[0])

        for (i in 1..totalIconPacks) {
            categories.forEach { category ->
                putBoolean("IconifyComponent$category$i.overlay", i == index)
            }
        }
    }
}
