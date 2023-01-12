package com.drdisagree.iconify.config;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.ui.activity.ColorPicker;
import com.drdisagree.iconify.ui.activity.Settings;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;

public class PrefConfig {

    @SuppressLint("StaticFieldLeak")
    private static final Context context = Iconify.getAppContext();
    private static final String SharedPref = context.getPackageName();

    // Save sharedPref config
    public static void savePrefBool(String key, boolean val) {
        SharedPreferences pref = context.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, val);
        editor.apply();
    }

    public static void savePrefInt(String key, int val) {
        SharedPreferences pref = context.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, val);
        editor.apply();
    }

    public static void savePrefSettings(String key, String val) {
        SharedPreferences pref = context.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, val);
        editor.apply();
    }

    // Load sharedPref config
    public static boolean loadPrefBool(String key) {
        SharedPreferences pref = context.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        return pref.getBoolean(key, false);
    }

    public static int loadPrefInt(String key) {
        SharedPreferences pref = context.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        return pref.getInt(key, 0);
    }

    public static String loadPrefSettings(String key) {
        SharedPreferences pref = context.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        return pref.getString(key, "null");
    }

    // Clear specific sharedPref config
    public static void clearPref(String key) {
        SharedPreferences pref = context.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        pref.edit().remove(key).apply();
    }

    // Clear all sharedPref config
    public static void clearAllPref() {
        SharedPreferences pref = context.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        pref.edit().clear().apply();
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
            if (objectOutputStream != null)
                objectOutputStream.close();
            outputStream.close();
        }
    }

    public static void importPrefs(SharedPreferences sharedPreferences, final @NonNull InputStream inputStream) throws IOException {
        ObjectInputStream objectInputStream = null;
        Map<String, Object> map;
        try {
            objectInputStream = new ObjectInputStream(inputStream);
            map = (Map<String, Object>) objectInputStream.readObject();
        } catch (Exception exception) {
            Log.e("ImportSettings", "Error deserializing preferences", BuildConfig.DEBUG ? exception : null);
            return;
        } finally {
            if (objectInputStream != null)
                objectInputStream.close();
            inputStream.close();
        }

        Settings.disableEverything();
        clearAllPref();

        boolean primaryColorApplied = false;
        boolean secondaryColorApplied = false;

        for (Map.Entry<String, Object> item : map.entrySet()) {
            if (item.getValue() instanceof Boolean) {
                savePrefBool(item.getKey(), (Boolean) item.getValue());

                if ((Boolean) item.getValue()) {
                    if (item.getKey().contains("IconifyComponent") && item.getKey().contains(".overlay"))
                        OverlayUtil.enableOverlay(item.getKey());
                }
            } else if (item.getValue() instanceof String) {
                if (Objects.equals(item.getKey(), "boot_id"))
                    PrefConfig.savePrefSettings(item.getKey(), Shell.cmd("cat /proc/sys/kernel/random/boot_id").exec().getOut().toString());
                else
                    savePrefSettings(item.getKey(), (String) item.getValue());

                if (item.getKey().contains("colorAccentPrimary") && !primaryColorApplied && loadPrefBool("fabricated" + item.getKey())) {
                    primaryColorApplied = true;
                    try {
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary", "color", "holo_blue_light", ColorPicker.ColorToSpecialHex(Integer.parseInt((String) item.getValue())));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary1", "color", "system_accent1_100", ColorPicker.ColorToSpecialHex(Integer.parseInt((String) item.getValue())));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary2", "color", "system_accent1_200", ColorPicker.ColorToSpecialHex(Integer.parseInt((String) item.getValue())));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary3", "color", "system_accent1_300", ColorPicker.ColorToSpecialHex(Integer.parseInt((String) item.getValue())));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary4", "color", "system_accent2_100", ColorPicker.ColorToSpecialHex(ColorUtils.blendARGB(Integer.parseInt((String) item.getValue()), Color.WHITE, 0.16f)));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary5", "color", "system_accent2_200", ColorPicker.ColorToSpecialHex(ColorUtils.blendARGB(Integer.parseInt((String) item.getValue()), Color.WHITE, 0.16f)));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary6", "color", "system_accent2_300", ColorPicker.ColorToSpecialHex(ColorUtils.blendARGB(Integer.parseInt((String) item.getValue()), Color.WHITE, 0.16f)));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimaryDark", "color", "holo_blue_dark", ColorPicker.ColorToSpecialHex(ColorUtils.blendARGB(ColorUtils.blendARGB(Integer.parseInt((String) item.getValue()), Color.BLACK, 0.8f), Color.WHITE, 0.12f)));
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (item.getKey().contains("colorAccentSecondary") && !secondaryColorApplied && !primaryColorApplied && loadPrefBool("fabricated" + item.getKey())) {
                    secondaryColorApplied = true;
                    try {
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentSecondary", "color", "holo_green_light", ColorPicker.ColorToSpecialHex(Integer.parseInt((String) item.getValue())));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentSecondary1", "color", "system_accent3_100", ColorPicker.ColorToSpecialHex(Integer.parseInt((String) item.getValue())));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentSecondary2", "color", "system_accent3_200", ColorPicker.ColorToSpecialHex(Integer.parseInt((String) item.getValue())));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentSecondary3", "color", "system_accent3_300", ColorPicker.ColorToSpecialHex(Integer.parseInt((String) item.getValue())));
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else if (item.getValue() instanceof Integer) {
                if (Objects.equals(item.getKey(), "versionCode"))
                    savePrefInt(item.getKey(), BuildConfig.VERSION_CODE);
                else
                    savePrefInt(item.getKey(), (Integer) item.getValue());
            }
        }
    }
}
