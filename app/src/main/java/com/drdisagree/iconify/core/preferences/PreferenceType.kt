package com.drdisagree.iconify.core.preferences

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.drdisagree.iconify.core.ui.components.preferences.FilePickerType

sealed class PreferenceType {
    /** A boolean toggle. */
    data class Switch(val isMasterSwitch: Boolean = false) : PreferenceType()

    /** An integer or float range slider. */
    data class Slider(
        val min: Float = 0f,
        val max: Float = 100f,
        val steps: Int = 0,
        val valueLabel: ((Float) -> String)? = null
    ) : PreferenceType()

    /** Single-choice list dialog. */
    data class ListPref(
        val entries: PrefArrayRes,
        val entryValues: PrefArrayRes
    ) : PreferenceType()

    /** Multi-choice list dialog. */
    data class MultiList(
        val entries: PrefArrayRes,
        val entryValues: PrefArrayRes
    ) : PreferenceType()

    /** Tappable item — fires an action, no stored value. */
    data class Action(val onClick: (Context, PreferenceController, NavController) -> Unit) :
        PreferenceType()

    /** Inline text field. */
    object EditText : PreferenceType()

    /** Color picker. */
    object ColorPicker : PreferenceType()

    /** File picker. */
    data class FilePicker(
        val pickerType: FilePickerType,
        val onFileSelected: (PreferenceController, String) -> Unit
    ) : PreferenceType()

    /** Tappable item — Two target switch. **/
    data class TwoTargetSwitch(val onClick: (Context, PreferenceController, NavController) -> Unit) :
        PreferenceType()

    /** Custom composables. */
    data class Custom(val content: @Composable () -> Unit) : PreferenceType()
}