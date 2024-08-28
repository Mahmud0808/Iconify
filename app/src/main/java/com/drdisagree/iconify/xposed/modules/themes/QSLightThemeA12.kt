package com.drdisagree.iconify.xposed.modules.themes

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.DUALTONE_QSPANEL
import com.drdisagree.iconify.common.Preferences.LIGHT_QSPANEL
import com.drdisagree.iconify.xposed.HookEntry.Companion.disableOverlay
import com.drdisagree.iconify.xposed.HookEntry.Companion.enableOverlay
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.utils.SystemUtils
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.hookMethod
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.findMethodExact
import de.robv.android.xposed.XposedHelpers.findMethodExactIfExists
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.setObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class QSLightThemeA12(context: Context?) : ModPack(context!!) {

    private var mBehindColors: Any? = null
    private var isDark: Boolean
    private val qsLightThemeOverlay = "IconifyComponentQSLT.overlay"
    private val qsDualToneOverlay = "IconifyComponentQSDT.overlay"

    init {
        isDark = SystemUtils.isDarkMode
    }

    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized) return

        Xprefs.apply {
            lightQSHeaderEnabled = getBoolean(LIGHT_QSPANEL, false)
            dualToneQSEnabled = lightQSHeaderEnabled && getBoolean(DUALTONE_QSPANEL, false)
        }

        applyOverlays(true)
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val utilsClass = findClass("com.android.settingslib.Utils", loadPackageParam.classLoader)
        val ongoingPrivacyChipClass = findClass(
            "$SYSTEMUI_PACKAGE.privacy.OngoingPrivacyChip",
            loadPackageParam.classLoader
        )
        val fragmentHostManagerClass = findClass(
            "$SYSTEMUI_PACKAGE.fragments.FragmentHostManager",
            loadPackageParam.classLoader
        )
        val scrimControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.ScrimController",
            loadPackageParam.classLoader
        )
        val gradientColorsClass = findClass(
            "com.android.internal.colorextraction.ColorExtractor.GradientColors",
            loadPackageParam.classLoader
        )
        val statusbarClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.StatusBar",
            loadPackageParam.classLoader
        )
        val interestingConfigChangesClass = findClass(
            "com.android.settingslib.applications.InterestingConfigChanges",
            loadPackageParam.classLoader
        )
        var applyStateMethod =
            findMethodExactIfExists(scrimControllerClass, "applyStateToAlpha")
        if (applyStateMethod == null) {
            applyStateMethod = findMethodExact(scrimControllerClass, "applyState", null)
        }

        try {
            mBehindColors = gradientColorsClass.getDeclaredConstructor().newInstance()
        } catch (throwable: Throwable) {
            log(TAG + throwable)
        }

        hookAllMethods(scrimControllerClass, "onUiModeChanged", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    mBehindColors = gradientColorsClass.getDeclaredConstructor().newInstance()
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        hookAllMethods(scrimControllerClass, "updateScrims", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!dualToneQSEnabled) return

                try {
                    val mScrimBehind =
                        getObjectField(param.thisObject, "mScrimBehind")
                    val mBlankScreen =
                        getObjectField(param.thisObject, "mBlankScreen") as Boolean
                    val alpha = callMethod(mScrimBehind, "getViewAlpha") as Float
                    val animateBehindScrim = alpha != 0f && !mBlankScreen

                    callMethod(
                        mScrimBehind,
                        "setColors",
                        mBehindColors,
                        animateBehindScrim
                    )
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        hookAllMethods(scrimControllerClass, "updateThemeColors", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!dualToneQSEnabled) return

                try {
                    @SuppressLint("DiscouragedApi") val states = callStaticMethod(
                        utilsClass,
                        "getColorAttr",
                        mContext,
                        mContext.resources.getIdentifier(
                            "android:attr/colorSurfaceHeader",
                            "attr",
                            mContext.packageName
                        )
                    ) as ColorStateList

                    val surfaceBackground = states.defaultColor
                    val accentStates = callStaticMethod(
                        utilsClass,
                        "getColorAccent",
                        mContext
                    ) as ColorStateList
                    val accent = accentStates.defaultColor

                    callMethod(mBehindColors, "setMainColor", surfaceBackground)
                    callMethod(mBehindColors, "setSecondaryColor", accent)

                    val contrast = ColorUtils.calculateContrast(
                        callMethod(
                            mBehindColors,
                            "getMainColor"
                        ) as Int, Color.WHITE
                    )

                    callMethod(mBehindColors, "setSupportsDarkText", contrast > 4.5)
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        findAndHookMethod(ongoingPrivacyChipClass, "updateResources", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!lightQSHeaderEnabled) return

                try {
                    val iconColor = mContext.resources.getColor(
                        mContext.resources.getIdentifier(
                            "android:color/system_neutral1_900",
                            "color",
                            mContext.packageName
                        ), mContext.theme
                    )

                    setObjectField(param.thisObject, "iconColor", iconColor)
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        hookMethod(applyStateMethod, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!lightQSHeaderEnabled) return

                try {
                    val mClipsQsScrim =
                        getObjectField(param.thisObject, "mClipsQsScrim") as Boolean

                    if (mClipsQsScrim) {
                        setObjectField(param.thisObject, "mBehindTint", Color.TRANSPARENT)
                    }
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        try {
            val scrimStateEnum = findClass(
                "$SYSTEMUI_PACKAGE.statusbar.phone.ScrimState",
                loadPackageParam.classLoader
            )
            val constants: Array<out Any>? = scrimStateEnum.getEnumConstants()

            if (constants != null) {
                for (constant in constants) {
                    when (constant.toString()) {
                        "KEYGUARD" -> findAndHookMethod(
                            constant.javaClass,
                            "prepare",
                            scrimStateEnum,
                            object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    if (!lightQSHeaderEnabled) return

                                    val mClipQsScrim = getObjectField(
                                        param.thisObject,
                                        "mClipQsScrim"
                                    ) as Boolean

                                    if (mClipQsScrim) {
                                        callMethod(
                                            param.thisObject,
                                            "updateScrimColor",
                                            getObjectField(param.thisObject, "mScrimBehind"),
                                            1f,
                                            Color.TRANSPARENT
                                        )
                                    }
                                }
                            })

                        "BOUNCER" -> findAndHookMethod(
                            constant.javaClass,
                            "prepare",
                            scrimStateEnum,
                            object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    if (!lightQSHeaderEnabled) return
                                    setObjectField(
                                        param.thisObject,
                                        "mBehindTint",
                                        Color.TRANSPARENT
                                    )
                                }
                            })

                        "SHADE_LOCKED" -> {
                            hookAllMethods(
                                constant.javaClass,
                                "prepare",
                                object : XC_MethodHook() {
                                    override fun afterHookedMethod(param: MethodHookParam) {
                                        if (!lightQSHeaderEnabled) return

                                        setObjectField(
                                            param.thisObject,
                                            "mBehindTint",
                                            Color.TRANSPARENT
                                        )

                                        val mClipQsScrim = getObjectField(
                                            param.thisObject,
                                            "mClipQsScrim"
                                        ) as Boolean

                                        if (mClipQsScrim) {
                                            callMethod(
                                                param.thisObject,
                                                "updateScrimColor",
                                                getObjectField(param.thisObject, "mScrimBehind"),
                                                1f,
                                                Color.TRANSPARENT
                                            )
                                        }
                                    }
                                })

                            hookAllMethods(
                                constant.javaClass,
                                "getBehindTint",
                                object : XC_MethodHook() {
                                    override fun beforeHookedMethod(param: MethodHookParam) {
                                        if (!lightQSHeaderEnabled) return

                                        param.result = Color.TRANSPARENT
                                    }
                                })
                        }

                        "UNLOCKED" -> findAndHookMethod(
                            constant.javaClass,
                            "prepare",
                            scrimStateEnum,
                            object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    if (!lightQSHeaderEnabled) return

                                    setObjectField(
                                        param.thisObject,
                                        "mBehindTint",
                                        Color.TRANSPARENT
                                    )

                                    callMethod(
                                        param.thisObject,
                                        "updateScrimColor",
                                        getObjectField(param.thisObject, "mScrimBehind"),
                                        1f,
                                        Color.TRANSPARENT
                                    )
                                }
                            })
                    }
                }
            }
        } catch (throwable: Throwable) {
            log(TAG + throwable)
        }

        hookAllConstructors(fragmentHostManagerClass, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!lightQSHeaderEnabled) return

                try {
                    setObjectField(
                        param.thisObject,
                        "mConfigChanges",
                        interestingConfigChangesClass.getDeclaredConstructor(
                            Int::class.javaPrimitiveType
                        ).newInstance(0x40000000 or 0x0004 or 0x0100 or -0x80000000 or 0x0200)
                    )
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        hookAllConstructors(statusbarClass, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    hookAllMethods(
                        getObjectField(param.thisObject, "mOnColorsChangedListener").javaClass,
                        "onColorsChanged",
                        object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                applyOverlays(true)
                            }
                        })
                } catch (ignored: Throwable) {
                }
            }
        })
    }

    private fun applyOverlays(force: Boolean) {
        val isCurrentlyDark: Boolean = SystemUtils.isDarkMode
        if (isCurrentlyDark == isDark && !force) return

        isDark = isCurrentlyDark

        if (!lightQSHeaderEnabled) {
            if (isCurrentlyDark) disableOverlay(qsLightThemeOverlay)
            if (!dualToneQSEnabled) disableOverlay(qsDualToneOverlay)
        }

        try {
            Thread.sleep(50)
        } catch (ignored: Throwable) {
        }

        if (lightQSHeaderEnabled) {
            if (!isCurrentlyDark) enableOverlay(qsLightThemeOverlay)
            if (dualToneQSEnabled) enableOverlay(qsDualToneOverlay)
        }
    }

    companion object {
        private val TAG = "Iconify - ${QSLightThemeA12::class.java.simpleName}: "
        private var lightQSHeaderEnabled = false
        private var dualToneQSEnabled = false
    }
}