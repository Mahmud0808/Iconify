package com.drdisagree.iconify.features.xposed.lockscreen.common.events

sealed class WeatherDialog {
    object LocationDisabled : WeatherDialog()
    object PermissionRationale : WeatherDialog()
    object OwmKey : WeatherDialog()
    object YandexKey : WeatherDialog()
}