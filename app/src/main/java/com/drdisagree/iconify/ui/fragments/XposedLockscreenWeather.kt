package com.drdisagree.iconify.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY
import com.drdisagree.iconify.common.Preferences.WEATHER_CENTER_VIEW
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_LOCATION
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_MARGINS_BOTTOM
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_MARGINS_SIDE
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_MARGINS_TOP
import com.drdisagree.iconify.common.Preferences.WEATHER_ICON_PACK
import com.drdisagree.iconify.common.Preferences.WEATHER_ICON_SIZE
import com.drdisagree.iconify.common.Preferences.WEATHER_OWM_KEY
import com.drdisagree.iconify.common.Preferences.WEATHER_PROVIDER
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
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.config.RPrefs.putString
import com.drdisagree.iconify.databinding.FragmentXposedLockscreenWeatherBinding
import com.drdisagree.iconify.services.WeatherScheduler
import com.drdisagree.iconify.ui.activities.LocationBrowseActivity
import com.drdisagree.iconify.ui.activities.LocationBrowseActivity.Companion.DATA_LOCATION_LAT
import com.drdisagree.iconify.ui.activities.LocationBrowseActivity.Companion.DATA_LOCATION_LON
import com.drdisagree.iconify.ui.activities.LocationBrowseActivity.Companion.DATA_LOCATION_NAME
import com.drdisagree.iconify.ui.adapters.IconsAdapter
import com.drdisagree.iconify.ui.adapters.IconsAdapter.Companion.WEATHER_ICONS_ADAPTER
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.EditTextDialog
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.OmniJawsClient
import com.drdisagree.iconify.utils.SystemUtil
import com.drdisagree.iconify.utils.weather.WeatherConfig
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import java.util.Locale

class XposedLockscreenWeather : BaseFragment(), OmniJawsClient.OmniJawsObserver {

    private lateinit var binding: FragmentXposedLockscreenWeatherBinding
    private lateinit var mWeatherClient: OmniJawsClient
    private val mapping: IntArray = intArrayOf(1, 2, 4, 6, 12)

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

        mWeatherClient = OmniJawsClient(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.enableLockscreenWeather.isSwitchChecked = getBoolean(WEATHER_SWITCH, false)
        binding.enableLockscreenWeather.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(WEATHER_SWITCH, isChecked)

            WeatherConfig.setEnabled(requireContext(), isChecked, WEATHER_SWITCH)
            queryAndUpdateWeather()

            if (isChecked) {
                WeatherScheduler.scheduleUpdates(requireContext())
                if (mWeatherClient.weatherInfo != null) {
                    // Weather enabled but updated more than 1h ago
                    if (System.currentTimeMillis() - mWeatherClient.weatherInfo!!.timeStamp!! > 3600000) {
                        WeatherScheduler.scheduleUpdateNow(requireContext());
                    }
                } else {
                    // Weather not enabled so we will update now
                    WeatherScheduler.scheduleUpdateNow(requireContext());
                }
            }

            updateUI()

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        val updateInterval = WeatherConfig.getUpdateInterval(requireContext())
        var selectedIndex = 1
        when (updateInterval) {
            1 -> selectedIndex = 0
            2 -> selectedIndex = 1
            4 -> selectedIndex = 2
            6 -> selectedIndex = 3
            12 -> selectedIndex = 4
        }
        binding.lockscreenWeatherUpdateInterval.setSelectedIndex(selectedIndex)
        binding.lockscreenWeatherUpdateInterval.setOnItemSelectedListener {
            putString(WEATHER_UPDATE_INTERVAL, mapping[it].toString())
            handlePermissions()
            forceRefreshWeatherSettings()
        }

        binding.lockscreenWeatherLastUpdate.setOnClickListener {
            handlePermissions()
            forceRefreshWeatherSettings()
        }

        binding.lockscreenWeatherProvider.setSelectedIndex(
            RPrefs.getString(WEATHER_PROVIDER, "0")!!.toInt()
        )
        binding.lockscreenWeatherProvider.setOnItemSelectedListener {
            putString(WEATHER_PROVIDER, it.toString())

            if (WeatherConfig.getOwmKey(requireContext()).isEmpty() && it != 0) {
                showOwnKeyDialog()
            } else {
                forceRefreshWeatherSettings()
            }
        }

        val owmKey = WeatherConfig.getOwmKey(requireContext())
        if (owmKey.isEmpty()) {
            binding.lockscreenWeatherOwmKey.setSummary(getString(R.string.not_available))
        } else {
            binding.lockscreenWeatherOwmKey.setSummary(
                "*".repeat(owmKey.length - 4) + owmKey.takeLast(
                    4
                )
            )
        }
        binding.lockscreenWeatherOwmKey.setEditTextValue(owmKey)
        binding.lockscreenWeatherOwmKey.setOnEditTextListener(object :
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

                binding.lockscreenWeatherOwmKey.setSummary(maskedKey)
                binding.lockscreenWeatherOwmKey.setEditTextValue(newText)
            }
        })

        binding.lockscreenWeatherUnits.setSelectedIndex(if (WeatherConfig.isMetric(requireContext())) 0 else 1)
        binding.lockscreenWeatherUnits.setOnItemSelectedListener { index: Int ->
            putString(WEATHER_UNITS, index.toString())
            forceRefreshWeatherSettings()
        }

        binding.lockscreenWeatherShowLocation.isSwitchChecked =
            getBoolean(WEATHER_SHOW_LOCATION, true)
        binding.lockscreenWeatherShowLocation.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(WEATHER_SHOW_LOCATION, isChecked)
        }

        binding.lockscreenWeatherShowCondition.isSwitchChecked =
            getBoolean(WEATHER_SHOW_CONDITION, true)
        binding.lockscreenWeatherShowCondition.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(WEATHER_SHOW_CONDITION, isChecked)
        }

        binding.lockscreenWeatherShowHumidity.isSwitchChecked =
            getBoolean(WEATHER_SHOW_HUMIDITY, false)
        binding.lockscreenWeatherShowHumidity.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(WEATHER_SHOW_HUMIDITY, isChecked)
        }

        binding.lockscreenWeatherShowWind.isSwitchChecked = getBoolean(WEATHER_SHOW_WIND, false)
        binding.lockscreenWeatherShowWind.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(WEATHER_SHOW_WIND, isChecked)
        }

        binding.lockscreenWeatherTextSize.sliderValue = getInt(WEATHER_TEXT_SIZE, 16)
        binding.lockscreenWeatherTextSize.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            putInt(WEATHER_TEXT_SIZE, value.toInt())
        }

        binding.lockscreenWeatherImageSize.sliderValue = getInt(WEATHER_ICON_SIZE, 18)
        binding.lockscreenWeatherImageSize.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            putInt(WEATHER_ICON_SIZE, value.toInt())
        }

        binding.lockscreenWeatherCustomColorSwitch.isSwitchChecked =
            getBoolean(WEATHER_TEXT_COLOR_SWITCH, false)
        binding.lockscreenWeatherCustomColorSwitch.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(WEATHER_TEXT_COLOR_SWITCH, isChecked)
            binding.lockscreenWeatherCustomColor.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }

        binding.lockscreenWeatherCustomColor.previewColor = getInt(WEATHER_TEXT_COLOR, Color.WHITE)
        binding.lockscreenWeatherCustomColor.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(WEATHER_TEXT_COLOR, Color.WHITE),
            showPresets = true,
            showAlphaSlider = false,
            showColorShades = true
        )
        binding.lockscreenWeatherCustomColor.setOnColorSelectedListener { color: Int ->
            putInt(WEATHER_TEXT_COLOR, color)
        }

        binding.lockscreenWeatherCustomLocation.isSwitchChecked =
            getBoolean(WEATHER_CUSTOM_LOCATION, false)
        binding.lockscreenWeatherCustomLocation.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(WEATHER_CUSTOM_LOCATION, isChecked)
            binding.lockscreenWeatherCustomLocationMenu.isEnabled = isChecked
            handlePermissions()
            forceRefreshWeatherSettings()
        }

        val locationName = WeatherConfig.getLocationName(requireContext())
        if (locationName.isNullOrEmpty()) {
            binding.lockscreenWeatherCustomLocationMenu.setSummary(R.string.not_available)
        } else {
            binding.lockscreenWeatherCustomLocationMenu.setSummary(locationName)
        }
        binding.lockscreenWeatherCustomLocationMenu.setOnClickListener {
            mCustomLocationLauncher.launch(
                Intent(
                    requireContext(),
                    LocationBrowseActivity::class.java
                )
            )
        }

        val entries: MutableList<String?> = ArrayList()
        val values: MutableList<String?> = ArrayList()
        val drawables: MutableList<Drawable?> = ArrayList()
        getAvailableWeatherIconPacks(entries, values, drawables)
        val entriesChar: Array<CharSequence> = entries.filterNotNull().toTypedArray()
        val valuesChar: Array<CharSequence> = values.filterNotNull().toTypedArray()
        val currentIconPack = if (TextUtils.isEmpty(WeatherConfig.getIconPack(requireContext()))) {
            valuesChar[0].toString()
        } else {
            WeatherConfig.getIconPack(requireContext())
        }
        val mAdapter = IconsAdapter(
            entriesChar,
            valuesChar,
            currentIconPack!!,
            WEATHER_ICONS_ADAPTER,
            object : IconsAdapter.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    val value = values[position]
                    putString(WEATHER_ICON_PACK, value!!)
                    binding.lockscreenWeatherIconPack.setSummary(entries[position]!!)
                    forceRefreshWeatherSettings()
                }
            }
        )
        mAdapter.setDrawables(drawables.filterNotNull().toTypedArray())
        val summary = if (!TextUtils.isEmpty(currentIconPack) && values.contains(currentIconPack)) {
            entries[values.indexOf(currentIconPack)]
        } else {
            entries[0]
        }
        binding.lockscreenWeatherIconPack.setSummary(summary!!)
        binding.lockscreenWeatherIconPack.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.lockscreenWeatherIconPack.setAdapter(mAdapter)

        binding.lockscreenWeatherMarginTop.sliderValue = getInt(WEATHER_CUSTOM_MARGINS_TOP, 20)
        binding.lockscreenWeatherMarginTop.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            putInt(WEATHER_CUSTOM_MARGINS_TOP, value.toInt())
        }

        binding.lockscreenWeatherMarginSide.sliderValue = getInt(WEATHER_CUSTOM_MARGINS_SIDE, 20)
        binding.lockscreenWeatherMarginSide.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            putInt(WEATHER_CUSTOM_MARGINS_SIDE, value.toInt())
        }

        binding.lockscreenWeatherMarginBottom.sliderValue =
            getInt(WEATHER_CUSTOM_MARGINS_BOTTOM, 20)
        binding.lockscreenWeatherMarginBottom.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            putInt(WEATHER_CUSTOM_MARGINS_BOTTOM, value.toInt())
        }

        binding.lockscreenWeatherBg.setSelectedIndex(getInt(WEATHER_STYLE, 0))
        binding.lockscreenWeatherBg.setOnItemSelectedListener {
            putInt(WEATHER_STYLE, it)
        }

        binding.lockscreenWeatherCenterView.isSwitchChecked = getBoolean(WEATHER_CENTER_VIEW, false)
        binding.lockscreenWeatherCenterView.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(WEATHER_CENTER_VIEW, isChecked)
        }

        if (getBoolean(WEATHER_SWITCH, false)) {
            queryAndUpdateWeather()
        }

        updateUI()
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
        if (getBoolean(WEATHER_SWITCH, false) &&
            !getBoolean(WEATHER_CUSTOM_LOCATION, false)
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

    private fun checkLocationPermission(force: Boolean) {
        if (!hasPermissions()) {
            if (getBoolean(WEATHER_SWITCH, false) &&
                !getBoolean(WEATHER_CUSTOM_LOCATION, false)
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
                binding.lockscreenWeatherLastUpdate.setSummary(mWeatherClient.weatherInfo!!.lastUpdateTime)
            }
        }
    }

    override fun weatherUpdated() {
        queryAndUpdateWeather()
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
            binding.lockscreenWeatherLastUpdate.setSummary(errorString)
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

    private fun updateUI() {
        val enabled = getBoolean(WEATHER_SWITCH, false)

        for (i in 0 until binding.lockscreenWeatherContainer.childCount) {
            val child = binding.lockscreenWeatherContainer.getChildAt(i)
            if (child != binding.enableLockscreenWeather) child.isEnabled = enabled
        }

        binding.lockscreenWeatherCustomColor.visibility =
            if (getBoolean(WEATHER_TEXT_COLOR_SWITCH, false)) View.VISIBLE else View.GONE
        binding.lockscreenWeatherCustomLocationMenu.isEnabled =
            getBoolean(WEATHER_CUSTOM_LOCATION, false)
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
                    binding.lockscreenWeatherCustomLocationMenu.setSummary(R.string.not_available)
                } else {
                    binding.lockscreenWeatherCustomLocationMenu.setSummary(locationName)
                }

                if (getBoolean(WEATHER_SWITCH, false)
                    && !getBoolean(WEATHER_CUSTOM_LOCATION, false)
                ) {
                    checkLocationEnabled(true)
                }
            }
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

    companion object {
        private const val DEFAULT_WEATHER_ICON_PACKAGE: String =
            "${BuildConfig.APPLICATION_ID}.google"
    }
}