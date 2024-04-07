package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SIDEMARGIN
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.HIDE_DATA_DISABLED_ICON
import com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_CARRIER
import com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_STATUSBAR
import com.drdisagree.iconify.common.Preferences.HIDE_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.common.Preferences.QSPANEL_HIDE_CARRIER
import com.drdisagree.iconify.config.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.HookRes.Companion.resParams
import com.drdisagree.iconify.xposed.ModPack
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.findClassIfExists
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.setObjectField
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam
import de.robv.android.xposed.callbacks.XC_LayoutInflated
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class Miscellaneous(context: Context?) : ModPack(context!!) {

    private var qsCarrierGroupHidden = false
    private var hideStatusIcons = false
    private var fixedStatusIcons = false
    private var hideLockscreenCarrier = false
    private var hideLockscreenStatusbar = false
    private var hideDataDisabledIcon = false
    private var sideMarginStatusIcons = 0
    private var topMarginStatusIcons = 8
    private var statusIcons: LinearLayout? = null
    private var statusIconContainer: LinearLayout? = null
    private var mobileSignalControllerParam: Any? = null

    override fun updatePrefs(vararg key: String) {
        if (Xprefs == null) return

        qsCarrierGroupHidden = Xprefs!!.getBoolean(QSPANEL_HIDE_CARRIER, false)
        hideStatusIcons = Xprefs!!.getBoolean(HIDE_STATUS_ICONS_SWITCH, false)
        fixedStatusIcons = Xprefs!!.getBoolean(FIXED_STATUS_ICONS_SWITCH, false)
        topMarginStatusIcons = Xprefs!!.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 8)
        sideMarginStatusIcons = Xprefs!!.getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0)
        hideLockscreenCarrier = Xprefs!!.getBoolean(HIDE_LOCKSCREEN_CARRIER, false)
        hideLockscreenStatusbar = Xprefs!!.getBoolean(HIDE_LOCKSCREEN_STATUSBAR, false)
        hideDataDisabledIcon = Xprefs!!.getBoolean(HIDE_DATA_DISABLED_ICON, false)

        if (key.isNotEmpty()) {
            if (key[0] == QSPANEL_HIDE_CARRIER) {
                hideQSCarrierGroup()
            }

            if (key[0] == HIDE_STATUS_ICONS_SWITCH) {
                hideStatusIcons()
            }

            if (key[0] == FIXED_STATUS_ICONS_SWITCH ||
                key[0] == HIDE_STATUS_ICONS_SWITCH ||
                key[0] == FIXED_STATUS_ICONS_TOPMARGIN ||
                key[0] == FIXED_STATUS_ICONS_SIDEMARGIN
            ) {
                fixedStatusIconsA12()
            }

            if (key[0] == HIDE_LOCKSCREEN_CARRIER ||
                key[0] == HIDE_LOCKSCREEN_STATUSBAR
            ) {
                hideLockscreenCarrierOrStatusbar()
            }

            if (key[0] == HIDE_DATA_DISABLED_ICON &&
                mobileSignalControllerParam != null
            ) callMethod(
                mobileSignalControllerParam,
                "updateTelephony"
            )
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        hideElements(loadPackageParam)
        hideQSCarrierGroup()
        hideStatusIcons()
        fixedStatusIconsA12()
        hideLockscreenCarrierOrStatusbar()
        hideDataDisabledIcon(loadPackageParam)
    }

    private fun hideElements(loadPackageParam: LoadPackageParam) {
        val quickStatusBarHeader = findClass(
            "$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader",
            loadPackageParam.classLoader
        )
        try {
            hookAllMethods(quickStatusBarHeader, "onFinishInflate", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (hideStatusIcons) {
                        try {
                            val mDateView = getObjectField(
                                param.thisObject,
                                "mDateView"
                            ) as View
                            mDateView.layoutParams.height = 0
                            mDateView.layoutParams.width = 0
                            mDateView.visibility = View.INVISIBLE
                        } catch (ignored: Throwable) {
                        }

                        try {
                            val mClockDateView = getObjectField(
                                param.thisObject,
                                "mClockDateView"
                            ) as TextView
                            mClockDateView.visibility = View.INVISIBLE
                            mClockDateView.setTextAppearance(0)
                            mClockDateView.setTextColor(0)
                        } catch (ignored: Throwable) {
                        }

                        try {
                            val mClockView = getObjectField(
                                param.thisObject,
                                "mClockView"
                            ) as TextView
                            mClockView.visibility = View.INVISIBLE
                            mClockView.setTextAppearance(0)
                            mClockView.setTextColor(0)
                        } catch (ignored: Throwable) {
                        }
                    }

                    if (hideStatusIcons || qsCarrierGroupHidden) {
                        try {
                            val mQSCarriers = getObjectField(
                                param.thisObject,
                                "mQSCarriers"
                            ) as View

                            mQSCarriers.visibility = View.INVISIBLE
                        } catch (ignored: Throwable) {
                        }
                    }
                }
            })
        } catch (ignored: Throwable) {
        }

        try {
            var shadeHeaderControllerClass = findClassIfExists(
                "$SYSTEMUI_PACKAGE.shade.LargeScreenShadeHeaderController",
                loadPackageParam.classLoader
            )
            if (shadeHeaderControllerClass == null) {
                shadeHeaderControllerClass = findClass(
                    "$SYSTEMUI_PACKAGE.shade.ShadeHeaderController",
                    loadPackageParam.classLoader
                )
            }

            hookAllMethods(shadeHeaderControllerClass, "onInit", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (hideStatusIcons) {
                        try {
                            val iconContainer = getObjectField(
                                param.thisObject,
                                "iconContainer"
                            ) as LinearLayout

                            (iconContainer.parent as ViewGroup).removeView(iconContainer)
                        } catch (ignored: Throwable) {
                        }
                        try {
                            val batteryIcon = getObjectField(
                                param.thisObject,
                                "batteryIcon"
                            ) as LinearLayout

                            (batteryIcon.parent as ViewGroup).removeView(batteryIcon)
                        } catch (ignored: Throwable) {
                        }
                    }

                    if (hideStatusIcons || qsCarrierGroupHidden) {
                        try {
                            val qsCarrierGroup = getObjectField(
                                param.thisObject,
                                "qsCarrierGroup"
                            ) as LinearLayout

                            (qsCarrierGroup.parent as ViewGroup).removeView(qsCarrierGroup)
                        } catch (ignored: Throwable) {
                        }

                        try {
                            val mShadeCarrierGroup = getObjectField(
                                param.thisObject,
                                "mShadeCarrierGroup"
                            ) as LinearLayout

                            (mShadeCarrierGroup.parent as ViewGroup).removeView(
                                mShadeCarrierGroup
                            )
                        } catch (ignored: Throwable) {
                        }
                    }
                }
            })
        } catch (ignored: Throwable) {
        }
    }

    private fun hideDataDisabledIcon(loadPackageParam: LoadPackageParam) {
        try {
            val mobileSignalController = findClass(
                "$SYSTEMUI_PACKAGE.statusbar.connectivity.MobileSignalController",
                loadPackageParam.classLoader
            )
            val alwaysShowDataRatIcon = booleanArrayOf(false)
            val mDataDisabledIcon = booleanArrayOf(false)

            hookAllMethods(mobileSignalController, "updateTelephony", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (mobileSignalControllerParam == null) {
                        mobileSignalControllerParam = param.thisObject
                    }

                    if (!hideDataDisabledIcon) return

                    alwaysShowDataRatIcon[0] = getObjectField(
                        getObjectField(
                            param.thisObject,
                            "mConfig"
                        ), "alwaysShowDataRatIcon"
                    ) as Boolean

                    setObjectField(
                        getObjectField(param.thisObject, "mConfig"),
                        "alwaysShowDataRatIcon",
                        false
                    )

                    try {
                        mDataDisabledIcon[0] = getObjectField(
                            param.thisObject,
                            "mDataDisabledIcon"
                        ) as Boolean

                        setObjectField(
                            param.thisObject,
                            "mDataDisabledIcon",
                            false
                        )
                    } catch (ignored: Throwable) {
                    }
                }

                override fun afterHookedMethod(param: MethodHookParam) {
                    if (mobileSignalControllerParam == null) {
                        mobileSignalControllerParam = param.thisObject
                    }

                    if (!hideDataDisabledIcon) return

                    setObjectField(
                        getObjectField(param.thisObject, "mConfig"),
                        "alwaysShowDataRatIcon",
                        alwaysShowDataRatIcon[0]
                    )

                    try {
                        setObjectField(
                            param.thisObject,
                            "mDataDisabledIcon",
                            mDataDisabledIcon[0]
                        )
                    } catch (ignored: Throwable) {
                    }
                }
            })
        } catch (ignored: Throwable) {
            log(TAG + "Not a crash... MobileSignalController class not found.")
        }
    }

    private fun hideQSCarrierGroup() {
        val resParam: InitPackageResourcesParam = resParams[SYSTEMUI_PACKAGE] ?: return

        try {
            resParam.res.hookLayout(
                SYSTEMUI_PACKAGE,
                "layout",
                "quick_qs_status_icons",
                object : XC_LayoutInflated() {
                    override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                        if (!qsCarrierGroupHidden) return

                        val carrierGroup =
                            liparam.view.findViewById<LinearLayout>(
                                liparam.res.getIdentifier(
                                    "carrier_group",
                                    "id",
                                    mContext.packageName
                                )
                            )
                        carrierGroup.layoutParams.height = 0
                        carrierGroup.layoutParams.width = 0
                        carrierGroup.setMinimumWidth(0)
                        carrierGroup.visibility = View.INVISIBLE
                    }
                })
        } catch (ignored: Throwable) {
        }
    }

    private fun hideStatusIcons() {
        val resParam: InitPackageResourcesParam = resParams[SYSTEMUI_PACKAGE] ?: return

        try {
            resParam.res.hookLayout(
                SYSTEMUI_PACKAGE,
                "layout",
                "quick_qs_status_icons",
                object : XC_LayoutInflated() {
                    override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                        if (!hideStatusIcons) return

                        try {
                            val clock =
                                liparam.view.findViewById<TextView>(
                                    liparam.res.getIdentifier(
                                        "clock",
                                        "id",
                                        mContext.packageName
                                    )
                                )
                            clock.layoutParams.height = 0
                            clock.layoutParams.width = 0
                            clock.setTextAppearance(0)
                            clock.setTextColor(0)
                        } catch (ignored: Throwable) {
                        }

                        try {
                            val dateClock =
                                liparam.view.findViewById<TextView>(
                                    liparam.res.getIdentifier(
                                        "date_clock",
                                        "id",
                                        mContext.packageName
                                    )
                                )
                            dateClock.layoutParams.height = 0
                            dateClock.layoutParams.width = 0
                            dateClock.setTextAppearance(0)
                            dateClock.setTextColor(0)
                        } catch (ignored: Throwable) {
                        }

                        try {
                            val carrierGroup =
                                liparam.view.findViewById<LinearLayout>(
                                    liparam.res.getIdentifier(
                                        "carrier_group",
                                        "id",
                                        mContext.packageName
                                    )
                                )
                            carrierGroup.layoutParams.height = 0
                            carrierGroup.layoutParams.width = 0
                            carrierGroup.setMinimumWidth(0)
                            carrierGroup.visibility = View.INVISIBLE
                        } catch (ignored: Throwable) {
                        }

                        try {
                            val statusIcons =
                                liparam.view.findViewById<LinearLayout>(
                                    liparam.res.getIdentifier(
                                        "statusIcons",
                                        "id",
                                        mContext.packageName
                                    )
                                )
                            statusIcons.layoutParams.height = 0
                            statusIcons.layoutParams.width = 0
                        } catch (ignored: Throwable) {
                        }

                        try {
                            val batteryRemainingIcon =
                                liparam.view.findViewById<LinearLayout>(
                                    liparam.res.getIdentifier(
                                        "batteryRemainingIcon",
                                        "id",
                                        mContext.packageName
                                    )
                                )
                            batteryRemainingIcon.layoutParams.height = 0
                            batteryRemainingIcon.layoutParams.width = 0
                        } catch (ignored: Throwable) {
                        }

                        try {
                            val rightLayout =
                                liparam.view.findViewById<FrameLayout>(
                                    liparam.res.getIdentifier(
                                        "rightLayout",
                                        "id",
                                        mContext.packageName
                                    )
                                )
                            rightLayout.layoutParams.height = 0
                            rightLayout.layoutParams.width = 0
                            rightLayout.visibility = View.INVISIBLE
                        } catch (ignored: Throwable) {
                        }

                        // Ricedroid date
                        try {
                            val date =
                                liparam.view.findViewById<TextView>(
                                    liparam.res.getIdentifier(
                                        "date",
                                        "id",
                                        mContext.packageName
                                    )
                                )
                            date.layoutParams.height = 0
                            date.layoutParams.width = 0
                            date.setTextAppearance(0)
                            date.setTextColor(0)
                        } catch (ignored: Throwable) {
                        }

                        // Nusantara clock
                        try {
                            val jrClock =
                                liparam.view.findViewById<TextView>(
                                    liparam.res.getIdentifier(
                                        "jr_clock",
                                        "id",
                                        mContext.packageName
                                    )
                                )
                            jrClock.layoutParams.height = 0
                            jrClock.layoutParams.width = 0
                            jrClock.setTextAppearance(0)
                            jrClock.setTextColor(0)
                        } catch (ignored: Throwable) {
                        }

                        // Nusantara date
                        try {
                            val jrDateContainer =
                                liparam.view.findViewById<LinearLayout>(
                                    liparam.res.getIdentifier(
                                        "jr_date_container",
                                        "id",
                                        mContext.packageName
                                    )
                                )
                            val jrDate = jrDateContainer.getChildAt(0) as TextView
                            jrDate.layoutParams.height = 0
                            jrDate.layoutParams.width = 0
                            jrDate.setTextAppearance(0)
                            jrDate.setTextColor(0)
                        } catch (ignored: Throwable) {
                        }
                    }
                })
        } catch (ignored: Throwable) {
        }

        try {
            resParam.res.hookLayout(
                SYSTEMUI_PACKAGE,
                "layout",
                "quick_status_bar_header_date_privacy",
                object : XC_LayoutInflated() {
                    override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                        if (!hideStatusIcons) return

                        try {
                            val date =
                                liparam.view.findViewById<TextView>(
                                    liparam.res.getIdentifier(
                                        "date",
                                        "id",
                                        mContext.packageName
                                    )
                                )
                            date.setTextAppearance(0)
                            date.layoutParams.height = 0
                            date.layoutParams.width = 0
                            date.setTextAppearance(0)
                            date.setTextColor(0)
                        } catch (ignored: Throwable) {
                        }
                    }
                })
        } catch (ignored: Throwable) {
        }
    }

    private fun fixedStatusIconsA12() {
        if (Build.VERSION.SDK_INT >= 33) return

        val resParam: InitPackageResourcesParam = resParams[SYSTEMUI_PACKAGE] ?: return

        try {
            resParam.res.hookLayout(
                SYSTEMUI_PACKAGE,
                "layout",
                "quick_qs_status_icons",
                object : XC_LayoutInflated() {
                    override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                        if (!fixedStatusIcons || hideStatusIcons) return

                        try {
                            statusIcons = liparam.view.findViewById(
                                liparam.res.getIdentifier(
                                    "statusIcons",
                                    "id",
                                    mContext.packageName
                                )
                            )

                            val batteryRemainingIcon = liparam.view.findViewById<LinearLayout>(
                                liparam.res.getIdentifier(
                                    "batteryRemainingIcon",
                                    "id",
                                    mContext.packageName
                                )
                            )

                            if (statusIcons != null) {
                                statusIconContainer = statusIcons!!.parent as LinearLayout
                                statusIcons!!.layoutParams.height = 0
                                statusIcons!!.layoutParams.width = 0
                                statusIcons!!.visibility = View.GONE
                                statusIcons!!.requestLayout()
                            }

                            if (batteryRemainingIcon != null) {
                                (batteryRemainingIcon.layoutParams as LinearLayout.LayoutParams)
                                    .weight = 0f
                                batteryRemainingIcon.layoutParams.height = 0
                                batteryRemainingIcon.layoutParams.width = 0
                                batteryRemainingIcon.visibility = View.GONE
                                batteryRemainingIcon.requestLayout()
                            }
                        } catch (ignored: Throwable) {
                        }
                    }
                })
        } catch (ignored: Throwable) {
        }

        try {
            resParam.res.hookLayout(
                SYSTEMUI_PACKAGE,
                "layout",
                "quick_status_bar_header_date_privacy",
                object : XC_LayoutInflated() {
                    override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                        if (!fixedStatusIcons || hideStatusIcons) return

                        try {
                            val privacyContainer =
                                liparam.view.findViewById<FrameLayout>(
                                    liparam.res.getIdentifier(
                                        "privacy_container",
                                        "id",
                                        mContext.packageName
                                    )
                                )

                            if (statusIconContainer != null && statusIconContainer!!.parent != null && statusIcons != null) {
                                try {
                                    (statusIconContainer!!.parent as FrameLayout).removeView(
                                        statusIconContainer
                                    )
                                } catch (ignored: Throwable) {
                                    (statusIconContainer!!.parent as LinearLayout).removeView(
                                        statusIconContainer
                                    )
                                }

                                val statusIcons =
                                    statusIconContainer!!.getChildAt(0) as LinearLayout
                                statusIcons.layoutParams.height = TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    28f,
                                    mContext.resources.displayMetrics
                                ).toInt()
                                statusIcons.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                                statusIcons.visibility = View.VISIBLE
                                statusIcons.requestLayout()

                                val batteryRemainingIcon =
                                    statusIconContainer!!.getChildAt(1) as LinearLayout
                                (batteryRemainingIcon.layoutParams as LinearLayout.LayoutParams).weight =
                                    1f
                                batteryRemainingIcon.layoutParams.height =
                                    TypedValue.applyDimension(
                                        TypedValue.COMPLEX_UNIT_DIP,
                                        28f,
                                        mContext.resources.displayMetrics
                                    ).toInt()
                                batteryRemainingIcon.layoutParams.width = 0
                                batteryRemainingIcon.visibility = View.VISIBLE
                                batteryRemainingIcon.requestLayout()

                                statusIconContainer!!.setLayoutParams(
                                    FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP,
                                            28f,
                                            mContext.resources.displayMetrics
                                        ).toInt(),
                                        Gravity.END
                                    )
                                )
                                statusIconContainer!!.gravity = Gravity.CENTER
                                (statusIconContainer!!.layoutParams as FrameLayout.LayoutParams).setMargins(
                                    0, TypedValue.applyDimension(
                                        TypedValue.COMPLEX_UNIT_DIP,
                                        topMarginStatusIcons.toFloat(),
                                        mContext.resources.displayMetrics
                                    ).toInt(), 0, 0
                                )
                                (statusIconContainer!!.layoutParams as FrameLayout.LayoutParams).setMarginEnd(
                                    TypedValue.applyDimension(
                                        TypedValue.COMPLEX_UNIT_DIP,
                                        sideMarginStatusIcons.toFloat(),
                                        mContext.resources.displayMetrics
                                    ).toInt()
                                )
                                statusIconContainer!!.requestLayout()

                                privacyContainer.addView(statusIconContainer)
                            }
                        } catch (ignored: Throwable) {
                        }
                    }
                })
        } catch (ignored: Throwable) {
        }
    }

    private fun hideLockscreenCarrierOrStatusbar() {
        val resParam: InitPackageResourcesParam = resParams[SYSTEMUI_PACKAGE] ?: return

        try {
            resParam.res.hookLayout(
                SYSTEMUI_PACKAGE,
                "layout",
                "keyguard_status_bar",
                object : XC_LayoutInflated() {
                    override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                        if (hideLockscreenCarrier) {
                            try {
                                val keyguardCarrierText =
                                    liparam.view.findViewById<TextView>(
                                        liparam.res.getIdentifier(
                                            "keyguard_carrier_text",
                                            "id",
                                            mContext.packageName
                                        )
                                    )
                                keyguardCarrierText.layoutParams.height = 0
                                keyguardCarrierText.visibility = View.INVISIBLE
                                keyguardCarrierText.requestLayout()
                            } catch (ignored: Throwable) {
                            }
                        }

                        if (hideLockscreenStatusbar) {
                            try {
                                val statusIconArea =
                                    liparam.view.findViewById<LinearLayout>(
                                        liparam.res.getIdentifier(
                                            "status_icon_area",
                                            "id",
                                            mContext.packageName
                                        )
                                    )
                                statusIconArea.layoutParams.height = 0
                                statusIconArea.visibility = View.INVISIBLE
                                statusIconArea.requestLayout()
                            } catch (ignored: Throwable) {
                            }

                            try {
                                val keyguardCarrierText =
                                    liparam.view.findViewById<TextView>(
                                        liparam.res.getIdentifier(
                                            "keyguard_carrier_text",
                                            "id",
                                            mContext.packageName
                                        )
                                    )
                                keyguardCarrierText.layoutParams.height = 0
                                keyguardCarrierText.visibility = View.INVISIBLE
                                keyguardCarrierText.requestLayout()
                            } catch (ignored: Throwable) {
                            }
                        }
                    }
                })
        } catch (ignored: Throwable) {
        }
    }

    companion object {
        private val TAG = "Iconify - ${Miscellaneous::class.java.simpleName}: "
    }
}
