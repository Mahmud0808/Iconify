package com.drdisagree.iconify.xposed.utils;

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

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;

import androidx.annotation.NonNull;

import com.topjohnwu.superuser.Shell;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;

public class Helpers {

    public static void enableOverlay(String pkgName) {
        Shell.cmd("cmd overlay enable --user current " + pkgName, "cmd overlay set-priority " + pkgName + " highest").exec();
    }

    public static void disableOverlay(String pkgName) {
        Shell.cmd("cmd overlay disable --user current " + pkgName).exec();
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

    @SuppressWarnings("unused")
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
        log("Class: " + ourClass.getName());
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
        log("End dump");
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
}
