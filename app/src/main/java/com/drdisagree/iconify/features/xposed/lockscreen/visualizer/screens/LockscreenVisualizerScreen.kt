package com.drdisagree.iconify.features.xposed.lockscreen.visualizer.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.arrayRes
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.common.viewmodels.SystemActionViewModel
import java.util.Locale
import kotlin.math.roundToInt

val lockscreenVisualizerPreferences = preferenceScreen {
    category {
        switch(
            key = XposedKey.LOCKSCREEN_VISUALIZER,
            isMasterSwitch = true,
            title = stringRes(R.string.activity_title_lockscreen_visualizer),
        )
    }

    category {
        listPref(
            key = XposedKey.LOCKSCREEN_VISUALIZER_COLOR_MODE,
            title = stringRes(R.string.lockscreen_visualizer_color_mode_title),
            entries = arrayRes(R.array.lockscreen_visualizer_color_mode_entries),
            entryValues = arrayRes(R.array.lockscreen_visualizer_color_mode_values),
            isEnabled = { pref -> pref.getBoolean(XposedKey.LOCKSCREEN_VISUALIZER) }
        )

        colorPicker(
            key = XposedKey.LOCKSCREEN_VISUALIZER_STATIC_COLOR,
            title = stringRes(R.string.lockscreen_visualizer_static_color_title),
            showAlphaSlider = true,
            isEnabled = { pref -> pref.getBoolean(XposedKey.LOCKSCREEN_VISUALIZER) },
            isVisible = { pref ->
                pref.getString(XposedKey.LOCKSCREEN_VISUALIZER_COLOR_MODE) in setOf("0")
            }
        )

        colorPicker(
            key = XposedKey.LOCKSCREEN_VISUALIZER_GRADIENT_COLOR_START,
            title = stringRes(R.string.lockscreen_visualizer_gradient_start_title),
            showAlphaSlider = true,
            isEnabled = { pref -> pref.getBoolean(XposedKey.LOCKSCREEN_VISUALIZER) },
            isVisible = { pref -> pref.getString(XposedKey.LOCKSCREEN_VISUALIZER_COLOR_MODE) == "2" }
        )

        colorPicker(
            key = XposedKey.LOCKSCREEN_VISUALIZER_GRADIENT_COLOR_END,
            title = stringRes(R.string.lockscreen_visualizer_gradient_end_title),
            showAlphaSlider = true,
            isEnabled = { pref -> pref.getBoolean(XposedKey.LOCKSCREEN_VISUALIZER) },
            isVisible = { pref -> pref.getString(XposedKey.LOCKSCREEN_VISUALIZER_COLOR_MODE) == "2" }
        )

        slider(
            key = XposedKey.LOCKSCREEN_VISUALIZER_LAVA_SPEED,
            title = stringRes(R.string.lockscreen_visualizer_lava_speed_title),
            min = 5f,
            max = 60f,
            valueLabel = { "${it.roundToInt()}s" },
            isEnabled = { pref -> pref.getBoolean(XposedKey.LOCKSCREEN_VISUALIZER) },
            isVisible = { pref -> pref.getString(XposedKey.LOCKSCREEN_VISUALIZER_COLOR_MODE) == "1" }
        )

        listPref(
            key = XposedKey.LOCKSCREEN_VISUALIZER_FPS,
            title = stringRes(R.string.lockscreen_visualizer_fps_title),
            entries = arrayRes(R.array.lockscreen_visualizer_fps_entries),
            entryValues = arrayRes(R.array.lockscreen_visualizer_fps_values),
            isEnabled = { pref -> pref.getBoolean(XposedKey.LOCKSCREEN_VISUALIZER) }
        )

        slider(
            key = XposedKey.LOCKSCREEN_VISUALIZER_SENSITIVITY,
            title = stringRes(R.string.lockscreen_visualizer_sensitivity_title),
            min = 0.5f,
            max = 3.0f,
            valueLabel = { String.format(Locale.getDefault(), "%.2fx", it) },
            isEnabled = { pref -> pref.getBoolean(XposedKey.LOCKSCREEN_VISUALIZER) }
        )

        slider(
            key = XposedKey.LOCKSCREEN_VISUALIZER_HEIGHT,
            title = stringRes(R.string.lockscreen_visualizer_height_title),
            min = 180f,
            max = 760f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { pref -> pref.getBoolean(XposedKey.LOCKSCREEN_VISUALIZER) }
        )

        slider(
            key = XposedKey.LOCKSCREEN_VISUALIZER_BAR_THICKNESS,
            title = stringRes(R.string.lockscreen_visualizer_bar_thickness_title),
            min = 6f,
            max = 32f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { pref -> pref.getBoolean(XposedKey.LOCKSCREEN_VISUALIZER) }
        )

        slider(
            key = XposedKey.LOCKSCREEN_VISUALIZER_SMOOTHNESS,
            title = stringRes(R.string.lockscreen_visualizer_smoothness_title),
            min = 0f,
            max = 100f,
            valueLabel = { "${it.roundToInt()}%" },
            isEnabled = { pref -> pref.getBoolean(XposedKey.LOCKSCREEN_VISUALIZER) }
        )
    }
}

@Composable
fun LockscreenVisualizerScreen(
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    PreferenceListener { event ->
        when (event.key) {
            XposedKey.LOCKSCREEN_VISUALIZER.name,
            XposedKey.LOCKSCREEN_VISUALIZER_COLOR_MODE.name,
            XposedKey.LOCKSCREEN_VISUALIZER_STATIC_COLOR.name,
            XposedKey.LOCKSCREEN_VISUALIZER_GRADIENT_COLOR_START.name,
            XposedKey.LOCKSCREEN_VISUALIZER_GRADIENT_COLOR_END.name,
            XposedKey.LOCKSCREEN_VISUALIZER_LAVA_SPEED.name,
            XposedKey.LOCKSCREEN_VISUALIZER_SENSITIVITY.name,
            XposedKey.LOCKSCREEN_VISUALIZER_HEIGHT.name,
            XposedKey.LOCKSCREEN_VISUALIZER_BAR_THICKNESS.name,
            XposedKey.LOCKSCREEN_VISUALIZER_SMOOTHNESS.name,
            XposedKey.LOCKSCREEN_VISUALIZER_FPS.name -> {
                systemActionViewModel?.shouldRestartSystemUI()
            }
        }
    }

    PreferenceScreen(
        items = lockscreenVisualizerPreferences,
        title = stringResource(R.string.activity_title_lockscreen_visualizer),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun LockscreenVisualizerScreenPreview() {
    PreviewComposable {
        LockscreenVisualizerScreen(null)
    }
}
