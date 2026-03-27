package com.drdisagree.iconify.core.utils

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt

object ColorUtils {

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
