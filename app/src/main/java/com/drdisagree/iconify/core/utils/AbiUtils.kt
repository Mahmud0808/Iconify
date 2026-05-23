package com.drdisagree.iconify.core.utils

import android.os.Build
import android.util.Log

object AbiUtils {

    fun getSupportedAbi(): String? {
        val supported = Build.SUPPORTED_ABIS
        return when {
            supported.contains("arm64-v8a") -> "arm64-v8a"
            supported.contains("armeabi-v7a") -> "armeabi-v7a"
            supported.contains("x86_64") -> "x86_64"
            supported.contains("x86") -> "x86"
            else -> {
                Log.e("AbiUtils", "Unsupported ABI: ${supported.joinToString()}")
                null
            }
        }
    }
}