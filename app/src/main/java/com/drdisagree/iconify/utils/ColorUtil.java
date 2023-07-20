package com.drdisagree.iconify.utils;

import android.content.Context;
import android.graphics.Color;

public class ColorUtil {

    public static int hsl(float hue, float saturation, float lightness) {
        return HSLColor.hslToColor(hue, saturation, lightness);
    }

    public static float getHue(int color) {
        return mil.nga.color.Color.color(color).getHue();
    }

    public static int setHue(int color, float hue) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        float[] hsv = new float[3];
        Color.RGBToHSV(r, g, b, hsv);
        hsv[0] = hue;

        return Color.HSVToColor(hsv);
    }

    public static float getSaturation(int color) {
        return mil.nga.color.Color.color(color).getSaturation();
    }

    public static int setSaturation(int color, float saturation) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        float[] hsv = new float[3];
        Color.RGBToHSV(r, g, b, hsv);
        hsv[1] += saturation;

        return Color.HSVToColor(hsv);
    }

    public static float getLightness(int color) {
        return mil.nga.color.Color.color(color).getLightness();
    }

    public static int setLightness(int color, float lightness) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        float[] hsv = new float[3];
        Color.RGBToHSV(r, g, b, hsv);
        hsv[2] += lightness;

        return Color.HSVToColor(hsv);
    }

    public static String colorToHex(int color) {
        int alpha = Color.alpha(color);
        int blue = Color.blue(color);
        int green = Color.green(color);
        int red = Color.red(color);

        return String.format("#%02X%02X%02X%02X", alpha, red, green, blue);
    }

    public static String colorToSpecialHex(int color) {
        int blue = Color.blue(color);
        int green = Color.green(color);
        int red = Color.red(color);

        return String.format("0x%02X%02X%02X", red, green, blue);
    }

    public static float[] getSystemTintList() {
        return new float[]{1.0f, 0.99f, 0.95f, 0.9f, 0.8f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.2f, 0.1f, 0.0f};
    }

    public static String[][] getColorNames() {
        String[] accentTypes = {"system_accent1", "system_accent2", "system_accent3", "system_neutral1", "system_neutral2"};
        String[] values = {"0", "10", "50", "100", "200", "300", "400", "500", "600", "700", "800", "900", "1000"};

        String[][] colorNames = new String[accentTypes.length][values.length];

        for (int i = 0; i < accentTypes.length; i++) {
            for (int j = 0; j < values.length; j++) {
                colorNames[i][j] = accentTypes[i] + "_" + values[j];
            }
        }

        return colorNames;
    }

    public static int[][] getSystemColors(Context context) {
        return new int[][]{
                new int[]{
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary100, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary99, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary95, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary90, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary80, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary70, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary60, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary50, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary40, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary30, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary20, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary10, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary0, context.getTheme())
                },

                new int[]{
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary100, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary99, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary95, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary90, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary80, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary70, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary60, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary50, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary40, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary30, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary20, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary10, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary0, context.getTheme())
                },

                new int[]{
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary100, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary99, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary95, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary90, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary80, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary70, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary60, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary50, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary40, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary30, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary20, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary10, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary0, context.getTheme())
                },

                new int[]{
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral100, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral99, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral95, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral90, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral80, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral70, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral60, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral50, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral40, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral30, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral20, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral10, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral0, context.getTheme())
                },

                new int[]{
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant100, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant99, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant95, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant90, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant80, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant70, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant60, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant50, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant40, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant30, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant20, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant10, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant0, context.getTheme())
                }};
    }
}
