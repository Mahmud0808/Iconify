package com.drdisagree.iconify.features.xposed.lockscreen.depthwallpaper.screens

import android.content.Context

fun checkMlKitModelAvailability(context: Context, onResult: (Boolean) -> Unit) {
    onResult(false)
}