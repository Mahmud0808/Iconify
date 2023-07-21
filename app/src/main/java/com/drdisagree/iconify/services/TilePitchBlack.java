package com.drdisagree.iconify.services;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

public class TilePitchBlack extends TileService {

    private boolean isPitchBlackEnabled = Prefs.getBoolean("IconifyComponentQSPBD.overlay") || Prefs.getBoolean("IconifyComponentQSPBA.overlay");

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

        if (Prefs.getBoolean("IconifyComponentQSPBD.overlay")) {
            OverlayUtil.changeOverlayState("IconifyComponentQSPBD.overlay", false, "IconifyComponentQSPBA.overlay", true);
            isPitchBlackEnabled = true;
        } else if (Prefs.getBoolean("IconifyComponentQSPBA.overlay")) {
            OverlayUtil.disableOverlay("IconifyComponentQSPBA.overlay");
            isPitchBlackEnabled = false;
        } else {
            OverlayUtil.enableOverlay("IconifyComponentQSPBD.overlay");
            isPitchBlackEnabled = true;
        }

        Tile pitchBlackTile = getQsTile();
        pitchBlackTile.setState(isPitchBlackEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        pitchBlackTile.setLabel(getResources().getString(R.string.tile_pitch_black));
        pitchBlackTile.setSubtitle(Prefs.getBoolean("IconifyComponentQSPBD.overlay") ? getResources().getString(R.string.tile_pitch_black_dark) : (Prefs.getBoolean("IconifyComponentQSPBA.overlay") ? getResources().getString(R.string.tile_pitch_black_amoled) : getResources().getString(R.string.general_off)));
        pitchBlackTile.updateTile();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }
}
