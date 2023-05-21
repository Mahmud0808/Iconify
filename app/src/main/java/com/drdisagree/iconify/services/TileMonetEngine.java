package com.drdisagree.iconify.services;

import static com.drdisagree.iconify.common.Preferences.MONET_ENGINE_SWITCH;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

public class TileMonetEngine extends TileService {

    private boolean isCustomMonetEnabled = Prefs.getBoolean("IconifyComponentME.overlay");

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
            Prefs.putBoolean("IconifyComponentME.overlay", false);
            OverlayUtil.disableOverlays("IconifyComponentDM.overlay", "IconifyComponentME.overlay");
        } else {
            OverlayUtil.enableOverlays("IconifyComponentDM.overlay", "IconifyComponentME.overlay");

            if (Prefs.getBoolean("IconifyComponentQSPBD.overlay")) {
                OverlayUtil.changeOverlayState("IconifyComponentQSPBD.overlay", false, "IconifyComponentQSPBD.overlay", true);
            } else if (Prefs.getBoolean("IconifyComponentQSPBA.overlay")) {
                OverlayUtil.changeOverlayState("IconifyComponentQSPBA.overlay", false, "IconifyComponentQSPBA.overlay", true);
            }
        }

        isCustomMonetEnabled = !isCustomMonetEnabled;
        Prefs.putBoolean(MONET_ENGINE_SWITCH, isCustomMonetEnabled);

        Tile customMonetTile = getQsTile();
        customMonetTile.setState(isCustomMonetEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        customMonetTile.setLabel("Monet Engine");
        customMonetTile.setContentDescription(isCustomMonetEnabled ? "On" : "Off");
        customMonetTile.updateTile();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }
}
