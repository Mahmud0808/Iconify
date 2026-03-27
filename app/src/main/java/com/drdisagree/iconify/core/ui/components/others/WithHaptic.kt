package com.drdisagree.iconify.core.ui.components.others

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.drdisagree.iconify.core.common.LocalSettings

@Composable
fun withHaptic(
    type: HapticFeedbackType = HapticFeedbackType.ContextClick,
    block: () -> Unit
): () -> Unit {
    val haptic = LocalHapticFeedback.current
    val isHapticEnabled = LocalSettings.current.isHapticEnabled
    val latestBlock = rememberUpdatedState(block)

    return retain(type, haptic, isHapticEnabled) {
        {
            if (isHapticEnabled) haptic.performHapticFeedback(type)
            latestBlock.value()
        }
    }
}

@Composable
fun withHapticResult(
    type: HapticFeedbackType = HapticFeedbackType.ContextClick,
    block: (Any?) -> Unit
): (Any?) -> Unit {
    val haptic = LocalHapticFeedback.current
    val isHapticEnabled = LocalSettings.current.isHapticEnabled
    val latestBlock = rememberUpdatedState(block)

    return retain(type, haptic, isHapticEnabled) {
        { value ->
            if (isHapticEnabled) haptic.performHapticFeedback(type)
            latestBlock.value(value)
        }
    }
}