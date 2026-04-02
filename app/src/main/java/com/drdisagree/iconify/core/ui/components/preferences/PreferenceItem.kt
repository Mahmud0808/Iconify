package com.drdisagree.iconify.core.ui.components.preferences

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceDefinition
import com.drdisagree.iconify.core.preferences.PreferenceType

@Composable
fun PreferenceItem(
    definition: PreferenceDefinition,
    prefController: PreferenceController,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    summary: String?,
    modifier: Modifier = Modifier,
) {
    when (val type = definition.type) {
        is PreferenceType.Custom -> {
            Box(modifier = modifier) {
                type.content()
            }
        }

        is PreferenceType.Switch -> SwitchPreferenceItem(
            def = definition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            summary = summary,
            type = type,
            modifier = modifier
        )

        is PreferenceType.Slider -> SliderPreferenceItem(
            def = definition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            summary = summary,
            type = type,
            modifier = modifier
        )

        is PreferenceType.ListPref -> ListPreferenceItem(
            def = definition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            summary = summary,
            type = type,
            modifier = modifier
        )

        is PreferenceType.MultiList -> MultiListPreferenceItem(
            def = definition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            summary = summary,
            type = type,
            modifier = modifier
        )

        is PreferenceType.Action -> ActionPreferenceItem(
            def = definition,
            shape = shape,
            isEnabled = isEnabled,
            summary = summary,
            type = type,
            modifier = modifier
        )

        is PreferenceType.EditText -> EditTextPreferenceItem(
            def = definition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            summary = summary,
            modifier = modifier
        )

        is PreferenceType.TwoTargetSwitch -> TwoTargetSwitchPreferenceItem(
            def = definition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            summary = summary,
            type = type,
            modifier = modifier,
        )

        is PreferenceType.ColorPicker -> ColorPickerPreferenceItem(
            def = definition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            summary = summary,
            modifier = modifier
        )

        is PreferenceType.FilePicker -> FilePickerPreferenceItem(
            def = definition,
            prefController = prefController,
            shape = shape,
            isEnabled = isEnabled,
            summary = summary,
            type = type,
            modifier = modifier
        )

        is PreferenceType.Info -> InfoPreferenceItem(
            def = definition,
            isEnabled = isEnabled,
            summary = summary,
            modifier = modifier
        )
    }
}