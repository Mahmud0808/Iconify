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
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.COLORED_NOTIFICATION_VIEW_SWITCH
import com.drdisagree.iconify.data.common.Preferences.NOTIFICATION_HEADSUP_BLUR
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.DisplayUtils.isNightMode
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getExtraFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethodMatchPattern
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.isMethodAvailable
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setExtraField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.util.Collections
import java.util.WeakHashMap

class HeadsUpBlur(context: Context) : ModPack(context) {

    private var headsUpBlurEnabled = false
    private val notificationViews: MutableSet<View> = Collections.newSetFromMap(WeakHashMap())
    private var isQsExpanded = false
    private var coloredNotificationView = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            headsUpBlurEnabled = getBoolean(NOTIFICATION_HEADSUP_BLUR, false)
            coloredNotificationView = getBoolean(COLORED_NOTIFICATION_VIEW_SWITCH, false)
        }
    }

    @SuppressLint("DiscouragedApi")
    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val headsUpManagerClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.policy.BaseHeadsUpManager",
            "$SYSTEMUI_PACKAGE.statusbar.notification.headsup.HeadsUpManagerImpl",
            "$SYSTEMUI_PACKAGE.statusbar.policy.HeadsUpManager"
        )

        headsUpManagerClass
            .hookMethod("addListener")
            .runAfter { param ->
                if (!headsUpBlurEnabled) return@runAfter

                val listener = param.args[0]

                listener::class.java
                    .hookMethod("onHeadsUpStateChanged")
                    .runAfter runAfter2@{ param ->
                        if (!headsUpBlurEnabled) return@runAfter2

                        val row = param.args[0].getFieldSilently("row") as? View ?: return@runAfter2

                        val isHeadsUpState = row.callMethod("isHeadsUpState") as Boolean
                        val mBackgroundNormal = row.getField("mBackgroundNormal") as View

                        mBackgroundNormal.setExtraField("shouldApplyBlur", isHeadsUpState)

                        notificationViews.add(row)
                    }
            }

        val expandableNotificationRowClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.row.ExpandableNotificationRow")

        expandableNotificationRowClass
            .hookMethod("onAppearAnimationFinished")
            .runAfter { param ->
                if (!headsUpBlurEnabled) return@runAfter

                notificationViews.add(param.thisObject as View)
            }

        val activatableNotificationViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.row.ActivatableNotificationView")

        activatableNotificationViewClass
            .hookMethod("startAppearAnimation")
            .runAfter { param ->
                if (!headsUpBlurEnabled) return@runAfter

                val isAppearing = param.args[0] as Boolean
                val mBackgroundNormal = param.thisObject.getField("mBackgroundNormal") as View
                val shouldApplyBlur = mBackgroundNormal.getExtraFieldSilently("shouldApplyBlur")
                        as? Boolean == true

                if (shouldApplyBlur && isAppearing && !isQsExpanded) {
                    param.thisObject.updateNotificationBackground(true)
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        param.thisObject.updateNotificationBackground(false)
                    }, 500)
                }
            }

        val visualStabilityCoordinatorClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.collection.coordinator.VisualStabilityCoordinator")

        visualStabilityCoordinatorClass
            .hookConstructor()
            .runAfter { param ->
                if (!headsUpBlurEnabled) return@runAfter

                val mStatusBarStateControllerListener =
                    param.thisObject.getField("mStatusBarStateControllerListener")

                mStatusBarStateControllerListener::class.java
                    .hookMethod("onExpandedChanged")
                    .runAfter runAfter2@{ param2 ->
                        if (!headsUpBlurEnabled) return@runAfter2

                        isQsExpanded = param2.args[0] as Boolean

                        notificationViews.forEach { view ->
                            view.updateNotificationBackground(!isQsExpanded)
                        }
                    }
            }

        val notificationBackgroundViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.row.NotificationBackgroundView")

        // Replace the method with ours otherwise we get ClassCastException
        notificationBackgroundViewClass
            .hookMethod("updateBackgroundRadii")
            .runBefore { param ->
                if (!headsUpBlurEnabled) return@runBefore

                if (param.thisObject.getField("mDontModifyCorners") as Boolean) {
                    param.result = null
                    return@runBefore
                }

                val drawable = param.thisObject.getField("mBackground") as Drawable

                if (drawable is LayerDrawable) {
                    val numberOfLayers = drawable.numberOfLayers
                    val mCornerRadii = param.thisObject.getField("mCornerRadii") as FloatArray
                    val mFocusOverlayCornerRadii =
                        param.thisObject.getField("mFocusOverlayCornerRadii") as FloatArray
                    val mFocusOverlayStroke =
                        param.thisObject.getField("mFocusOverlayStroke") as Float

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
                }

                param.result = null
            }

        // Replace original notification background drawable with out blur drawable
        notificationBackgroundViewClass
            .hookMethodMatchPattern("setCustomBackground.*")
            .runBefore { param ->
                if (!headsUpBlurEnabled) return@runBefore

                val mBackgroundNormal = param.thisObject as View

                val blurDrawable = mBackgroundNormal.getExtraFieldSilently("mBackgroundDrawable")
                        as? Drawable ?: return@runBefore

                if (param.args.isNotEmpty() && param.args[0] is Drawable) {
                    param.args[0] = blurDrawable
                    return@runBefore
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

                param.result = null
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
        getFieldSilently("mEntry")?.let { mEntry ->
            val mSbn = mEntry.getField("mSbn")
            val notification = mSbn.callMethod("getNotification") as Notification
            val mNotifyBackgroundColor =
                notification.getExtraFieldSilently("mNotifyBackgroundColor") as? Int

            if (mNotifyBackgroundColor != null) {
                notificationColor = mNotifyBackgroundColor
                shouldApplyTint = true
            }
        }

        if (shouldApplyBlur) {
            val alpha = (255 * 0.7f).toInt()
            val blurDrawable = mBackgroundNormal
                .callMethod("getViewRootImpl")
                .callMethod("createBackgroundBlurDrawable") as? Drawable
                ?: return

            blurDrawable.callMethod(
                "setCornerRadius",
                context.resources.getDimensionPixelSize(
                    context.resources.getIdentifier(
                        "notification_scrim_corner_radius",
                        "dimen",
                        SYSTEMUI_PACKAGE
                    )
                ).toFloat()
            )
            blurDrawable.callMethod("setBlurRadius", context.toPx(12))
            blurDrawable.callMethod(
                "setColor",
                ColorUtils.setAlphaComponent(notificationColor, alpha)
            )

            val mutatedDrawable = notificationBgDrawable.mutate() as LayerDrawable
            val baseLayer = mutatedDrawable.getDrawable(0).apply {
                setTint(Color.TRANSPARENT)
            }
            val statefulLayer = mutatedDrawable.getDrawable(1).apply {
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

            setNotificationBackground(mBackgroundNormal, layerDrawable)
        } else {
            val mutatedDrawable = notificationBgDrawable.mutate() as LayerDrawable

            if (shouldApplyTint) {
                DrawableCompat.setTint(mutatedDrawable, notificationColor)
            }

            mBackgroundNormal.setExtraField("mBackgroundDrawable", mutatedDrawable)

            setNotificationBackground(mBackgroundNormal, mutatedDrawable)
        }

        callMethod("updateBackgroundColors")

        callMethod("updateBackgroundTint", true)

        val outlineAlphaValue = 0.0f
        val mOutlineAlpha = getField("mOutlineAlpha") as Float

        if (outlineAlphaValue != mOutlineAlpha) {
            setField("mOutlineAlpha", outlineAlphaValue)
            callMethod("applyRoundnessAndInvalidate")
        }
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
}