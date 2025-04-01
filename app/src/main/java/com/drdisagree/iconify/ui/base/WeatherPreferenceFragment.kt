package com.drdisagree.iconify.ui.base

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.preference.Preference
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Preferences.PREF_KEY_UPDATE_STATUS
import com.drdisagree.iconify.data.common.Preferences.WEATHER_CUSTOM_LOCATION
import com.drdisagree.iconify.data.common.Preferences.WEATHER_ICON_PACK
import com.drdisagree.iconify.data.common.Preferences.WEATHER_OWM_KEY
import com.drdisagree.iconify.data.common.Preferences.WEATHER_PROVIDER
import com.drdisagree.iconify.data.common.Preferences.WEATHER_YANDEX_KEY
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.data.config.RPrefs.getBoolean
import com.drdisagree.iconify.services.WeatherScheduler
import com.drdisagree.iconify.ui.fragments.xposed.LocationBrowse.Companion.DATA_LOCATION_KEY
import com.drdisagree.iconify.ui.fragments.xposed.LocationBrowse.Companion.DATA_LOCATION_NAME
import com.drdisagree.iconify.ui.preferences.BottomSheetListPreference
import com.drdisagree.iconify.ui.preferences.SwitchPreference
import com.drdisagree.iconify.utils.OmniJawsClient
import com.drdisagree.iconify.utils.weather.WeatherConfig
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

abstract class WeatherPreferenceFragment : ControlledPreferenceFragmentCompat(),
    OmniJawsClient.OmniJawsObserver {

    private var mCustomLocation: SwitchPreference? = null
    private var mWeatherIconPack: BottomSheetListPreference? = null
    private var mUpdateStatus: Preference? = null
    private var mWeatherClient: OmniJawsClient? = null

    abstract fun getMainSwitchKey(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(DATA_LOCATION_KEY) { _, bundle ->
            val locationName = bundle.getString(DATA_LOCATION_NAME)

            Log.d("WeatherPreferenceFragment", "locationName: $locationName")

            if (WeatherConfig.isEnabled(requireContext())
                && !getBoolean(WEATHER_CUSTOM_LOCATION, false)
            ) {
                checkLocationEnabled(true)
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        mWeatherClient = OmniJawsClient(requireContext())

        mWeatherIconPack = findPreference(WEATHER_ICON_PACK)

        var settingHeaderPackage: String = WeatherConfig.getIconPack(requireContext()).toString()
        val entries: MutableList<String?> = ArrayList()
        val values: MutableList<String?> = ArrayList()
        val drawables: MutableList<Drawable?> = ArrayList()
        getAvailableWeatherIconPacks(entries, values, drawables)
        mWeatherIconPack!!.entries = entries.toTypedArray()
        mWeatherIconPack!!.entryValues = values.toTypedArray()
        mWeatherIconPack!!.createDefaultAdapter(
            drawables.filterNotNull().toTypedArray(),
            object : BottomSheetListPreference.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    RPrefs.putString(WEATHER_ICON_PACK, values[position])
                    mWeatherIconPack!!.setSummary(entries[position])
                    forceRefreshWeatherSettings()
                }
            })
        var valueIndex: Int = mWeatherIconPack!!.findIndexOfValue(settingHeaderPackage)
        if (valueIndex == -1) {
            // no longer found
            settingHeaderPackage = DEFAULT_WEATHER_ICON_PACKAGE
            valueIndex = mWeatherIconPack!!.findIndexOfValue(settingHeaderPackage)
        }
        mWeatherIconPack!!.setValueIndex(if (valueIndex >= 0) valueIndex else 0)
        mWeatherIconPack!!.setSummary(mWeatherIconPack!!.getEntry())

        mUpdateStatus = findPreference(PREF_KEY_UPDATE_STATUS)
        mUpdateStatus?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            forceRefreshWeatherSettings()
            true
        }

        mCustomLocation = findPreference(WEATHER_CUSTOM_LOCATION)
        mCustomLocation?.setOnPreferenceClickListener {
            forceRefreshWeatherSettings()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        mWeatherClient!!.addObserver(this)

        handlePermissions()
    }

    private fun handlePermissions() {
        if (WeatherConfig.isEnabled(requireContext()) &&
            !getBoolean(WEATHER_CUSTOM_LOCATION, false)
        ) {
            checkLocationEnabled(false)
        } else {
            forceRefreshWeatherSettings()
        }
    }

    private fun hasPermissions(): Boolean {
        return (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) &&
                (requireContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) &&
                (requireContext().checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)
    }

    private fun isLocationEnabled(): Boolean {
        val lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isLocationEnabled
    }

    private fun requestLocationPermission(locationPermissionRequest: ActivityResultLauncher<Array<String>>) {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ||
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            showApplicationPermissionDialog()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }
    }

    private fun checkLocationEnabled(force: Boolean) {
        if (!isLocationEnabled()) {
            showLocationPermissionDialog()
        } else {
            checkLocationPermission(force)
        }
    }

    private fun checkLocationPermission(force: Boolean) {
        if (!hasPermissions() && !getBoolean(WEATHER_CUSTOM_LOCATION, false)) {
            requestLocationPermission(requestPermissionLauncher)
        } else {
            if (force) {
                forceRefreshWeatherSettings()
            }
            queryAndUpdateWeather()
        }
    }

    private fun showLocationPermissionDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.weather_retrieve_location_dialog_title)
            .setMessage(R.string.weather_retrieve_location_dialog_message)
            .setCancelable(false)
            .setPositiveButton(R.string.weather_retrieve_location_dialog_enable_button) { _, _ ->
                startActivity(
                    Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    ).apply {
                        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .show()
    }

    private fun showApplicationPermissionDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.weather_permission_dialog_title)
            .setMessage(R.string.weather_permission_dialog_message)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        setData(
                            Uri.fromParts(
                                "package",
                                requireContext().packageName,
                                null
                            )
                        )
                    }
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .show()
    }

    private fun enableService() {
        WeatherScheduler.scheduleUpdates(requireContext())
    }

    override fun onPause() {
        super.onPause()
        mWeatherClient!!.removeObserver(this)
    }

    private fun showOwnKeyDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(appContextLocale.getString(R.string.weather_provider_owm_key_title))
            .setMessage(appContextLocale.getString(R.string.weather_provider_owm_key_message))
            .setCancelable(false)
            .setPositiveButton(appContextLocale.getString(R.string.understood), null)
            .create()
            .show()
    }

    private fun showYandexKeyDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(appContextLocale.getString(R.string.weather_provider_yandex_key_title))
            .setMessage(appContextLocale.getString(R.string.weather_provider_yandex_key_message))
            .setCancelable(false)
            .setPositiveButton(appContextLocale.getString(R.string.understood), null)
            .create()
            .show()
    }

    override fun updateScreen(key: String?) {
        super.updateScreen(key)

        if (key == null) return

        val mainKey = getMainSwitchKey()

        if (key == mainKey) {
            WeatherConfig.setEnabled(requireContext(), getBoolean(mainKey, false), mainKey)
            if (getBoolean(mainKey, false)) {
                handlePermissions()
                enableService()
                forceRefreshWeatherSettings()
            }
        } else if (key == WEATHER_PROVIDER) {
            forceRefreshWeatherSettings()
            if (RPrefs.getString(WEATHER_PROVIDER) == "1" && RPrefs.getString(WEATHER_OWM_KEY)
                    .isNullOrEmpty()
            ) {
                showOwnKeyDialog()
            } else if (RPrefs.getString(WEATHER_PROVIDER) == "2" && RPrefs.getString(
                    WEATHER_YANDEX_KEY
                )
                    .isNullOrEmpty()) {
                showYandexKeyDialog()
            }
        }
    }

    @Suppress("DiscouragedApi")
    private fun getAvailableWeatherIconPacks(
        entries: MutableList<String?>,
        values: MutableList<String?>,
        drawables: MutableList<Drawable?>
    ) {
        val i = Intent()
        val packageManager = requireContext().packageManager
        i.setAction(BuildConfig.APPLICATION_ID + ".WeatherIconPack")
        for (r in packageManager.queryIntentActivities(i, 0)) {
            val packageName = r.activityInfo.applicationInfo.packageName
            if (packageName == DEFAULT_WEATHER_ICON_PACKAGE) {
                values.add(0, r.activityInfo.name)
                drawables.add(
                    0,
                    ResourcesCompat.getDrawable(
                        resources,
                        resources.getIdentifier(
                            "google_30",
                            "drawable",
                            BuildConfig.APPLICATION_ID
                        ),
                        requireContext().theme
                    )
                )
            } else {
                values.add(packageName + "." + r.activityInfo.name.split(".").last())
                val name = r.activityInfo.name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                drawables.add(
                    ResourcesCompat.getDrawable(
                        resources, resources.getIdentifier(
                            name[name.size - 1].lowercase(
                                Locale.getDefault()
                            ) + "_30", "drawable", BuildConfig.APPLICATION_ID
                        ), requireContext().theme
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

    override fun weatherUpdated() {
        queryAndUpdateWeather()
    }

    override fun weatherError(errorReason: Int) {
        val errorString: String = when (errorReason) {
            OmniJawsClient.EXTRA_ERROR_DISABLED -> {
                resources.getString(R.string.omnijaws_service_disabled)
            }

            OmniJawsClient.EXTRA_ERROR_LOCATION -> {
                resources.getString(R.string.omnijaws_service_error_location)
            }

            OmniJawsClient.EXTRA_ERROR_NETWORK -> {
                resources.getString(R.string.omnijaws_service_error_network)
            }

            OmniJawsClient.EXTRA_ERROR_NO_PERMISSIONS -> {
                resources.getString(R.string.omnijaws_service_error_permissions)
            }

            else -> {
                resources.getString(R.string.omnijaws_service_error_long)
            }
        }

        requireActivity().runOnUiThread {
            mUpdateStatus?.summary = errorString
        }
    }

    private fun queryAndUpdateWeather() {
        mWeatherClient!!.queryWeather()
        mWeatherClient?.mCachedInfo?.let {
            requireActivity().runOnUiThread {
                mUpdateStatus?.setSummary(it.lastUpdateTime)
            }
        }
    }

    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val fineLocationGranted: Boolean = result.getOrDefault(
                Manifest.permission.ACCESS_FINE_LOCATION, false
            )
            val coarseLocationGranted: Boolean = result.getOrDefault(
                Manifest.permission.ACCESS_COARSE_LOCATION, false
            )
            if (fineLocationGranted || coarseLocationGranted) {
                forceRefreshWeatherSettings()
            }
        }

    private fun forceRefreshWeatherSettings() {
        WeatherScheduler.scheduleUpdateNow(appContext)
    }

    companion object {
        private const val DEFAULT_WEATHER_ICON_PACKAGE: String =
            "${BuildConfig.APPLICATION_ID}.google"
    }
}