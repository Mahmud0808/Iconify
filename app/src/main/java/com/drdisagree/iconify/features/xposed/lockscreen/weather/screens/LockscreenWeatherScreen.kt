package com.drdisagree.iconify.features.xposed.lockscreen.weather.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
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
import com.drdisagree.iconify.data.common.XposedConst.LOCKSCREEN_WEATHER_FONT_FILE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.xposed.lockscreen.common.events.WeatherDialog
import com.drdisagree.iconify.features.xposed.lockscreen.common.events.WeatherEvent
import com.drdisagree.iconify.features.xposed.lockscreen.common.viewmodels.WeatherViewModel
import com.drdisagree.iconify.helpers.maskKey
import com.drdisagree.iconify.helpers.toXposedSharedPath
import kotlinx.coroutines.flow.collectLatest

fun lsWeatherPreferences(
    weatherViewModel: WeatherViewModel?,
    updateStatusSummary: String?,
    selectedPackLabel: String,
    onIconPackClick: () -> Unit
) =
    preferenceScreen {
        category {
            switch(
                key = XposedKey.LOCKSCREEN_WEATHER,
                isMasterSwitch = true,
                title = stringRes(R.string.lockscreen_weather_title),
                summary = { _, _ -> stringRes(R.string.lockscreen_weather_desc) }
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
                summary = { _, _ ->
                    if (updateStatusSummary != null) stringRes(updateStatusSummary)
                    else stringRes(R.string.not_available)
                },
                onClick = { _, _, _ -> weatherViewModel?.onUpdateStatusClicked() },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
            )

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
                summary = { prefs, _ ->
                    val currentVal = prefs.getString(XposedKey.WEATHER_OWM_KEY)

                    if (currentVal.isEmpty()) stringRes("No key provided")
                    else stringRes(currentVal.maskKey())
                },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
                isVisible = { it.getString(XposedKey.WEATHER_PROVIDER) in setOf("1") },
            )

            editText(
                key = XposedKey.WEATHER_YANDEX_KEY,
                title = stringRes(R.string.yandex_api_key),
                summary = { prefs, _ ->
                    val currentVal = prefs.getString(XposedKey.WEATHER_YANDEX_KEY)

                    if (currentVal.isEmpty()) stringRes("No key provided")
                    else stringRes(currentVal.maskKey())
                },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
                isVisible = { it.getString(XposedKey.WEATHER_PROVIDER) in setOf("2") },
            )

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
                summary = { prefs, key ->
                    val currentVal =
                        prefs.getBoolean(key, XposedKey.WEATHER_SHOW_LOCATION.default as Boolean)
                    if (currentVal) stringRes(R.string.general_on)
                    else stringRes(R.string.general_off)
                },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
            )

            switch(
                key = XposedKey.WEATHER_SHOW_CONDITION,
                title = stringRes(R.string.weather_show_condition),
                summary = { prefs, key ->
                    val currentVal =
                        prefs.getBoolean(key, XposedKey.WEATHER_SHOW_CONDITION.default as Boolean)
                    if (currentVal) stringRes(R.string.general_on)
                    else stringRes(R.string.general_off)
                },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
            )

            switch(
                key = XposedKey.WEATHER_SHOW_HUMIDITY,
                title = stringRes(R.string.weather_show_humidity),
                summary = { prefs, key ->
                    val currentVal =
                        prefs.getBoolean(key, XposedKey.WEATHER_SHOW_HUMIDITY.default as Boolean)
                    if (currentVal) stringRes(R.string.general_on)
                    else stringRes(R.string.general_off)
                },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
            )

            switch(
                key = XposedKey.WEATHER_SHOW_WIND,
                title = stringRes(R.string.weather_show_wind),
                summary = { prefs, key ->
                    val currentVal =
                        prefs.getBoolean(key, XposedKey.WEATHER_SHOW_WIND.default as Boolean)
                    if (currentVal) stringRes(R.string.general_on)
                    else stringRes(R.string.general_off)
                },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
            )

            slider(
                key = XposedKey.WEATHER_TEXT_SIZE,
                title = stringRes(R.string.weather_text_size),
                min = 13f,
                max = 24f,
                steps = 10,
                valueLabel = { "${it.toInt()}px" },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) }
            )

            slider(
                key = XposedKey.WEATHER_ICON_SIZE,
                title = stringRes(R.string.weather_image_size),
                min = 13f,
                max = 24f,
                steps = 10,
                valueLabel = { "${it.toInt()}px" },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) }
            )

            switch(
                key = XposedKey.WEATHER_TEXT_COLOR_ENABLED,
                title = stringRes(R.string.weather_custom_color_switch_title),
                summary = { prefs, key ->
                    val currentVal = prefs.getBoolean(
                        key,
                        XposedKey.WEATHER_TEXT_COLOR_ENABLED.default as Boolean
                    )
                    if (currentVal) stringRes(R.string.general_on)
                    else stringRes(R.string.general_off)
                },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
            )

            colorPicker(
                key = XposedKey.WEATHER_TEXT_COLOR,
                title = stringRes(R.string.weather_custom_color_title),
                summary = { _, _ -> stringRes(R.string.weather_custom_color_summary) },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
                isVisible = { it.getBoolean(XposedKey.WEATHER_TEXT_COLOR_ENABLED) }
            )

            twoTargetSwitch(
                key = XposedKey.WEATHER_CUSTOM_LOCATION,
                title = stringRes(R.string.custom_location_title),
                summary = { prefs, key ->
                    val currentVal = prefs.getBoolean(
                        key,
                        XposedKey.WEATHER_CUSTOM_LOCATION.default as Boolean
                    )
                    if (currentVal) stringRes(R.string.general_on)
                    else stringRes(R.string.general_off)
                },
                onClick = { _, _, nav ->
                    nav.navigate(NavRoutes.Xposed.Lockscreen.Location) {
                        launchSingleTop = true
                    }
                },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
            )

            action(
                key = XposedKey.WEATHER_ICON_PACK,
                title = stringRes(R.string.weather_icon_pack_title),
                summary = { _, _ -> stringRes(selectedPackLabel) },
                onClick = { _, _, _ -> onIconPackClick() },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
            )

            switch(
                key = XposedKey.WEATHER_CENTER_VIEW,
                title = stringRes(R.string.weather_center_view_title),
                summary = { _, _ -> stringRes(R.string.weather_center_view_summary) },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) },
            )

            slider(
                key = XposedKey.WEATHER_CUSTOM_MARGINS_SIDE,
                title = stringRes(R.string.weather_margin_side),
                min = 0f,
                max = 100f,
                valueLabel = { "${it.toInt()}dp" },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) }
            )

            slider(
                key = XposedKey.WEATHER_CUSTOM_MARGINS_TOP,
                title = stringRes(R.string.weather_margin_top),
                min = 0f,
                max = 100f,
                valueLabel = { "${it.toInt()}dp" },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) }
            )

            slider(
                key = XposedKey.WEATHER_CUSTOM_MARGINS_BOTTOM,
                title = stringRes(R.string.weather_margin_bottom),
                min = 0f,
                max = 100f,
                valueLabel = { "${it.toInt()}dp" },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) }
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
                onFileSelected = { _, uriString ->
                    if (uriString.isNotEmpty()) {
                        uriString.toUri().toXposedSharedPath(LOCKSCREEN_WEATHER_FONT_FILE.name)
                    }
                },
                isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_WEATHER) }
            )
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockscreenWeatherScreen(weatherViewModel: WeatherViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val state by weatherViewModel.state.collectAsStateWithLifecycle()

    PreferenceListener { event ->
        when (event.key) {
            XposedKey.LOCKSCREEN_WEATHER.name -> {
                val isEnabled = (event.newValue as PrefValue.BoolValue).v
                weatherViewModel.onMainSwitchChanged(isEnabled, XposedKey.LOCKSCREEN_WEATHER)
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

    var showIconPackSheet by remember { mutableStateOf(false) }
    val onIconPackClick: () -> Unit = { showIconPackSheet = true }

    val selectedPackLabel = state.iconPacks.getOrNull(state.selectedIconPackIndex)?.label ?: ""

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val coarse = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

        weatherViewModel.onPermissionResult(fineGranted = fine, coarseGranted = coarse)
    }

    DisposableEffect(Unit) {
        weatherViewModel.attachObserver()

        onDispose { weatherViewModel.detachObserver() }
    }

    LaunchedEffect(Unit) {
        weatherViewModel.events.collectLatest { event ->
            when (event) {
                WeatherEvent.OpenLocationSettings -> {
                    context.startActivity(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    )
                }

                WeatherEvent.OpenAppPermissionSettings -> {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts(
                                "package",
                                context.packageName,
                                null
                            )
                        }
                    )
                }

                WeatherEvent.RequestLocationPermissions -> {
                    val shouldShow = activity?.let {
                        it.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                                it.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                                it.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    } ?: false

                    if (shouldShow) {
                        weatherViewModel.onShouldShowPermissionRationale(true)
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                        )
                    }
                }
            }
        }
    }

    state.dialog?.let { dialog ->
        when (dialog) {
            WeatherDialog.LocationDisabled -> {
                AlertDialog(
                    onDismissRequest = { weatherViewModel.dismissDialog() },
                    title = { Text(stringResource(R.string.weather_retrieve_location_dialog_title)) },
                    text = { Text(stringResource(R.string.weather_retrieve_location_dialog_message)) },
                    confirmButton = {
                        TextButton(onClick = { weatherViewModel.onOpenLocationSettingsConfirmed() }) {
                            Text(stringResource(R.string.weather_retrieve_location_dialog_enable_button))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { weatherViewModel.dismissDialog() }) {
                            Text(stringResource(android.R.string.cancel))
                        }
                    }
                )
            }

            WeatherDialog.PermissionRationale -> {
                AlertDialog(
                    onDismissRequest = { weatherViewModel.dismissDialog() },
                    title = { Text(stringResource(R.string.weather_permission_dialog_title)) },
                    text = { Text(stringResource(R.string.weather_permission_dialog_message)) },
                    confirmButton = {
                        TextButton(onClick = { weatherViewModel.onOpenAppPermissionSettingsConfirmed() }) {
                            Text(stringResource(android.R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { weatherViewModel.dismissDialog() }) {
                            Text(stringResource(android.R.string.cancel))
                        }
                    }
                )
            }

            WeatherDialog.OwmKey -> {
                AlertDialog(
                    onDismissRequest = { weatherViewModel.dismissDialog() },
                    title = { Text(stringResource(R.string.weather_provider_owm_key_title)) },
                    text = { Text(stringResource(R.string.weather_provider_owm_key_message)) },
                    confirmButton = {
                        TextButton(onClick = { weatherViewModel.dismissDialog() }) {
                            Text(stringResource(R.string.understood))
                        }
                    }
                )
            }

            WeatherDialog.YandexKey -> {
                AlertDialog(
                    onDismissRequest = { weatherViewModel.dismissDialog() },
                    title = { Text(stringResource(R.string.weather_provider_yandex_key_title)) },
                    text = { Text(stringResource(R.string.weather_provider_yandex_key_message)) },
                    confirmButton = {
                        TextButton(onClick = { weatherViewModel.dismissDialog() }) {
                            Text(stringResource(R.string.understood))
                        }
                    }
                )
            }
        }
    }

    if (showIconPackSheet) {
        ModalBottomSheet(onDismissRequest = { showIconPackSheet = false }) {
            Text(
                text = stringResource(R.string.weather_icon_pack_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                itemsIndexed(state.iconPacks) { index, pack ->
                    val isSelected = index == state.selectedIconPackIndex
                    ListItem(
                        modifier = Modifier.clickable {
                            weatherViewModel.onIconPackSelected(index)
                            showIconPackSheet = false
                        },
                        headlineContent = { Text(pack.label) },
                        leadingContent = {
                            pack.drawable?.let { drawable ->
                                Image(
                                    bitmap = drawable.toBitmap(48, 48).asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                            }
                        },
                        trailingContent = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                    if (index < state.iconPacks.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
                // bottom padding so last item clears the nav bar
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    LockscreenWeatherScreenContent(
        weatherViewModel = weatherViewModel,
        updateStatusSummary = state.updateStatusSummary,
        selectedPackLabel = selectedPackLabel,
        onIconPackClick = onIconPackClick,
    )
}

@Composable
private fun LockscreenWeatherScreenContent(
    weatherViewModel: WeatherViewModel?,
    updateStatusSummary: String?,
    selectedPackLabel: String,
    onIconPackClick: () -> Unit
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
fun LockscreenWeatherScreenPreview() {
    PreviewComposable {
        LockscreenWeatherScreenContent(
            null,
            null,
            ""
        ) {}
    }
}