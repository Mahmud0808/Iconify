package com.drdisagree.iconify.features.home.tweaks.cornerradius.components

import androidx.compose.animation.AnimatedVisibility
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

    val cornerRadius by prefController.observe(
        TweaksKey.UI_CORNER_RADIUS.name,
        TweaksKey.UI_CORNER_RADIUS.default as Float
    )
    val cornerRadiusSaved by prefController.observe(
        TweaksKey.UI_CORNER_RADIUS_SAVED.name,
        TweaksKey.UI_CORNER_RADIUS_SAVED.default as Float
    )

    val showDisableButton = remember(cornerRadiusSaved) {
        derivedStateOf {
            cornerRadiusSaved != TweaksKey.UI_CORNER_RADIUS_SAVED.default
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = withHaptic {
                prefController.setFloat(TweaksKey.UI_CORNER_RADIUS_SAVED, cornerRadius)
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
                    prefController.setFloat(
                        TweaksKey.UI_CORNER_RADIUS_SAVED,
                        TweaksKey.UI_CORNER_RADIUS_SAVED.default
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