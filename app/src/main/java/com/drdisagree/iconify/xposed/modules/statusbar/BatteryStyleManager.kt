package com.drdisagree.iconify.xposed.modules.statusbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_CIRCLE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_CUSTOM_L_LANDSCAPE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_CUSTOM_R_LANDSCAPE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_DEFAULT
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_DOTTED_CIRCLE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_FILLED_CIRCLE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_A
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_B
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_C
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_D
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_E
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_F
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_G
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_H
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_I
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_J
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_K
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_L
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_M
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_N
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERY_O
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_IOS_15
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_IOS_16
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_KIM
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_MIUI_PILL
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_ONE_UI_7
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_LANDSCAPE_SMILEY
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_L_LANDSCAPE_COLOROS
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_L_LANDSCAPE_STYLE_A
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_L_LANDSCAPE_STYLE_B
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_PORTRAIT_AIROO
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_PORTRAIT_CAPSULE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_PORTRAIT_LORN
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_PORTRAIT_MX
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_PORTRAIT_ORIGAMI
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_R_LANDSCAPE_COLOROS
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_R_LANDSCAPE_STYLE_A
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_R_LANDSCAPE_STYLE_B
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_LS_BATTERY_ICON_TAG
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_QS_BATTERY_ICON_TAG
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_SB_BATTERY_ICON_TAG
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.SettingsLibUtils
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.DualToneHandler
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.hideView
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callStaticMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getExtraFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.statusbar.BatteryStyleManager.BatteryView.Companion.getBatteryView
import com.drdisagree.iconify.xposed.modules.statusbar.StatusbarMisc.Companion.getStatusbarColors
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.BatteryDrawable
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.CircleBattery
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.CircleFilledBattery
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBattery
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryA
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryB
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryC
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryColorOS
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryD
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryE
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryF
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryG
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryH
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryI
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryJ
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryK
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryKim
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryL
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryM
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryMIUIPill
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryN
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryO
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryOneUI7
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatterySmiley
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryStyleA
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryStyleB
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryiOS15
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryiOS16
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.PortraitBatteryAiroo
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.PortraitBatteryCapsule
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.PortraitBatteryLorn
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.PortraitBatteryMx
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.PortraitBatteryOrigami
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.RLandscapeBattery
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.RLandscapeBatteryColorOS
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.RLandscapeBatteryStyleA
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.RLandscapeBatteryStyleB
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class BatteryStyleManager(context: Context) : ModPack(context) {

    private var mBatteryStyle = 0
    private var mShowPercentInside = false
    private var mHidePercentage = false
    private var mHideBattery = false
    private var customBatteryEnabled = false
    private var mBatteryWidth = 20
    private var mBatteryHeight = 20
    private var mBatteryMarginLeft = 2
    private var mBatteryMarginTop = 0
    private var mBatteryMarginRight = 2
    private var mBatteryMarginBottom = 0
    private var frameColor = Color.WHITE
    private var mBatteryLayoutReverse = false
    private var mScaledPerimeterAlpha = false
    private var mScaledFillAlpha = false
    private var mRainbowFillColor = false
    private var mCustomBlendColor = false
    private var mCustomChargingColor = Color.BLACK
    private var mCustomFillColor = Color.BLACK
    private var mCustomFillGradColor = Color.BLACK
    private var mCustomPowerSaveColor = Color.BLACK
    private var mCustomPowerSaveFillColor = Color.BLACK
    private var mSwapPercentage = false
    private var mChargingIconEnabled = false
    private var mChargingIconStyle = 0
    private var mChargingIconML = 1
    private var mChargingIconMR = 0
    private var mChargingIconWH = 14
    private var hideDefaultBattery = false
    private var dualStatusbarEnabled = false
    private var linkToCustomColor = false

    private data class BatteryCallbackState(
        val level: Int = 0,
        val isPluggedIn: Boolean = false,
        val isPowerSaveEnabled: Boolean = false,
        val isExtremePowerSaveEnabled: Boolean = false,
        val isBatteryDefenderEnabled: Boolean = false,
        val isStateUnknown: Boolean = false,
        val isIncompatibleCharging: Boolean = false,
    )

    private data class BatteryView(
        val batteryIcon: ImageView,
        val chargingIcon: ImageView,
        val percentageText: TextView
    ) {
        companion object {
            fun ViewGroup.getBatteryView(): BatteryView {
                return BatteryView(
                    batteryIcon = findViewWithTag(BATTERY_ICON_TAG),
                    chargingIcon = findViewWithTag(CHARGING_ICON_TAG),
                    percentageText = findViewWithTag(PERCENTAGE_TEXT_TAG)
                )
            }
        }
    }

    override fun updatePrefs(vararg key: String) {
        val batteryIconStyle = Xprefs.getString(XposedKey.CUSTOM_BATTERY_STYLE).toInt()
        val chargingIconStyle =
            Xprefs.getString(XposedKey.CUSTOM_BATTERY_CHARGING_ICON_STYLE).toInt()

        Xprefs.apply {
            val hidePercentage = getBoolean(XposedKey.CUSTOM_BATTERY_HIDE_PERCENTAGE)
            val defaultInsidePercentage = batteryIconStyle in listOf(
                BATTERY_STYLE_LANDSCAPE_IOS_16,
                BATTERY_STYLE_LANDSCAPE_BATTERY_L,
                BATTERY_STYLE_LANDSCAPE_BATTERY_M,
                BATTERY_STYLE_LANDSCAPE_ONE_UI_7
            )
            val insidePercentage = defaultInsidePercentage ||
                    getBoolean(XposedKey.CUSTOM_BATTERY_INSIDE_PERCENTAGE)
            customBatteryEnabled = batteryIconStyle != BATTERY_STYLE_DEFAULT

            mHidePercentage = hidePercentage || insidePercentage
            mShowPercentInside = insidePercentage && (defaultInsidePercentage || !hidePercentage)
            mHideBattery = getBoolean(XposedKey.CUSTOM_BATTERY_HIDE_BATTERY)
            mBatteryLayoutReverse = getBoolean(XposedKey.CUSTOM_BATTERY_LAYOUT_REVERSE)
            mBatteryWidth = getInt(XposedKey.CUSTOM_BATTERY_WIDTH)
            mBatteryHeight = getInt(XposedKey.CUSTOM_BATTERY_HEIGHT)
            mScaledPerimeterAlpha = getBoolean(XposedKey.CUSTOM_BATTERY_PERIMETER_ALPHA)
            mScaledFillAlpha = getBoolean(XposedKey.CUSTOM_BATTERY_FILL_ALPHA)
            mRainbowFillColor = getBoolean(XposedKey.CUSTOM_BATTERY_RAINBOW_FILL_COLOR)
            mCustomBlendColor = getBoolean(XposedKey.CUSTOM_BATTERY_BLEND_COLOR)
            mCustomChargingColor = getColor(XposedKey.CUSTOM_BATTERY_CHARGING_COLOR)
            mCustomFillColor = getColor(XposedKey.CUSTOM_BATTERY_FILL_COLOR)
            mCustomFillGradColor = getColor(XposedKey.CUSTOM_BATTERY_FILL_GRAD_COLOR)
            mCustomPowerSaveColor = getColor(XposedKey.CUSTOM_BATTERY_POWER_SAVE_INDICATOR_COLOR)
            mCustomPowerSaveFillColor = getColor(XposedKey.CUSTOM_BATTERY_POWER_SAVE_FILL_COLOR)
            mSwapPercentage = getBoolean(XposedKey.CUSTOM_BATTERY_SWAP_PERCENTAGE)
            mBatteryMarginLeft = mContext.toPx(getInt(XposedKey.CUSTOM_BATTERY_MARGIN_LEFT))
            mBatteryMarginTop = mContext.toPx(getInt(XposedKey.CUSTOM_BATTERY_MARGIN_TOP))
            mBatteryMarginRight = mContext.toPx(getInt(XposedKey.CUSTOM_BATTERY_MARGIN_RIGHT))
            mBatteryMarginBottom = mContext.toPx(getInt(XposedKey.CUSTOM_BATTERY_MARGIN_BOTTOM))
            mChargingIconEnabled =
                customBatteryEnabled && getBoolean(XposedKey.CUSTOM_BATTERY_CHARGING_ICON_SWITCH)
            mChargingIconML = getInt(XposedKey.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT)
            mChargingIconMR = getInt(XposedKey.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT)
            mChargingIconWH = getInt(XposedKey.CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT)
            hideDefaultBattery = getBoolean(XposedKey.HIDE_DEFAULT_BATTERY_VIEW)
            dualStatusbarEnabled = getBoolean(XposedKey.DUAL_STATUSBAR)
            linkToCustomColor = getBoolean(XposedKey.STATUSBAR_LINK_TO_CUSTOM_COLOR)
        }

        updateBatteryDrawableIfRequired(batteryIconStyle)
        updateChargingDrawableIfRequired(chargingIconStyle)

        when (key.firstOrNull()) {
            in setOf(
                XposedKey.CUSTOM_BATTERY_STYLE.name,
                XposedKey.CUSTOM_BATTERY_HIDE_BATTERY.name,
                XposedKey.CUSTOM_BATTERY_HIDE_PERCENTAGE.name,
                XposedKey.CUSTOM_BATTERY_INSIDE_PERCENTAGE.name,
                XposedKey.CUSTOM_BATTERY_LAYOUT_REVERSE.name,
                XposedKey.CUSTOM_BATTERY_WIDTH.name,
                XposedKey.CUSTOM_BATTERY_HEIGHT.name,
                XposedKey.CUSTOM_BATTERY_PERIMETER_ALPHA.name,
                XposedKey.CUSTOM_BATTERY_FILL_ALPHA.name,
                XposedKey.CUSTOM_BATTERY_RAINBOW_FILL_COLOR.name,
                XposedKey.CUSTOM_BATTERY_BLEND_COLOR.name,
                XposedKey.CUSTOM_BATTERY_CHARGING_COLOR.name,
                XposedKey.CUSTOM_BATTERY_FILL_COLOR.name,
                XposedKey.CUSTOM_BATTERY_FILL_GRAD_COLOR.name,
                XposedKey.CUSTOM_BATTERY_POWER_SAVE_INDICATOR_COLOR.name,
                XposedKey.CUSTOM_BATTERY_POWER_SAVE_FILL_COLOR.name,
                XposedKey.CUSTOM_BATTERY_SWAP_PERCENTAGE.name,
                XposedKey.CUSTOM_BATTERY_CHARGING_ICON_SWITCH.name,
                XposedKey.CUSTOM_BATTERY_CHARGING_ICON_STYLE.name,
                XposedKey.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT.name,
                XposedKey.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT.name,
                XposedKey.CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT.name,
                XposedKey.CUSTOM_BATTERY_MARGIN_LEFT.name,
                XposedKey.CUSTOM_BATTERY_MARGIN_TOP.name,
                XposedKey.CUSTOM_BATTERY_MARGIN_RIGHT.name,
                XposedKey.CUSTOM_BATTERY_MARGIN_BOTTOM.name
            ) -> {
                refreshBatteryData()
            }
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val batteryControllerImplClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.policy.BatteryControllerImpl")
        val batteryCallbackStateClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.pipeline.battery.data.repository.BatteryCallbackState")
        val phoneStatusBarViewControllerClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.PhoneStatusBarViewController")
        val keyguardStatusBarViewControllerClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.KeyguardStatusBarViewController")
        val shadeHeaderControllerClass =
            findClass("$SYSTEMUI_PACKAGE.shade.ShadeHeaderController")
        val modernStatusBarViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.pipeline.shared.ui.view.ModernStatusBarView")
        val keyguardStatusBarViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.KeyguardStatusBarView")
        val configurationControllerListenerClass =
            findClass($$"$$SYSTEMUI_PACKAGE.shade.ShadeHeaderController$configurationControllerListener$1")
        val darkIconDispatcherClass = findClass("$SYSTEMUI_PACKAGE.plugins.DarkIconDispatcher")

        batteryControllerImplClass
            .hookMethod(
                "onReceive",
                "dispatchSafeChange",
                "fireWirelessChargingChanged",
                "fireBatteryLevelChanged",
                "fireBatteryUnknownStateChanged",
                "firePowerSaveChanged",
                "fireIsBatteryDefenderChanged",
                "fireIsIncompatibleChargingChanged",
                "updatePowerSave",
            )
            .runAfter { param ->
                val mLevel = param.thisObject.getField("mLevel") as Int
                val isPluggedIn = param.thisObject.getField("mPluggedIn") as Boolean
                val isCharging = param.thisObject.getFieldSilently("mCharging") as? Boolean
                    ?: false
                val isWirelessCharging =
                    param.thisObject.getField("mWirelessCharging") as Boolean
                val isBatteryDefenderEnabled =
                    param.thisObject.getField("mIsBatteryDefender") as Boolean
                val isStateUnknown = param.thisObject.getField("mStateUnknown") as Boolean
                val isPowerSaveEnabled = param.thisObject.getField("mPowerSave") as Boolean
                val isExtremePowerSaveEnabled =
                    param.thisObject.getFieldSilently("mAodPowerSave") as? Boolean
                        ?: false
                val isIncompatibleCharging =
                    param.thisObject.getField("mIsIncompatibleCharging") as Boolean

                batteryCallbackState = batteryCallbackState.copy(
                    level = mLevel,
                    isPluggedIn = isPluggedIn || isCharging || isWirelessCharging,
                    isPowerSaveEnabled = isPowerSaveEnabled,
                    isExtremePowerSaveEnabled = isExtremePowerSaveEnabled,
                    isBatteryDefenderEnabled = isBatteryDefenderEnabled,
                    isStateUnknown = isStateUnknown,
                    isIncompatibleCharging = isIncompatibleCharging
                )

                refreshBatteryData()
            }

        batteryCallbackStateClass
            .hookConstructor()
            .runAfter { refreshBatteryData() }

        phoneStatusBarViewControllerClass
            .hookMethod("onViewAttached")
            .runAfter { param ->
                val mView = param.thisObject.getField("mView") as View

                var mBatteryMeterView =
                    mView.findViewWithTag<ViewGroup?>(ICONIFY_SB_BATTERY_ICON_TAG)

                if (mBatteryMeterView == null) {
                    mBatteryMeterView = createBatteryMeterView(ICONIFY_SB_BATTERY_ICON_TAG)

                    val systemIconsContainer = mView.findViewById<ViewGroup>(
                        mContext.resources.getIdentifier(
                            "system_icons",
                            "id",
                            SYSTEMUI_PACKAGE
                        )
                    )
                    systemIconsContainer.addView(mBatteryMeterView, -1)

                    batteryViews.add(mBatteryMeterView)
                }

                refreshBatteryData()

                mView.hideStockBatteryIcon()
            }

        phoneStatusBarViewControllerClass
            .hookMethod("onViewDetached")
            .runAfter { param ->
                val mView = param.thisObject.getField("mView") as View

                mView.findViewWithTag<ViewGroup?>(ICONIFY_SB_BATTERY_ICON_TAG)?.let {
                    batteryViews.remove(it)
                }
            }

        keyguardStatusBarViewControllerClass
            .hookMethod("onViewAttached")
            .runAfter { param ->
                val mView = param.thisObject.getField("mView") as View

                var mBatteryMeterView =
                    mView.findViewWithTag<ViewGroup?>(ICONIFY_LS_BATTERY_ICON_TAG)

                if (mBatteryMeterView == null) {
                    mBatteryMeterView = createBatteryMeterView(ICONIFY_LS_BATTERY_ICON_TAG)

                    val systemIconsContainer = mView.findViewById<ViewGroup>(
                        mContext.resources.getIdentifier(
                            "system_icons",
                            "id",
                            SYSTEMUI_PACKAGE
                        )
                    )
                    systemIconsContainer.addView(mBatteryMeterView, -1)

                    batteryViews.add(mBatteryMeterView)
                }

                refreshBatteryData()

                mView.hideStockBatteryIcon()

                mView.callMethod(
                    "onThemeChanged",
                    param.thisObject.getField("mTintedIconManager")
                )
            }

        keyguardStatusBarViewControllerClass
            .hookMethod("onViewDetached")
            .runAfter { param ->
                val mView = param.thisObject.getField("mView") as View

                mView.findViewWithTag<ViewGroup?>(ICONIFY_LS_BATTERY_ICON_TAG)?.let {
                    batteryViews.remove(it)
                }
            }

        shadeHeaderControllerClass
            .hookMethod("onViewAttached")
            .runAfter { param ->
                var mView = param.thisObject.getField("mView") as View

                param.thisObject.getExtraFieldSilently("mQsIconsContainer")?.let {
                    // Enabling custom header clock moves the clock to a different view group
                    mView = it as View
                }

                val header = param.thisObject.getField("header") as View
                val context = header.context

                var mBatteryMeterView =
                    mView.findViewWithTag<ViewGroup?>(ICONIFY_QS_BATTERY_ICON_TAG)

                if (mBatteryMeterView == null) {
                    mBatteryMeterView = createBatteryMeterView(ICONIFY_QS_BATTERY_ICON_TAG)

                    val systemIconsContainer = mView.findViewById<ViewGroup>(
                        context.resources.getIdentifier(
                            "hover_system_icons_container",
                            "id",
                            SYSTEMUI_PACKAGE
                        )
                    )
                    systemIconsContainer.addView(mBatteryMeterView, -1)

                    batteryViews.add(mBatteryMeterView)
                }

                val (fgColor, bgColor) = getQsIconColors(context)

                applyBatteryColors(
                    tag = ICONIFY_QS_BATTERY_ICON_TAG,
                    fgColor = fgColor,
                    bgColor = bgColor,
                    singleToneColor = fgColor
                )

                refreshBatteryData()

                mView.hideStockBatteryIcon(onQS = true)
            }

        shadeHeaderControllerClass
            .hookMethod("onViewDetached")
            .runAfter { param ->
                val mView = param.thisObject.getField("mView") as View

                mView.findViewWithTag<ViewGroup?>(ICONIFY_QS_BATTERY_ICON_TAG)?.let {
                    batteryViews.remove(it)
                }
            }

        modernStatusBarViewClass
            .hookMethod("onDarkChangedWithContrast")
            .runAfter { param ->
                val areas = param.args[0]
                val tint = param.args[1] as Int

                val newTint = darkIconDispatcherClass.callStaticMethod(
                    "getTint",
                    areas,
                    param.thisObject,
                    tint
                ) as Int

                val (statusbarColorLight, statusbarColorDark) = getStatusbarColors(mContext)
                val statusbarColor =
                    if (ColorUtils.calculateLuminance(newTint) > 0.5)
                        statusbarColorLight
                    else
                        statusbarColorDark

                val nonAdaptedSingleToneColor =
                    if (linkToCustomColor)
                        DualToneHandler.getSingleColorWithTint(statusbarColor)
                    else
                        DualToneHandler.getSingleColorWithTint(newTint)
                val nonAdaptedForegroundColor =
                    if (linkToCustomColor)
                        DualToneHandler.getFillColorWithTint(statusbarColor)
                    else
                        DualToneHandler.getFillColorWithTint(newTint)
                val nonAdaptedBackgroundColor =
                    if (linkToCustomColor)
                        DualToneHandler.getBackgroundColorWithTint(statusbarColor)
                    else
                        DualToneHandler.getBackgroundColorWithTint(newTint)

                applyBatteryColors(
                    tag = ICONIFY_SB_BATTERY_ICON_TAG,
                    fgColor = nonAdaptedForegroundColor,
                    bgColor = nonAdaptedBackgroundColor,
                    singleToneColor = nonAdaptedSingleToneColor
                )
            }

        keyguardStatusBarViewClass
            .hookMethod("onThemeChanged")
            .runAfter { param ->
                val tintAreas = param.thisObject.getField("mEmptyTintRect")
                val textColor: Int = SettingsLibUtils.getColorAttrDefaultColor(
                    mContext,
                    mContext.resources.getIdentifier(
                        "wallpaperTextColor",
                        "attr",
                        SYSTEMUI_PACKAGE
                    )
                )
                val lockscreenIntensity =
                    if (ColorUtils.calculateLuminance(textColor) > 0.5) 0f else 1f

                val isInAreas = darkIconDispatcherClass.callStaticMethod(
                    "isInAreas",
                    tintAreas,
                    param.thisObject
                ) as Boolean

                val darkIntensity = if (isInAreas) lockscreenIntensity else 0f

                val (statusbarColorLight, statusbarColorDark) = getStatusbarColors(mContext)
                val statusbarColor =
                    if (darkIntensity > 0.5) statusbarColorLight else statusbarColorDark

                val nonAdaptedSingleToneColor =
                    if (linkToCustomColor)
                        DualToneHandler.getSingleColorWithTint(darkIntensity, statusbarColor)
                    else
                        DualToneHandler.getSingleColor(darkIntensity)
                val nonAdaptedForegroundColor =
                    if (linkToCustomColor)
                        DualToneHandler.getFillColorWithTint(darkIntensity, statusbarColor)
                    else
                        DualToneHandler.getFillColor(darkIntensity)
                val nonAdaptedBackgroundColor =
                    if (linkToCustomColor)
                        DualToneHandler.getBackgroundColorWithTint(darkIntensity, statusbarColor)
                    else
                        DualToneHandler.getBackgroundColor(darkIntensity)

                applyBatteryColors(
                    tag = ICONIFY_LS_BATTERY_ICON_TAG,
                    fgColor = nonAdaptedForegroundColor,
                    bgColor = nonAdaptedBackgroundColor,
                    singleToneColor = nonAdaptedSingleToneColor
                )
            }

        configurationControllerListenerClass
            .hookMethod("onThemeChanged", "onUiModeChanged")
            .runAfter { param ->
                var fieldName: String? = null

                for (field in param.thisObject.javaClass.declaredFields) {
                    field.isAccessible = true

                    if (field.type.simpleName == "ShadeHeaderController") {
                        fieldName = field.name
                        break
                    }
                }

                fieldName ?: run {
                    log($$"Could not find ShadeHeaderController field inside ShadeHeaderController$configurationControllerListener class")
                    return@runAfter
                }

                val shadeHeaderController = param.thisObject.getField(fieldName)
                val header = shadeHeaderController.getField("header") as View
                val context = header.context

                val (fgColor, bgColor) = getQsIconColors(context)

                applyBatteryColors(
                    tag = ICONIFY_QS_BATTERY_ICON_TAG,
                    fgColor = fgColor,
                    bgColor = bgColor,
                    singleToneColor = fgColor
                )
            }
    }

    private fun createBatteryMeterView(tag: String): LinearLayout {
        val parent = LinearLayout(mContext).apply {
            this.tag = tag
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            orientation = LinearLayout.HORIZONTAL
        }

        val batteryIconView = ImageView(mContext).apply {
            this.tag = BATTERY_ICON_TAG
            layoutParams = LinearLayout.LayoutParams(
                mContext.toPx(mBatteryWidth),
                mContext.toPx(mBatteryHeight)
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        val percentageTextView = TextView(mContext).apply {
            this.tag = PERCENTAGE_TEXT_TAG
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "%d%%".format(batteryCallbackState.level)
            val styleResId = mContext.resources.getIdentifier(
                when (tag) {
                    ICONIFY_QS_BATTERY_ICON_TAG -> "TextAppearance.QS.Status"
                    else -> "TextAppearance.StatusBar.Default"
                },
                "style",
                SYSTEMUI_PACKAGE
            )
            if (styleResId != 0) setTextAppearance(styleResId)
            else log(
                $$"Could not find text appearance resource for battery percentage text view with id: $${
                    when (tag) {
                        ICONIFY_QS_BATTERY_ICON_TAG -> "TextAppearance.QS.Status"
                        else -> "TextAppearance.StatusBar.Default"
                    }
                }"
            )
        }
        val chargingIconView = ImageView(mContext).apply {
            this.tag = CHARGING_ICON_TAG
            layoutParams = LinearLayout.LayoutParams(
                mContext.toPx(mChargingIconWH),
                mContext.toPx(mChargingIconWH)
            ).apply {
                setMargins(
                    mContext.toPx(mChargingIconML),
                    0,
                    mContext.toPx(mChargingIconMR),
                    0
                )
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        parent.addView(batteryIconView)
        parent.addView(percentageTextView)
        parent.addView(chargingIconView)

        return parent
    }

    private fun updateBatteryDrawableIfRequired(batteryStyle: Int) {
        if (batteryStyle == mBatteryStyle) return

        mBatteryStyle = batteryStyle

        for (batteryView in batteryViews) {
            val mBatteryView = batteryView.getBatteryView()
            val mBatteryIconView = mBatteryView.batteryIcon

            mBatteryIconView.setImageDrawable(getNewBatteryDrawable(mContext))
        }
    }

    private fun updateChargingDrawableIfRequired(chargingIconStyle: Int) {
        if (chargingIconStyle == mChargingIconStyle) return

        mChargingIconStyle = chargingIconStyle

        for (batteryView in batteryViews) {
            val mBatteryView = batteryView.getBatteryView()
            val mChargingIconView = mBatteryView.chargingIcon

            mChargingIconView.setImageDrawable(getNewChargingDrawable())
        }
    }

    private fun refreshBatteryData() {
        for (batteryView in batteryViews) {
            batteryView.visibility = if (customBatteryEnabled) {
                // Handle a bug where statusbar battery is duplicated on lockscreen with dual statusbar enabled
                if (dualStatusbarEnabled &&
                    DualStatusbar.isKeyguardShown &&
                    batteryView.tag == ICONIFY_SB_BATTERY_ICON_TAG
                ) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            } else {
                View.GONE
            }

            val mBatteryView = batteryView.getBatteryView()
            val mBatteryIconView = mBatteryView.batteryIcon
            val mChargingIconView = mBatteryView.chargingIcon
            val mPercentageView = mBatteryView.percentageText

            val mBatteryDrawable: BatteryDrawable = (mBatteryIconView.drawable as? BatteryDrawable)
                ?: getNewBatteryDrawable(mContext)?.also {
                    mBatteryIconView.setImageDrawable(it)
                } ?: continue

            val level = batteryCallbackState.level
            val isCharging = batteryCallbackState.isPluggedIn &&
                    !batteryCallbackState.isIncompatibleCharging
            val isPowerSave = batteryCallbackState.isPowerSaveEnabled ||
                    batteryCallbackState.isExtremePowerSaveEnabled

            batteryView.post {
                mBatteryDrawable.apply {
                    setBatteryLevel(level)
                    setChargingEnabled(isCharging)
                    setPowerSavingEnabled(isPowerSave)
                    updateBatteryColors(this)
                    setShowPercentEnabled(mShowPercentInside)
                }
                mBatteryIconView.apply {
                    updateBatteryIconView(this)
                }
                mChargingIconView.apply {
                    drawable ?: getNewChargingDrawable().also {
                        mChargingIconView.setImageDrawable(it)
                    }
                    updateChargingIconView(this, isCharging)
                }
                mPercentageView.apply {
                    text = "%d%%".format(level)
                    visibility = if (mHidePercentage) View.GONE else View.VISIBLE
                }
                updateFlipper(batteryView)
            }
        }
    }

    private fun getQsIconColors(context: Context): Pair<Int, Int> {
        val fgColor = context.getColor(
            context.resources.getIdentifier(
                "shade_header_text_color",
                "color",
                SYSTEMUI_PACKAGE
            )
        )
        val bgColor = context.getColor(
            context.resources.getIdentifier(
                "shade_header_text_color_bg",
                "color",
                SYSTEMUI_PACKAGE
            )
        )
        return Pair(fgColor, bgColor)
    }

    private fun applyBatteryColors(
        tag: String,
        fgColor: Int,
        bgColor: Int,
        singleToneColor: Int
    ) {
        batteryViews
            .filter { it.tag == tag }
            .forEach { batteryView ->
                val mBatteryView = batteryView.getBatteryView()
                val mBatteryIconView = mBatteryView.batteryIcon
                val mChargingIconView = mBatteryView.chargingIcon
                val mPercentageView = mBatteryView.percentageText

                (mBatteryIconView.drawable as? BatteryDrawable)?.setColors(
                    fgColor,
                    bgColor,
                    singleToneColor
                )
                mChargingIconView.setColorFilter(singleToneColor, PorterDuff.Mode.SRC_IN)
                mPercentageView.setTextColor(singleToneColor)
            }
    }

    private fun updateBatteryIconView(mBatteryIconView: ImageView) {
        val lp = (mBatteryIconView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            width = mContext.toPx(mBatteryWidth)
            height = mContext.toPx(mBatteryHeight)

            setMargins(
                mBatteryMarginLeft,
                mBatteryMarginTop,
                mBatteryMarginRight,
                mBatteryMarginBottom
            )
        }

        mBatteryIconView.apply {
            layoutParams = lp
            updateBatteryRotation(this)
            visibility = if (mHideBattery) View.GONE else View.VISIBLE
        }
    }

    private fun updateChargingIconView(
        mChargingIconView: ImageView,
        isCharging: Boolean
    ) {
        val lp = (mChargingIconView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            width = mContext.toPx(mChargingIconWH)
            height = mContext.toPx(mChargingIconWH)

            setMargins(
                mContext.toPx(mChargingIconML),
                0,
                mContext.toPx(mChargingIconMR),
                0
            )
        }

        mChargingIconView.apply {
            layoutParams = lp
            visibility = if (isCharging && mChargingIconEnabled) View.VISIBLE else View.GONE
        }
    }

    private fun updateFlipper(batteryView: ViewGroup) {
        val mBatteryView = batteryView.getBatteryView()
        val mBatteryIconView = mBatteryView.batteryIcon
        val mChargingIconView = mBatteryView.chargingIcon
        val mPercentageView = mBatteryView.percentageText

        val batteryIndex = batteryView.indexOfChild(mBatteryIconView)
        val percentageIndex = batteryView.indexOfChild(mPercentageView)
        val chargingIndex = batteryView.indexOfChild(mChargingIconView)

        if (batteryIndex == -1 || percentageIndex == -1 || chargingIndex == -1) {
            return
        }

        val isCurrentlySwapped = percentageIndex < batteryIndex

        if (isCurrentlySwapped == mSwapPercentage) return

        if (mSwapPercentage) {
            batteryView.removeView(mPercentageView)
            batteryView.removeView(mBatteryIconView)

            batteryView.addView(mPercentageView, 0)
            batteryView.addView(mBatteryIconView, 1)
        } else {
            batteryView.removeView(mBatteryIconView)
            batteryView.removeView(mPercentageView)

            batteryView.addView(mBatteryIconView, 0)
            batteryView.addView(mPercentageView, 1)
        }
    }

    private fun updateBatteryRotation(mBatteryIconView: ImageView) {
        mBatteryIconView.rotation = if (mBatteryLayoutReverse) {
            180
        } else {
            0
        }.toFloat()
    }

    private fun updateBatteryColors(mBatteryDrawable: BatteryDrawable) {
        mBatteryDrawable.customizeBatteryDrawable(
            mBatteryLayoutReverse,
            mScaledPerimeterAlpha,
            mScaledFillAlpha,
            mCustomBlendColor,
            mRainbowFillColor,
            mCustomFillColor,
            mCustomFillGradColor,
            mCustomChargingColor,
            mCustomPowerSaveColor,
            mCustomPowerSaveFillColor,
            mChargingIconEnabled
        )
    }

    private fun View.hideStockBatteryIcon(onQS: Boolean = false) {
        if (!customBatteryEnabled && !hideDefaultBattery) return

        findViewById<View?>(
            mContext.resources.getIdentifier(
                "battery",
                "id",
                SYSTEMUI_PACKAGE
            )
        )?.hideView()

        val systemIconsView = findViewById<ViewGroup>(
            mContext.resources.getIdentifier(
                if (onQS) "hover_system_icons_container" else "system_icons",
                "id",
                SYSTEMUI_PACKAGE
            )
        )

        for (i in systemIconsView.childCount - 1 downTo 0) {
            val child = systemIconsView.getChildAt(i)
            if (child.javaClass.simpleName == "ComposeView") {
                child.hideView()
                break
            }
        }
    }

    private fun getNewBatteryDrawable(context: Context): BatteryDrawable? {
        return when (mBatteryStyle) {
            BATTERY_STYLE_CUSTOM_R_LANDSCAPE -> RLandscapeBattery(context, frameColor)
            BATTERY_STYLE_CUSTOM_L_LANDSCAPE -> LandscapeBattery(context, frameColor)
            BATTERY_STYLE_PORTRAIT_CAPSULE -> PortraitBatteryCapsule(context, frameColor)
            BATTERY_STYLE_PORTRAIT_LORN -> PortraitBatteryLorn(context, frameColor)
            BATTERY_STYLE_PORTRAIT_MX -> PortraitBatteryMx(context, frameColor)
            BATTERY_STYLE_PORTRAIT_AIROO -> PortraitBatteryAiroo(context, frameColor)
            BATTERY_STYLE_R_LANDSCAPE_STYLE_A -> RLandscapeBatteryStyleA(context, frameColor)
            BATTERY_STYLE_L_LANDSCAPE_STYLE_A -> LandscapeBatteryStyleA(context, frameColor)
            BATTERY_STYLE_R_LANDSCAPE_STYLE_B -> RLandscapeBatteryStyleB(context, frameColor)
            BATTERY_STYLE_L_LANDSCAPE_STYLE_B -> LandscapeBatteryStyleB(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_IOS_15 -> LandscapeBatteryiOS15(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_IOS_16 -> LandscapeBatteryiOS16(context, frameColor)
            BATTERY_STYLE_PORTRAIT_ORIGAMI -> PortraitBatteryOrigami(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_SMILEY -> LandscapeBatterySmiley(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_MIUI_PILL -> LandscapeBatteryMIUIPill(context, frameColor)
            BATTERY_STYLE_L_LANDSCAPE_COLOROS -> LandscapeBatteryColorOS(context, frameColor)
            BATTERY_STYLE_R_LANDSCAPE_COLOROS -> RLandscapeBatteryColorOS(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_BATTERY_A -> LandscapeBatteryA(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_BATTERY_B -> LandscapeBatteryB(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_BATTERY_C -> LandscapeBatteryC(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_BATTERY_D -> LandscapeBatteryD(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_BATTERY_E -> LandscapeBatteryE(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_BATTERY_F -> LandscapeBatteryF(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_BATTERY_G -> LandscapeBatteryG(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_BATTERY_H -> LandscapeBatteryH(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_BATTERY_I -> LandscapeBatteryI(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_BATTERY_J -> LandscapeBatteryJ(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_BATTERY_K -> LandscapeBatteryK(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_BATTERY_L -> LandscapeBatteryL(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_BATTERY_M -> LandscapeBatteryM(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_BATTERY_N -> LandscapeBatteryN(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_BATTERY_O -> LandscapeBatteryO(context, frameColor)
            BATTERY_STYLE_FILLED_CIRCLE -> CircleFilledBattery(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_KIM -> LandscapeBatteryKim(context, frameColor)
            BATTERY_STYLE_LANDSCAPE_ONE_UI_7 -> LandscapeBatteryOneUI7(context, frameColor)
            BATTERY_STYLE_CIRCLE,
            BATTERY_STYLE_DOTTED_CIRCLE -> CircleBattery(context, frameColor).apply {
                setMeterStyle(mBatteryStyle)
            }

            else -> null
        }
    }

    private fun getNewChargingDrawable(): Drawable? {
        fun getDrawable(@DrawableRes res: Int): Drawable? {
            return ResourcesCompat.getDrawable(modRes, res, mContext.theme)
        }

        return when (mChargingIconStyle) {
            0 -> getDrawable(R.drawable.ic_charging_bold)
            1 -> getDrawable(R.drawable.ic_charging_asus)
            2 -> getDrawable(R.drawable.ic_charging_buddy)
            3 -> getDrawable(R.drawable.ic_charging_evplug)
            4 -> getDrawable(R.drawable.ic_charging_idc)
            5 -> getDrawable(R.drawable.ic_charging_ios)
            6 -> getDrawable(R.drawable.ic_charging_koplak)
            7 -> getDrawable(R.drawable.ic_charging_miui)
            8 -> getDrawable(R.drawable.ic_charging_mmk)
            9 -> getDrawable(R.drawable.ic_charging_moto)
            10 -> getDrawable(R.drawable.ic_charging_nokia)
            11 -> getDrawable(R.drawable.ic_charging_plug)
            12 -> getDrawable(R.drawable.ic_charging_powercable)
            13 -> getDrawable(R.drawable.ic_charging_powercord)
            14 -> getDrawable(R.drawable.ic_charging_powerstation)
            15 -> getDrawable(R.drawable.ic_charging_realme)
            16 -> getDrawable(R.drawable.ic_charging_soak)
            17 -> getDrawable(R.drawable.ic_charging_stres)
            18 -> getDrawable(R.drawable.ic_charging_strip)
            19 -> getDrawable(R.drawable.ic_charging_usbcable)
            20 -> getDrawable(R.drawable.ic_charging_xiaomi)
            else -> null
        }
    }

    companion object {
        private val batteryViews = mutableSetOf<ViewGroup>()
        private var batteryCallbackState = BatteryCallbackState()
        private const val BATTERY_ICON_TAG = "battery_icon"
        private const val CHARGING_ICON_TAG = "charging_con"
        private const val PERCENTAGE_TEXT_TAG = "percentage_text"
    }
}