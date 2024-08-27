package com.drdisagree.iconify.ui.fragments.xposed

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY
import com.drdisagree.iconify.common.Preferences.CHIP_QSSTATUSICONS_STYLE
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_STYLE_CHANGED
import com.drdisagree.iconify.common.Preferences.QSPANEL_STATUSICONSBG_SWITCH
import com.drdisagree.iconify.common.Preferences.STATUSBAR_CLOCKBG_SWITCH
import com.drdisagree.iconify.common.Preferences.STATUSBAR_CLOCK_COLOR_CODE
import com.drdisagree.iconify.common.Preferences.STATUSBAR_CLOCK_COLOR_OPTION
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.databinding.FragmentXposedBackgroundChipBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtil
import com.drdisagree.iconify.utils.overlay.OverlayUtil.enableOverlay

class XposedBackgroundChip : BaseFragment() {

    private lateinit var binding: FragmentXposedBackgroundChipBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentXposedBackgroundChipBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_background_chip
        )

        // Statusbar clock Chip
        binding.clockBgChip.isSwitchChecked = RPrefs.getBoolean(STATUSBAR_CLOCKBG_SWITCH, false)
        binding.clockBgChip.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            RPrefs.putBoolean(STATUSBAR_CLOCKBG_SWITCH, isChecked)

            binding.clockTextColor.setEnabled(isChecked)
            binding.clockTextColorPicker.setEnabled(isChecked)

            if (!isChecked) {
                Handler(Looper.getMainLooper()).postDelayed(
                    { SystemUtil.handleSystemUIRestart() },
                    SWITCH_ANIMATION_DELAY
                )
            }
        }

        // Statusbar clock chip style
        addItemStatusBar(initStatusBarChipStyles())
        refreshBackgroundStatusBar()

        // Statusbar Clock Color
        binding.clockTextColor.setEnabled(RPrefs.getBoolean(STATUSBAR_CLOCKBG_SWITCH, false))
        binding.clockTextColor.setSelectedIndex(RPrefs.getInt(STATUSBAR_CLOCK_COLOR_OPTION, 0))
        binding.clockTextColor.setOnItemSelectedListener { index: Int ->
            RPrefs.putInt(STATUSBAR_CLOCK_COLOR_OPTION, index)
            binding.clockTextColorPicker.visibility = if (index == 2) View.VISIBLE else View.GONE
        }

        // Clock Color Picker
        binding.clockTextColorPicker.setEnabled(
            RPrefs.getBoolean(
                STATUSBAR_CLOCKBG_SWITCH,
                false
            )
        )
        binding.clockTextColorPicker.visibility =
            if (RPrefs.getInt(STATUSBAR_CLOCK_COLOR_OPTION, 0) == 2) {
                View.VISIBLE
            } else {
                View.GONE
            }
        binding.clockTextColorPicker.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = RPrefs.getInt(STATUSBAR_CLOCK_COLOR_CODE, Color.WHITE),
            showPresets = true,
            showAlphaSlider = true,
            showColorShades = true
        )
        binding.clockTextColorPicker.setOnColorSelectedListener { color: Int ->
            binding.clockTextColorPicker.previewColor = color
            RPrefs.putInt(STATUSBAR_CLOCK_COLOR_CODE, color)
        }

        // Status icons chip
        binding.statusIconsChip.isSwitchChecked =
            RPrefs.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false)
        binding.statusIconsChip.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            RPrefs.putBoolean(QSPANEL_STATUSICONSBG_SWITCH, isChecked)

            Handler(Looper.getMainLooper()).postDelayed({
                enableOverlay("IconifyComponentIXCC.overlay")

                Handler(Looper.getMainLooper()).postDelayed(
                    { SystemUtil.handleSystemUIRestart() },
                    SWITCH_ANIMATION_DELAY
                )
            }, SWITCH_ANIMATION_DELAY)
        }

        // Status icons chip style
        addItemStatusIcons(initStatusIconsChipStyles())
        refreshBackgroundStatusIcons()

        return view
    }

    private fun initStatusBarChipStyles(): ArrayList<Array<Any>> {
        val statusBarChipStyle = ArrayList<Array<Any>>().apply {
            add(arrayOf(R.drawable.chip_status_bar_1, String.format(appContextLocale.resources.getString(R.string.style), 1)))
            add(arrayOf(R.drawable.chip_status_bar_2, String.format(appContextLocale.resources.getString(R.string.style), 2)))
            add(arrayOf(R.drawable.chip_status_bar_3, String.format(appContextLocale.resources.getString(R.string.style), 3)))
            add(arrayOf(R.drawable.chip_status_bar_4, String.format(appContextLocale.resources.getString(R.string.style), 4)))
            add(arrayOf(R.drawable.chip_status_bar_5, String.format(appContextLocale.resources.getString(R.string.style), 5)))
            add(arrayOf(R.drawable.chip_status_bar_6, String.format(appContextLocale.resources.getString(R.string.style), 6)))
            add(arrayOf(R.drawable.chip_status_bar_7, String.format(appContextLocale.resources.getString(R.string.style), 7)))
        }

        return statusBarChipStyle
    }

    private fun initStatusIconsChipStyles(): ArrayList<Array<Any>> {
        val statusIconsChipStyle = ArrayList<Array<Any>>().apply {
            add(arrayOf(R.drawable.chip_status_icons_1, String.format(appContextLocale.resources.getString(R.string.style), 1)))
            add(arrayOf(R.drawable.chip_status_icons_2, String.format(appContextLocale.resources.getString(R.string.style), 2)))
            add(arrayOf(R.drawable.chip_status_icons_3, String.format(appContextLocale.resources.getString(R.string.style), 3)))
            add(arrayOf(R.drawable.chip_status_icons_4, String.format(appContextLocale.resources.getString(R.string.style), 4)))
            add(arrayOf(R.drawable.chip_status_icons_5, String.format(appContextLocale.resources.getString(R.string.style), 5)))
            add(arrayOf(R.drawable.chip_status_icons_6, String.format(appContextLocale.resources.getString(R.string.style), 6)))
        }

        return statusIconsChipStyle
    }

    // Function to add new item in list
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun addItemStatusBar(pack: ArrayList<Array<Any>>) {
        for (i in pack.indices) {
            val list = LayoutInflater.from(requireContext())
                .inflate(
                    R.layout.view_status_bar_chip,
                    binding.statusBarChipContainer,
                    false
                )

            val clockContainer = list.findViewById<LinearLayout>(R.id.clock_container)
            clockContainer.background = ContextCompat.getDrawable(appContext, pack[i][0] as Int)

            val styleName = list.findViewById<TextView>(R.id.style_name)
            styleName.text = pack[i][1] as String

            list.setOnClickListener {
                RPrefs.putInt(CHIP_STATUSBAR_CLOCK_STYLE_CHANGED, i)
                refreshBackgroundStatusBar()
            }

            binding.statusBarChipContainer.addView(list)
        }
    }

    // Function to check for bg drawable changes
    private fun refreshBackgroundStatusBar() {
        for (i in 0 until binding.statusBarChipContainer.childCount) {
            val child = binding.statusBarChipContainer.getChildAt(i)
                .findViewById<LinearLayout>(R.id.list_item_chip)

            val title = child.findViewById<TextView>(R.id.style_name)

            if (i == RPrefs.getInt(CHIP_STATUSBAR_CLOCK_STYLE_CHANGED, 0)) {
                title.setTextColor(resources.getColor(R.color.colorAccent, appContext.theme))
            } else {
                title.setTextColor(resources.getColor(R.color.textColorSecondary, appContext.theme))
            }
        }
    }

    // Function to add new item in list
    private fun addItemStatusIcons(pack: ArrayList<Array<Any>>) {
        for (i in pack.indices) {
            val list = LayoutInflater.from(requireContext())
                .inflate(R.layout.view_status_icons_chip, binding.statusIconsChipContainer, false)

            val iconContainer = list.findViewById<LinearLayout>(R.id.clock_container)
            iconContainer.background = ContextCompat.getDrawable(appContext, pack[i][0] as Int)

            val styleName = list.findViewById<TextView>(R.id.style_name)
            styleName.text = pack[i][1] as String

            list.setOnClickListener {
                RPrefs.putInt(CHIP_QSSTATUSICONS_STYLE, i)

                refreshBackgroundStatusIcons()

                if (RPrefs.getBoolean(
                        QSPANEL_STATUSICONSBG_SWITCH,
                        false
                    ) && Build.VERSION.SDK_INT < 33
                ) {
                    Handler(Looper.getMainLooper()).postDelayed(
                        { SystemUtil.doubleToggleDarkMode() },
                        SWITCH_ANIMATION_DELAY
                    )
                }
            }
            binding.statusIconsChipContainer.addView(list)
        }
    }

    // Function to check for bg drawable changes
    private fun refreshBackgroundStatusIcons() {
        for (i in 0 until binding.statusIconsChipContainer.childCount) {
            val child = binding.statusIconsChipContainer.getChildAt(i)
                .findViewById<LinearLayout>(R.id.list_item_chip)

            val title = child.findViewById<TextView>(R.id.style_name)

            if (i == RPrefs.getInt(CHIP_QSSTATUSICONS_STYLE, 0)) {
                title.setTextColor(resources.getColor(R.color.colorAccent, appContext.theme))
            } else {
                title.setTextColor(resources.getColor(R.color.textColorSecondary, appContext.theme))
            }
        }
    }
}