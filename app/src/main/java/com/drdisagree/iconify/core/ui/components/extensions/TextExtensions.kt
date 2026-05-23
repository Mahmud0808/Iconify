package com.drdisagree.iconify.core.ui.components.extensions

import androidx.compose.ui.graphics.Color
import com.drdisagree.iconify.core.preferences.PrefStringRes

private const val SECONDARY_TEXT_ALPHA = 0.75f

fun Color.secondaryText() = copy(alpha = SECONDARY_TEXT_ALPHA)

/** Allows `title = "Foo"` wherever a StringRes is expected. */
@Suppress("NOTHING_TO_INLINE")
inline fun String.toStringRes(): PrefStringRes = PrefStringRes.Hardcoded(this)