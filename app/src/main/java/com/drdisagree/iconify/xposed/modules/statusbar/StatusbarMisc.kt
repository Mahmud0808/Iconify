package com.drdisagree.iconify.xposed.modules.statusbar

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.StateListAnimator
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.XResources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.provider.AlarmClock
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_SB_CENTER_CLOCK_CONTAINER_TAG
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.HookRes.Companion.resParams
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.GraphicsColorKt
import com.drdisagree.iconify.xposed.modules.extras.SettingsLibUtils
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.StatusBarClock.getCenterClockView
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.StatusBarClock.getLeftClockView
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.StatusBarClock.getRightClockView
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.StatusBarClock.setClockGravity
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callStaticMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookLayout
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setStaticField
import com.drdisagree.iconify.xposed.modules.extras.views.AlphaOptimizedLinearLayout
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import kotlin.math.roundToInt

@SuppressLint("DiscouragedApi")
class StatusbarMisc(context: Context) : ModPack(context) {

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
    private var clockPosition = 0
    private var show4GInsteadOfLTE = false
    private var notifIconsLimit = -1
    private var dualStatusbarEnabled = false
    private var linkToCustomColor = false
    private var darkIconDispatcherImplInstance: Any? = null

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            sbClockSizeSwitch = getBoolean(XposedKey.STATUSBAR_CLOCK_TEXT_SIZE_SWITCH)
            sbClockSize = getInt(XposedKey.STATUSBAR_CLOCK_TEXT_SIZE)
            hideLockscreenCarrier = getBoolean(XposedKey.HIDE_LOCKSCREEN_CARRIER)
            hideLockscreenStatusbar = getBoolean(XposedKey.HIDE_LOCKSCREEN_STATUSBAR)
            clockPosition = getString(XposedKey.STATUSBAR_CLOCK_POSITION).toInt()
            show4GInsteadOfLTE = getBoolean(XposedKey.SHOW_4G_INSTEAD_OF_LTE)
            notifIconsLimit = getInt(XposedKey.NOTIFICATION_ICONS_LIMIT)
            dualStatusbarEnabled = getBoolean(XposedKey.DUAL_STATUSBAR)
            mClockClickable = getBoolean(XposedKey.STATUSBAR_CLOCK_CLICKABLE)
            linkToCustomColor = getBoolean(XposedKey.STATUSBAR_LINK_TO_CUSTOM_COLOR)
        }

        when (key.firstOrNull()) {
            in setOf(
                XposedKey.STATUSBAR_CLOCK_TEXT_SIZE_SWITCH.name,
                XposedKey.STATUSBAR_CLOCK_TEXT_SIZE.name
            ) -> setClockSize()

            in setOf(
                XposedKey.HIDE_LOCKSCREEN_CARRIER.name,
                XposedKey.HIDE_LOCKSCREEN_STATUSBAR.name
            ) -> hideLockscreenCarrierOrStatusbar()

            XposedKey.STATUSBAR_LINK_TO_CUSTOM_COLOR.name,
            XposedKey.STATUSBAR_CUSTOM_COLOR_CHANGED.name -> applyIconTint()
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        hideLockscreenCarrierOrStatusbar()
        applyClockSize()
        setClockPosition()
        show4GInsteadOfLTE()
        notificationIconsLimit()
        clickableClockView()
        setStatusbarColor()
    }

    private fun hideLockscreenCarrierOrStatusbar() {
        val xResources: XResources = resParams[SYSTEMUI_PACKAGE]?.res ?: return

        xResources
            .hookLayout()
            .packageName(SYSTEMUI_PACKAGE)
            .resource("layout", "keyguard_status_bar")
            .suppressError()
            .run { liparam ->
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
                    } catch (_: Throwable) {
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
                    } catch (_: Throwable) {
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
                    } catch (_: Throwable) {
                    }
                }
            }
    }

    private fun applyClockSize() {
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

        fun addClockTextListener() {
            mClockView?.addTextChangedListener(textChangeListener)
            mCenterClockView?.addTextChangedListener(textChangeListener)
            mRightClockView?.addTextChangedListener(textChangeListener)
        }

        fun removeClockTextListener() {
            mClockView?.removeTextChangedListener(textChangeListener)
            mCenterClockView?.removeTextChangedListener(textChangeListener)
            mRightClockView?.removeTextChangedListener(textChangeListener)
        }

        fun updateClockTextSize() {
            mLeftClockSize = mClockView?.textSize?.toInt() ?: 14
            mCenterClockSize = mCenterClockView?.textSize?.toInt() ?: 14
            mRightClockSize = mRightClockView?.textSize?.toInt() ?: 14

            setClockSize()
            addClockTextListener()
        }

        val collapsedStatusBarFragment = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.CollapsedStatusBarFragment",
            "$SYSTEMUI_PACKAGE.statusbar.phone.fragment.CollapsedStatusBarFragment"
        )

        collapsedStatusBarFragment
            .hookMethod("onViewCreated")
            .parameters(
                View::class.java,
                Bundle::class.java
            )
            .runAfter { param ->
                mClockView = getLeftClockView(mContext, param) as? TextView
                mCenterClockView = getCenterClockView(mContext, param) as? TextView
                mRightClockView = getRightClockView(mContext, param) as? TextView

                updateClockTextSize()
            }

        val phoneStatusBarViewControllerClass = findClass(
            "com.android.systemui.statusbar.phone.PhoneStatusBarViewController",
            suppressError = true
        )

        phoneStatusBarViewControllerClass
            .hookMethod("onViewAttached")
            .runAfter { param ->
                mClockView = param.thisObject.getField("clock") as TextView
                mCenterClockView = null
                mRightClockView = null

                updateClockTextSize()
            }

        phoneStatusBarViewControllerClass
            .hookMethod("onViewDetached")
            .runBefore { removeClockTextListener() }
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

    private fun setClockPosition() {
        val phoneStatusBarViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.PhoneStatusBarView")
        val shadeHeaderControllerClass =
            findClass("$SYSTEMUI_PACKAGE.shade.ShadeHeaderController")

        var phoneStatusBarViewParam: ViewGroup? = null

        fun ViewGroup?.moveStatusBarClock() {
            if (this == null) return

            val statusBarContents = findViewById<ViewGroup>(
                mContext.resources.getIdentifier(
                    "status_bar_contents",
                    "id",
                    mContext.packageName
                )
            )
            val statusBarClock = findViewById<View>(
                mContext.resources.getIdentifier(
                    "clock",
                    "id",
                    mContext.packageName
                )
            )
            val startPadding = mContext.resources.getDimensionPixelSize(
                mContext.resources.getIdentifier(
                    "status_bar_left_clock_starting_padding",
                    "dimen",
                    mContext.packageName
                )
            )
            val endPadding = mContext.resources.getDimensionPixelSize(
                mContext.resources.getIdentifier(
                    "status_bar_left_clock_end_padding",
                    "dimen",
                    mContext.packageName
                )
            )

            if (!dualStatusbarEnabled) {
                when (clockPosition) {
                    0 -> { // Left
                        // do nothing, clock is on the left by default
                    }

                    1 -> { // Center
                        val container =
                            findViewWithTag<LinearLayout>(ICONIFY_SB_CENTER_CLOCK_CONTAINER_TAG)
                                ?: AlphaOptimizedLinearLayout(mContext).apply {
                                    tag = ICONIFY_SB_CENTER_CLOCK_CONTAINER_TAG
                                    gravity = Gravity.CENTER
                                    orientation = LinearLayout.HORIZONTAL
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT
                                    )
                                    setPadding(
                                        0,
                                        mContext.resources.getDimensionPixelSize(
                                            mContext.resources.getIdentifier(
                                                "status_bar_padding_top",
                                                "dimen",
                                                mContext.packageName
                                            )
                                        ),
                                        0,
                                        0
                                    )
                                    reAddView(statusBarClock)
                                }
                        reAddView(container)
                        statusBarClock?.setPaddingRelative(0, 0, 0, 0)
                        (statusBarClock?.layoutParams as? ViewGroup.MarginLayoutParams)
                            ?.setMargins(0, 0, 0, 0)
                        (statusBarClock?.layoutParams as? LinearLayout.LayoutParams)?.gravity =
                            Gravity.CENTER
                    }

                    2 -> { // Right
                        statusBarContents?.reAddView(statusBarClock)
                        statusBarClock?.setPaddingRelative(0, 0, 0, 0)
                        (statusBarClock?.layoutParams as? ViewGroup.MarginLayoutParams)
                            ?.setMargins(endPadding, 0, startPadding, 0)
                        (statusBarClock?.layoutParams as? LinearLayout.LayoutParams)?.gravity =
                            Gravity.CENTER_VERTICAL or Gravity.END
                    }
                }
            }
        }

        phoneStatusBarViewClass
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                phoneStatusBarViewParam = param.thisObject as ViewGroup

                phoneStatusBarViewParam.moveStatusBarClock()
            }

        shadeHeaderControllerClass
            .hookMethod("updateQQSPaddings")
            .suppressError()
            .runAfter { phoneStatusBarViewParam.moveStatusBarClock() }
    }

    private fun show4GInsteadOfLTE() {
        val mobileMappingsConfigClass =
            findClass($$"com.android.settingslib.mobile.MobileMappings$Config")

        mobileMappingsConfigClass
            .hookMethod("readConfig")
            .runAfter { param ->
                param.result.setField("show4gForLte", show4GInsteadOfLTE)
            }
    }

    private fun notificationIconsLimit() {
        ResourceHookManager
            .hookInteger()
            .forPackageName(SYSTEMUI_PACKAGE)
            .whenCondition { notifIconsLimit != -1 }
            .addResource("max_notif_static_icons") { notifIconsLimit }
            .addResource("max_notif_icons_on_lockscreen") { notifIconsLimit }
            .apply()
    }

    private fun clickableClockView() {
        val phoneStatusBarViewControllerClass = findClass(
            "com.android.systemui.statusbar.phone.PhoneStatusBarViewController",
            suppressError = true
        )

        phoneStatusBarViewControllerClass
            .hookMethod("onViewAttached")
            .runAfter { param ->
                mClockView = param.thisObject.getField("clock") as TextView
                mCenterClockView = null
                mRightClockView = null

                listOf(
                    mClockView,
                    mCenterClockView,
                    mRightClockView
                ).forEach { clockView ->
                    if (mClockClickable && clockView != null) {
                        // Add click animation for Clock Chip
                        setClockChipClickable(mContext, clockView, ClockChip.cornerRadii)

                        clockView.setOnClickListener {
                            try {
                                // First try to open the clock app via ACTION_SHOW_ALARMS
                                mContext.startActivity(
                                    Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                )
                            } catch (_: Throwable) {
                                try {
                                    // Fallback: Open the Google Clock app directly
                                    mContext.startActivity(
                                        Intent(Intent.ACTION_MAIN).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            component = ComponentName(
                                                "com.google.android.deskclock",
                                                "com.android.deskclock.DeskClock"
                                            )
                                        }
                                    )
                                } catch (_: Throwable) {
                                    try {
                                        // Second fallback: Try AOSP Clock app
                                        mContext.startActivity(
                                            Intent(Intent.ACTION_MAIN).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                component = ComponentName(
                                                    "com.android.deskclock",
                                                    "com.android.deskclock.DeskClock"
                                                )
                                            }
                                        )
                                    } catch (throwable: Throwable) {
                                        log(
                                            this@StatusbarMisc,
                                            "Could not open any clock app: $throwable"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun setStatusbarColor() {
        val darkIconDispatcherImplClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.DarkIconDispatcherImpl")

        fun updateStatusbarColor(param: XC_MethodHook.MethodHookParam) {
            if (!linkToCustomColor) return

            val (statusbarColorLight, statusbarColorDark) = getStatusbarColors(mContext)

            param.thisObject.apply {
                setField("mLightModeIconColorSingleTone", statusbarColorLight)
                setField("mDarkModeIconColorSingleTone", statusbarColorDark)
                setField("mLightModeContrastColor", statusbarColorLight)
                setField("mDarkModeContrastColor", statusbarColorDark)
            }
        }

        darkIconDispatcherImplClass
            .hookConstructor()
            .runAfter { param ->
                darkIconDispatcherImplInstance = param.thisObject
                updateStatusbarColor(param)
            }

        darkIconDispatcherImplClass
            .hookMethod(
                "addDarkReceiver",
                "applyDark",
                "applyDarkIntensity",
                "applyIconTint"
            )
            .runBefore { param ->
                darkIconDispatcherImplInstance = param.thisObject
                updateStatusbarColor(param)
            }

        updateBatteryColors()
        applyIconTint()
    }

    private fun applyIconTint() {
        if (darkIconDispatcherImplInstance == null) return

        val (statusbarColorLight, statusbarColorDark) = getStatusbarColors(mContext)

        val mDarkIntensity = darkIconDispatcherImplInstance.getField("mDarkIntensity") as Float
        val argbEvaluator = ArgbEvaluator::class.java.callStaticMethod("getInstance")

        val mIconTint = argbEvaluator.callMethod(
            "evaluate",
            mDarkIntensity,
            statusbarColorLight,
            statusbarColorDark
        ).callMethod("intValue")
        val mContrastTint = argbEvaluator.callMethod(
            "evaluate",
            mDarkIntensity,
            statusbarColorLight,
            statusbarColorDark
        ).callMethod("intValue")

        darkIconDispatcherImplInstance.apply {
            setField("mIconTint", mIconTint)
            setField("mContrastTint", mContrastTint)
            callMethod("applyIconTint")
        }
    }

    private fun updateBatteryColors() {
        if (!linkToCustomColor) return

        val (statusbarColorLight, statusbarColorDark) = getStatusbarColors(mContext)

        val batteryLightThemeClass =
            findClass($$"$$SYSTEMUI_PACKAGE.statusbar.pipeline.battery.shared.ui.BatteryColors$LightTheme")
        val batteryDarkThemeClass =
            findClass($$"$$SYSTEMUI_PACKAGE.statusbar.pipeline.battery.shared.ui.BatteryColors$DarkTheme")

        batteryLightThemeClass.setStaticField(
            "lowAlphaBg",
            GraphicsColorKt.colorOf(
                ColorUtils.setAlphaComponent(
                    statusbarColorLight,
                    (255 * 0.20f).roundToInt()
                )
            )
        )
        batteryLightThemeClass.setStaticField(
            "highAlphaBg",
            GraphicsColorKt.colorOf(
                ColorUtils.setAlphaComponent(
                    statusbarColorLight,
                    (255 * 0.55f).roundToInt()
                )
            )
        )
        batteryDarkThemeClass.setStaticField(
            "lowAlphaBg",
            GraphicsColorKt.colorOf(
                ColorUtils.setAlphaComponent(
                    statusbarColorLight,
                    (255 * 0.45f).roundToInt()
                )
            )
        )
        batteryDarkThemeClass.setStaticField(
            "highAlphaBg",
            GraphicsColorKt.colorOf(
                ColorUtils.setAlphaComponent(
                    statusbarColorLight,
                    (255 * 0.55f).roundToInt()
                )
            )
        )

        val batteryLightThemeDefaultClass =
            findClass($$"$$SYSTEMUI_PACKAGE.statusbar.pipeline.battery.shared.ui.BatteryColors$LightTheme$Default")
        val batteryDarkThemeDefaultClass =
            findClass($$"$$SYSTEMUI_PACKAGE.statusbar.pipeline.battery.shared.ui.BatteryColors$DarkTheme$Default")

        batteryLightThemeDefaultClass.setStaticField(
            "fill",
            GraphicsColorKt.colorOf(statusbarColorLight)
        )
        batteryDarkThemeDefaultClass.setStaticField(
            "fill",
            GraphicsColorKt.colorOf(statusbarColorDark)
        )
    }

    companion object {

        private var mClockClickable = false

        fun setClockChipClickable(
            mContext: Context,
            clockView: View,
            cornerRadius: FloatArray
        ) {
            if (mClockClickable && clockView.background != null) {
                clockView.isClickable = true
                clockView.isFocusable = true

                // Add a ripple effect
                val rippleColor = ColorStateList.valueOf(
                    ColorUtils.setAlphaComponent(
                        Color.WHITE,
                        102 // 0.4f
                    )
                )

                val pixelCornerRadii = cornerRadius.map {
                    mContext.toPx(it.toInt()).toFloat()
                }.toFloatArray()
                val mask = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadii = pixelCornerRadii
                    setColor(Color.WHITE)
                }
                val rippleDrawable = RippleDrawable(
                    rippleColor,
                    clockView.background, mask
                )
                clockView.background = rippleDrawable

                // Add a StateListAnimator for scaling animation
                val stateListAnimator = StateListAnimator()

                // Animation for pressed state: Scale to 90%
                val pressedAnim = ObjectAnimator.ofPropertyValuesHolder(
                    clockView,
                    PropertyValuesHolder.ofFloat("scaleX", 0.9f),
                    PropertyValuesHolder.ofFloat("scaleY", 0.9f)
                ).apply {
                    duration = 100
                    interpolator = AccelerateDecelerateInterpolator()
                }

                // Animation for normal state: Scale back to 100%
                val defaultAnim = ObjectAnimator.ofPropertyValuesHolder(
                    clockView,
                    PropertyValuesHolder.ofFloat("scaleX", 1.0f),
                    PropertyValuesHolder.ofFloat("scaleY", 1.0f)
                ).apply {
                    duration = 200
                    interpolator = AccelerateDecelerateInterpolator()
                }

                // Add the animations to the StateListAnimator
                stateListAnimator.addState(
                    intArrayOf(android.R.attr.state_pressed),
                    pressedAnim
                )
                stateListAnimator.addState(
                    intArrayOf(android.R.attr.state_focused),
                    pressedAnim
                )
                stateListAnimator.addState(intArrayOf(), defaultAnim)

                clockView.stateListAnimator = stateListAnimator
            }
        }

        fun getStatusbarColors(context: Context): Pair<Int, Int> {
            val statusbarColorLight = SettingsLibUtils.getColorStateListDefaultColor(
                context,
                context.resources.getIdentifier(
                    "light_mode_icon_color_single_tone",
                    "color",
                    SYSTEMUI_PACKAGE
                )
            )
            val statusbarColorDark = SettingsLibUtils.getColorStateListDefaultColor(
                context,
                context.resources.getIdentifier(
                    "dark_mode_icon_color_single_tone",
                    "color",
                    SYSTEMUI_PACKAGE
                )
            )

            return Pair(statusbarColorLight, statusbarColorDark)
        }
    }
}