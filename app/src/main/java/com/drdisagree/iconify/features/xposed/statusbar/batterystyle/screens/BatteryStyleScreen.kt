package com.drdisagree.iconify.features.xposed.statusbar.batterystyle.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.preferences.PrefValue
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_CIRCLE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_DEFAULT
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_DOTTED_CIRCLE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_FILLED_CIRCLE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_A
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_I
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_J
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_L
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_M
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_O
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_IOS_16
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_KIM
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_ONE_UI_7
import com.drdisagree.iconify.data.keys.Key
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.common.viewmodels.SystemActionViewModel
import com.drdisagree.iconify.features.xposed.statusbar.batterystyle.components.BatteryStyleBottomSheet
import com.drdisagree.iconify.features.xposed.statusbar.batterystyle.components.rememberBatteryIconItems
import com.drdisagree.iconify.features.xposed.statusbar.batterystyle.components.rememberChargingIconItems
import kotlin.math.roundToInt

fun batteryStylePreferences(
    selectedBatteryIconLabel: () -> String = { "" },
    onBatteryStyleClick: () -> Unit = {},
    selectedChargingIconLabel: () -> String = { "" },
    onChargingStyleClick: () -> Unit = {}
) = preferenceScreen {
    fun PreferenceController.isPrefEnabled(key: Key): Boolean {
        val batteryStyle = getString(XposedKey.CUSTOM_BATTERY_STYLE).toInt()
        val customBatteryStyle = batteryStyle != BATTERY_STYLE_DEFAULT

        return when (key.name) {
            XposedKey.CUSTOM_BATTERY_WIDTH.name,
            XposedKey.CUSTOM_BATTERY_HEIGHT.name -> customBatteryStyle

            else -> true
        }
    }

    fun PreferenceController.isPrefVisible(key: Key): Boolean {
        val batteryStyle = getString(XposedKey.CUSTOM_BATTERY_STYLE).toInt()
        val customBatteryStyle = batteryStyle != BATTERY_STYLE_DEFAULT
        val showAdvancedCustomizations =
            batteryStyle in BATTERY_STYLE_LANDSCAPE_BATTERY_A..BATTERY_STYLE_LANDSCAPE_BATTERY_O
        val showColorPickers = getBoolean(XposedKey.CUSTOM_BATTERY_BLEND_COLOR)
        val showRainbowBattery = batteryStyle in listOf(
            BATTERY_STYLE_LANDSCAPE_BATTERY_I,
            BATTERY_STYLE_LANDSCAPE_BATTERY_J
        )
        val showPercentage = customBatteryStyle && batteryStyle !in listOf(
            BATTERY_STYLE_LANDSCAPE_IOS_16,
            BATTERY_STYLE_LANDSCAPE_BATTERY_L,
            BATTERY_STYLE_LANDSCAPE_BATTERY_M,
            BATTERY_STYLE_LANDSCAPE_ONE_UI_7
        )
        val kimBattery = batteryStyle == BATTERY_STYLE_LANDSCAPE_KIM
        val showInsidePercentage = showPercentage &&
                !kimBattery &&
                !getBoolean(XposedKey.CUSTOM_BATTERY_HIDE_PERCENTAGE)
        val showChargingIconCustomization = customBatteryStyle &&
                getBoolean(XposedKey.CUSTOM_BATTERY_CHARGING_ICON_SWITCH)
        val circleBattery = batteryStyle in listOf(
            BATTERY_STYLE_CIRCLE,
            BATTERY_STYLE_DOTTED_CIRCLE,
            BATTERY_STYLE_FILLED_CIRCLE
        )

        return when (key.name) {
            XposedKey.HIDE_DEFAULT_BATTERY_VIEW.name -> !customBatteryStyle

            XposedKey.CUSTOM_BATTERY_HIDE_PERCENTAGE.name -> showPercentage

            XposedKey.CUSTOM_BATTERY_INSIDE_PERCENTAGE.name -> showInsidePercentage

            XposedKey.CUSTOM_BATTERY_PERIMETER_ALPHA.name,
            XposedKey.CUSTOM_BATTERY_FILL_ALPHA.name -> showAdvancedCustomizations

            XposedKey.CUSTOM_BATTERY_LAYOUT_REVERSE.name -> showAdvancedCustomizations || kimBattery

            XposedKey.CUSTOM_BATTERY_BLEND_COLOR.name -> showAdvancedCustomizations || circleBattery

            XposedKey.CUSTOM_BATTERY_FILL_COLOR.name,
            XposedKey.CUSTOM_BATTERY_FILL_GRAD_COLOR.name,
            XposedKey.CUSTOM_BATTERY_CHARGING_COLOR.name,
            XposedKey.CUSTOM_BATTERY_POWER_SAVE_FILL_COLOR.name,
            XposedKey.CUSTOM_BATTERY_POWER_SAVE_INDICATOR_COLOR.name -> (showAdvancedCustomizations || circleBattery) && showColorPickers

            XposedKey.CUSTOM_BATTERY_RAINBOW_FILL_COLOR.name -> (showAdvancedCustomizations || circleBattery) && showRainbowBattery

            XposedKey.CUSTOM_BATTERY_MARGIN_LEFT.name,
            XposedKey.CUSTOM_BATTERY_MARGIN_RIGHT.name,
            XposedKey.CUSTOM_BATTERY_MARGIN_TOP.name,
            XposedKey.CUSTOM_BATTERY_MARGIN_BOTTOM.name -> customBatteryStyle

            XposedKey.CUSTOM_BATTERY_SWAP_PERCENTAGE.name -> showInsidePercentage

            XposedKey.CUSTOM_BATTERY_HIDE_BATTERY.name,
            XposedKey.CUSTOM_BATTERY_CHARGING_ICON_SWITCH.name -> customBatteryStyle

            XposedKey.CUSTOM_BATTERY_CHARGING_ICON_STYLE.name,
            XposedKey.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT.name,
            XposedKey.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT.name,
            XposedKey.CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT.name -> showChargingIconCustomization

            else -> true
        }
    }

    category {
        action(
            key = "battery_icon_style",
            title = stringRes(R.string.battery_style_title),
            summary = { stringRes(selectedBatteryIconLabel()) },
            onClick = { onBatteryStyleClick() },
        )

        switch(
            key = XposedKey.HIDE_DEFAULT_BATTERY_VIEW,
            title = stringRes(R.string.hide_battery_title),
            summary = { stringRes(R.string.hide_default_battery_desc) },
            isVisible = { it.isPrefVisible(XposedKey.HIDE_DEFAULT_BATTERY_VIEW) }
        )

        slider(
            key = XposedKey.CUSTOM_BATTERY_WIDTH,
            title = stringRes(R.string.battery_width_title),
            min = 10f,
            max = 30f,
            steps = 19,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.isPrefEnabled(XposedKey.CUSTOM_BATTERY_WIDTH) }
        )

        slider(
            key = XposedKey.CUSTOM_BATTERY_HEIGHT,
            title = stringRes(R.string.battery_height_title),
            min = 10f,
            max = 30f,
            steps = 19,
            valueLabel = { "${it.roundToInt()}dp" },
            isEnabled = { it.isPrefEnabled(XposedKey.CUSTOM_BATTERY_HEIGHT) }
        )

        slider(
            key = XposedKey.CUSTOM_BATTERY_MARGIN_LEFT,
            title = stringRes(R.string.battery_margin_left_title),
            min = 0f,
            max = 8f,
            steps = 7,
            valueLabel = { "${it.roundToInt()}dp" },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_MARGIN_LEFT) }
        )

        slider(
            key = XposedKey.CUSTOM_BATTERY_MARGIN_RIGHT,
            title = stringRes(R.string.battery_margin_right_title),
            min = 0f,
            max = 8f,
            steps = 7,
            valueLabel = { "${it.roundToInt()}dp" },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_MARGIN_RIGHT) }
        )

        slider(
            key = XposedKey.CUSTOM_BATTERY_MARGIN_TOP,
            title = stringRes(R.string.battery_margin_top_title),
            min = 0f,
            max = 8f,
            steps = 7,
            valueLabel = { "${it.roundToInt()}dp" },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_MARGIN_TOP) }
        )

        slider(
            key = XposedKey.CUSTOM_BATTERY_MARGIN_BOTTOM,
            title = stringRes(R.string.battery_margin_bottom_title),
            min = 0f,
            max = 8f,
            steps = 7,
            valueLabel = { "${it.roundToInt()}dp" },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_MARGIN_BOTTOM) }
        )
    }

    category {
        switch(
            key = XposedKey.CUSTOM_BATTERY_HIDE_PERCENTAGE,
            title = stringRes(R.string.hide_percentage_title),
            summary = { stringRes(R.string.hide_percentage_desc) },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_HIDE_PERCENTAGE) }
        )

        switch(
            key = XposedKey.CUSTOM_BATTERY_INSIDE_PERCENTAGE,
            title = stringRes(R.string.inside_percentage_title),
            summary = { stringRes(R.string.inside_percentage_desc) },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_INSIDE_PERCENTAGE) }
        )

        switch(
            key = XposedKey.CUSTOM_BATTERY_HIDE_BATTERY,
            title = stringRes(R.string.hide_battery_title),
            summary = { stringRes(R.string.hide_battery_desc) },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_HIDE_BATTERY) }
        )

        switch(
            key = XposedKey.CUSTOM_BATTERY_SWAP_PERCENTAGE,
            title = stringRes(R.string.reverse_layout_title),
            summary = { stringRes(R.string.reverse_layout_desc) },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_SWAP_PERCENTAGE) }
        )

        switch(
            key = XposedKey.CUSTOM_BATTERY_LAYOUT_REVERSE,
            title = stringRes(R.string.rotate_layout_title),
            summary = { stringRes(R.string.rotate_layout_desc) },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_LAYOUT_REVERSE) }
        )
    }

    category {
        switch(
            key = XposedKey.CUSTOM_BATTERY_PERIMETER_ALPHA,
            title = stringRes(R.string.perimeter_alpha_title),
            summary = { stringRes(R.string.perimeter_alpha_desc) },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_PERIMETER_ALPHA) }
        )

        switch(
            key = XposedKey.CUSTOM_BATTERY_FILL_ALPHA,
            title = stringRes(R.string.fill_alpha_title),
            summary = { stringRes(R.string.fill_alpha_desc) },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_FILL_ALPHA) }
        )

        switch(
            key = XposedKey.CUSTOM_BATTERY_RAINBOW_FILL_COLOR,
            title = stringRes(R.string.rainbow_color_title),
            summary = { stringRes(R.string.rainbow_color_desc) },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_RAINBOW_FILL_COLOR) }
        )

        switch(
            key = XposedKey.CUSTOM_BATTERY_BLEND_COLOR,
            title = stringRes(R.string.blend_color_title),
            summary = { stringRes(R.string.blend_color_desc) },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_BLEND_COLOR) }
        )

        colorPicker(
            key = XposedKey.CUSTOM_BATTERY_FILL_COLOR,
            title = stringRes(R.string.fill_color_title),
            summary = { stringRes(R.string.fill_color_title) },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_FILL_COLOR) }
        )

        colorPicker(
            key = XposedKey.CUSTOM_BATTERY_FILL_GRAD_COLOR,
            title = stringRes(R.string.fill_gradient_color_title),
            summary = { stringRes(R.string.fill_gradient_color_desc) },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_FILL_GRAD_COLOR) }
        )

        colorPicker(
            key = XposedKey.CUSTOM_BATTERY_CHARGING_COLOR,
            title = stringRes(R.string.charging_fill_color_title),
            summary = { stringRes(R.string.charging_fill_color_desc) },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_CHARGING_COLOR) }
        )

        colorPicker(
            key = XposedKey.CUSTOM_BATTERY_POWER_SAVE_FILL_COLOR,
            title = stringRes(R.string.powersave_fill_color_title),
            summary = { stringRes(R.string.powersave_fill_color_desc) },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_POWER_SAVE_FILL_COLOR) }
        )

        colorPicker(
            key = XposedKey.CUSTOM_BATTERY_POWER_SAVE_INDICATOR_COLOR,
            title = stringRes(R.string.powersave_indicator_color_title),
            summary = { stringRes(R.string.powersave_indicator_color_desc) },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_POWER_SAVE_INDICATOR_COLOR) }
        )
    }

    category {
        switch(
            key = XposedKey.CUSTOM_BATTERY_CHARGING_ICON_SWITCH,
            title = stringRes(R.string.custom_charging_icon_title),
            summary = { stringRes(R.string.custom_charging_icon_desc) },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_CHARGING_ICON_SWITCH) }
        )

        action(
            key = "charging_icon_style",
            title = stringRes(R.string.charging_icon_style_title),
            summary = { stringRes(selectedChargingIconLabel()) },
            onClick = { onChargingStyleClick() },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_CHARGING_ICON_STYLE) }
        )

        slider(
            key = XposedKey.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT,
            title = stringRes(R.string.charging_icon_margin_left_title),
            min = 0f,
            max = 6f,
            steps = 5,
            valueLabel = { "${it.roundToInt()}dp" },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT) }
        )

        slider(
            key = XposedKey.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT,
            title = stringRes(R.string.charging_icon_margin_right_title),
            min = 0f,
            max = 6f,
            steps = 5,
            valueLabel = { "${it.roundToInt()}dp" },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT) }
        )

        slider(
            key = XposedKey.CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT,
            title = stringRes(R.string.charging_icon_size_title),
            min = 8f,
            max = 20f,
            steps = 11,
            valueLabel = { "${it.roundToInt()}dp" },
            isVisible = { it.isPrefVisible(XposedKey.CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryStyleScreen(
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    val context = LocalContext.current
    val prefController = LocalPreferenceController.current

    val batteryIconItems = rememberBatteryIconItems(context)
    val selectedBatteryIconValue by prefController.observe(
        XposedKey.CUSTOM_BATTERY_STYLE.name,
        XposedKey.CUSTOM_BATTERY_STYLE.default as String
    )
    val selectedBatteryIconIndex = remember(selectedBatteryIconValue, batteryIconItems) {
        batteryIconItems.indexOfFirst { it.value == selectedBatteryIconValue }.takeIf { it >= 0 }
            ?: 0
    }

    val chargingIconItems = rememberChargingIconItems(context)
    val selectedChargingIconValue by prefController.observe(
        XposedKey.CUSTOM_BATTERY_CHARGING_ICON_STYLE.name,
        XposedKey.CUSTOM_BATTERY_CHARGING_ICON_STYLE.default as String
    )
    val selectedChargingIconIndex = remember(selectedChargingIconValue, chargingIconItems) {
        chargingIconItems.indexOfFirst { it.value == selectedChargingIconValue }.takeIf { it >= 0 }
            ?: 0
    }

    var showBatteryIconSheet by rememberSaveable { mutableStateOf(false) }
    var showChargingIconSheet by rememberSaveable { mutableStateOf(false) }

    val batteryIconArray = stringArrayResource(R.array.custom_battery_style_entries)
    val chargingIconArray = stringArrayResource(R.array.custom_battery_style_entries)
    val notAvailableText = stringResource(R.string.not_available)

    val batteryIconLabel = remember(selectedBatteryIconIndex, batteryIconItems) {
        batteryIconItems.getOrNull(selectedBatteryIconIndex)?.label
            ?: batteryIconArray.getOrNull(0)
            ?: notAvailableText
    }
    val chargingIconLabel = remember(selectedChargingIconIndex, chargingIconItems) {
        chargingIconItems.getOrNull(selectedChargingIconIndex)?.label
            ?: chargingIconArray.getOrNull(0)
            ?: notAvailableText
    }

    val batteryIconLabelState = rememberUpdatedState(batteryIconLabel)
    val chargingIconLabelState = rememberUpdatedState(chargingIconLabel)

    val preferences = remember {
        batteryStylePreferences(
            selectedBatteryIconLabel = { batteryIconLabelState.value },
            onBatteryStyleClick = { showBatteryIconSheet = true },
            selectedChargingIconLabel = { chargingIconLabelState.value },
            onChargingStyleClick = { showChargingIconSheet = true }
        )
    }

    if (showBatteryIconSheet) {
        BatteryStyleBottomSheet(
            title = stringResource(R.string.battery_style_title),
            icons = batteryIconItems,
            selectedItemIndex = selectedBatteryIconIndex,
            onItemClick = { index ->
                prefController.setString(
                    XposedKey.CUSTOM_BATTERY_STYLE.name,
                    batteryIconItems[index].value
                )
            },
            onDismiss = { showBatteryIconSheet = false }
        )
    }

    if (showChargingIconSheet) {
        BatteryStyleBottomSheet(
            title = stringResource(R.string.charging_icon_style_title),
            icons = chargingIconItems,
            selectedItemIndex = selectedChargingIconIndex,
            onItemClick = { index ->
                prefController.setString(
                    XposedKey.CUSTOM_BATTERY_CHARGING_ICON_STYLE.name,
                    chargingIconItems[index].value
                )
            },
            onDismiss = { showChargingIconSheet = false }
        )
    }

    PreferenceListener { event ->
        when (event.key) {
            XposedKey.CUSTOM_BATTERY_STYLE.name -> {
                val oldValue = (event.oldValue as PrefValue.StringValue).v
                val newValue = (event.newValue as PrefValue.StringValue).v

                if ((oldValue == "0" && newValue != "0") || (oldValue != "0" && newValue == "0")) {
                    systemActionViewModel?.shouldRestartSystemUI()
                }
            }

            XposedKey.HIDE_DEFAULT_BATTERY_VIEW.name -> {
                systemActionViewModel?.shouldRestartSystemUI()
            }
        }
    }

    PreferenceScreen(
        items = preferences,
        title = stringResource(R.string.activity_title_battery_style),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun BatteryStyleScreenPreview() {
    PreviewComposable {
        BatteryStyleScreen(null)
    }
}