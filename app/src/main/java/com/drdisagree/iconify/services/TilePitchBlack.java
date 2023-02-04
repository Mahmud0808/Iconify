package com.drdisagree.iconify.services;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.util.List;

public class TilePitchBlack extends TileService {

    public static List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
    private boolean isPitchBlackEnabled = OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentQSPB.overlay");

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
            OverlayUtil.disableOverlay("IconifyComponentQSPB.overlay");
        } else {
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
