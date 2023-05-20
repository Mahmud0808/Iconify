package com.drdisagree.iconify.xposed.utils;

/* Modified from AOSPMods
 * https://github.com/siavash79/AOSPMods/blob/canary/app/src/main/java/sh/siava/AOSPMods/utils/SettingsLibUtils.java
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

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import android.content.Context;
import android.content.res.ColorStateList;

public class SettingsLibUtils {
    private static Class<?> UtilsClass = null;

    public static void init(ClassLoader classLoader) {
        if (UtilsClass == null)
            UtilsClass = findClass("com.android.settingslib.Utils", classLoader);
    }

    public static ColorStateList getColorAttr(int resID, Context context) {
        if (UtilsClass == null) return null;

        try {
            return (ColorStateList) callStaticMethod(UtilsClass, "getColorAttr", resID, context);
        } catch (Throwable t) {
            return (ColorStateList) callStaticMethod(UtilsClass, "getColorAttr", context, resID);
        }
    }

    public static int getColorAttrDefaultColor(int resID, Context context) {
        if (UtilsClass == null) return 0;

        try { // A13 QPR2
            return (int) callStaticMethod(UtilsClass, "getColorAttrDefaultColor", context, resID, 0);
        } catch (Throwable t) { // A13 QPR1
            try {
                return (int) callStaticMethod(UtilsClass, "getColorAttrDefaultColor", resID, context);
            } catch (Throwable th) { // Custom roms
                return (int) callStaticMethod(UtilsClass, "getColorAttrDefaultColor", context, resID);
            }
        }
    }
}