package com.drdisagree.iconify.ui.fragments.tweaks

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY
import com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY_LIGHT
import com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY
import com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY_LIGHT
import com.drdisagree.iconify.common.Preferences.CUSTOM_ACCENT
import com.drdisagree.iconify.common.Preferences.CUSTOM_PRIMARY_COLOR_SWITCH
import com.drdisagree.iconify.common.Preferences.CUSTOM_SECONDARY_COLOR_SWITCH
import com.drdisagree.iconify.common.Preferences.MONET_ACCURATE_SHADES
import com.drdisagree.iconify.common.Preferences.MONET_BACKGROUND_LIGHTNESS
import com.drdisagree.iconify.common.Preferences.MONET_BACKGROUND_SATURATION
import com.drdisagree.iconify.common.Preferences.MONET_ENGINE_SWITCH
import com.drdisagree.iconify.common.Preferences.MONET_PRIMARY_ACCENT_SATURATION
import com.drdisagree.iconify.common.Preferences.MONET_PRIMARY_COLOR
import com.drdisagree.iconify.common.Preferences.MONET_SECONDARY_ACCENT_SATURATION
import com.drdisagree.iconify.common.Preferences.MONET_SECONDARY_COLOR
import com.drdisagree.iconify.common.Preferences.MONET_STYLE
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.config.RPrefs.clearPrefs
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.getString
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.config.RPrefs.putString
import com.drdisagree.iconify.databinding.FragmentMonetEngineBinding
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.events.ColorSelectedEvent
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtils
import com.drdisagree.iconify.utils.SystemUtils.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.requestStoragePermission
import com.drdisagree.iconify.utils.color.ColorSchemeUtils.generateColorPalette
import com.drdisagree.iconify.utils.color.ColorUtils.colorNames
import com.drdisagree.iconify.utils.color.ColorUtils.getSystemColors
import com.drdisagree.iconify.utils.color.ColorUtils.setLightness
import com.drdisagree.iconify.utils.color.ColorUtils.setSaturation
import com.drdisagree.iconify.utils.helper.ImportExport.exportSettings
import com.drdisagree.iconify.utils.overlay.FabricatedUtils.disableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtils
import com.drdisagree.iconify.utils.overlay.OverlayUtils.changeOverlayState
import com.drdisagree.iconify.utils.overlay.manager.MonetEngineManager.buildOverlay
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.util.Objects
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

@Suppress("deprecation")

class MonetEngine : BaseFragment() {

    private lateinit var binding: FragmentMonetEngineBinding
    private lateinit var colorTableRows: Array<LinearLayout>

    private var isDarkMode = SystemUtils.isDarkMode
    private var showApplyButton = false
    private var showDisableButton = false

    private val finalPalette: MutableList<List<MutableList<Any>>> = ArrayList()
    private val selectedChild = IntArray(2)

    var monetPrimaryAccentSaturation = intArrayOf(getInt(MONET_PRIMARY_ACCENT_SATURATION))
    var monetSecondaryAccentSaturation = intArrayOf(getInt(MONET_SECONDARY_ACCENT_SATURATION))
    var monetBackgroundSaturation = intArrayOf(getInt(MONET_BACKGROUND_SATURATION))
    var monetBackgroundLightness = intArrayOf(getInt(MONET_BACKGROUND_LIGHTNESS))

    private var startExportActivityIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result1: ActivityResult ->
        if (result1.resultCode == Activity.RESULT_OK) {
            val data = result1.data ?: return@registerForActivityResult

            try {
                exportSettings(
                    RPrefs.getPrefs,
                    requireContext().contentResolver.openOutputStream(data.data!!)!!
                )

                Toast.makeText(
                    appContext,
                    requireContext().resources.getString(R.string.toast_export_settings_successfull),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (exception: Exception) {
                Toast.makeText(
                    appContext,
                    requireContext().resources.getString(R.string.toast_error),
                    Toast.LENGTH_SHORT
                ).show()

                Log.e("MonetEngine", "Error exporting settings", exception)
            }
        }
    }

    private var startImportActivityIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result2: ActivityResult ->
        if (result2.resultCode == Activity.RESULT_OK) {
            val data = result2.data ?: return@registerForActivityResult

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(requireContext().resources.getString(R.string.import_settings_confirmation_title))
                .setMessage(requireContext().resources.getString(R.string.import_settings_confirmation_desc))
                .setPositiveButton(requireContext().resources.getString(R.string.btn_positive)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()

                    Handler(Looper.getMainLooper()).post {
                        try {
                            val success = importMonetSettings(
                                RPrefs.getPrefs,
                                requireContext().contentResolver.openInputStream(data.data!!)!!
                            )

                            if (success) {
                                Toast.makeText(
                                    appContext,
                                    requireContext().resources.getString(R.string.toast_import_settings_successfull),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    appContext,
                                    requireContext().resources.getString(R.string.toast_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (exception: Exception) {
                            Toast.makeText(
                                appContext,
                                requireContext().resources.getString(R.string.toast_error),
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.e("MonetEngine", "Error importing settings", exception)
                        }
                    }
                }
                .setNegativeButton(requireContext().resources.getString(R.string.btn_negative)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMonetEngineBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_monet_engine
        )
        setHasOptionsMenu(true)

        colorTableRows = arrayOf(
            binding.monetEngine.systemAccent1,
            binding.monetEngine.systemAccent2,
            binding.monetEngine.systemAccent3,
            binding.monetEngine.systemNeutral1,
            binding.monetEngine.systemNeutral2
        )

        isDarkMode = SystemUtils.isDarkMode
        selectedStyle = getString(
            MONET_STYLE,
            appContextLocale.resources.getString(R.string.monet_tonalspot)
        )

        // Monet Style
        val selectedIndex = listOf(*resources.getStringArray(R.array.monet_style))
            .indexOf(selectedStyle)

        binding.monetStyles.setSelectedIndex(selectedIndex)
        binding.monetStyles.setOnItemSelectedListener { index: Int ->
            selectedStyle = listOf(*resources.getStringArray(R.array.monet_style))[index]
            assignCustomColorsToPalette()
            binding.floatingActionMenu.show()
            showApplyButton = true
        }

        accentPrimary = getString(
            MONET_PRIMARY_COLOR,
            resources.getColor(
                if (isDarkMode) android.R.color.system_accent1_300 else android.R.color.system_accent1_600,
                appContext.theme
            ).toString()
        )

        accentSecondary = getString(
            MONET_SECONDARY_COLOR,
            resources.getColor(
                if (isDarkMode) android.R.color.system_accent3_300 else android.R.color.system_accent3_600,
                appContext.theme
            ).toString()
        )

        assignStockColorsToPalette()

        binding.colorAccentPrimary.setColorPickerListener(
            activity = requireActivity(), defaultColor = accentPrimary!!.toInt(),
            showPresets = true,
            showAlphaSlider = false,
            showColorShades = true
        )

        binding.colorAccentPrimary.setBeforeColorPickerListener {
            binding.enableCustomMonet.hide()
            binding.disableCustomMonet.hide()
        }

        binding.colorAccentPrimary.setOnColorSelectedListener { color: Int ->
            isSelectedPrimary = true
            accentPrimary = color.toString()
            binding.floatingActionMenu.show()
            showApplyButton = true
            assignCustomColorsToPalette()
        }

        binding.colorAccentSecondary.setColorPickerListener(
            activity = requireActivity(), defaultColor = accentSecondary!!.toInt(),
            showPresets = true,
            showAlphaSlider = false,
            showColorShades = true
        )

        binding.colorAccentSecondary.setBeforeColorPickerListener {
            binding.enableCustomMonet.hide()
            binding.disableCustomMonet.hide()
        }

        binding.colorAccentSecondary.setOnColorSelectedListener { color: Int ->
            isSelectedSecondary = true
            accentSecondary = color.toString()
            binding.floatingActionMenu.show()
            showApplyButton = true
            assignCustomColorsToPalette()
        }

        // Monet Accurate Shades
        binding.accurateShades.isSwitchChecked = getBoolean(MONET_ACCURATE_SHADES, true)
        binding.accurateShades.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            accurateShades = isChecked
            assignCustomColorsToPalette()
            binding.floatingActionMenu.show()
            binding.enableCustomMonet.hide()
            binding.disableCustomMonet.hide()
            showApplyButton = true
        }

        // Monet primary accent saturation
        binding.primaryAccentSaturation.sliderValue = getInt(MONET_PRIMARY_ACCENT_SATURATION)
        binding.primaryAccentSaturation.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            monetPrimaryAccentSaturation[0] = value.toInt()
            assignCustomColorsToPalette()
        }
        binding.primaryAccentSaturation.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                binding.enableCustomMonet.hide()
                binding.disableCustomMonet.hide()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                monetPrimaryAccentSaturation[0] = slider.value.toInt()
                binding.floatingActionMenu.show()
                showApplyButton = true
            }
        })

        // Long Click Reset
        binding.primaryAccentSaturation.setResetClickListener {
            monetPrimaryAccentSaturation[0] = 0
            assignCustomColorsToPalette()
            binding.floatingActionMenu.show()
            showApplyButton = true
            true
        }

        // Monet secondary accent saturation
        binding.secondaryAccentSaturation.sliderValue = getInt(MONET_SECONDARY_ACCENT_SATURATION)
        binding.secondaryAccentSaturation.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            monetSecondaryAccentSaturation[0] = value.toInt()
            assignCustomColorsToPalette()
        }
        binding.secondaryAccentSaturation.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                binding.enableCustomMonet.hide()
                binding.disableCustomMonet.hide()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                monetSecondaryAccentSaturation[0] = slider.value.toInt()
                binding.floatingActionMenu.show()
                showApplyButton = true
            }
        })

        // Long Click Reset
        binding.secondaryAccentSaturation.setResetClickListener {
            monetSecondaryAccentSaturation[0] = 0
            assignCustomColorsToPalette()
            binding.floatingActionMenu.show()
            showApplyButton = true
            true
        }

        // Monet background saturation
        binding.backgroundSaturation.sliderValue = getInt(MONET_BACKGROUND_SATURATION)
        binding.backgroundSaturation.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            monetBackgroundSaturation[0] = value.toInt()
            assignCustomColorsToPalette()
        }
        binding.backgroundSaturation.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                binding.enableCustomMonet.hide()
                binding.disableCustomMonet.hide()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                monetBackgroundSaturation[0] = slider.value.toInt()
                binding.floatingActionMenu.show()
                showApplyButton = true
            }
        })

        // Reset button
        binding.backgroundSaturation.setResetClickListener {
            monetBackgroundSaturation[0] = 0
            assignCustomColorsToPalette()
            binding.floatingActionMenu.show()
            binding.enableCustomMonet.hide()
            binding.disableCustomMonet.hide()
            showApplyButton = true
            true
        }

        // Monet background lightness
        binding.backgroundLightness.sliderValue = getInt(MONET_BACKGROUND_LIGHTNESS)
        binding.backgroundLightness.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            monetBackgroundLightness[0] = value.toInt()
            assignCustomColorsToPalette()
        }
        binding.backgroundLightness.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                binding.enableCustomMonet.hide()
                binding.disableCustomMonet.hide()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                monetBackgroundLightness[0] = slider.value.toInt()
                binding.floatingActionMenu.show()
                showApplyButton = true
            }
        })

        // Long Click Reset
        binding.backgroundLightness.setResetClickListener {
            monetBackgroundLightness[0] = 0
            assignCustomColorsToPalette()
            binding.floatingActionMenu.show()
            binding.enableCustomMonet.hide()
            binding.disableCustomMonet.hide()
            showApplyButton = true
            true
        }

        // Enable custom colors button
        binding.floatingActionMenu.hide()
        showApplyButton = false
        binding.enableCustomMonet.setOnClickListener {
            if (!hasStoragePermission()) {
                requestStoragePermission(requireContext())
            } else if (selectedStyle == null) {
                Toast.makeText(
                    appContext,
                    appContextLocale.resources.getString(R.string.toast_select_style),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                putBoolean(MONET_ACCURATE_SHADES, accurateShades)

                if (isSelectedPrimary) putString(MONET_PRIMARY_COLOR, accentPrimary)
                if (isSelectedSecondary) putString(MONET_SECONDARY_COLOR, accentSecondary)

                putString(MONET_STYLE, selectedStyle)
                putInt(MONET_PRIMARY_ACCENT_SATURATION, monetPrimaryAccentSaturation[0])
                putInt(MONET_SECONDARY_ACCENT_SATURATION, monetSecondaryAccentSaturation[0])
                putInt(MONET_BACKGROUND_SATURATION, monetBackgroundSaturation[0])
                putInt(MONET_BACKGROUND_LIGHTNESS, monetBackgroundLightness[0])

                disableBasicColors()

                val hasErroredOut = AtomicBoolean(false)

                Thread {
                    try {
                        if (buildOverlay(finalPalette, true)) {
                            hasErroredOut.set(true)
                        }
                    } catch (e: Exception) {
                        hasErroredOut.set(true)
                        Log.e("MonetEngine", e.toString())
                    }

                    Handler(Looper.getMainLooper()).post {
                        if (!hasErroredOut.get()) {
                            putBoolean(MONET_ENGINE_SWITCH, true)

                            if (getBoolean("IconifyComponentQSPBD.overlay")) {
                                changeOverlayState(
                                    "IconifyComponentQSPBD.overlay",
                                    false,
                                    "IconifyComponentQSPBD.overlay",
                                    true
                                )
                            } else if (getBoolean("IconifyComponentQSPBA.overlay")) {
                                changeOverlayState(
                                    "IconifyComponentQSPBA.overlay",
                                    false,
                                    "IconifyComponentQSPBA.overlay",
                                    true
                                )
                            }
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            if (!hasErroredOut.get()) {
                                Toast.makeText(
                                    appContext,
                                    appContextLocale.resources.getString(R.string.toast_applied),
                                    Toast.LENGTH_SHORT
                                ).show()

                                try {
                                    binding.floatingActionMenu.show()
                                    binding.enableCustomMonet.hide()
                                    showApplyButton = false
                                    showDisableButton = true
                                } catch (ignored: Exception) {
                                }
                            } else Toast.makeText(
                                appContext,
                                appContextLocale.resources.getString(R.string.toast_error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }, 20)
                    }
                }.start()
            }
        }

        // Disable custom colors button
        showDisableButton = if (getBoolean(MONET_ENGINE_SWITCH)) {
            binding.floatingActionMenu.show()
            true
        } else {
            binding.floatingActionMenu.hide()
            false
        }

        binding.disableCustomMonet.setOnClickListener {
            Thread {
                putBoolean(MONET_ENGINE_SWITCH, false)
                clearPrefs(MONET_PRIMARY_COLOR, MONET_SECONDARY_COLOR)

                OverlayUtils.disableOverlays(
                    "IconifyComponentDM.overlay",
                    "IconifyComponentME.overlay"
                )

                Handler(Looper.getMainLooper()).postDelayed({
                    Toast.makeText(
                        appContext,
                        appContextLocale.resources.getString(R.string.toast_disabled),
                        Toast.LENGTH_SHORT
                    ).show()

                    try {
                        if (!showApplyButton) {
                            binding.floatingActionMenu.hide()
                        }

                        binding.disableCustomMonet.hide()

                        showDisableButton = false
                        isSelectedPrimary = false
                        isSelectedSecondary = false
                    } catch (ignored: Exception) {
                    }
                }, 2000)
            }.start()
        }

        for (i in colorTableRows.indices) {
            for (j in 0 until colorTableRows[i].childCount) {
                val child = colorTableRows[i].getChildAt(j)

                child.setOnClickListener {
                    selectedChild[0] = i
                    selectedChild[1] = j

                    binding.enableCustomMonet.hide()
                    binding.disableCustomMonet.hide()

                    (requireActivity() as MainActivity).showColorPickerDialog(
                        dialogId = -1,
                        defaultColor = (if (child.tag == null) Color.WHITE else child.tag as Int),
                        showPresets = true,
                        showAlphaSlider = false,
                        showColorShades = true
                    )
                }
            }
        }

        if (!binding.floatingActionMenu.isShown()) {
            binding.enableCustomMonet.hide()
            binding.disableCustomMonet.hide()
        }

        binding.floatingActionMenu.setOnClickListener {
            if (showApplyButton && !binding.enableCustomMonet.isShown()) {
                binding.enableCustomMonet.show()
            } else {
                binding.enableCustomMonet.hide()
            }

            if (showDisableButton && !binding.disableCustomMonet.isShown()) {
                binding.disableCustomMonet.show()
            } else {
                binding.disableCustomMonet.hide()
            }
        }

        return view
    }

    @SuppressLint("SetTextI18n")
    private fun assignStockColorsToPalette() {
        val systemColors = getSystemColors(requireContext())
        val temp: MutableList<MutableList<Any>> = ArrayList()

        for (row in systemColors) {
            val temp2: MutableList<Any> = ArrayList()
            for (col in row) {
                temp2.add(col)
            }
            temp.add(temp2)
        }

        finalPalette.clear()
        finalPalette.add(temp)
        finalPalette.add(temp)

        for (i in colorTableRows.indices) {
            for (j in 0 until colorTableRows[i].childCount) {
                colorTableRows[i].getChildAt(j).background.setTint(systemColors[i][j])
                colorTableRows[i].getChildAt(j).tag = systemColors[i][j]

                val textView = TextView(requireContext())
                textView.text = colorCodes[j].toString()
                textView.rotation = 270f
                textView.setTextColor(calculateTextColor(systemColors[i][j]))
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                textView.setAlpha(0.8f)
                textView.setMaxLines(1)
                textView.setSingleLine(true)
                textView.setAutoSizeTextTypeUniformWithConfiguration(
                    1,
                    20,
                    1,
                    TypedValue.COMPLEX_UNIT_SP
                )

                (colorTableRows[i].getChildAt(j) as ViewGroup).addView(textView)
                (colorTableRows[i].getChildAt(j) as LinearLayout).gravity = Gravity.CENTER
            }
        }
    }

    private fun calculateTextColor(@ColorInt color: Int): Int {
        val darkness =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return if (darkness < 0.5) Color.BLACK else Color.WHITE
    }

    private fun assignCustomColorsToPalette() {
        val palette: List<MutableList<Any>> = generateColorPalette(
            requireContext(),
            selectedStyle,
            accentPrimary!!.toInt()
        )
        val paletteNight = cloneList(palette)

        if (selectedStyle != appContextLocale.resources.getString(R.string.monet_monochrome)) {
            // Set primary accent saturation
            for (i in 0..1) {
                for (j in palette[i].size - 2 downTo 1) {
                    val color: Int = if (j == 1) setSaturation(
                        (palette[i][j + 1] as Int).toString().toInt(),
                        -0.1f
                    ) else setSaturation(
                        (palette[i][j] as Int).toString().toInt(),
                        (monetPrimaryAccentSaturation[0].toFloat() / 1000.0f * min(
                            (3.0f - j / 5f).toDouble(),
                            3.0
                        )).toFloat()
                    )

                    palette[i][j] = color
                    paletteNight[i][j] = color

                    if (!accurateShades) {
                        if (i == 0 && j == 8) palette[i][j] = accentPrimary!!.toInt()
                        if (i == 0 && j == 5) paletteNight[i][j] = accentPrimary!!.toInt()
                    }
                }
            }

            // Set secondary accent saturation
            val i = 2
            for (j in palette[i].size - 2 downTo 1) {
                val color: Int = if (j == 1) setSaturation(
                    (palette[i][j + 1] as Int).toString().toInt(),
                    -0.1f
                ) else setSaturation(
                    (palette[i][j] as Int).toString().toInt(),
                    (monetSecondaryAccentSaturation[0].toFloat() / 1000.0f * min(
                        (3.0f - j / 5f).toDouble(),
                        3.0
                    )).toFloat()
                )

                palette[i][j] = color
                paletteNight[i][j] = color
            }
        }

        // Set background saturation
        if (selectedStyle != appContextLocale.resources.getString(R.string.monet_monochrome)) {
            for (i in 3 until palette.size) {
                for (j in palette[i].size - 2 downTo 1) {
                    val color: Int = if (j == 1) setSaturation(
                        (palette[i][j + 1] as Int).toString().toInt(),
                        -0.1f
                    ) else setSaturation(
                        (palette[i][j] as Int).toString().toInt(),
                        (monetBackgroundSaturation[0].toFloat() / 1000.0f * min(
                            (3.0f - j / 5f).toDouble(),
                            3.0
                        )).toFloat()
                    )

                    palette[i][j] = color
                    paletteNight[i][j] = color
                }
            }
        }

        // Set background lightness
        val startIdx =
            if (selectedStyle == appContextLocale.resources.getString(R.string.monet_monochrome)) {
                0
            } else {
                3
            }
        for (i in startIdx until palette.size) {
            for (j in 1 until palette[i].size - 1) {
                val color = setLightness(
                    (palette[i][j] as Int).toString().toInt(),
                    monetBackgroundLightness[0].toFloat() / 1000.0f
                )

                palette[i][j] = color
                paletteNight[i][j] = color
            }
        }

        for (i in colorTableRows.indices) {
            if (i == 2 &&
                (getBoolean(CUSTOM_SECONDARY_COLOR_SWITCH) || isSelectedSecondary) &&
                selectedStyle != appContextLocale.resources.getString(R.string.monet_monochrome)
            ) {
                putBoolean(CUSTOM_SECONDARY_COLOR_SWITCH, true)

                val secondaryPalette = generateColorPalette(
                    requireContext(),
                    selectedStyle,
                    accentSecondary!!.toInt()
                )
                for (j in colorTableRows[i].childCount - 1 downTo 0) {
                    val color: Int = when (j) {
                        0, colorTableRows[i].childCount - 1 -> {
                            secondaryPalette[0][j] as Int
                        }

                        1 -> {
                            setSaturation(
                                (palette[i][j + 1] as Int).toString().toInt(),
                                -0.1f
                            )
                        }

                        else -> {
                            setSaturation(
                                (secondaryPalette[0][j] as Int).toString().toInt(),
                                (monetSecondaryAccentSaturation[0].toFloat() / 1000.0f * min(
                                    (3.0f - j / 5f).toDouble(),
                                    3.0
                                )).toFloat()
                            )
                        }
                    }

                    palette[i][j] = color
                    paletteNight[i][j] = color

                    if (!accurateShades) {
                        if (j == 8) {
                            palette[i][j] = accentSecondary!!.toInt()
                        }

                        if (j == 5) {
                            paletteNight[i][j] = accentSecondary!!.toInt()
                        }
                    }

                    colorTableRows[i].getChildAt(j).background.setTint(if (!isDarkMode) palette[i][j] as Int else paletteNight[i][j] as Int)
                    colorTableRows[i].getChildAt(j).tag = if (!isDarkMode) {
                        palette[i][j]
                    } else {
                        paletteNight[i][j]
                    }

                    ((colorTableRows[i].getChildAt(j) as ViewGroup).getChildAt(0) as TextView).setTextColor(
                        calculateTextColor(if (!isDarkMode) palette[i][j] as Int else paletteNight[i][j] as Int)
                    )
                }
            } else {
                for (j in 0 until colorTableRows[i].childCount) {
                    try {
                        colorTableRows[i].getChildAt(j).background.setTint(if (!isDarkMode) palette[i][j] as Int else paletteNight[i][j] as Int)
                        colorTableRows[i].getChildAt(j).tag = if (!isDarkMode) {
                            palette[i][j]
                        } else {
                            paletteNight[i][j]
                        }

                        ((colorTableRows[i].getChildAt(j) as ViewGroup).getChildAt(0) as TextView).setTextColor(
                            calculateTextColor(if (!isDarkMode) palette[i][j] as Int else paletteNight[i][j] as Int)
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        finalPalette.clear()
        finalPalette.add(palette)
        finalPalette.add(paletteNight)
    }

    private fun disableBasicColors() {
        clearPrefs(
            CUSTOM_ACCENT,
            COLOR_ACCENT_PRIMARY,
            COLOR_ACCENT_SECONDARY,
            CUSTOM_PRIMARY_COLOR_SWITCH,
            CUSTOM_SECONDARY_COLOR_SWITCH
        )
        disableOverlays(
            COLOR_ACCENT_PRIMARY,
            COLOR_ACCENT_PRIMARY_LIGHT,
            COLOR_ACCENT_SECONDARY,
            COLOR_ACCENT_SECONDARY_LIGHT
        )
    }

    private fun cloneList(src: List<MutableList<Any>>): List<MutableList<Any>> {
        val cloned: MutableList<MutableList<Any>> = ArrayList()

        for (sublist in src) {
            cloned.add(ArrayList(sublist))
        }

        return cloned
    }

    private fun importExportSettings(export: Boolean) {
        if (!hasStoragePermission()) {
            requestStoragePermission(requireContext())
        } else {
            val fileIntent = Intent()
            fileIntent.setAction(if (export) Intent.ACTION_CREATE_DOCUMENT else Intent.ACTION_GET_CONTENT)
            fileIntent.setType("*/*")
            fileIntent.putExtra(Intent.EXTRA_TITLE, "monet_configs" + ".iconify")

            if (export) {
                startExportActivityIntent.launch(fileIntent)
            } else {
                startImportActivityIntent.launch(fileIntent)
            }
        }
    }

    @Suppress("unused")
    @Subscribe
    fun onColorSelected(event: ColorSelectedEvent) {
        if (event.dialogId == -1) {
            val colorCell = colorTableRows[selectedChild[0]].getChildAt(selectedChild[1])
            colorCell.background.setTint(event.selectedColor)
            colorCell.tag = event.selectedColor
            ((colorCell as ViewGroup).getChildAt(0) as TextView).setTextColor(
                calculateTextColor(event.selectedColor)
            )

            finalPalette[0][selectedChild[0]][selectedChild[1]] = event.selectedColor
            finalPalette[1][selectedChild[0]][selectedChild[1]] = event.selectedColor

            binding.floatingActionMenu.show()
            showApplyButton = true
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class)
    fun importMonetSettings(
        sharedPreferences: SharedPreferences,
        inputStream: InputStream
    ): Boolean {
        var objectInputStream: ObjectInputStream? = null
        val map: Map<String?, Any>

        try {
            objectInputStream = ObjectInputStream(inputStream)
            map = objectInputStream.readObject() as Map<String?, Any>
        } catch (exception: Exception) {
            Log.e("ImportSettings", "Error deserializing preferences", exception)
            return false
        } finally {
            objectInputStream?.close()
            inputStream.close()
        }

        val editor = sharedPreferences.edit()

        for ((key, value) in map) {
            if (key == null) continue

            if (value is Boolean &&
                (key.contains(MONET_ENGINE_SWITCH) || key.contains(MONET_ACCURATE_SHADES))
            ) {
                editor.putBoolean(key, value)
            } else if (value is String &&
                (key.endsWith("_day") ||
                        key.endsWith("_night") ||
                        key.contains(MONET_STYLE) ||
                        key.contains(MONET_PRIMARY_COLOR) ||
                        key.contains(MONET_SECONDARY_COLOR))
            ) {
                editor.putString(key, value)
            } else if (value is Int &&
                (key.contains(MONET_PRIMARY_ACCENT_SATURATION) ||
                        key.contains(MONET_SECONDARY_ACCENT_SATURATION) ||
                        key.contains(MONET_BACKGROUND_SATURATION) ||
                        key.contains(MONET_BACKGROUND_LIGHTNESS))
            ) {
                editor.putInt(key, value)
            }
        }

        var status = editor.commit()

        try {
            val colors = colorNames
            val palette: MutableList<List<List<Any>>> = ArrayList()
            val statNames = arrayOf("_day", "_night")

            for (stat in statNames) {
                val temp: MutableList<List<Any>> = ArrayList()

                for (types in colors) {
                    val tmp: MutableList<Any> = ArrayList()

                    for (color in types) {
                        tmp.add(Objects.requireNonNull(map[color + stat]).toString().toInt())
                    }

                    temp.add(tmp)
                }

                palette.add(temp)
            }

            status = status && !buildOverlay(palette, true)

            if (status) {
                putBoolean(MONET_ENGINE_SWITCH, true)

                if (getBoolean("IconifyComponentQSPBD.overlay")) {
                    changeOverlayState(
                        "IconifyComponentQSPBD.overlay",
                        false,
                        "IconifyComponentQSPBD.overlay",
                        true
                    )
                } else if (getBoolean("IconifyComponentQSPBA.overlay")) {
                    changeOverlayState(
                        "IconifyComponentQSPBA.overlay",
                        false,
                        "IconifyComponentQSPBA.overlay",
                        true
                    )
                }

                Toast.makeText(
                    appContext,
                    appContextLocale.resources.getString(R.string.toast_applied),
                    Toast.LENGTH_SHORT
                ).show()

                binding.floatingActionMenu.show()

                showApplyButton = false
                showDisableButton = true
            } else {
                Toast.makeText(
                    appContext,
                    appContextLocale.resources.getString(R.string.toast_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (exception: Exception) {
            Log.e("ImportSettings", "Error building Monet Engine", exception)
        }

        return status
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.monet_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemID = item.itemId

        if (itemID == R.id.menu_export_settings) {
            importExportSettings(true)
        } else if (itemID == R.id.menu_import_settings) {
            importExportSettings(false)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()

        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)

        super.onStop()
    }

    companion object {
        private val colorCodes = intArrayOf(
            0,
            10,
            50,
            100,
            200,
            300,
            400,
            500,
            600,
            700,
            800,
            900,
            1000
        )
        private var accentPrimary: String? = null
        private var accentSecondary: String? = null
        private var selectedStyle: String? = null
        private var isSelectedPrimary = false
        private var isSelectedSecondary = false
        private var accurateShades = getBoolean(MONET_ACCURATE_SHADES, true)
    }
}