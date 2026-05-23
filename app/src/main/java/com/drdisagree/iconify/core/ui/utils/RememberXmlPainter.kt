package com.drdisagree.iconify.core.ui.utils

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.core.content.ContextCompat
import com.drdisagree.iconify.core.common.LocalDarkMode
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun rememberXmlPainter(resId: Int): Painter {
    val context = LocalContext.current
    val isDarkTheme = LocalDarkMode.current
    val resources = LocalResources.current

    val themedContext = remember(isDarkTheme) {
        val config = Configuration(resources.configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                    if (isDarkTheme)
                        Configuration.UI_MODE_NIGHT_YES
                    else
                        Configuration.UI_MODE_NIGHT_NO
        }
        context.createConfigurationContext(config)
    }

    val drawable = remember(resId) {
        ContextCompat.getDrawable(themedContext, resId)
    }

    return rememberDrawablePainter(drawable)
}