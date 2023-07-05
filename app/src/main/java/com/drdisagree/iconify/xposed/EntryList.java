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

import com.drdisagree.iconify.xposed.mods.BackgroundChip;
import com.drdisagree.iconify.xposed.mods.BatteryStyleManager;
import com.drdisagree.iconify.xposed.mods.HeaderClock;
import com.drdisagree.iconify.xposed.mods.HeaderImage;
import com.drdisagree.iconify.xposed.mods.IconUpdater;
import com.drdisagree.iconify.xposed.mods.LockscreenClock;
import com.drdisagree.iconify.xposed.mods.Miscellaneous;
import com.drdisagree.iconify.xposed.mods.QSBlackTheme;
import com.drdisagree.iconify.xposed.mods.QSFluidTheme;
import com.drdisagree.iconify.xposed.mods.QSLightTheme;
import com.drdisagree.iconify.xposed.mods.QSLightThemeA12;
import com.drdisagree.iconify.xposed.mods.QSTransparency;
import com.drdisagree.iconify.xposed.mods.QuickSettings;

import java.util.ArrayList;

public class EntryList {

    public static ArrayList<Class<?>> getEntries() {
        ArrayList<Class<?>> modPacks = new ArrayList<>();

        modPacks.add(BackgroundChip.class);
        modPacks.add(HeaderClock.class);
        modPacks.add(HeaderImage.class);
        modPacks.add(IconUpdater.class);
        modPacks.add(LockscreenClock.class);
        modPacks.add(Miscellaneous.class);
        modPacks.add(QSTransparency.class);
        modPacks.add(QuickSettings.class);
        modPacks.add(QSLightTheme.class);
        modPacks.add(QSLightThemeA12.class);
        modPacks.add(QSBlackTheme.class);
        modPacks.add(QSFluidTheme.class);
        modPacks.add(BatteryStyleManager.class);

        return modPacks;
    }
}
