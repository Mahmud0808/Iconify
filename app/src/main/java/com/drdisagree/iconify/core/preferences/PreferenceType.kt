package com.drdisagree.iconify.core.preferences

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.drdisagree.iconify.core.ui.components.preferences.FilePickerType

sealed class PreferenceType {
    data class Switch(val isMasterSwitch: Boolean = false) : PreferenceType()

    data class Slider(
        val min: Float = 0f,
        val max: Float = 100f,
        val steps: Int = 0,
        val valueLabel: ((Float) -> String)? = null,
        val applyOnValueChangeFinished: Boolean,
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
        val onClick: (Context, PreferenceController, NavController) -> Unit
    ) : PreferenceType()

    object EditText : PreferenceType()

    object ColorPicker : PreferenceType()

    data class FilePicker(
        val pickerType: FilePickerType,
        val saveFileUri: Boolean,
        val onFileSelected: (PreferenceController, String) -> Unit
    ) : PreferenceType()

    data class TwoTargetSwitch(
        val onClick: (Context, PreferenceController, NavController) -> Unit
    ) : PreferenceType()

    data class Custom(val content: @Composable () -> Unit) : PreferenceType()

    object Info : PreferenceType()
}