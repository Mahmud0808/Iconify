package com.drdisagree.iconify.features.xposed.volumepanel.screens

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.PrefValue
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.utils.SystemUtils
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.common.viewmodels.SystemActionViewModel
import kotlin.math.roundToInt

val volumePanelPreferences = preferenceScreen {
    category {
        switch(
            key = XposedKey.VOLUME_PANEL_PERCENTAGE,
            title = stringRes(R.string.volume_percentage_title),
            summary = { stringRes(R.string.volume_percentage_desc) },
        )

        switch(
            key = XposedKey.VOLUME_PANEL_SAFETY_WARNING,
            title = stringRes(R.string.safety_warning_title),
            summary = { stringRes(R.string.safety_warning_desc) },
        )

        switch(
            key = XposedKey.VOLUME_PANEL_APP_VOLUME,
            title = stringRes(R.string.app_volume_button_title),
            summary = { stringRes(R.string.app_volume_button_desc) },
        )

        switch(
            key = XposedKey.VOLUME_PANEL_MULTI_AUDIO_FOCUS,
            title = stringRes(R.string.multi_audio_focus_title),
            summary = { stringRes(R.string.multi_audio_focus_desc) },
        )
    }

    category(title = stringRes(R.string.section_title_volume_shape)) {
        slider(
            key = XposedKey.VOLUME_CORNER_RADIUS_OFFSET,
            title = stringRes(R.string.volume_corner_radius_offset_title),
            summary = { stringRes(R.string.volume_corner_radius_offset_desc) },
            min = -50f,
            max = 50f,
            valueLabel = { value ->
                val offset = value.roundToInt()
                if (offset > 0) "+${offset}dp" else "${offset}dp"
            },
            showResetButton = true,
            isVisible = { Build.VERSION.SDK_INT >= 36 },
        )

        switch(
            key = XposedKey.HIDE_VOLUME_SLIDER_THUMB,
            title = stringRes(R.string.hide_volume_slider_thumb_title),
            summary = { stringRes(R.string.hide_volume_slider_thumb_desc) },
            isVisible = { Build.VERSION.SDK_INT >= 36 },
        )
    }
}

@Composable
fun VolumePanelScreen(
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    PreferenceListener { event ->
        when (event.key) {
            XposedKey.VOLUME_PANEL_PERCENTAGE.name,
            XposedKey.VOLUME_PANEL_SAFETY_WARNING.name,
            XposedKey.VOLUME_PANEL_APP_VOLUME.name,
            XposedKey.VOLUME_CORNER_RADIUS_OFFSET.name,
            XposedKey.HIDE_VOLUME_SLIDER_THUMB.name -> {
                systemActionViewModel?.shouldRestartSystemUI()
            }

            XposedKey.VOLUME_PANEL_MULTI_AUDIO_FOCUS.name -> {
                val enabled = (event.newValue as? PrefValue.BoolValue)?.v ?: false
                SystemUtils.setMultiAudioFocusEnabled(enabled)
                systemActionViewModel?.shouldRebootDevice()
            }
        }
    }

    PreferenceScreen(
        items = volumePanelPreferences,
        title = stringResource(R.string.activity_title_volume_panel),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun VolumePanelScreenPreview() {
    PreviewComposable {
        VolumePanelScreen(null)
    }
}