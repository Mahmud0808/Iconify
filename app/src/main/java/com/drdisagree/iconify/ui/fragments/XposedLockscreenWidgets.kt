package com.drdisagree.iconify.ui.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_BIG_ACTIVE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_BIG_ICON_ACTIVE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_BIG_ICON_INACTIVE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_BIG_INACTIVE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_BOTTOM_MARGIN
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_CUSTOM_COLOR
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_ENABLED
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_EXTRAS
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_SMALL_ACTIVE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_SMALL_INACTIVE
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.config.RPrefs.putString
import com.drdisagree.iconify.databinding.FragmentXposedLockscreenWidgetsBinding
import com.drdisagree.iconify.services.WeatherScheduler
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.replaceFragment
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.EditTextDialog
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.OmniJawsClient
import com.drdisagree.iconify.utils.SystemUtil
import com.drdisagree.iconify.utils.weather.WeatherConfig
import com.google.android.material.slider.Slider

class XposedLockscreenWidgets : BaseFragment() {

    private lateinit var binding: FragmentXposedLockscreenWidgetsBinding
    private lateinit var mWeatherClient: OmniJawsClient

    private var bigWidget1 = 0
    private var bigWidget2 = 0
    private var miniWidget1 = 0
    private var miniWidget2 = 0
    private var miniWidget3 = 0
    private var miniWidget4 = 0

    private var mWidgetsValues: Array<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentXposedLockscreenWidgetsBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        mWeatherClient = OmniJawsClient(requireContext())
        mWeatherClient.queryWeather()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_lockscreen_widgets
        )

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readPrefs()
        updateUI()

        // Main Switch
        binding.lockscreenWidgetsSwitch.isSwitchChecked =
            getBoolean(LOCKSCREEN_WIDGETS_ENABLED, false)
        binding.lockscreenWidgetsSwitch.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->

            putBoolean(LOCKSCREEN_WIDGETS_ENABLED, isChecked)

            updateUI()

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Device Widget
        binding.deviceWidget.isSwitchChecked = getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET, false)
        binding.deviceWidget.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET, isChecked)
            updateUI()
        }

        binding.deviceWidgetCustomColor.isSwitchChecked =
            getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH, false)
        binding.deviceWidgetCustomColor.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH, isChecked)
            updateUI()
        }

        binding.widgetDeviceName.setEditTextValue(
            RPrefs.getString(
                LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE, Build.MODEL
            )!!
        )
        binding.widgetDeviceName.setOnEditTextListener(object :
            EditTextDialog.EditTextDialogListener {
            override fun onOkPressed(dialogId: Int, newText: String) {
                putString(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE, newText)
                binding.widgetDeviceName.setEditTextValue(newText)
            }
        })

        binding.deviceWidgetLinearProgressColor.previewColor =
            getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR, Color.WHITE)
        binding.deviceWidgetLinearProgressColor.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR, Color.WHITE),
            showPresets = true,
            showAlphaSlider = false,
            showColorShades = true
        )
        binding.deviceWidgetLinearProgressColor.setOnColorSelectedListener { color: Int ->
            putInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR, color)
        }

        binding.deviceWidgetCircularProgressColor.previewColor =
            getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR, Color.WHITE)
        binding.deviceWidgetCircularProgressColor.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR, Color.WHITE),
            showPresets = true,
            showAlphaSlider = false,
            showColorShades = true
        )
        binding.deviceWidgetCircularProgressColor.setOnColorSelectedListener { color: Int ->
            putInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR, color)
        }

        binding.deviceWidgetTextColor.previewColor = getInt(
            LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR, Color.WHITE
        )
        binding.deviceWidgetTextColor.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR, Color.WHITE),
            showPresets = true,
            showAlphaSlider = false,
            showColorShades = true
        )
        binding.deviceWidgetTextColor.setOnColorSelectedListener { color: Int ->
            putInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR, color)
        }

        // Big Widget 1
        binding.bigWidget1.setSelectedIndex(bigWidget1)
        binding.bigWidget1.setOnItemSelectedListener { index: Int ->
            bigWidget1 = index
            updatePrefs()
        }

        // Big Widget 2
        binding.bigWidget2.setSelectedIndex(bigWidget2)
        binding.bigWidget2.setOnItemSelectedListener { index: Int ->
            bigWidget2 = index
            updatePrefs()
        }

        // Mini Widget 1
        binding.miniWidget1.setSelectedIndex(miniWidget1)
        binding.miniWidget1.setOnItemSelectedListener { index: Int ->
            miniWidget1 = index
            updatePrefs()
        }

        // Mini Widget 2
        binding.miniWidget2.setSelectedIndex(miniWidget2)
        binding.miniWidget2.setOnItemSelectedListener { index: Int ->
            miniWidget2 = index
            updatePrefs()
        }

        // Mini Widget 3
        binding.miniWidget3.setSelectedIndex(miniWidget3)
        binding.miniWidget3.setOnItemSelectedListener { index: Int ->
            miniWidget3 = index
            updatePrefs()
        }

        // Mini Widget 4
        binding.miniWidget4.setSelectedIndex(miniWidget4)
        binding.miniWidget4.setOnItemSelectedListener { index: Int ->
            miniWidget4 = index
            updatePrefs()
        }

        // Weather Settings
        binding.weatherSettings.setOnClickListener {
            replaceFragment(WeatherSettings())
        }

        // Widgets Colors
        binding.widgetsCustomColor.isSwitchChecked =
            getBoolean(LOCKSCREEN_WIDGETS_CUSTOM_COLOR, false)
        binding.widgetsCustomColor.setSwitchChangeListener { _, isChecked ->
            putBoolean(LOCKSCREEN_WIDGETS_CUSTOM_COLOR, isChecked)
            updateUI()
        }

        val widgetsColors = arrayOf(
            binding.widgetsBigActive,
            binding.widgetsBigInactive,
            binding.widgetsBigIconActive,
            binding.widgetsBigIconInactive,
            binding.widgetsSmallActive,
            binding.widgetsSmallInactive,
            binding.widgetsSmallIconActive,
            binding.widgetsSmallIconInactive
        )
        val widgetsColorPrefs = arrayOf(
            LOCKSCREEN_WIDGETS_BIG_ACTIVE,
            LOCKSCREEN_WIDGETS_BIG_INACTIVE,
            LOCKSCREEN_WIDGETS_BIG_ICON_ACTIVE,
            LOCKSCREEN_WIDGETS_BIG_ICON_INACTIVE,
            LOCKSCREEN_WIDGETS_SMALL_ACTIVE,
            LOCKSCREEN_WIDGETS_SMALL_INACTIVE,
            LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE,
            LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE
        )
        val widgetsColorDefaultPrefs = arrayOf(
            Color.BLACK,
            Color.WHITE,
            Color.BLACK,
            Color.WHITE,
            Color.WHITE,
            Color.BLACK,
            Color.WHITE,
            Color.BLACK
        )

        if (widgetsColors.size == widgetsColorPrefs.size) {
            for (i in widgetsColors.indices) {
                widgetsColors[i].previewColor =
                    getInt(widgetsColorPrefs[i], widgetsColorDefaultPrefs[i])
                widgetsColors[i].setColorPickerListener(
                    activity = requireActivity(),
                    defaultColor = getInt(widgetsColorPrefs[i], widgetsColorDefaultPrefs[i]),
                    showPresets = true,
                    showAlphaSlider = true,
                    showColorShades = true
                )
                widgetsColors[i].setOnColorSelectedListener { color: Int ->
                    putInt(widgetsColorPrefs[i], color)
                }
            }
        }

        binding.widgetsBottomMargin.sliderValue = getInt(LOCKSCREEN_WIDGETS_BOTTOM_MARGIN, 0)
        binding.widgetsBottomMargin.setOnSliderChangeListener { slider: Slider, _: Float, _: Boolean ->
            putInt(
                LOCKSCREEN_WIDGETS_BOTTOM_MARGIN,
                slider.value.toInt()
            )
        }

    }

    private fun updateUI() {
        val widgetsEnabled = getBoolean(LOCKSCREEN_WIDGETS_ENABLED, false)
        val deviceWidgetEnabled = getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET, false)
        val deviceWidgetCustomColor = getBoolean(
            LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH, false
        )
        val widgetsCustomColor = getBoolean(LOCKSCREEN_WIDGETS_CUSTOM_COLOR, false)
        val mainWidgets: String? = RPrefs.getString(LOCKSCREEN_WIDGETS, "")
        val extraWidgets: String? = RPrefs.getString(LOCKSCREEN_WIDGETS_EXTRAS, "")

        // Device Widget Prefs
        binding.deviceWidgetCustomColor.isEnabled = widgetsEnabled && deviceWidgetEnabled
        binding.deviceWidgetTextColor.isEnabled = widgetsEnabled && deviceWidgetEnabled
        binding.widgetDeviceName.isEnabled = widgetsEnabled && deviceWidgetEnabled
        binding.deviceWidgetLinearProgressColor.visibility =
            if (widgetsEnabled && deviceWidgetEnabled && deviceWidgetCustomColor) View.VISIBLE else View.GONE
        binding.deviceWidgetCircularProgressColor.visibility =
            if (widgetsEnabled && deviceWidgetEnabled && deviceWidgetCustomColor) View.VISIBLE else View.GONE

        binding.deviceWidget.isEnabled = widgetsEnabled
        binding.bigWidget1.isEnabled = widgetsEnabled
        binding.bigWidget2.isEnabled = widgetsEnabled
        binding.miniWidget1.isEnabled = widgetsEnabled
        binding.miniWidget2.isEnabled = widgetsEnabled
        binding.miniWidget3.isEnabled = widgetsEnabled
        binding.miniWidget4.isEnabled = widgetsEnabled

        // Visibility of color picker based on switch
        binding.widgetsBigActive.visibility = if (widgetsCustomColor) View.VISIBLE else View.GONE
        binding.widgetsBigInactive.visibility = if (widgetsCustomColor) View.VISIBLE else View.GONE
        binding.widgetsBigIconActive.visibility =
            if (widgetsCustomColor) View.VISIBLE else View.GONE
        binding.widgetsBigIconInactive.visibility =
            if (widgetsCustomColor) View.VISIBLE else View.GONE
        binding.widgetsSmallActive.visibility = if (widgetsCustomColor) View.VISIBLE else View.GONE
        binding.widgetsSmallInactive.visibility =
            if (widgetsCustomColor) View.VISIBLE else View.GONE
        binding.widgetsSmallIconActive.visibility =
            if (widgetsCustomColor) View.VISIBLE else View.GONE
        binding.widgetsSmallIconInactive.visibility =
            if (widgetsCustomColor) View.VISIBLE else View.GONE

        // Switch enabled based on main switch
        binding.widgetsCustomColor.isEnabled = widgetsEnabled
        binding.widgetsBigActive.isEnabled = widgetsEnabled && widgetsCustomColor
        binding.widgetsBigInactive.isEnabled = widgetsEnabled && widgetsCustomColor
        binding.widgetsBigIconActive.isEnabled = widgetsEnabled && widgetsCustomColor
        binding.widgetsBigIconInactive.isEnabled = widgetsEnabled && widgetsCustomColor
        binding.widgetsSmallActive.isEnabled = widgetsEnabled && widgetsCustomColor
        binding.widgetsSmallInactive.isEnabled = widgetsEnabled && widgetsCustomColor
        binding.widgetsSmallIconActive.isEnabled = widgetsEnabled && widgetsCustomColor
        binding.widgetsSmallIconInactive.isEnabled = widgetsEnabled && widgetsCustomColor

        binding.weatherSettings.isEnabled =
            mainWidgets!!.contains("weather") || extraWidgets!!.contains("weather")

        binding.widgetsBottomMargin.isEnabled = widgetsEnabled

    }

    private fun readPrefs() {
        val mainWidgets: String? = RPrefs.getString(LOCKSCREEN_WIDGETS, "")
        val extraWidgets: String? = RPrefs.getString(LOCKSCREEN_WIDGETS_EXTRAS, "")

        mWidgetsValues = resources.getStringArray(R.array.lockscreen_widgets_values)

        fun assignWidgets(widgets: Array<String>, indices: MutableList<Int>) {
            for (i in widgets.indices) {
                indices[i] = getWidgetIndex(mWidgetsValues!!, widgets[i])
            }
        }

        val mainWi = mainWidgets?.split(",")?.toTypedArray() ?: emptyArray()
        val bigWidgets = mutableListOf(0, 0)
        assignWidgets(mainWi, bigWidgets)
        bigWidget1 = bigWidgets[0]
        bigWidget2 = bigWidgets[1]

        val extraWi = extraWidgets?.split(",")?.toTypedArray() ?: emptyArray()
        val miniWidgets = mutableListOf(0, 0, 0, 0)
        assignWidgets(extraWi, miniWidgets)
        miniWidget1 = miniWidgets[0]
        miniWidget2 = miniWidgets[1]
        miniWidget3 = miniWidgets[2]
        miniWidget4 = miniWidgets[3]

    }

    private fun getWidgetIndex(values: Array<String>, widget: String): Int {
        val mainIndex = values.indexOf(widget)
        return if (mainIndex != -1) {
            mainIndex
        } else {
            val extraIndex = values.indexOf(widget)
            if (extraIndex != -1) values.size + extraIndex else 0
        }
    }

    private fun updatePrefs() {
        mWeatherClient.queryWeather()

        val mainWidgets = "${mWidgetsValues!![bigWidget1]},${mWidgetsValues!![bigWidget2]}"
        val extraWidgets =
            "${mWidgetsValues!![miniWidget1]},${mWidgetsValues!![miniWidget2]},${mWidgetsValues!![miniWidget3]},${mWidgetsValues!![miniWidget4]}"

        Log.d("LockscreenWidgets", "Main: $mainWidgets")
        Log.d("LockscreenWidgets", "Extra: $extraWidgets")

        val wasWeatherEnabled: Boolean = WeatherConfig.isEnabled(requireContext())

        putString(LOCKSCREEN_WIDGETS, mainWidgets)
        putString(LOCKSCREEN_WIDGETS_EXTRAS, extraWidgets)

        val weatherEnabled =
            mainWidgets.contains("weather") || extraWidgets.contains("weather")

        binding.weatherSettings.isEnabled = weatherEnabled

        if (weatherEnabled && wasWeatherEnabled && mWeatherClient.weatherInfo != null) {
            // Weather enabled but updater more than 1h ago
            if (System.currentTimeMillis() - mWeatherClient.weatherInfo?.timeStamp!! > 3600000) {
                WeatherScheduler.scheduleUpdateNow(requireContext())
            }
        } else if (weatherEnabled) {
            // Weather not enabled (LS Weather) so we will update now
            WeatherScheduler.scheduleUpdates(requireContext())
            WeatherScheduler.scheduleUpdateNow(requireContext())
        }
    }

}