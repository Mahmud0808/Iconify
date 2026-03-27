package com.drdisagree.iconify.features.xposed.statusbar.dualstatusbar.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable

val dualStatusbarPreferences = preferenceScreen {
}

@Composable
fun DualStatusbarScreen() {
    PreferenceScreen(
        items = dualStatusbarPreferences,
        title = stringResource(R.string.activity_title_dual_statusbar),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun DualStatusbarScreenPreview() {
    PreviewComposable {
        DualStatusbarScreen()
    }
}