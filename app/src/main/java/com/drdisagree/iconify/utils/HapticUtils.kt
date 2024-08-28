package com.drdisagree.iconify.utils

import android.view.HapticFeedbackConstants
import android.view.View
import com.drdisagree.iconify.common.Preferences.VIBRATE_UI
import com.drdisagree.iconify.config.RPrefs

object HapticUtils {

    enum class VibrationType {
        Weak,
        Strong
    }

    private fun View.vibrate(type: VibrationType) {
        if (RPrefs.getBoolean(VIBRATE_UI, true))
        when (type) {
            VibrationType.Weak -> performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            VibrationType.Strong -> performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    fun View.weakVibrate() {
        vibrate(VibrationType.Weak)
    }

    fun View.strongVibrate() {
        vibrate(VibrationType.Strong)
    }
}