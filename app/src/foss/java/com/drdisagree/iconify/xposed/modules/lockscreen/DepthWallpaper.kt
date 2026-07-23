package com.drdisagree.iconify.xposed.modules.lockscreen

import android.content.Context
import android.graphics.Bitmap

class DepthWallpaper(context: Context) : BaseDepthWallpaper(context) {

    override fun handleSubjectExtraction(scaledWallpaper: Bitmap?) {
        if (mAiMode != 0) {
            sendPluginIntent()
        }
    }
}