package com.drdisagree.iconify.ui.models

import android.graphics.drawable.Drawable

data class IconPackModel(
    var name: String? = null,
    var desc: Int = 0,
    var icon1: Int = 0,
    var icon2: Int = 0,
    var icon3: Int = 0,
    var icon4: Int = 0,
    var drawableIcon1: Drawable? = null,
    var drawableIcon2: Drawable? = null,
    var drawableIcon3: Drawable? = null,
    var drawableIcon4: Drawable? = null,
    var isEnabled: Boolean = false
) {
    var packageName: String? = null

    constructor(
        packName: String?,
        pkgName: String,
        icon1: Drawable?,
        icon2: Drawable?,
        icon3: Drawable?,
        icon4: Drawable?,
        isEnabled: Boolean
    ) : this(
        name = packName,
        drawableIcon1 = icon1,
        drawableIcon2 = icon2,
        drawableIcon3 = icon3,
        drawableIcon4 = icon4,
        isEnabled = isEnabled
    ) {
        this.packageName = pkgName
    }
}