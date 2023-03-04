package com.drdisagree.iconify.xposed.mods;

/* Modified from AOSPMods
 * https://github.com/siavash79/AOSPMods/blob/canary/app/src/main/java/sh/siava/AOSPMods/launcher/AOSPModsIconUpdater.java
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
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import android.content.Context;
import android.os.UserHandle;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.xposed.ModPack;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressWarnings("RedundantThrows")
public class IconUpdater extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - IconUpdater: ";

    private static final String listenPackage = PIXEL_LAUNCHER_PACKAGE;
    private Object LauncherModel;

    public IconUpdater(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Class<?> LauncherModelClass = findClass("com.android.launcher3.LauncherModel", lpparam.classLoader);
        Class<?> BaseDraggingActivityClass = findClass("com.android.launcher3.BaseDraggingActivity", lpparam.classLoader);

        hookAllMethods(BaseDraggingActivityClass, "onResume", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    callMethod(LauncherModel, "onAppIconChanged", BuildConfig.APPLICATION_ID, UserHandle.getUserHandleForUid(0));
                } catch (Throwable ignored) {
                }
            }
        });
        hookAllConstructors(LauncherModelClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                LauncherModel = param.thisObject;
            }
        });

    }
}