package com.drdisagree.iconify.core.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.drdisagree.iconify.core.ui.components.preferences.FilePickerType
import com.drdisagree.iconify.data.keys.Key
import com.materialkolor.ktx.toHex

@Suppress("UNCHECKED_CAST", "unused")
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
        summary: ((PrefParam<Boolean>) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key = key,
            title = title,
            defaultValue = defaultValue.toPrefValue(),
            type = PreferenceType.Switch(isMasterSwitch),
            icon = icon,
            summary = summary as ((PrefParam<Any?>) -> PrefStringRes?)?,
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    )

    fun switch(
        key: Key,
        isMasterSwitch: Boolean = false,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: Boolean = key.default as? Boolean ?: false,
        summary: ((PrefParam<Boolean>) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key = key.name,
            title = title,
            defaultValue = defaultValue.toPrefValue(),
            type = PreferenceType.Switch(isMasterSwitch),
            icon = icon,
            summary = summary as ((PrefParam<Any?>) -> PrefStringRes?)?,
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    )

    // twoTargetSwitch: onClick always receives a Boolean value — T is only for the
    // visibility/summary lambdas, not for the click handler.
    fun twoTargetSwitch(
        key: String,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        summary: ((PrefParam<Boolean>) -> PrefStringRes?)? = null,
        defaultValue: Boolean = false,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
        onClick: (PrefParam<Boolean>) -> Unit,
    ) = prefs.add(
        PreferenceDefinition(
            key = key,
            title = title,
            defaultValue = defaultValue.toPrefValue(),
            type = PreferenceType.TwoTargetSwitch(onClick),
            icon = icon,
            summary = summary as ((PrefParam<Any?>) -> PrefStringRes?)?,
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    )

    fun twoTargetSwitch(
        key: Key,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        summary: ((PrefParam<Boolean>) -> PrefStringRes?)? = null,
        defaultValue: Boolean = key.default as? Boolean ?: false,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
        onClick: (PrefParam<Boolean>) -> Unit,
    ) = prefs.add(
        PreferenceDefinition(
            key = key.name,
            title = title,
            defaultValue = defaultValue.toPrefValue(),
            type = PreferenceType.TwoTargetSwitch(onClick),
            icon = icon,
            summary = summary as ((PrefParam<Any?>) -> PrefStringRes?)?,
            isVisible = isVisible,
            isEnabled = isEnabled,
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
        applyImmediately: Boolean = false,
        showResetButton: Boolean = false,
        showDefaultIndicator: Boolean = false,
        hideDefaultValue: Boolean = false,
        summary: ((PrefParam<Float>) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key = key,
            title = title,
            defaultValue = defaultValue.toPrefValue(),
            type = PreferenceType.Slider(
                min, max, steps, valueLabel, applyImmediately,
                showResetButton, showDefaultIndicator, hideDefaultValue
            ),
            icon = icon,
            summary = summary as ((PrefParam<Any?>) -> PrefStringRes?)?,
            isVisible = isVisible,
            isEnabled = isEnabled,
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
        applyImmediately: Boolean = false,
        showResetButton: Boolean = false,
        showDefaultIndicator: Boolean = false,
        hideDefaultValue: Boolean = false,
        summary: ((PrefParam<Float>) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key = key.name,
            title = title,
            defaultValue = defaultValue.toPrefValue(),
            type = PreferenceType.Slider(
                min, max, steps, valueLabel, applyImmediately,
                showResetButton, showDefaultIndicator, hideDefaultValue
            ),
            icon = icon,
            summary = summary as ((PrefParam<Any?>) -> PrefStringRes?)?,
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    )

    fun listPref(
        key: String,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: String = "",
        entries: PrefArrayRes,
        entryValues: PrefArrayRes,
        summary: ((PrefParam<String>) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key = key,
            title = title,
            defaultValue = defaultValue.toPrefValue(),
            type = PreferenceType.ListPref(entries, entryValues),
            icon = icon,
            summary = summary as ((PrefParam<Any?>) -> PrefStringRes?)?,
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    )

    fun listPref(
        key: Key,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: String = key.default as? String ?: "",
        entries: PrefArrayRes,
        entryValues: PrefArrayRes,
        summary: ((PrefParam<String>) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key = key.name,
            title = title,
            defaultValue = defaultValue.toPrefValue(),
            type = PreferenceType.ListPref(entries, entryValues),
            icon = icon,
            summary = summary as ((PrefParam<Any?>) -> PrefStringRes?)?,
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    )

    fun multiList(
        key: String,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: Set<String> = emptySet(),
        entries: PrefArrayRes,
        entryValues: PrefArrayRes,
        summary: ((PrefParam<Set<String>>) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key = key,
            title = title,
            defaultValue = defaultValue.toPrefValue(),
            type = PreferenceType.MultiList(entries, entryValues),
            icon = icon,
            summary = summary as ((PrefParam<Any?>) -> PrefStringRes?)?,
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    )

    fun multiList(
        key: Key,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: Set<String> = key.default as? Set<String> ?: emptySet(),
        entries: PrefArrayRes,
        entryValues: PrefArrayRes,
        summary: ((PrefParam<Set<String>>) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key = key.name,
            title = title,
            defaultValue = defaultValue.toPrefValue(),
            type = PreferenceType.MultiList(entries, entryValues),
            icon = icon,
            summary = summary as ((PrefParam<Any?>) -> PrefStringRes?)?,
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    )

    fun editText(
        key: String,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: String = "",
        summary: ((PrefParam<String>) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key = key,
            title = title,
            defaultValue = defaultValue.toPrefValue(),
            type = PreferenceType.EditText,
            icon = icon,
            summary = summary as ((PrefParam<Any?>) -> PrefStringRes?)?,
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    )

    fun editText(
        key: Key,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        defaultValue: String = key.default as? String ?: "",
        summary: ((PrefParam<String>) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
    ) = prefs.add(
        PreferenceDefinition(
            key = key.name,
            title = title,
            defaultValue = defaultValue.toPrefValue(),
            type = PreferenceType.EditText,
            icon = icon,
            summary = summary as ((PrefParam<Any?>) -> PrefStringRes?)?,
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    )

    fun colorPicker(
        key: String,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        summary: ((PrefParam<String>) -> PrefStringRes?)? = null,
        defaultValue: String = Color.White.toHex(),
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
        showAlphaSlider: Boolean = true,
    ) = prefs.add(
        PreferenceDefinition(
            key = key,
            icon = icon,
            title = title,
            summary = summary as ((PrefParam<Any?>) -> PrefStringRes?)?,
            defaultValue = defaultValue.toPrefValue(),
            type = PreferenceType.ColorPicker(showAlphaSlider = showAlphaSlider),
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    )

    fun colorPicker(
        key: Key,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        summary: ((PrefParam<String>) -> PrefStringRes?)? = null,
        defaultValue: String = key.default as? String ?: Color.White.toHex(),
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
        showAlphaSlider: Boolean = true,
    ) = prefs.add(
        PreferenceDefinition(
            key = key.name,
            icon = icon,
            title = title,
            summary = summary as ((PrefParam<Any?>) -> PrefStringRes?)?,
            defaultValue = defaultValue.toPrefValue(),
            type = PreferenceType.ColorPicker(showAlphaSlider = showAlphaSlider),
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    )

    // filePicker: the selected value is always a file URI string, so T is always
    // String here. The separate <T> on summary/isVisible is no longer needed;
    // collapse to a single concrete signature.
    fun filePicker(
        key: String,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        summary: ((PrefParam<String>) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
        pickerType: FilePickerType = FilePickerType.Any,
        saveFileUri: Boolean = false,
        onFileSelected: suspend (PrefParam<String>) -> Unit = {},
    ) = prefs.add(
        PreferenceDefinition(
            key = key,
            title = title,
            defaultValue = PrefValue.None,
            type = PreferenceType.FilePicker(pickerType, saveFileUri, onFileSelected),
            icon = icon,
            summary = summary as ((PrefParam<Any?>) -> PrefStringRes?)?,
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    )

    fun filePicker(
        key: Key,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        summary: ((PrefParam<String>) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
        pickerType: FilePickerType = FilePickerType.Any,
        saveFileUri: Boolean = false,
        onFileSelected: suspend (PrefParam<String>) -> Unit = {},
    ) = prefs.add(
        PreferenceDefinition(
            key = key.name,
            title = title,
            defaultValue = PrefValue.None,
            type = PreferenceType.FilePicker(pickerType, saveFileUri, onFileSelected),
            icon = icon,
            summary = summary as ((PrefParam<Any?>) -> PrefStringRes?)?,
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    )

    fun action(
        key: String,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        summary: ((PrefParam<*>) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
        onClick: (PrefParam<Any?>) -> Unit,
    ) = prefs.add(
        PreferenceDefinition(
            key = key,
            title = title,
            defaultValue = PrefValue.None,
            type = PreferenceType.Action(onClick),
            icon = icon,
            summary = summary,
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    )

    fun action(
        key: Key,
        icon: PrefIconRes? = null,
        title: PrefStringRes,
        summary: ((PrefParam<*>) -> PrefStringRes?)? = null,
        isVisible: ((PreferenceController) -> Boolean) = { true },
        isEnabled: ((PreferenceController) -> Boolean) = { true },
        onClick: (PrefParam<Any?>) -> Unit,
    ) = prefs.add(
        PreferenceDefinition(
            key = key.name,
            title = title,
            defaultValue = PrefValue.None,
            type = PreferenceType.Action(onClick),
            icon = icon,
            summary = summary,
            isVisible = isVisible,
            isEnabled = isEnabled,
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
            key = key,
            title = text,
            defaultValue = PrefValue.None,
            type = PreferenceType.Info,
            icon = icon,
            isVisible = isVisible,
            isEnabled = isEnabled,
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
            key = key.name,
            title = text,
            defaultValue = PrefValue.None,
            type = PreferenceType.Info,
            icon = icon,
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    )
}