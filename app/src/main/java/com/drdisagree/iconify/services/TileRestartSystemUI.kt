package com.drdisagree.iconify.services

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.drdisagree.iconify.R
import com.drdisagree.iconify.utils.SystemUtils
import com.drdisagree.iconify.utils.overlay.OverlayUtils

class TileRestartSystemUI : TileService() {

    override fun onStartListening() {
        super.onStartListening()

        val tile = qsTile
        tile.state = Tile.STATE_INACTIVE
        updateTileIcon(tile)
        tile.updateTile()
    }

    override fun onStopListening() {
        val tile = qsTile
        tile.state = Tile.STATE_INACTIVE
        updateTileIcon(tile)
        tile.updateTile()

        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()

        SystemUtils.restartSystemUI()
        val tile = qsTile
        tile.state = Tile.STATE_INACTIVE
        tile.label = resources.getString(R.string.restart_sysui_title)
        tile.subtitle = ""

        updateTileIcon(tile)

        tile.updateTile()
    }

    private fun updateTileIcon(tile: Tile) {
        var iconResId = R.drawable.ic_tile_restart_systemui

        if (OverlayUtils.isOverlayEnabled("IconifyComponentIPAS1.overlay")) {
            iconResId = R.drawable.ic_tile_restart_systemui_aurora
        } else if (OverlayUtils.isOverlayEnabled("IconifyComponentIPAS2.overlay")) {
            iconResId = R.drawable.ic_tile_restart_systemui_gradicon
        } else if (OverlayUtils.isOverlayEnabled("IconifyComponentIPAS3.overlay")) {
            iconResId = R.drawable.ic_tile_restart_systemui_lorn
        } else if (OverlayUtils.isOverlayEnabled("IconifyComponentIPAS4.overlay")) {
            iconResId = R.drawable.ic_tile_restart_systemui_plumpy
        }

        tile.icon = Icon.createWithResource(this, iconResId)
    }
}
