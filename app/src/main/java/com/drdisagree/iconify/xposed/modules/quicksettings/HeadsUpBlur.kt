package com.drdisagree.iconify.xposed.modules.quicksettings

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.core.PackageParam
import com.drdisagree.iconify.xposed.core.hook.hookMethod
import com.drdisagree.iconify.xposed.core.hook.hookMethodMatchPattern
import com.drdisagree.iconify.xposed.core.hook.proceedWithArgs
import com.drdisagree.iconify.xposed.core.reflect.callMethod
import com.drdisagree.iconify.xposed.core.reflect.callMethodSilently
import com.drdisagree.iconify.xposed.core.reflect.findClass
import com.drdisagree.iconify.xposed.core.reflect.getExtraFieldSilently
import com.drdisagree.iconify.xposed.core.reflect.getField
import com.drdisagree.iconify.xposed.core.reflect.getFieldSilently
import com.drdisagree.iconify.xposed.core.reflect.hasField
import com.drdisagree.iconify.xposed.core.reflect.isMethodAvailable
import com.drdisagree.iconify.xposed.core.reflect.setExtraField
import com.drdisagree.iconify.xposed.core.reflect.setField
import com.drdisagree.iconify.xposed.modules.extras.callbacks.QsShowingCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.DisplayUtils.isNightMode
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.toPx
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import java.util.Collections
import java.util.WeakHashMap
import kotlin.math.roundToInt

class HeadsUpBlur(context: Context) : ModPack(context) {

    private var headsUpBlurEnabled = false
    private var headsUpBlurRadius = 12f
    private var headsUpTransparency = 17.5f
    private val notificationViews: MutableSet<View> = Collections.newSetFromMap(WeakHashMap())
    private var isQsExpanded = false
    private var coloredNotificationView = false
    private val hookedListenerClasses: MutableSet<Class<*>> = Collections.newSetFromMap(WeakHashMap())
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            headsUpBlurEnabled = getBoolean(XposedKey.NOTIFICATION_HEADS_UP_BLUR)
            headsUpBlurRadius = getFloat(XposedKey.NOTIFICATION_HEADS_UP_BLUR_RADIUS) / 100f * 25f
            headsUpTransparency =
                getFloat(XposedKey.NOTIFICATION_HEADS_UP_TRANSPARENCY) / 100f * 255f
            coloredNotificationView = getBoolean(XposedKey.COLORED_NOTIFICATION_VIEW)
        }
    }

    @SuppressLint("DiscouragedApi")
    override fun handleLoadPackage(packageParam: PackageParam) {
        val headsUpManagerClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.notification.headsup.HeadsUpManagerImpl",
            "$SYSTEMUI_PACKAGE.statusbar.policy.BaseHeadsUpManager",
            "$SYSTEMUI_PACKAGE.statusbar.policy.HeadsUpManager"
        )

        headsUpManagerClass
            .hookMethod("addListener")
            .runAfter { chain, _ ->
                val listenerClass = chain.args[0]?.javaClass ?: return@runAfter
                if (!hookedListenerClasses.add(listenerClass)) return@runAfter

                listenerClass
                    .hookMethod("onHeadsUpStateChanged")
                    .suppressError()
                    .runAfter runAfter2@{ listenerChain, _ ->
                        if (!headsUpBlurEnabled) return@runAfter2
                        if (listenerChain.thisObject?.javaClass != listenerClass) return@runAfter2

                        val row = listenerChain.args[0].headsUpRow() ?: return@runAfter2
                        val isHeadsUp = listenerChain.args.getOrNull(1) as? Boolean
                            ?: row.callMethod("isHeadsUpState") as Boolean

                        row.onHeadsUpChanged(isHeadsUp && row.isPinnedRow())
                    }
            }

        findClass(
            $$"$$SYSTEMUI_PACKAGE.statusbar.notification.headsup.HeadsUpManagerImpl$HeadsUpEntry",
            suppressError = true
        )
            .hookMethod("setRowPinnedStatus")
            .suppressError()
            .runAfter { chain, _ ->
                if (!headsUpBlurEnabled) return@runAfter

                val row = chain.thisObject.getFieldSilently("mEntry").headsUpRow()
                    ?: return@runAfter
                val isPinned = row.callMethodSilently("isPinned") as? Boolean ?: return@runAfter

                row.onHeadsUpChanged(isPinned)
            }

        val expandableNotificationRowClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.row.ExpandableNotificationRow")

        expandableNotificationRowClass
            .hookMethod("onAppearAnimationFinished")
            .runAfter { chain, _ ->
                if (!headsUpBlurEnabled) return@runAfter

                notificationViews.add(chain.thisObject as View)
            }

        val activatableNotificationViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.row.ActivatableNotificationView")

        activatableNotificationViewClass
            .hookMethod("startAppearAnimation")
            .runAfter { chain, _ ->
                if (!headsUpBlurEnabled) return@runAfter

                val isAppearing = chain.args[0] as Boolean
                val mBackgroundNormal = chain.thisObject.getField("mBackgroundNormal") as View
                val shouldApplyBlur = mBackgroundNormal.getExtraFieldSilently("shouldApplyBlur")
                        as? Boolean == true

                if (shouldApplyBlur && isAppearing && !isQsExpanded) {
                    chain.thisObject.updateNotificationBackground(true)
                } else {
                    chain.thisObject.scheduleBlurReset()
                }
            }

        fun updateAllNotificationBackgrounds(isQsExpanded: Boolean) {
            if (!headsUpBlurEnabled) return

            this.isQsExpanded = isQsExpanded

            notificationViews.forEach { view ->
                if (isQsExpanded) {
                    view.updateNotificationBackground(shouldApplyBlur = false)
                } else if (view.shouldApplyBlur() && view.isAttachedToWindow) {
                    view.updateNotificationBackground(shouldApplyBlur = true)
                }
            }
        }

        QsShowingCallback.getInstance().registerQsShowingListener(
            object : QsShowingCallback.QsShowingListener {
                override fun onQuickSettingsExpanded() {
                    updateAllNotificationBackgrounds(isQsExpanded = true)
                }

                override fun onQuickSettingsCollapsed() {
                    updateAllNotificationBackgrounds(isQsExpanded = false)
                }
            }
        )

        val notificationBackgroundViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.row.NotificationBackgroundView")

        val hasFocusOverlayRadii = notificationBackgroundViewClass.hasField("mFocusOverlayCornerRadii")

        // Replace the method with ours otherwise we get ClassCastException
        notificationBackgroundViewClass
            .hookMethod("updateBackgroundRadii")
            .intercept { chain ->
                if (!headsUpBlurEnabled || !hasFocusOverlayRadii) return@intercept chain.proceed()

                if (chain.thisObject.getField("mDontModifyCorners") as Boolean) {
                    return@intercept null
                    return@intercept chain.proceed()
                }

                val drawable = chain.thisObject.getField("mBackground") as Drawable

                if (drawable !is LayerDrawable) {
                    return@intercept null
                    return@intercept chain.proceed()
                }

                val numberOfLayers = drawable.numberOfLayers
                val mCornerRadii = chain.thisObject.getField("mCornerRadii") as FloatArray
                val mFocusOverlayCornerRadii =
                    chain.thisObject.getField("mFocusOverlayCornerRadii") as FloatArray
                val mFocusOverlayStroke =
                    chain.thisObject.getField("mFocusOverlayStroke") as Float

                for (i in 0 until numberOfLayers) {
                    val drawableItem = drawable.getDrawable(i)
                    if (drawableItem is GradientDrawable) {
                        drawableItem.cornerRadii = mCornerRadii
                    }
                }

                val gradientDrawable = drawable.findDrawableByLayerId(
                    mContext.resources.getIdentifier(
                        "notification_focus_overlay",
                        "id",
                        SYSTEMUI_PACKAGE
                    )
                )

                mCornerRadii.forEachIndexed { index, value ->
                    mFocusOverlayCornerRadii[index] = maxOf(0.0f, value - mFocusOverlayStroke)
                }

                if (gradientDrawable is GradientDrawable) {
                    gradientDrawable.cornerRadii = mFocusOverlayCornerRadii
                }

                null
            }

        // Replace original notification background drawable with our blur drawable
        notificationBackgroundViewClass
            .hookMethodMatchPattern("setCustomBackground.*")
            .intercept { chain ->
                chain.proceedWithArgs { args ->
                    if (!headsUpBlurEnabled) return@proceedWithArgs

                    val mBackgroundNormal = chain.thisObject as View

                    val blurDrawable = mBackgroundNormal.getExtraFieldSilently("mBackgroundDrawable")
                            as? Drawable ?: return@proceedWithArgs

                    if (args.isNotEmpty() && args[0] is Drawable) {
                        args[0] = blurDrawable
                        return@proceedWithArgs
                    }

                    var mBackground = mBackgroundNormal.getFieldSilently("mBackground") as? Drawable
                    if (mBackground != null) {
                        mBackground.callback = null
                        mBackgroundNormal.callMethod("unscheduleDrawable", mBackground)
                    }

                    mBackground = blurDrawable
                    mBackgroundNormal.setField("mBackground", blurDrawable)
                    mBackgroundNormal.setField("mRippleColor", null)
                    blurDrawable.mutate()

                    mBackground.callback = mBackgroundNormal
                    val mTintColor = mBackgroundNormal.getField("mTintColor")
                    mBackgroundNormal.callMethod("setTint", mTintColor)

                    if (mBackground is RippleDrawable) {
                        mBackground.callMethod("setForceSoftware", true)
                    }

                    mBackgroundNormal.callMethod("updateBackgroundRadii")
                    mBackgroundNormal.callMethod("invalidate")

                    return@intercept null
                }
            }
    }

    // Create blur drawable and set it to notification background
    @SuppressLint("DiscouragedApi")
    private fun Any.updateNotificationBackground(shouldApplyBlur: Boolean) {
        val mBackgroundNormal = getField("mBackgroundNormal") as View
        val context = mBackgroundNormal.context

        val notificationBgDrawable = ContextCompat.getDrawable(
            context,
            context.resources.getIdentifier(
                "notification_material_bg",
                "drawable",
                SYSTEMUI_PACKAGE
            )
        ) as LayerDrawable

        val colorName = if (context.isNightMode) "system_neutral1_900" else "system_neutral2_10"
        var notificationColor = context.resources.getColor(
            context.resources.getIdentifier(
                "android:color/$colorName",
                "color",
                context.packageName
            ),
            context.theme
        )
        var shouldApplyTint = false

        // Colored notification view support
        val sbn = getFieldSilently("mEntry")?.getFieldSilently("mSbn")
            ?: getFieldSilently("mEntryAdapter")?.callMethodSilently("getSbn")
        sbn?.let { mSbn ->
            val notification = mSbn.callMethod("getNotification") as Notification
            val mNotifyBackgroundColor =
                notification.getExtraFieldSilently("mNotifyBackgroundColor") as? Int

            if (mNotifyBackgroundColor != null) {
                notificationColor = mNotifyBackgroundColor
                shouldApplyTint = true
            }
        }

        fun updateColorAndOutline() {
            callMethod("updateBackgroundColors")

            callMethod("updateBackgroundTint", true)

            val outlineAlphaValue = 0.0f
            val mOutlineAlpha = getField("mOutlineAlpha") as Float

            if (outlineAlphaValue != mOutlineAlpha) {
                setField("mOutlineAlpha", outlineAlphaValue)
                callMethod("applyRoundnessAndInvalidate")
            }
        }

        fun setDefaultNotificationBackground() {
            val mutatedDrawable = notificationBgDrawable.mutate() as LayerDrawable

            if (shouldApplyTint) {
                DrawableCompat.setTint(mutatedDrawable, notificationColor)
            }

            mBackgroundNormal.setExtraField("mBackgroundDrawable", mutatedDrawable)
            mBackgroundNormal.setExtraField("mBlurApplied", false)

            setNotificationBackground(mBackgroundNormal, mutatedDrawable)

            updateColorAndOutline()
        }

        val mBlurApplied =
            mBackgroundNormal.getExtraFieldSilently("mBlurApplied") as? Boolean == true

        if (shouldApplyBlur && !mBlurApplied) {
            mBackgroundNormal.doOnAttach {
                if (!headsUpBlurEnabled || isQsExpanded || !shouldApplyBlur()) return@doOnAttach
                if (mBackgroundNormal.getExtraFieldSilently("mBlurApplied") == true) return@doOnAttach

                val blurDrawable = mBackgroundNormal
                    .callMethod("getViewRootImpl")
                    .callMethod("createBackgroundBlurDrawable") as? Drawable
                    ?: return@doOnAttach

                blurDrawable.callMethod(
                    "setCornerRadius",
                    (context.resources.getDimensionPixelSize(
                        context.resources.getIdentifier(
                            "notification_corner_radius",
                            "dimen",
                            SYSTEMUI_PACKAGE
                        )
                    ) - mContext.toPx(4)).coerceAtLeast(0).toFloat()
                )
                blurDrawable.callMethod(
                    "setBlurRadius",
                    context.toPx(headsUpBlurRadius.roundToInt())
                )
                blurDrawable.callMethod(
                    "setColor",
                    ColorUtils.setAlphaComponent(notificationColor, headsUpTransparency.toInt())
                )

                val mutatedDrawable = notificationBgDrawable.mutate() as LayerDrawable
                val baseLayer = mutatedDrawable.getDrawable(0).mutate().apply {
                    setTint(Color.TRANSPARENT)
                }
                val statefulLayer = mutatedDrawable.getDrawable(1).mutate().apply {
                    setTint(Color.TRANSPARENT)
                }

                val layerDrawable = LayerDrawable(
                    arrayOf(
                        baseLayer,
                        statefulLayer,
                        blurDrawable
                    )
                )

                mBackgroundNormal.setExtraField("mBackgroundDrawable", layerDrawable)
                mBackgroundNormal.setExtraField("mBlurApplied", true)

                setNotificationBackground(mBackgroundNormal, layerDrawable)

                updateColorAndOutline()

                mBackgroundNormal.doOnDetach {
                    setDefaultNotificationBackground()
                }
            }
        } else if (!shouldApplyBlur && mBlurApplied) {
            setDefaultNotificationBackground()
        }
    }

    private fun View.onHeadsUpChanged(isHeadsUp: Boolean) {
        val mBackgroundNormal = getField("mBackgroundNormal") as View
        mBackgroundNormal.setExtraField("shouldApplyBlur", isHeadsUp)
        notificationViews.add(this)

        if (isHeadsUp) {
            mainHandler.post {
                if (headsUpBlurEnabled && !isQsExpanded && shouldApplyBlur()) {
                    updateNotificationBackground(true)
                }
            }
        } else {
            scheduleBlurReset()
        }
    }

    private fun View.isPinnedRow(): Boolean =
        callMethodSilently("isPinned") as? Boolean ?: true

    private fun Any.shouldApplyBlur(): Boolean {
        val mBackgroundNormal = getFieldSilently("mBackgroundNormal") as? View ?: return false
        return mBackgroundNormal.getExtraFieldSilently("shouldApplyBlur") as? Boolean == true
    }

    private fun Any.scheduleBlurReset() {
        mainHandler.postDelayed({
            if (!shouldApplyBlur() || isQsExpanded) updateNotificationBackground(false)
        }, BLUR_RESET_DELAY)
    }

    private fun Any?.headsUpRow(): View? {
        if (this == null) return null

        (getFieldSilently("row") as? View)?.let { return it }

        val entry = getFieldSilently("entry") ?: callMethodSilently("getEntry")
        return entry?.getFieldSilently("row") as? View
    }

    private fun setNotificationBackground(
        mBackgroundNormal: View,
        layerDrawable: LayerDrawable
    ) {
        if (mBackgroundNormal.isMethodAvailable("setCustomBackground", Drawable::class.java)) {
            mBackgroundNormal.callMethod("setCustomBackground", layerDrawable)
        } else {
            mBackgroundNormal.callMethod("setCustomBackground$1")
        }
    }

    companion object {
        private const val BLUR_RESET_DELAY = 500L
    }
}
