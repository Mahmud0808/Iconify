package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.CHIP_QSSTATUSICONS_STYLE
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCKBG_STYLE
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SIDEMARGIN
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SWITCH
import com.drdisagree.iconify.common.Preferences.HIDE_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.common.Preferences.QSPANEL_STATUSICONSBG_SWITCH
import com.drdisagree.iconify.common.Preferences.STATUSBAR_CLOCKBG_SWITCH
import com.drdisagree.iconify.common.Preferences.STATUSBAR_CLOCK_COLOR_CODE
import com.drdisagree.iconify.common.Preferences.STATUSBAR_CLOCK_COLOR_OPTION
import com.drdisagree.iconify.config.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.HookRes.Companion.resParams
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.Helpers.findClassInArray
import com.drdisagree.iconify.xposed.modules.utils.StatusBarClock
import com.drdisagree.iconify.xposed.modules.utils.StatusBarClock.setClockGravity
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.toPx
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.findClassIfExists
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.getStaticObjectField
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam
import de.robv.android.xposed.callbacks.XC_LayoutInflated
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class BackgroundChip(context: Context?) : ModPack(context!!) {

    private val mQsStatusIconsContainer = LinearLayout(mContext)
    private var mShowSBClockBg = false
    private var hideStatusIcons = false
    private var mShowQSStatusIconsBg = false
    private var showHeaderClock = false
    private var topMarginStatusIcons = 8
    private var sideMarginStatusIcons = 0
    private var qsStatusIconsChipStyle = 0
    private var statusBarClockChipStyle = 0
    private var statusBarClockColorOption = 0
    private var statusBarClockColorCode = Color.WHITE
    private var fixedStatusIcons = false
    private var constraintLayoutId = -1
    private var header: ViewGroup? = null
    private var mClockView: View? = null
    private var mCenterClockView: View? = null
    private var mRightClockView: View? = null
    private var dependencyClass: Class<*>? = null
    private var darkIconDispatcherClass: Class<*>? = null
    private var mLoadPackageParam: LoadPackageParam? = null

    override fun updatePrefs(vararg key: String) {
        if (Xprefs == null) return

        mShowSBClockBg = Xprefs!!.getBoolean(STATUSBAR_CLOCKBG_SWITCH, false)
        mShowQSStatusIconsBg = Xprefs!!.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false)
        qsStatusIconsChipStyle = Xprefs!!.getInt(CHIP_QSSTATUSICONS_STYLE, 0)
        statusBarClockChipStyle = Xprefs!!.getInt(CHIP_STATUSBAR_CLOCKBG_STYLE, 0)
        statusBarClockColorOption = Xprefs!!.getInt(STATUSBAR_CLOCK_COLOR_OPTION, 0)
        statusBarClockColorCode = Xprefs!!.getInt(STATUSBAR_CLOCK_COLOR_CODE, Color.WHITE)
        showHeaderClock = Xprefs!!.getBoolean(HEADER_CLOCK_SWITCH, false)
        hideStatusIcons = Xprefs!!.getBoolean(HIDE_STATUS_ICONS_SWITCH, false)
        fixedStatusIcons = Xprefs!!.getBoolean(FIXED_STATUS_ICONS_SWITCH, false)
        topMarginStatusIcons = Xprefs!!.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 8)
        sideMarginStatusIcons = Xprefs!!.getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0)

        if (key.isNotEmpty()) {
            if (key[0] == STATUSBAR_CLOCKBG_SWITCH ||
                key[0] == CHIP_STATUSBAR_CLOCKBG_STYLE ||
                key[0] == STATUSBAR_CLOCK_COLOR_OPTION ||
                key[0] == STATUSBAR_CLOCK_COLOR_CODE
            ) {
                updateStatusBarClock()
            }

            if (key[0] == QSPANEL_STATUSICONSBG_SWITCH ||
                key[0] == CHIP_STATUSBAR_CLOCKBG_STYLE ||
                key[0] == HEADER_CLOCK_SWITCH ||
                key[0] == HIDE_STATUS_ICONS_SWITCH ||
                key[0] == FIXED_STATUS_ICONS_SWITCH
            ) {
                setQSStatusIconsBgA12()
            }

            if (key[0] == CHIP_QSSTATUSICONS_STYLE ||
                key[0] == FIXED_STATUS_ICONS_TOPMARGIN ||
                key[0] == FIXED_STATUS_ICONS_SIDEMARGIN
            ) {
                updateStatusIcons()
            }
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        mLoadPackageParam = loadPackageParam
        statusBarClockChip(loadPackageParam)
        statusIconsChip(loadPackageParam)
    }

    private fun statusBarClockChip(loadPackageParam: LoadPackageParam) {
        val collapsedStatusBarFragment = findClassInArray(
            loadPackageParam.classLoader,
            "$SYSTEMUI_PACKAGE.statusbar.phone.CollapsedStatusBarFragment",
            "$SYSTEMUI_PACKAGE.statusbar.phone.fragment.CollapsedStatusBarFragment"

        )
        dependencyClass = findClass(
            "$SYSTEMUI_PACKAGE.Dependency",
            loadPackageParam.classLoader
        )
        darkIconDispatcherClass = findClass(
            "$SYSTEMUI_PACKAGE.plugins.DarkIconDispatcher",
            loadPackageParam.classLoader
        )

        findAndHookMethod(
            collapsedStatusBarFragment,
            "onViewCreated",
            View::class.java,
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    mClockView = StatusBarClock.getLeftClockView(mContext, param)
                    mCenterClockView = StatusBarClock.getCenterClockView(mContext, param)
                    mRightClockView = StatusBarClock.getRightClockView(mContext, param)

                    (getObjectField(
                        param.thisObject,
                        "mStatusBar"
                    ) as ViewGroup).addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                        updateStatusBarClock()
                    }

                    updateStatusBarClock()

                    if (mShowSBClockBg) {
                        try {
                            val mStatusBar = getObjectField(
                                param.thisObject,
                                "mStatusBar"
                            ) as FrameLayout

                            val statusBarStartSideContent =
                                mStatusBar.findViewById<FrameLayout>(
                                    mContext.resources.getIdentifier(
                                        "status_bar_start_side_content",
                                        "id",
                                        mContext.packageName
                                    )
                                )
                            statusBarStartSideContent.layoutParams.height =
                                FrameLayout.LayoutParams.MATCH_PARENT
                            statusBarStartSideContent.requestLayout()

                            val statusBarStartSideExceptHeadsUp =
                                mStatusBar.findViewById<LinearLayout>(
                                    mContext.resources.getIdentifier(
                                        "status_bar_start_side_except_heads_up",
                                        "id",
                                        mContext.packageName
                                    )
                                )
                            (statusBarStartSideExceptHeadsUp.layoutParams as FrameLayout.LayoutParams).gravity =
                                Gravity.START or Gravity.CENTER
                            statusBarStartSideExceptHeadsUp.gravity =
                                Gravity.START or Gravity.CENTER
                            statusBarStartSideExceptHeadsUp.requestLayout()
                        } catch (throwable: Throwable) {
                            log(TAG + throwable)
                        }
                    }
                }
            })
    }

    private fun statusIconsChip(loadPackageParam: LoadPackageParam) {
        setQSStatusIconsBgA12()
        setQSStatusIconsBgA13Plus(loadPackageParam)
    }

    @SuppressLint("RtlHardcoded")
    private fun updateStatusBarClock() {
        if (!mShowSBClockBg) return

        val clockPaddingStartEnd: Int = mContext.toPx(8)
        val clockPaddingTopBottom: Int = mContext.toPx(2)

        updateClockView(
            mClockView,
            clockPaddingStartEnd,
            clockPaddingTopBottom,
            Gravity.LEFT or Gravity.CENTER
        )

        updateClockView(
            mCenterClockView,
            clockPaddingStartEnd,
            clockPaddingTopBottom,
            Gravity.CENTER
        )

        updateClockView(
            mRightClockView,
            clockPaddingStartEnd,
            clockPaddingTopBottom,
            Gravity.RIGHT or Gravity.CENTER
        )
    }

    private fun updateStatusIcons() {
        if (mQsStatusIconsContainer.childCount == 0) return

        val paddingTopBottom: Int = mContext.toPx(4)
        val paddingStartEnd: Int = mContext.toPx(12)

        if (mShowQSStatusIconsBg) {
            setStatusIconsBackgroundChip(mQsStatusIconsContainer)
            mQsStatusIconsContainer.setPadding(
                paddingStartEnd,
                paddingTopBottom,
                paddingStartEnd,
                paddingTopBottom
            )
        }

        if (mQsStatusIconsContainer.layoutParams is FrameLayout.LayoutParams) {
            (mQsStatusIconsContainer.layoutParams as FrameLayout.LayoutParams).setMargins(
                0,
                mContext.toPx(topMarginStatusIcons),
                0,
                0
            )

            (mQsStatusIconsContainer.layoutParams as FrameLayout.LayoutParams).setMarginEnd(
                mContext.toPx(sideMarginStatusIcons)
            )
        } else if (mQsStatusIconsContainer.layoutParams is LinearLayout.LayoutParams) {
            (mQsStatusIconsContainer.layoutParams as LinearLayout.LayoutParams).setMargins(
                0,
                mContext.toPx(topMarginStatusIcons),
                0,
                0
            )

            (mQsStatusIconsContainer.layoutParams as LinearLayout.LayoutParams).setMarginEnd(
                mContext.toPx(sideMarginStatusIcons)
            )
        } else if (mLoadPackageParam != null && header != null && constraintLayoutId != -1) {
            try {
                val constraintSetClass = findClass(
                    "androidx.constraintlayout.widget.ConstraintSet",
                    mLoadPackageParam!!.classLoader
                )
                val mConstraintSet = constraintSetClass.getDeclaredConstructor().newInstance()

                callMethod(mConstraintSet, "clone", header)

                callMethod(
                    mConstraintSet,
                    "connect",
                    constraintLayoutId,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    0
                )

                callMethod(
                    mConstraintSet,
                    "connect",
                    constraintLayoutId,
                    ConstraintSet.END,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.END,
                    0
                )

                callMethod(mConstraintSet, "applyTo", header)

                callMethod(
                    callMethod(mQsStatusIconsContainer, "getLayoutParams"),
                    "setMargins",
                    0,
                    mContext.toPx(topMarginStatusIcons),
                    0,
                    0
                )

                callMethod(
                    callMethod(mQsStatusIconsContainer, "getLayoutParams"),
                    "setMarginEnd",
                    mContext.toPx(sideMarginStatusIcons)
                )
            } catch (throwable: Throwable) {
                log(TAG + throwable)
            }
        }

        mQsStatusIconsContainer.requestLayout()

        val config = mContext.resources.configuration

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mQsStatusIconsContainer.visibility = View.GONE
        } else {
            mQsStatusIconsContainer.visibility = View.VISIBLE
        }
    }

    private fun setSBClockBackgroundChip(view: View) {
        try {
            val pc = mContext.createPackageContext(
                BuildConfig.APPLICATION_ID,
                Context.CONTEXT_IGNORE_SECURITY
            )

            val res = pc.resources

            val bg = when (statusBarClockChipStyle) {
                0 -> ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_1, pc.theme)
                1 -> ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_2, pc.theme)
                2 -> ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_3, pc.theme)
                3 -> ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_4, pc.theme)
                4 -> ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_5, pc.theme)
                5 -> ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_6, pc.theme)
                6 -> ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_7, pc.theme)
                else -> null
            }

            if (bg != null) {
                view.background = bg
            }
        } catch (throwable: Throwable) {
            log(TAG + throwable)
        }
    }

    private fun setStatusIconsBackgroundChip(layout: LinearLayout) {
        try {
            val pc = mContext.createPackageContext(
                BuildConfig.APPLICATION_ID,
                Context.CONTEXT_IGNORE_SECURITY
            )

            val res = pc.resources

            val bg = when (qsStatusIconsChipStyle) {
                0 -> ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_1, pc.theme)
                1 -> ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_2, pc.theme)
                2 -> ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_3, pc.theme)
                3 -> ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_4, pc.theme)
                4 -> ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_5, pc.theme)
                5 -> ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_6, pc.theme)
                else -> null
            }

            if (bg != null) {
                layout.background = bg
            }
        } catch (throwable: Throwable) {
            log(TAG + throwable)
        }
    }

    @SuppressLint("RtlHardcoded")
    private fun updateClockView(clockView: View?, startEnd: Int, topBottom: Int, gravity: Int) {
        if (clockView == null) return

        clockView.setPadding(startEnd, topBottom, startEnd, topBottom)

        setSBClockBackgroundChip(clockView)

        when (statusBarClockColorOption) {
            0 -> {
                (clockView as TextView).paint.setXfermode(null)
                try {
                    callMethod(
                        callStaticMethod(dependencyClass, "get", darkIconDispatcherClass),
                        "addDarkReceiver",
                        clockView
                    )
                } catch (ignored: Throwable) {
                    callMethod(
                        callMethod(
                            getStaticObjectField(
                                dependencyClass,
                                "sDependency"
                            ),
                            "getDependencyInner",
                            darkIconDispatcherClass
                        ),
                        "addDarkReceiver",
                        clockView
                    )
                }
            }

            1 -> {
                (clockView as TextView).paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_OUT))
            }

            2 -> {
                (clockView as TextView).paint.setXfermode(null)
                try {
                    callMethod(
                        callStaticMethod(dependencyClass, "get", darkIconDispatcherClass),
                        "removeDarkReceiver",
                        clockView
                    )
                } catch (ignored: Throwable) {
                    callMethod(
                        callMethod(
                            getStaticObjectField(
                                dependencyClass,
                                "sDependency"
                            ),
                            "getDependencyInner",
                            darkIconDispatcherClass
                        ),
                        "removeDarkReceiver",
                        clockView
                    )
                }
                clockView.setTextColor(statusBarClockColorCode)
            }
        }

        setClockGravity(clockView, gravity)
    }

    private fun setQSStatusIconsBgA12() {
        if (Build.VERSION.SDK_INT >= 33) return

        val resParam: InitPackageResourcesParam = resParams[SYSTEMUI_PACKAGE] ?: return

        resParam.res.hookLayout(
            SYSTEMUI_PACKAGE,
            "layout",
            "quick_qs_status_icons",
            object : XC_LayoutInflated() {
                override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                    if (!mShowQSStatusIconsBg || hideStatusIcons || fixedStatusIcons) return

                    try {
                        val statusIcons =
                            liparam.view.findViewById<LinearLayout>(
                                liparam.res.getIdentifier(
                                    "statusIcons",
                                    "id",
                                    mContext.packageName
                                )
                            )

                        val statusIconContainer = statusIcons.parent as LinearLayout
                        (statusIconContainer.layoutParams as FrameLayout.LayoutParams).gravity =
                            Gravity.CENTER_VERTICAL or Gravity.END
                        statusIconContainer.layoutParams.height = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            28f,
                            mContext.resources.displayMetrics
                        ).toInt()
                        statusIconContainer.requestLayout()

                        val paddingTopBottom = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            2f,
                            mContext.resources.displayMetrics
                        ).toInt()
                        val paddingStartEnd = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            8f,
                            mContext.resources.displayMetrics
                        ).toInt()

                        statusIconContainer.setPadding(
                            paddingStartEnd,
                            paddingTopBottom,
                            paddingStartEnd,
                            paddingTopBottom
                        )

                        setStatusIconsBackgroundChip(statusIconContainer)
                    } catch (throwable: Throwable) {
                        log(TAG + throwable)
                    }
                }
            })

        resParam.res.hookLayout(
            SYSTEMUI_PACKAGE,
            "layout",
            "quick_status_bar_header_date_privacy",
            object : XC_LayoutInflated() {
                override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                    if (!mShowQSStatusIconsBg || hideStatusIcons || !fixedStatusIcons) return

                    try {
                        val statusIcons =
                            liparam.view.findViewById<LinearLayout>(
                                liparam.res.getIdentifier(
                                    "statusIcons",
                                    "id",
                                    mContext.packageName
                                )
                            )
                        if (statusIcons != null) {
                            val statusIconContainer = statusIcons.parent as LinearLayout
                            val paddingTopBottom = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                2f,
                                mContext.resources.displayMetrics
                            ).toInt()
                            val paddingStartEnd = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                8f,
                                mContext.resources.displayMetrics
                            ).toInt()

                            statusIconContainer.setPadding(
                                paddingStartEnd,
                                paddingTopBottom,
                                paddingStartEnd,
                                paddingTopBottom
                            )

                            setStatusIconsBackgroundChip(statusIconContainer)
                        }
                    } catch (throwable: Throwable) {
                        log(TAG + throwable)
                    }
                }
            })
    }

    private fun setQSStatusIconsBgA13Plus(loadPackageParam: LoadPackageParam) {
        if (Build.VERSION.SDK_INT < 33) return

        val quickStatusBarHeader = findClass(
            "$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader",
            loadPackageParam.classLoader
        )
        var correctClass = false
        val fs = quickStatusBarHeader.declaredFields
        for (f in fs) {
            if (f.getName() == "mIconContainer") {
                correctClass = true
            }
        }

        if (correctClass) {
            hookAllMethods(quickStatusBarHeader, "onFinishInflate", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!mShowQSStatusIconsBg && !fixedStatusIcons || hideStatusIcons) return

                    val mQuickStatusBarHeader = param.thisObject as FrameLayout
                    val mIconContainer = getObjectField(
                        param.thisObject,
                        "mIconContainer"
                    ) as LinearLayout
                    val mBatteryRemainingIcon = getObjectField(
                        param.thisObject,
                        "mBatteryRemainingIcon"
                    ) as LinearLayout
                    var layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    mQsStatusIconsContainer.setLayoutParams(layoutParams)
                    mQsStatusIconsContainer.gravity = Gravity.CENTER
                    mQsStatusIconsContainer.orientation = LinearLayout.HORIZONTAL

                    if (mQsStatusIconsContainer.parent != null) {
                        (mQsStatusIconsContainer.parent as ViewGroup).removeView(
                            mQsStatusIconsContainer
                        )
                    }

                    if (mQsStatusIconsContainer.childCount > 0) {
                        mQsStatusIconsContainer.removeAllViews()
                    }

                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    (mIconContainer.parent as ViewGroup).removeView(mIconContainer)
                    mIconContainer.setLayoutParams(layoutParams)
                    mIconContainer.layoutParams.height = mContext.toPx(32)

                    (mBatteryRemainingIcon.parent as ViewGroup).removeView(mBatteryRemainingIcon)
                    mBatteryRemainingIcon.layoutParams.height = mContext.toPx(32)

                    mQsStatusIconsContainer.addView(mIconContainer)
                    mQsStatusIconsContainer.addView(mBatteryRemainingIcon)

                    mQuickStatusBarHeader.addView(
                        mQsStatusIconsContainer,
                        mQuickStatusBarHeader.childCount - 1
                    )

                    (mQsStatusIconsContainer.layoutParams as FrameLayout.LayoutParams).gravity =
                        Gravity.TOP or Gravity.END

                    updateStatusIcons()
                }
            })

            hookAllMethods(quickStatusBarHeader, "updateResources", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!mShowQSStatusIconsBg && !fixedStatusIcons || hideStatusIcons) return

                    updateStatusIcons()
                }
            })
        } else {
            var shadeHeaderControllerClass = findClassIfExists(
                "$SYSTEMUI_PACKAGE.shade.ShadeHeaderController",
                loadPackageParam.classLoader
            )
            if (shadeHeaderControllerClass == null) {
                shadeHeaderControllerClass = findClass(
                    "$SYSTEMUI_PACKAGE.shade.LargeScreenShadeHeaderController",
                    loadPackageParam.classLoader
                )
            }

            hookAllMethods(shadeHeaderControllerClass, "onInit", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!mShowQSStatusIconsBg && !fixedStatusIcons || hideStatusIcons) return

                    val iconContainer = getObjectField(
                        param.thisObject,
                        "iconContainer"
                    ) as LinearLayout
                    val batteryIcon = getObjectField(
                        param.thisObject,
                        "batteryIcon"
                    ) as LinearLayout
                    header = iconContainer.parent as ViewGroup
                    val constraintLayoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    )

                    constraintLayoutId = View.generateViewId()
                    constraintLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                    constraintLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    mQsStatusIconsContainer.setLayoutParams(constraintLayoutParams)
                    mQsStatusIconsContainer.gravity = Gravity.CENTER
                    mQsStatusIconsContainer.orientation = LinearLayout.HORIZONTAL
                    mQsStatusIconsContainer.setId(constraintLayoutId)

                    if (mQsStatusIconsContainer.parent != null) {
                        (mQsStatusIconsContainer.parent as ViewGroup).removeView(
                            mQsStatusIconsContainer
                        )
                    }

                    if (mQsStatusIconsContainer.childCount > 0) {
                        mQsStatusIconsContainer.removeAllViews()
                    }

                    val linearLayoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    (iconContainer.parent as ViewGroup).removeView(iconContainer)
                    iconContainer.setLayoutParams(linearLayoutParams)
                    iconContainer.layoutParams.height = mContext.toPx(32)

                    (batteryIcon.parent as ViewGroup).removeView(batteryIcon)
                    batteryIcon.layoutParams.height = mContext.toPx(32)

                    mQsStatusIconsContainer.addView(iconContainer)
                    mQsStatusIconsContainer.addView(batteryIcon)

                    header!!.addView(mQsStatusIconsContainer, header!!.childCount - 1)

                    updateStatusIcons()
                }
            })

            hookAllMethods(shadeHeaderControllerClass, "updateResources", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!mShowQSStatusIconsBg && !fixedStatusIcons || hideStatusIcons) return

                    updateStatusIcons()
                }
            })
        }
    }

    companion object {
        private val TAG = "Iconify - ${BackgroundChip::class.java.simpleName}: "
    }
}
