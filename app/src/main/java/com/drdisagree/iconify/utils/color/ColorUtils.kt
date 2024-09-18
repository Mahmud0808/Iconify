package com.drdisagree.iconify.utils.color

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt

object ColorUtils {

    fun hsl(hue: Float, saturation: Float, lightness: Float): Int {
        return HSLColor.hslToColor(hue, saturation, lightness)
    }

    fun getHue(color: Int): Float {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        val hsv = FloatArray(3)
        Color.RGBToHSV(r, g, b, hsv)

        return hsv[0]
    }

    fun setHue(color: Int, hue: Float): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        val hsv = FloatArray(3)
        Color.RGBToHSV(r, g, b, hsv)
        hsv[0] = hue

        return Color.HSVToColor(hsv)
    }

    fun getSaturation(color: Int): Float {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        val hsv = FloatArray(3)
        Color.RGBToHSV(r, g, b, hsv)

        return hsv[1]
    }

    fun setSaturation(color: Int, saturation: Float): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        val hsv = FloatArray(3)
        Color.RGBToHSV(r, g, b, hsv)
        hsv[1] += saturation

        return Color.HSVToColor(hsv)
    }

    fun getLightness(color: Int): Float {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        val hsv = FloatArray(3)
        Color.RGBToHSV(r, g, b, hsv)

        return hsv[2]
    }

    fun setLightness(color: Int, lightness: Float): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        val hsv = FloatArray(3)
        Color.RGBToHSV(r, g, b, hsv)
        hsv[2] += lightness

        return Color.HSVToColor(hsv)
    }

    fun colorToHex(color: Int): String {
        val alpha = Color.alpha(color)
        val blue = Color.blue(color)
        val green = Color.green(color)
        val red = Color.red(color)

        return String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
    }

    fun colorToSpecialHex(color: Int): String {
        val blue = Color.blue(color)
        val green = Color.green(color)
        val red = Color.red(color)

        return String.format("0xff%02X%02X%02X", red, green, blue)
    }

    val systemTintList: FloatArray
        get() = floatArrayOf(
            1.0f,
            0.99f,
            0.95f,
            0.9f,
            0.8f,
            0.7f,
            0.6f,
            0.5f,
            0.4f,
            0.3f,
            0.2f,
            0.1f,
            0.0f
        )

    val colorNames: Array<Array<String?>>
        get() {
            val accentTypes = arrayOf(
                "system_accent1",
                "system_accent2",
                "system_accent3",
                "system_neutral1",
                "system_neutral2"
            )
            val values = arrayOf(
                "0",
                "10",
                "50",
                "100",
                "200",
                "300",
                "400",
                "500",
                "600",
                "700",
                "800",
                "900",
                "1000"
            )

            val colorNames = Array(accentTypes.size) { arrayOfNulls<String>(values.size) }
            for (i in accentTypes.indices) {
                for (j in values.indices) {
                    colorNames[i][j] = accentTypes[i] + "_" + values[j]
                }
            }

            return colorNames
        }

    fun getSystemColors(context: Context): Array<IntArray> {
        return arrayOf(
            intArrayOf(
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary100,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary99,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary95,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary90,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary80,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary70,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary60,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary50,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary40,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary30,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary20,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary10,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary0,
                    context.theme
                )
            ), intArrayOf(
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary100,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary99,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary95,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary90,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary80,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary70,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary60,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary50,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary40,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary30,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary20,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary10,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary0,
                    context.theme
                )
            ), intArrayOf(
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary100,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary99,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary95,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary90,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary80,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary70,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary60,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary50,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary40,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary30,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary20,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary10,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary0,
                    context.theme
                )
            ), intArrayOf(
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral100,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral99,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral95,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral90,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral80,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral70,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral60,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral50,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral40,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral30,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral20,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral10,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral0,
                    context.theme
                )
            ), intArrayOf(
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant100,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant99,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant95,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant90,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant80,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant70,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant60,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant50,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant40,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant30,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant20,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant10,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant0,
                    context.theme
                )
            )
        )
    }

    fun getColorResCompat(context: Context, @AttrRes id: Int): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(id, typedValue, false)

        val arr = context.obtainStyledAttributes(typedValue.data, intArrayOf(id))
        @ColorInt val color = arr.getColor(0, -1)
        arr.recycle()

        return color
    }
}
