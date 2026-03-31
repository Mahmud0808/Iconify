package com.drdisagree.iconify.features.xposed.lockscreen.common.events

sealed class WeatherEvent {
    object OpenLocationSettings : WeatherEvent()
    object OpenAppPermissionSettings : WeatherEvent()
    object RequestLocationPermissions : WeatherEvent()
}