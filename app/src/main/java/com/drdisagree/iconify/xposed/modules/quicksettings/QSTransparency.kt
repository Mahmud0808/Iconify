package com.drdisagree.iconify.xposed.modules.quicksettings

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.widget.LinearLayout
import androidx.core.graphics.ColorUtils
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.core.PackageParam
import com.drdisagree.iconify.xposed.core.compose.ComposeToolkit
import com.drdisagree.iconify.xposed.core.hook.hookConstructor
import com.drdisagree.iconify.xposed.core.hook.hookMethod
import com.drdisagree.iconify.xposed.core.hook.proceedWithArgs
import com.drdisagree.iconify.xposed.core.log.log
import com.drdisagree.iconify.xposed.core.reflect.MemberResolver
import com.drdisagree.iconify.xposed.core.reflect.callMethod
import com.drdisagree.iconify.xposed.core.reflect.findClass
import com.drdisagree.iconify.xposed.core.reflect.getField
import com.drdisagree.iconify.xposed.core.reflect.getFieldSilently
import com.drdisagree.iconify.xposed.core.reflect.setField
import com.drdisagree.iconify.xposed.core.resources.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.callbacks.DozeCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ComposeViewHost
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import java.util.WeakHashMap
import kotlin.math.roundToInt

@SuppressLint("DiscouragedApi")
class QSTransparency(context: Context) : ModPack(context) {

    private val keyguardAlpha = 0.85f
    private var qsTransparencyActive = false
    private var onlyNotifTransparencyActive = false
    private var keepLockScreenShade = false
    private var qsAlpha = 60f
    private var blurEnabled = false
    private var blurRadius = 23
    private var quickSettingsController: Any? = null
    private val notificationScrimAlphas = WeakHashMap<Any, Any>()

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            qsTransparencyActive = getBoolean(XposedKey.QUICK_SETTINGS_TRANSPARENCY)
            onlyNotifTransparencyActive = Build.VERSION.SDK_INT < 37 &&
                    getBoolean(XposedKey.NOTIFICATION_TRANSPARENCY)
            keepLockScreenShade = getBoolean(XposedKey.LOCKSCREEN_SHADE)
            qsAlpha = getFloat(XposedKey.QUICK_SETTINGS_ALPHA_LEVEL) / 100f
            blurEnabled = getBoolean(XposedKey.QUICK_SETTINGS_BLUR)
            blurRadius = getInt(XposedKey.QUICK_SETTINGS_BLUR_RADIUS)
        }

        when (key.firstOrNull()) {
            XposedKey.QUICK_SETTINGS_TRANSPARENCY.name,
            XposedKey.NOTIFICATION_TRANSPARENCY.name,
            XposedKey.QUICK_SETTINGS_ALPHA_LEVEL.name -> updateQsScrimRadius()
        }
    }

    override fun handleLoadPackage(packageParam: PackageParam) {
        setQsTransparency()
        setBlurRadius()
    }

    private fun setQsTransparency() {
        val shadeColorsClass = findClass("$SYSTEMUI_PACKAGE.shade.ui.ShadeColors")

        shadeColorsClass
            .hookMethod("shadePanel")
            .intercept { chain ->
                var result = chain.proceed()
                if (!qsTransparencyActive && !onlyNotifTransparencyActive) return@intercept result
                if (!qsTransparencyActive && !isCalledFromShadeScene()) return@intercept result
                if (result == null) return@intercept result

                result = ColorUtils.setAlphaComponent(
                    result as Int,
                    (255 * qsAlpha).roundToInt()
                )

                result
            }

        val scrimControllerClass = findClass("$SYSTEMUI_PACKAGE.statusbar.phone.ScrimController")

        scrimControllerClass
            .hookMethod("updateScrimColor")
            .suppressError()
            .intercept { chain ->
                chain.proceedWithArgs { args ->
                    if (!qsTransparencyActive && !onlyNotifTransparencyActive) return@proceedWithArgs

                    val alphaIndex = if (args[2] is Float) 2 else 1
                    val scrimState = chain.thisObject.getField("mState").toString()

                    if (scrimState == "KEYGUARD") {
                        if (!keepLockScreenShade) args[alphaIndex] = 0.0f
                    } else if (scrimState.contains("BOUNCER")) {
                        args[alphaIndex] = args[alphaIndex] as Float * keyguardAlpha
                    } else {
                        val scrimName = when (args[0]) {
                            MemberResolver.findField(
                                chain.thisObject.javaClass,
                                "mScrimInFront"
                            )[chain.thisObject] -> "front_scrim"

                            MemberResolver.findField(
                                chain.thisObject.javaClass,
                                "mScrimBehind"
                            )[chain.thisObject] -> "behind_scrim"

                            MemberResolver.findField(
                                chain.thisObject.javaClass,
                                "mNotificationsScrim"
                            )[chain.thisObject] -> "notifications_scrim"

                            else -> "unknown_scrim"
                        }

                        when (scrimName) {
                            "behind_scrim" -> {
                                if (!onlyNotifTransparencyActive) {
                                    args[alphaIndex] = args[alphaIndex] as Float * qsAlpha
                                }
                            }

                            "notifications_scrim" -> {
                                args[alphaIndex] = args[alphaIndex] as Float * qsAlpha
                            }

                            else -> {}
                        }
                    }
                }
            }

        // Compose implementation of QS Footer actions
        val footerActionsViewBinderClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.footer.ui.binder.FooterActionsViewBinder",
            suppressError = true
        )

        footerActionsViewBinderClass
            .hookMethod("bind")
            .suppressError()
            .runAfter { chain, _ ->
                if (!qsTransparencyActive && !onlyNotifTransparencyActive) return@runAfter

                val view = chain.args[0] as LinearLayout
                view.setBackgroundColor(Color.TRANSPARENT)
                view.elevation = 0f
            }

        val footerActionsViewModelClass =
            findClass("$SYSTEMUI_PACKAGE.qs.footer.ui.viewmodel.FooterActionsViewModel")

        footerActionsViewModelClass
            .hookConstructor()
            .runAfter { chain, _ ->
                if (!qsTransparencyActive && !onlyNotifTransparencyActive) return@runAfter

                val stateFlowImplClass = findClass("kotlinx.coroutines.flow.StateFlowImpl")!!
                val readonlyStateFlowClass =
                    findClass("kotlinx.coroutines.flow.ReadonlyStateFlow")!!

                try {
                    val zeroAlphaFlow = stateFlowImplClass
                        .getConstructor(Any::class.java)
                        .newInstance(0f)

                    val readonlyStateFlowInstance = try {
                        readonlyStateFlowClass.constructors[0].newInstance(zeroAlphaFlow)
                    } catch (_: Throwable) {
                        readonlyStateFlowClass.constructors[0].newInstance(zeroAlphaFlow, null)
                    }

                    chain.thisObject.setField(
                        "backgroundAlpha",
                        readonlyStateFlowInstance
                    )
                } catch (throwable: Throwable) {
                    log(this@QSTransparency, throwable)
                }
            }

        val quickSettingsControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.shade.QuickSettingsControllerImpl",
            "$SYSTEMUI_PACKAGE.shade.QuickSettingsController"
        )

        quickSettingsControllerClass
            .hookConstructor()
            .runAfter { chain, _ -> quickSettingsController = chain.thisObject }

        findClass(
            "$SYSTEMUI_PACKAGE.statusbar.notification.row.ActivatableNotificationView",
            suppressError = true
        )
            .hookMethod("usesTransparentBackground")
            .suppressError()
            .intercept { chain ->
                var result = chain.proceed()
                if (!qsTransparencyActive && !onlyNotifTransparencyActive) return@intercept result
                if (result == true) return@intercept result
                if (chain.thisObject.getFieldSilently("mOnKeyguard") != false) return@intercept result

                result = true

                result
            }

        hookComposeNotificationScrim()
        hookComposeLockscreenScrim()
    }

    private fun hookComposeLockscreenScrim() {
        findClass(
            "$SYSTEMUI_PACKAGE.keyguard.ui.composable.LockscreenBehindScrimKt",
            suppressError = true
        )
            .hookMethod("LockscreenBehindScrim")
            .suppressError()
            .intercept { chain ->
                if (!qsTransparencyActive && !onlyNotifTransparencyActive) return@intercept chain.proceed()
                if (keepLockScreenShade) return@intercept chain.proceed()
                if (DozeCallback.getInstance().isDozing()) return@intercept chain.proceed()

                ComposeViewHost.proceedHidingModifier(chain)
            }
    }

    private fun hookComposeNotificationScrim() {
        findClass(
            "com.android.compose.modifiers.AnimatedBackgroundKt",
            suppressError = true
        )
            .hookMethod("animatedBackground")
            .suppressError()
            .intercept { chain ->
                chain.proceedWithArgs { args ->
                    if (args.size < 3) return@proceedWithArgs

                    val alpha = args[2] ?: return@proceedWithArgs
                    if (alpha in notificationScrimAlphas.values) return@proceedWithArgs
                    if (!isCalledFromNotificationPanel()) return@proceedWithArgs

                    args[2] = notificationScrimAlphas.getOrPut(alpha) {
                        ComposeToolkit.function0("IconifyNotificationScrimAlpha") {
                            val value = ComposeToolkit.invokeFunction(alpha) as? Float ?: 1f
                            if (qsTransparencyActive || onlyNotifTransparencyActive) {
                                value * qsAlpha
                            } else {
                                value
                            }
                        } ?: return@proceedWithArgs
                    }
                }
            }
    }

    private fun isCalledFromShadeScene(): Boolean =
        Thread.currentThread().stackTrace.any { it.className.startsWith(SHADE_SCENE_CLASS) }

    private fun isCalledFromNotificationPanel(): Boolean {
        val caller = Thread.currentThread().stackTrace.firstOrNull { frame ->
            (frame.className.startsWith("com.android.systemui.") ||
                    frame.className.startsWith("com.android.compose.")) &&
                    !frame.className.startsWith(ANIMATED_BACKGROUND_CLASS)
        } ?: return false

        return caller.className.startsWith(NOTIFICATIONS_COMPOSABLE_CLASS)
    }

    private fun updateQsScrimRadius() {
        if (quickSettingsController == null) return

        quickSettingsController.setField(
            "mScrimCornerRadius",
            mContext.resources.getDimensionPixelSize(
                mContext.resources.getIdentifier(
                    "notification_scrim_corner_radius",
                    "dimen",
                    mContext.packageName
                )
            )
        )

        quickSettingsController.callMethod("setClippingBounds")
    }

    private fun setBlurRadius() {
        ResourceHookManager
            .hookDimen()
            .whenCondition { blurEnabled }
            .forPackageName(SYSTEMUI_PACKAGE)
            .addResource("max_window_blur_radius") { blurRadius }
            .apply()
    }

    companion object {
        private const val ANIMATED_BACKGROUND_CLASS =
            "com.android.compose.modifiers.AnimatedBackgroundKt"
        private const val SHADE_SCENE_CLASS =
            "$SYSTEMUI_PACKAGE.shade.ui.composable.ShadeSceneKt"
        private const val NOTIFICATIONS_COMPOSABLE_CLASS =
            "$SYSTEMUI_PACKAGE.notifications.ui.composable.NotificationsKt"
    }
}
