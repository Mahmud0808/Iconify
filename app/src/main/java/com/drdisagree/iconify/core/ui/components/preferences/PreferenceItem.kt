package com.drdisagree.iconify.core.ui.components.preferences

import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceDefinition
import com.drdisagree.iconify.core.preferences.PreferenceType
import kotlinx.coroutines.delay

@Composable
fun PreferenceItem(
    modifier: Modifier = Modifier,
    prefDefinition: PreferenceDefinition,
    prefController: PreferenceController,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    isHighlighted: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(isHighlighted) {
        if (!isHighlighted) return@LaunchedEffect

        var activePress: PressInteraction.Press? = null
        try {
            delay(500)
            val rippleCount = 2
            repeat(rippleCount) { index ->
                activePress = PressInteraction.Press(Offset.Zero)
                interactionSource.emit(activePress)
                delay(200)
                interactionSource.emit(PressInteraction.Release(activePress))
                activePress = null

                if (index < rippleCount - 1) {
                    delay(250)
                }
            }
        } finally {
            activePress?.let { press ->
                interactionSource.tryEmit(PressInteraction.Release(press))
            }
        }
    }

    val finalModifier = modifier
        .clip(shape)
        .indication(interactionSource, ripple())

    when (val type = prefDefinition.type) {
        is PreferenceType.Custom -> {
            Box(modifier = finalModifier) {
                type.content()
            }
        }

        is PreferenceType.Switch -> SwitchPreferenceItem(
            prefDefinition = prefDefinition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            type = type,
            modifier = finalModifier
        )

        is PreferenceType.Slider -> SliderPreferenceItem(
            prefDefinition = prefDefinition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            type = type,
            modifier = finalModifier
        )

        is PreferenceType.ListPref -> ListPreferenceItem(
            prefDefinition = prefDefinition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            type = type,
            modifier = finalModifier
        )

        is PreferenceType.MultiList -> MultiListPreferenceItem(
            prefDefinition = prefDefinition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            type = type,
            modifier = finalModifier
        )

        is PreferenceType.Action -> ActionPreferenceItem(
            prefDefinition = prefDefinition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            type = type,
            modifier = finalModifier
        )

        is PreferenceType.EditText -> EditTextPreferenceItem(
            prefDefinition = prefDefinition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            modifier = finalModifier
        )

        is PreferenceType.TwoTargetSwitch -> TwoTargetSwitchPreferenceItem(
            prefDefinition = prefDefinition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            type = type,
            modifier = finalModifier,
        )

        is PreferenceType.ColorPicker -> ColorPickerPreferenceItem(
            prefDefinition = prefDefinition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            type = type,
            modifier = finalModifier
        )

        is PreferenceType.FilePicker -> FilePickerPreferenceItem(
            prefDefinition = prefDefinition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            type = type,
            modifier = finalModifier
        )

        is PreferenceType.Info -> InfoPreferenceItem(
            prefDefinition = prefDefinition,
            prefController = prefController,
            isEnabled = isEnabled,
            modifier = finalModifier
        )
    }
}