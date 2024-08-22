package com.drdisagree.iconify.xposed.utils

import android.annotation.SuppressLint
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import java.util.Calendar

object BootLoopProtector {

    const val LOAD_TIME_KEY_KEY = "packageLastLoad_"
    const val PACKAGE_STRIKE_KEY_KEY = "packageStrike_"

    @SuppressLint("ApplySharedPref")
    fun isBootLooped(packageName: String): Boolean {
        if (!XprefsIsInitialized) return false

        val loadTimeKey = "$LOAD_TIME_KEY_KEY$packageName"
        val strikeKey = "$PACKAGE_STRIKE_KEY_KEY$packageName"
        val currentTime = Calendar.getInstance().time.time
        val lastLoadTime = Xprefs.getLong(loadTimeKey, 0)
        var strikeCount = Xprefs.getInt(strikeKey, 0)

        if (currentTime - lastLoadTime > 40000) {
            Xprefs.edit()
                .putLong(loadTimeKey, currentTime)
                .putInt(strikeKey, 0)
                .commit()
        } else if (strikeCount >= 3) {
            return true
        } else {
            Xprefs.edit()
                .putInt(strikeKey, ++strikeCount)
                .commit()
        }

        return false
    }

    @SuppressLint("ApplySharedPref")
    fun resetCounter(packageName: String) {
        if (!XprefsIsInitialized) return

        try {
            val loadTimeKey = "$LOAD_TIME_KEY_KEY$packageName"
            val strikeKey = "$PACKAGE_STRIKE_KEY_KEY$packageName"
            val currentTime = Calendar.getInstance().time.time

            Xprefs.edit()
                .putLong(loadTimeKey, currentTime)
                .putInt(strikeKey, 0)
                .commit()
        } catch (ignored: Throwable) {
        }
    }
}