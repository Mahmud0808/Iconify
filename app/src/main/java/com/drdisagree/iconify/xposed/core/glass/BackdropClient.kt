package com.drdisagree.iconify.xposed.core.glass

import android.graphics.Rect
import android.view.SurfaceControl

interface BackdropClient {

    val displayId: Int

    fun captureRect(out: Rect): Boolean

    fun excludeLayer(): SurfaceControl?

    fun onBackdrop(frame: BackdropFrame)
}
