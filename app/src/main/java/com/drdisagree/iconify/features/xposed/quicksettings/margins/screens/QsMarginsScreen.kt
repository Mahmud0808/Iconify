package com.drdisagree.iconify.features.xposed.quicksettings.margins.screens

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

val qsMarginsPreferences = preferenceScreen {
    category {
        switch(
            key = XposedKey.CUSTOM_QS_MARGINS,
            isMasterSwitch = true,
            title = stringRes(R.string.custom_qs_margin_title),
        )
    }

    category(title = stringRes(R.string.section_title_portrait)) {
        slider(
            key = XposedKey.QQS_TOP_MARGIN_PORTRAIT,
            title = stringRes(R.string.qqs_panel_top_margin_title),
            min = 0f,
            max = 300f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_MARGINS) }
        )

        slider(
            key = XposedKey.QS_TOP_MARGIN_PORTRAIT,
            title = stringRes(R.string.qs_panel_top_margin_title),
            min = 0f,
            max = 300f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_MARGINS) }
        )
    }

    category(title = stringRes(R.string.section_title_landscape)) {
        slider(
            key = XposedKey.QQS_TOP_MARGIN_LANDSCAPE,
            title = stringRes(R.string.qqs_panel_top_margin_title),
            min = 0f,
            max = 300f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_MARGINS) }
        )

        slider(
            key = XposedKey.QS_TOP_MARGIN_LANDSCAPE,
            title = stringRes(R.string.qs_panel_top_margin_title),
            min = 0f,
            max = 300f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_MARGINS) }
        )
    }

    category {
        info(
            key = "qs_margins_info",
            text = stringRes(R.string.qs_panel_top_margin_info),
        )
    }
}

@Composable
fun QsMarginsScreen(
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    PreferenceListener(key = null) { event ->
        when (event.key) {
            XposedKey.CUSTOM_QS_MARGINS.name,
            XposedKey.QQS_TOP_MARGIN_PORTRAIT.name,
            XposedKey.QS_TOP_MARGIN_PORTRAIT.name,
            XposedKey.QQS_TOP_MARGIN_LANDSCAPE.name,
            XposedKey.QS_TOP_MARGIN_LANDSCAPE.name -> systemActionViewModel?.shouldRestartSystemUI()
        }
    }

    PreferenceScreen(
        items = qsMarginsPreferences,
        title = stringResource(R.string.activity_title_qs_panel_margin),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun QsMarginsScreenPreview() {
    PreviewComposable {
        QsMarginsScreen(null)
    }
}