package com.drdisagree.iconify.services;

import static com.drdisagree.iconify.common.Preferences.MONET_ENGINE_SWITCH;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.drdisagree.iconify.R;
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
        customMonetTile.setLabel(getResources().getString(R.string.activity_title_monet_engine));
        customMonetTile.setContentDescription(isCustomMonetEnabled ? getResources().getString(R.string.general_on) : getResources().getString(R.string.general_off));
        customMonetTile.updateTile();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }
}
