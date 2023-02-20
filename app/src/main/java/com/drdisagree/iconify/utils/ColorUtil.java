package com.drdisagree.iconify.utils;

import android.graphics.Color;

import com.drdisagree.iconify.Iconify;

public class ColorUtil {

    public static float getSaturation(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        float[] hsv = new float[3];
        Color.RGBToHSV(r, g, b, hsv);

        return hsv[1];
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
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        float[] hsv = new float[3];
        Color.RGBToHSV(r, g, b, hsv);

        return hsv[2];
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

    public static String[][] getColorNames() {
        return new String[][]{
                new String[]{
                        "system_accent1_0",
                        "system_accent1_10",
                        "system_accent1_50",
                        "system_accent1_100",
                        "system_accent1_200",
                        "system_accent1_300",
                        "system_accent1_400",
                        "system_accent1_500",
                        "system_accent1_600",
                        "system_accent1_700",
                        "system_accent1_800",
                        "system_accent1_900",
                        "system_accent1_1000"
                },
                new String[]{
                        "system_accent2_0",
                        "system_accent2_10",
                        "system_accent2_50",
                        "system_accent2_100",
                        "system_accent2_200",
                        "system_accent2_300",
                        "system_accent2_400",
                        "system_accent2_500",
                        "system_accent2_600",
                        "system_accent2_700",
                        "system_accent2_800",
                        "system_accent2_900",
                        "system_accent2_1000"
                },
                new String[]{
                        "system_accent3_0",
                        "system_accent3_10",
                        "system_accent3_50",
                        "system_accent3_100",
                        "system_accent3_200",
                        "system_accent3_300",
                        "system_accent3_400",
                        "system_accent3_500",
                        "system_accent3_600",
                        "system_accent3_700",
                        "system_accent3_800",
                        "system_accent3_900",
                        "system_accent3_1000"
                },
                new String[]{
                        "system_neutral1_0",
                        "system_neutral1_10",
                        "system_neutral1_50",
                        "system_neutral1_100",
                        "system_neutral1_200",
                        "system_neutral1_300",
                        "system_neutral1_400",
                        "system_neutral1_500",
                        "system_neutral1_600",
                        "system_neutral1_700",
                        "system_neutral1_800",
                        "system_neutral1_900",
                        "system_neutral1_1000"
                },
                new String[]{
                        "system_neutral2_0",
                        "system_neutral2_10",
                        "system_neutral2_50",
                        "system_neutral2_100",
                        "system_neutral2_200",
                        "system_neutral2_300",
                        "system_neutral2_400",
                        "system_neutral2_500",
                        "system_neutral2_600",
                        "system_neutral2_700",
                        "system_neutral2_800",
                        "system_neutral2_900",
                        "system_neutral2_1000"
                }
        };
    }

    public static int[][] getSystemColors() {
        return new int[][]{
                new int[]{
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_primary100),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_primary99),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_primary95),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_primary90),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_primary80),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_primary70),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_primary60),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_primary50),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_primary40),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_primary30),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_primary20),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_primary10),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_primary0)
                },

                new int[]{
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary100),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary99),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary95),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary90),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary80),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary70),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary60),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary50),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary40),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary30),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary20),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary10),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary0)
                },

                new int[]{
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary100),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary99),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary95),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary90),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary80),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary70),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary60),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary50),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary40),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary30),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary20),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary10),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary0)
                },

                new int[]{
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral100),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral99),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral95),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral90),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral80),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral70),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral60),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral50),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral40),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral30),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral20),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral10),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral0)
                },

                new int[]{
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant100),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant99),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant95),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant90),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant80),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant70),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant60),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant50),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant40),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant30),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant20),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant10),
                        Iconify.getAppContext().getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant0)
                }};
    }

    public static String ColorToHex(int color, boolean opacity, boolean hash) {
        int alpha = android.graphics.Color.alpha(color);
        int blue = android.graphics.Color.blue(color);
        int green = android.graphics.Color.green(color);
        int red = android.graphics.Color.red(color);

        String alphaHex = To00Hex(alpha);
        String blueHex = To00Hex(blue);
        String greenHex = To00Hex(green);
        String redHex = To00Hex(red);

        StringBuilder str;

        if (hash)
            str = new StringBuilder("#");
        else
            str = new StringBuilder();

        if (opacity)
            str.append(alphaHex);
        str.append(redHex);
        str.append(greenHex);
        str.append(blueHex);
        return str.toString();
    }

    public static String ColorToSpecialHex(int color) {
        int alpha = android.graphics.Color.alpha(color);
        int blue = android.graphics.Color.blue(color);
        int green = android.graphics.Color.green(color);
        int red = android.graphics.Color.red(color);

        String alphaHex = To00Hex(alpha);
        String blueHex = To00Hex(blue);
        String greenHex = To00Hex(green);
        String redHex = To00Hex(red);

        return "0xff" + redHex + greenHex + blueHex;
    }

    private static String To00Hex(int value) {
        String hex = "00".concat(Integer.toHexString(value));
        return hex.substring(hex.length() - 2);
    }
}
