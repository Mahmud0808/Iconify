package com.drdisagree.iconify.services;

import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;

public class TileRestartSystemUI extends TileService {

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        Tile tile = getQsTile();
        tile.setState(Tile.STATE_INACTIVE);
        updateTileIcon(tile);
        tile.updateTile();
    }

    @Override
    public void onStopListening() {
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_INACTIVE);
        updateTileIcon(tile);
        tile.updateTile();

        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();

        SystemUtil.restartSystemUI();
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_INACTIVE);
        tile.setLabel(getResources().getString(R.string.restart_sysui_title));
        tile.setSubtitle("");
        updateTileIcon(tile);
        tile.updateTile();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    private void updateTileIcon(Tile tile) {
        int iconResId = R.drawable.ic_tile_restart_systemui;

        if (OverlayUtil.isOverlayEnabled("IconifyComponentIPAS1.overlay")) {
            iconResId = R.drawable.ic_tile_restart_systemui_aurora;
        } else if (OverlayUtil.isOverlayEnabled("IconifyComponentIPAS2.overlay")) {
            iconResId = R.drawable.ic_tile_restart_systemui_gradicon;
        } else if (OverlayUtil.isOverlayEnabled("IconifyComponentIPAS3.overlay")) {
            iconResId = R.drawable.ic_tile_restart_systemui_lorn;
        } else if (OverlayUtil.isOverlayEnabled("IconifyComponentIPAS4.overlay")) {
            iconResId = R.drawable.ic_tile_restart_systemui_plumpy;
        }

        tile.setIcon(Icon.createWithResource(this, iconResId));
    }
}
