package com.drdisagree.iconify.features.xposed.quicksettings.clock.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.preferences.FilePickerType
import com.drdisagree.iconify.core.utils.FileUtils.copyToXposedSharedPath
import com.drdisagree.iconify.data.common.XposedConst.HEADER_CLOCK_FONT_FILE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.common.viewmodels.SystemActionViewModel
import com.drdisagree.iconify.features.xposed.quicksettings.clock.components.HeaderClockPreview
import kotlin.math.roundToInt

val headerClockPreferences = preferenceScreen {
    category {
        switch(
            key = XposedKey.CUSTOM_HEADER_CLOCK,
            isMasterSwitch = true,
            title = stringRes(R.string.activity_title_header_clock),
        )
    }

    composable(key = "clock_previews") {
        HeaderClockPreview()
    }

    category {
        filePicker(
            key = XposedKey.HEADER_CLOCK_FONT_FILE_URI,
            title = stringRes(R.string.header_clock_font_title),
            summary = { stringRes(R.string.header_clock_font_desc) },
            pickerType = FilePickerType.Font,
            saveFileUri = true,
            onFileSelected = {
                val uriString = it.newValue
                if (uriString.isNotEmpty()) {
                    uriString.toUri().copyToXposedSharedPath(HEADER_CLOCK_FONT_FILE.name)
                }
            },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_HEADER_CLOCK) }
        )
    }

    category {
        switch(
            key = XposedKey.HEADER_CLOCK_CENTER_VIEW,
            title = stringRes(R.string.header_clock_center_clock_title),
            summary = { stringRes(R.string.header_clock_center_clock_desc) },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_HEADER_CLOCK) }
        )

        switch(
            key = XposedKey.HEADER_CLOCK_HALF_WIDTH_IN_LANDSCAPE,
            title = stringRes(R.string.header_clock_half_width_in_landscape_title),
            summary = { stringRes(R.string.header_clock_half_width_in_landscape_desc) },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_HEADER_CLOCK) },
        )
    }

    category {
        slider(
            key = XposedKey.HEADER_CLOCK_TEXT_SCALE,
            title = stringRes(R.string.header_clock_font_text_scaling_title),
            min = 0.5f,
            max = 2.5f,
            steps = 19,
            valueLabel = { "${"%.1f".format(it)}x" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_HEADER_CLOCK) }
        )

        slider(
            key = XposedKey.HEADER_CLOCK_TOP_MARGIN,
            title = stringRes(R.string.header_clock_top_margin_title),
            min = 0f,
            max = 250f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_HEADER_CLOCK) }
        )

        slider(
            key = XposedKey.HEADER_CLOCK_SIDE_MARGIN,
            title = stringRes(R.string.header_clock_side_margin_title),
            min = 0f,
            max = 200f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_HEADER_CLOCK) }
        )

        slider(
            key = XposedKey.HEADER_CLOCK_EXPANSION_Y,
            title = stringRes(R.string.header_clock_expansion_y_title),
            min = 0f,
            max = 250f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_HEADER_CLOCK) }
        )

        slider(
            key = XposedKey.HEADER_CLOCK_LANDSCAPE_OFFSET_Y,
            title = stringRes(R.string.header_clock_landscape_offset_y_title),
            min = -20f,
            max = 40f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_HEADER_CLOCK) }
        )
    }

    category {
        switch(
            key = XposedKey.HEADER_CLOCK_CUSTOM_COLOR,
            title = stringRes(R.string.custom_header_clock_color_title),
            summary = { stringRes(R.string.custom_header_clock_color_desc) },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_HEADER_CLOCK) }
        )

        colorPicker(
            key = XposedKey.HEADER_CLOCK_COLOR_ACCENT_PRIMARY,
            title = stringRes(R.string.accent_primary),
            isEnabled = {
                it.getBoolean(XposedKey.HEADER_CLOCK_CUSTOM_COLOR) &&
                        it.getBoolean(XposedKey.CUSTOM_HEADER_CLOCK)
            },
            isVisible = { it.getBoolean(XposedKey.HEADER_CLOCK_CUSTOM_COLOR) }
        )

        colorPicker(
            key = XposedKey.HEADER_CLOCK_COLOR_ACCENT_SECONDARY,
            title = stringRes(R.string.accent_secondary),
            isEnabled = {
                it.getBoolean(XposedKey.HEADER_CLOCK_CUSTOM_COLOR) &&
                        it.getBoolean(XposedKey.CUSTOM_HEADER_CLOCK)
            },
            isVisible = { it.getBoolean(XposedKey.HEADER_CLOCK_CUSTOM_COLOR) }
        )

        colorPicker(
            key = XposedKey.HEADER_CLOCK_COLOR_ACCENT_TERTIARY,
            title = stringRes(R.string.accent_tertiary),
            isEnabled = {
                it.getBoolean(XposedKey.HEADER_CLOCK_CUSTOM_COLOR) &&
                        it.getBoolean(XposedKey.CUSTOM_HEADER_CLOCK)
            },
            isVisible = { it.getBoolean(XposedKey.HEADER_CLOCK_CUSTOM_COLOR) }
        )

        colorPicker(
            key = XposedKey.HEADER_CLOCK_COLOR_TEXT_PRIMARY,
            title = stringRes(R.string.text_color_primary),
            isEnabled = {
                it.getBoolean(XposedKey.HEADER_CLOCK_CUSTOM_COLOR) &&
                        it.getBoolean(XposedKey.CUSTOM_HEADER_CLOCK)
            },
            isVisible = { it.getBoolean(XposedKey.HEADER_CLOCK_CUSTOM_COLOR) }
        )

        colorPicker(
            key = XposedKey.HEADER_CLOCK_COLOR_TEXT_INVERSE,
            title = stringRes(R.string.text_color_inverse),
            isEnabled = {
                it.getBoolean(XposedKey.HEADER_CLOCK_CUSTOM_COLOR) &&
                        it.getBoolean(XposedKey.CUSTOM_HEADER_CLOCK)
            },
            isVisible = { it.getBoolean(XposedKey.HEADER_CLOCK_CUSTOM_COLOR) }
        )
    }
}

@Composable
fun HeaderClockScreen(
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    PreferenceListener(key = XposedKey.CUSTOM_HEADER_CLOCK) {
        systemActionViewModel?.shouldRestartSystemUI()
    }

    PreferenceScreen(
        items = headerClockPreferences,
        title = stringResource(R.string.activity_title_header_clock),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun HeaderClockScreenPreview() {
    PreviewComposable {
        HeaderClockScreen(null)
    }
}