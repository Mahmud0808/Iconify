package com.drdisagree.iconify.services

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.MONET_ENGINE_SWITCH
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.utils.overlay.OverlayUtils


class TileMonetEngine : TileService() {

    private var isCustomMonetEnabled = getBoolean("IconifyComponentME.overlay")

    override fun onStartListening() {
        super.onStartListening()

        val tile = qsTile
        tile.state = if (isCustomMonetEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()

        if (isCustomMonetEnabled) {
            OverlayUtils.disableOverlays("IconifyComponentDM.overlay", "IconifyComponentME.overlay")
        } else {
            OverlayUtils.enableOverlays("IconifyComponentDM.overlay", "IconifyComponentME.overlay")

            if (getBoolean("IconifyComponentQSPBD.overlay")) {
                OverlayUtils.changeOverlayState(
                    "IconifyComponentQSPBD.overlay",
                    false,
                    "IconifyComponentQSPBD.overlay",
                    true
                )
            } else if (getBoolean("IconifyComponentQSPBA.overlay")) {
                OverlayUtils.changeOverlayState(
                    "IconifyComponentQSPBA.overlay",
                    false,
                    "IconifyComponentQSPBA.overlay",
                    true
                )
            }
        }

        isCustomMonetEnabled = !isCustomMonetEnabled
        putBoolean(MONET_ENGINE_SWITCH, isCustomMonetEnabled)

        val tile = qsTile
        tile.state = if (isCustomMonetEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = resources.getString(R.string.activity_title_monet_engine)
        tile.subtitle =
            if (isCustomMonetEnabled) resources.getString(R.string.general_on)
            else resources.getString(R.string.general_off)

        tile.updateTile()
    }
}
