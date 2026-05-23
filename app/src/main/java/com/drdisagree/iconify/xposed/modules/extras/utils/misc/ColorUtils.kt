package com.drdisagree.iconify.xposed.modules.extras.utils.misc

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt

fun getColorResCompat(context: Context, @AttrRes id: Int): Int {
    val typedValue = TypedValue()
    val theme = context.theme
    theme.resolveAttribute(id, typedValue, false)
    val arr = context.obtainStyledAttributes(typedValue.data, intArrayOf(id))
    @ColorInt val color = arr.getColor(0, -1)
    arr.recycle()
    return color
}