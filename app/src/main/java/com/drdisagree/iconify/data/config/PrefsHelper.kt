package com.drdisagree.iconify.data.config

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Dynamic.isAtleastA14
import com.drdisagree.iconify.data.common.Preferences.AGGRESSIVE_QSPANEL_BLUR_SWITCH
import com.drdisagree.iconify.data.common.Preferences.ALBUM_ART_ON_LOCKSCREEN_BLUR
import com.drdisagree.iconify.data.common.Preferences.ALBUM_ART_ON_LOCKSCREEN_FILTER
import com.drdisagree.iconify.data.common.Preferences.APP_DRAWER_THEMED_ICONS
import com.drdisagree.iconify.data.common.Preferences.APP_LANGUAGE
import com.drdisagree.iconify.data.common.Preferences.AUTO_UPDATE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_CIRCLE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_DEFAULT
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_DEFAULT_LANDSCAPE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_DEFAULT_RLANDSCAPE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_DOTTED_CIRCLE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_FILLED_CIRCLE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYA
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYI
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYJ
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYL
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYM
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYO
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_IOS_16
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_KIM
import com.drdisagree.iconify.data.common.Preferences.BLUR_MEDIA_PLAYER_ARTWORK
import com.drdisagree.iconify.data.common.Preferences.BLUR_MEDIA_PLAYER_ARTWORK_RADIUS
import com.drdisagree.iconify.data.common.Preferences.BLUR_RADIUS_VALUE
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.data.common.Preferences.COLORED_NOTIFICATION_ALTERNATIVE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.COLORED_NOTIFICATION_VIEW_SWITCH
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_BLEND_COLOR
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_CHARGING_COLOR
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_STYLE
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_SWITCH
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_DIMENSION
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_FILL_ALPHA
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_FILL_COLOR
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_FILL_GRAD_COLOR
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_HEIGHT
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_HIDE_BATTERY
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_HIDE_PERCENTAGE
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_INSIDE_PERCENTAGE
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_LAYOUT_REVERSE
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_MARGIN_BOTTOM
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_MARGIN_LEFT
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_MARGIN_RIGHT
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_MARGIN_TOP
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_PERIMETER_ALPHA
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_POWERSAVE_FILL_COLOR
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_RAINBOW_FILL_COLOR
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_STYLE
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_SWAP_PERCENTAGE
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_BATTERY_WIDTH
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_DEPTH_WALLPAPER_SWITCH
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_QS_TEXT_COLOR
import com.drdisagree.iconify.data.common.Preferences.DEPTH_WALLPAPER_AI_MODE
import com.drdisagree.iconify.data.common.Preferences.DEPTH_WALLPAPER_AI_STATUS
import com.drdisagree.iconify.data.common.Preferences.DEPTH_WALLPAPER_BACKGROUND_MOVEMENT_MULTIPLIER
import com.drdisagree.iconify.data.common.Preferences.DEPTH_WALLPAPER_FADE_ANIMATION
import com.drdisagree.iconify.data.common.Preferences.DEPTH_WALLPAPER_FOREGROUND_MOVEMENT_MULTIPLIER
import com.drdisagree.iconify.data.common.Preferences.DEPTH_WALLPAPER_ON_AOD
import com.drdisagree.iconify.data.common.Preferences.DEPTH_WALLPAPER_PARALLAX_EFFECT
import com.drdisagree.iconify.data.common.Preferences.DUALTONE_QSPANEL
import com.drdisagree.iconify.data.common.Preferences.FIXED_STATUS_ICONS_SIDEMARGIN
import com.drdisagree.iconify.data.common.Preferences.FIXED_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.data.common.Preferences.FIXED_STATUS_ICONS_TOPMARGIN
import com.drdisagree.iconify.data.common.Preferences.FIX_NOTIFICATION_COLOR
import com.drdisagree.iconify.data.common.Preferences.FIX_NOTIFICATION_FOOTER_BUTTON_COLOR
import com.drdisagree.iconify.data.common.Preferences.FIX_QS_TILE_COLOR
import com.drdisagree.iconify.data.common.Preferences.FLUID_NOTIF_TRANSPARENCY
import com.drdisagree.iconify.data.common.Preferences.FLUID_POWERMENU_TRANSPARENCY
import com.drdisagree.iconify.data.common.Preferences.FLUID_QSPANEL
import com.drdisagree.iconify.data.common.Preferences.FORCE_THEMED_ICONS
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT1
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT2
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT3
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_COLOR_CODE_TEXT1
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_COLOR_CODE_TEXT2
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_COLOR_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_EXPANSION_Y
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_FONT_PICKER
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_FONT_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_LANDSCAPE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HIDE_QSLABEL_SWITCH
import com.drdisagree.iconify.data.common.Preferences.LIGHT_QSPANEL
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_SHADE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WALLPAPER_BLUR
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WALLPAPER_BLUR_RADIUS
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_BIG_ACTIVE
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_BIG_ICON_ACTIVE
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_BIG_ICON_INACTIVE
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_BIG_INACTIVE
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_CUSTOM_COLOR
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE_NAME
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_STYLE
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_SMALL_ACTIVE
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_SMALL_INACTIVE
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_BOTTOMMARGIN
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT1
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT2
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT3
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_COLOR_CODE_TEXT1
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_COLOR_CODE_TEXT2
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_COLOR_SWITCH
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_DEVICENAME
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_FONT_LINEHEIGHT
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_FONT_PICKER
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_FONT_SWITCH
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_FONT_TEXT_SCALING
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_IMAGE_PICKER1
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_IMAGE_PICKER2
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_IMAGE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_MOVE_NOTIFICATION_ICONS
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_STYLE
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_SWITCH
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_TOPMARGIN
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_USERNAME
import com.drdisagree.iconify.data.common.Preferences.NEW_UPDATE_FOUND
import com.drdisagree.iconify.data.common.Preferences.NOTIFICATION_HEADSUP_BLUR
import com.drdisagree.iconify.data.common.Preferences.NOTIFICATION_HEADSUP_BLUR_RADIUS
import com.drdisagree.iconify.data.common.Preferences.NOTIFICATION_HEADSUP_TRANSPARENCY
import com.drdisagree.iconify.data.common.Preferences.NOTIF_TRANSPARENCY_SWITCH
import com.drdisagree.iconify.data.common.Preferences.OP_QS_HEADER_BLUR_LEVEL
import com.drdisagree.iconify.data.common.Preferences.OP_QS_HEADER_SHOW_ARTWORK
import com.drdisagree.iconify.data.common.Preferences.PREF_KEY_UPDATE_STATUS
import com.drdisagree.iconify.data.common.Preferences.QSALPHA_LEVEL
import com.drdisagree.iconify.data.common.Preferences.QSPANEL_BLUR_SWITCH
import com.drdisagree.iconify.data.common.Preferences.QS_TRANSPARENCY_SWITCH
import com.drdisagree.iconify.data.common.Preferences.SB_CLOCK_SIZE
import com.drdisagree.iconify.data.common.Preferences.SB_CLOCK_SIZE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.SELECTED_QS_TEXT_COLOR
import com.drdisagree.iconify.data.common.Preferences.SHOW_HOME_CARD
import com.drdisagree.iconify.data.common.Preferences.STATUSBAR_LOGO_SWITCH
import com.drdisagree.iconify.data.common.Preferences.STATUSBAR_LOGO_TINT
import com.drdisagree.iconify.data.common.Preferences.UNZOOM_DEPTH_WALLPAPER
import com.drdisagree.iconify.data.common.Preferences.UPDATE_OVER_WIFI
import com.drdisagree.iconify.data.common.Preferences.VERTICAL_QSTILE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.WEATHER_CUSTOM_LOCATION
import com.drdisagree.iconify.data.common.Preferences.WEATHER_CUSTOM_LOCATION_PICKER
import com.drdisagree.iconify.data.common.Preferences.WEATHER_ICON_PACK
import com.drdisagree.iconify.data.common.Preferences.WEATHER_OWM_KEY
import com.drdisagree.iconify.data.common.Preferences.WEATHER_PROVIDER
import com.drdisagree.iconify.data.common.Preferences.WEATHER_TEXT_COLOR
import com.drdisagree.iconify.data.common.Preferences.WEATHER_TEXT_COLOR_SWITCH
import com.drdisagree.iconify.data.common.Preferences.WEATHER_UNITS
import com.drdisagree.iconify.data.common.Preferences.WEATHER_UPDATE_INTERVAL
import com.drdisagree.iconify.data.common.Preferences.WEATHER_YANDEX_KEY
import com.drdisagree.iconify.data.common.Preferences.XPOSED_HOOK_CHECK
import com.drdisagree.iconify.data.common.Resources.shouldShowRebootDialog
import com.drdisagree.iconify.data.config.RPrefs.getBoolean
import com.drdisagree.iconify.data.config.RPrefs.getInt
import com.drdisagree.iconify.data.config.RPrefs.getSliderFloat
import com.drdisagree.iconify.data.config.RPrefs.getString
import com.drdisagree.iconify.data.config.RPrefs.getStringSet
import com.drdisagree.iconify.ui.preferences.TwoTargetSwitchPreference
import com.drdisagree.iconify.utils.weather.WeatherConfig

object PrefsHelper {

    fun isVisible(key: String?): Boolean {
        val lockscreenClockStyle = getInt(LSCLOCK_STYLE, 0)
        val lockscreenClockWithImage1 = lockscreenClockStyle in setOf(26, 27, 30, 39, 40, 42, 53)
        val lockscreenClockWithImage2 = lockscreenClockStyle in setOf(26)

        return when (key) {
            UPDATE_OVER_WIFI -> getBoolean(AUTO_UPDATE, true)

            "iconifyHomeCard" -> getBoolean(SHOW_HOME_CARD, true)

            "rebootReminder" -> shouldShowRebootDialog()

            "newUpdate" -> getBoolean(NEW_UPDATE_FOUND)

            XPOSED_HOOK_CHECK -> !getBoolean(key)

            LOCKSCREEN_SHADE_SWITCH,
            QSALPHA_LEVEL -> getBoolean(QS_TRANSPARENCY_SWITCH) ||
                    getBoolean(NOTIF_TRANSPARENCY_SWITCH)

            AGGRESSIVE_QSPANEL_BLUR_SWITCH -> getBoolean(QSPANEL_BLUR_SWITCH)

            HIDE_QSLABEL_SWITCH -> getBoolean(VERTICAL_QSTILE_SWITCH)

            SB_CLOCK_SIZE -> getBoolean(SB_CLOCK_SIZE_SWITCH)

            LSCLOCK_COLOR_CODE_ACCENT1,
            LSCLOCK_COLOR_CODE_ACCENT2,
            LSCLOCK_COLOR_CODE_ACCENT3,
            LSCLOCK_COLOR_CODE_TEXT1,
            LSCLOCK_COLOR_CODE_TEXT2 -> getBoolean(LSCLOCK_COLOR_SWITCH)

            LSCLOCK_DEVICENAME -> lockscreenClockStyle in setOf(19, 32, 47)

            LSCLOCK_USERNAME -> lockscreenClockStyle in setOf(7, 32, 35, 36, 42, 48, 50, 53)

            // Weather Common
            WEATHER_OWM_KEY -> getString(WEATHER_PROVIDER, "0") == "1"
            WEATHER_CUSTOM_LOCATION_PICKER -> getBoolean(WEATHER_CUSTOM_LOCATION)

            // Lockscreen Weather
            WEATHER_TEXT_COLOR -> getBoolean(WEATHER_TEXT_COLOR_SWITCH)

            HEADER_CLOCK_COLOR_CODE_ACCENT1,
            HEADER_CLOCK_COLOR_CODE_ACCENT2,
            HEADER_CLOCK_COLOR_CODE_ACCENT3,
            HEADER_CLOCK_COLOR_CODE_TEXT1,
            HEADER_CLOCK_COLOR_CODE_TEXT2 -> getBoolean(HEADER_CLOCK_COLOR_SWITCH)

            DUALTONE_QSPANEL -> getBoolean(LIGHT_QSPANEL)

            FLUID_NOTIF_TRANSPARENCY,
            FLUID_POWERMENU_TRANSPARENCY -> getBoolean(FLUID_QSPANEL)

            "xposedThemesOthers",
            FIX_QS_TILE_COLOR,
            FIX_NOTIFICATION_COLOR,
            FIX_NOTIFICATION_FOOTER_BUTTON_COLOR -> isAtleastA14

            CUSTOM_DEPTH_WALLPAPER_SWITCH,
            DEPTH_WALLPAPER_ON_AOD -> Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU

            DEPTH_WALLPAPER_FADE_ANIMATION,
            DEPTH_WALLPAPER_PARALLAX_EFFECT,
            DEPTH_WALLPAPER_BACKGROUND_MOVEMENT_MULTIPLIER,
            DEPTH_WALLPAPER_FOREGROUND_MOVEMENT_MULTIPLIER,
            UNZOOM_DEPTH_WALLPAPER -> Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU

            "xposed_depthwallpaperbgimagepicker",
            "xposed_depthwallpaperfgimagepicker" -> getBoolean(CUSTOM_DEPTH_WALLPAPER_SWITCH) ||
                    Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU

            LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH,
            LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE_NAME -> getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET)

            LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR,
            LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR ->
                getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH)

            LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR -> {
                return getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH) &&
                        getString(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_STYLE, "0") == "0"
            }

            LOCKSCREEN_WIDGETS_BIG_ACTIVE,
            LOCKSCREEN_WIDGETS_BIG_INACTIVE,
            LOCKSCREEN_WIDGETS_BIG_ICON_ACTIVE,
            LOCKSCREEN_WIDGETS_BIG_ICON_INACTIVE,
            LOCKSCREEN_WIDGETS_SMALL_ACTIVE,
            LOCKSCREEN_WIDGETS_SMALL_INACTIVE,
            LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE,
            LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE -> getBoolean(LOCKSCREEN_WIDGETS_CUSTOM_COLOR)

            "xposed_lockscreenwidget_weather_settings" -> WeatherConfig.isEnabled(appContext)

            CUSTOM_BATTERY_STYLE,
            CUSTOM_BATTERY_WIDTH,
            CUSTOM_BATTERY_HEIGHT,
            CUSTOM_BATTERY_HIDE_PERCENTAGE,
            CUSTOM_BATTERY_INSIDE_PERCENTAGE,
            CUSTOM_BATTERY_HIDE_BATTERY,
            CUSTOM_BATTERY_SWAP_PERCENTAGE,
            CUSTOM_BATTERY_LAYOUT_REVERSE,
            CUSTOM_BATTERY_PERIMETER_ALPHA,
            CUSTOM_BATTERY_FILL_ALPHA,
            CUSTOM_BATTERY_RAINBOW_FILL_COLOR,
            CUSTOM_BATTERY_BLEND_COLOR,
            CUSTOM_BATTERY_FILL_COLOR,
            CUSTOM_BATTERY_FILL_GRAD_COLOR,
            CUSTOM_BATTERY_CHARGING_COLOR,
            CUSTOM_BATTERY_POWERSAVE_FILL_COLOR,
            CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR,
            CUSTOM_BATTERY_DIMENSION,
            CUSTOM_BATTERY_MARGIN_LEFT,
            CUSTOM_BATTERY_MARGIN_RIGHT,
            CUSTOM_BATTERY_MARGIN_TOP,
            CUSTOM_BATTERY_MARGIN_BOTTOM,
            CUSTOM_BATTERY_CHARGING_ICON_SWITCH,
            CUSTOM_BATTERY_CHARGING_ICON_STYLE,
            CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT,
            CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT,
            CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT -> {
                return isBatteryPrefsVisible(key)
            }

            // Status icons
            "xposedStatusIcons",
            CHIP_STATUS_ICONS_SWITCH,
            "xposed_chipstatusiconscustomizer" -> {
                return Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE
            }

            "mediaPlayerTweaks" -> Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE

            HEADER_CLOCK_LANDSCAPE_SWITCH -> Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE

            HEADER_CLOCK_EXPANSION_Y -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

            "xposedOthers",
            "xposedOthersStatusIcons",
            FIXED_STATUS_ICONS_SWITCH -> Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU

            FIXED_STATUS_ICONS_SIDEMARGIN,
            FIXED_STATUS_ICONS_TOPMARGIN -> Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
                    getBoolean(FIXED_STATUS_ICONS_SWITCH)

            "xposedOpQsHeader" -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

            DEPTH_WALLPAPER_AI_MODE,
            DEPTH_WALLPAPER_AI_STATUS -> Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU &&
                    !getBoolean(CUSTOM_DEPTH_WALLPAPER_SWITCH)

            COLORED_NOTIFICATION_ALTERNATIVE_SWITCH -> getBoolean(COLORED_NOTIFICATION_VIEW_SWITCH)

            LOCKSCREEN_WALLPAPER_BLUR -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

            LOCKSCREEN_WALLPAPER_BLUR_RADIUS -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                    getBoolean(LOCKSCREEN_WALLPAPER_BLUR)

            SELECTED_QS_TEXT_COLOR -> getBoolean(CUSTOM_QS_TEXT_COLOR)

            FORCE_THEMED_ICONS, APP_DRAWER_THEMED_ICONS -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

            LSCLOCK_MOVE_NOTIFICATION_ICONS -> Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM

            ALBUM_ART_ON_LOCKSCREEN_BLUR -> getString(
                ALBUM_ART_ON_LOCKSCREEN_FILTER,
                "0"
            )!!.toInt() in setOf(3, 4)

            OP_QS_HEADER_BLUR_LEVEL -> getBoolean(OP_QS_HEADER_SHOW_ARTWORK)

            LOCKSCREEN_WIDGETS_DEVICE_WIDGET_STYLE -> getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET)

            BLUR_RADIUS_VALUE -> getBoolean(QSPANEL_BLUR_SWITCH)

            HEADER_CLOCK_FONT_PICKER -> getBoolean(HEADER_CLOCK_FONT_SWITCH)

            LSCLOCK_FONT_PICKER -> getBoolean(LSCLOCK_FONT_SWITCH)

            LSCLOCK_IMAGE_SWITCH -> lockscreenClockWithImage1

            LSCLOCK_IMAGE_PICKER1 -> getBoolean(LSCLOCK_IMAGE_SWITCH) && lockscreenClockWithImage1

            LSCLOCK_IMAGE_PICKER2 -> getBoolean(LSCLOCK_IMAGE_SWITCH) && lockscreenClockWithImage2

            BLUR_MEDIA_PLAYER_ARTWORK_RADIUS -> getBoolean(BLUR_MEDIA_PLAYER_ARTWORK)

            NOTIFICATION_HEADSUP_BLUR_RADIUS,
            NOTIFICATION_HEADSUP_TRANSPARENCY -> getBoolean(NOTIFICATION_HEADSUP_BLUR)

            STATUSBAR_LOGO_TINT -> getBoolean(STATUSBAR_LOGO_SWITCH)

            else -> true
        }
    }

    private fun isBatteryPrefsVisible(key: String): Boolean {
        val batteryStyle: Int = getString(CUSTOM_BATTERY_STYLE, 0.toString())!!.toInt()

        val showAdvancedCustomizations =
            batteryStyle in BATTERY_STYLE_LANDSCAPE_BATTERYA..BATTERY_STYLE_LANDSCAPE_BATTERYO

        val showColorPickers: Boolean = getBoolean(CUSTOM_BATTERY_BLEND_COLOR)

        val showRainbowBattery = batteryStyle == BATTERY_STYLE_LANDSCAPE_BATTERYI ||
                batteryStyle == BATTERY_STYLE_LANDSCAPE_BATTERYJ

        val showBatteryDimensions = batteryStyle > BATTERY_STYLE_DEFAULT_LANDSCAPE &&
                getBoolean(CUSTOM_BATTERY_DIMENSION)

        val showPercentage = batteryStyle != BATTERY_STYLE_DEFAULT &&
                batteryStyle != BATTERY_STYLE_DEFAULT_LANDSCAPE &&
                batteryStyle != BATTERY_STYLE_DEFAULT_RLANDSCAPE &&
                batteryStyle != BATTERY_STYLE_LANDSCAPE_IOS_16 &&
                batteryStyle != BATTERY_STYLE_LANDSCAPE_BATTERYL &&
                batteryStyle != BATTERY_STYLE_LANDSCAPE_BATTERYM

        val kimBattery = batteryStyle == BATTERY_STYLE_LANDSCAPE_KIM

        val showInsidePercentage = showPercentage && !kimBattery &&
                !getBoolean(CUSTOM_BATTERY_HIDE_PERCENTAGE)

        val showChargingIconCustomization: Boolean =
            batteryStyle > BATTERY_STYLE_DEFAULT_LANDSCAPE &&
                    getBoolean(CUSTOM_BATTERY_CHARGING_ICON_SWITCH)

        val showSwapLayout: Boolean = showInsidePercentage &&
                batteryStyle > BATTERY_STYLE_DEFAULT_LANDSCAPE

        val circleBattery = batteryStyle == BATTERY_STYLE_CIRCLE ||
                batteryStyle == BATTERY_STYLE_DOTTED_CIRCLE ||
                batteryStyle == BATTERY_STYLE_FILLED_CIRCLE

        return when (key) {
            CUSTOM_BATTERY_PERIMETER_ALPHA,
            CUSTOM_BATTERY_FILL_ALPHA -> showAdvancedCustomizations

            CUSTOM_BATTERY_LAYOUT_REVERSE -> showAdvancedCustomizations || kimBattery

            CUSTOM_BATTERY_BLEND_COLOR -> showAdvancedCustomizations || circleBattery

            CUSTOM_BATTERY_FILL_COLOR,
            CUSTOM_BATTERY_FILL_GRAD_COLOR,
            CUSTOM_BATTERY_CHARGING_COLOR,
            CUSTOM_BATTERY_POWERSAVE_FILL_COLOR,
            CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR -> (showAdvancedCustomizations || circleBattery) &&
                    showColorPickers

            CUSTOM_BATTERY_RAINBOW_FILL_COLOR -> (showAdvancedCustomizations || circleBattery) &&
                    showRainbowBattery

            CUSTOM_BATTERY_DIMENSION -> batteryStyle > BATTERY_STYLE_DEFAULT_LANDSCAPE

            CUSTOM_BATTERY_MARGIN_LEFT,
            CUSTOM_BATTERY_MARGIN_RIGHT,
            CUSTOM_BATTERY_MARGIN_TOP,
            CUSTOM_BATTERY_MARGIN_BOTTOM -> showBatteryDimensions

            CUSTOM_BATTERY_HIDE_PERCENTAGE -> showPercentage

            CUSTOM_BATTERY_INSIDE_PERCENTAGE -> showInsidePercentage

            CUSTOM_BATTERY_SWAP_PERCENTAGE -> showSwapLayout

            CUSTOM_BATTERY_HIDE_BATTERY,
            CUSTOM_BATTERY_CHARGING_ICON_SWITCH -> batteryStyle > BATTERY_STYLE_DEFAULT_LANDSCAPE

            CUSTOM_BATTERY_CHARGING_ICON_STYLE,
            CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT,
            CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT,
            CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT -> showChargingIconCustomization

            else -> true
        }
    }

    fun isEnabled(key: String): Boolean {
        return when (key) {

            // Weather Common Prefs
            WEATHER_UPDATE_INTERVAL,
            PREF_KEY_UPDATE_STATUS,
            WEATHER_PROVIDER,
            WEATHER_UNITS,
            WEATHER_CUSTOM_LOCATION,
            WEATHER_ICON_PACK -> WeatherConfig.isEnabled(appContext)

            WEATHER_OWM_KEY -> getString(WEATHER_PROVIDER, "0") == "1" &&
                    WeatherConfig.isEnabled(appContext)

            WEATHER_YANDEX_KEY -> getString(WEATHER_PROVIDER, "0") == "2" &&
                    WeatherConfig.isEnabled(appContext)

            CUSTOM_BATTERY_WIDTH,
            CUSTOM_BATTERY_HEIGHT -> getString(CUSTOM_BATTERY_STYLE, 0.toString())!!.toInt() != 0

            LSCLOCK_FONT_SWITCH,
            LSCLOCK_FONT_PICKER,
            LSCLOCK_IMAGE_PICKER1,
            LSCLOCK_IMAGE_PICKER2,
            LSCLOCK_STYLE,
            LSCLOCK_TOPMARGIN,
            LSCLOCK_BOTTOMMARGIN,
            LSCLOCK_COLOR_SWITCH,
            LSCLOCK_COLOR_CODE_ACCENT1,
            LSCLOCK_COLOR_CODE_ACCENT2,
            LSCLOCK_COLOR_CODE_ACCENT3,
            LSCLOCK_COLOR_CODE_TEXT1,
            LSCLOCK_COLOR_CODE_TEXT2,
            LSCLOCK_FONT_LINEHEIGHT,
            LSCLOCK_FONT_TEXT_SCALING,
            LSCLOCK_USERNAME,
            LSCLOCK_DEVICENAME -> getBoolean(LSCLOCK_SWITCH)

            else -> true
        }
    }

    @SuppressLint("DefaultLocale")
    fun getSummary(fragmentCompat: Context, key: String): String? {
        when {
            key.endsWith("Slider") -> {
                val value = String.format("%.2f", getSliderFloat(key, 0f))
                return if (value.endsWith(".00")) value.substring(0, value.length - 3) else value
            }

            key.endsWith("List") -> {
                return getString(key, "")
            }

            key.endsWith("EditText") -> {
                return getString(key, "")
            }

            key.endsWith("MultiSelect") -> {
                return getStringSet(key, emptySet()).toString()
            }

            else -> return when (key) {
                APP_LANGUAGE -> {
                    val currentLanguageCode =
                        listOf<String?>(*fragmentCompat.resources.getStringArray(R.array.locale_code))
                            .indexOf(
                                getString(
                                    APP_LANGUAGE,
                                    fragmentCompat.resources.configuration.locales[0].language
                                )
                            )
                    val selectedLanguageCode = if (currentLanguageCode < 0) listOf<String>(
                        *fragmentCompat.resources.getStringArray(R.array.locale_code)
                    ).indexOf("en-US") else currentLanguageCode

                    return listOf<String>(*fragmentCompat.resources.getStringArray(R.array.locale_name))[selectedLanguageCode]
                }

                "checkForUpdatePref" -> BuildConfig.VERSION_NAME

                else -> null
            }
        }
    }

    fun setupAllPreferences(group: PreferenceGroup) {
        var i = 0

        while (true) {
            try {
                val thisPreference = group.getPreference(i)

                setupPreference(thisPreference)

                if (thisPreference is PreferenceGroup) {
                    setupAllPreferences(thisPreference)
                } else if (thisPreference is TwoTargetSwitchPreference) {
                    val switchPreference: TwoTargetSwitchPreference = thisPreference
                    switchPreference.isChecked = getBoolean(switchPreference.key)
                }
            } catch (ignored: Throwable) {
                break
            }

            i++
        }
    }

    private fun setupPreference(preference: Preference) {
        try {
            val key = preference.key

            preference.isVisible = isVisible(key)
            preference.isEnabled = isEnabled(key)

            getSummary(preference.context, key)?.let {
                preference.summary = it
            }
        } catch (ignored: Throwable) {
        }
    }
}
