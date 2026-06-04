package com.drdisagree.iconify.features.xposed.volumepanel.screens

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
}

@Composable
fun VolumePanelScreen(
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    PreferenceListener { event ->
        when (event.key) {
            XposedKey.VOLUME_PANEL_PERCENTAGE.name,
            XposedKey.VOLUME_PANEL_SAFETY_WARNING.name,
            XposedKey.VOLUME_PANEL_APP_VOLUME.name -> {
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