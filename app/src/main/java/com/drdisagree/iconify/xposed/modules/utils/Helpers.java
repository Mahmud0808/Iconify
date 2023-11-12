package com.drdisagree.iconify.xposed.modules.utils;

/* Modified from AOSPMods
 * https://github.com/siavash79/AOSPMods/blob/canary/app/src/main/java/sh/siava/AOSPMods/utils/Helpers.java
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

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.FORCE_RELOAD_OVERLAY_STATE;
import static com.drdisagree.iconify.common.Preferences.FORCE_RELOAD_PACKAGE_NAME;
import static com.drdisagree.iconify.common.Preferences.RESTART_SYSUI_BEHAVIOR;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.modRes;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.topjohnwu.superuser.Shell;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;

@SuppressWarnings({"unused", "DiscouragedApi"})
public class Helpers {

    public static void forceReloadUI(Context context) {
        boolean forceReload = false;
        boolean state = false;

        try {
            forceReload = Xprefs.getBoolean(RESTART_SYSUI_BEHAVIOR, true);
        } catch (Throwable ignored) {
            forceReload = RPrefs.getBoolean(RESTART_SYSUI_BEHAVIOR, true);
        }

        try {
            state = Xprefs.getBoolean(FORCE_RELOAD_OVERLAY_STATE, false);
        } catch (Throwable ignored) {
            state = RPrefs.getBoolean(FORCE_RELOAD_OVERLAY_STATE, false);
        }

        if (forceReload) {
            boolean finalState = state;
            String pkgName = FORCE_RELOAD_PACKAGE_NAME;
            new Handler(Looper.getMainLooper()).postDelayed(() -> Shell.cmd("cmd overlay " + (finalState ? "disable" : "enable") + " --user current " + pkgName + "; cmd overlay " + (finalState ? "enable" : "disable") + " --user current " + pkgName).submit(), SWITCH_ANIMATION_DELAY);
        } else {
            try {
                Toast.makeText(context, modRes.getString(R.string.settings_systemui_restart_required), Toast.LENGTH_SHORT).show();
            } catch (Throwable ignored) {
                Toast.makeText(Iconify.getAppContext(), Iconify.getAppContext().getResources().getString(R.string.settings_systemui_restart_required), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void enableOverlay(String pkgName) {
        Shell.cmd("cmd overlay enable --user current " + pkgName, "cmd overlay set-priority " + pkgName + " highest").exec();
    }

    public static void disableOverlay(String pkgName) {
        Shell.cmd("cmd overlay disable --user current " + pkgName).exec();
    }

    public static void enableOverlays(String... pkgNames) {
        StringBuilder command = new StringBuilder();

        for (String pkgName : pkgNames) {
            command.append("cmd overlay enable --user current ").append(pkgName).append("; cmd overlay set-priority ").append(pkgName).append(" highest; ");
        }

        Shell.cmd(command.toString().trim()).submit();
    }

    public static void disableOverlays(String... pkgNames) {
        StringBuilder command = new StringBuilder();

        for (String pkgName : pkgNames) {
            command.append("cmd overlay disable --user current ").append(pkgName).append("; ");
        }

        Shell.cmd(command.toString().trim()).submit();
    }

    @NonNull
    public static Class<?> findAndDumpClass(String className, ClassLoader classLoader) {
        dumpClass(className, classLoader);
        return findClass(className, classLoader);
    }

    public static Class<?> findAndDumpClassIfExists(String className, ClassLoader classLoader) {
        dumpClass(className, classLoader);
        return findClassIfExists(className, classLoader);
    }

    public static void dumpClass(String className, ClassLoader classLoader) {
        Class<?> ourClass = findClassIfExists(className, classLoader);
        if (ourClass == null) {
            log("Class: " + className + " not found");
            return;
        }
        dumpClass(ourClass);
    }

    public static void dumpClass(Class<?> ourClass) {
        Method[] ms = ourClass.getDeclaredMethods();
        log("\n\nClass: " + ourClass.getName());
        log("extends: " + ourClass.getSuperclass().getName());
        log("Subclasses:");
        Class<?>[] scs = ourClass.getClasses();
        for (Class<?> c : scs) {
            log(c.getName());
        }
        log("Methods:");

        Constructor<?>[] cons = ourClass.getDeclaredConstructors();
        for (Constructor<?> m : cons) {
            log(m.getName() + " - " + " - " + m.getParameterCount());
            Class<?>[] cs = m.getParameterTypes();
            for (Class<?> c : cs) {
                log("\t\t" + c.getTypeName());
            }
        }

        for (Method m : ms) {
            log(m.getName() + " - " + m.getReturnType() + " - " + m.getParameterCount());
            Class<?>[] cs = m.getParameterTypes();
            for (Class<?> c : cs) {
                log("\t\t" + c.getTypeName());
            }
        }
        log("Fields:");

        Field[] fs = ourClass.getDeclaredFields();
        for (Field f : fs) {
            log("\t\t" + f.getName() + "-" + f.getType().getName());
        }
        log("End dump\n\n");
    }

    public static void tryHookAllMethods(Class<?> clazz, String method, XC_MethodHook hook) {
        try {
            hookAllMethods(clazz, method, hook);
        } catch (Throwable ignored) {
        }
    }

    public static void tryHookAllConstructors(Class<?> clazz, XC_MethodHook hook) {
        try {
            hookAllConstructors(clazz, hook);
        } catch (Throwable ignored) {
        }
    }

    public static void dumpChildViews(Context context, View view) {
        if (view instanceof ViewGroup viewGroup) {
            logViewInfo(context, viewGroup, 0);
            dumpChildViewsRecursive(context, viewGroup, 0);
        } else {
            logViewInfo(context, view, 0);
        }
    }

    private static void logViewInfo(Context context, View view, int indentationLevel) {
        String indentation = repeatString("\t", indentationLevel);
        String viewName = view.getClass().getSimpleName();
        Drawable backgroundDrawable = view.getBackground();

        int childCount = (view instanceof ViewGroup) ? ((ViewGroup) view).getChildCount() : 0;
        String resourceIdName = "none";

        try {
            int viewId = view.getId();
            resourceIdName = context.getResources().getResourceName(viewId);
        } catch (Throwable ignored) {
        }

        String logMessage = indentation + viewName + " - ID: " + resourceIdName;
        if (childCount > 0) {
            logMessage += " - ChildCount: " + childCount;
        }
        if (backgroundDrawable != null) {
            logMessage += " - Background: " + backgroundDrawable.getClass().getSimpleName() + "";
        }

        log(logMessage);
    }

    private static void dumpChildViewsRecursive(Context context, ViewGroup viewGroup, int indentationLevel) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childView = viewGroup.getChildAt(i);
            logViewInfo(context, childView, indentationLevel + 1);
            if (childView instanceof ViewGroup) {
                dumpChildViewsRecursive(context, (ViewGroup) childView, indentationLevel + 1);
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static String repeatString(String str, int times) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < times; i++) {
            result.append(str);
        }
        return result.toString();
    }

    public static int getColorResCompat(Context context, @AttrRes int id) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(id, typedValue, false);
        TypedArray arr = context.obtainStyledAttributes(typedValue.data, new int[]{id});
        @ColorInt int color = arr.getColor(0, -1);
        arr.recycle();
        return color;
    }
}
