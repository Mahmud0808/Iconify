package com.drdisagree.iconify.ui.fragments

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_LOCATION
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_MARGINS
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_MARGINS_LEFT
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_MARGINS_TOP
import com.drdisagree.iconify.common.Preferences.WEATHER_ICON_SIZE
import com.drdisagree.iconify.common.Preferences.WEATHER_SHOW_CONDITION
import com.drdisagree.iconify.common.Preferences.WEATHER_SHOW_HUMIDITY
import com.drdisagree.iconify.common.Preferences.WEATHER_SHOW_LOCATION
import com.drdisagree.iconify.common.Preferences.WEATHER_SHOW_WIND
import com.drdisagree.iconify.common.Preferences.WEATHER_STYLE
import com.drdisagree.iconify.common.Preferences.WEATHER_SWITCH
import com.drdisagree.iconify.common.Preferences.WEATHER_TEXT_COLOR
import com.drdisagree.iconify.common.Preferences.WEATHER_TEXT_COLOR_SWITCH
import com.drdisagree.iconify.common.Preferences.WEATHER_TEXT_SIZE
import com.drdisagree.iconify.common.Preferences.WEATHER_UNITS
import com.drdisagree.iconify.common.Preferences.WEATHER_UPDATE_INTERVAL
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.config.RPrefs.putString
import com.drdisagree.iconify.databinding.FragmentXposedLockscreenWeatherBinding
import com.drdisagree.iconify.services.WeatherScheduler
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.OmniJawsClient
import com.drdisagree.iconify.weather.Config
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider

class XposedLockscreenWeather:BaseFragment(),
    OmniJawsClient.OmniJawsObserver {

    private lateinit var binding: FragmentXposedLockscreenWeatherBinding
    private lateinit var mWeatherClient: OmniJawsClient
    private var mTriggerPermissionCheck = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentXposedLockscreenWeatherBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_lockscreen_weather
        )

        mWeatherClient = OmniJawsClient(requireContext(), false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!hasPermissions()) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }

        binding.enableLockscreenWeather.isSwitchChecked = getBoolean(WEATHER_SWITCH, false)
        binding.enableLockscreenWeather.setSwitchChangeListener{ _: CompoundButton?, isChecked: Boolean ->

            putBoolean(WEATHER_SWITCH, isChecked)
            Config.setEnabled(requireContext(), isChecked, WEATHER_SWITCH)

//            updateEnabled(isChecked)

            if (!hasPermissions()) {
                showPermissionDialog()
            } else {
                checkLocationPermissions(false)
            }
        }

        binding.lockscreenWeatherUpdateInterval.setSelectedIndex(getInt(WEATHER_UPDATE_INTERVAL, 1))
        binding.lockscreenWeatherUpdateInterval.setOnItemSelectedListener{
            putInt(WEATHER_UPDATE_INTERVAL, it)
            forceRefreshWeatherSettings()
        }

        binding.lockscreenWeatherLastUpdate.setOnClickListener {
            checkLocationPermissions(false)
            forceRefreshWeatherSettings()
        }

        binding.lockscreenWeatherUnits.setSelectedIndex(if (Config.isMetric(requireContext())) 0 else 1)
        binding.lockscreenWeatherUnits.setOnItemSelectedListener{
            putString(WEATHER_UNITS, it.toString())
            forceRefreshWeatherSettings()
        }

        binding.lockscreenWeatherShowLocation.isSwitchChecked = getBoolean(WEATHER_SHOW_LOCATION, true)
        binding.lockscreenWeatherShowLocation.setSwitchChangeListener{ _: CompoundButton?, isChecked: Boolean ->
            putBoolean(WEATHER_SHOW_LOCATION, isChecked)
        }

        binding.lockscreenWeatherShowCondition.isSwitchChecked = getBoolean(WEATHER_SHOW_CONDITION, true)
        binding.lockscreenWeatherShowCondition.setSwitchChangeListener{ _: CompoundButton?, isChecked: Boolean ->
            putBoolean(WEATHER_SHOW_CONDITION, isChecked)
        }

        binding.lockscreenWeatherShowHumidity.isSwitchChecked = getBoolean(WEATHER_SHOW_HUMIDITY, false)
        binding.lockscreenWeatherShowHumidity.setSwitchChangeListener{ _: CompoundButton?, isChecked: Boolean ->
            putBoolean(WEATHER_SHOW_HUMIDITY, isChecked)
        }

        binding.lockscreenWeatherShowWind.isSwitchChecked = getBoolean(WEATHER_SHOW_WIND, false)
        binding.lockscreenWeatherShowWind.setSwitchChangeListener{ _: CompoundButton?, isChecked: Boolean ->
            putBoolean(WEATHER_SHOW_WIND, isChecked)
        }

        binding.lockscreenWeatherTextSize.sliderValue = getInt(WEATHER_TEXT_SIZE, 16)
        binding.lockscreenWeatherTextSize.setOnSliderChangeListener{ _: Slider?, value: Float, _: Boolean ->
            putInt(WEATHER_TEXT_SIZE, value.toInt())
        }

        binding.lockscreenWeatherImageSize.sliderValue = getInt(WEATHER_ICON_SIZE, 18)
        binding.lockscreenWeatherImageSize.setOnSliderChangeListener{ _: Slider?, value: Float, _: Boolean ->
            putInt(WEATHER_ICON_SIZE, value.toInt())
        }

        binding.lockscreenWeatherCustomColorSwitch.isSwitchChecked = getBoolean(WEATHER_TEXT_COLOR_SWITCH, false)
        binding.lockscreenWeatherCustomColorSwitch.setSwitchChangeListener{ _: CompoundButton?, isChecked: Boolean ->
            putBoolean(WEATHER_TEXT_COLOR_SWITCH, isChecked)
            binding.lockscreenWeatherCustomColor.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.lockscreenWeatherCustomColor.previewColor = getInt(WEATHER_TEXT_COLOR, 0xFFFFFFFF.toInt())
        binding.lockscreenWeatherCustomColor.setOnColorSelectedListener { color: Int ->
            putInt(WEATHER_TEXT_COLOR, color)
        }

        binding.lockscreenWeatherCustomLocation.isSwitchChecked = getBoolean(WEATHER_CUSTOM_LOCATION, false)
        binding.lockscreenWeatherCustomLocation.setSwitchChangeListener{ _: CompoundButton?, isChecked: Boolean ->
            putBoolean(WEATHER_CUSTOM_LOCATION, isChecked)
        }

        binding.lockscreenWeatherCustomLocationMenu.setOnClickListener(View.OnClickListener {
            //TODO: Implement custom location
        })

        binding.lockscreenWeatherCustomMargins.isSwitchChecked = getBoolean(WEATHER_CUSTOM_MARGINS, false)
        binding.lockscreenWeatherCustomMargins.setSwitchChangeListener{ _: CompoundButton?, isChecked: Boolean ->
            putBoolean(WEATHER_CUSTOM_MARGINS, isChecked)
            if (isChecked) {
                binding.lockscreenWeatherMarginTop.visibility = View.VISIBLE
                binding.lockscreenWeatherMarginLeft.visibility = View.VISIBLE
            } else {
                binding.lockscreenWeatherMarginTop.visibility = View.GONE
                binding.lockscreenWeatherMarginLeft.visibility = View.GONE
            }
        }

        binding.lockscreenWeatherMarginTop.sliderValue = getInt(WEATHER_CUSTOM_MARGINS_TOP, 0)
        binding.lockscreenWeatherMarginTop.setOnSliderChangeListener{ _: Slider?, value: Float, _: Boolean ->
            putInt(WEATHER_CUSTOM_MARGINS_TOP, value.toInt())
        }

        binding.lockscreenWeatherMarginLeft.sliderValue = getInt(WEATHER_CUSTOM_MARGINS_LEFT, 0)
        binding.lockscreenWeatherMarginLeft.setOnSliderChangeListener{ _: Slider?, value: Float, _: Boolean ->
            putInt(WEATHER_CUSTOM_MARGINS_LEFT, value.toInt())
        }

        binding.lockscreenWeatherBg.setSelectedIndex(getInt(WEATHER_STYLE, 0))
        binding.lockscreenWeatherBg.setOnItemSelectedListener{
            putInt(WEATHER_STYLE, it)
        }

//        updateUI(getBoolean(WEATHER_SWITCH, false))
    }

    override fun onResume() {
        super.onResume()
        mWeatherClient.addObserver(this)
        if (mTriggerPermissionCheck) {
            checkLocationPermissions(true)
            mTriggerPermissionCheck = false
        }
        queryAndUpdateWeather()
    }

    private fun hasPermissions(): Boolean {
        return (
                requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) &&
                (requireContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) &&
                (requireContext().checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun showPermissionDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(R.string.weather_permission_dialog_title)
        builder.setMessage(R.string.weather_permission_dialog_message)
        builder.setCancelable(false)
        builder.setPositiveButton(android.R.string.ok) { dialog: DialogInterface?, which: Int ->
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri =
                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            intent.setData(uri)
            startActivity(intent)
        }
        builder.show()
    }

    private fun showDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())

        // Build and show the dialog
        builder.setTitle(R.string.weather_retrieve_location_dialog_title)
        builder.setMessage(R.string.weather_retrieve_location_dialog_message)
        builder.setCancelable(false)
        builder.setPositiveButton(
            R.string.weather_retrieve_location_dialog_enable_button
        ) { dialog1, whichButton ->
            mTriggerPermissionCheck = true
            val intent =
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog: Dialog = builder.create()
        dialog.show()
    }

    private fun checkLocationPermissions(force: Boolean) {
        if (!hasPermissions()) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        } else {
            if (force) {
                forceRefreshWeatherSettings()
            }
            queryAndUpdateWeather()
        }
    }

    private fun checkLocationEnabled() {
        if (!doCheckLocationEnabled()) {
            showDialog()
        } else {
            checkLocationPermissions(false)
        }
    }

    private fun checkLocationEnabledInitial() {
        if (!doCheckLocationEnabled()) {
            showDialog()
        } else {
            checkLocationPermissions(true)
        }
    }

    private fun doCheckLocationEnabled(): Boolean {
        val lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isLocationEnabled
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
            if ((fineLocationGranted) ||
                (coarseLocationGranted)
            ) {
                forceRefreshWeatherSettings()
            }
        }

    private fun forceRefreshWeatherSettings() {
        WeatherScheduler.scheduleUpdateNow(context)
    }

    private fun queryAndUpdateWeather() {
        mWeatherClient.queryWeather()
        Log.d("Weather", "Querying weather " + mWeatherClient.getWeatherInfo().toString())
        if (mWeatherClient.getWeatherInfo() != null) {
            requireActivity().runOnUiThread {
                binding.lockscreenWeatherLastUpdate.setSummary(mWeatherClient.getWeatherInfo()!!.lastUpdateTime)
            }
        }
    }

    override fun weatherUpdated() {
        queryAndUpdateWeather()
    }

    override fun weatherError(errorReason: Int) {
        var errorString: String? = null
        errorString = if (errorReason == OmniJawsClient.EXTRA_ERROR_DISABLED) {
            appContextLocale.getString(R.string.omnijaws_service_disabled)
        } else if (errorReason == OmniJawsClient.EXTRA_ERROR_LOCATION) {
            appContextLocale.getString(R.string.omnijaws_service_error_location)
        } else if (errorReason == OmniJawsClient.EXTRA_ERROR_NETWORK) {
            appContextLocale.getString(R.string.omnijaws_service_error_network)
        } else if (errorReason == OmniJawsClient.EXTRA_ERROR_NO_PERMISSIONS) {
            appContextLocale.getString(R.string.omnijaws_service_error_permissions)
        } else {
            appContextLocale.getString(R.string.omnijaws_service_error_long)
        }
        val s: String = errorString
        requireActivity().runOnUiThread {
            binding.lockscreenWeatherLastUpdate.setSummary(s)
        }
    }

}