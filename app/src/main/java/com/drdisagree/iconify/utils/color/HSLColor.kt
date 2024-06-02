package com.drdisagree.iconify.utils.color

import android.graphics.ColorSpace
import androidx.annotation.ColorInt
import kotlin.math.max
import kotlin.math.min

object HSLColor {

    @ColorInt
    fun hslToColor(hue: Float, saturation: Float, lightness: Float): Int {
        return hslToColor(hue, saturation, lightness, 1f, ColorSpace.get(ColorSpace.Named.SRGB))
    }

    @ColorInt
    fun hslToColor(
        hue: Float,
        saturation: Float,
        lightness: Float,
        alpha: Float,
        colorSpace: ColorSpace
    ): Int {
        require(hue in 0f..360f && saturation >= 0f && saturation <= 1f && lightness >= 0f && lightness <= 1f) { "HSL ($hue, $saturation, $lightness) must be in range (0..360, 0..1, 0..1)" }

        val red = hslToRgbComponent(0, hue, saturation, lightness)
        val green = hslToRgbComponent(8, hue, saturation, lightness)
        val blue = hslToRgbComponent(4, hue, saturation, lightness)

        return colorToArgb(red, green, blue, alpha, colorSpace)
    }

    private fun hslToRgbComponent(n: Int, h: Float, s: Float, l: Float): Float {
        val k = (n + h / 30f) % 12f
        val a = (s * min(l.toDouble(), (1f - l).toDouble())).toFloat()

        return (l - a * max(
            -1.0,
            min(min((k - 3).toDouble(), (9 - k).toDouble()), 1.0)
        )).toFloat()
    }

    @ColorInt
    fun colorToArgb(
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float,
        colorSpace: ColorSpace
    ): Int {
        require(
            red >= colorSpace.getMinValue(0) && red <= colorSpace.getMaxValue(0) && green >= colorSpace.getMinValue(
                1
            ) && green <= colorSpace.getMaxValue(1) && blue >= colorSpace.getMinValue(2) && blue <= colorSpace.getMaxValue(
                2
            ) && alpha >= 0f && alpha <= 1f
        ) { "Color values outside the range for $colorSpace" }

        if (colorSpace.isSrgb) {
            return (alpha * 255.0f + 0.5f).toInt() shl 24 or
                    ((red * 255.0f + 0.5f).toInt() shl 16) or
                    ((green * 255.0f + 0.5f).toInt() shl 8) or (blue * 255.0f + 0.5f).toInt()
        }

        require(colorSpace.componentCount == 3) { "Color only works with ColorSpaces with 3 components" }

        val id = colorSpace.id

        require(id != ColorSpace.MIN_ID) { "Unknown color space, please use a color space in ColorSpaces" }

        val r = (max(0.0, min(red.toDouble(), 1.0)) * 1023.0f + 0.5f).toFloat()
        val g = (max(0.0, min(green.toDouble(), 1.0)) * 1023.0f + 0.5f).toFloat()
        val b = (max(0.0, min(blue.toDouble(), 1.0)) * 1023.0f + 0.5f).toFloat()
        val a = (max(0.0, min(alpha.toDouble(), 1.0)) * 1023.0f + 0.5f).toInt()

        return (java.lang.Float.floatToRawIntBits(r).toLong() and 0xffffL shl 48 or
                (java.lang.Float.floatToRawIntBits(g).toLong() and 0xffffL shl 32) or
                (java.lang.Float.floatToRawIntBits(b).toLong() and 0xffffL shl 16) or
                (a.toLong() and 0x3ffL shl 6) or (
                id.toLong() and 0x3fL)).toInt()
    }
}
