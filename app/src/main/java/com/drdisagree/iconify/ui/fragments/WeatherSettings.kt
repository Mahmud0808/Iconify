package com.drdisagree.iconify.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_LOCATION
import com.drdisagree.iconify.common.Preferences.WEATHER_OWM_KEY
import com.drdisagree.iconify.common.Preferences.WEATHER_PROVIDER
import com.drdisagree.iconify.common.Preferences.WEATHER_UNITS
import com.drdisagree.iconify.common.Preferences.WEATHER_UPDATE_INTERVAL
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.putString
import com.drdisagree.iconify.databinding.FragmentWeatherSettingsBinding
import com.drdisagree.iconify.services.WeatherScheduler
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.EditTextDialog
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.OmniJawsClient
import com.drdisagree.iconify.utils.weather.WeatherConfig
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class WeatherSettings: BaseFragment(), OmniJawsClient.OmniJawsObserver {

    private lateinit var binding: FragmentWeatherSettingsBinding
    private lateinit var mWeatherClient: OmniJawsClient
    private val mapping: IntArray = intArrayOf(1, 2, 4, 6, 12)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherSettingsBinding.inflate(inflater, container, false)

        mWeatherClient = OmniJawsClient(requireContext())
        mWeatherClient.queryWeather()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_lockscreen_widgets
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val updateInterval = WeatherConfig.getUpdateInterval(requireContext())
        var selectedIndex = 1
        when (updateInterval) {
            1 -> selectedIndex = 0
            2 -> selectedIndex = 1
            4 -> selectedIndex = 2
            6 -> selectedIndex = 3
            12 -> selectedIndex = 4
        }
        binding.weatherUpdateInterval.setSelectedIndex(selectedIndex)
        binding.weatherUpdateInterval.setOnItemSelectedListener {
            putString(WEATHER_UPDATE_INTERVAL, mapping[it].toString())
            handlePermissions()
            forceRefreshWeatherSettings()
        }

        binding.weatherLastUpdate.setOnClickListener {
            handlePermissions()
            forceRefreshWeatherSettings()
        }

        binding.weatherProvider.setSelectedIndex(
            RPrefs.getString(WEATHER_PROVIDER, "0")!!.toInt()
        )
        binding.weatherProvider.setOnItemSelectedListener {
            putString(WEATHER_PROVIDER, it.toString())

            if (WeatherConfig.getOwmKey(requireContext()).isEmpty() && it != 0) {
                showOwnKeyDialog()
            } else {
                forceRefreshWeatherSettings()
            }
        }

        val owmKey = WeatherConfig.getOwmKey(requireContext())
        if (owmKey.isEmpty()) {
            binding.weatherOwmKey.setSummary(getString(R.string.not_available))
        } else {
            binding.weatherOwmKey.setSummary(
                "*".repeat(owmKey.length - 4) + owmKey.takeLast(
                    4
                )
            )
        }
        binding.weatherOwmKey.setEditTextValue(owmKey)
        binding.weatherOwmKey.setOnEditTextListener(object :
            EditTextDialog.EditTextDialogListener {
            override fun onOkPressed(dialogId: Int, newText: String) {
                putString(WEATHER_OWM_KEY, newText)
                handlePermissions()
                forceRefreshWeatherSettings()

                val maskedKey = if (newText.length > 4) {
                    "*".repeat(newText.length - 4) + newText.takeLast(4)
                } else {
                    newText.ifEmpty {
                        getString(R.string.not_available)
                    }
                }

                binding.weatherOwmKey.setSummary(maskedKey)
                binding.weatherOwmKey.setEditTextValue(newText)
            }
        })

        binding.weatherUnits.setSelectedIndex(if (WeatherConfig.isMetric(requireContext())) 0 else 1)
        binding.weatherUnits.setOnItemSelectedListener { index: Int ->
            putString(WEATHER_UNITS, index.toString())
            forceRefreshWeatherSettings()
        }

    }

    override fun weatherError(errorReason: Int) {
        val errorString: String = when (errorReason) {
            OmniJawsClient.EXTRA_ERROR_DISABLED -> {
                appContextLocale.getString(R.string.omnijaws_service_disabled)
            }

            OmniJawsClient.EXTRA_ERROR_LOCATION -> {
                appContextLocale.getString(R.string.omnijaws_service_error_location)
            }

            OmniJawsClient.EXTRA_ERROR_NETWORK -> {
                appContextLocale.getString(R.string.omnijaws_service_error_network)
            }

            OmniJawsClient.EXTRA_ERROR_NO_PERMISSIONS -> {
                appContextLocale.getString(R.string.omnijaws_service_error_permissions)
            }

            else -> {
                appContextLocale.getString(R.string.omnijaws_service_error_long)
            }
        }

        requireActivity().runOnUiThread {
            binding.weatherLastUpdate.setSummary(errorString)
        }
    }

    @SuppressLint("DiscouragedApi")
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

    override fun onResume() {
        super.onResume()

        mWeatherClient.addObserver(this)
        handlePermissions()
    }

    override fun onPause() {
        super.onPause()

        mWeatherClient.removeObserver(this)
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

    private fun handlePermissions() {
        if (!getBoolean(WEATHER_CUSTOM_LOCATION, false)
        ) {
            checkLocationEnabled(false)
        }
    }

    private fun checkLocationEnabled(force: Boolean) {
        if (!isLocationEnabled()) {
            showLocationPermissionDialog()
        } else {
            checkLocationPermission(force)
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

    private fun checkLocationPermission(force: Boolean) {
        if (!hasPermissions()) {
            if (!getBoolean(WEATHER_CUSTOM_LOCATION, false)
            ) {
                requestLocationPermission()
            }
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

    private fun requestLocationPermission() {
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

    private fun isLocationEnabled(): Boolean {
        val lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isLocationEnabled
    }

    private var locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted: Boolean = permissions.getOrDefault(
            Manifest.permission.ACCESS_FINE_LOCATION, false
        )
        val coarseLocationGranted: Boolean = permissions.getOrDefault(
            Manifest.permission.ACCESS_COARSE_LOCATION, false
        )

        if (fineLocationGranted || coarseLocationGranted) {
            forceRefreshWeatherSettings()
        }
    }

    private fun forceRefreshWeatherSettings() {
        WeatherScheduler.scheduleUpdateNow(requireContext())
    }

    private fun queryAndUpdateWeather() {
        mWeatherClient.queryWeather()

        if (mWeatherClient.weatherInfo != null && activity != null) {
            requireActivity().runOnUiThread {
                binding.weatherLastUpdate.setSummary(mWeatherClient.weatherInfo!!.lastUpdateTime)
            }
        }
    }

    override fun weatherUpdated() {
        queryAndUpdateWeather()
    }

    companion object {
        private const val DEFAULT_WEATHER_ICON_PACKAGE: String =
            "${BuildConfig.APPLICATION_ID}.google"
    }

}