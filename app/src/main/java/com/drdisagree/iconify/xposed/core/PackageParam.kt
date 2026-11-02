package com.drdisagree.iconify.xposed.core

import android.content.pm.ApplicationInfo

data class PackageParam(
    val packageName: String,
    val processName: String,
    val classLoader: ClassLoader,
    val appInfo: ApplicationInfo?,
    val isFirstPackage: Boolean
)
