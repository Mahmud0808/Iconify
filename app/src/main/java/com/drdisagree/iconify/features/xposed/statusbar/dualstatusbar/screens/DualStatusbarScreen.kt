package com.drdisagree.iconify.features.xposed.statusbar.dualstatusbar.screens

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

val dualStatusbarPreferences = preferenceScreen {
    category {
        switch(
            key = XposedKey.DUAL_STATUSBAR,
            isMasterSwitch = true,
            title = stringRes(R.string.dsb_title),
        )
    }

    category {
        switch(
            key = XposedKey.DUAL_STATUSBAR_PORTRAIT_ONLY,
            title = stringRes(R.string.dsb_portrait_only_title),
            summary = { stringRes(R.string.dsb_portrait_only_desc) },
            isEnabled = { it.getBoolean(XposedKey.DUAL_STATUSBAR) }
        )

        switch(
            key = XposedKey.DUAL_STATUSBAR_START_SIDE_SINGLE_ROW,
            title = stringRes(R.string.dsb_single_line_start_side_title),
            summary = { stringRes(R.string.dsb_single_line_start_side_desc) },
            isEnabled = { it.getBoolean(XposedKey.DUAL_STATUSBAR) }
        )

        switch(
            key = XposedKey.DUAL_STATUSBAR_END_SIDE_SINGLE_ROW,
            title = stringRes(R.string.dsb_single_line_end_side_title),
            summary = { stringRes(R.string.dsb_single_line_end_side_desc) },
            isEnabled = { it.getBoolean(XposedKey.DUAL_STATUSBAR) }
        )

        switch(
            key = XposedKey.DUAL_STATUSBAR_SWAP_START_SIDE,
            title = stringRes(R.string.dsb_swap_start_side_title),
            summary = { stringRes(R.string.dsb_swap_start_side_desc) },
            isEnabled = { it.getBoolean(XposedKey.DUAL_STATUSBAR) }
        )

        switch(
            key = XposedKey.DUAL_STATUSBAR_SWAP_END_SIDE,
            title = stringRes(R.string.dsb_swap_end_side_title),
            summary = { stringRes(R.string.dsb_swap_end_side_desc) },
            isEnabled = { it.getBoolean(XposedKey.DUAL_STATUSBAR) }
        )
    }

    category {
        slider(
            key = XposedKey.DUAL_STATUSBAR_HEIGHT,
            title = stringRes(R.string.dsb_statusbar_height_title),
            min = -1f,
            max = 80f,
            valueLabel = { "${it.roundToInt()}dp" },
            showDefaultIndicator = true,
            hideDefaultValue = true,
            showResetButton = true,
            isEnabled = { it.getBoolean(XposedKey.DUAL_STATUSBAR) }
        )

        slider(
            key = XposedKey.DUAL_STATUSBAR_START_PADDING,
            title = stringRes(R.string.dsb_start_padding_title),
            min = -1f,
            max = 140f,
            valueLabel = { "${it.roundToInt()}dp" },
            showDefaultIndicator = true,
            hideDefaultValue = true,
            showResetButton = true,
            isEnabled = { it.getBoolean(XposedKey.DUAL_STATUSBAR) }
        )

        slider(
            key = XposedKey.DUAL_STATUSBAR_END_PADDING,
            title = stringRes(R.string.dsb_end_padding_title),
            min = -1f,
            max = 140f,
            valueLabel = { "${it.roundToInt()}dp" },
            showDefaultIndicator = true,
            hideDefaultValue = true,
            showResetButton = true,
            isEnabled = { it.getBoolean(XposedKey.DUAL_STATUSBAR) }
        )

        slider(
            key = XposedKey.DUAL_STATUSBAR_TOP_PADDING,
            title = stringRes(R.string.dsb_top_padding_title),
            min = -1f,
            max = 40f,
            valueLabel = { "${it.roundToInt()}dp" },
            showDefaultIndicator = true,
            hideDefaultValue = true,
            showResetButton = true,
            isEnabled = { it.getBoolean(XposedKey.DUAL_STATUSBAR) }
        )

        slider(
            key = XposedKey.DUAL_STATUSBAR_START_TOP_MARGIN,
            title = stringRes(R.string.dsb_start_top_margin_title),
            min = 0f,
            max = 40f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.DUAL_STATUSBAR) }
        )

        slider(
            key = XposedKey.DUAL_STATUSBAR_START_BOTTOM_MARGIN,
            title = stringRes(R.string.dsb_start_bottom_margin_title),
            min = 0f,
            max = 40f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.DUAL_STATUSBAR) }
        )

        slider(
            key = XposedKey.DUAL_STATUSBAR_END_TOP_MARGIN,
            title = stringRes(R.string.dsb_end_top_margin_title),
            min = 0f,
            max = 40f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.DUAL_STATUSBAR) }
        )

        slider(
            key = XposedKey.DUAL_STATUSBAR_END_BOTTOM_MARGIN,
            title = stringRes(R.string.dsb_end_bottom_margin_title),
            min = 0f,
            max = 40f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.DUAL_STATUSBAR) }
        )
    }
}

@Composable
fun DualStatusbarScreen(
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    PreferenceListener { event ->
        when (event.key) {
            XposedKey.DUAL_STATUSBAR.name -> {
                systemActionViewModel?.shouldRestartSystemUI()
            }
        }
    }

    PreferenceScreen(
        items = dualStatusbarPreferences,
        title = stringResource(R.string.activity_title_dual_statusbar),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun DualStatusbarScreenPreview() {
    PreviewComposable {
        DualStatusbarScreen(null)
    }
}