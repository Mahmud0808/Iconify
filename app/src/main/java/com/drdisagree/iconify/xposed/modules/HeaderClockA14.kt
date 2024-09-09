package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.os.UserManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.TextUtilsCompat
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.ACTION_BOOT_COMPLETED
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_CENTERED
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT1
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT2
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT3
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_TEXT1
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_TEXT2
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_EXPANSION_Y
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_TEXT_SCALING
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_LANDSCAPE_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SIDEMARGIN
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_STYLE
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.HIDE_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.common.Preferences.ICONIFY_HEADER_CLOCK_TAG
import com.drdisagree.iconify.common.Preferences.ICONIFY_QS_HEADER_CONTAINER_SHADE_TAG
import com.drdisagree.iconify.common.Preferences.ICONIFY_QS_HEADER_CONTAINER_TAG
import com.drdisagree.iconify.common.Preferences.OP_QS_HEADER_SWITCH
import com.drdisagree.iconify.common.Preferences.QSPANEL_HIDE_CARRIER
import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.utils.TextUtils
import com.drdisagree.iconify.xposed.HookRes.Companion.resParams
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.Helpers.findClassInArray
import com.drdisagree.iconify.xposed.modules.utils.Helpers.getColorResCompat
import com.drdisagree.iconify.xposed.modules.utils.TouchAnimator
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.applyFontRecursively
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.applyTextScalingRecursively
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.findViewContainsTag
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.findViewWithTagAndChangeColor
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.setMargins
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.findClassIfExists
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam
import de.robv.android.xposed.callbacks.XC_LayoutInflated
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SuppressLint("DiscouragedApi")
class HeaderClockA14(context: Context?) : ModPack(context!!) {

    private var appContext: Context? = null
    private var showHeaderClock = false
    private var centeredClockView = false
    private var mQsHeaderClockContainer: LinearLayout = LinearLayout(mContext)
    private var mQsHeaderContainerShade: LinearLayout = LinearLayout(mContext).apply {
        tag = ICONIFY_QS_HEADER_CONTAINER_SHADE_TAG
    }
    private var mQsClockContainer: LinearLayout = LinearLayout(mContext)
    private var mQsIconsContainer: LinearLayout = LinearLayout(mContext)
    private var mQsPanelView: ViewGroup? = null
    private var mQuickStatusBarHeader: FrameLayout? = null
    private var mUserManager: UserManager? = null
    private var mActivityStarter: Any? = null
    private var mQQSContainerAnimator: TouchAnimator? = null
    private var mQQSExpansionY: Float = 8f
    private var hideQsCarrierGroup = false
    private var hideStatusIcons = false
    private var mBroadcastRegistered = false
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action != null) {
                if (intent.action == ACTION_BOOT_COMPLETED) {
                    updateClockView()
                }
            }
        }
    }
    private val mViewOnClickListener = View.OnClickListener { v: View ->
        val tag = v.tag.toString()
        if (tag == "clock") {
            onClockClick()
        } else if (tag == "date") {
            onDateClick()
        }
    }
    private var showOpQsHeaderView = false

    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized) return

        Xprefs.apply {
            showHeaderClock = getBoolean(HEADER_CLOCK_SWITCH, false)
            centeredClockView = getBoolean(HEADER_CLOCK_CENTERED, false)
            mQQSExpansionY = getSliderInt(HEADER_CLOCK_EXPANSION_Y, 24).toFloat()
            hideQsCarrierGroup = getBoolean(QSPANEL_HIDE_CARRIER, false)
            hideStatusIcons = getBoolean(HIDE_STATUS_ICONS_SWITCH, false)
            showOpQsHeaderView = getBoolean(OP_QS_HEADER_SWITCH, false)
        }

        if (key.isNotEmpty()) {
            if (key[0] == HEADER_CLOCK_EXPANSION_Y) {
                buildHeaderViewExpansion()
            }

            if (key[0] == HEADER_CLOCK_SWITCH ||
                key[0] == HEADER_CLOCK_COLOR_SWITCH ||
                key[0] == HEADER_CLOCK_COLOR_CODE_ACCENT1 ||
                key[0] == HEADER_CLOCK_COLOR_CODE_ACCENT2 ||
                key[0] == HEADER_CLOCK_COLOR_CODE_ACCENT3 ||
                key[0] == HEADER_CLOCK_COLOR_CODE_TEXT1 ||
                key[0] == HEADER_CLOCK_COLOR_CODE_TEXT2 ||
                key[0] == HEADER_CLOCK_FONT_SWITCH ||
                key[0] == HEADER_CLOCK_SIDEMARGIN ||
                key[0] == HEADER_CLOCK_TOPMARGIN ||
                key[0] == HEADER_CLOCK_STYLE ||
                key[0] == HEADER_CLOCK_CENTERED ||
                key[0] == HEADER_CLOCK_FONT_TEXT_SCALING ||
                key[0] == HEADER_CLOCK_LANDSCAPE_SWITCH
            ) {
                updateClockView()
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        if (!mBroadcastRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(ACTION_BOOT_COMPLETED)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mContext.registerReceiver(
                    mReceiver,
                    intentFilter,
                    Context.RECEIVER_EXPORTED
                )
            } else {
                mContext.registerReceiver(
                    mReceiver,
                    intentFilter
                )
            }

            mBroadcastRegistered = true
        }

        initResources(mContext)

        val qsPanelClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.QSPanel",
            loadPackageParam.classLoader
        )
        val qsImplClass = findClassInArray(
            loadPackageParam.classLoader,
            "$SYSTEMUI_PACKAGE.qs.QSImpl",
            "$SYSTEMUI_PACKAGE.qs.QSFragment"
        )
        val shadeHeaderControllerClass = findClassInArray(
            loadPackageParam.classLoader,
            "$SYSTEMUI_PACKAGE.shade.LargeScreenShadeHeaderController",
            "$SYSTEMUI_PACKAGE.shade.ShadeHeaderController"
        )
        val qsPanelControllerBase = findClassIfExists(
            "$SYSTEMUI_PACKAGE.qs.QSPanelControllerBase",
            loadPackageParam.classLoader
        )
        val qsSecurityFooterUtilsClass = findClassIfExists(
            "$SYSTEMUI_PACKAGE.qs.QSSecurityFooterUtils",
            loadPackageParam.classLoader
        )
        val quickStatusBarHeaderClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader",
            loadPackageParam.classLoader
        )
        val dependencyClass = findClass(
            "$SYSTEMUI_PACKAGE.Dependency",
            loadPackageParam.classLoader
        )
        val activityStarterClass = findClass(
            "$SYSTEMUI_PACKAGE.plugins.ActivityStarter",
            loadPackageParam.classLoader
        )

        if (qsSecurityFooterUtilsClass == null) {
            hookAllConstructors(quickStatusBarHeaderClass, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    mActivityStarter = callStaticMethod(
                        dependencyClass,
                        "get",
                        activityStarterClass
                    )
                }
            })
        } else {
            hookAllConstructors(qsSecurityFooterUtilsClass, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    mActivityStarter = getObjectField(
                        param.thisObject,
                        "mActivityStarter"
                    )
                }
            })
        }

        hookAllMethods(quickStatusBarHeaderClass, "onFinishInflate", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!showHeaderClock) return

                mQuickStatusBarHeader = param.thisObject as FrameLayout

                mQsHeaderClockContainer.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = mContext.toPx(16)
                    }
                    orientation = LinearLayout.HORIZONTAL
                }

                mQsHeaderContainerShade.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.VERTICAL
                }

                mQsClockContainer.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1f
                    )
                    orientation = LinearLayout.VERTICAL
                }

                mQsIconsContainer.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.END or Gravity.CENTER
                }

                mQsHeaderClockContainer.apply {
                    (this.parent as? ViewGroup)?.removeView(this)
                    removeAllViews()
                    addView(mQsClockContainer)
                    addView(mQsIconsContainer)
                }

                (mQsHeaderClockContainer.parent as? ViewGroup)?.removeView(mQsHeaderClockContainer)
                val headerImageAvailable = mQuickStatusBarHeader!!.findViewWithTag<ViewGroup?>(
                    ICONIFY_QS_HEADER_CONTAINER_TAG
                )
                mQuickStatusBarHeader!!.addView(
                    mQsHeaderClockContainer,
                    if (headerImageAvailable == null) {
                        -1
                    } else {
                        mQuickStatusBarHeader!!.indexOfChild(headerImageAvailable) + 1
                    }
                )

                handleOldHeaderView(param)

                updateClockView()
            }
        })

        hookAllMethods(quickStatusBarHeaderClass, "updateResources", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!showHeaderClock) return

                mQuickStatusBarHeader = param.thisObject as FrameLayout

                buildHeaderViewExpansion()

                val isLandscape =
                    mContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                if (isLandscape) {
                    if (mQsHeaderClockContainer.parent != mQsHeaderContainerShade) {
                        (mQsHeaderClockContainer.parent as? ViewGroup)?.removeView(
                            mQsHeaderClockContainer
                        )
                        mQsHeaderContainerShade.addView(mQsHeaderClockContainer, 0)
                    }
                    mQsHeaderContainerShade.visibility = View.VISIBLE
                } else {
                    if (mQsHeaderClockContainer.parent != mQuickStatusBarHeader) {
                        val headerImageAvailable =
                            mQuickStatusBarHeader!!.findViewWithTag<ViewGroup?>(
                                ICONIFY_QS_HEADER_CONTAINER_TAG
                            )
                        (mQsHeaderClockContainer.parent as? ViewGroup)?.removeView(
                            mQsHeaderClockContainer
                        )
                        mQuickStatusBarHeader?.addView(
                            mQsHeaderClockContainer,
                            if (headerImageAvailable == null) {
                                0
                            } else {
                                mQuickStatusBarHeader!!.indexOfChild(headerImageAvailable) + 1
                            }
                        )
                    }
                    mQsHeaderContainerShade.visibility = View.GONE
                }

                updateClockView()
            }
        })

        val hasSwitchAllContentToParent = qsPanelClass.declaredMethods.any {
            it.name == "switchAllContentToParent"
        }
        val hasSwitchToParentMethod = qsPanelClass.declaredMethods.any { method ->
            method.name == "switchToParent" &&
                    method.parameterTypes.contentEquals(
                        arrayOf(View::class.java, ViewGroup::class.java, Int::class.java)
                    )
        }

        if (hasSwitchAllContentToParent && hasSwitchToParentMethod) {
            hookAllMethods(qsPanelClass, "switchAllContentToParent", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!showHeaderClock) return

                    val parent = param.args[0] as ViewGroup
                    val mMovableContentStartIndex = getObjectField(
                        param.thisObject, "mMovableContentStartIndex"
                    ) as Int
                    val index = if (parent === param.thisObject) mMovableContentStartIndex else 0
                    val targetParentId = mContext.resources.getIdentifier(
                        "quick_settings_panel",
                        "id",
                        mContext.packageName
                    )

                    if (parent.id == targetParentId) {
                        val checkExistingView =
                            parent.findViewWithTag<ViewGroup?>(ICONIFY_QS_HEADER_CONTAINER_SHADE_TAG)
                        if (checkExistingView != null) {
                            mQsHeaderContainerShade = checkExistingView as LinearLayout
                            if (parent.indexOfChild(mQsHeaderContainerShade) == index) {
                                return
                            }
                        }

                        callMethod(
                            param.thisObject,
                            "switchToParent",
                            mQsHeaderContainerShade,
                            parent,
                            index
                        )
                    }
                }
            })

            if (showHeaderClock) {
                findAndHookMethod(
                    qsPanelClass,
                    "switchToParent",
                    View::class.java,
                    ViewGroup::class.java,
                    Int::class.java,
                    object : XC_MethodReplacement() {
                        override fun replaceHookedMethod(param: MethodHookParam): Any? {
                            val view = param.args[0] as View
                            val parent = param.args[1] as ViewGroup
                            val tempIndex = param.args[2] as Int
                            val index = if (view.tag == ICONIFY_QS_HEADER_CONTAINER_SHADE_TAG) {
                                tempIndex
                            } else {
                                tempIndex + 1
                            }
                            val tag = callMethod(param.thisObject, "getDumpableTag")

                            callMethod(
                                param.thisObject,
                                "switchToParent",
                                view,
                                parent,
                                index.coerceAtMost(parent.childCount),
                                tag
                            )

                            return null
                        }
                    }
                )
            }

            if (showHeaderClock) {
                findAndHookMethod(
                    qsPanelClass,
                    "switchToParent",
                    View::class.java,
                    ViewGroup::class.java,
                    Int::class.java,
                    String::class.java,
                    object : XC_MethodReplacement() {
                        override fun replaceHookedMethod(param: MethodHookParam): Any? {
                            val view = param.args[0] as View
                            val newParent = param.args[1] as? ViewGroup
                            val tempIndex = param.args[2] as Int

                            if (newParent == null) {
                                return null
                            }

                            val index = if (view.tag == ICONIFY_QS_HEADER_CONTAINER_SHADE_TAG) {
                                tempIndex
                            } else {
                                tempIndex + 1
                            }.coerceAtMost(newParent.childCount)

                            val currentParent = view.parent as? ViewGroup

                            if (currentParent != newParent) {
                                currentParent?.removeView(view)
                                newParent.addView(view, index.coerceAtMost(newParent.childCount))
                            } else if (newParent.indexOfChild(view) == index) {
                                return null
                            } else {
                                newParent.removeView(view)
                                newParent.addView(view, index.coerceAtMost(newParent.childCount))
                            }

                            return null
                        }
                    }
                )
            }
        } else { // Some ROMs don't have this method switchAllContentToParent()
            hookAllMethods(
                qsPanelControllerBase,
                "onInit",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        mQsPanelView = getObjectField(
                            param.thisObject,
                            "mView"
                        ) as ViewGroup
                    }
                }
            )

            findAndHookMethod(
                qsPanelClass,
                "switchToParent",
                View::class.java,
                ViewGroup::class.java,
                Int::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!showHeaderClock ||
                            mQsPanelView == null ||
                            (param.args[1] as? ViewGroup) == null
                        ) return

                        val parent = param.args[1] as ViewGroup
                        val mMovableContentStartIndex = getObjectField(
                            mQsPanelView, "mMovableContentStartIndex"
                        ) as Int
                        val index = if (parent === mQsPanelView) mMovableContentStartIndex else 0
                        val targetParentId = mContext.resources.getIdentifier(
                            "quick_settings_panel",
                            "id",
                            SYSTEMUI_PACKAGE
                        )

                        if (parent.id == targetParentId) {
                            val mQsHeaderContainerShadeParent =
                                mQsHeaderContainerShade.parent as? ViewGroup
                            if (mQsHeaderContainerShadeParent != parent ||
                                mQsHeaderContainerShadeParent.indexOfChild(mQsHeaderContainerShade) != index
                            ) {
                                mQsHeaderContainerShadeParent?.removeView(mQsHeaderContainerShade)
                                parent.addView(
                                    mQsHeaderContainerShade,
                                    index.coerceAtMost(parent.childCount)
                                )
                            }
                        }

                        param.args[2] = (param.args[2] as Int) + 1
                    }
                }
            )
        }

        hookAllMethods(qsImplClass, "setQsExpansion", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!showHeaderClock) return

                val onKeyguard = callMethod(
                    param.thisObject,
                    "isKeyguardState"
                ) as Boolean
                val mShowCollapsedOnKeyguard = getObjectField(
                    param.thisObject,
                    "mShowCollapsedOnKeyguard"
                ) as Boolean

                val onKeyguardAndExpanded = onKeyguard && !mShowCollapsedOnKeyguard
                val expansion = param.args[0] as Float

                setExpansion(onKeyguardAndExpanded, expansion);
            }
        })

        hookAllMethods(
            android.content.res.Resources::class.java,
            "getBoolean",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!showHeaderClock) return

                    val resId1 = mContext.resources.getIdentifier(
                        "config_use_split_notification_shade",
                        "bool",
                        SYSTEMUI_PACKAGE
                    )

                    val resId2 = mContext.resources.getIdentifier(
                        "config_skinnyNotifsInLandscape",
                        "bool",
                        SYSTEMUI_PACKAGE
                    )

                    if (param.args[0] == resId1) {
                        val isLandscape =
                            mContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                        param.result = isLandscape
                    } else if (param.args[0] == resId2) {
                        param.result = false
                    }
                }
            }
        )

        hookAllMethods(
            android.content.res.Resources::class.java,
            "getDimensionPixelSize",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!showHeaderClock) return

                    val res1 = mContext.resources.getIdentifier(
                        "qs_brightness_margin_top",
                        "dimen",
                        SYSTEMUI_PACKAGE
                    )

                    if (res1 != 0 && param.args[0] == res1) {
                        param.result = 0
                    }
                }
            }
        )

        hookAllMethods(shadeHeaderControllerClass, "onInit", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!showHeaderClock) return

                val clock = getObjectField(param.thisObject, "clock") as TextView
                (clock.parent as? ViewGroup)?.removeView(clock)

                val date = getObjectField(param.thisObject, "date") as TextView
                (date.parent as? ViewGroup)?.removeView(date)

                mQsIconsContainer.removeAllViews()

                try {
                    val qsCarrierGroup =
                        getObjectField(param.thisObject, "qsCarrierGroup") as LinearLayout
                    (qsCarrierGroup.parent as? ViewGroup)?.removeView(qsCarrierGroup)
                    if (hideQsCarrierGroup) qsCarrierGroup.visibility = View.GONE
                    mQsIconsContainer.addView(qsCarrierGroup)
                } catch (ignored: Throwable) {
                    val mShadeCarrierGroup =
                        getObjectField(
                            param.thisObject,
                            "mShadeCarrierGroup"
                        ) as LinearLayout
                    (mShadeCarrierGroup.parent as? ViewGroup)?.removeView(mShadeCarrierGroup)
                    if (hideQsCarrierGroup) mShadeCarrierGroup.visibility = View.GONE
                    mQsIconsContainer.addView(mShadeCarrierGroup)
                }

                try {
                    val systemIconsHoverContainer =
                        getObjectField(
                            param.thisObject,
                            "systemIconsHoverContainer"
                        ) as LinearLayout
                    (systemIconsHoverContainer.parent as? ViewGroup)?.removeView(
                        systemIconsHoverContainer
                    )
                    if (hideStatusIcons) systemIconsHoverContainer.visibility = View.GONE
                    mQsIconsContainer.addView(systemIconsHoverContainer)
                } catch (ignored: Throwable) {
                    val iconsContainer = LinearLayout(mContext).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            mContext.toPx(32)
                        )
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.END or Gravity.CENTER
                    }

                    val statusIcons = getObjectField(param.thisObject, "iconContainer") as View
                    (statusIcons.parent as? ViewGroup)?.removeView(statusIcons)

                    val batteryIcon = getObjectField(param.thisObject, "batteryIcon") as View
                    (batteryIcon.parent as? ViewGroup)?.removeView(batteryIcon)

                    iconsContainer.apply {
                        addView(statusIcons)
                        addView(batteryIcon)
                        if (hideStatusIcons) visibility = View.GONE
                        mQsIconsContainer.addView(this)
                    }
                }
            }
        })

        handleLegacyHeaderView()

        try {
            val executor = Executors.newSingleThreadScheduledExecutor()
            executor.scheduleAtFixedRate({
                val androidDir =
                    File(Environment.getExternalStorageDirectory().toString() + "/Android")

                if (androidDir.isDirectory) {
                    updateClockView()
                    executor.shutdown()
                    executor.shutdownNow()
                }
            }, 0, 5, TimeUnit.SECONDS)
        } catch (ignored: Throwable) {
        }
    }

    private fun buildHeaderViewExpansion() {
        val isLandscape =
            mContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val mQQSExpansionY = if (isLandscape) 0 else mQQSExpansionY

        val builderP: TouchAnimator.Builder = TouchAnimator.Builder()
            .addFloat(
                mQsHeaderClockContainer,
                "translationY",
                0F,
                mContext.toPx(mQQSExpansionY.toInt()).toFloat()
            )

        mQQSContainerAnimator = builderP.build()
    }

    private fun setExpansion(forceExpanded: Boolean, expansionFraction: Float) {
        val keyguardExpansionFraction = if (forceExpanded) 1f else expansionFraction
        mQQSContainerAnimator?.setPosition(keyguardExpansionFraction)

        mQsHeaderClockContainer.alpha = if (forceExpanded) expansionFraction else 1f
    }

    private fun initResources(context: Context) {
        try {
            appContext = context.createPackageContext(
                BuildConfig.APPLICATION_ID,
                Context.CONTEXT_IGNORE_SECURITY
            )
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        Handler(Looper.getMainLooper()).post {
            mUserManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        }
    }

    private fun updateClockView() {
        if (!showHeaderClock) return

        val isClockAdded =
            mQsClockContainer.findViewWithTag<View?>(ICONIFY_HEADER_CLOCK_TAG) != null

        if (isClockAdded) {
            mQsClockContainer.removeView(
                mQsClockContainer.findViewWithTag(
                    ICONIFY_HEADER_CLOCK_TAG
                )
            )
        }

        clockView?.let {
            if (centeredClockView) {
                mQsClockContainer.gravity = Gravity.CENTER
            } else {
                mQsClockContainer.gravity = Gravity.START
            }

            it.tag = ICONIFY_HEADER_CLOCK_TAG

            TextUtils.convertTextViewsToTitleCase(it)

            mQsClockContainer.addView(it)

            it.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT

            modifyClockView(it)
            setOnClickListener(it)
        }
    }

    private val clockView: View?
        get() {
            if (appContext == null || !XprefsIsInitialized) return null

            val inflater = LayoutInflater.from(appContext)
            val clockStyle: Int = Xprefs.getInt(HEADER_CLOCK_STYLE, 0)

            return inflater.inflate(
                appContext!!.resources.getIdentifier(
                    Resources.HEADER_CLOCK_LAYOUT + clockStyle,
                    "layout",
                    BuildConfig.APPLICATION_ID
                ),
                null
            )
        }

    private fun modifyClockView(clockView: View) {
        if (!XprefsIsInitialized) return

        val isLandscape =
            mContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val clockStyle: Int = Xprefs.getInt(HEADER_CLOCK_STYLE, 0)
        val customFontEnabled: Boolean = Xprefs.getBoolean(HEADER_CLOCK_FONT_SWITCH, false)
        val clockScale: Float =
            (Xprefs.getSliderInt(HEADER_CLOCK_FONT_TEXT_SCALING, 10) / 10.0).toFloat()
        val sideMargin: Int = Xprefs.getSliderInt(HEADER_CLOCK_SIDEMARGIN, 0)
        val topMargin: Int = if (isLandscape) 0 else Xprefs.getSliderInt(HEADER_CLOCK_TOPMARGIN, 8)
        val customFont = Environment.getExternalStorageDirectory().toString() +
                "/.iconify_files/headerclock_font.ttf"

        val customColorEnabled = Xprefs.getBoolean(HEADER_CLOCK_COLOR_SWITCH, false)
        var accent1: Int = mContext.resources.getColor(
            mContext.resources.getIdentifier(
                "android:color/system_accent1_300",
                "color",
                mContext.packageName
            ), mContext.theme
        )
        var accent2: Int = mContext.resources.getColor(
            mContext.resources.getIdentifier(
                "android:color/system_accent2_300",
                "color",
                mContext.packageName
            ), mContext.theme
        )
        var accent3: Int = mContext.resources.getColor(
            mContext.resources.getIdentifier(
                "android:color/system_accent3_300",
                "color",
                mContext.packageName
            ), mContext.theme
        )
        var textPrimary: Int = getColorResCompat(mContext, android.R.attr.textColorPrimary)
        var textInverse: Int = getColorResCompat(mContext, android.R.attr.textColorPrimaryInverse)

        if (customColorEnabled) {
            accent1 = Xprefs.getInt(
                HEADER_CLOCK_COLOR_CODE_ACCENT1,
                mContext.resources.getColor(
                    mContext.resources.getIdentifier(
                        "android:color/system_accent1_300",
                        "color",
                        mContext.packageName
                    ), mContext.theme
                )
            )
            accent2 = Xprefs.getInt(
                HEADER_CLOCK_COLOR_CODE_ACCENT2,
                mContext.resources.getColor(
                    mContext.resources.getIdentifier(
                        "android:color/system_accent2_300",
                        "color",
                        mContext.packageName
                    ), mContext.theme
                )
            )
            accent3 = Xprefs.getInt(
                HEADER_CLOCK_COLOR_CODE_ACCENT3,
                mContext.resources.getColor(
                    mContext.resources.getIdentifier(
                        "android:color/system_accent3_300",
                        "color",
                        mContext.packageName
                    ), mContext.theme
                )
            )
            textPrimary = Xprefs.getInt(
                HEADER_CLOCK_COLOR_CODE_TEXT1,
                getColorResCompat(mContext, android.R.attr.textColorPrimary)
            )
            textInverse = Xprefs.getInt(
                HEADER_CLOCK_COLOR_CODE_TEXT2,
                getColorResCompat(mContext, android.R.attr.textColorPrimaryInverse)
            )
        }

        var typeface: Typeface? = null

        if (customFontEnabled && File(customFont).exists()) typeface =
            Typeface.createFromFile(File(customFont))


        setMargins(mQsHeaderClockContainer, mContext, 0, topMargin, 0, 0)

        if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL) {
            setMargins(clockView, mContext, 0, 0, sideMargin, 0)
        } else {
            setMargins(clockView, mContext, sideMargin, 0, 0, 0)
        }

        findViewWithTagAndChangeColor(clockView, "accent1", accent1)
        findViewWithTagAndChangeColor(clockView, "accent2", accent2)
        findViewWithTagAndChangeColor(clockView, "accent3", accent3)
        findViewWithTagAndChangeColor(clockView, "text1", textPrimary)
        findViewWithTagAndChangeColor(clockView, "text2", textInverse)
        findViewWithTagAndChangeColor(clockView, "gradient", accent1, accent2, 26);

        if (typeface != null) {
            applyFontRecursively(clockView, typeface)
        }

        if (clockScale != 1f) {
            applyTextScalingRecursively(clockView, clockScale)
        }

        when (clockStyle) {
            6 -> {
                val imageView =
                    clockView.findViewContainsTag("user_profile_image") as ImageView?
                userImage?.let { imageView?.setImageDrawable(it) }
            }
        }
    }

    private val userImage: Drawable?
        get() = if (mUserManager == null) {
            ResourcesCompat.getDrawable(
                appContext!!.resources,
                R.drawable.default_avatar,
                appContext!!.theme
            )
        } else try {
            val getUserIconMethod =
                mUserManager!!.javaClass.getMethod(
                    "getUserIcon",
                    Int::class.javaPrimitiveType
                )
            val userId =
                UserHandle::class.java.getDeclaredMethod("myUserId").invoke(null) as Int
            val bitmapUserIcon = getUserIconMethod.invoke(mUserManager, userId) as Bitmap

            BitmapDrawable(mContext.resources, bitmapUserIcon)
        } catch (throwable: Throwable) {
            if (throwable !is NullPointerException) {
                log(TAG + throwable)
            }

            ResourcesCompat.getDrawable(
                appContext!!.resources,
                R.drawable.default_avatar,
                appContext!!.theme
            )
        }

    private fun setOnClickListener(view: View?) {
        if (view == null) return

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child: View = view.getChildAt(i)
                val tag = if (child.tag == null) "" else child.tag.toString()

                if (tag.lowercase(Locale.getDefault()).contains("clock") ||
                    tag.lowercase(Locale.getDefault()).contains("date")
                ) {
                    child.setOnClickListener(mViewOnClickListener)
                }

                (child as? ViewGroup)?.let { setOnClickListener(it) }
            }
        } else {
            val tag = if (view.tag == null) "" else view.tag.toString()

            if (tag.lowercase(Locale.getDefault()).contains("clock") ||
                tag.lowercase(Locale.getDefault()).contains("date")
            ) {
                view.setOnClickListener(mViewOnClickListener)
            }
        }
    }

    private fun onClockClick() {
        if (mActivityStarter == null) return

        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)

        callMethod(mActivityStarter, "postStartActivityDismissingKeyguard", intent, 0)
    }

    private fun onDateClick() {
        if (mActivityStarter == null) return

        val builder = CalendarContract.CONTENT_URI.buildUpon()
        builder.appendPath("time")
        builder.appendPath(System.currentTimeMillis().toString())

        val intent = Intent(Intent.ACTION_VIEW, builder.build())

        callMethod(mActivityStarter, "postStartActivityDismissingKeyguard", intent, 0)
    }

    private fun handleOldHeaderView(param: XC_MethodHook.MethodHookParam) {
        if (!showHeaderClock) return

        try {
            val mDateView =
                getObjectField(param.thisObject, "mDateView") as View
            mDateView.layoutParams.height = 0
            mDateView.layoutParams.width = 0
            mDateView.visibility = View.INVISIBLE
        } catch (ignored: Throwable) {
        }

        try {
            val mClockView =
                getObjectField(param.thisObject, "mClockView") as TextView
            mClockView.visibility = View.INVISIBLE
            mClockView.setTextAppearance(0)
            mClockView.setTextColor(0)
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
            val mQSCarriers =
                getObjectField(param.thisObject, "mQSCarriers") as View
            mQSCarriers.visibility = View.INVISIBLE
        } catch (ignored: Throwable) {
        }
    }

    private fun handleLegacyHeaderView() {
        if (!showHeaderClock) return

        val resParam: InitPackageResourcesParam = resParams[SYSTEMUI_PACKAGE] ?: return

        try {
            resParam.res.hookLayout(
                SYSTEMUI_PACKAGE,
                "layout",
                "quick_qs_status_icons",
                object : XC_LayoutInflated() {
                    override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                        if (!showHeaderClock) return

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
                            date.visibility = View.GONE
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
                            jrClock.visibility = View.GONE
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
                            jrDate.visibility = View.GONE
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
                        if (!showHeaderClock) return

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
                            date.visibility = View.GONE
                        } catch (ignored: Throwable) {
                        }
                    }
                })
        } catch (ignored: Throwable) {
        }
    }

    companion object {
        private val TAG = "Iconify - ${HeaderClockA14::class.java.simpleName}: "
    }
}