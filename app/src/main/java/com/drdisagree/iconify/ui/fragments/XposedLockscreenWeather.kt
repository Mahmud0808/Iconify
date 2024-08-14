package com.drdisagree.iconify.ui.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
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
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_LOCATION
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_MARGINS_BOTTOM
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_MARGINS_LEFT
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_MARGINS_TOP
import com.drdisagree.iconify.common.Preferences.WEATHER_ICON_PACK
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
import com.drdisagree.iconify.ui.activities.LocationBrowseActivity
import com.drdisagree.iconify.ui.activities.LocationBrowseActivity.Companion.DATA_LOCATION_LAT
import com.drdisagree.iconify.ui.activities.LocationBrowseActivity.Companion.DATA_LOCATION_LON
import com.drdisagree.iconify.ui.activities.LocationBrowseActivity.Companion.DATA_LOCATION_NAME
import com.drdisagree.iconify.ui.adapters.IconsAdapter
import com.drdisagree.iconify.ui.adapters.IconsAdapter.Companion.WEATHER_ICONS_ADAPTER
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.OmniJawsClient
import com.drdisagree.iconify.utils.SystemUtil
import com.drdisagree.iconify.weather.AbstractWeatherProvider.Companion.PART_COORDINATES
import com.drdisagree.iconify.weather.Config
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import java.util.Locale

class XposedLockscreenWeather:BaseFragment(),
    OmniJawsClient.OmniJawsObserver {

    private val DEFAULT_WEATHER_ICON_PACKAGE: String = BuildConfig.APPLICATION_ID + ".google"
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

        mWeatherClient = OmniJawsClient(requireContext())

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


            if (!hasPermissions()) {
                showPermissionDialog()
            } else {
                checkLocationPermissions(false)
            }
            updateUI()

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
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

        binding.lockscreenWeatherCustomLocation.isSwitchChecked = getBoolean(WEATHER_CUSTOM_LOCATION, false)
        binding.lockscreenWeatherCustomLocation.setSwitchChangeListener{ _: CompoundButton?, isChecked: Boolean ->
            putBoolean(WEATHER_CUSTOM_LOCATION, isChecked)
            binding.lockscreenWeatherCustomLocationMenu.isEnabled = isChecked
        }

        binding.lockscreenWeatherCustomLocationMenu.setSummary(Config.getLocationName(requireContext()))
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
        val currentIconPack = if (TextUtils.isEmpty(Config.getIconPack(requireContext()))) valuesChar[0].toString() else Config.getIconPack(requireContext())
        val mAdapter =
            IconsAdapter(
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
                })
        mAdapter.setDrawables(drawables.filterNotNull().toTypedArray())
        val summary = if (!TextUtils.isEmpty(currentIconPack) && values.contains(currentIconPack)) entries[values.indexOf(currentIconPack)] else entries[0]
        binding.lockscreenWeatherIconPack.setSummary(summary!!)
        binding.lockscreenWeatherIconPack.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.lockscreenWeatherIconPack.setAdapter(mAdapter)

        binding.lockscreenWeatherMarginTop.sliderValue = getInt(WEATHER_CUSTOM_MARGINS_TOP, 20)
        binding.lockscreenWeatherMarginTop.setOnSliderChangeListener{ _: Slider?, value: Float, _: Boolean ->
            putInt(WEATHER_CUSTOM_MARGINS_TOP, value.toInt())
        }

        binding.lockscreenWeatherMarginLeft.sliderValue = getInt(WEATHER_CUSTOM_MARGINS_LEFT, 20)
        binding.lockscreenWeatherMarginLeft.setOnSliderChangeListener{ _: Slider?, value: Float, _: Boolean ->
            putInt(WEATHER_CUSTOM_MARGINS_LEFT, value.toInt())
        }

        binding.lockscreenWeatherMarginBottom.sliderValue = getInt(WEATHER_CUSTOM_MARGINS_BOTTOM, 20)
        binding.lockscreenWeatherMarginBottom.setOnSliderChangeListener{ _: Slider?, value: Float, _: Boolean ->
            putInt(WEATHER_CUSTOM_MARGINS_BOTTOM, value.toInt())
        }

        binding.lockscreenWeatherBg.setSelectedIndex(getInt(WEATHER_STYLE, 0))
        binding.lockscreenWeatherBg.setOnItemSelectedListener{
            putInt(WEATHER_STYLE, it)
        }

        if (!getBoolean(WEATHER_CUSTOM_LOCATION, false)) {
            checkLocationEnabledInitial()
        } else {
            forceRefreshWeatherSettings()
        }

        updateUI()
    }

    override fun onResume() {
        super.onResume()

        mWeatherClient.addObserver(this)

        if (getBoolean(WEATHER_SWITCH, false)
            && !getBoolean(WEATHER_CUSTOM_LOCATION, false)
        ) {
            checkLocationEnabled()
        }
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
        if (mWeatherClient.weatherInfo != null) {
            requireActivity().runOnUiThread {
                binding.lockscreenWeatherLastUpdate.setSummary(mWeatherClient.weatherInfo!!.lastUpdateTime)
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
            var label: String? = r.activityInfo.loadLabel(packageManager).toString()
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

    private fun updateUI() {

        val enabled = getBoolean(WEATHER_SWITCH, false)

        for (i in 0 until binding.lockscreenWeatherContainer.childCount) {
            val child = binding.lockscreenWeatherContainer.getChildAt(i)
            if (child != binding.enableLockscreenWeather) child.isEnabled = enabled
        }

        binding.lockscreenWeatherCustomColor.visibility = if (getBoolean(WEATHER_TEXT_COLOR_SWITCH, false)) View.VISIBLE else View.GONE
        binding.lockscreenWeatherCustomLocationMenu.isEnabled = getBoolean(WEATHER_CUSTOM_LOCATION, false)

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
                Config.setLocationId(requireContext(), lat.toString(), lon.toString())
                Config.setLocationName(requireContext(), locationName)
                binding.lockscreenWeatherCustomLocationMenu.setSummary(locationName)
                if (getBoolean(WEATHER_SWITCH, false)
                    && !getBoolean(WEATHER_CUSTOM_LOCATION, false)
                ) {
                    checkLocationEnabled()
                }
                forceRefreshWeatherSettings()
            }
        }
    }

}