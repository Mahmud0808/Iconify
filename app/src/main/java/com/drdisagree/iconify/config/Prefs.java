package com.drdisagree.iconify.config;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.BOOT_ID;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_PIXEL_DARK_BG;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.Preferences.VER_CODE;
import static com.drdisagree.iconify.common.Resources.SharedPref;
import static com.drdisagree.iconify.utils.ColorUtil.ColorToSpecialHex;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.ui.activities.Settings;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;

public class Prefs {

    static SharedPreferences pref = Iconify.getAppContext().getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
    static SharedPreferences.Editor editor = pref.edit();

    // Save sharedPref config
    public static void putBoolean(String key, boolean val) {
        editor.putBoolean(key, val).apply();
    }

    public static void putInt(String key, int val) {
        editor.putInt(key, val).apply();
    }

    public static void putLong(String key, long val) {
        editor.putLong(key, val).apply();
    }

    public static void putString(String key, String val) {
        editor.putString(key, val).apply();
    }

    // Load sharedPref config
    public static boolean getBoolean(String key) {
        return pref.getBoolean(key, false);
    }

    public static boolean getBoolean(String key, Boolean defValue) {
        return pref.getBoolean(key, defValue);
    }

    public static int getInt(String key) {
        return pref.getInt(key, 0);
    }

    public static int getInt(String key, int defValue) {
        return pref.getInt(key, defValue);
    }

    public static long getLong(String key) {
        return pref.getLong(key, 0);
    }

    public static long getLong(String key, long defValue) {
        return pref.getLong(key, defValue);
    }

    public static String getString(String key) {
        return pref.getString(key, STR_NULL);
    }

    public static String getString(String key, String defValue) {
        return pref.getString(key, defValue);
    }

    // Clear specific sharedPref config
    public static void clearPref(String key) {
        editor.remove(key).apply();
    }

    // Clear all sharedPref config
    public static void clearAllPrefs() {
        editor.clear().apply();
    }

    public static void exportPrefs(SharedPreferences preferences, final @NonNull OutputStream outputStream) throws IOException {
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(preferences.getAll());
            objectOutputStream.close();
        } catch (IOException ioException) {
            Log.e("ExportSettings", "Error serializing preferences", BuildConfig.DEBUG ? ioException : null);
        } finally {
            if (objectOutputStream != null) objectOutputStream.close();
            outputStream.close();
        }
    }

    public static void importPrefs(final @NonNull InputStream inputStream) throws IOException {
        ObjectInputStream objectInputStream = null;
        Map<String, Object> map;
        try {
            objectInputStream = new ObjectInputStream(inputStream);
            map = (Map<String, Object>) objectInputStream.readObject();
        } catch (Exception exception) {
            Log.e("ImportSettings", "Error deserializing preferences", BuildConfig.DEBUG ? exception : null);
            return;
        } finally {
            if (objectInputStream != null) objectInputStream.close();
            inputStream.close();
        }

        Settings.disableEverything();
        clearAllPrefs();

        boolean primaryColorApplied = false;
        boolean secondaryColorApplied = false;

        for (Map.Entry<String, Object> item : map.entrySet()) {
            if (item.getValue() instanceof Boolean) {
                putBoolean(item.getKey(), (Boolean) item.getValue());

                if ((Boolean) item.getValue()) {
                    if (item.getKey().contains("IconifyComponent") && item.getKey().contains(".overlay"))
                        OverlayUtil.enableOverlay(item.getKey());
                    else if (item.getKey().contains("fabricated")) {
                        Log.e("Import", item.getKey());
                        FabricatedUtil.buildAndEnableOverlay((String) map.get("FOCMDtarget" + item.getKey().replace("fabricated", "")), (String) map.get("FOCMDname" + item.getKey().replace("fabricated", "")), (String) map.get("FOCMDtype" + item.getKey().replace("fabricated", "")), (String) map.get("FOCMDresourceName" + item.getKey().replace("fabricated", "")), (String) map.get("FOCMDval" + item.getKey().replace("fabricated", "")));
                    }
                } else {
                    if (item.getKey().contains("fabricated")) {
                        FabricatedUtil.disableOverlay(item.getKey().replace("fabricated", ""));
                    }
                }
            } else if (item.getValue() instanceof String) {
                if (Objects.equals(item.getKey(), BOOT_ID)) SystemUtil.getBootId();
                else putString(item.getKey(), (String) item.getValue());

                if (item.getKey().contains(COLOR_ACCENT_PRIMARY) && !primaryColorApplied && getBoolean("fabricated" + item.getKey())) {
                    primaryColorApplied = true;
                    try {
                        FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY, "color", "holo_blue_light", ColorToSpecialHex(Integer.parseInt((String) item.getValue())));
                        FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_PIXEL_DARK_BG, "color", "holo_blue_dark", ColorToSpecialHex(ColorUtils.blendARGB(ColorUtils.blendARGB(Integer.parseInt((String) item.getValue()), Color.BLACK, 0.8f), Color.WHITE, 0.12f)));
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (item.getKey().contains(COLOR_ACCENT_SECONDARY) && !secondaryColorApplied && !primaryColorApplied && getBoolean("fabricated" + item.getKey())) {
                    secondaryColorApplied = true;
                    try {
                        FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY, "color", "holo_green_light", ColorToSpecialHex(Integer.parseInt((String) item.getValue())));
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else if (item.getValue() instanceof Integer) {
                if (Objects.equals(item.getKey(), VER_CODE))
                    putInt(item.getKey(), BuildConfig.VERSION_CODE);
                else putInt(item.getKey(), (Integer) item.getValue());
            }
        }
    }
}
