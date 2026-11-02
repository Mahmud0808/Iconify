package com.drdisagree.iconify.xposed.core.layout

import android.content.res.Resources
import android.view.View

data class LayoutInflatedParam(
    val view: View,
    val res: Resources,
    val resId: Int
)
