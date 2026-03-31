package com.drdisagree.iconify.features.xposed.statusbar.logo.models

import android.graphics.drawable.Drawable

data class StatusbarLogoItem(
    val label: String,
    val value: String,
    val drawable: Drawable?
)