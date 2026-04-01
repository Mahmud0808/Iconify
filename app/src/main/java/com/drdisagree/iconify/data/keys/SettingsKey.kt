package com.drdisagree.iconify.data.keys

import androidx.appcompat.app.AppCompatDelegate
import com.drdisagree.iconify.data.models.AppSeedColors
import com.materialkolor.Contrast
import com.materialkolor.PaletteStyle

enum class SettingsKey(override val default: Any?) : Key {
    THEME_MODE(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString()),
    PALETTE_STYLE(PaletteStyle.TonalSpot.name),
    AMOLED_THEME(false),
    EXPRESSIVE_COLORS(false),
    CONTRAST_LEVEL(Contrast.Default.value.toString()),
    SEED_COLOR(AppSeedColors.Blue.seedColor.primaryColor.toString()),
    DYNAMIC_COLORS(true),
    HAPTICS_AND_VIBRATION(true),
    FLOATING_BOTTOM_BAR(true),
    BLUR_EFFECT(true),
    UI_SCALE(1f),
    TEXT_SCALE(1f),
    SAVED_VERSION_CODE(-1),
    RESTART_SYSTEMUI_AFTER_BOOT(false),
    ON_HOME_PAGE(false),
    FIRST_INSTALL(true),
    UPDATE_DETECTED(false),
    XPOSED_ONLY_MODE(false),
}