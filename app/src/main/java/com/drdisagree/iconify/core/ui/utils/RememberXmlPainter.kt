package com.drdisagree.iconify.core.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun rememberXmlPainter(resId: Int): Painter {
    val context = LocalContext.current
    val drawable = remember(resId) { ContextCompat.getDrawable(context, resId) }
    return rememberDrawablePainter(drawable)
}