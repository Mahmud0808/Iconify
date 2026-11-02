package com.drdisagree.iconify.xposed.modules.misc

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.core.PackageParam
import com.drdisagree.iconify.xposed.core.hook.hookConstructor
import com.drdisagree.iconify.xposed.core.hook.hookMethod
import com.drdisagree.iconify.xposed.core.hook.hookMethodMatchPattern
import com.drdisagree.iconify.xposed.core.hook.proceedWithArgs
import com.drdisagree.iconify.xposed.core.layout.hookLayout
import com.drdisagree.iconify.xposed.core.reflect.findClass
import com.drdisagree.iconify.xposed.core.reflect.getField
import com.drdisagree.iconify.xposed.core.reflect.getFieldSilently
import com.drdisagree.iconify.xposed.core.reflect.setField
import com.drdisagree.iconify.xposed.core.reflect.setFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ComposeViewHost
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs

@SuppressLint("DiscouragedApi")
class Miscellaneous(context: Context) : ModPack(context) {

    private var hideQsCarrierGroup = false
    private var hideStatusIcons = false
    private var hideDataDisabledIcon = false
    private var mobileSignalControllerParam: Any? = null
    private var coloredStatusbarOverlayEnabled = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            hideQsCarrierGroup = getBoolean(XposedKey.QS_PANEL_HIDE_CARRIER)
            hideStatusIcons = getBoolean(XposedKey.HIDE_STATUS_ICONS)
            hideDataDisabledIcon = false
            coloredStatusbarOverlayEnabled = false
        }

        when (key.firstOrNull()) {
            XposedKey.QS_PANEL_HIDE_CARRIER.name -> hideQSCarrierGroup()

            //            HIDE_DATA_DISABLED_ICON -> mobileSignalControllerParam.callMethod("updateTelephony")
        }
    }

    override fun handleLoadPackage(packageParam: PackageParam) {
        hideElements()
        hideQSCarrierGroup()
        hideDataDisabledIcon()
        fixRotationViewColor()
    }

    private fun hideElements() {
        val quickStatusBarHeader = findClass("$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader", suppressError = true)

        quickStatusBarHeader
            .hookMethod("onFinishInflate")
            .runAfter { chain, _ ->
                if (hideStatusIcons) {
                    (chain.thisObject.getFieldSilently("mDateView") as? View)?.apply {
                        layoutParams.height = 0
                        layoutParams.width = 0
                        visibility = View.INVISIBLE
                    }

                    (chain.thisObject.getFieldSilently("mClockDateView") as? TextView)?.apply {
                        visibility = View.INVISIBLE
                        setTextAppearance(0)
                        setTextColor(0)
                    }

                    (chain.thisObject.getFieldSilently("mClockView") as? TextView)?.apply {
                        visibility = View.INVISIBLE
                        setTextAppearance(0)
                        setTextColor(0)
                    }
                }

                if (hideStatusIcons || hideQsCarrierGroup) {
                    val mQSCarriers = chain.thisObject.getFieldSilently("mQSCarriers") as? View
                    mQSCarriers?.visibility = View.INVISIBLE
                }
            }

        val shadeHeaderControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.shade.LargeScreenShadeHeaderController",
            "$SYSTEMUI_PACKAGE.shade.ShadeHeaderController"
        )

        shadeHeaderControllerClass
            .hookMethod("onInit")
            .runAfter { chain, _ ->
                if (hideStatusIcons) {
                    val iconContainer = chain.thisObject.getFieldSilently(
                        "iconContainer"
                    ) as? LinearLayout
                    (iconContainer?.parent as? ViewGroup)?.removeView(iconContainer)

                    val batteryIcon = chain.thisObject.getFieldSilently(
                        "batteryIcon"
                    ) as? LinearLayout
                    (batteryIcon?.parent as? ViewGroup)?.removeView(batteryIcon)
                }

                if (hideStatusIcons || hideQsCarrierGroup) {
                    val qsCarrierGroup = chain.thisObject.getFieldSilently(
                        "qsCarrierGroup"
                    ) as? LinearLayout
                    (qsCarrierGroup?.parent as? ViewGroup)?.removeView(qsCarrierGroup)

                    val mShadeCarrierGroup = chain.thisObject.getFieldSilently(
                        "mShadeCarrierGroup"
                    ) as? LinearLayout
                    (mShadeCarrierGroup?.parent as? ViewGroup)?.removeView(mShadeCarrierGroup)
                }
            }

        val shadeHeaderClass = findClass(
            "$SYSTEMUI_PACKAGE.shade.ui.composable.ShadeHeaderKt",
            suppressError = true
        )

        shadeHeaderClass
            .hookMethod("ShadeCarrierGroup")
            .suppressError()
            .intercept { chain ->
                if (hideStatusIcons || hideQsCarrierGroup) ComposeViewHost.proceedHidingModifier(chain) else chain.proceed()
            }

        shadeHeaderClass
            .hookMethod("StatusIcons")
            .suppressError()
            .intercept { chain ->
                if (hideStatusIcons) ComposeViewHost.proceedHidingModifier(chain) else chain.proceed()
            }

        shadeHeaderClass
            .hookMethodMatchPattern("BatteryInfo(-.*)?")
            .suppressError()
            .intercept { chain ->
                if (hideStatusIcons) ComposeViewHost.proceedHidingModifier(chain) else chain.proceed()
            }

        val axQuickSettingsHeaderClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.ax.ui.header.AxQuickSettingsHeaderKt",
            suppressError = true
        )

        axQuickSettingsHeaderClass
            .hookMethod("AxStatusIcons")
            .suppressError()
            .intercept { chain ->
                if (hideStatusIcons) ComposeViewHost.proceedHidingModifier(chain) else chain.proceed()
            }

        axQuickSettingsHeaderClass
            .hookMethodMatchPattern("AxBatteryInfo(-.*)?")
            .suppressError()
            .intercept { chain ->
                if (hideStatusIcons) ComposeViewHost.proceedHidingModifier(chain) else chain.proceed()
            }
    }

    private fun hideDataDisabledIcon() {
        val mobileSignalControllerClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.connectivity.MobileSignalController")
        val signalIconModelCellularClass = findClass(
            $$"$$SYSTEMUI_PACKAGE.statusbar.pipeline.mobile.domain.model.SignalIconModel$Cellular",
            suppressError = true
        )

        mobileSignalControllerClass
            .hookMethod("updateTelephony")
            .intercept { chain ->
                val controller = chain.thisObject
                if (mobileSignalControllerParam == null) {
                    mobileSignalControllerParam = controller
                }

                if (!hideDataDisabledIcon) return@intercept chain.proceed()

                val config = controller.getField("mConfig")
                val alwaysShowDataRatIcon = config.getField("alwaysShowDataRatIcon") as Boolean
                config.setField("alwaysShowDataRatIcon", true)

                val mDataDisabledIcon = try {
                    (controller.getField("mDataDisabledIcon") as Boolean).also {
                        controller.setField("mDataDisabledIcon", false)
                    }
                } catch (_: Throwable) {
                    null
                }

                try {
                    chain.proceed()
                } finally {
                    config.setField("alwaysShowDataRatIcon", alwaysShowDataRatIcon)
                    mDataDisabledIcon?.let {
                        controller.setFieldSilently("mDataDisabledIcon", it)
                    }
                }
            }

        signalIconModelCellularClass
            .hookConstructor()
            .runAfter { chain, _ ->
                if (!hideDataDisabledIcon) return@runAfter

                chain.thisObject.setField("showExclamationMark", false)
            }
    }

    private fun hideQSCarrierGroup() {

        hookLayout()
            .packageName(SYSTEMUI_PACKAGE)
            .resource("layout", "quick_qs_status_icons")
            .suppressError()
            .run { liparam ->
                if (!hideQsCarrierGroup) return@run

                liparam.view.findViewById<LinearLayout>(
                    liparam.res.getIdentifier(
                        "carrier_group",
                        "id",
                        mContext.packageName
                    )
                ).apply {
                    layoutParams.height = 0
                    layoutParams.width = 0
                    minimumWidth = 0
                    visibility = View.INVISIBLE
                }
            }
    }

    private fun fixRotationViewColor() {
        val floatingRotationButtonClass =
            findClass("$SYSTEMUI_PACKAGE.shared.rotation.FloatingRotationButton")

        floatingRotationButtonClass
            .hookMethod("updateIcon")
            .intercept { chain ->
                chain.proceedWithArgs { args ->
                    if (coloredStatusbarOverlayEnabled) {
                        args[1] = Color.BLACK
                    }
                }
            }
    }
}