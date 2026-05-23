package com.drdisagree.iconify.features.xposed.quicksettings.transparency.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.preferences.PrefValue
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.utils.SystemUtils
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.common.viewmodels.SystemActionViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

val qsTransparencyPreferences = preferenceScreen {
    category(title = stringRes(R.string.section_title_transparency)) {
        switch(
            key = XposedKey.QUICK_SETTINGS_TRANSPARENCY,
            title = stringRes(R.string.transparent_qs_panel_title),
            summary = { stringRes(R.string.transparent_qs_panel_desc) },
        )

        switch(
            key = XposedKey.NOTIFICATION_TRANSPARENCY,
            title = stringRes(R.string.transparent_notif_shade_title),
            summary = { stringRes(R.string.transparent_notif_shade_desc) },
        )

        switch(
            key = XposedKey.LOCKSCREEN_SHADE,
            title = stringRes(R.string.keep_lockscreen_shade_title),
            summary = { stringRes(R.string.keep_lockscreen_shade_desc) },
            isVisible = { pref ->
                pref.getBoolean(XposedKey.QUICK_SETTINGS_TRANSPARENCY) ||
                        pref.getBoolean(XposedKey.NOTIFICATION_TRANSPARENCY)
            }
        )

        slider(
            key = XposedKey.QUICK_SETTINGS_ALPHA_LEVEL,
            title = stringRes(R.string.qs_background_opacity_title),
            min = 0f,
            max = 100f,
            valueLabel = { "${it.roundToInt()}%" },
            isVisible = { pref ->
                pref.getBoolean(XposedKey.QUICK_SETTINGS_TRANSPARENCY) ||
                        pref.getBoolean(XposedKey.NOTIFICATION_TRANSPARENCY)
            }
        )
    }

    category(title = stringRes(R.string.section_title_blur)) {
        switch(
            key = XposedKey.QUICK_SETTINGS_BLUR,
            title = stringRes(R.string.window_level_blur_title),
            summary = { stringRes(R.string.window_level_blur_desc) },
        )

        switch(
            key = XposedKey.QUICK_SETTINGS_AGGRESSIVE_BLUR,
            title = stringRes(R.string.aggressive_blur_title),
            summary = { stringRes(R.string.aggressive_blur_desc) },
            isVisible = { pref -> pref.getBoolean(XposedKey.QUICK_SETTINGS_BLUR) },
        )

        slider(
            key = XposedKey.QUICK_SETTINGS_BLUR_RADIUS,
            title = stringRes(R.string.blur_intensity_title),
            min = 0f,
            max = 15f,
            steps = 14,
            valueLabel = { "${it.roundToInt()}px" },
            isVisible = { pref -> pref.getBoolean(XposedKey.QUICK_SETTINGS_BLUR) },
        )
    }
}

@Composable
fun QsTransparencyScreen(
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val prefController = LocalPreferenceController.current

    LaunchedEffect(Unit) {
        if (SystemUtils.isBlurEnabled(force = false)) {
            prefController.setBoolean(XposedKey.QUICK_SETTINGS_BLUR, true)
        }
        if (SystemUtils.isBlurEnabled(force = true)) {
            prefController.setBoolean(XposedKey.QUICK_SETTINGS_AGGRESSIVE_BLUR, true)
        }
    }

    PreferenceListener(key = null) { event ->
        when (event.key) {
            XposedKey.QUICK_SETTINGS_BLUR.name -> {
                val isEnabled = (event.newValue as PrefValue.BoolValue).v

                scope.launch {
                    if (isEnabled) {
                        SystemUtils.enableBlur(force = false)
                    } else {
                        prefController.set(XposedKey.QUICK_SETTINGS_AGGRESSIVE_BLUR)
                        SystemUtils.disableBlur(force = false)
                    }

                    systemActionViewModel?.shouldRebootDevice()
                }
            }

            XposedKey.QUICK_SETTINGS_AGGRESSIVE_BLUR.name -> {
                val isEnabled = (event.newValue as PrefValue.BoolValue).v

                scope.launch {
                    if (isEnabled) {
                        SystemUtils.enableBlur(force = true)
                    } else {
                        SystemUtils.disableBlur(force = true)
                    }

                    systemActionViewModel?.shouldRebootDevice()
                }
            }

            XposedKey.QUICK_SETTINGS_BLUR_RADIUS.name -> {
                systemActionViewModel?.shouldRestartSystemUI()
            }
        }
    }

    PreferenceScreen(
        items = qsTransparencyPreferences,
        title = stringResource(R.string.activity_title_transparency_blur),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun QsTransparencyScreenPreview() {
    PreviewComposable {
        QsTransparencyScreen(null)
    }
}