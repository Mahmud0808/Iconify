package com.drdisagree.iconify.xposed.core.glass

import android.graphics.Bitmap
import android.graphics.Rect

data class BackdropFrame(
    val bitmap: Bitmap,
    val screenRect: Rect,
    val scale: Float
)
