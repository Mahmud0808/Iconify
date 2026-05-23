package com.drdisagree.iconify.features.xposed.lockscreen.widgets.weather.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.xposed.lockscreen.common.components.WeatherEventHandler
import com.drdisagree.iconify.features.xposed.lockscreen.common.components.WeatherIconPackBottomSheet
import com.drdisagree.iconify.features.xposed.lockscreen.common.viewmodels.WeatherViewModel
import com.drdisagree.iconify.helpers.maskKey

fun lsWidgetsWeatherPreferences(
    weatherViewModel: WeatherViewModel? = null,
    updateStatusSummary: String? = null,
    selectedPackLabel: String = "",
    onIconPackClick: () -> Unit = {}
) = preferenceScreen {
    category {
        listPref(
            key = XposedKey.WEATHER_UPDATE_INTERVAL,
            title = stringRes(R.string.update_interval_title),
            entries = arrayRes(R.array.update_interval_entries),
            entryValues = arrayRes(R.array.update_interval_values),
        )

        action(
            key = "weather_update_status",
            title = stringRes(R.string.last_update_time),
            summary = {
                if (updateStatusSummary != null) stringRes(updateStatusSummary)
                else stringRes(R.string.not_available)
            },
            onClick = { weatherViewModel?.onUpdateStatusClicked() },
        )

        listPref(
            key = XposedKey.WEATHER_PROVIDER,
            title = stringRes(R.string.weather_provider),
            entries = arrayRes(R.array.weather_provider_entries),
            entryValues = arrayRes(R.array.weather_provider_values),
        )

        editText(
            key = XposedKey.WEATHER_OWM_KEY,
            title = stringRes(R.string.weather_api_key),
            summary = {
                val currentVal = it.newValue
                if (currentVal.isEmpty()) stringRes(R.string.no_key_provided)
                else stringRes(currentVal.maskKey())
            },
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
            isVisible = { it.getString(XposedKey.WEATHER_PROVIDER) in setOf("2") },
        )

        listPref(
            key = XposedKey.WEATHER_UNITS,
            title = stringRes(R.string.units_title),
            entries = arrayRes(R.array.units_entries),
            entryValues = arrayRes(R.array.units_values),
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
        )

        action(
            key = XposedKey.WEATHER_ICON_PACK,
            title = stringRes(R.string.weather_icon_pack_title),
            summary = { stringRes(selectedPackLabel) },
            onClick = { onIconPackClick() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockscreenWidgetsWeatherScreen(weatherViewModel: WeatherViewModel = hiltViewModel()) {
    val screenState by weatherViewModel.screenState.collectAsStateWithLifecycle()

    PreferenceListener { event ->
        when (event.key) {
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

    LockscreenWidgetsWeatherScreenContent(
        weatherViewModel = weatherViewModel,
        updateStatusSummary = screenState.updateStatusSummary,
        selectedPackLabel = selectedPackLabel,
        onIconPackClick = { showIconPackSheet = true },
    )
}

@Composable
private fun LockscreenWidgetsWeatherScreenContent(
    weatherViewModel: WeatherViewModel? = null,
    updateStatusSummary: String? = null,
    selectedPackLabel: String = "",
    onIconPackClick: () -> Unit = {}
) {
    PreferenceScreen(
        items = lsWidgetsWeatherPreferences(
            weatherViewModel = weatherViewModel,
            updateStatusSummary = updateStatusSummary,
            selectedPackLabel = selectedPackLabel,
            onIconPackClick = onIconPackClick
        ),
        title = stringResource(R.string.activity_title_xposed_weather_settings),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun LockscreenWidgetsWeatherScreenPreview() {
    PreviewComposable {
        LockscreenWidgetsWeatherScreenContent()
    }
}