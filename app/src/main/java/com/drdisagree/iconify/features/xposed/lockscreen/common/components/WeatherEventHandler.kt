package com.drdisagree.iconify.features.xposed.lockscreen.common.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.features.xposed.lockscreen.common.events.WeatherDialog
import com.drdisagree.iconify.features.xposed.lockscreen.common.events.WeatherEvent
import com.drdisagree.iconify.features.xposed.lockscreen.common.viewmodels.WeatherViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun WeatherEventHandler(weatherViewModel: WeatherViewModel) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val screenState by weatherViewModel.screenState.collectAsStateWithLifecycle()

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

    screenState.dialog?.let { dialog ->
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
}