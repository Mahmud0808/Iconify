package com.drdisagree.iconify.features.home.tweaks.mediaplayer.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PrefValue
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.dialogs.LoadingDialog
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.ToastAppliedEvent
import com.drdisagree.iconify.data.keys.TweaksKey
import com.drdisagree.iconify.features.home.tweaks.mediaplayer.viewmodels.MediaPlayerViewModel

val mediaPlayerPreferences = preferenceScreen {
    category {
        switch(
            key = TweaksKey.DISABLE_PROGRESS_WAVE,
            title = stringRes(R.string.disable_progress_wave_title),
            summary = { stringRes(R.string.disable_progress_wave_desc) }
        )
    }
}

@Composable
fun MediaPlayerScreen(
    mediaPlayerViewModel: MediaPlayerViewModel = hiltViewModel()
) {
    val isApplying by mediaPlayerViewModel.isLoading.collectAsStateWithLifecycle()

    if (isApplying) {
        LoadingDialog()
    }

    ToastAppliedEvent(mediaPlayerViewModel.uiEvent)

    PreferenceListener { event ->
        when (event.key) {
            TweaksKey.DISABLE_PROGRESS_WAVE.name -> {
                val enable = (event.newValue as PrefValue.BoolValue).v
                mediaPlayerViewModel.toggleDisableProgressWave(enable)
            }
        }
    }

    MediaPlayerScreenContent()
}

@Composable
private fun MediaPlayerScreenContent() {
    PreferenceScreen(
        items = mediaPlayerPreferences,
        title = stringResource(R.string.activity_title_media_player),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun MediaPlayerScreenPreview() {
    PreviewComposable {
        MediaPlayerScreenContent()
    }
}