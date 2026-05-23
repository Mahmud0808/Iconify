package com.drdisagree.iconify.features.xposed.lockscreen.albumart.screens

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
import kotlin.math.roundToInt

val lsAlbumArtPreferences = preferenceScreen {
    category {
        switch(
            key = XposedKey.ALBUM_ART_ON_LOCKSCREEN,
            isMasterSwitch = true,
            title = stringRes(R.string.media_art_title),
        )
    }

    category {
        listPref(
            key = XposedKey.ALBUM_ART_ON_LOCKSCREEN_FILTER,
            title = stringRes(R.string.albumart_filter_title),
            entries = arrayRes(R.array.lockscreen_albumart_filter_entries),
            entryValues = arrayRes(R.array.lockscreen_albumart_filter_values),
            isEnabled = { it.getBoolean(XposedKey.ALBUM_ART_ON_LOCKSCREEN) }
        )

        slider(
            key = XposedKey.ALBUM_ART_ON_LOCKSCREEN_BLUR,
            title = stringRes(R.string.media_art_blur_level_title),
            min = 0f,
            max = 100f,
            valueLabel = { "${it.roundToInt()}%" },
            isVisible = { pref ->
                pref.getString(XposedKey.ALBUM_ART_ON_LOCKSCREEN_FILTER) in setOf("3", "4")
            },
            isEnabled = { it.getBoolean(XposedKey.ALBUM_ART_ON_LOCKSCREEN) }
        )
    }
}

@Composable
fun LockscreenAlbumArtScreen(
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    PreferenceListener { event ->
        when (event.key) {
            XposedKey.ALBUM_ART_ON_LOCKSCREEN.name -> {
                systemActionViewModel?.shouldRestartSystemUI()
            }
        }
    }

    PreferenceScreen(
        items = lsAlbumArtPreferences,
        title = stringResource(R.string.activity_title_lockscreen_album_art),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun LockscreenAlbumArtScreenPreview() {
    PreviewComposable {
        LockscreenAlbumArtScreen()
    }
}