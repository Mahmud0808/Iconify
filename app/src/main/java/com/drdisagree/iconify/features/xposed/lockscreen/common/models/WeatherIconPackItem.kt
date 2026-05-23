package com.drdisagree.iconify.features.xposed.lockscreen.common.models

import android.graphics.drawable.Drawable

data class WeatherIconPackItem(
    val label: String,
    val value: String,
    val drawable: Drawable?
)