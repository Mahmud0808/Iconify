package com.drdisagree.iconify.features.xposed.lockscreen.depthwallpaper.screens

import android.content.Context
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.BitmapSubjectSegmenter

fun checkMlKitModelAvailability(context: Context, onResult: (Boolean) -> Unit) {
    BitmapSubjectSegmenter(context).checkModelAvailability { response ->
        onResult(response.areModulesAvailable())
    }
}