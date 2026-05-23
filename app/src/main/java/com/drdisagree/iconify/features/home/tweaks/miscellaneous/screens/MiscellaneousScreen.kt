package com.drdisagree.iconify.features.home.tweaks.miscellaneous.screens

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
import com.drdisagree.iconify.features.home.tweaks.miscellaneous.viewmodels.MiscellaneousViewModel

val miscellaneousPreferences = preferenceScreen {
    category(title = stringRes(R.string.section_title_tablet_mode)) {
        switch(
            key = TweaksKey.TABLET_LANDSCAPE,
            title = stringRes(R.string.tablet_landscape_title),
            summary = { stringRes(R.string.tablet_landscape_desc) }
        )

        switch(
            key = TweaksKey.TABLET_HEADER,
            title = stringRes(R.string.tablet_header_title),
            summary = { stringRes(R.string.tablet_header_desc) }
        )

        switch(
            key = TweaksKey.NOTCH_BAR_KILLER,
            title = stringRes(R.string.notch_bar_killer_title),
            summary = { stringRes(R.string.notch_bar_killer_desc) }
        )
    }

    category(title = stringRes(R.string.section_title_privacy_chip)) {
        switch(
            key = TweaksKey.ACCENT_PRIVACY_CHIP,
            title = stringRes(R.string.accent_privacy_chip_title),
            summary = { stringRes(R.string.accent_privacy_chip_desc) }
        )
    }
}

@Composable
fun MiscellaneousScreen(
    miscellaneousViewModel: MiscellaneousViewModel = hiltViewModel()
) {
    val isApplying by miscellaneousViewModel.isLoading.collectAsStateWithLifecycle()

    if (isApplying) {
        LoadingDialog()
    }

    ToastAppliedEvent(miscellaneousViewModel.uiEvent)

    PreferenceListener { event ->
        when (event.key) {
            TweaksKey.TABLET_LANDSCAPE.name -> {
                val enable = (event.newValue as PrefValue.BoolValue).v
                miscellaneousViewModel.toggleTabletLandscape(enable)
            }

            TweaksKey.TABLET_HEADER.name -> {
                val enable = (event.newValue as PrefValue.BoolValue).v
                miscellaneousViewModel.toggleTabletHeader(enable)
            }

            TweaksKey.NOTCH_BAR_KILLER.name -> {
                val enable = (event.newValue as PrefValue.BoolValue).v
                miscellaneousViewModel.toggleNotchBarKiller(enable)
            }

            TweaksKey.ACCENT_PRIVACY_CHIP.name -> {
                val enable = (event.newValue as PrefValue.BoolValue).v
                miscellaneousViewModel.toggleAccentPrivacyChip(enable)
            }
        }
    }

    MiscellaneousScreenContent()
}

@Composable
private fun MiscellaneousScreenContent() {
    PreferenceScreen(
        items = miscellaneousPreferences,
        title = stringResource(R.string.activity_title_miscellaneous),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun MiscellaneousScreenPreview() {
    PreviewComposable {
        MiscellaneousScreenContent()
    }
}