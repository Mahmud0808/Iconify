package com.drdisagree.iconify.ui.base

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_LOCATION
import com.drdisagree.iconify.common.Preferences.WEATHER_ICON_PACK
import com.drdisagree.iconify.common.Preferences.WEATHER_PROVIDER
import com.drdisagree.iconify.common.Preferences.WEATHER_SWITCH
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.services.WeatherScheduler
import com.drdisagree.iconify.ui.activities.LocationBrowseActivity
import com.drdisagree.iconify.ui.activities.LocationBrowseActivity.Companion.DATA_LOCATION_LAT
import com.drdisagree.iconify.ui.activities.LocationBrowseActivity.Companion.DATA_LOCATION_LON
import com.drdisagree.iconify.ui.activities.LocationBrowseActivity.Companion.DATA_LOCATION_NAME
import com.drdisagree.iconify.ui.preferences.BottomSheetListPreference
import com.drdisagree.iconify.ui.preferences.SwitchPreference
import com.drdisagree.iconify.utils.OmniJawsClient
import com.drdisagree.iconify.utils.weather.WeatherConfig
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

abstract class WeatherPreferenceFragment : ControlledPreferenceFragmentCompat(),
    OmniJawsClient.OmniJawsObserver {

    private var mCustomLocation: SwitchPreference? = null
    private var mInitialCheck = true
    private var mWeatherIconPack: BottomSheetListPreference? = null
    private var mUpdateStatus: Preference? = null
    private var mWeatherClient: OmniJawsClient? = null
    private var mCustomLocationActivity: Preference? = null

    abstract fun getMainSwitchKey(): String

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        mWeatherClient = OmniJawsClient(requireContext())

        mWeatherIconPack = findPreference(WEATHER_ICON_PACK)

        var settingHeaderPackage: String = WeatherConfig.getIconPack(requireContext()).toString()
        val entries: MutableList<String?> = ArrayList()
        val values: MutableList<String> = ArrayList()
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
                }
            })
        var valueIndex: Int = mWeatherIconPack!!.findIndexOfValue(settingHeaderPackage)
        if (valueIndex == -1) {
            // no longer found
            settingHeaderPackage = Companion.DEFAULT_WEATHER_ICON_PACKAGE
            //WeatherConfig.setIconPack(getContext(), settingHeaderPackage);
            valueIndex = mWeatherIconPack!!.findIndexOfValue(settingHeaderPackage)
        }
        mWeatherIconPack!!.setValueIndex(if (valueIndex >= 0) valueIndex else 0)
        mWeatherIconPack!!.setSummary(mWeatherIconPack!!.getEntry())

        mUpdateStatus = findPreference(Companion.PREF_KEY_UPDATE_STATUS)
        if (mUpdateStatus != null) {
            mUpdateStatus!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    forceRefreshWeatherSettings()
                    true
                }
        }

        mCustomLocation = findPreference(WEATHER_CUSTOM_LOCATION)
        mCustomLocation?.setOnPreferenceClickListener {
            forceRefreshWeatherSettings()
            true
        }

        mCustomLocationActivity = findPreference("weather_custom_location_picker")
        if (mCustomLocationActivity != null) {
            mCustomLocationActivity!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    mCustomLocationLauncher.launch(
                        Intent(
                            context,
                            LocationBrowseActivity::class.java
                        )
                    )
                    true
                }
            mCustomLocationActivity!!.setSummary(WeatherConfig.getLocationName(requireContext()))
        }
    }

    override fun onResume() {
        super.onResume()
        mWeatherClient!!.addObserver(this)

        handlePermissions()
    }

    private fun handlePermissions() {
        if (getBoolean(WEATHER_SWITCH, false) &&
            !getBoolean(WEATHER_CUSTOM_LOCATION, false)
        ) {
            checkLocationEnabled(mInitialCheck)
            mInitialCheck = false
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

    private var mCustomLocationLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent: Intent? = result.data

            if (intent!!.hasExtra(DATA_LOCATION_NAME)) {
                val locationName = intent.getStringExtra(DATA_LOCATION_NAME)
                val lat = intent.getDoubleExtra(DATA_LOCATION_LAT, 0.0)
                val lon = intent.getDoubleExtra(DATA_LOCATION_LON, 0.0)

                WeatherConfig.apply {
                    setLocationId(requireContext(), lat.toString(), lon.toString())
                    setLocationName(requireContext(), locationName)
                }

                if (locationName.isNullOrEmpty()) {
                    mUpdateStatus!!.setSummary(com.drdisagree.iconify.R.string.not_available)
                } else {
                    mUpdateStatus!!.setSummary(locationName)
                }

                if (getBoolean(WEATHER_SWITCH, false)
                    && !getBoolean(WEATHER_CUSTOM_LOCATION, false)
                ) {
                    checkLocationEnabled(true)
                }
            }
        }
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
            .setTitle(com.drdisagree.iconify.R.string.weather_retrieve_location_dialog_title)
            .setMessage(com.drdisagree.iconify.R.string.weather_retrieve_location_dialog_message)
            .setCancelable(false)
            .setPositiveButton(com.drdisagree.iconify.R.string.weather_retrieve_location_dialog_enable_button) { _, _ ->
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
            .setTitle(com.drdisagree.iconify.R.string.weather_permission_dialog_title)
            .setMessage(com.drdisagree.iconify.R.string.weather_permission_dialog_message)
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
        }
    }

    @Suppress("DiscouragedApi")
    private fun getAvailableWeatherIconPacks(
        entries: MutableList<String?>,
        values: MutableList<String>,
        drawables: MutableList<Drawable?>
    ) {
        val i = Intent()
        val packageManager = requireContext().packageManager
        i.setAction(BuildConfig.APPLICATION_ID + ".WeatherIconPack")
        for (r in packageManager.queryIntentActivities(i, 0)) {
            val packageName = r.activityInfo.packageName
            if (packageName == Companion.DEFAULT_WEATHER_ICON_PACKAGE) {
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
                values.add(r.activityInfo.name)
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
            var label: String? = r.activityInfo?.loadLabel(packageManager)?.toString()
            if (label == null) {
                label = r.activityInfo.packageName
            }
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
        val s: String = errorString
        requireActivity().runOnUiThread {
            if (mUpdateStatus != null) {
                mUpdateStatus!!.summary = s
            }
        }
    }

    private fun queryAndUpdateWeather() {
        mWeatherClient!!.queryWeather()
        if (mWeatherClient?.weatherInfo != null) {
            requireActivity().runOnUiThread {
                if (mUpdateStatus != null) {
                    mUpdateStatus!!.setSummary(mWeatherClient!!.weatherInfo!!.lastUpdateTime)
                }
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
        private const val PREF_KEY_UPDATE_STATUS: String = "update_status"
    }
}