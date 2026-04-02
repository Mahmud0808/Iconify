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
import com.drdisagree.iconify.core.ui.components.svg.themePicker
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
    ContrastLevel(Contrast.Default, stringRes("Default")),
    ContrastLevel(Contrast.Medium, stringRes("Medium")),
    ContrastLevel(Contrast.High, stringRes("High")),
    ContrastLevel(Contrast.Reduced, stringRes("Reduced"))
)

private data class PaletteStyleOption(
    val paletteStyle: PaletteStyle,
    val name: PrefStringRes
)

private val paletteStyles = listOf(
    PaletteStyleOption(PaletteStyle.TonalSpot, stringRes("Tonal Spot")),
    PaletteStyleOption(PaletteStyle.Neutral, stringRes("Neutral")),
    PaletteStyleOption(PaletteStyle.Vibrant, stringRes("Vibrant")),
    PaletteStyleOption(PaletteStyle.Expressive, stringRes("Expressive")),
    PaletteStyleOption(PaletteStyle.Rainbow, stringRes("Rainbow")),
    PaletteStyleOption(PaletteStyle.FruitSalad, stringRes("FruitSalad")),
    PaletteStyleOption(PaletteStyle.Monochrome, stringRes("Monochrome")),
    PaletteStyleOption(PaletteStyle.Fidelity, stringRes("Fidelity")),
    PaletteStyleOption(PaletteStyle.Content, stringRes("Content")),
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
                imageVector = DynamicColorImageVectors.themePicker(),
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
            summary = { ctrl, key ->
                val selected = ctrl.getString(key, SettingsKey.THEME_MODE.default as String).toInt()
                themeModes.find { it.themeMode == selected }!!.name
            }
        )

        listPref(
            key = "theme_color",
            icon = iconRes(Icons.Rounded.FormatColorFill),
            title = stringRes("Theme Color"),
            defaultValue = "dynamic",
            entries = arrayRes(listOf(stringRes("Dynamic")) + allSeedColors.map {
                stringRes(it.seedColor.name)
            }.toList()),
            entryValues = arrayRes(listOf("dynamic") + allSeedColors.map {
                it.seedColor.name
                    .lowercase()
                    .replace(" ", "_")
            }.toList()),
            summary = { ctrl, key ->
                val v = ctrl.getString(key, "dynamic")
                stringRes(v.replaceFirstChar { it.uppercase() })
            }
        )

        listPref(
            key = SettingsKey.PALETTE_STYLE,
            icon = iconRes(Icons.Rounded.Palette),
            title = stringRes("Palette Style"),
            entries = arrayRes(paletteStyles.map { it.name }.toList()),
            entryValues = arrayRes(paletteStyles.map { it.paletteStyle.name }.toList()),
            summary = { ctrl, key ->
                val v = ctrl.getString(key, SettingsKey.PALETTE_STYLE.default as String)
                paletteStyles.find { it.paletteStyle.name == v }!!.name
            },
            isVisible = { ctrl -> ctrl.getString("theme_color", "dynamic") != "dynamic" }
        )

        switch(
            key = SettingsKey.AMOLED_THEME,
            icon = iconRes(Icons.Rounded.NightsStay),
            title = stringRes("Amoled Mode"),
            summary = { _, _ -> stringRes("Black background in dark mode") },
            isVisible = { ctrl -> ctrl.getString("theme_color", "dynamic") != "dynamic" }
        )

        switch(
            key = SettingsKey.EXPRESSIVE_COLORS,
            icon = iconRes(Icons.Rounded.WaterDrop),
            title = stringRes("Expressive Colors"),
            summary = { _, _ -> stringRes("Use colors that are more chromatic") },
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
            title = stringRes("Contrast Level"),
            entries = arrayRes(contrastLevels.map { it.name }),
            entryValues = arrayRes(contrastLevels.map { it.contrast.value.toString() }),
            summary = { ctrl, key ->
                val selected =
                    ctrl.getString(key, SettingsKey.CONTRAST_LEVEL.default as String).toDouble()
                contrastLevels.find { it.contrast.value == selected }!!.name
            },
            isVisible = { ctrl -> ctrl.getString("theme_color", "dynamic") != "dynamic" }
        )
    }

    category(title = "Additional Settings") {
        switch(
            key = SettingsKey.BLUR_EFFECT,
            icon = iconRes(Icons.Rounded.BlurOn),
            title = stringRes("Blur Effect"),
            summary = { _, _ -> stringRes("Apply a blur effect to backgrounds and UI elements") },
        )

        switch(
            key = SettingsKey.FLOATING_BOTTOM_BAR,
            icon = iconRes(Icons.Rounded.SpaceBar),
            title = stringRes("Floating Bottom Bar"),
            summary = { _, _ -> stringRes("Make the bottom bar float over content") },
        )

        slider(
            key = SettingsKey.UI_SCALE,
            icon = iconRes(Icons.Rounded.PhotoSizeSelectLarge),
            title = stringRes("UI Scale"),
            summary = { _, _ -> stringRes("Change the size of UI elements") },
            min = 0.5f,
            max = 1.2f,
            steps = 6,
            valueLabel = { "${"%.1f".format(it)}x" },
            showDefaultIndicator = true,
            hideDefaultValue = true,
            showResetButton = true,
            applyOnValueChangeFinished = true,
        )

        slider(
            key = SettingsKey.TEXT_SCALE,
            icon = iconRes(Icons.Rounded.FormatSize),
            title = stringRes("Text Scale"),
            summary = { _, _ -> stringRes("Change the size of text") },
            min = 0.5f,
            max = 2f,
            steps = 14,
            valueLabel = { "${"%.1f".format(it)}x" },
            showDefaultIndicator = true,
            hideDefaultValue = true,
            showResetButton = true,
            applyOnValueChangeFinished = true,
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
        title = "Look & Feel",
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun LookAndFeelScreenPreview() {
    PreviewComposable {
        LookAndFeelScreen()
    }
}