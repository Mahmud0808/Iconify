package com.drdisagree.iconify.services

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.common.Preferences.NOTCH_BAR_KILLER_SWITCH
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.utils.SystemUtils
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceEntry
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager

class TileNotchBarKiller : TileService() {

    private var isNotchBarKillerEnabled = getBoolean(NOTCH_BAR_KILLER_SWITCH, false)

    override fun onStartListening() {
        super.onStartListening()

        val tile = qsTile
        tile.state = if (isNotchBarKillerEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()

        if (!SystemUtils.hasStoragePermission()) {
            val tile = qsTile
            tile.subtitle = resources.getString(R.string.need_storage_perm_title)
            tile.updateTile()
            return
        }

        isNotchBarKillerEnabled = !isNotchBarKillerEnabled

        putBoolean(NOTCH_BAR_KILLER_SWITCH, isNotchBarKillerEnabled)

        val tile = qsTile
        tile.state = if (isNotchBarKillerEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = resources.getString(R.string.notch_bar_killer_title)
        tile.subtitle =
            if (isNotchBarKillerEnabled) resources.getString(R.string.general_on) else resources.getString(
                R.string.general_off
            )
        tile.updateTile()

        if (isNotchBarKillerEnabled) {
            ResourceManager.buildOverlayWithResource(
                applicationContext,
                ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "bool",
                    "config_fillMainBuiltInDisplayCutout",
                    "true"
                ),
                ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "string",
                    "config_mainBuiltInDisplayCutout",
                    "M 0,0 L 0, 0 C 0,0 0,0 0,0"
                ),
                ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "string",
                    "config_mainBuiltInDisplayCutoutRectApproximation",
                    "@string/config_mainBuiltInDisplayCutout"
                )
            )
        } else {
            ResourceManager.removeResourceFromOverlay(
                applicationContext,
                ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "bool",
                    "config_fillMainBuiltInDisplayCutout"
                ),
                ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "string",
                    "config_mainBuiltInDisplayCutout"
                ),
                ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "string",
                    "config_mainBuiltInDisplayCutoutRectApproximation"
                )
            )
        }
    }
}
