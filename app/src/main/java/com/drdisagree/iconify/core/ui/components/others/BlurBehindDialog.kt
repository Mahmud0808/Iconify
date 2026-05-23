package com.drdisagree.iconify.core.ui.components.others

import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import com.drdisagree.iconify.core.common.LocalSettings

const val BLUR_RADIUS = 23

@Composable
fun BlurBehindDialog() {
    val settings = LocalSettings.current
    val blurEffect = settings.blurEffect

    if (!blurEffect) return

    val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window

    LaunchedEffect(dialogWindow) {
        dialogWindow?.let { win ->
            win.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            win.attributes = win.attributes.apply {
                blurBehindRadius = BLUR_RADIUS
                dimAmount = 0f
            }
        }
    }
}