package com.drdisagree.iconify.features.xposed.quicksettings.grid.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.common.viewmodels.SystemActionViewModel
import kotlin.math.roundToInt

val qsGridPreferences = preferenceScreen {
    category {
        switch(
            key = XposedKey.CUSTOM_QS_GRID,
            isMasterSwitch = true,
            title = stringRes(R.string.activity_title_qs_row_column),
        )
    }

    category(title = stringRes(R.string.section_title_portrait)) {
        slider(
            key = XposedKey.QQS_ROW_PORTRAIT,
            title = stringRes(R.string.quick_qspanel_row_title),
            min = 1f,
            max = 4f,
            steps = 2,
            valueLabel = { "${it.roundToInt()}" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_GRID) },
        )

        slider(
            key = XposedKey.QS_ROW_PORTRAIT,
            title = stringRes(R.string.qspanel_row_title),
            min = 1f,
            max = 8f,
            steps = 6,
            valueLabel = { "${it.roundToInt()}" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_GRID) }
        )

        slider(
            key = XposedKey.QS_COLUMN_PORTRAIT,
            title = stringRes(R.string.qspanel_column_title),
            min = 2f,
            max = 8f,
            steps = 5,
            valueLabel = { "${it.roundToInt()}" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_GRID) },
        )
    }

    category(title = stringRes(R.string.section_title_landscape)) {
        slider(
            key = XposedKey.QQS_ROW_LANDSCAPE,
            title = stringRes(R.string.quick_qspanel_row_title),
            min = 1f,
            max = 4f,
            steps = 2,
            valueLabel = { "${it.roundToInt()}" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_GRID) },
        )

        slider(
            key = XposedKey.QS_ROW_LANDSCAPE,
            title = stringRes(R.string.qspanel_row_title),
            min = 1f,
            max = 8f,
            steps = 6,
            valueLabel = { "${it.roundToInt()}" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_GRID) }
        )

        slider(
            key = XposedKey.QS_COLUMN_LANDSCAPE,
            title = stringRes(R.string.qspanel_column_title),
            min = 4f,
            max = 12f,
            steps = 7,
            valueLabel = { "${it.roundToInt()}" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_GRID) },
        )
    }

    category {
        info(
            key = "qs_grid_info",
            text = stringRes(R.string.qs_grid_footer_info),
        )
    }
}

@Composable
fun QsGridScreen(
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    PreferenceListener(key = XposedKey.CUSTOM_QS_GRID) {
        systemActionViewModel?.shouldRestartSystemUI()
    }

    PreferenceScreen(
        items = qsGridPreferences,
        title = stringResource(R.string.activity_title_qs_row_column),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun QsGridScreenPreview() {
    PreviewComposable {
        QsGridScreen(null)
    }
}