package com.drdisagree.iconify.services;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.NOTCH_BAR_KILLER_SWITCH;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceEntry;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager;

public class TileNotchBarKiller extends TileService {

    private boolean isNotchBarKillerEnabled = Prefs.getBoolean(NOTCH_BAR_KILLER_SWITCH, false);

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        tile.setState(isNotchBarKillerEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        if (!SystemUtil.hasStoragePermission()) {
            Tile tile = getQsTile();
            tile.setSubtitle(getResources().getString(R.string.need_storage_perm_title));
            tile.updateTile();
            return;
        }

        super.onClick();

        isNotchBarKillerEnabled = !isNotchBarKillerEnabled;

        Prefs.putBoolean(NOTCH_BAR_KILLER_SWITCH, isNotchBarKillerEnabled);

        Tile tile = getQsTile();
        tile.setState(isNotchBarKillerEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel(getResources().getString(R.string.notch_bar_killer_title));
        tile.setSubtitle(isNotchBarKillerEnabled ? getResources().getString(R.string.general_on) : getResources().getString(R.string.general_off));
        tile.updateTile();

        if (isNotchBarKillerEnabled) {
            ResourceManager.buildOverlayWithResource(
                    getApplicationContext(),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "bool", "config_fillMainBuiltInDisplayCutout", "true"),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "string", "config_mainBuiltInDisplayCutout", "M 0,0 L 0, 0 C 0,0 0,0 0,0"),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "string", "config_mainBuiltInDisplayCutoutRectApproximation", "@string/config_mainBuiltInDisplayCutout")
            );
        } else {
            ResourceManager.removeResourceFromOverlay(
                    getApplicationContext(),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "bool", "config_fillMainBuiltInDisplayCutout"),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "string", "config_mainBuiltInDisplayCutout"),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "string", "config_mainBuiltInDisplayCutoutRectApproximation")
            );
        }
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }
}
