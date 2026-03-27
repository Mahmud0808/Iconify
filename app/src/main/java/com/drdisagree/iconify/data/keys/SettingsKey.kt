package com.drdisagree.iconify.data.keys

import androidx.appcompat.app.AppCompatDelegate
import com.drdisagree.iconify.data.models.AppSeedColors
import com.materialkolor.Contrast
import com.materialkolor.PaletteStyle

enum class SettingsKey(override val default: Any?) : Key {
    FIRST_LAUNCH(true),
    LOOK_AND_FEEL(null),
    LANGUAGE(null),
    THEME_MODE(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString()),
    PALETTE_STYLE(PaletteStyle.TonalSpot.name),
    DARK_THEME(null),
    AMOLED_THEME(false),
    EXPRESSIVE_COLORS(false),
    CONTRAST_LEVEL(Contrast.Default.value.toString()),
    SEED_COLOR(AppSeedColors.Blue.seedColor.primaryColor.toString()),
    DYNAMIC_COLORS(true),
    HAPTICS_AND_VIBRATION(true),
    SAVED_VERSION_CODE(0),
    RESTART_SYSTEMUI_AFTER_BOOT(false),
}