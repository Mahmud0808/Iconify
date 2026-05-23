package com.drdisagree.iconify.core.ui.components.extensions

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Modifier.customClickable(
    interactionSource: MutableInteractionSource?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
): Modifier {
    val viewConfiguration = LocalViewConfiguration.current

    LaunchedEffect(interactionSource) {
        var longPressJob: Job? = null
        var didLongClick = false

        interactionSource?.interactions?.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    longPressJob?.cancel()
                    didLongClick = false
                    longPressJob = launch {
                        delay(viewConfiguration.longPressTimeoutMillis)
                        didLongClick = true
                        onLongClick()
                    }
                }

                is PressInteraction.Release -> {
                    longPressJob?.cancel()
                    longPressJob = null
                    if (!didLongClick) onClick()
                }

                is PressInteraction.Cancel -> {
                    longPressJob?.cancel()
                    longPressJob = null
                    didLongClick = false
                }
            }
        }
    }

    return this
}