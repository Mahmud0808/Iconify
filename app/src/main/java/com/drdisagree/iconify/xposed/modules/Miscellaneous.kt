package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_LOCK_ICON
import com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_STATUSBAR
import com.drdisagree.iconify.common.Preferences.HIDE_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.common.Preferences.QSPANEL_HIDE_CARRIER
import com.drdisagree.iconify.common.Preferences.SB_CLOCK_SIZE
import com.drdisagree.iconify.common.Preferences.SB_CLOCK_SIZE_SWITCH
import com.drdisagree.iconify.config.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.HookRes.Companion.resParams
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.Helpers.findClassInArray
import com.drdisagree.iconify.xposed.modules.utils.StatusBarClock.getCenterClockView
import com.drdisagree.iconify.xposed.modules.utils.StatusBarClock.getLeftClockView
import com.drdisagree.iconify.xposed.modules.utils.StatusBarClock.getRightClockView
import com.drdisagree.iconify.xposed.modules.utils.StatusBarClock.setClockGravity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
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
    private var hideLockscreenLockIcon = false
    private var hideDataDisabledIcon = false
    private var sideMarginStatusIcons = 0
    private var topMarginStatusIcons = 8
    private var statusIcons: LinearLayout? = null
    private var statusIconContainer: LinearLayout? = null
    private var mobileSignalControllerParam: Any? = null
    private var sbClockSizeSwitch = false
    private var sbClockSize = 14
    private var mClockView: TextView? = null
    private var mCenterClockView: TextView? = null
    private var mRightClockView: TextView? = null

    override fun updatePrefs(vararg key: String) {
        if (Xprefs == null) return

        Xprefs!!.apply {
            qsCarrierGroupHidden = getBoolean(QSPANEL_HIDE_CARRIER, false)
            hideStatusIcons = getBoolean(HIDE_STATUS_ICONS_SWITCH, false)
            fixedStatusIcons = getBoolean(FIXED_STATUS_ICONS_SWITCH, false)
            topMarginStatusIcons = getInt(FIXED_STATUS_ICONS_TOPMARGIN, 8)
            sideMarginStatusIcons = getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0)
            hideLockscreenCarrier = getBoolean(HIDE_LOCKSCREEN_CARRIER, false)
            hideLockscreenStatusbar = getBoolean(HIDE_LOCKSCREEN_STATUSBAR, false)
            hideLockscreenLockIcon = getBoolean(HIDE_LOCKSCREEN_LOCK_ICON, false)
            hideDataDisabledIcon = getBoolean(HIDE_DATA_DISABLED_ICON, false)
            sbClockSizeSwitch = getBoolean(SB_CLOCK_SIZE_SWITCH, false)
            sbClockSize = getInt(SB_CLOCK_SIZE, 14)
        }

        if (key.isNotEmpty()) {
            key[0].let {
                if (it == QSPANEL_HIDE_CARRIER) {
                    hideQSCarrierGroup()
                }

                if (it == HIDE_STATUS_ICONS_SWITCH) {
                    hideStatusIcons()
                }

                if (it == FIXED_STATUS_ICONS_SWITCH ||
                    it == HIDE_STATUS_ICONS_SWITCH ||
                    it == FIXED_STATUS_ICONS_TOPMARGIN ||
                    it == FIXED_STATUS_ICONS_SIDEMARGIN
                ) {
                    fixedStatusIconsA12()
                }

                if (it == SB_CLOCK_SIZE_SWITCH || it == SB_CLOCK_SIZE) {
                    setClockSize()
                }

                if (it == HIDE_LOCKSCREEN_CARRIER ||
                    it == HIDE_LOCKSCREEN_STATUSBAR
                ) {
                    hideLockscreenCarrierOrStatusbar()
                }

                if (it == HIDE_LOCKSCREEN_LOCK_ICON) {
                    hideLockscreenLockIcon()
                }

                if (it == HIDE_DATA_DISABLED_ICON &&
                    mobileSignalControllerParam != null
                ) {
                    callMethod(mobileSignalControllerParam, "updateTelephony")
                }
            }
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        hideElements(loadPackageParam)
        hideQSCarrierGroup()
        hideStatusIcons()
        fixedStatusIconsA12()
        hideLockscreenCarrierOrStatusbar()
        hideLockscreenLockIcon()
        hideDataDisabledIcon(loadPackageParam)
        applyClockSize(loadPackageParam)
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
                            (getObjectField(
                                param.thisObject,
                                "mDateView"
                            ) as View).apply {
                                layoutParams.height = 0
                                layoutParams.width = 0
                                visibility = View.INVISIBLE
                            }
                        } catch (ignored: Throwable) {
                        }

                        try {
                            (getObjectField(
                                param.thisObject,
                                "mClockDateView"
                            ) as TextView).apply {
                                visibility = View.INVISIBLE
                                setTextAppearance(0)
                                setTextColor(0)
                            }
                        } catch (ignored: Throwable) {
                        }

                        try {
                            (getObjectField(
                                param.thisObject,
                                "mClockView"
                            ) as TextView).apply {
                                visibility = View.INVISIBLE
                                setTextAppearance(0)
                                setTextColor(0)
                            }
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

                            (mShadeCarrierGroup.parent as ViewGroup).removeView(mShadeCarrierGroup)
                        } catch (ignored: Throwable) {
                        }
                    }
                }
            })
        } catch (ignored: Throwable) {
        }
    }

    private fun hideDataDisabledIcon(loadPackageParam: LoadPackageParam) {
        val mobileSignalController = findClassIfExists(
            "$SYSTEMUI_PACKAGE.statusbar.connectivity.MobileSignalController",
            loadPackageParam.classLoader
        )
        val alwaysShowDataRatIcon = booleanArrayOf(false)
        val mDataDisabledIcon = booleanArrayOf(false)

        if (mobileSignalController != null) {
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

                        liparam.view.findViewById<LinearLayout>(
                            liparam.res.getIdentifier(
                                "carrier_group",
                                "id",
                                mContext.packageName
                            )
                        ).apply {
                            layoutParams.height = 0
                            layoutParams.width = 0
                            setMinimumWidth(0)
                            visibility = View.INVISIBLE
                        }
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
                            liparam.view.findViewById<TextView>(
                                liparam.res.getIdentifier(
                                    "clock",
                                    "id",
                                    mContext.packageName
                                )
                            ).apply {
                                layoutParams.height = 0
                                layoutParams.width = 0
                                setTextAppearance(0)
                                setTextColor(0)
                            }
                        } catch (ignored: Throwable) {
                        }

                        try {
                            liparam.view.findViewById<TextView>(
                                liparam.res.getIdentifier(
                                    "date_clock",
                                    "id",
                                    mContext.packageName
                                )
                            ).apply {
                                layoutParams.height = 0
                                layoutParams.width = 0
                                setTextAppearance(0)
                                setTextColor(0)
                            }
                        } catch (ignored: Throwable) {
                        }

                        try {
                            liparam.view.findViewById<LinearLayout>(
                                liparam.res.getIdentifier(
                                    "carrier_group",
                                    "id",
                                    mContext.packageName
                                )
                            ).apply {
                                layoutParams.height = 0
                                layoutParams.width = 0
                                setMinimumWidth(0)
                                visibility = View.INVISIBLE
                            }
                        } catch (ignored: Throwable) {
                        }

                        try {
                            liparam.view.findViewById<LinearLayout>(
                                liparam.res.getIdentifier(
                                    "statusIcons",
                                    "id",
                                    mContext.packageName
                                )
                            ).apply {
                                layoutParams.height = 0
                                layoutParams.width = 0
                            }
                        } catch (ignored: Throwable) {
                        }

                        try {
                            liparam.view.findViewById<LinearLayout>(
                                liparam.res.getIdentifier(
                                    "batteryRemainingIcon",
                                    "id",
                                    mContext.packageName
                                )
                            ).apply {
                                layoutParams.height = 0
                                layoutParams.width = 0
                            }
                        } catch (ignored: Throwable) {
                        }

                        try {
                            liparam.view.findViewById<FrameLayout>(
                                liparam.res.getIdentifier(
                                    "rightLayout",
                                    "id",
                                    mContext.packageName
                                )
                            ).apply {
                                layoutParams.height = 0
                                layoutParams.width = 0
                                visibility = View.INVISIBLE
                            }
                        } catch (ignored: Throwable) {
                        }

                        // Ricedroid date
                        try {
                            liparam.view.findViewById<TextView>(
                                liparam.res.getIdentifier(
                                    "date",
                                    "id",
                                    mContext.packageName
                                )
                            ).apply {
                                layoutParams.height = 0
                                layoutParams.width = 0
                                setTextAppearance(0)
                                setTextColor(0)
                            }
                        } catch (ignored: Throwable) {
                        }

                        // Nusantara clock
                        try {
                            liparam.view.findViewById<TextView>(
                                liparam.res.getIdentifier(
                                    "jr_clock",
                                    "id",
                                    mContext.packageName
                                )
                            ).apply {
                                layoutParams.height = 0
                                layoutParams.width = 0
                                setTextAppearance(0)
                                setTextColor(0)
                            }
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
                            (jrDateContainer.getChildAt(0) as TextView).apply {
                                layoutParams.height = 0
                                layoutParams.width = 0
                                setTextAppearance(0)
                                setTextColor(0)
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
                        if (!hideStatusIcons) return

                        try {
                            liparam.view.findViewById<TextView>(
                                liparam.res.getIdentifier(
                                    "date",
                                    "id",
                                    mContext.packageName
                                )
                            ).apply {
                                setTextAppearance(0)
                                layoutParams.height = 0
                                layoutParams.width = 0
                                setTextAppearance(0)
                                setTextColor(0)
                            }
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

                            if (statusIcons != null) {
                                statusIconContainer = statusIcons!!.parent as LinearLayout
                                statusIcons!!.layoutParams.height = 0
                                statusIcons!!.layoutParams.width = 0
                                statusIcons!!.visibility = View.GONE
                                statusIcons!!.requestLayout()
                            }

                            val batteryRemainingIcon = liparam.view.findViewById<LinearLayout>(
                                liparam.res.getIdentifier(
                                    "batteryRemainingIcon",
                                    "id",
                                    mContext.packageName
                                )
                            )

                            batteryRemainingIcon?.let {
                                (it.layoutParams as LinearLayout.LayoutParams)
                                    .weight = 0f
                                it.layoutParams.height = 0
                                it.layoutParams.width = 0
                                it.visibility = View.GONE
                                it.requestLayout()
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
                                statusIcons.let {
                                    it.layoutParams.height = TypedValue.applyDimension(
                                        TypedValue.COMPLEX_UNIT_DIP,
                                        28f,
                                        mContext.resources.displayMetrics
                                    ).toInt()
                                    it.layoutParams.width =
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    it.visibility = View.VISIBLE
                                    it.requestLayout()
                                }

                                val batteryRemainingIcon =
                                    (statusIconContainer!!.getChildAt(1) as LinearLayout)
                                batteryRemainingIcon.let {
                                    (it.layoutParams as LinearLayout.LayoutParams).weight =
                                        1f
                                    it.layoutParams.height =
                                        TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP,
                                            28f,
                                            mContext.resources.displayMetrics
                                        ).toInt()
                                    it.layoutParams.width = 0
                                    it.visibility = View.VISIBLE
                                    it.requestLayout()
                                }

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
                                liparam.view.findViewById<TextView>(
                                    liparam.res.getIdentifier(
                                        "keyguard_carrier_text",
                                        "id",
                                        mContext.packageName
                                    )
                                ).apply {
                                    layoutParams.height = 0
                                    visibility = View.INVISIBLE
                                    requestLayout()
                                }
                            } catch (ignored: Throwable) {
                            }
                        }

                        if (hideLockscreenStatusbar) {
                            try {
                                liparam.view.findViewById<LinearLayout>(
                                    liparam.res.getIdentifier(
                                        "status_icon_area",
                                        "id",
                                        mContext.packageName
                                    )
                                ).apply {
                                    layoutParams.height = 0
                                    visibility = View.INVISIBLE
                                    requestLayout()
                                }
                            } catch (ignored: Throwable) {
                            }

                            try {
                                liparam.view.findViewById<TextView>(
                                    liparam.res.getIdentifier(
                                        "keyguard_carrier_text",
                                        "id",
                                        mContext.packageName
                                    )
                                ).apply {
                                    layoutParams.height = 0
                                    visibility = View.INVISIBLE
                                    requestLayout()
                                }
                            } catch (ignored: Throwable) {
                            }
                        }
                    }
                })
        } catch (ignored: Throwable) {
        }
    }

    private fun hideLockscreenLockIcon() {
        val resParam: InitPackageResourcesParam = resParams[SYSTEMUI_PACKAGE] ?: return

        try {
            resParam.res.hookLayout(
                SYSTEMUI_PACKAGE,
                "layout",
                "status_bar_expanded",
                object : XC_LayoutInflated() {
                    override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                        liparam.view.findViewById<View>(
                            liparam.res.getIdentifier(
                                "lock_icon_view",
                                "id",
                                mContext.packageName
                            )
                        ).apply {
                            if (!hideLockscreenLockIcon) return

                            layoutParams.height = 0
                            layoutParams.width = 0
                            visibility = View.GONE
                            viewTreeObserver.addOnDrawListener {
                                visibility = View.GONE
                            }
                            requestLayout()
                        }
                    }
                })
        } catch (ignored: Throwable) {
        }
    }

    private fun applyClockSize(loadPackageParam: LoadPackageParam) {
        val collapsedStatusBarFragment = findClassInArray(
            loadPackageParam,
            "$SYSTEMUI_PACKAGE.statusbar.phone.CollapsedStatusBarFragment",
            "$SYSTEMUI_PACKAGE.statusbar.phone.fragment.CollapsedStatusBarFragment"

        )

        if (collapsedStatusBarFragment == null) return

        findAndHookMethod(collapsedStatusBarFragment,
            "onViewCreated",
            View::class.java,
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    mClockView = getLeftClockView(mContext, param) as? TextView
                    mCenterClockView = getCenterClockView(mContext, param) as? TextView
                    mRightClockView = getRightClockView(mContext, param) as? TextView

                    setClockSize()

                    val textClock = mClockView ?: mCenterClockView ?: mRightClockView as TextView
                    textClock.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable) {
                            setClockSize()
                        }
                    })
                }
            })

    }

    @SuppressLint("RtlHardcoded")
    private fun setClockSize() {
        if (!sbClockSizeSwitch) return

        if (mClockView != null) mClockView!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, sbClockSize.toFloat())
        if (mCenterClockView != null) mCenterClockView!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, sbClockSize.toFloat())
        if (mRightClockView != null) mRightClockView!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, sbClockSize.toFloat())

        setClockGravity(
            mClockView,
            Gravity.LEFT or Gravity.CENTER
        )

        setClockGravity(
            mCenterClockView,
            Gravity.CENTER
        )

        setClockGravity(
            mRightClockView,
            Gravity.RIGHT or Gravity.CENTER
        )

    }

    companion object {
        private val TAG = "Iconify - ${Miscellaneous::class.java.simpleName}: "
    }
}
