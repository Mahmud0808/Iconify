package com.drdisagree.iconify.features.xposed.lockscreen.weather.models

import android.graphics.drawable.Drawable

data class WeatherIconPackItem(
    val label: String,
    val value: String,
    val drawable: Drawable?
)