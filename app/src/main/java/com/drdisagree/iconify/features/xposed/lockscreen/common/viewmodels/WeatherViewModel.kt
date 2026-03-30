package com.drdisagree.iconify.features.xposed.lockscreen.common.viewmodels

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.LocationManager
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.di.SharedPrefs
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.toPrefValue
import com.drdisagree.iconify.core.utils.OmniJawsClient
import com.drdisagree.iconify.core.utils.weather.WeatherConfig
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.data.storage.PreferenceStorage
import com.drdisagree.iconify.features.xposed.lockscreen.common.events.WeatherDialog
import com.drdisagree.iconify.features.xposed.lockscreen.common.events.WeatherEvent
import com.drdisagree.iconify.features.xposed.lockscreen.common.models.WeatherIconPackItem
import com.drdisagree.iconify.features.xposed.lockscreen.common.states.WeatherScreenState
import com.drdisagree.iconify.services.schedulers.WeatherScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    @param:ApplicationContext val context: Context,
    private val weatherClient: OmniJawsClient,
    @param:SharedPrefs private val preferenceStorage: PreferenceStorage,
) : ViewModel() {

    val controller = PreferenceController(preferenceStorage)

    // ---- public state -------------------------------------------------------

    private val _state = MutableStateFlow(WeatherScreenState())
    val state: StateFlow<WeatherScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<WeatherEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            loadIconPacks()
        }
    }

    // ---- OmniJaws observer (mirrors onResume / onPause) --------------------

    /**
     * Call from a [DisposableEffect] in the composable:
     *   DisposableEffect(Unit) {
     *       viewModel.attachObserver()
     *       onDispose { viewModel.detachObserver() }
     *   }
     */
    fun attachObserver() {
        weatherClient.addObserver(omniJawsObserver)
        handlePermissions()
    }

    fun detachObserver() {
        weatherClient.removeObserver(omniJawsObserver)
    }

    private val omniJawsObserver = object : OmniJawsClient.OmniJawsObserver {
        override fun weatherUpdated() {
            queryAndUpdateWeather()
        }

        override fun weatherError(errorReason: Int) {
            val msg = when (errorReason) {
                OmniJawsClient.EXTRA_ERROR_DISABLED -> {
                    context.getString(R.string.omnijaws_service_disabled)
                }

                OmniJawsClient.EXTRA_ERROR_LOCATION -> {
                    context.getString(R.string.omnijaws_service_error_location)
                }

                OmniJawsClient.EXTRA_ERROR_NETWORK -> {
                    context.getString(R.string.omnijaws_service_error_network)
                }

                OmniJawsClient.EXTRA_ERROR_NO_PERMISSIONS -> {
                    context.getString(R.string.omnijaws_service_error_permissions)
                }

                else -> {
                    context.getString(R.string.omnijaws_service_error_long)
                }
            }
            _state.update { it.copy(updateStatusSummary = msg) }
        }
    }

    // ---- Initialisation (mirrors onCreatePreferences) ----------------------

    @Suppress("DiscouragedApi")
    private suspend fun loadIconPacks() = withContext(Dispatchers.Default) {
        val entries = mutableListOf<String?>()
        val values = mutableListOf<String?>()
        val drawables = mutableListOf<Drawable?>()

        getAvailableWeatherIconPacks(entries, values, drawables)

        val items = entries.indices.map { i ->
            WeatherIconPackItem(
                label = entries[i] ?: "",
                value = values[i] ?: "",
                drawable = drawables[i]
            )
        }

        val savedPack = WeatherConfig.getIconPack(context).toString()
        var selectedIndex = values.indexOfFirst { it == savedPack }
        if (selectedIndex == -1) {
            selectedIndex = values.indexOfFirst { it == DEFAULT_WEATHER_ICON_PACKAGE }
            if (selectedIndex == -1) selectedIndex = 0
        }

        withContext(Dispatchers.Main) {
            _state.update { it.copy(iconPacks = items, selectedIconPackIndex = selectedIndex) }
        }
    }

    // ---- Called when the location result comes back (mirrors fragmentResultListener) --

    /**
     * Equivalent to the [setFragmentResultListener] for [DATA_LOCATION_KEY].
     * Call this when the location picker screen returns a result.
     */
    fun onLocationNameReceived(locationName: String?) {
        if (WeatherConfig.isEnabled(context)
            && !controller.getBoolean(XposedKey.WEATHER_CUSTOM_LOCATION)
        ) {
            checkLocationEnabled(force = true)
        }
    }

    // ---- Preference change handlers (mirrors updateScreen) -----------------

    /** Called when the master on/off switch is toggled. */
    fun onMainSwitchChanged(enabled: Boolean, key: XposedKey) {
        WeatherConfig.setEnabled(context, enabled, key.name)

        if (enabled) {
            handlePermissions()
            enableService()
            forceRefreshWeatherSettings()
        }
    }

    /** Called when WEATHER_PROVIDER changes. */
    fun onWeatherProviderChanged(provider: String) {
        forceRefreshWeatherSettings()

        when (provider) {
            "1" if controller.getString(XposedKey.WEATHER_OWM_KEY).isEmpty() -> {
                _state.update { it.copy(dialog = WeatherDialog.OwmKey) }
            }

            "2" if controller.getString(XposedKey.WEATHER_YANDEX_KEY).isEmpty() -> {
                _state.update { it.copy(dialog = WeatherDialog.YandexKey) }
            }
        }
    }

    /** Called when WEATHER_CUSTOM_LOCATION switch is toggled. */
    fun onCustomLocationChanged() {
        forceRefreshWeatherSettings()
    }

    /** Called when the user taps "Update status" row. */
    fun onUpdateStatusClicked() {
        forceRefreshWeatherSettings()
    }

    /** Called when the user picks an icon pack from the bottom sheet. */
    fun onIconPackSelected(index: Int) {
        val packs = _state.value.iconPacks
        if (index !in packs.indices) return

        controller.set(XposedKey.WEATHER_ICON_PACK, packs[index].value.toPrefValue())
        _state.update { it.copy(selectedIconPackIndex = index) }

        forceRefreshWeatherSettings()
    }

    // ---- Dialog dismissal --------------------------------------------------

    fun dismissDialog() {
        _state.update { it.copy(dialog = null) }
    }

    /** User confirmed "open location settings". */
    fun onOpenLocationSettingsConfirmed() {
        dismissDialog()
        emit(WeatherEvent.OpenLocationSettings)
    }

    /** User confirmed "open app permission settings". */
    fun onOpenAppPermissionSettingsConfirmed() {
        dismissDialog()
        emit(WeatherEvent.OpenAppPermissionSettings)
    }

    // ---- Permission result (mirrors requestPermissionLauncher callback) -----

    fun onPermissionResult(fineGranted: Boolean, coarseGranted: Boolean) {
        if (fineGranted || coarseGranted) {
            forceRefreshWeatherSettings()
        }
    }

    // ---- Private helpers (direct ports of private fragment methods) --------

    /** Mirrors handlePermissions() */
    private fun handlePermissions() {
        if (WeatherConfig.isEnabled(context) &&
            !controller.getBoolean(XposedKey.WEATHER_CUSTOM_LOCATION)
        ) {
            checkLocationEnabled(force = false)
        } else {
            forceRefreshWeatherSettings()
        }
    }

    /** Mirrors hasPermissions() */
    private fun hasPermissions(): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

    /** Mirrors isLocationEnabled() */
    private fun isLocationEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isLocationEnabled
    }

    /** Mirrors checkLocationEnabled() */
    private fun checkLocationEnabled(force: Boolean) {
        if (!isLocationEnabled()) {
            _state.update { it.copy(dialog = WeatherDialog.LocationDisabled) }
        } else {
            checkLocationPermission(force)
        }
    }

    /** Mirrors checkLocationPermission() */
    private fun checkLocationPermission(force: Boolean) {
        if (!hasPermissions() && !controller.getBoolean(XposedKey.WEATHER_CUSTOM_LOCATION)) {
            // The composable will check shouldShowRationale and decide whether to show
            // the rationale dialog or fire the system permission request directly.
            // We emit the event and let the UI decide (mirrors requestLocationPermission logic).
            emit(WeatherEvent.RequestLocationPermissions)
        } else {
            if (force) forceRefreshWeatherSettings()
            queryAndUpdateWeather()
        }
    }

    /**
     * The composable calls this after evaluating shouldShowRequestPermissionRationale.
     * If rationale should be shown → show our dialog; otherwise the launcher was already fired.
     */
    fun onShouldShowPermissionRationale(shouldShow: Boolean) {
        if (shouldShow) {
            _state.update { it.copy(dialog = WeatherDialog.PermissionRationale) }
        }
        // If false the composable fires the system launcher directly (no dialog needed)
    }

    /** Mirrors enableService() */
    private fun enableService() {
        WeatherScheduler.scheduleUpdates(context)
    }

    /** Mirrors forceRefreshWeatherSettings() */
    fun forceRefreshWeatherSettings() {
        WeatherScheduler.scheduleUpdateNow(context)
    }

    /** Mirrors queryAndUpdateWeather() */
    private fun queryAndUpdateWeather() {
        weatherClient.queryWeather()
        weatherClient.mCachedInfo?.let { info ->
            _state.update { it.copy(updateStatusSummary = info.lastUpdateTime) }
        }
    }

    /** Mirrors getAvailableWeatherIconPacks() */
    @Suppress("DiscouragedApi")
    private fun getAvailableWeatherIconPacks(
        entries: MutableList<String?>,
        values: MutableList<String?>,
        drawables: MutableList<Drawable?>
    ) {
        val packageManager = context.packageManager
        val i = Intent().apply {
            action = BuildConfig.APPLICATION_ID + ".WeatherIconPack"
        }

        for (r in packageManager.queryIntentActivities(i, 0)) {
            val packageName = r.activityInfo.applicationInfo.packageName
            val resources = context.resources

            if (packageName == DEFAULT_WEATHER_ICON_PACKAGE) {
                values.add(0, r.activityInfo.name)
                drawables.add(
                    0,
                    ResourcesCompat.getDrawable(
                        resources,
                        resources.getIdentifier(
                            "google_30", "drawable", BuildConfig.APPLICATION_ID
                        ),
                        context.theme
                    )
                )
            } else {
                values.add(packageName + "." + r.activityInfo.name.split(".").last())
                val name = r.activityInfo.name
                    .split("\\.".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                drawables.add(
                    ResourcesCompat.getDrawable(
                        resources,
                        resources.getIdentifier(
                            name[name.size - 1].lowercase(Locale.getDefault()) + "_30",
                            "drawable",
                            BuildConfig.APPLICATION_ID
                        ),
                        context.theme
                    )
                )
            }

            val label: String = r.activityInfo.loadLabel(packageManager).toString()
            if (packageName == DEFAULT_WEATHER_ICON_PACKAGE) {
                entries.add(0, label)
            } else {
                entries.add(label)
            }
        }
    }

    private fun emit(event: WeatherEvent) {
        viewModelScope.launch { _events.emit(event) }
    }

    companion object {
        private const val TAG = "WeatherViewModel"
        private const val DEFAULT_WEATHER_ICON_PACKAGE = "${BuildConfig.APPLICATION_ID}.google"
    }
}