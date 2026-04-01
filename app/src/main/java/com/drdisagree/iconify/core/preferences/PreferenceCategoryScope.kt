package com.drdisagree.iconify.core.preferences

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.drdisagree.iconify.core.ui.components.preferences.FilePickerType
import com.drdisagree.iconify.data.keys.Key
import com.materialkolor.ktx.toHex

class PreferenceCategoryScope {
    val prefs = mutableListOf<PreferenceDefinition>()

    fun composable(
        key: String,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        content: @Composable () -> Unit,
    ) = prefs.add(
        PreferenceDefinition(
            key = key,
            title = PrefStringRes.Hardcoded(""),
            defaultValue = PrefValue.None,
            type = PreferenceType.Custom(content),
            isVisible = isVisible,
        )
    )

    fun composable(
        key: Key,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        content: @Composable () -> Unit,
    ) = prefs.add(
        PreferenceDefinition(
            key = key.name,
            title = PrefStringRes.Hardcoded(""),
            defaultValue = PrefValue.None,
            type = PreferenceType.Custom(content),
            isVisible = isVisible,
        )
    )

    fun switch(
        key: String,
        isMasterSwitch: Boolean = false,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: Boolean = false,
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key, title, defaultValue.toPrefValue(), PreferenceType.Switch(isMasterSwitch),
            icon, summary, isVisible, isEnabled
        )
    )

    fun switch(
        key: Key,
        isMasterSwitch: Boolean = false,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: Boolean = key.default as? Boolean ?: false,
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key.name, title, defaultValue.toPrefValue(), PreferenceType.Switch(isMasterSwitch),
            icon, summary, isVisible, isEnabled
        )
    )

    fun twoTargetSwitch(
        key: String,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        defaultValue: Boolean = false,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
        onClick: (Context, PreferenceController, NavController) -> Unit,
    ) = prefs.add(
        PreferenceDefinition(
            key, title, defaultValue.toPrefValue(),
            PreferenceType.TwoTargetSwitch(onClick),
            icon, summary, isVisible, isEnabled
        )
    )

    fun twoTargetSwitch(
        key: Key,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        defaultValue: Boolean = key.default as? Boolean ?: false,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
        onClick: (Context, PreferenceController, NavController) -> Unit,
    ) = prefs.add(
        PreferenceDefinition(
            key.name, title, defaultValue.toPrefValue(),
            PreferenceType.TwoTargetSwitch(onClick),
            icon, summary, isVisible, isEnabled
        )
    )

    fun slider(
        key: String,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: Float = 0f,
        min: Float = 0f,
        max: Float = 100f,
        steps: Int = 0,
        valueLabel: ((Float) -> String)? = null,
        applyOnValueChangeFinished: Boolean = false,
        showResetButton: Boolean = false,
        showDefaultIndicator: Boolean = false,
        hideDefaultValue: Boolean = false,
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key, title, defaultValue.toPrefValue(),
            PreferenceType.Slider(
                min, max, steps, valueLabel, applyOnValueChangeFinished,
                showResetButton, showDefaultIndicator, hideDefaultValue
            ),
            icon, summary, isVisible, isEnabled
        )
    )

    fun slider(
        key: Key,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: Float = key.default as? Float ?: 0f,
        min: Float = 0f,
        max: Float = 100f,
        steps: Int = 0,
        valueLabel: ((Float) -> String)? = null,
        applyOnValueChangeFinished: Boolean = false,
        showResetButton: Boolean = false,
        showDefaultIndicator: Boolean = false,
        hideDefaultValue: Boolean = false,
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key.name, title, defaultValue.toPrefValue(),
            PreferenceType.Slider(
                min, max, steps, valueLabel, applyOnValueChangeFinished,
                showResetButton, showDefaultIndicator, hideDefaultValue
            ),
            icon, summary, isVisible, isEnabled
        )
    )

    fun listPref(
        key: String,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: String = "",
        entries: PrefArrayRes,
        entryValues: PrefArrayRes,
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key, title, defaultValue.toPrefValue(),
            PreferenceType.ListPref(entries, entryValues),
            icon, summary, isVisible, isEnabled
        )
    )

    fun listPref(
        key: Key,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: String = key.default as? String ?: "",
        entries: PrefArrayRes,
        entryValues: PrefArrayRes,
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key.name, title, defaultValue.toPrefValue(),
            PreferenceType.ListPref(entries, entryValues),
            icon, summary, isVisible, isEnabled
        )
    )

    fun multiList(
        key: String,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: Set<String> = emptySet(),
        entries: PrefArrayRes,
        entryValues: PrefArrayRes,
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key, title, defaultValue.toPrefValue(),
            PreferenceType.MultiList(entries, entryValues),
            icon, summary, isVisible, isEnabled
        )
    )

    @Suppress("UNCHECKED_CAST")
    fun multiList(
        key: Key,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: Set<String> = key.default as? Set<String> ?: emptySet(),
        entries: PrefArrayRes,
        entryValues: PrefArrayRes,
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key.name, title, defaultValue.toPrefValue(),
            PreferenceType.MultiList(entries, entryValues),
            icon, summary, isVisible, isEnabled
        )
    )

    fun editText(
        key: String,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: String = "",
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key, title, defaultValue.toPrefValue(), PreferenceType.EditText,
            icon, summary, isVisible, isEnabled
        )
    )

    fun editText(
        key: Key,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: String = key.default as? String ?: "",
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key.name, title, defaultValue.toPrefValue(), PreferenceType.EditText,
            icon, summary, isVisible, isEnabled
        )
    )

    fun colorPicker(
        key: String,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: String = Color.White.toHex(),
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key, title, defaultValue.toPrefValue(), PreferenceType.ColorPicker,
            icon, summary, isVisible, isEnabled
        )
    )

    fun colorPicker(
        key: Key,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: String = key.default as? String ?: Color.White.toHex(),
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key.name, title, defaultValue.toPrefValue(), PreferenceType.ColorPicker,
            icon, summary, isVisible, isEnabled
        )
    )

    fun filePicker(
        key: String,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
        pickerType: FilePickerType = FilePickerType.Any,
        saveFileUri: Boolean = false,
        onFileSelected: (PreferenceController, String) -> Unit = { _, _ -> },
    ) = prefs.add(
        PreferenceDefinition(
            key,
            title,
            PrefValue.None,
            PreferenceType.FilePicker(pickerType, saveFileUri, onFileSelected),
            icon, summary, isVisible, isEnabled
        )
    )

    fun filePicker(
        key: Key,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
        pickerType: FilePickerType = FilePickerType.Any,
        saveFileUri: Boolean = false,
        onFileSelected: (PreferenceController, String) -> Unit = { _, _ -> },
    ) = prefs.add(
        PreferenceDefinition(
            key.name,
            title,
            PrefValue.None,
            PreferenceType.FilePicker(pickerType, saveFileUri, onFileSelected),
            icon, summary, isVisible, isEnabled
        )
    )

    fun action(
        key: String,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
        onClick: (Context, PreferenceController, NavController) -> Unit,
    ) = prefs.add(
        PreferenceDefinition(
            key, title, PrefValue.None,
            PreferenceType.Action(onClick),
            icon, summary, isVisible, isEnabled
        )
    )

    fun action(
        key: Key,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
        onClick: (Context, PreferenceController, NavController) -> Unit,
    ) = prefs.add(
        PreferenceDefinition(
            key.name, title, PrefValue.None,
            PreferenceType.Action(onClick),
            icon, summary, isVisible, isEnabled
        )
    )

    fun info(
        key: String,
        icon: PrefIconRes? = null,
        text: PrefStringRes,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key, text, PrefValue.None, PreferenceType.Info,
            icon, null, isVisible, isEnabled
        )
    )

    fun info(
        key: Key,
        icon: PrefIconRes? = null,
        text: PrefStringRes,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key.name, text, PrefValue.None, PreferenceType.Info,
            icon, null, isVisible, isEnabled
        )
    )
}