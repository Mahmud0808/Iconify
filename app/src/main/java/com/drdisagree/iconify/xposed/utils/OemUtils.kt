package com.drdisagree.iconify.xposed.utils

import android.os.Build

object OemUtils {
    val isSony: Boolean by lazy {
        Build.MANUFACTURER.equals("Sony", ignoreCase = true)
    }
}
