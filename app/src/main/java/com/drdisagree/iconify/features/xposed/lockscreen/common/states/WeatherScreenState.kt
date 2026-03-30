package com.drdisagree.iconify.features.xposed.lockscreen.common.states

import com.drdisagree.iconify.features.xposed.lockscreen.common.events.WeatherDialog
import com.drdisagree.iconify.features.xposed.lockscreen.common.models.WeatherIconPackItem

data class WeatherScreenState(
    val dialog: WeatherDialog? = null,
    val updateStatusSummary: String? = null,
    val iconPacks: List<WeatherIconPackItem> = emptyList(),
    val selectedIconPackIndex: Int = 0,
)