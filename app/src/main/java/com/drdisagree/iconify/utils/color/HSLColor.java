package com.drdisagree.iconify.utils.color;

import android.graphics.ColorSpace;

import androidx.annotation.ColorInt;

public class HSLColor {

    @ColorInt
    public static int hslToColor(float hue, float saturation, float lightness) {
        return hslToColor(hue, saturation, lightness, 1f, ColorSpace.get(ColorSpace.Named.SRGB));
    }

    @ColorInt
    public static int hslToColor(float hue, float saturation, float lightness, float alpha, ColorSpace colorSpace) {
        if (!(hue >= 0f && hue <= 360f && saturation >= 0f && saturation <= 1f && lightness >= 0f && lightness <= 1f)) {
            throw new IllegalArgumentException("HSL (" + hue + ", " + saturation + ", " + lightness + ") must be in range (0..360, 0..1, 0..1)");
        }

        float red = hslToRgbComponent(0, hue, saturation, lightness);
        float green = hslToRgbComponent(8, hue, saturation, lightness);
        float blue = hslToRgbComponent(4, hue, saturation, lightness);

        return colorToArgb(red, green, blue, alpha, colorSpace);
    }

    private static float hslToRgbComponent(int n, float h, float s, float l) {
        float k = (n + h / 30f) % 12f;
        float a = s * Math.min(l, 1f - l);
        return l - a * Math.max(-1f, Math.min(Math.min(k - 3, 9 - k), 1f));
    }

    @ColorInt
    public static int colorToArgb(float red, float green, float blue, float alpha, ColorSpace colorSpace) {
        if (!(red >= colorSpace.getMinValue(0) && red <= colorSpace.getMaxValue(0) &&
                green >= colorSpace.getMinValue(1) && green <= colorSpace.getMaxValue(1) &&
                blue >= colorSpace.getMinValue(2) && blue <= colorSpace.getMaxValue(2) &&
                alpha >= 0f && alpha <= 1f)) {
            throw new IllegalArgumentException("Color values outside the range for " + colorSpace);
        }

        if (colorSpace.isSrgb()) {

            return (((int) (alpha * 255.0f + 0.5f) << 24) |
                    ((int) (red * 255.0f + 0.5f) << 16) |
                    ((int) (green * 255.0f + 0.5f) << 8) |
                    (int) (blue * 255.0f + 0.5f));
        }

        if (colorSpace.getComponentCount() != 3) {
            throw new IllegalArgumentException("Color only works with ColorSpaces with 3 components");
        }

        int id = colorSpace.getId();
        if (id == ColorSpace.MIN_ID) {
            throw new IllegalArgumentException("Unknown color space, please use a color space in ColorSpaces");
        }

        float r = Math.max(0.0f, Math.min(red, 1.0f)) * 1023.0f + 0.5f;
        float g = Math.max(0.0f, Math.min(green, 1.0f)) * 1023.0f + 0.5f;
        float b = Math.max(0.0f, Math.min(blue, 1.0f)) * 1023.0f + 0.5f;
        int a = (int) (Math.max(0.0f, Math.min(alpha, 1.0f)) * 1023.0f + 0.5f);

        return (int) ((((long) Float.floatToRawIntBits(r) & 0xffffL) << 48) |
                (((long) Float.floatToRawIntBits(g) & 0xffffL) << 32) |
                (((long) Float.floatToRawIntBits(b) & 0xffffL) << 16) |
                (((long) a & 0x3ffL) << 6) |
                (long) id & 0x3fL);
    }
}
