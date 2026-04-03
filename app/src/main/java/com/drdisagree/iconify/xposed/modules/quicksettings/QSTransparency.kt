package com.drdisagree.iconify.xposed.modules.quicksettings

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.XposedHelpers.findField
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class QSTransparency(context: Context) : ModPack(context) {

    private val keyguardAlpha = 0.85f
    private var qsTransparencyActive = false
    private var onlyNotifTransparencyActive = false
    private var keepLockScreenShade = false
    private var alpha = 60f
    private var blurEnabled = false
    private var blurRadius = 23
    private var quickSettingsController: Any? = null

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            qsTransparencyActive = getBoolean(XposedKey.QUICK_SETTINGS_TRANSPARENCY)
            onlyNotifTransparencyActive = getBoolean(XposedKey.NOTIFICATION_TRANSPARENCY)
            keepLockScreenShade = getBoolean(XposedKey.LOCKSCREEN_SHADE)
            alpha = getFloat(XposedKey.QUICK_SETTINGS_ALPHA_LEVEL) / 100f
            blurEnabled = getBoolean(XposedKey.QUICK_SETTINGS_BLUR)
            blurRadius = getInt(XposedKey.QUICK_SETTINGS_BLUR_RADIUS)
        }

        when (key.firstOrNull()) {
            XposedKey.QUICK_SETTINGS_TRANSPARENCY.name,
            XposedKey.NOTIFICATION_TRANSPARENCY.name,
            XposedKey.QUICK_SETTINGS_ALPHA_LEVEL.name -> updateQsScrimRadius()
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        setQsTransparency()
        setBlurRadius()
    }

    private fun setQsTransparency() {
        val scrimControllerClass = findClass("$SYSTEMUI_PACKAGE.statusbar.phone.ScrimController")

        scrimControllerClass
            .hookMethod("updateScrimColor")
            .runBefore { param ->
                if (!qsTransparencyActive && !onlyNotifTransparencyActive) return@runBefore

                val alphaIndex = if (param.args[2] is Float) 2 else 1
                val scrimState = param.thisObject.getField("mState").toString()

                if (scrimState == "KEYGUARD") {
                    if (!keepLockScreenShade) {
                        param.args[alphaIndex] = 0.0f
                    }
                } else if (scrimState.contains("BOUNCER")) {
                    param.args[alphaIndex] = param.args[alphaIndex] as Float * keyguardAlpha
                } else {
                    val scrimName = when {
                        findField(
                            scrimControllerClass,
                            "mScrimInFront"
                        )[param.thisObject] == param.args[0] -> {
                            "front_scrim"
                        }

                        findField(
                            scrimControllerClass,
                            "mScrimBehind"
                        )[param.thisObject] == param.args[0] -> {
                            "behind_scrim"
                        }

                        findField(
                            scrimControllerClass,
                            "mNotificationsScrim"
                        )[param.thisObject] == param.args[0] -> {
                            "notifications_scrim"
                        }

                        else -> {
                            "unknown_scrim"
                        }
                    }

                    when (scrimName) {
                        "behind_scrim" -> {
                            if (!onlyNotifTransparencyActive) {
                                param.args[alphaIndex] = param.args[alphaIndex] as Float * alpha
                            }
                        }

                        "notifications_scrim" -> {
                            param.args[alphaIndex] = param.args[alphaIndex] as Float * alpha
                        }

                        else -> {}
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
            .runAfter { param ->
                if (!qsTransparencyActive && !onlyNotifTransparencyActive) return@runAfter

                val view = param.args[0] as LinearLayout
                view.setBackgroundColor(Color.TRANSPARENT)
                view.elevation = 0f
            }

        val footerActionsViewModelClass =
            findClass("$SYSTEMUI_PACKAGE.qs.footer.ui.viewmodel.FooterActionsViewModel")

        footerActionsViewModelClass
            .hookConstructor()
            .runAfter { param ->
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

                    param.thisObject.setField(
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
            .runAfter { param -> quickSettingsController = param.thisObject }

        ResourceHookManager
            .hookDimen()
            .whenCondition { qsTransparencyActive && !onlyNotifTransparencyActive && alpha == 0f }
            .forPackageName(SYSTEMUI_PACKAGE)
            .addResource("notification_scrim_corner_radius") { 0 }
            .apply()
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
}
