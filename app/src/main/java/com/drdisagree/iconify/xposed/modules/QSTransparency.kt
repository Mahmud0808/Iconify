package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.widget.LinearLayout
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.BLUR_RADIUS_VALUE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_SHADE_SWITCH
import com.drdisagree.iconify.common.Preferences.NOTIF_TRANSPARENCY_SWITCH
import com.drdisagree.iconify.common.Preferences.QSALPHA_LEVEL
import com.drdisagree.iconify.common.Preferences.QSPANEL_BLUR_SWITCH
import com.drdisagree.iconify.common.Preferences.QS_TRANSPARENCY_SWITCH
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.findClassIfExists
import de.robv.android.xposed.XposedHelpers.findField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class QSTransparency(context: Context?) : ModPack(context!!) {

    private val keyguardAlpha = 0.85f
    private var qsTransparencyActive = false
    private var onlyNotifTransparencyActive = false
    private var keepLockScreenShade = false
    private var alpha = 60f
    private var blurEnabled = false
    private var blurRadius = 23

    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized) return

        Xprefs.apply {
            qsTransparencyActive = getBoolean(QS_TRANSPARENCY_SWITCH, false)
            onlyNotifTransparencyActive = getBoolean(NOTIF_TRANSPARENCY_SWITCH, false)
            keepLockScreenShade = getBoolean(LOCKSCREEN_SHADE_SWITCH, false)
            alpha = (getSliderInt(QSALPHA_LEVEL, 60).toFloat() / 100.0).toFloat()
            blurEnabled = getBoolean(QSPANEL_BLUR_SWITCH, false)
            blurRadius = getSliderInt(BLUR_RADIUS_VALUE, 23)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        setQsTransparency(loadPackageParam)
        setBlurRadius()
    }

    private fun setQsTransparency(loadPackageParam: LoadPackageParam) {
        val scrimControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.ScrimController",
            loadPackageParam.classLoader
        )

        hookAllMethods(scrimControllerClass, "updateScrimColor", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!qsTransparencyActive && !onlyNotifTransparencyActive) return

                val alphaIndex = if (param.args[2] is Float) 2 else 1
                val scrimState = getObjectField(param.thisObject, "mState").toString()

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
        })

        // Compose implementation of QS Footer actions
        val footerActionsViewBinderClass = findClassIfExists(
            "$SYSTEMUI_PACKAGE.qs.footer.ui.binder.FooterActionsViewBinder",
            loadPackageParam.classLoader
        )

        if (footerActionsViewBinderClass != null) {
            hookAllMethods(footerActionsViewBinderClass, "bind", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!qsTransparencyActive && !onlyNotifTransparencyActive) return

                    val view = param.args[0] as LinearLayout
                    view.setBackgroundColor(Color.TRANSPARENT)
                    view.elevation = 0f
                }
            })
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun setBlurRadius() {
        hookAllMethods(Resources::class.java, "getDimensionPixelSize", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!blurEnabled) return

                try {
                    val resId = mContext.resources.getIdentifier(
                        "max_window_blur_radius",
                        "dimen",
                        mContext.packageName
                    )

                    if (param.args[0] == resId) {
                        param.result = blurRadius
                    }
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })
    }

    companion object {
        private val TAG = "Iconify - ${QSTransparency::class.java.simpleName}: "
    }
}
