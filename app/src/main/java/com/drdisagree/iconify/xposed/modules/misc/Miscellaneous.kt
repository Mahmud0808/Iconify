package com.drdisagree.iconify.xposed.modules.misc

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.XResources
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.HookRes.Companion.resParams
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookLayout
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setFieldSilently
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

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

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        hideElements()
        hideQSCarrierGroup()
        hideDataDisabledIcon()
        fixRotationViewColor()
    }

    private fun hideElements() {
        val quickStatusBarHeader = findClass("$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader")

        quickStatusBarHeader
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                if (hideStatusIcons) {
                    (param.thisObject.getFieldSilently("mDateView") as? View)?.apply {
                        layoutParams.height = 0
                        layoutParams.width = 0
                        visibility = View.INVISIBLE
                    }

                    (param.thisObject.getFieldSilently("mClockDateView") as? TextView)?.apply {
                        visibility = View.INVISIBLE
                        setTextAppearance(0)
                        setTextColor(0)
                    }

                    (param.thisObject.getFieldSilently("mClockView") as? TextView)?.apply {
                        visibility = View.INVISIBLE
                        setTextAppearance(0)
                        setTextColor(0)
                    }
                }

                if (hideStatusIcons || hideQsCarrierGroup) {
                    val mQSCarriers = param.thisObject.getFieldSilently("mQSCarriers") as? View
                    mQSCarriers?.visibility = View.INVISIBLE
                }
            }

        val shadeHeaderControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.shade.LargeScreenShadeHeaderController",
            "$SYSTEMUI_PACKAGE.shade.ShadeHeaderController"
        )

        shadeHeaderControllerClass
            .hookMethod("onInit")
            .runAfter { param ->
                if (hideStatusIcons) {
                    val iconContainer = param.thisObject.getFieldSilently(
                        "iconContainer"
                    ) as? LinearLayout
                    (iconContainer?.parent as? ViewGroup)?.removeView(iconContainer)

                    val batteryIcon = param.thisObject.getFieldSilently(
                        "batteryIcon"
                    ) as? LinearLayout
                    (batteryIcon?.parent as? ViewGroup)?.removeView(batteryIcon)
                }

                if (hideStatusIcons || hideQsCarrierGroup) {
                    val qsCarrierGroup = param.thisObject.getFieldSilently(
                        "qsCarrierGroup"
                    ) as? LinearLayout
                    (qsCarrierGroup?.parent as? ViewGroup)?.removeView(qsCarrierGroup)

                    val mShadeCarrierGroup = param.thisObject.getFieldSilently(
                        "mShadeCarrierGroup"
                    ) as? LinearLayout
                    (mShadeCarrierGroup?.parent as? ViewGroup)?.removeView(mShadeCarrierGroup)
                }
            }
    }

    private fun hideDataDisabledIcon() {
        val mobileSignalControllerClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.connectivity.MobileSignalController")
        val signalIconModelCellularClass = findClass(
            $$"$$SYSTEMUI_PACKAGE.statusbar.pipeline.mobile.domain.model.SignalIconModel$Cellular",
            suppressError = true
        )

        var alwaysShowDataRatIcon = false
        var mDataDisabledIcon = false

        mobileSignalControllerClass
            .hookMethod("updateTelephony")
            .runBefore { param ->
                if (mobileSignalControllerParam == null) {
                    mobileSignalControllerParam = param.thisObject
                }

                if (!hideDataDisabledIcon) return@runBefore

                alwaysShowDataRatIcon = param.thisObject
                    .getField("mConfig")
                    .getField("alwaysShowDataRatIcon") as Boolean

                param.thisObject
                    .getField("mConfig")
                    .setField("alwaysShowDataRatIcon", true)

                try {
                    mDataDisabledIcon = param.thisObject.getField(
                        "mDataDisabledIcon"
                    ) as Boolean

                    param.thisObject.setField("mDataDisabledIcon", false)
                } catch (_: Throwable) {
                }
            }
            .runAfter { param ->
                if (mobileSignalControllerParam == null) {
                    mobileSignalControllerParam = param.thisObject
                }

                if (!hideDataDisabledIcon) return@runAfter

                param.thisObject
                    .getField("mConfig")
                    .setField("alwaysShowDataRatIcon", alwaysShowDataRatIcon)

                param.thisObject.setFieldSilently("mDataDisabledIcon", mDataDisabledIcon)
            }

        signalIconModelCellularClass
            .hookConstructor()
            .runAfter { param ->
                if (!hideDataDisabledIcon) return@runAfter

                param.thisObject.setField("showExclamationMark", false)
            }
    }

    private fun hideQSCarrierGroup() {
        val xResources: XResources = resParams[SYSTEMUI_PACKAGE]?.res ?: return

        xResources
            .hookLayout()
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
            .runBefore { param ->
                if (coloredStatusbarOverlayEnabled) {
                    param.args[1] = Color.BLACK
                }
            }
    }
}