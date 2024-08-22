package com.drdisagree.iconify.ui.utils

import androidx.appcompat.app.AppCompatDelegate
import com.drdisagree.iconify.common.Preferences.APP_THEME
import com.drdisagree.iconify.config.RPrefs
import java.lang.Integer.parseInt

object ThemeHelper {

    val theme: Int
        get() {
            val theme = parseInt(
                RPrefs.getString(
                    APP_THEME,
                    "2"
                )!!
            )
            return when (theme) {
                0 -> AppCompatDelegate.MODE_NIGHT_NO
                1 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        }
}
