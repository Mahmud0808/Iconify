package com.drdisagree.iconify.common

import com.drdisagree.iconify.SplashActivity
import com.drdisagree.iconify.config.RPrefs.getBoolean

object Preferences {

    // Xposed mods
    const val XPOSED_HOOK_CHECK = "xposedHookCheck"
    const val FORCE_RELOAD_OVERLAY_STATE = "xposed_force_reload_overlay_state"
    const val QS_TRANSPARENCY_SWITCH = "xposed_qstransparency"
    const val NOTIF_TRANSPARENCY_SWITCH = "xposed_notiftransparency"
    const val LOCKSCREEN_SHADE_SWITCH = "xposed_lockscreen_shade"
    const val QSALPHA_LEVEL = "xposed_qsalpha"
    const val STATUSBAR_CLOCKBG_SWITCH = "xposed_sbclockbg"
    const val CHIP_STATUSBAR_CLOCK_SWITCH = "xposed_chipstatusbarclock"
    const val CHIP_STATUSBAR_CLOCK_STYLE_CHANGED = "xposed_chipstatusbarclockstylechanged"
    const val CHIP_STATUSBAR_CLOCK_TEXT_COLOR_OPTION = "xposed_sbclockcolor"
    const val CHIP_STATUSBAR_CLOCK_TEXT_COLOR_CODE = "xposed_sbclockcolorcode"
    const val CHIP_STATUSBAR_CLOCK_ACCENT = "xposed_chipstatusbarclockaccent"
    const val CHIP_STATUSBAR_CLOCK_START_COLOR = "xposed_chipstatusbarclockstartcolor"
    const val CHIP_STATUSBAR_CLOCK_END_COLOR = "xposed_chipstatusbarclockendcolor"
    const val CHIP_STATUSBAR_CLOCK_GRADIENT_DIRECTION = "xposed_chipstatusbarclockgradientdirection"
    const val CHIP_STATUSBAR_CLOCK_PADDING_LEFT = "xposed_chipstatusbarclockpaddingleft"
    const val CHIP_STATUSBAR_CLOCK_PADDING_RIGHT = "xposed_chipstatusbarclockpaddingright"
    const val CHIP_STATUSBAR_CLOCK_PADDING_TOP = "xposed_chipstatusbarclockpaddingtop"
    const val CHIP_STATUSBAR_CLOCK_PADDING_BOTTOM = "xposed_chipstatusbarclockpaddingbottom"
    const val CHIP_STATUSBAR_CLOCK_STROKE_SWITCH = "xposed_chipstatusbarclockstroke"
    const val CHIP_STATUSBAR_CLOCK_STROKE_WIDTH = "xposed_chipstatusbarclockstrokewidth"
    const val CHIP_STATUSBAR_CLOCK_STROKE_ACCENT = "xposed_chipstatusbarclockstrokeaccent"
    const val CHIP_STATUSBAR_CLOCK_STROKE_COLOR = "xposed_chipstatusbarclockstrokecolor"
    const val CHIP_STATUSBAR_CLOCK_STROKE_DASH = "xposed_chipstatusbarclockstrokedash"
    const val CHIP_STATUSBAR_CLOCK_STROKE_DASH_WIDTH = "xposed_chipstatusbarclockstrokedashwidth"
    const val CHIP_STATUSBAR_CLOCK_STROKE_DASH_GAP = "xposed_chipstatusbarclockstrokedashgap"
    const val CHIP_STATUSBAR_CLOCK_RADIUS_TOP_LEFT = "xposed_chipstatusbarclockradiustopleft"
    const val CHIP_STATUSBAR_CLOCK_RADIUS_TOP_RIGHT = "xposed_chipstatusbarclockradiustopright"
    const val CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_RIGHT =
        "xposed_chipstatusbarclockradiusbottomright"
    const val CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_LEFT = "xposed_chipstatusbarclockradiusbottomleft"
    const val CHIP_STATUS_ICONS_SWITCH = "xposed_chipstatusicons"
    const val CHIP_STATUS_ICONS_STYLE_CHANGED = "xposed_chipstatusiconsstylechanged"
    const val CHIP_STATUS_ICONS_ACCENT = "xposed_chipstatusiconsaccent"
    const val CHIP_STATUS_ICONS_START_COLOR = "xposed_chipstatusiconsstartcolor"
    const val CHIP_STATUS_ICONS_END_COLOR = "xposed_chipstatusiconsendcolor"
    const val CHIP_STATUS_ICONS_GRADIENT_DIRECTION = "xposed_chipstatusiconsgradientdirection"
    const val CHIP_STATUS_ICONS_PADDING_LEFT = "xposed_chipstatusiconspaddingleft"
    const val CHIP_STATUS_ICONS_PADDING_RIGHT = "xposed_chipstatusiconspaddingright"
    const val CHIP_STATUS_ICONS_PADDING_TOP = "xposed_chipstatusiconspaddingtop"
    const val CHIP_STATUS_ICONS_PADDING_BOTTOM = "xposed_chipstatusiconspaddingbottom"
    const val CHIP_STATUS_ICONS_STROKE_SWITCH = "xposed_chipstatusiconsstroke"
    const val CHIP_STATUS_ICONS_STROKE_WIDTH = "xposed_chipstatusiconsstrokewidth"
    const val CHIP_STATUS_ICONS_STROKE_ACCENT = "xposed_chipstatusiconsstrokeaccent"
    const val CHIP_STATUS_ICONS_STROKE_COLOR = "xposed_chipstatusiconsstrokecolor"
    const val CHIP_STATUS_ICONS_STROKE_DASH = "xposed_chipstatusiconsstrokedash"
    const val CHIP_STATUS_ICONS_STROKE_DASH_WIDTH = "xposed_chipstatusiconsstrokedashwidth"
    const val CHIP_STATUS_ICONS_STROKE_DASH_GAP = "xposed_chipstatusiconsstrokedashgap"
    const val CHIP_STATUS_ICONS_RADIUS_TOP_LEFT = "xposed_chipstatusiconsradiustopleft"
    const val CHIP_STATUS_ICONS_RADIUS_TOP_RIGHT = "xposed_chipstatusiconsradiustopright"
    const val CHIP_STATUS_ICONS_RADIUS_BOTTOM_RIGHT = "xposed_chipstatusiconsradiusbottomright"
    const val CHIP_STATUS_ICONS_RADIUS_BOTTOM_LEFT = "xposed_chipstatusiconsradiusbottomleft"
    const val QSPANEL_STATUSICONSBG_SWITCH = "xposed_qsstatusiconsbg"
    const val CHIP_QSSTATUSICONS_STYLE = "xposed_chipqsstatusiconsstyle"
    const val VERTICAL_QSTILE_SWITCH = "xposed_verticalqstile"
    const val HIDE_QSLABEL_SWITCH = "xposed_hideqslabel"
    const val VOLUME_PANEL_PERCENTAGE = "xposed_volumepanelpercentage"
    const val VOLUME_PANEL_SAFETY_WARNING = "xposed_volumepanelsafetywarning"
    const val VOLUME_COLORED_RINGER_ICON = "xposed_volumecoloredringericon"
    const val HEADER_IMAGE_SWITCH = "xposed_headerimage"
    const val HEADER_IMAGE_LANDSCAPE_SWITCH = "xposed_headerimagelandscape"
    const val HEADER_IMAGE_BOTTOM_FADE_AMOUNT = "xposed_headerimagebottomfadeamount"
    const val HEADER_IMAGE_HEIGHT = "xposed_headerimageheight"
    const val HEADER_IMAGE_ALPHA = "xposed_headerimagealpha"
    const val HEADER_IMAGE_ZOOMTOFIT = "xposed_headerimagezoomtofit"
    const val HEADER_IMAGE_OVERLAP = "xposed_headerimageoverlap"
    const val HIDE_STATUS_ICONS_SWITCH = "xposed_hidestatusicons"
    const val HEADER_CLOCK_SWITCH = "xposed_headerclock"
    const val HEADER_CLOCK_SIDEMARGIN = "xposed_headerclocksidemargin"
    const val HEADER_CLOCK_TOPMARGIN = "xposed_headerclocktopmargin"
    const val HEADER_CLOCK_CENTERED = "xposed_headerclockcentered"
    const val HEADER_CLOCK_LANDSCAPE_SWITCH = "xposed_headerclocklandscape"
    const val HEADER_CLOCK_STYLE = "xposed_headerclockstyle"
    const val HEADER_CLOCK_FONT_SWITCH = "xposed_headerclockfont"
    const val HEADER_CLOCK_COLOR_SWITCH = "xposed_headerclockcolor"
    const val HEADER_CLOCK_COLOR_CODE_ACCENT1 = "xposed_headerclockcolorcodeaccent1"
    const val HEADER_CLOCK_COLOR_CODE_ACCENT2 = "xposed_headerclockcolorcodeaccent2"
    const val HEADER_CLOCK_COLOR_CODE_ACCENT3 = "xposed_headerclockcolorcodeaccent3"
    const val HEADER_CLOCK_COLOR_CODE_TEXT1 = "xposed_headerclockcolorcodetext1"
    const val HEADER_CLOCK_COLOR_CODE_TEXT2 = "xposed_headerclockcolorcodetext2"
    const val HEADER_CLOCK_FONT_TEXT_SCALING = "xposed_headerclocktextscaling"
    const val QSPANEL_HIDE_CARRIER = "xposed_qspanelhidecarrier"
    const val LSCLOCK_SWITCH = "xposed_lockscreenclock"
    const val LSCLOCK_STYLE = "xposed_lockscreenclockstyle"
    const val LSCLOCK_TOPMARGIN = "xposed_lockscreenclocktopmargin"
    const val LSCLOCK_BOTTOMMARGIN = "xposed_lockscreenclockbottommargin"
    const val LSCLOCK_COLOR_SWITCH = "xposed_lockscreenclockcolor"
    const val LSCLOCK_COLOR_CODE_ACCENT1 = "xposed_lockscreenclockcolorcodeaccent1"
    const val LSCLOCK_COLOR_CODE_ACCENT2 = "xposed_lockscreenclockcolorcodeaccent2"
    const val LSCLOCK_COLOR_CODE_ACCENT3 = "xposed_lockscreenclockcolorcodeaccent3"
    const val LSCLOCK_COLOR_CODE_TEXT1 = "xposed_lockscreenclockcolorcodetext1"
    const val LSCLOCK_COLOR_CODE_TEXT2 = "xposed_lockscreenclockcolorcodetext2"
    const val LSCLOCK_FONT_SWITCH = "xposed_lockscreenclockfont"
    const val LSCLOCK_FONT_LINEHEIGHT = "xposed_lockscreenclockfontlineheight"
    const val LSCLOCK_FONT_TEXT_SCALING = "xposed_lockscreenclocktextscaling"
    const val LSCLOCK_USERNAME = "xposed_lockscreenclockcustomusername"
    const val LSCLOCK_DEVICENAME = "xposed_lockscreenclockcustomdevicename"

    // LS Widgets
    const val LOCKSCREEN_WIDGETS_ENABLED: String = "lockscreen_widgets_enabled"
    const val LOCKSCREEN_WIDGETS_DEVICE_WIDGET: String = "lockscreen_device_widget"
    const val LOCKSCREEN_WIDGETS: String = "lockscreen_widgets"
    const val LOCKSCREEN_WIDGETS_EXTRAS: String = "lockscreen_widgets_extras"
    const val MAIN_WIDGET_1_KEY: String = "main_custom_widgets1"
    const val MAIN_WIDGET_2_KEY: String = "main_custom_widgets2"
    const val EXTRA_WIDGET_1_KEY: String = "custom_widgets1"
    const val EXTRA_WIDGET_2_KEY: String = "custom_widgets2"
    const val EXTRA_WIDGET_3_KEY: String = "custom_widgets3"
    const val EXTRA_WIDGET_4_KEY: String = "custom_widgets4"
    const val LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH: String =
        "lockscreen_device_widget_custom_color"
    const val LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR: String =
        "lockscreen_device_widget_linear_progress_color"
    const val LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR: String =
        "lockscreen_device_widget_circular_progress_color"
    const val LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR: String =
        "lockscreen_device_widget_text_color"
    const val LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE: String =
        "lockscreen_device_widget_device_name"
    const val LOCKSCREEN_WIDGETS_CUSTOM_COLOR: String = "lockscreen_widgets_custom_color"
    const val LOCKSCREEN_WIDGETS_BIG_ACTIVE: String = "lockscreen_widgets_big_active"
    const val LOCKSCREEN_WIDGETS_BIG_INACTIVE: String = "lockscreen_widgets_big_inactive"
    const val LOCKSCREEN_WIDGETS_SMALL_ACTIVE: String = "lockscreen_widgets_small_active"
    const val LOCKSCREEN_WIDGETS_SMALL_INACTIVE: String = "lockscreen_widgets_small_inactive"
    const val LOCKSCREEN_WIDGETS_BIG_ICON_ACTIVE: String = "lockscreen_widgets_big_icon_active"
    const val LOCKSCREEN_WIDGETS_BIG_ICON_INACTIVE: String = "lockscreen_widgets_big_icon_inactive"
    const val LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE: String = "lockscreen_widgets_small_icon_active"
    const val LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE: String =
        "lockscreen_widgets_small_icon_inactive"
    const val LOCKSCREEN_WIDGETS_BOTTOM_MARGIN: String = "lockscreen_widgets_bottom_margin"
    const val LOCKSCREEN_WIDGETS_SCALE: String = "lockscreen_widgets_scale"
    const val FIXED_STATUS_ICONS_SWITCH = "xposed_fixedstatusicons"
    const val FIXED_STATUS_ICONS_SIDEMARGIN = "xposed_fixedstatusiconssidemargin"
    const val FIXED_STATUS_ICONS_TOPMARGIN = "xposed_fixedstatusiconstopmargin"
    const val HIDE_LOCKSCREEN_STATUSBAR = "xposed_hidelockscreenstatusbar"
    const val SB_CLOCK_SIZE_SWITCH = "xposed_sbclocksizeswitch"
    const val SB_CLOCK_SIZE = "xposed_sbclocksize"
    const val COLORED_STATUSBAR_ICON = "xposed_coloredstatusbaricon"
    const val HIDE_LOCKSCREEN_CARRIER = "xposed_hidelockscreencarrier"
    const val HIDE_LOCKSCREEN_LOCK_ICON = "xposed_hidelockscreenlockicon"
    const val LIGHT_QSPANEL = "xposed_lightqspanel"
    const val DUALTONE_QSPANEL = "xposed_dualtoneqspanel"
    const val BLACK_QSPANEL = "xposed_blackqspanel"
    const val FLUID_QSPANEL = "xposed_fluidqspanel"
    const val FLUID_NOTIF_TRANSPARENCY = "xposed_fluidnotiftransparency"
    const val FLUID_POWERMENU_TRANSPARENCY = "xposed_fluidpowermenutransparency"
    const val CUSTOM_BATTERY_STYLE = "xposed_custombatterystyle"
    const val CUSTOM_BATTERY_WIDTH = "xposed_custombatterywidth"
    const val CUSTOM_BATTERY_HEIGHT = "xposed_custombatteryheight"
    const val HIDE_DATA_DISABLED_ICON = "xposed_hideDataDisabledIcon"
    const val DEPTH_WALLPAPER_SWITCH = "xposed_depthwallpaper"
    const val CUSTOM_DEPTH_WALLPAPER_SWITCH = "xposed_customdepthwallpaper"
    const val DEPTH_WALLPAPER_FOREGROUND_ALPHA = "xposed_depthwallpaperforegroundalpha"
    const val DEPTH_WALLPAPER_ON_AOD = "xposed_depthwallpaperaonaod"
    const val DEPTH_WALLPAPER_FADE_ANIMATION = "xposed_depthwallpaperfadeanimation"
    const val DEPTH_WALLPAPER_PARALLAX_EFFECT = "xposed_depthwallpaperparallaxeffect"
    const val DEPTH_WALLPAPER_BACKGROUND_MOVEMENT_MULTIPLIER =
        "xposed_depthwallpaperbackgroundmovementmultiplier"
    const val DEPTH_WALLPAPER_FOREGROUND_MOVEMENT_MULTIPLIER =
        "xposed_depthwallpaperforegroundmovementmultiplier"
    const val DEPTH_WALLPAPER_CHANGED = "xposed_depthwallpaperchanged"
    const val UNZOOM_DEPTH_WALLPAPER = "xposed_unzoomdepthwallpaper"
    const val CUSTOM_BATTERY_LAYOUT_REVERSE = "xposed_custombatterylayoutreverse"
    const val CUSTOM_BATTERY_DIMENSION = "xposed_custombatterydimension"
    const val CUSTOM_BATTERY_MARGIN_LEFT = "xposed_custombatterymarginleft"
    const val CUSTOM_BATTERY_MARGIN_TOP = "xposed_custombatterymargintop"
    const val CUSTOM_BATTERY_MARGIN_RIGHT = "xposed_custombatterymarginright"
    const val CUSTOM_BATTERY_MARGIN_BOTTOM = "xposed_custombatterymarginbottom"
    const val CUSTOM_BATTERY_PERIMETER_ALPHA = "xposed_custombatteryperimeteralpha"
    const val CUSTOM_BATTERY_FILL_ALPHA = "xposed_custombatteryfilledalpha"
    const val CUSTOM_BATTERY_RAINBOW_FILL_COLOR = "xposed_custombatteryrainbowfillcolor"
    const val CUSTOM_BATTERY_BLEND_COLOR = "xposed_custombatteryblendcolor"
    const val CUSTOM_BATTERY_CHARGING_COLOR = "xposed_custombatterychargingcolor"
    const val CUSTOM_BATTERY_FILL_COLOR = "xposed_custombatteryfillcolor"
    const val CUSTOM_BATTERY_FILL_GRAD_COLOR = "xposed_custombatteryfillgradcolor"
    const val CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR =
        "xposed_custombatterypowersaveindicatorcolor"
    const val CUSTOM_BATTERY_POWERSAVE_FILL_COLOR = "xposed_custombatterypowersavefillcolor"
    const val CUSTOM_BATTERY_SWAP_PERCENTAGE = "xposed_custombatteryswappercentage"
    const val CUSTOM_BATTERY_CHARGING_ICON_SWITCH = "xposed_custombatterychargingiconswitch"
    const val CUSTOM_BATTERY_CHARGING_ICON_STYLE = "xposed_custombatterychargingiconstyle"
    const val CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT =
        "xposed_custombatterychargingiconmarginleft"
    const val CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT =
        "xposed_custombatterychargingiconmarginright"
    const val CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT =
        "xposed_custombatterychargingiconwidthheight"
    const val CUSTOM_BATTERY_HIDE_PERCENTAGE = "xposed_custombatteryhidepercentage"
    const val CUSTOM_BATTERY_INSIDE_PERCENTAGE = "xposed_custombatteryinsidepercentage"
    const val CUSTOM_BATTERY_HIDE_BATTERY = "xposed_custombatteryhidebattery"
    const val BLUR_RADIUS_VALUE = "xposed_blurradiusvalue"
    const val CUSTOM_QS_MARGIN = "xposed_customqsmargin"
    const val QQS_TOPMARGIN = "xposed_qqspanelTopMargin"
    const val QS_TOPMARGIN = "xposed_qspanelTopMargin"
    const val FIX_QS_TILE_COLOR = "xposed_fixqstilecolor"
    const val FIX_NOTIFICATION_COLOR = "xposed_fixnotificationcolor"
    const val FIX_NOTIFICATION_FOOTER_BUTTON_COLOR = "xposed_fixnotificationfooterbuttoncolor"
    const val HIDE_QS_SILENT_TEXT = "xposed_hideqssilenttext"
    const val HIDE_QS_ON_LOCKSCREEN = "xposed_hideqsonlockscreen"
    const val HIDE_QS_FOOTER_BUTTONS = "xposed_hideqsfooterbuttons"
    const val QS_TEXT_ALWAYS_WHITE = "xposed_qstextalwayswhite"
    const val QS_TEXT_FOLLOW_ACCENT = "xposed_qstextfollowaccent"

    // Xposed view tags
    const val ICONIFY_HEADER_CLOCK_TAG = "iconify_header_clock"
    const val ICONIFY_LOCKSCREEN_CLOCK_TAG = "iconify_lockscreen_clock"
    const val ICONIFY_DEPTH_WALLPAPER_TAG = "iconify_depth_wallpaper"
    const val ICONIFY_CHARGING_ICON_TAG = "iconify_charging_icon"

    // Battery styles
    const val BATTERY_STYLE_DEFAULT = 0
    const val BATTERY_STYLE_DEFAULT_RLANDSCAPE = 1
    const val BATTERY_STYLE_DEFAULT_LANDSCAPE = 2
    const val BATTERY_STYLE_CUSTOM_RLANDSCAPE = 3
    const val BATTERY_STYLE_CUSTOM_LANDSCAPE = 4
    const val BATTERY_STYLE_PORTRAIT_CAPSULE = 5
    const val BATTERY_STYLE_PORTRAIT_LORN = 6
    const val BATTERY_STYLE_PORTRAIT_MX = 7
    const val BATTERY_STYLE_PORTRAIT_AIROO = 8
    const val BATTERY_STYLE_RLANDSCAPE_STYLE_A = 9
    const val BATTERY_STYLE_LANDSCAPE_STYLE_A = 10
    const val BATTERY_STYLE_RLANDSCAPE_STYLE_B = 11
    const val BATTERY_STYLE_LANDSCAPE_STYLE_B = 12
    const val BATTERY_STYLE_LANDSCAPE_IOS_15 = 13
    const val BATTERY_STYLE_LANDSCAPE_IOS_16 = 14
    const val BATTERY_STYLE_PORTRAIT_ORIGAMI = 15
    const val BATTERY_STYLE_LANDSCAPE_SMILEY = 16
    const val BATTERY_STYLE_LANDSCAPE_MIUI_PILL = 17
    const val BATTERY_STYLE_LANDSCAPE_COLOROS = 18
    const val BATTERY_STYLE_RLANDSCAPE_COLOROS = 19
    const val BATTERY_STYLE_LANDSCAPE_BATTERYA = 20
    const val BATTERY_STYLE_LANDSCAPE_BATTERYB = 21
    const val BATTERY_STYLE_LANDSCAPE_BATTERYC = 22
    const val BATTERY_STYLE_LANDSCAPE_BATTERYD = 23
    const val BATTERY_STYLE_LANDSCAPE_BATTERYE = 24
    const val BATTERY_STYLE_LANDSCAPE_BATTERYF = 25
    const val BATTERY_STYLE_LANDSCAPE_BATTERYG = 26
    const val BATTERY_STYLE_LANDSCAPE_BATTERYH = 27
    const val BATTERY_STYLE_LANDSCAPE_BATTERYI = 28
    const val BATTERY_STYLE_LANDSCAPE_BATTERYJ = 29
    const val BATTERY_STYLE_LANDSCAPE_BATTERYK = 30
    const val BATTERY_STYLE_LANDSCAPE_BATTERYL = 31
    const val BATTERY_STYLE_LANDSCAPE_BATTERYM = 32
    const val BATTERY_STYLE_LANDSCAPE_BATTERYN = 33
    const val BATTERY_STYLE_LANDSCAPE_BATTERYO = 34
    const val BATTERY_STYLE_CIRCLE = 35
    const val BATTERY_STYLE_DOTTED_CIRCLE = 36
    const val BATTERY_STYLE_FILLED_CIRCLE = 37

    // Xposed force reload overlay
    const val FORCE_RELOAD_PACKAGE_NAME = "com.android.internal.display.cutout.emulation.corner"

    // Xposed settings
    const val RESTART_SYSUI_BEHAVIOR_EXT = "IconifyRestartSysuiBehaviorExtended"

    // Preference keys
    const val UPDATE_SCHEDULE = "iconify_update_schedule"
    const val UPDATE_CHECK_TIME = "iconify_update_check_time"
    const val LAST_UPDATE_CHECK_TIME = "iconify_last_update_check_time"
    const val NEW_UPDATE_VERSION_CODE = "iconify_new_update_version_code"
    const val FIRST_INSTALL = "firstInstall"
    const val UPDATE_DETECTED = "updateDetected"
    const val ON_HOME_PAGE = "onHomePage"
    const val COLORED_BATTERY_SWITCH = "isColoredBatteryEnabled"
    const val COLOR_ACCENT_PRIMARY = "colorAccentPrimary"
    const val COLOR_ACCENT_PRIMARY_LIGHT = "colorAccentPrimaryLight"
    const val COLOR_ACCENT_SECONDARY = "colorAccentSecondary"
    const val COLOR_ACCENT_SECONDARY_LIGHT = "colorAccentSecondaryLight"
    const val CUSTOM_PRIMARY_COLOR_SWITCH = "customPrimaryColor"
    const val CUSTOM_SECONDARY_COLOR_SWITCH = "customSecondaryColor"
    const val QS_ROW_COLUMN_SWITCH = "fabricatedqsRowColumn"
    const val MONET_ENGINE_SWITCH = "customMonet"
    const val QSPANEL_BLUR_SWITCH = "qsBlurSwitch"
    const val AGGRESSIVE_QSPANEL_BLUR_SWITCH = "aggressiveQsBlurSwitch"
    const val UI_CORNER_RADIUS = "uiCornerRadius"
    const val MONET_STYLE = "customMonetStyle"
    const val MONET_PRIMARY_COLOR = "monetPrimaryColor"
    const val MONET_SECONDARY_COLOR = "monetSecondaryColor"
    const val MONET_PRIMARY_ACCENT_SATURATION = "monetPrimaryAccentSaturationValue"
    const val MONET_SECONDARY_ACCENT_SATURATION = "monetSecondaryAccentSaturationValue"
    const val MONET_BACKGROUND_SATURATION = "monetBackgroundSaturationValue"
    const val MONET_BACKGROUND_LIGHTNESS = "monetBackgroundLightnessValue"
    const val MONET_ACCURATE_SHADES = "monetAccurateShades"
    const val PORT_QSTILE_EXPANDED_HEIGHT = "portraitQsTileExpandedHeight"
    const val PORT_QSTILE_NONEXPANDED_HEIGHT = "portraitQsTileNonExpandedHeight"
    const val LAND_QSTILE_EXPANDED_HEIGHT = "landscapeQsTileExpandedHeight"
    const val LAND_QSTILE_NONEXPANDED_HEIGHT = "landscapeQsTileNonExpandedHeight"
    const val SELECTED_SETTINGS_ICONS_COLOR = "selectedSettignsIconsColor"
    const val SELECTED_SETTINGS_ICONS_BG = "selectedSettignsIconsBg"
    const val SELECTED_SETTINGS_ICONS_SHAPE = "selectedSettignsIconsShape"
    const val SELECTED_SETTINGS_ICONS_SIZE = "selectedSettignsIconsSize"
    const val SELECTED_SETTINGS_ICONS_SET = "selectedSettignsIconsSet"
    const val SELECTED_TOAST_FRAME = "selectedToastFrame"
    const val SELECTED_ICON_SHAPE = "selectedIconShape"
    const val RESTART_SYSUI_AFTER_BOOT = "restartSysuiAfterBoot"
    const val VOLUME_PANEL_BACKGROUND_WIDTH = "volumePanelBackgroundWidth"
    const val SELECTED_PROGRESSBAR = "selectedProgressbar"
    const val SELECTED_SWITCH = "selectedSwitch"
    const val COLORED_BATTERY_CHECK = "isColoredBatteryEnabledByUser"
    const val CUSTOM_ACCENT = "customAccentColor"
    const val QS_TEXT_COLOR_VARIANT = "qsTextColorVariant"
    const val QS_TEXT_COLOR_VARIANT_NORMAL = "qsTextColorVariantNormal"
    const val QS_TEXT_COLOR_VARIANT_PIXEL = "qsTextColorVariantPixel"
    const val PORT_QQS_TOP_MARGIN = "portraitQqsTopMargin"
    const val PORT_QS_TOP_MARGIN = "portraitQsTopMargin"
    const val LAND_QQS_TOP_MARGIN = "landscapeQqsTopMargin"
    const val LAND_QS_TOP_MARGIN = "landscapeQsTopMargin"
    const val DYNAMIC_OVERLAY_RESOURCES = "dynamicOverlayResources"
    const val DYNAMIC_OVERLAY_RESOURCES_NIGHT = "dynamicOverlayResourcesNight"
    const val DYNAMIC_OVERLAY_RESOURCES_LAND = "dynamicOverlayResourcesLand"
    const val QS_HIDE_LABEL_SWITCH = "qsHideLabelSwitch"
    const val PROGRESS_WAVE_ANIMATION_SWITCH = "progressWaveAnimationSwitch"
    const val NOTCH_BAR_KILLER_SWITCH = "notchBarKillerSwitch"
    const val TABLET_LANDSCAPE_SWITCH = "tabletLandscapeSwitch"
    const val PILL_SHAPE_SWITCH = "navigationPillShapeSwitch"
    const val NAVBAR_FULL_SCREEN = "navbarfullscreen"
    const val NAVBAR_IMMERSIVE_V1 = "navbarimmersivev1"
    const val NAVBAR_IMMERSIVE_V2 = "navbarimmersivev2"
    const val NAVBAR_IMMERSIVE_V3 = "navbarimmersivev3"
    const val NAVBAR_GCAM_LAG_FIX = "navbargcamlagfix"
    const val NAVBAR_LOW_SENS = "navbarlowsens"
    const val NAVBAR_HIDE_PILL = "navbarhidepill"

    // Weather
    const val WEATHER_SWITCH: String = "weather_switch"
    const val PREF_KEY_UPDATE_STATUS: String = "update_status"
    const val WEATHER_ICON_PACK: String = "weather_icon_pack"
    const val WEATHER_UPDATE_INTERVAL: String = "weather_update_interval"
    const val WEATHER_SHOW_LOCATION: String = "weather_show_location"
    const val WEATHER_SHOW_CONDITION: String = "weather_show_condition"
    const val WEATHER_SHOW_HUMIDITY: String = "weather_show_humidity"
    const val WEATHER_SHOW_WIND: String = "weather_show_wind"
    const val WEATHER_TEXT_SIZE: String = "weather_text_size"
    const val WEATHER_ICON_SIZE: String = "weather_icon_size"
    const val WEATHER_TEXT_COLOR_SWITCH: String = "weather_text_color_switch"
    const val WEATHER_TEXT_COLOR: String = "weather_text_color"
    const val WEATHER_OWM_KEY: String = "owm_key"
    const val WEATHER_UNITS: String = "weather_units"
    const val WEATHER_PROVIDER: String = "weather_provider"
    const val WEATHER_CUSTOM_LOCATION: String = "weather_custom_location_switch"
    const val WEATHER_CUSTOM_LOCATION_PICKER: String = "weather_custom_location_picker"
    const val WEATHER_CUSTOM_MARGINS_TOP: String = "weather_custom_margins_top"
    const val WEATHER_CUSTOM_MARGINS_SIDE: String = "weather_custom_margins_side"
    const val WEATHER_CUSTOM_MARGINS_BOTTOM: String = "weather_custom_margins_bottom"
    const val WEATHER_STYLE: String = "weather_style"
    const val WEATHER_CENTER_VIEW: String = "weather_center_view"

    // Settings
    const val APP_LANGUAGE = "IconifyAppLanguage"
    const val APP_ICON = "IconifyAppIcon"
    const val APP_THEME = "IconifyAppTheme"
    const val AUTO_UPDATE = "IconifyAutoUpdate"
    const val UPDATE_OVER_WIFI = "IconifyUpdateOverWifi"
    const val SHOW_HOME_CARD = "IconifyShowHomeCard"
    const val VIBRATE_UI = "IconifyUiVibration"
    const val XPOSED_ONLY_MODE = "IconifyXposedOnlyMode"
    const val NEW_UPDATE_FOUND = "newUpdateFound"

    var isXposedOnlyMode = getBoolean(XPOSED_ONLY_MODE, true) &&
            !SplashActivity.SKIP_TO_HOMEPAGE_FOR_TESTING

    // Others
    const val BOOT_ID = "boot_id"
    const val VER_CODE = "versionCode"
    const val EASTER_EGG = "iconify_easter_egg"
    const val EXPERIMENTAL_FEATURES = "experimentalFeatures"
    const val ALERT_DIALOG_QSROWCOL = "alertDialogQsRowCol"
}
