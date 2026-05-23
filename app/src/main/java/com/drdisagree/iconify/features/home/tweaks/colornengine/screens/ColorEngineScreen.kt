package com.drdisagree.iconify.features.home.tweaks.colornengine.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.preferences.PrefValue
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.dialogs.LoadingDialog
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.ToastAppliedEvent
import com.drdisagree.iconify.core.utils.AppUtils.launchAppThrowError
import com.drdisagree.iconify.core.utils.AppUtils.openUrl
import com.drdisagree.iconify.data.common.Const.COLORBLENDR_PACKAGE
import com.drdisagree.iconify.data.common.Const.COLORBLENDR_URL
import com.drdisagree.iconify.data.keys.TweaksKey
import com.drdisagree.iconify.features.home.tweaks.colornengine.components.ColorEngineGradient
import com.drdisagree.iconify.features.home.tweaks.colornengine.models.ColorMode
import com.drdisagree.iconify.features.home.tweaks.colornengine.viewmodels.ColorEngineViewModel

fun colorEnginePreferences(
    onAdvancedClick: () -> Unit = {}
) = preferenceScreen {
    category {
        composable(key = "color_preview") {
            ColorEngineGradient()
        }
    }

    category(title = stringRes(R.string.activity_title_basic_colors)) {
        colorPicker(
            key = TweaksKey.BASIC_COLOR_PRIMARY,
            title = stringRes(R.string.primary_color),
            summary = { stringRes(R.string.primary_color_desc) },
            showAlphaSlider = false,
            isEnabled = {
                !it.getBoolean(TweaksKey.MONET_ACCENT) &&
                        !it.getBoolean(TweaksKey.MONET_GRADIENT)
            },
        )

        colorPicker(
            key = TweaksKey.BASIC_COLOR_SECONDARY,
            title = stringRes(R.string.secondary_color),
            summary = { stringRes(R.string.secondary_color_desc) },
            showAlphaSlider = false,
            isEnabled = {
                !it.getBoolean(TweaksKey.MONET_ACCENT) &&
                        !it.getBoolean(TweaksKey.MONET_GRADIENT)
            },
        )
    }

    category(title = stringRes(R.string.section_title_stock_colors)) {
        switch(
            key = TweaksKey.MONET_ACCENT,
            title = stringRes(R.string.apply_monet_accent_title),
            summary = { stringRes(R.string.apply_monet_accent_desc) }
        )

        switch(
            key = TweaksKey.MONET_GRADIENT,
            title = stringRes(R.string.apply_monet_gradient_title),
            summary = { stringRes(R.string.apply_monet_gradient_desc) }
        )
    }

    category {
        action(
            key = "advancedColorCustomization",
            title = stringRes(R.string.advanced_color_settings_title),
            summary = { stringRes(R.string.advanced_color_settings_desc) },
            onClick = { onAdvancedClick() }
        )
    }
}

@Composable
fun ColorEngineScreen(
    colorEngineViewModel: ColorEngineViewModel = hiltViewModel()
) {
    val prefController = LocalPreferenceController.current
    val isApplying by colorEngineViewModel.isLoading.collectAsStateWithLifecycle()

    if (isApplying) {
        LoadingDialog()
    }

    ToastAppliedEvent(colorEngineViewModel.uiEvent)

    PreferenceListener { event ->
        when (event.key) {
            TweaksKey.BASIC_COLOR_PRIMARY.name -> {
                val color = (event.newValue as PrefValue.StringValue).v
                colorEngineViewModel.applyPrimaryColor(color)
            }

            TweaksKey.BASIC_COLOR_SECONDARY.name -> {
                val color = (event.newValue as PrefValue.StringValue).v
                colorEngineViewModel.applySecondaryColor(color)
            }

            TweaksKey.MONET_ACCENT.name -> {
                val enable = (event.newValue as PrefValue.BoolValue).v

                if (enable) {
                    prefController.setBoolean(TweaksKey.MONET_GRADIENT, false)
                    colorEngineViewModel.setColorMode(ColorMode.MONET_ACCENT)
                } else {
                    val gradientEnabled = prefController.getBoolean(TweaksKey.MONET_GRADIENT)

                    if (!gradientEnabled) {
                        colorEngineViewModel.setColorMode(ColorMode.BASIC)
                    }
                }
            }

            TweaksKey.MONET_GRADIENT.name -> {
                val enable = (event.newValue as PrefValue.BoolValue).v

                if (enable) {
                    prefController.setBoolean(TweaksKey.MONET_ACCENT, false)
                    colorEngineViewModel.setColorMode(ColorMode.MONET_GRADIENT)
                } else {
                    val accentEnabled = prefController.getBoolean(TweaksKey.MONET_ACCENT)

                    if (!accentEnabled) {
                        colorEngineViewModel.setColorMode(ColorMode.BASIC)
                    }
                }
            }
        }
    }

    ColorEngineScreenContent()
}

@Composable
private fun ColorEngineScreenContent() {
    val context = LocalContext.current
    val activity = LocalActivity.current

    PreferenceScreen(
        items = colorEnginePreferences(
            onAdvancedClick = {
                try {
                    launchAppThrowError(activity!!, COLORBLENDR_PACKAGE)
                } catch (_: Exception) {
                    openUrl(context, COLORBLENDR_URL)
                }
            }
        ),
        title = stringResource(R.string.activity_title_color_engine),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun ColorEngineScreenPreview() {
    PreviewComposable {
        ColorEngineScreenContent()
    }
}