package com.drdisagree.iconify.features.settings.lookandfeel.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FormatColorFill
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoSizeSelectLarge
import androidx.compose.material.icons.rounded.SpaceBar
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.preferences.PrefStringRes
import com.drdisagree.iconify.core.preferences.PrefValue
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.arrayRes
import com.drdisagree.iconify.core.preferences.iconRes
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.svg.DynamicColorImageVectors
import com.drdisagree.iconify.core.ui.components.svg.undrawThemePicker
import com.drdisagree.iconify.data.keys.SettingsKey
import com.drdisagree.iconify.data.models.allSeedColors
import com.materialkolor.Contrast
import com.materialkolor.PaletteStyle

private data class ThemeMode(
    val themeMode: Int,
    val name: PrefStringRes
)

private val themeModes = listOf(
    ThemeMode(
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        stringRes(R.string.settings_app_theme_system)
    ),
    ThemeMode(
        AppCompatDelegate.MODE_NIGHT_NO,
        stringRes(R.string.settings_app_theme_light)
    ),
    ThemeMode(
        AppCompatDelegate.MODE_NIGHT_YES,
        stringRes(R.string.settings_app_theme_dark)
    )
)

private data class ContrastLevel(
    val contrast: Contrast,
    val name: PrefStringRes
)

private val contrastLevels = listOf(
    ContrastLevel(Contrast.Default, stringRes(R.string.contrast_default)),
    ContrastLevel(Contrast.Medium, stringRes(R.string.contrast_medium)),
    ContrastLevel(Contrast.High, stringRes(R.string.contrast_high)),
    ContrastLevel(Contrast.Reduced, stringRes(R.string.contrast_reduced))
)

private data class PaletteStyleOption(
    val paletteStyle: PaletteStyle,
    val name: PrefStringRes
)

private val paletteStyles = listOf(
    PaletteStyleOption(PaletteStyle.TonalSpot, stringRes(R.string.monet_tonalspot)),
    PaletteStyleOption(PaletteStyle.Neutral, stringRes(R.string.monet_neutral)),
    PaletteStyleOption(PaletteStyle.Vibrant, stringRes(R.string.monet_vibrant)),
    PaletteStyleOption(PaletteStyle.Expressive, stringRes(R.string.monet_expressive)),
    PaletteStyleOption(PaletteStyle.Rainbow, stringRes(R.string.monet_rainbow)),
    PaletteStyleOption(PaletteStyle.FruitSalad, stringRes(R.string.monet_fruitsalad)),
    PaletteStyleOption(PaletteStyle.Monochrome, stringRes(R.string.monet_monochrome)),
    PaletteStyleOption(PaletteStyle.Fidelity, stringRes(R.string.monet_fidelity)),
    PaletteStyleOption(PaletteStyle.Content, stringRes(R.string.monet_content)),
)

val lookAndFeelPreferences = preferenceScreen {
    composable(key = "header_look_and_feel") {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(vertical = 16.dp),
                imageVector = DynamicColorImageVectors.undrawThemePicker(),
                contentDescription = null
            )
        }
    }

    category {
        listPref(
            key = SettingsKey.THEME_MODE,
            icon = iconRes(Icons.Rounded.DarkMode),
            title = stringRes(R.string.settings_app_theme),
            entries = arrayRes(themeModes.map { it.name }),
            entryValues = arrayRes(themeModes.map { it.themeMode.toString() }),
            summary = {
                val selected = it.prefController.getString(it.key, it.defValue).toInt()
                themeModes.find { mode -> mode.themeMode == selected }!!.name
            }
        )

        listPref(
            key = "theme_color",
            icon = iconRes(Icons.Rounded.FormatColorFill),
            title = stringRes(R.string.theme_color),
            defaultValue = "dynamic",
            entries = arrayRes(listOf(stringRes(R.string.theme_color_dynamic)) + allSeedColors.map {
                stringRes(it.seedColor.name)
            }.toList()),
            entryValues = arrayRes(listOf("dynamic") + allSeedColors.map {
                it.seedColor.name
                    .lowercase()
                    .replace(" ", "_")
            }.toList()),
            summary = {
                val v = it.prefController.getString(it.key, "dynamic")
                stringRes(v.replaceFirstChar { char -> char.uppercase() })
            }
        )

        listPref(
            key = SettingsKey.PALETTE_STYLE,
            icon = iconRes(Icons.Rounded.Palette),
            title = stringRes(R.string.palette_style),
            entries = arrayRes(paletteStyles.map { it.name }.toList()),
            entryValues = arrayRes(paletteStyles.map { it.paletteStyle.name }.toList()),
            summary = {
                val v = it.prefController.getString(it.key, it.defValue)
                paletteStyles.find { style -> style.paletteStyle.name == v }!!.name
            },
            isVisible = { it.getString("theme_color", "dynamic") != "dynamic" }
        )

        switch(
            key = SettingsKey.AMOLED_THEME,
            icon = iconRes(Icons.Rounded.NightsStay),
            title = stringRes(R.string.amoled_mode_title),
            summary = { stringRes(R.string.amoled_mode_desc) },
            isVisible = { ctrl -> ctrl.getString("theme_color", "dynamic") != "dynamic" }
        )

        switch(
            key = SettingsKey.EXPRESSIVE_COLORS,
            icon = iconRes(Icons.Rounded.WaterDrop),
            title = stringRes(R.string.expressive_colors_title),
            summary = { stringRes(R.string.expressive_colors_desc) },
            isVisible = { ctrl ->
                ctrl.getString("theme_color", "dynamic") != "dynamic" &&
                        ctrl.getString(SettingsKey.PALETTE_STYLE) in listOf(
                    PaletteStyle.TonalSpot.name,
                    PaletteStyle.Neutral.name,
                    PaletteStyle.Vibrant.name,
                    PaletteStyle.Expressive.name,
                )
            }
        )

        listPref(
            key = SettingsKey.CONTRAST_LEVEL,
            icon = iconRes(Icons.Rounded.Contrast),
            title = stringRes(R.string.contrast_level),
            entries = arrayRes(contrastLevels.map { it.name }),
            entryValues = arrayRes(contrastLevels.map { it.contrast.value.toString() }),
            summary = {
                val selected =
                    it.prefController.getString(it.key, it.defValue).toDouble()
                contrastLevels.find { level -> level.contrast.value == selected }!!.name
            },
            isVisible = { it.getString("theme_color", "dynamic") != "dynamic" }
        )
    }

    category(title = stringRes(R.string.section_title_additional_settings)) {
        switch(
            key = SettingsKey.BLUR_EFFECT,
            icon = iconRes(Icons.Rounded.BlurOn),
            title = stringRes(R.string.blur_effect_title),
            summary = { stringRes(R.string.blur_effect_desc) },
        )

        switch(
            key = SettingsKey.FLOATING_BOTTOM_BAR,
            icon = iconRes(Icons.Rounded.SpaceBar),
            title = stringRes(R.string.floating_bottom_bar_title),
            summary = { stringRes(R.string.floating_bottom_bar_desc) },
        )

        slider(
            key = SettingsKey.UI_SCALE,
            icon = iconRes(Icons.Rounded.PhotoSizeSelectLarge),
            title = stringRes(R.string.display_scale_title),
            summary = { stringRes(R.string.display_scale_desc) },
            min = 0.5f,
            max = 1.2f,
            steps = 6,
            valueLabel = { "${"%.1f".format(it)}x" },
            showDefaultIndicator = true,
            hideDefaultValue = true,
            showResetButton = true,
        )

        slider(
            key = SettingsKey.TEXT_SCALE,
            icon = iconRes(Icons.Rounded.FormatSize),
            title = stringRes(R.string.text_scale_title),
            summary = { stringRes(R.string.text_scale_desc) },
            min = 0.5f,
            max = 2f,
            steps = 14,
            valueLabel = { "${"%.1f".format(it)}x" },
            showDefaultIndicator = true,
            hideDefaultValue = true,
            showResetButton = true,
        )
    }
}

@Composable
fun LookAndFeelScreen() {
    val prefController = LocalPreferenceController.current

    PreferenceListener(key = "theme_color") {
        val newValue = (it.newValue as PrefValue.StringValue).v

        if (newValue == "dynamic") {
            prefController.setBoolean(SettingsKey.DYNAMIC_COLORS, true)
        } else {
            allSeedColors.find { seed ->
                newValue == seed.seedColor.name
                    .lowercase()
                    .replace(" ", "_")
            }?.let { seedColor ->
                prefController.setString(
                    SettingsKey.SEED_COLOR,
                    seedColor.seedColor.primaryColor.toString()
                )
                prefController.setBoolean(SettingsKey.DYNAMIC_COLORS, false)
            }
        }
    }

    PreferenceScreen(
        items = lookAndFeelPreferences,
        title = stringResource(R.string.look_and_feel_title),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun LookAndFeelScreenPreview() {
    PreviewComposable {
        LookAndFeelScreen()
    }
}