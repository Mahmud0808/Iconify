package com.drdisagree.iconify.features.xposed.statusbar.clockchip.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PrefValue
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.arrayRes
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.common.viewmodels.SystemActionViewModel

val clockChipPreferences = preferenceScreen {
    category {
        switch(
            key = XposedKey.STATUSBAR_CLOCK_CHIP,
            isMasterSwitch = true,
            title = stringRes(R.string.sb_clock_background_chip_title),
        )
    }

    category(title = stringRes(R.string.section_title_text)) {
        listPref(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_TEXT_COLOR_OPTION,
            title = stringRes(R.string.statusbar_clock_text_color_title),
            entries = arrayRes(R.array.statusbar_clock_text_color_entries),
            entryValues = arrayRes(R.array.statusbar_clock_text_color_values),
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) },
        )

        colorPicker(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_TEXT_COLOR_CODE,
            title = stringRes(R.string.clock_text_color_picker_title),
            summary = { _, _ -> stringRes(R.string.clock_text_color_picker_desc) },
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) },
            isVisible = { it.getString(XposedKey.STATUSBAR_CLOCK_CHIP_TEXT_COLOR_OPTION) == "2" }
        )
    }

    category(title = stringRes(R.string.section_title_background)) {
        listPref(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_FILL_COLOR_OPTION,
            title = stringRes(R.string.statusbar_clock_fill_color_title),
            entries = arrayRes(R.array.statusbar_clock_fill_color_entries),
            entryValues = arrayRes(R.array.statusbar_clock_fill_color_values),
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) },
        )

        listPref(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_FILL_COLOR_GRADIENT_DIRECTION,
            title = stringRes(R.string.gradient_direction),
            entries = arrayRes(R.array.gradient_direction_entries),
            entryValues = arrayRes(R.array.gradient_direction_values),
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) },
            isVisible = { it.getString(XposedKey.STATUSBAR_CLOCK_CHIP_FILL_COLOR_OPTION) == "2" }
        )

        colorPicker(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_FILL_COLOR_GRADIENT_COLOR1,
            title = stringRes(R.string.fill_start_color),
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) },
            isVisible = { it.getString(XposedKey.STATUSBAR_CLOCK_CHIP_FILL_COLOR_OPTION) == "2" }
        )

        colorPicker(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_FILL_COLOR_GRADIENT_COLOR2,
            title = stringRes(R.string.fill_end_color),
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) },
            isVisible = { it.getString(XposedKey.STATUSBAR_CLOCK_CHIP_FILL_COLOR_OPTION) == "2" }
        )
    }

    category(title = stringRes(R.string.section_title_border)) {
        switch(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_BORDER,
            title = stringRes(R.string.enable_border),
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) }
        )

        listPref(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_BORDER_COLOR_OPTION,
            title = stringRes(R.string.border_color_title),
            entries = arrayRes(R.array.statusbar_clock_border_color_entries),
            entryValues = arrayRes(R.array.statusbar_clock_border_color_values),
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) },
            isVisible = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP_BORDER) }
        )

        colorPicker(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_BORDER_COLOR_CODE,
            title = stringRes(R.string.border_color_custom_title),
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) },
            isVisible = {
                it.getString(XposedKey.STATUSBAR_CLOCK_CHIP_BORDER_COLOR_OPTION) == "1" &&
                        it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP_BORDER)
            }
        )

        switch(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_DASHED_BORDER,
            title = stringRes(R.string.dashed_border),
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) },
            isVisible = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP_BORDER) }
        )

        slider(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_DASHED_BORDER_WIDTH,
            title = stringRes(R.string.dash_width),
            min = 0f,
            max = 12f,
            steps = 11,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) },
            isVisible = {
                it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP_DASHED_BORDER) &&
                        it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP_BORDER)
            }
        )

        slider(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_DASHED_BORDER_GAP,
            title = stringRes(R.string.dash_gap),
            min = 0f,
            max = 12f,
            steps = 11,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) },
            isVisible = {
                it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP_DASHED_BORDER) &&
                        it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP_BORDER)
            }
        )

        slider(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_BORDER_THICKNESS,
            title = stringRes(R.string.border_thickness),
            min = 0f,
            max = 12f,
            steps = 11,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) },
            isVisible = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP_BORDER) }
        )
    }

    category(title = stringRes(R.string.section_title_padding)) {
        slider(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_PADDING_LEFT,
            title = stringRes(R.string.padding_left),
            min = 0f,
            max = 12f,
            steps = 11,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) }
        )

        slider(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_PADDING_RIGHT,
            title = stringRes(R.string.padding_right),
            min = 0f,
            max = 12f,
            steps = 11,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) }
        )

        slider(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_PADDING_TOP,
            title = stringRes(R.string.padding_top),
            min = 0f,
            max = 12f,
            steps = 11,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) }
        )

        slider(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_PADDING_BOTTOM,
            title = stringRes(R.string.padding_bottom),
            min = 0f,
            max = 12f,
            steps = 11,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) }
        )
    }

    category(title = stringRes(R.string.section_title_corner_radius)) {
        slider(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_RADIUS_TOP_LEFT,
            title = stringRes(R.string.top_left_radius),
            min = 0f,
            max = 40f,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) }
        )

        slider(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_RADIUS_TOP_RIGHT,
            title = stringRes(R.string.top_right_radius),
            min = 0f,
            max = 40f,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) }
        )

        slider(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_RADIUS_BOTTOM_LEFT,
            title = stringRes(R.string.bottom_left_radius),
            min = 0f,
            max = 40f,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) }
        )

        slider(
            key = XposedKey.STATUSBAR_CLOCK_CHIP_RADIUS_BOTTOM_RIGHT,
            title = stringRes(R.string.bottom_right_radius),
            min = 0f,
            max = 40f,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) }
        )
    }
}

@Composable
fun ClockChipScreen(
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    PreferenceListener(key = XposedKey.STATUSBAR_CLOCK_CHIP) { event ->
        val newValue = (event.newValue as PrefValue.BoolValue).v
        if (!newValue) {
            systemActionViewModel?.shouldRestartSystemUI()
        }
    }

    PreferenceScreen(
        items = clockChipPreferences,
        title = stringResource(R.string.activity_title_background_chip),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun ClockChipScreenPreview() {
    PreviewComposable {
        ClockChipScreen(null)
    }
}