package com.drdisagree.iconify.services;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.util.List;

public class TileCustomMonet extends TileService {

    public static List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
    private boolean isCustomMonetEnabled = OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentME.overlay");

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile customMonetTile = getQsTile();
        customMonetTile.setState(isCustomMonetEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        customMonetTile.updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();

        if (isCustomMonetEnabled) {
            OverlayUtil.disableOverlay("IconifyComponentME.overlay");
        } else {
            OverlayUtil.enableOverlay("IconifyComponentME.overlay");
        }

        isCustomMonetEnabled = !isCustomMonetEnabled;
        Prefs.putBoolean("customMonet", isCustomMonetEnabled);

        Tile customMonetTile = getQsTile();
        customMonetTile.setState(isCustomMonetEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        customMonetTile.setLabel("Custom Monet");
        customMonetTile.setContentDescription(isCustomMonetEnabled ? "On" : "Off");
        customMonetTile.updateTile();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }
}
