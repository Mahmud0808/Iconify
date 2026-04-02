package com.drdisagree.iconify.features.xposed.lockscreen.widgets.main.screens

import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.preferences.PrefValue
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.arrayRes
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.utils.weather.WeatherConfig
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.common.viewmodels.SystemActionViewModel
import com.drdisagree.iconify.features.xposed.lockscreen.common.viewmodels.WeatherViewModel
import com.drdisagree.iconify.services.schedulers.WeatherScheduler
import java.util.stream.Collectors

fun lsWidgetsPreferences(isWeatherSettingsVisible: Boolean = false) = preferenceScreen {
    category {
        switch(
            key = XposedKey.LOCKSCREEN_WIDGETS,
            isMasterSwitch = true,
            title = stringRes(R.string.lockscreen_widgets_enabled_title),
        )
    }

    category(title = stringRes(R.string.lockscreen_display_widgets_title)) {
        switch(
            key = XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET,
            title = stringRes(R.string.lockscreen_display_widgets_title),
            summary = { _, _ -> stringRes(R.string.lockscreen_display_widgets_summary) },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) }
        )

        listPref(
            key = XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET_STYLE,
            title = stringRes(R.string.lockscreen_device_widget_style_title),
            entries = arrayRes(R.array.lockscreen_device_widget_entries),
            entryValues = arrayRes(R.array.lockscreen_device_widget_values),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
            isVisible = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET) },
        )

        switch(
            key = XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET_CUSTOM_COLORS,
            title = stringRes(R.string.widgets_custom_color),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
            isVisible = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET) },
        )

        colorPicker(
            key = XposedKey.LOCKSCREEN_WIDGETS_LINEAR_PROGRESS_COLOR,
            title = stringRes(R.string.linear_progress_color),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
            isVisible = {
                it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET_CUSTOM_COLORS) &&
                        it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET)
            }
        )

        colorPicker(
            key = XposedKey.LOCKSCREEN_WIDGETS_CIRCULAR_PROGRESS_COLOR,
            title = stringRes(R.string.circular_progress_color),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
            isVisible = {
                it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET_CUSTOM_COLORS) &&
                        it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET)
            }
        )

        colorPicker(
            key = XposedKey.LOCKSCREEN_WIDGETS_TEXT_COLOR,
            title = stringRes(R.string.text_color),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
            isVisible = {
                it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET_CUSTOM_COLORS) &&
                        it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET)
            }
        )

        editText(
            key = XposedKey.LOCKSCREEN_WIDGETS_CUSTOM_DEVICE_NAME,
            title = stringRes(R.string.custom_device_name),
            summary = { prefs, _ ->
                val currentVal = prefs.getString(XposedKey.LOCKSCREEN_WIDGETS_CUSTOM_DEVICE_NAME)

                if (currentVal.isNotEmpty()) stringRes(currentVal)
                else stringRes(R.string.custom_device_name_summary)
            },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
            isVisible = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET) },
        )
    }

    category(title = stringRes(R.string.large_widgets_category_title)) {
        listPref(
            key = XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET1,
            title = stringRes(R.string.main_custom_widget_1),
            entries = arrayRes(R.array.lockscreen_widgets_entries),
            entryValues = arrayRes(R.array.lockscreen_widgets_values),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
        )

        listPref(
            key = XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET2,
            title = stringRes(R.string.main_custom_widget_2),
            entries = arrayRes(R.array.lockscreen_widgets_entries),
            entryValues = arrayRes(R.array.lockscreen_widgets_values),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
        )
    }

    category(title = stringRes(R.string.mini_widgets_category_title)) {
        listPref(
            key = XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET1,
            title = stringRes(R.string.custom_widget_1),
            entries = arrayRes(R.array.lockscreen_widgets_entries),
            entryValues = arrayRes(R.array.lockscreen_widgets_values),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
        )

        listPref(
            key = XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET2,
            title = stringRes(R.string.custom_widget_2),
            entries = arrayRes(R.array.lockscreen_widgets_entries),
            entryValues = arrayRes(R.array.lockscreen_widgets_values),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
        )

        listPref(
            key = XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET3,
            title = stringRes(R.string.custom_widget_3),
            entries = arrayRes(R.array.lockscreen_widgets_entries),
            entryValues = arrayRes(R.array.lockscreen_widgets_values),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
        )

        listPref(
            key = XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET4,
            title = stringRes(R.string.custom_widget_4),
            entries = arrayRes(R.array.lockscreen_widgets_entries),
            entryValues = arrayRes(R.array.lockscreen_widgets_values),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
        )
    }

    category(title = stringRes(R.string.widgets_custom_color)) {
        switch(
            key = XposedKey.LOCKSCREEN_WIDGETS_CUSTOM_WIDGET_COLORS,
            title = stringRes(R.string.widgets_custom_color_title),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
        )

        colorPicker(
            key = XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET_ACTIVE_COLOR,
            title = stringRes(R.string.big_widget_active),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
            isVisible = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_CUSTOM_WIDGET_COLORS) },
        )

        colorPicker(
            key = XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET_INACTIVE_COLOR,
            title = stringRes(R.string.big_widget_inactive),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
            isVisible = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_CUSTOM_WIDGET_COLORS) },
        )

        colorPicker(
            key = XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET_ICON_ACTIVE_COLOR,
            title = stringRes(R.string.big_widget_icon_active),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
            isVisible = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_CUSTOM_WIDGET_COLORS) },
        )

        colorPicker(
            key = XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET_ICON_INACTIVE_COLOR,
            title = stringRes(R.string.big_widget_icon_inactive),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
            isVisible = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_CUSTOM_WIDGET_COLORS) },
        )

        colorPicker(
            key = XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET_ACTIVE_COLOR,
            title = stringRes(R.string.mini_widget_active),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
            isVisible = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_CUSTOM_WIDGET_COLORS) },
        )

        colorPicker(
            key = XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET_INACTIVE_COLOR,
            title = stringRes(R.string.mini_widget_inactive),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
            isVisible = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_CUSTOM_WIDGET_COLORS) },
        )

        colorPicker(
            key = XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET_ICON_ACTIVE_COLOR,
            title = stringRes(R.string.mini_widget_icon_active),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
            isVisible = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_CUSTOM_WIDGET_COLORS) },
        )

        colorPicker(
            key = XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET_ICON_INACTIVE_COLOR,
            title = stringRes(R.string.mini_widget_icon_inactive),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
            isVisible = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS_CUSTOM_WIDGET_COLORS) },
        )
    }

    category(title = stringRes(R.string.activity_title_miscellaneous)) {
        action(
            key = "xposed_lockscreen_widget_weather_settings",
            title = stringRes(R.string.weather_settings),
            onClick = { _, _, nav ->
                nav.navigate(NavRoutes.Xposed.Lockscreen.Widgets.Weather) {
                    launchSingleTop = true
                }
            },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) },
            isVisible = { isWeatherSettingsVisible }
        )

        slider(
            key = XposedKey.LOCKSCREEN_WIDGETS_TOP_MARGIN,
            title = stringRes(R.string.lockscreen_clock_top_margin_title),
            min = -100f,
            max = 400f,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) }
        )

        slider(
            key = XposedKey.LOCKSCREEN_WIDGETS_BOTTOM_MARGIN,
            title = stringRes(R.string.lockscreen_clock_bottom_margin_title),
            min = -100f,
            max = 400f,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) }
        )

        slider(
            key = XposedKey.LOCKSCREEN_WIDGETS_CORNER_RADIUS,
            title = stringRes(R.string.lockscreen_widgets_roundness),
            min = 0f,
            max = 140f,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) }
        )

        slider(
            key = XposedKey.LOCKSCREEN_WIDGETS_VIEW_SCALE,
            title = stringRes(R.string.lockscreen_widgets_scale),
            min = 0.5f,
            max = 1.5f,
            steps = 9,
            valueLabel = { "${it.toInt()}x" },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WIDGETS) }
        )
    }

    category {
        info(
            key = "xposed_lockscreen_widget_info",
            text = stringRes(R.string.custom_tiles_footer_info)
        )
    }
}

@Composable
fun LockscreenWidgetsScreen(
    weatherViewModel: WeatherViewModel = hiltViewModel(),
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    val context = LocalContext.current
    val prefController = LocalPreferenceController.current

    var isWeatherSettingsVisible by rememberSaveable { mutableStateOf(false) }

    fun List<String>.replaceEmptyWithNone(): MutableList<String> {
        return stream()
            .map { s: String? -> if (TextUtils.isEmpty(s)) "none" else s }
            .collect(Collectors.toList())
    }

    fun getMainWidgetsList() = listOf(
        prefController.getString(XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET1),
        prefController.getString(XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET2)
    ).replaceEmptyWithNone()

    fun getExtraWidgetsList() = listOf(
        prefController.getString(XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET1),
        prefController.getString(XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET2),
        prefController.getString(XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET3),
        prefController.getString(XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET4)
    ).replaceEmptyWithNone()

    fun isWeatherTileEnabled(): Boolean {
        return (getMainWidgetsList() + getExtraWidgetsList()).any { it.contains("weather") }
    }

    LaunchedEffect(Unit) {
        val wasWeatherEnabled = WeatherConfig.isEnabled()
        val mWeatherClient = weatherViewModel.mWeatherClient
        val widgetsEnabled = prefController.getBoolean(XposedKey.LOCKSCREEN_WIDGETS)
        isWeatherSettingsVisible = isWeatherTileEnabled()
        val weatherEnabled = widgetsEnabled && isWeatherSettingsVisible

        if (weatherEnabled && wasWeatherEnabled && mWeatherClient.mCachedInfo != null) {
            if (System.currentTimeMillis() - mWeatherClient.mCachedInfo!!.timeStamp > 3600000) {
                WeatherScheduler.scheduleUpdateNow(context)
            }
        } else if (weatherEnabled) {
            WeatherScheduler.scheduleUpdates(context)
            WeatherScheduler.scheduleUpdateNow(context)
        }
    }

    PreferenceListener { event ->
        when (event.key) {
            XposedKey.LOCKSCREEN_WIDGETS.name -> {
                val isEnabled = (event.newValue as PrefValue.BoolValue).v
                weatherViewModel.onMainSwitchChanged(isEnabled, XposedKey.LOCKSCREEN_WIDGETS)
                systemActionViewModel?.shouldRestartSystemUI()
            }

            XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET1.name,
            XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET2.name -> {
                prefController.setString(
                    XposedKey.LOCKSCREEN_WIDGETS_MAIN,
                    TextUtils.join(",", getMainWidgetsList())
                )
            }

            XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET1.name,
            XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET2.name,
            XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET3.name,
            XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET4.name -> {
                prefController.setString(
                    XposedKey.LOCKSCREEN_WIDGETS_EXTRAS,
                    TextUtils.join(",", getExtraWidgetsList())
                )
            }

            XposedKey.LOCKSCREEN_WIDGETS_MAIN.name,
            XposedKey.LOCKSCREEN_WIDGETS_EXTRAS.name -> {
                isWeatherSettingsVisible = isWeatherTileEnabled()
            }
        }
    }

    LockscreenWidgetsScreenContent(isWeatherSettingsVisible = isWeatherSettingsVisible)
}

@Composable
private fun LockscreenWidgetsScreenContent(isWeatherSettingsVisible: Boolean = false) {
    PreferenceScreen(
        items = lsWidgetsPreferences(isWeatherSettingsVisible),
        title = stringResource(R.string.activity_title_lockscreen_widget),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun LockscreenWidgetsScreenPreview() {
    PreviewComposable {
        LockscreenWidgetsScreenContent()
    }
}