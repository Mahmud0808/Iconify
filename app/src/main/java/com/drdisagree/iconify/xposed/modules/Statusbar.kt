package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.COLORED_STATUSBAR_ICON
import com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_CARRIER
import com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_STATUSBAR
import com.drdisagree.iconify.common.Preferences.SB_CLOCK_SIZE
import com.drdisagree.iconify.common.Preferences.SB_CLOCK_SIZE_SWITCH
import com.drdisagree.iconify.xposed.HookRes.Companion.resParams
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.Helpers.findClassInArray
import com.drdisagree.iconify.xposed.modules.utils.StatusBarClock.getCenterClockView
import com.drdisagree.iconify.xposed.modules.utils.StatusBarClock.getLeftClockView
import com.drdisagree.iconify.xposed.modules.utils.StatusBarClock.getRightClockView
import com.drdisagree.iconify.xposed.modules.utils.StatusBarClock.setClockGravity
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.findClassIfExists
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.setObjectField
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam
import de.robv.android.xposed.callbacks.XC_LayoutInflated
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class Statusbar(context: Context?) : ModPack(context!!) {

    private var mColoredStatusbarIcon = false
    private var sbClockSizeSwitch = false
    private var sbClockSize = 14
    private var mClockView: TextView? = null
    private var mCenterClockView: TextView? = null
    private var mRightClockView: TextView? = null
    private var mLeftClockSize = 14
    private var mCenterClockSize = 14
    private var mRightClockSize = 14
    private var hideLockscreenCarrier = false
    private var hideLockscreenStatusbar = false

    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized) return

        Xprefs.apply {
            mColoredStatusbarIcon = getBoolean(COLORED_STATUSBAR_ICON, false)
            sbClockSizeSwitch = getBoolean(SB_CLOCK_SIZE_SWITCH, false)
            sbClockSize = getInt(SB_CLOCK_SIZE, 14)
            hideLockscreenCarrier = getBoolean(HIDE_LOCKSCREEN_CARRIER, false)
            hideLockscreenStatusbar = getBoolean(HIDE_LOCKSCREEN_STATUSBAR, false)
        }

        if (key.isNotEmpty()) {
            key[0].let {
                if (it == SB_CLOCK_SIZE_SWITCH ||
                    it == SB_CLOCK_SIZE
                ) {
                    setClockSize()
                }

                if (it == HIDE_LOCKSCREEN_CARRIER ||
                    it == HIDE_LOCKSCREEN_STATUSBAR
                ) {
                    hideLockscreenCarrierOrStatusbar()
                }
            }
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        setColoredNotificationIcons(loadPackageParam)
        hideLockscreenCarrierOrStatusbar()
        applyClockSize(loadPackageParam)
    }

    private fun setColoredNotificationIcons(loadPackageParam: LoadPackageParam) {
        if (!mColoredStatusbarIcon) return

        val notificationIconContainerClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.NotificationIconContainer",
            loadPackageParam.classLoader
        )
        val iconStateClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.NotificationIconContainer\$IconState",
            loadPackageParam.classLoader
        )
        val legacyNotificationIconAreaControllerImplClass = findClassIfExists(
            "$SYSTEMUI_PACKAGE.statusbar.phone.LegacyNotificationIconAreaControllerImpl",
            loadPackageParam.classLoader
        )
        val drawableSizeClass = findClassIfExists(
            "$SYSTEMUI_PACKAGE.util.drawable.DrawableSize",
            loadPackageParam.classLoader
        )
        val scalingDrawableWrapperClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.ScalingDrawableWrapper",
            loadPackageParam.classLoader
        )
        val statusBarIconViewClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.StatusBarIconView",
            loadPackageParam.classLoader
        )

        findAndHookMethod(
            notificationIconContainerClass,
            "applyIconStates",
            @Suppress("UNCHECKED_CAST")
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val mIconStates: HashMap<View, Any> = getObjectField(
                        param.thisObject,
                        "mIconStates"
                    ) as HashMap<View, Any>

                    for (icon in mIconStates.keys) {
                        removeTintForStatusbarIcon(icon)
                    }
                }
            }
        )

        findAndHookMethod(
            iconStateClass,
            "initFrom",
            View::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    removeTintForStatusbarIcon(param)
                }
            }
        )

        findAndHookMethod(
            iconStateClass,
            "applyToView",
            View::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    removeTintForStatusbarIcon(param)
                }
            }
        )

        hookAllMethods(
            statusBarIconViewClass,
            "updateIconColor",
            object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Any? {
                    return null
                }
            }
        )

        legacyNotificationIconAreaControllerImplClass?.let { thisClass ->
            hookAllMethods(
                thisClass,
                "updateTintForIcon",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        removeTintForStatusbarIcon(param)

                        try {
                            val view = param.args[0] as View
                            callMethod(
                                view,
                                "setStaticDrawableColor",
                                0 // StatusBarIconView.NO_COLOR
                            )
                            callMethod(
                                view,
                                "setDecorColor",
                                Color.WHITE
                            )
                        } catch (ignored: Throwable) {
                        }
                    }
                }
            )
        }

        try {
            findAndHookMethod(
                statusBarIconViewClass,
                "getIcon",
                Context::class.java,
                Context::class.java,
                "com.android.internal.statusbar.StatusBarIcon",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val sysuiContext = param.args[0] as Context
                        val context = param.args[1] as Context
                        val statusBarIcon = param.args[2]

                        setNotificationIcon(
                            statusBarIcon,
                            context,
                            sysuiContext,
                            drawableSizeClass,
                            param,
                            scalingDrawableWrapperClass
                        )
                    }
                }
            )
        } catch (ignored: Throwable) {
            findAndHookMethod(
                statusBarIconViewClass,
                "getIcon",
                "com.android.internal.statusbar.StatusBarIcon",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val sysuiContext = mContext
                        var context: Context? = null
                        val statusBarIcon = param.args[0]
                        val statusBarNotification = getObjectField(
                            param.thisObject,
                            "mNotification"
                        )

                        if (statusBarNotification != null) {
                            context = callMethod(
                                statusBarNotification,
                                "getPackageContext",
                                mContext
                            ) as Context?
                        }

                        if (context == null) {
                            context = mContext
                        }

                        setNotificationIcon(
                            statusBarIcon,
                            context,
                            sysuiContext,
                            drawableSizeClass,
                            param,
                            scalingDrawableWrapperClass
                        )
                    }
                }
            )
        }
    }

    private fun removeTintForStatusbarIcon(param: XC_MethodHook.MethodHookParam) {
        val icon = param.args[0] as View
        removeTintForStatusbarIcon(icon)
    }

    private fun removeTintForStatusbarIcon(icon: View) {
        try {
            val pkgName = getObjectField(
                getObjectField(
                    icon,
                    "mIcon"
                ),
                "pkg"
            ) as String

            if (!pkgName.contains("systemui")) {
                setObjectField(
                    icon,
                    "mCurrentSetColor",
                    0 // StatusBarIconView.NO_COLOR
                )
                callMethod(
                    icon,
                    "updateIconColor"
                )
            }
        } catch (ignored: Throwable) {
            log(TAG + ignored)
        }
    }

    private fun setNotificationIcon(
        statusBarIcon: Any?,
        context: Context,
        sysuiContext: Context,
        drawableSize: Class<*>?,
        param: XC_MethodHook.MethodHookParam,
        scalingDrawableWrapper: Class<*>
    ) {
        var icon: Drawable
        val res = sysuiContext.resources
        val pkgName = getObjectField(statusBarIcon, "pkg") as String

        if (listOf("com.android", "systemui").any { pkgName.contains(it) }) {
            return
        }

        try {
            icon = context.packageManager.getApplicationIcon(pkgName)
        } catch (e: Throwable) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isLowRamDevice = callStaticMethod(
                ActivityManager::class.java,
                "isLowRamDeviceStatic"
            ) as Boolean

            val maxIconSize = res.getDimensionPixelSize(
                res.getIdentifier(
                    if (isLowRamDevice) {
                        "notification_small_icon_size_low_ram"
                    } else {
                        "notification_small_icon_size"
                    },
                    "dimen",
                    FRAMEWORK_PACKAGE
                )
            )

            if (drawableSize != null) {
                icon = callStaticMethod(
                    drawableSize,
                    "downscaleToSize",
                    res,
                    icon,
                    maxIconSize,
                    maxIconSize
                ) as Drawable
            }
        }

        val typedValue = TypedValue()
        res.getValue(
            res.getIdentifier(
                "status_bar_icon_scale_factor",
                "dimen",
                SYSTEMUI_PACKAGE
            ),
            typedValue,
            true
        )
        val scaleFactor = typedValue.float

        if (scaleFactor == 1f) {
            param.result = icon
        } else {
            param.result = scalingDrawableWrapper.getConstructor(
                Drawable::class.java,
                Float::class.javaPrimitiveType
            ).newInstance(icon, scaleFactor)
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

    private fun applyClockSize(loadPackageParam: LoadPackageParam) {
        val collapsedStatusBarFragment = findClassInArray(
            loadPackageParam.classLoader,
            "$SYSTEMUI_PACKAGE.statusbar.phone.CollapsedStatusBarFragment",
            "$SYSTEMUI_PACKAGE.statusbar.phone.fragment.CollapsedStatusBarFragment"
        )

        if (collapsedStatusBarFragment == null) {
            log(TAG + "CollapsedStatusBarFragment not found")
            return
        }

        findAndHookMethod(collapsedStatusBarFragment,
            "onViewCreated",
            View::class.java,
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    mClockView = getLeftClockView(mContext, param) as? TextView
                    mCenterClockView = getCenterClockView(mContext, param) as? TextView
                    mRightClockView = getRightClockView(mContext, param) as? TextView

                    mLeftClockSize = mClockView?.textSize?.toInt() ?: 14
                    mCenterClockSize = mCenterClockView?.textSize?.toInt() ?: 14
                    mRightClockSize = mRightClockView?.textSize?.toInt() ?: 14

                    setClockSize()

                    val textChangeListener = object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                        }

                        override fun afterTextChanged(s: Editable) {
                            setClockSize()
                        }
                    }

                    mClockView?.addTextChangedListener(textChangeListener)
                    mCenterClockView?.addTextChangedListener(textChangeListener)
                    mRightClockView?.addTextChangedListener(textChangeListener)
                }
            })

    }

    @SuppressLint("RtlHardcoded")
    private fun setClockSize() {
        val leftClockSize = if (sbClockSizeSwitch) sbClockSize else mLeftClockSize
        val centerClockSize = if (sbClockSizeSwitch) sbClockSize else mCenterClockSize
        val rightClockSize = if (sbClockSizeSwitch) sbClockSize else mRightClockSize
        val unit = if (sbClockSizeSwitch) TypedValue.COMPLEX_UNIT_SP else TypedValue.COMPLEX_UNIT_PX

        mClockView?.let {
            it.setTextSize(unit, leftClockSize.toFloat())

            if (sbClockSizeSwitch) {
                setClockGravity(it, Gravity.LEFT or Gravity.CENTER)
            }
        }

        mCenterClockView?.let {
            it.setTextSize(unit, centerClockSize.toFloat())

            if (sbClockSizeSwitch) {
                setClockGravity(it, Gravity.CENTER)
            }
        }

        mRightClockView?.let {
            it.setTextSize(unit, rightClockSize.toFloat())

            if (sbClockSizeSwitch) {
                setClockGravity(it, Gravity.RIGHT or Gravity.CENTER)
            }
        }
    }

    companion object {
        private val TAG = "Iconify - ${Statusbar::class.java.simpleName}: "
    }
}