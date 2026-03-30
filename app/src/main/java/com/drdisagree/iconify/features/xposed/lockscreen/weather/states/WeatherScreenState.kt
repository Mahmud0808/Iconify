package com.drdisagree.iconify.features.xposed.lockscreen.weather.states

import com.drdisagree.iconify.features.xposed.lockscreen.weather.events.WeatherDialog
import com.drdisagree.iconify.features.xposed.lockscreen.weather.models.WeatherIconPackItem

data class WeatherScreenState(
    val dialog: WeatherDialog? = null,
    val updateStatusSummary: String? = null,
    val iconPacks: List<WeatherIconPackItem> = emptyList(),
    val selectedIconPackIndex: Int = 0,
)