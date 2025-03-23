package com.drdisagree.iconify.xposed.modules.statusbar

import android.R
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
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_CLICKABLE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.DUAL_STATUSBAR
import com.drdisagree.iconify.data.common.Preferences.HIDE_LOCKSCREEN_CARRIER
import com.drdisagree.iconify.data.common.Preferences.HIDE_LOCKSCREEN_STATUSBAR
import com.drdisagree.iconify.data.common.Preferences.NOTIFICATION_ICONS_LIMIT
import com.drdisagree.iconify.data.common.Preferences.SB_CLOCK_SIZE
import com.drdisagree.iconify.data.common.Preferences.SB_CLOCK_SIZE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.SHOW_4G_INSTEAD_OF_LTE
import com.drdisagree.iconify.data.common.Preferences.SHOW_CLOCK_ON_RIGHT_SIDE
import com.drdisagree.iconify.xposed.HookRes.Companion.resParams
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.BackgroundChip
import com.drdisagree.iconify.xposed.modules.extras.utils.StatusBarClock.getCenterClockView
import com.drdisagree.iconify.xposed.modules.extras.utils.StatusBarClock.getLeftClockView
import com.drdisagree.iconify.xposed.modules.extras.utils.StatusBarClock.getRightClockView
import com.drdisagree.iconify.xposed.modules.extras.utils.StatusBarClock.setClockGravity
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookLayout
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

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
    private var clockOnRightSide = false
    private var show4GInsteadOfLTE = false
    private var notifIconsLimit = -1
    private var dualStatusbarEnabled = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            sbClockSizeSwitch = getBoolean(SB_CLOCK_SIZE_SWITCH, false)
            sbClockSize = getSliderInt(SB_CLOCK_SIZE, 14)
            hideLockscreenCarrier = getBoolean(HIDE_LOCKSCREEN_CARRIER, false)
            hideLockscreenStatusbar = getBoolean(HIDE_LOCKSCREEN_STATUSBAR, false)
            clockOnRightSide = getBoolean(SHOW_CLOCK_ON_RIGHT_SIDE, false)
            show4GInsteadOfLTE = getBoolean(SHOW_4G_INSTEAD_OF_LTE, false)
            notifIconsLimit = getSliderInt(NOTIFICATION_ICONS_LIMIT, -1)
            dualStatusbarEnabled = getBoolean(DUAL_STATUSBAR, false)
            mClockClickable = getBoolean(CHIP_STATUSBAR_CLOCK_CLICKABLE_SWITCH, false)
        }

        when (key.firstOrNull()) {
            in setOf(
                SB_CLOCK_SIZE_SWITCH,
                SB_CLOCK_SIZE
            ) -> setClockSize()

            in setOf(
                HIDE_LOCKSCREEN_CARRIER,
                HIDE_LOCKSCREEN_STATUSBAR
            ) -> hideLockscreenCarrierOrStatusbar()
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        hideLockscreenCarrierOrStatusbar()
        applyClockSize()
        clockOnRightSide()
        show4GInsteadOfLTE()
        notificationIconsLimit()
        clickableClockView()
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

    private fun clockOnRightSide() {
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

            if (clockOnRightSide && !dualStatusbarEnabled) {
                (statusBarClock?.parent as? ViewGroup)?.removeView(statusBarClock)
                statusBarContents?.addView(statusBarClock)
                statusBarClock?.setPaddingRelative(endPadding, 0, startPadding, 0)
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
            findClass("com.android.settingslib.mobile.MobileMappings\$Config")

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

                listOf(
                    mClockView,
                    mCenterClockView,
                    mRightClockView
                ).forEach { clockView ->
                    if (mClockClickable && clockView != null) {
                        // Add click animation for Clock Chip
                        setClockChipClickable(mContext, clockView, BackgroundChip.cornerRadii)

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
                    intArrayOf(R.attr.state_pressed),
                    pressedAnim
                )
                stateListAnimator.addState(
                    intArrayOf(R.attr.state_focused),
                    pressedAnim
                )
                stateListAnimator.addState(intArrayOf(), defaultAnim)

                clockView.stateListAnimator = stateListAnimator
            }
        }
    }
}