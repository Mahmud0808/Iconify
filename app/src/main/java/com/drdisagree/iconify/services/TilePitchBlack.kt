package com.drdisagree.iconify.services

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.drdisagree.iconify.R
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.utils.overlay.OverlayUtils

class TilePitchBlack : TileService() {

    private var isPitchBlackEnabled = getBoolean("IconifyComponentQSPBD.overlay") ||
            getBoolean("IconifyComponentQSPBA.overlay")

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile
        tile.state =
            if (isPitchBlackEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()

        isPitchBlackEnabled = if (getBoolean("IconifyComponentQSPBD.overlay")) {
            OverlayUtils.changeOverlayState(
                "IconifyComponentQSPBD.overlay",
                false,
                "IconifyComponentQSPBA.overlay",
                true
            )
            true
        } else if (getBoolean("IconifyComponentQSPBA.overlay")) {
            OverlayUtils.disableOverlay("IconifyComponentQSPBA.overlay")
            false
        } else {
            OverlayUtils.enableOverlay("IconifyComponentQSPBD.overlay")
            true
        }

        val tile = qsTile
        tile.state = if (isPitchBlackEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = resources.getString(R.string.tile_pitch_black)
        tile.subtitle =
            if (getBoolean("IconifyComponentQSPBD.overlay")) resources.getString(R.string.tile_pitch_black_dark) else if (getBoolean(
                    "IconifyComponentQSPBA.overlay"
                )
            ) resources.getString(R.string.tile_pitch_black_amoled) else resources.getString(R.string.general_off)

        tile.updateTile()
    }
}
