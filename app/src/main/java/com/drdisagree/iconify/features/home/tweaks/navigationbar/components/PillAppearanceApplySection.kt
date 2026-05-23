package com.drdisagree.iconify.features.home.tweaks.navigationbar.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.data.keys.TweaksKey

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PillAppearanceApplySection() {
    val prefController = LocalPreferenceController.current

    val hidePill by prefController.observe(
        TweaksKey.NAVIGATION_BAR_HIDE_PILL.name,
        TweaksKey.NAVIGATION_BAR_HIDE_PILL.default as Boolean
    )
    val displayModeFullScreen by prefController.observe(
        TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_FULL_SCREEN.name,
        TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_FULL_SCREEN.default as Boolean
    )
    val displayModeImmersive by prefController.observe(
        TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_IMMERSIVE.name,
        TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_IMMERSIVE.default as Boolean
    )

    val pillAppearanceValue by prefController.observe(
        TweaksKey.NAVIGATION_BAR_PILL_APPEARANCE.name,
        TweaksKey.NAVIGATION_BAR_PILL_APPEARANCE.default as String
    )

    val (savedWidth, savedThickness) = pillAppearanceValue
        .split(",")
        .map { it.toFloat() }

    val tempWidth by prefController.observe(
        TweaksKey.NAVIGATION_BAR_PILL_WIDTH.name,
        TweaksKey.NAVIGATION_BAR_PILL_WIDTH.default as Float
    )
    val tempThickness by prefController.observe(
        TweaksKey.NAVIGATION_BAR_PILL_THICKNESS.name,
        TweaksKey.NAVIGATION_BAR_PILL_THICKNESS.default as Float
    )

    val (defaultWidth, defaultThickness) = TweaksKey.NAVIGATION_BAR_PILL_APPEARANCE.default
        .split(",").map { it.toFloat() }

    val showApplySection by remember {
        derivedStateOf {
            !hidePill && !displayModeFullScreen && !displayModeImmersive
        }
    }

    val showApplyButton = remember {
        derivedStateOf {
            tempWidth != defaultWidth && tempThickness != defaultThickness
        }
    }

    val showDisableButton = remember(pillAppearanceValue) {
        derivedStateOf {
            savedWidth != defaultWidth && savedThickness != defaultThickness
        }
    }

    AnimatedVisibility(
        visible = showApplySection,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                enabled = showApplyButton.value,
                onClick = withHaptic {
                    prefController.setString(
                        TweaksKey.NAVIGATION_BAR_PILL_APPEARANCE,
                        "$tempWidth,$tempThickness"
                    )
                },
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(stringResource(R.string.btn_apply))
            }

            AnimatedVisibility(visible = showDisableButton.value) {
                Button(
                    onClick = withHaptic {
                        prefController.setString(
                            TweaksKey.NAVIGATION_BAR_PILL_APPEARANCE,
                            "$defaultWidth,$defaultThickness"
                        )
                    },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(stringResource(R.string.btn_disable))
                }
            }
        }
    }
}