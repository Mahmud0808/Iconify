package com.drdisagree.iconify.core.preferences

import androidx.compose.runtime.Composable
import com.drdisagree.iconify.core.ui.components.preferences.FilePickerType

sealed class PreferenceType {
    data class Switch(val isMasterSwitch: Boolean = false) : PreferenceType()

    data class Slider(
        val min: Float = 0f,
        val max: Float = 100f,
        val steps: Int = 0,
        val valueLabel: ((Float) -> String)? = null,
        val applyImmediately: Boolean,
        val showResetButton: Boolean = false,
        val showDefaultIndicator: Boolean = false,
        val hideDefaultValue: Boolean = false
    ) : PreferenceType()

    data class ListPref(
        val entries: PrefArrayRes,
        val entryValues: PrefArrayRes
    ) : PreferenceType()

    data class MultiList(
        val entries: PrefArrayRes,
        val entryValues: PrefArrayRes
    ) : PreferenceType()

    data class Action(
        val onClick: (PrefParam<Any?>) -> Unit
    ) : PreferenceType()

    object EditText : PreferenceType()

    data class ColorPicker(
        val showAlphaSlider: Boolean,
    ) : PreferenceType()

    data class FilePicker(
        val pickerType: FilePickerType,
        val saveFileUri: Boolean,
        val onFileSelected: suspend (PrefParam<String>) -> Unit
    ) : PreferenceType()

    data class TwoTargetSwitch(
        val onClick: (PrefParam<Boolean>) -> Unit
    ) : PreferenceType()

    data class Custom(val content: @Composable () -> Unit) : PreferenceType()

    object Info : PreferenceType()
}