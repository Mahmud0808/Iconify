package com.drdisagree.iconify.services;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

public class TileNotchBarKiller extends TileService {

    private boolean isNotchBarKillerEnabled = Prefs.getBoolean("IconifyComponentNBK.overlay");

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile notchBarKillerTile = getQsTile();
        notchBarKillerTile.setState(isNotchBarKillerEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        notchBarKillerTile.updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();

        if (isNotchBarKillerEnabled) {
            OverlayUtil.disableOverlay("IconifyComponentNBK.overlay");
        } else {
            OverlayUtil.enableOverlay("IconifyComponentNBK.overlay");
        }

        isNotchBarKillerEnabled = !isNotchBarKillerEnabled;

        Tile pitchBlackTile = getQsTile();
        pitchBlackTile.setState(isNotchBarKillerEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        pitchBlackTile.setLabel(getResources().getString(R.string.notch_bar_killer_title));
        pitchBlackTile.setContentDescription(isNotchBarKillerEnabled ? getResources().getString(R.string.general_on) : getResources().getString(R.string.general_off));
        pitchBlackTile.updateTile();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }
}
