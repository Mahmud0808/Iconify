package com.drdisagree.iconify.xposed;

/* Modified from AOSPMods
 * https://github.com/siavash79/AOSPMods/blob/canary/app/src/main/java/sh/siava/AOSPMods/modpacks/ModPacks.java
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/].
 */

import static com.drdisagree.iconify.common.Const.PIXEL_LAUNCHER_PACKAGE;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;

import android.os.Build;

import com.drdisagree.iconify.xposed.mods.BackgroundChip;
import com.drdisagree.iconify.xposed.mods.BatteryStyleManager;
import com.drdisagree.iconify.xposed.mods.DepthWallpaper;
import com.drdisagree.iconify.xposed.mods.HeaderClock;
import com.drdisagree.iconify.xposed.mods.HeaderImage;
import com.drdisagree.iconify.xposed.mods.HookCheck;
import com.drdisagree.iconify.xposed.mods.IconUpdater;
import com.drdisagree.iconify.xposed.mods.LockscreenClock;
import com.drdisagree.iconify.xposed.mods.Miscellaneous;
import com.drdisagree.iconify.xposed.mods.QSBlackTheme;
import com.drdisagree.iconify.xposed.mods.QSFluidTheme;
import com.drdisagree.iconify.xposed.mods.QSLightTheme;
import com.drdisagree.iconify.xposed.mods.QSLightThemeA12;
import com.drdisagree.iconify.xposed.mods.QSTransparency;
import com.drdisagree.iconify.xposed.mods.QuickSettings;
import com.drdisagree.iconify.xposed.utils.SettingsLibUtils;

import java.util.ArrayList;

public class EntryList {

    public static ArrayList<Class<? extends ModPack>> getEntries(String packageName) {
        ArrayList<Class<? extends ModPack>> modPacks = new ArrayList<>();

        modPacks.add(SettingsLibUtils.class);
        modPacks.add(HookCheck.class);

        switch (packageName) {
            case SYSTEMUI_PACKAGE -> {
                if (!HookEntry.isChildProcess) {
                    modPacks.add(BackgroundChip.class);
                    modPacks.add(HeaderClock.class);
                    modPacks.add(HeaderImage.class);
                    modPacks.add(DepthWallpaper.class);
                    modPacks.add(LockscreenClock.class);
                    modPacks.add(Miscellaneous.class);
                    modPacks.add(QSBlackTheme.class);
                    modPacks.add(QSFluidTheme.class);
                    modPacks.add(QSTransparency.class);
                    modPacks.add(QuickSettings.class);
                    modPacks.add(BatteryStyleManager.class);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        modPacks.add(QSLightTheme.class);
                    }

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        modPacks.add(QSLightThemeA12.class);
                    }
                }
            }
            case PIXEL_LAUNCHER_PACKAGE -> modPacks.add(IconUpdater.class);
        }

        return modPacks;
    }
}
