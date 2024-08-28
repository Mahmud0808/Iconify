package com.drdisagree.iconify.utils

import android.view.HapticFeedbackConstants
import android.view.View

object HapticUtils {

    enum class VibrationType {
        Weak,
        Strong
    }

    private fun View.vibrate(type: VibrationType) {
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