package com.drdisagree.iconify.features.xposed.lockscreen.weather.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.preferences.PrefValue
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.arrayRes
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.preferences.FilePickerType
import com.drdisagree.iconify.core.utils.FileUtils.copyToXposedSharedPath
import com.drdisagree.iconify.data.common.XposedConst.LOCKSCREEN_WEATHER_FONT_FILE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.common.viewmodels.SystemActionViewModel
import com.drdisagree.iconify.features.xposed.lockscreen.common.components.WeatherEventHandler
import com.drdisagree.iconify.features.xposed.lockscreen.common.components.WeatherIconPackBottomSheet
import com.drdisagree.iconify.features.xposed.lockscreen.common.viewmodels.WeatherViewModel
import com.drdisagree.iconify.helpers.maskKey
import kotlin.math.roundToInt

fun lsWeatherPreferences(
    weatherViewModel: WeatherViewModel? = null,
    updateStatusSummary: String? = null,
    selectedPackLabel: String = "",
    onIconPackClick: () -> Unit = {}
) = preferenceScreen {
    category {
        switch(
            key = XposedKey.LOCKSCREEN_WEATHER,
            isMasterSwitch = true,
            title = stringRes(R.string.lockscreen_weather_title),
        )
    }

    category {
        listPref(
            key = XposedKey.WEATHER_UPDATE_INTERVAL,
            title = stringRes(R.string.update_interval_title),
            entries = arrayRes(R.array.update_interval_entries),
            entryValues = arrayRes(R.array.update_interval_values),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
        )

        action(
            key = "weather_update_status",
            title = stringRes(R.string.last_update_time),
            summary = {
                if (updateStatusSummary != null) stringRes(updateStatusSummary)
                else stringRes(R.string.not_available)
            },
            onClick = { weatherViewModel?.onUpdateStatusClicked() },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
        )
    }

    category {
        listPref(
            key = XposedKey.WEATHER_PROVIDER,
            title = stringRes(R.string.weather_provider),
            entries = arrayRes(R.array.weather_provider_entries),
            entryValues = arrayRes(R.array.weather_provider_values),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
        )

        editText(
            key = XposedKey.WEATHER_OWM_KEY,
            title = stringRes(R.string.weather_api_key),
            summary = {
                val currentVal = it.newValue
                if (currentVal.isEmpty()) stringRes(R.string.no_key_provided)
                else stringRes(currentVal.maskKey())
            },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
            isVisible = { it.getString(XposedKey.WEATHER_PROVIDER) in setOf("1") },
        )

        editText(
            key = XposedKey.WEATHER_YANDEX_KEY,
            title = stringRes(R.string.yandex_api_key),
            summary = {
                val currentVal = it.newValue
                if (currentVal.isEmpty()) stringRes(R.string.no_key_provided)
                else stringRes(currentVal.maskKey())
            },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
            isVisible = { it.getString(XposedKey.WEATHER_PROVIDER) in setOf("2") },
        )

        twoTargetSwitch(
            key = XposedKey.WEATHER_CUSTOM_LOCATION,
            title = stringRes(R.string.custom_location_title),
            summary = {
                val currentVal = it.newValue
                if (currentVal) stringRes(R.string.general_on)
                else stringRes(R.string.general_off)
            },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.Lockscreen.Location) {
                    launchSingleTop = true
                }
            },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
        )
    }

    category {
        listPref(
            key = XposedKey.WEATHER_UNITS,
            title = stringRes(R.string.units_title),
            entries = arrayRes(R.array.units_entries),
            entryValues = arrayRes(R.array.units_values),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
        )

        switch(
            key = XposedKey.WEATHER_SHOW_LOCATION,
            title = stringRes(R.string.weather_show_location),
            summary = {
                val currentVal = it.newValue
                if (currentVal) stringRes(R.string.general_on)
                else stringRes(R.string.general_off)
            },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
        )

        switch(
            key = XposedKey.WEATHER_SHOW_CONDITION,
            title = stringRes(R.string.weather_show_condition),
            summary = {
                val currentVal = it.newValue
                if (currentVal) stringRes(R.string.general_on)
                else stringRes(R.string.general_off)
            },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
        )

        switch(
            key = XposedKey.WEATHER_SHOW_HUMIDITY,
            title = stringRes(R.string.weather_show_humidity),
            summary = {
                val currentVal = it.newValue
                if (currentVal) stringRes(R.string.general_on)
                else stringRes(R.string.general_off)
            },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
        )

        switch(
            key = XposedKey.WEATHER_SHOW_WIND,
            title = stringRes(R.string.weather_show_wind),
            summary = {
                val currentVal = it.newValue
                if (currentVal) stringRes(R.string.general_on)
                else stringRes(R.string.general_off)
            },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
        )
    }

    category {
        switch(
            key = XposedKey.WEATHER_TEXT_COLOR,
            title = stringRes(R.string.weather_custom_color_switch_title),
            summary = {
                val currentVal = it.newValue
                if (currentVal) stringRes(R.string.general_on)
                else stringRes(R.string.general_off)
            },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
        )

        colorPicker(
            key = XposedKey.WEATHER_TEXT_COLOR_CODE,
            title = stringRes(R.string.weather_custom_color_title),
            summary = { stringRes(R.string.weather_custom_color_summary) },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
            isVisible = { it.getBoolean(XposedKey.WEATHER_TEXT_COLOR) }
        )

        action(
            key = XposedKey.WEATHER_ICON_PACK,
            title = stringRes(R.string.weather_icon_pack_title),
            summary = { stringRes(selectedPackLabel) },
            onClick = { onIconPackClick() },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
        )

        listPref(
            key = XposedKey.WEATHER_STYLE,
            title = stringRes(R.string.lockscreen_weather_selection_title),
            entries = arrayRes(R.array.lockscreen_weather_bg_entries),
            entryValues = arrayRes(R.array.lockscreen_weather_bg_values),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
        )

        filePicker(
            key = XposedKey.WEATHER_CUSTOM_FONT_FILE_URI,
            title = stringRes(R.string.choose_weather_font),
            pickerType = FilePickerType.Font,
            saveFileUri = true,
            onFileSelected = {
                val uriString = it.newValue
                if (uriString.isNotEmpty()) {
                    uriString.toUri().copyToXposedSharedPath(LOCKSCREEN_WEATHER_FONT_FILE.name)
                }
            },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) }
        )
    }

    category {
        switch(
            key = XposedKey.WEATHER_CENTER_VIEW,
            title = stringRes(R.string.weather_center_view_title),
            summary = { stringRes(R.string.weather_center_view_summary) },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
        )

        slider(
            key = XposedKey.WEATHER_TEXT_SIZE,
            title = stringRes(R.string.weather_text_size),
            min = 13f,
            max = 24f,
            steps = 10,
            valueLabel = { "${it.roundToInt()}px" },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) }
        )

        slider(
            key = XposedKey.WEATHER_ICON_SIZE,
            title = stringRes(R.string.weather_image_size),
            min = 13f,
            max = 24f,
            steps = 10,
            valueLabel = { "${it.roundToInt()}px" },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) }
        )

        slider(
            key = XposedKey.WEATHER_CUSTOM_MARGINS_SIDE,
            title = stringRes(R.string.weather_margin_side),
            min = 0f,
            max = 100f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) }
        )

        slider(
            key = XposedKey.WEATHER_CUSTOM_MARGINS_TOP,
            title = stringRes(R.string.weather_margin_top),
            min = 0f,
            max = 100f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) }
        )

        slider(
            key = XposedKey.WEATHER_CUSTOM_MARGINS_BOTTOM,
            title = stringRes(R.string.weather_margin_bottom),
            min = 0f,
            max = 100f,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockscreenWeatherScreen(
    weatherViewModel: WeatherViewModel = hiltViewModel(),
    systemActionViewModel: SystemActionViewModel = hiltViewModel(),
) {
    val screenState by weatherViewModel.screenState.collectAsStateWithLifecycle()

    PreferenceListener { event ->
        when (event.key) {
            XposedKey.LOCKSCREEN_WEATHER.name -> {
                val isEnabled = (event.newValue as PrefValue.BoolValue).v
                weatherViewModel.onMainSwitchChanged(isEnabled, XposedKey.LOCKSCREEN_WEATHER)
                systemActionViewModel.shouldRestartSystemUI()
            }

            XposedKey.WEATHER_CUSTOM_LOCATION.name -> {
                weatherViewModel.onCustomLocationChanged()
            }

            XposedKey.WEATHER_PROVIDER.name -> {
                val provider = (event.newValue as PrefValue.StringValue).v
                weatherViewModel.onWeatherProviderChanged(provider)
            }
        }
    }

    WeatherEventHandler(weatherViewModel = weatherViewModel)

    var showIconPackSheet by rememberSaveable { mutableStateOf(false) }

    if (showIconPackSheet) {
        WeatherIconPackBottomSheet(
            iconPacks = screenState.iconPacks,
            selectedIconPackIndex = screenState.selectedIconPackIndex,
            onItemClick = { weatherViewModel.onIconPackSelected(it) },
            onDismiss = { showIconPackSheet = false }
        )
    }

    val selectedPackLabel = screenState.iconPacks
        .getOrNull(screenState.selectedIconPackIndex)
        ?.label
        ?: stringResource(R.string.not_available)

    LockscreenWeatherScreenContent(
        weatherViewModel = weatherViewModel,
        updateStatusSummary = screenState.updateStatusSummary,
        selectedPackLabel = selectedPackLabel,
        onIconPackClick = { showIconPackSheet = true },
    )
}

@Composable
private fun LockscreenWeatherScreenContent(
    weatherViewModel: WeatherViewModel? = null,
    updateStatusSummary: String? = null,
    selectedPackLabel: String = "",
    onIconPackClick: () -> Unit = {}
) {
    PreferenceScreen(
        items = lsWeatherPreferences(
            weatherViewModel = weatherViewModel,
            updateStatusSummary = updateStatusSummary,
            selectedPackLabel = selectedPackLabel,
            onIconPackClick = onIconPackClick
        ),
        title = stringResource(R.string.activity_title_lockscreen_weather),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun LockscreenWeatherScreenPreview() {
    PreviewComposable {
        LockscreenWeatherScreenContent()
    }
}