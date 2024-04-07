package com.drdisagree.iconify.services

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.drdisagree.iconify.R
import com.drdisagree.iconify.utils.SystemUtil
import com.drdisagree.iconify.utils.overlay.OverlayUtil

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

        SystemUtil.restartSystemUI()
        val tile = qsTile
        tile.state = Tile.STATE_INACTIVE
        tile.label = resources.getString(R.string.restart_sysui_title)
        tile.subtitle = ""

        updateTileIcon(tile)

        tile.updateTile()
    }

    private fun updateTileIcon(tile: Tile) {
        var iconResId = R.drawable.ic_tile_restart_systemui

        if (OverlayUtil.isOverlayEnabled("IconifyComponentIPAS1.overlay")) {
            iconResId = R.drawable.ic_tile_restart_systemui_aurora
        } else if (OverlayUtil.isOverlayEnabled("IconifyComponentIPAS2.overlay")) {
            iconResId = R.drawable.ic_tile_restart_systemui_gradicon
        } else if (OverlayUtil.isOverlayEnabled("IconifyComponentIPAS3.overlay")) {
            iconResId = R.drawable.ic_tile_restart_systemui_lorn
        } else if (OverlayUtil.isOverlayEnabled("IconifyComponentIPAS4.overlay")) {
            iconResId = R.drawable.ic_tile_restart_systemui_plumpy
        }

        tile.icon = Icon.createWithResource(this, iconResId)
    }
}
