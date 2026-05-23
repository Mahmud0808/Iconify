package com.drdisagree.iconify.features.home.tweaks.cornerradius.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import com.drdisagree.iconify.data.keys.TweaksKey
import com.drdisagree.iconify.features.home.tweaks.cornerradius.components.CornerRadiusBoxes
import com.drdisagree.iconify.features.home.tweaks.cornerradius.components.PillAppearanceApplySection
import com.drdisagree.iconify.features.home.tweaks.cornerradius.viewmodels.CornerRadiusViewModel
import kotlin.math.roundToInt

val cornerRadiusPreferences = preferenceScreen {
    composable(key = "corner_radius_preview") {
        CornerRadiusBoxes()
    }

    category {
        slider(
            key = TweaksKey.UI_CORNER_RADIUS,
            title = stringRes(R.string.corner_radius_title),
            min = 0f,
            max = 44f,
            steps = 43,
            valueLabel = { "${it.roundToInt()}dp" },
            applyImmediately = true
        )
    }

    composable(key = "corner_radius_apply_section") {
        PillAppearanceApplySection()
    }
}

@Composable
fun CornerRadiusScreen(
    cornerRadiusViewModel: CornerRadiusViewModel = hiltViewModel()
) {
    val prefController = LocalPreferenceController.current
    val isApplying by cornerRadiusViewModel.isLoading.collectAsStateWithLifecycle()

    if (isApplying) {
        LoadingDialog()
    }

    ToastAppliedEvent(cornerRadiusViewModel.uiEvent)

    PreferenceListener { event ->
        when (event.key) {
            TweaksKey.UI_CORNER_RADIUS_SAVED.name -> {
                val value = (event.newValue as PrefValue.FloatValue).v
                cornerRadiusViewModel.applyCornerRadius(value.roundToInt())
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val cornerRadius = prefController.getFloat(TweaksKey.UI_CORNER_RADIUS_SAVED)

            val shouldResetRadius = cornerRadius != -1f &&
                    prefController.getFloat(TweaksKey.UI_CORNER_RADIUS) != cornerRadius

            if (shouldResetRadius) {
                prefController.setFloat(TweaksKey.UI_CORNER_RADIUS, cornerRadius)
            }
        }
    }

    CornerRadiusScreenContent()
}

@Composable
private fun CornerRadiusScreenContent() {
    PreferenceScreen(
        items = cornerRadiusPreferences,
        title = stringResource(R.string.activity_title_ui_roundness),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun CornerRadiusScreenPreview() {
    PreviewComposable {
        CornerRadiusScreenContent()
    }
}