package com.drdisagree.iconify.utils.overlay.fabricated;

import android.content.om.OverlayIdentifier;
import android.util.Log;
import android.util.TypedValue;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused"})
public class FabricatedOverlay {
    private static final Class<?> oiClass;

    static {
        try {
            oiClass = Class.forName("android.content.om.OverlayIdentifier");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found: android.content.om.OverlayIdentifier");
        }
    }

    public final String overlayName;
    public final String targetPackage;
    public final String sourcePackage;

    public Map<String, FabricatedOverlayEntry> entries = new HashMap<>();

    public FabricatedOverlay(String overlayName, String targetPackage, String sourcePackage) {
        this.overlayName = overlayName;
        this.targetPackage = targetPackage;
        this.sourcePackage = sourcePackage;
    }

    public FabricatedOverlay(String overlayName, String targetPackage) {
        this(overlayName, targetPackage, "com.android.shell");
    }

    public static OverlayIdentifier generateOverlayIdentifier(String overlayName, String sourcePackage) {
        try {
        Constructor<?> constructor = oiClass.getConstructor(String.class, String.class);
        return (OverlayIdentifier) constructor.newInstance(sourcePackage, overlayName);
        } catch (Exception e) {
            Log.e("FabricatedOverlay", "generateOverlayIdentifier: ", e);
            return null;
        }
    }

    public void setInteger(String name, int value) {
        String formattedName = formatName(name, "integer");
        entries.put(formattedName, new FabricatedOverlayEntry(formattedName, TypedValue.TYPE_INT_DEC, value));
    }

    public void setBoolean(String name, boolean value) {
        String formattedName = formatName(name, "bool");
        entries.put(formattedName, new FabricatedOverlayEntry(formattedName, TypedValue.TYPE_INT_BOOLEAN, value ? 1 : 0));
    }

    public void setDimension(String name, int value) {
        String formattedName = formatName(name, "dimen");
        entries.put(formattedName, new FabricatedOverlayEntry(formattedName, TypedValue.TYPE_DIMENSION, value));
    }

    public void setAttribute(String name, int value) {
        String formattedName = formatName(name, "attr");
        entries.put(formattedName, new FabricatedOverlayEntry(formattedName, TypedValue.TYPE_ATTRIBUTE, value));
    }

    public void setColor(String name, int value) {
        String formattedName = formatName(name, "color");
        entries.put(formattedName, new FabricatedOverlayEntry(formattedName, TypedValue.TYPE_INT_COLOR_ARGB8, value));
    }

    public Map<String, FabricatedOverlayEntry> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, FabricatedOverlayEntry> entries) {
        this.entries = entries;
    }

    private String formatName(String name, String type) {
        if (name.contains(":") && name.contains("/")) {
            return name;
        } else {
            return targetPackage + ":" + type + "/" + name;
        }
    }
}