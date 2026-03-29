package com.drdisagree.iconify.features.xposed.lockscreen.clock.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.preferences.FilePickerType
import com.drdisagree.iconify.data.common.XposedConst.LSCLOCK_FONT_FILE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.xposed.lockscreen.clock.components.LockscreenClockPreview
import com.drdisagree.iconify.helpers.toXposedSharedPath

val lsClockPreferences = preferenceScreen {
    category {
        switch(
            key = XposedKey.CUSTOM_LSCLOCK,
            isMasterSwitch = true,
            title = stringRes(R.string.activity_title_lockscreen_clock),
            summary = { _, _ -> stringRes(R.string.activity_desc_lockscreen_clock) }
        )
    }

    composable(key = "clock_previews") {
        LockscreenClockPreview()
    }

    category {
        filePicker(
            key = XposedKey.LSCLOCK_FONT_FILE_URI,
            title = stringRes(R.string.lockscreen_clock_font_title),
            summary = { _, _ -> stringRes(R.string.lockscreen_clock_font_desc) },
            pickerType = FilePickerType.Font,
            saveFileUri = true,
            onFileSelected = { _, uriString ->
                if (uriString.isNotEmpty()) {
                    uriString.toUri().toXposedSharedPath(LSCLOCK_FONT_FILE.name)
                }
            },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_LSCLOCK) }
        )
    }

    category {
        slider(
            key = XposedKey.LSCLOCK_LINE_HEIGHT,
            title = stringRes(R.string.lockscreen_font_line_height_title),
            min = -120f,
            max = 120f,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_LSCLOCK) }
        )

        slider(
            key = XposedKey.LSCLOCK_TEXT_SCALING,
            title = stringRes(R.string.lockscreen_font_text_scaling_title),
            min = 5f,
            max = 25f,
            steps = 19,
            valueLabel = { "${"%.1f".format(it / 10f)}x" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_LSCLOCK) }
        )

        slider(
            key = XposedKey.LSCLOCK_TOP_MARGIN,
            title = stringRes(R.string.lockscreen_clock_top_margin_title),
            min = 0f,
            max = 600f,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_LSCLOCK) }
        )

        slider(
            key = XposedKey.LSCLOCK_BOTTOM_MARGIN,
            title = stringRes(R.string.lockscreen_clock_bottom_margin_title),
            min = 0f,
            max = 600f,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_LSCLOCK) }
        )
    }

    category {
        switch(
            key = XposedKey.LSCLOCK_CUSTOM_COLOR,
            title = stringRes(R.string.lsclock_custom_color_title),
            summary = { _, _ -> stringRes(R.string.lsclock_custom_color_desc) },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_LSCLOCK) }
        )

        colorPicker(
            key = XposedKey.LSCLOCK_COLOR_ACCENT_PRIMARY,
            title = stringRes(R.string.accent_primary),
            isEnabled = {
                it.getBoolean(XposedKey.LSCLOCK_CUSTOM_COLOR) &&
                        it.getBoolean(XposedKey.CUSTOM_LSCLOCK)
            },
            isVisible = { it.getBoolean(XposedKey.LSCLOCK_CUSTOM_COLOR) }
        )

        colorPicker(
            key = XposedKey.LSCLOCK_COLOR_ACCENT_SECONDARY,
            title = stringRes(R.string.accent_primary),
            isEnabled = {
                it.getBoolean(XposedKey.LSCLOCK_CUSTOM_COLOR) &&
                        it.getBoolean(XposedKey.CUSTOM_LSCLOCK)
            },
            isVisible = { it.getBoolean(XposedKey.LSCLOCK_CUSTOM_COLOR) }
        )

        colorPicker(
            key = XposedKey.LSCLOCK_COLOR_ACCENT_TERTIARY,
            title = stringRes(R.string.accent_primary),
            isEnabled = {
                it.getBoolean(XposedKey.LSCLOCK_CUSTOM_COLOR) &&
                        it.getBoolean(XposedKey.CUSTOM_LSCLOCK)
            },
            isVisible = { it.getBoolean(XposedKey.LSCLOCK_CUSTOM_COLOR) }
        )

        colorPicker(
            key = XposedKey.LSCLOCK_COLOR_TEXT_PRIMARY,
            title = stringRes(R.string.accent_primary),
            isEnabled = {
                it.getBoolean(XposedKey.LSCLOCK_CUSTOM_COLOR) &&
                        it.getBoolean(XposedKey.CUSTOM_LSCLOCK)
            },
            isVisible = { it.getBoolean(XposedKey.LSCLOCK_CUSTOM_COLOR) }
        )

        colorPicker(
            key = XposedKey.LSCLOCK_COLOR_TEXT_INVERSE,
            title = stringRes(R.string.accent_primary),
            isEnabled = {
                it.getBoolean(XposedKey.LSCLOCK_CUSTOM_COLOR) &&
                        it.getBoolean(XposedKey.CUSTOM_LSCLOCK)
            },
            isVisible = { it.getBoolean(XposedKey.LSCLOCK_CUSTOM_COLOR) }
        )
    }

    category {
        editText(
            key = XposedKey.LSCLOCK_DEVICE_NAME,
            title = stringRes(R.string.lockscreen_clock_custom_devicename),
            summary = { prefs, _ ->
                val currentVal = prefs.getString(XposedKey.LSCLOCK_DEVICE_NAME)

                if (currentVal.isNotEmpty()) stringRes(currentVal)
                else stringRes(R.string.lockscreen_clock_custom_devicename_desc)
            },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_LSCLOCK) },
            isVisible = { it.getInt(XposedKey.LSCLOCK_STYLE) in setOf(19, 32, 47) },
        )

        editText(
            key = XposedKey.LSCLOCK_USER_NAME,
            title = stringRes(R.string.lockscreen_clock_custom_username),
            summary = { prefs, _ ->
                val currentVal = prefs.getString(XposedKey.LSCLOCK_USER_NAME)

                if (currentVal.isNotEmpty()) stringRes(currentVal)
                else stringRes(R.string.lockscreen_clock_custom_username_desc)
            },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_LSCLOCK) },
            isVisible = {
                it.getInt(XposedKey.LSCLOCK_STYLE) in
                        setOf(7, 32, 35, 36, 42, 48, 50, 53)
            },
        )
    }
}

@Composable
fun LockscreenClockScreen() {
    PreferenceScreen(
        items = lsClockPreferences,
        title = stringResource(R.string.activity_title_lockscreen_clock),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun LockscreenClockScreenPreview() {
    PreviewComposable {
        LockscreenClockScreen()
    }
}