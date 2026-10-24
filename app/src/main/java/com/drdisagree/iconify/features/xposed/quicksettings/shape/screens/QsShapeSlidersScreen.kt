package com.drdisagree.iconify.features.xposed.quicksettings.shape.screens

import android.os.Build
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

private val radiusSliders = listOf(
    XposedKey.QS_ACTIVE_TILE_CORNER_RADIUS_OFFSET to R.string.qs_active_tile_radius_title,
    XposedKey.QS_ACTIVE_ICON_CORNER_RADIUS_OFFSET to R.string.qs_active_icon_radius_title,
    XposedKey.QS_INACTIVE_TILE_CORNER_RADIUS_OFFSET to R.string.qs_inactive_tile_radius_title,
    XposedKey.QS_INACTIVE_ICON_CORNER_RADIUS_OFFSET to R.string.qs_inactive_icon_radius_title,
    XposedKey.QS_BRIGHTNESS_SLIDER_CORNER_RADIUS_OFFSET to R.string.qs_brightness_slider_radius_title,
    XposedKey.QS_FOOTER_BUTTONS_CORNER_RADIUS_OFFSET to R.string.qs_footer_buttons_radius_title,
)

val qsShapeSlidersPreferences = preferenceScreen {
    category(title = stringRes(R.string.qs_corner_radius_offset_title)) {
        radiusSliders.forEach { (key, title) ->
            slider(
                key = key,
                title = stringRes(title),
                min = -50f,
                max = 50f,
                valueLabel = { value ->
                    val offset = value.roundToInt()
                    if (offset > 0) "+${offset}dp" else "${offset}dp"
                },
                showResetButton = true,
                isVisible = { Build.VERSION.SDK_INT >= 36 },
            )
        }
    }

    category(title = stringRes(R.string.section_title_qs_brightness_slider)) {
        switch(
            key = XposedKey.HIDE_QS_SLIDER_THUMB,
            title = stringRes(R.string.hide_qs_slider_thumb_title),
            summary = { stringRes(R.string.hide_qs_slider_thumb_desc) },
            isVisible = { Build.VERSION.SDK_INT >= 36 },
        )

        switch(
            key = XposedKey.QS_BRIGHTNESS_SLIDER_BOTTOM,
            title = stringRes(R.string.qs_brightness_slider_bottom_title),
            summary = { stringRes(R.string.qs_brightness_slider_bottom_desc) }
        )

        switch(
            key = XposedKey.QQS_BRIGHTNESS_SLIDER,
            title = stringRes(R.string.qqs_brightness_slider_title),
            summary = { stringRes(R.string.qqs_brightness_slider_desc) }
        )
    }
}

@Composable
fun QsShapeSlidersScreen(
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    PreferenceListener(key = null) { event ->
        when (event.key) {
            in radiusSliders.map { it.first.name },
            XposedKey.HIDE_QS_SLIDER_THUMB.name,
            XposedKey.QS_BRIGHTNESS_SLIDER_BOTTOM.name,
            XposedKey.QQS_BRIGHTNESS_SLIDER.name -> systemActionViewModel?.shouldRestartSystemUI()
        }
    }

    PreferenceScreen(
        items = qsShapeSlidersPreferences,
        title = stringResource(R.string.activity_title_qs_shape_sliders),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun QsShapeSlidersScreenPreview() {
    PreviewComposable {
        QsShapeSlidersScreen(null)
    }
}
