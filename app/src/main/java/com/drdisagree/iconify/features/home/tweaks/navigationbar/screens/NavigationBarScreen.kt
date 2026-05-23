package com.drdisagree.iconify.features.home.tweaks.navigationbar.screens

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
import com.drdisagree.iconify.features.home.tweaks.navigationbar.components.PillAppearanceApplySection
import com.drdisagree.iconify.features.home.tweaks.navigationbar.viewmodels.NavigationBarViewModel
import kotlin.math.roundToInt

val navigationbarPreferences = preferenceScreen {
    category(title = stringRes(R.string.section_title_display_mode)) {
        switch(
            key = TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_FULL_SCREEN,
            title = stringRes(R.string.navbar_fullscreen_title),
            summary = { stringRes(R.string.navbar_fullscreen_desc) },
        )

        switch(
            key = TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_IMMERSIVE,
            title = stringRes(R.string.navbar_immersive_title),
            summary = { stringRes(R.string.navbar_immersive_desc) },
        )

        switch(
            key = TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_GCAM_LAG_FIX,
            title = stringRes(R.string.navbar_gcam_lag_fix_title),
            summary = { stringRes(R.string.navbar_gcam_lag_fix_desc) },
            isVisible = {
                it.getBoolean(TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_FULL_SCREEN) ||
                        it.getBoolean(TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_IMMERSIVE)
            }
        )
    }

    category(title = stringRes(R.string.section_title_buttons)) {
        switch(
            key = TweaksKey.NAVIGATION_BAR_HIDE_KEYBOARD_BUTTONS,
            title = stringRes(R.string.navbar_hide_kb_buttons_title),
            summary = { stringRes(R.string.navbar_hide_kb_buttons_desc) },
        )
    }

    category(title = stringRes(R.string.section_title_gesture)) {
        switch(
            key = TweaksKey.NAVIGATION_BAR_LOWER_SENSITIVITY,
            title = stringRes(R.string.navbar_lower_sensitivity_title),
            summary = { stringRes(R.string.navbar_lower_sensitivity_desc) },
        )

        switch(
            key = TweaksKey.NAVIGATION_BAR_DISABLE_LEFT_GESTURE,
            title = stringRes(R.string.navbar_disable_left_gesture_title),
            summary = { stringRes(R.string.navbar_disable_left_gesture_desc) },
        )

        switch(
            key = TweaksKey.NAVIGATION_BAR_DISABLE_RIGHT_GESTURE,
            title = stringRes(R.string.navbar_disable_right_gesture_title),
            summary = { stringRes(R.string.navbar_disable_right_gesture_desc) },
        )
    }

    category(title = stringRes(R.string.section_title_pill_navigation)) {
        switch(
            key = TweaksKey.NAVIGATION_BAR_HIDE_PILL,
            title = stringRes(R.string.navbar_hide_pill_title),
            summary = { stringRes(R.string.navbar_hide_pill_desc) },
        )

        switch(
            key = TweaksKey.NAVIGATION_BAR_MONET_PILL,
            title = stringRes(R.string.navbar_monet_pill_title),
            summary = { stringRes(R.string.navbar_monet_pill_desc) },
        )
    }

    category(title = stringRes(R.string.section_title_pill_appearance)) {
        slider(
            key = TweaksKey.NAVIGATION_BAR_PILL_WIDTH,
            title = stringRes(R.string.pill_width_title),
            min = 40f,
            max = 300f,
            valueLabel = { "${it.roundToInt()}dp" },
            isVisible = {
                val hidePill = it.getBoolean(TweaksKey.NAVIGATION_BAR_HIDE_PILL)
                val displayModeFullScreen =
                    it.getBoolean(TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_FULL_SCREEN)
                val displayModeImmersive =
                    it.getBoolean(TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_IMMERSIVE)

                !hidePill && !displayModeFullScreen && !displayModeImmersive
            }
        )

        slider(
            key = TweaksKey.NAVIGATION_BAR_PILL_THICKNESS,
            title = stringRes(R.string.pill_thickness_title),
            min = 0f,
            max = 12f,
            steps = 11,
            valueLabel = { "${it.roundToInt()}dp" },
            isVisible = {
                val hidePill = it.getBoolean(TweaksKey.NAVIGATION_BAR_HIDE_PILL)
                val displayModeFullScreen =
                    it.getBoolean(TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_FULL_SCREEN)
                val displayModeImmersive =
                    it.getBoolean(TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_IMMERSIVE)

                !hidePill && !displayModeFullScreen && !displayModeImmersive
            }
        )
    }

    composable(key = "pill_appearance_apply_section") {
        PillAppearanceApplySection()
    }
}

@Composable
fun NavigationBarScreen(
    navigationbarViewModel: NavigationBarViewModel = hiltViewModel()
) {
    val prefController = LocalPreferenceController.current
    val isApplying by navigationbarViewModel.isLoading.collectAsStateWithLifecycle()

    if (isApplying) {
        LoadingDialog()
    }

    ToastAppliedEvent(navigationbarViewModel.uiEvent)

    PreferenceListener { event ->
        when (event.key) {
            TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_FULL_SCREEN.name -> {
                val enable = (event.newValue as PrefValue.BoolValue).v

                if (enable) {
                    prefController.setBoolean(
                        TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_IMMERSIVE,
                        false
                    )
                    navigationbarViewModel.updateDisplayMode()
                } else {
                    val immersiveEnabled =
                        prefController.getBoolean(TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_IMMERSIVE)

                    if (!immersiveEnabled) {
                        navigationbarViewModel.updateDisplayMode()
                    }
                }
            }

            TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_IMMERSIVE.name -> {
                val enable = (event.newValue as PrefValue.BoolValue).v

                if (enable) {
                    prefController.setBoolean(
                        TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_FULL_SCREEN,
                        false
                    )
                    navigationbarViewModel.updateDisplayMode()
                } else {
                    val fullScreenEnabled =
                        prefController.getBoolean(TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_FULL_SCREEN)

                    if (!fullScreenEnabled) {
                        navigationbarViewModel.updateDisplayMode()
                    }
                }
            }

            TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_GCAM_LAG_FIX.name -> {
                navigationbarViewModel.updateDisplayMode()
            }

            TweaksKey.NAVIGATION_BAR_HIDE_KEYBOARD_BUTTONS.name -> {
                val enable = (event.newValue as PrefValue.BoolValue).v
                navigationbarViewModel.applyHideKeyboardButtons(enable)
            }

            TweaksKey.NAVIGATION_BAR_LOWER_SENSITIVITY.name -> {
                val enable = (event.newValue as PrefValue.BoolValue).v
                navigationbarViewModel.applyLowerSensitivity(enable)
            }

            TweaksKey.NAVIGATION_BAR_DISABLE_LEFT_GESTURE.name -> {
                val enable = (event.newValue as PrefValue.BoolValue).v
                navigationbarViewModel.applyDisableLeftGesture(enable)
            }

            TweaksKey.NAVIGATION_BAR_DISABLE_RIGHT_GESTURE.name -> {
                val enable = (event.newValue as PrefValue.BoolValue).v
                navigationbarViewModel.applyDisableRightGesture(enable)
            }

            TweaksKey.NAVIGATION_BAR_HIDE_PILL.name -> {
                val enable = (event.newValue as PrefValue.BoolValue).v
                navigationbarViewModel.applyHidePill(enable)
            }

            TweaksKey.NAVIGATION_BAR_MONET_PILL.name -> {
                val enable = (event.newValue as PrefValue.BoolValue).v
                navigationbarViewModel.applyMonetPill(enable)
            }

            TweaksKey.NAVIGATION_BAR_PILL_APPEARANCE.name -> {
                val (width, thickness) = (event.newValue as PrefValue.StringValue).v
                    .split(",")
                    .map { it.toFloat() }

                navigationbarViewModel.applyPillAppearance(width, thickness)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val (width, thickness) = prefController.getString(
                TweaksKey.NAVIGATION_BAR_PILL_APPEARANCE
            ).split(",").map { it.toFloat() }

            val shouldResetWidth = width != -1f &&
                    prefController.getFloat(TweaksKey.NAVIGATION_BAR_PILL_WIDTH) != width
            val shouldResetThickness = thickness != -1f &&
                    prefController.getFloat(TweaksKey.NAVIGATION_BAR_PILL_THICKNESS) != thickness

            prefController.apply {
                if (shouldResetWidth) setFloat(
                    TweaksKey.NAVIGATION_BAR_PILL_WIDTH,
                    width
                )
                if (shouldResetThickness) setFloat(
                    TweaksKey.NAVIGATION_BAR_PILL_THICKNESS,
                    thickness
                )
            }
        }
    }

    NavigationBarScreenContent()
}

@Composable
private fun NavigationBarScreenContent() {
    PreferenceScreen(
        items = navigationbarPreferences,
        title = stringResource(R.string.activity_title_navigation_bar),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun NavigationBarScreenPreview() {
    PreviewComposable {
        NavigationBarScreenContent()
    }
}