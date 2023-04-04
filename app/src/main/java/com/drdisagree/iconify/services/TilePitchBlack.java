package com.drdisagree.iconify.services;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

public class TilePitchBlack extends TileService {

    private boolean isPitchBlackEnabled = Prefs.getBoolean("IconifyComponentQSPB.overlay");

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile pitchBlackTile = getQsTile();
        pitchBlackTile.setState(isPitchBlackEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        pitchBlackTile.updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();

        if (isPitchBlackEnabled) {
            Prefs.putBoolean("IconifyComponentQSPB.overlay", false);
            OverlayUtil.disableOverlay("IconifyComponentQSPB.overlay");
        } else {
            Prefs.putBoolean("IconifyComponentQSPB.overlay", true);
            OverlayUtil.enableOverlay("IconifyComponentQSPB.overlay");
        }

        isPitchBlackEnabled = !isPitchBlackEnabled;

        Tile pitchBlackTile = getQsTile();
        pitchBlackTile.setState(isPitchBlackEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        pitchBlackTile.setLabel("Pitch Black");
        pitchBlackTile.setContentDescription(isPitchBlackEnabled ? "On" : "Off");
        pitchBlackTile.updateTile();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }
}
