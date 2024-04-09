package com.drdisagree.iconify.xposed.modules.utils

import android.graphics.Paint

/*
* When setting a paint's color, alpha gets reset... naturally.
* So this is kind of paint that remembers its alpha and keeps it intact
*/
class AlphaRefreshedPaint(flag: Int) : Paint(flag) {
    override fun setColor(color: Int) {
        val alpha = alpha
        super.setColor(color)
        setAlpha(alpha)
    }
}
