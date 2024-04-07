package com.drdisagree.iconify.xposed.modules.themes

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.service.quicksettings.Tile
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.BLACK_QSPANEL
import com.drdisagree.iconify.common.Preferences.QS_TEXT_ALWAYS_WHITE
import com.drdisagree.iconify.common.Preferences.QS_TEXT_FOLLOW_ACCENT
import com.drdisagree.iconify.config.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.SettingsLibUtils.Companion.getColorAttr
import com.drdisagree.iconify.xposed.utils.SystemUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.findClassIfExists
import de.robv.android.xposed.XposedHelpers.getFloatField
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.setObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.util.function.Consumer

@SuppressLint("DiscouragedApi")
class QSBlackThemeA14(context: Context?) : ModPack(context!!) {

    private var mBehindColors: Any? = null
    private var isDark: Boolean
    private var colorText: Int? = null
    private var colorTextAlpha: Int? = null
    private var mClockViewQSHeader: Any? = null
    private var qsTextAlwaysWhite = false
    private var qsTextFollowAccent = false
    private var shadeCarrierGroupController: Any? = null
    private val modernShadeCarrierGroupMobileViews = ArrayList<Any>()

    init {
        isDark = SystemUtil.isDarkMode
    }

    override fun updatePrefs(vararg key: String) {
        if (Xprefs == null) return

        blackQSHeaderEnabled = Xprefs!!.getBoolean(BLACK_QSPANEL, false)
        qsTextAlwaysWhite = Xprefs!!.getBoolean(QS_TEXT_ALWAYS_WHITE, false)
        qsTextFollowAccent = Xprefs!!.getBoolean(QS_TEXT_FOLLOW_ACCENT, false)

        initColors(true)
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val qsTileViewImplClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.tileimpl.QSTileViewImpl",
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
            "com.android.internal.colorextraction.ColorExtractor\$GradientColors",
            loadPackageParam.classLoader
        )
        val qsPanelControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.QSPanelController",
            loadPackageParam.classLoader
        )
        val scrimStateEnum = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.ScrimState",
            loadPackageParam.classLoader
        )
        val qsIconViewImplClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.tileimpl.QSIconViewImpl",
            loadPackageParam.classLoader
        )
        val centralSurfacesImplClass = findClassIfExists(
            "$SYSTEMUI_PACKAGE.statusbar.phone.CentralSurfacesImpl",
            loadPackageParam.classLoader
        )
        val qsCustomizerClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.customize.QSCustomizer",
            loadPackageParam.classLoader
        )
        val qsContainerImplClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.QSContainerImpl",
            loadPackageParam.classLoader
        )
        val shadeCarrierClass = findClass(
            "$SYSTEMUI_PACKAGE.shade.carrier.ShadeCarrier",
            loadPackageParam.classLoader
        )
        val interestingConfigChangesClass = findClass(
            "com.android.settingslib.applications.InterestingConfigChanges",
            loadPackageParam.classLoader
        )
        val batteryStatusChipClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.BatteryStatusChip",
            loadPackageParam.classLoader
        )
        val textButtonViewHolderClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.footer.ui.binder.TextButtonViewHolder",
            loadPackageParam.classLoader
        )
        val numberButtonViewHolderClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.footer.ui.binder.NumberButtonViewHolder",
            loadPackageParam.classLoader
        )
        val qsFooterViewClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.QSFooterView",
            loadPackageParam.classLoader
        )
        val brightnessControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.settings.brightness.BrightnessController",
            loadPackageParam.classLoader
        )
        val brightnessMirrorControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.policy.BrightnessMirrorController",
            loadPackageParam.classLoader
        )
        val brightnessSliderControllerClass = findClassIfExists(
            "$SYSTEMUI_PACKAGE.settings.brightness.BrightnessSliderController",
            loadPackageParam.classLoader
        )
        val quickStatusBarHeaderClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader",
            loadPackageParam.classLoader
        )
        val clockClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.policy.Clock",
            loadPackageParam.classLoader
        )
        val themeColorKtClass = findClassIfExists(
            "com.android.compose.theme.ColorKt",
            loadPackageParam.classLoader
        )
        val expandableControllerImplClass = findClassIfExists(
            "com.android.compose.animation.ExpandableControllerImpl",
            loadPackageParam.classLoader
        )
        val footerActionsViewModelClass = findClassIfExists(
            "$SYSTEMUI_PACKAGE.qs.footer.ui.viewmodel.FooterActionsViewModel",
            loadPackageParam.classLoader
        )
        val footerActionsViewBinderClass = findClassIfExists(
            "$SYSTEMUI_PACKAGE.qs.footer.ui.binder.FooterActionsViewBinder",
            loadPackageParam.classLoader
        )
        var shadeHeaderControllerClass = findClassIfExists(
            "$SYSTEMUI_PACKAGE.shade.ShadeHeaderController",
            loadPackageParam.classLoader
        )
        if (shadeHeaderControllerClass == null) {
            shadeHeaderControllerClass = findClass(
                "$SYSTEMUI_PACKAGE.shade.LargeScreenShadeHeaderController",
                loadPackageParam.classLoader
            )
        }

        // Background color of android 14's charging chip. Fix for light QS theme situation
        val batteryStatusChipColorHook: XC_MethodHook = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (blackQSHeaderEnabled) {
                    (getObjectField(param.thisObject, "roundedContainer") as LinearLayout)
                        .background.setTint(Color.DKGRAY)

                    callMethod(
                        getObjectField(param.thisObject, "batteryMeterView"),
                        "updateColors",
                        Color.WHITE,
                        Color.GRAY,
                        Color.WHITE
                    )
                }
            }
        }

        hookAllConstructors(batteryStatusChipClass, batteryStatusChipColorHook)
        hookAllMethods(batteryStatusChipClass, "onConfigurationChanged", batteryStatusChipColorHook)

        hookAllConstructors(qsPanelControllerClass, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                calculateColors()
            }
        })

        hookAllMethods(shadeHeaderControllerClass, "onInit", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val mView = getObjectField(param.thisObject, "mView") as View
                    val iconManager = getObjectField(param.thisObject, "iconManager")
                    val batteryIcon = getObjectField(param.thisObject, "batteryIcon")
                    val configurationControllerListener = getObjectField(
                        param.thisObject,
                        "configurationControllerListener"
                    )

                    hookAllMethods(
                        configurationControllerListener.javaClass,
                        "onConfigChanged",
                        object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                setHeaderComponentsColor(mView, iconManager, batteryIcon)
                            }
                        })

                    setHeaderComponentsColor(mView, iconManager, batteryIcon)
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        try { // A14 ap11 onwards - modern implementation of mobile icons
            val shadeCarrierGroupControllerClass = findClass(
                "$SYSTEMUI_PACKAGE.shade.carrier.ShadeCarrierGroupController",
                loadPackageParam.classLoader
            )
            val mobileIconBinderClass = findClass(
                "$SYSTEMUI_PACKAGE.statusbar.pipeline.mobile.ui.binder.MobileIconBinder",
                loadPackageParam.classLoader
            )

            hookAllConstructors(shadeCarrierGroupControllerClass, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    shadeCarrierGroupController = param.thisObject
                }
            })

            hookAllMethods(mobileIconBinderClass, "bind", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (param.args[1].javaClass.getName()
                            .contains("ShadeCarrierGroupMobileIconViewModel")
                    ) {
                        modernShadeCarrierGroupMobileViews.add(param.result)

                        if (blackQSHeaderEnabled) {
                            setMobileIconTint(param.result, Color.WHITE)
                        }
                    }
                }
            })
        } catch (ignored: Throwable) {
        }

        hookAllMethods(qsContainerImplClass, "updateResources", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!blackQSHeaderEnabled) return

                try {
                    val res = mContext.resources
                    val view = param.thisObject as ViewGroup

                    val settingsButtonContainer = view.findViewById<View>(
                        res.getIdentifier(
                            "settings_button_container",
                            "id",
                            mContext.packageName
                        )
                    )

                    val settingsIcon = settingsButtonContainer.findViewById<ImageView>(
                        res.getIdentifier(
                            "icon",
                            "id",
                            mContext.packageName
                        )
                    )

                    settingsIcon.setImageTintList(ColorStateList.valueOf(Color.WHITE))
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        // QS Customize panel
        hookAllConstructors(qsCustomizerClass, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (blackQSHeaderEnabled) {
                    val mainView = param.thisObject as ViewGroup

                    for (i in 0 until mainView.childCount) {
                        mainView.getChildAt(i).setBackgroundColor(Color.BLACK)
                    }
                }
            }
        })

        // Mobile signal icons - this is the legacy model. new model uses viewmodels
        hookAllMethods(shadeCarrierClass, "updateState", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!blackQSHeaderEnabled) return

                (getObjectField(param.thisObject, "mMobileSignal") as ImageView)
                    .setImageTintList(ColorStateList.valueOf(Color.WHITE))
            }
        })

        // QS security footer count circle
        hookAllConstructors(numberButtonViewHolderClass, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (blackQSHeaderEnabled) {
                    (getObjectField(param.thisObject, "newDot") as ImageView)
                        .setColorFilter(Color.WHITE)

                    (getObjectField(param.thisObject, "number") as TextView)
                        .setTextColor(Color.WHITE)
                }
            }
        })

        // QS security footer
        hookAllConstructors(textButtonViewHolderClass, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (blackQSHeaderEnabled) {
                    (getObjectField(param.thisObject, "chevron") as ImageView)
                        .setColorFilter(Color.WHITE)

                    (getObjectField(param.thisObject, "icon") as ImageView)
                        .setColorFilter(Color.WHITE)

                    (getObjectField(param.thisObject, "newDot") as ImageView)
                        .setColorFilter(Color.WHITE)

                    (getObjectField(param.thisObject, "text") as TextView)
                        .setTextColor(Color.WHITE)
                }
            }
        })
        try {
            //QS Footer built text row
            hookAllMethods(qsFooterViewClass, "onFinishInflate", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (blackQSHeaderEnabled) {
                        (getObjectField(param.thisObject, "mBuildText") as TextView)
                            .setTextColor(Color.WHITE)

                        (getObjectField(param.thisObject, "mEditButton") as ImageView)
                            .setColorFilter(Color.WHITE)

                        setObjectField(
                            getObjectField(param.thisObject, "mPageIndicator"),
                            "mTint",
                            ColorStateList.valueOf(Color.WHITE)
                        )
                    }
                }
            })
        } catch (ignored: Throwable) {
        }

        // QS tile primary label color
        hookAllMethods(qsTileViewImplClass, "getLabelColorForState", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!blackQSHeaderEnabled) return

                try {
                    if (param.args[0] as Int == Tile.STATE_ACTIVE) {
                        param.result = colorText
                    }
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        // QS tile secondary label color
        hookAllMethods(
            qsTileViewImplClass,
            "getSecondaryLabelColorForState",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!blackQSHeaderEnabled) return

                    try {
                        if (param.args[0] as Int == Tile.STATE_ACTIVE) {
                            param.result = colorText
                        }
                    } catch (throwable: Throwable) {
                        log(TAG + throwable)
                    }
                }
            })

        // Auto Brightness Icon Color
        hookAllMethods(brightnessControllerClass, "updateIcon", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!blackQSHeaderEnabled) return

                try {
                    (getObjectField(param.thisObject, "mIcon") as ImageView)
                        .setImageTintList(ColorStateList.valueOf(colorText!!))
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        if (brightnessSliderControllerClass != null) {
            hookAllConstructors(brightnessSliderControllerClass, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!blackQSHeaderEnabled) return

                    try {
                        (getObjectField(param.thisObject, "mIcon") as ImageView)
                            .setImageTintList(ColorStateList.valueOf(colorText!!))
                    } catch (throwable: Throwable) {
                        try {
                            (getObjectField(param.thisObject, "mIconView") as ImageView)
                                .setImageTintList(ColorStateList.valueOf(colorText!!))
                        } catch (ignored: Throwable) {
                        }
                    }
                }
            })
        } else {
            log(TAG + "Not a crash... BrightnessSliderController class not found.")
        }

        hookAllMethods(brightnessMirrorControllerClass, "updateIcon", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!blackQSHeaderEnabled) return

                try {
                    (getObjectField(param.thisObject, "mIcon") as ImageView)
                        .setImageTintList(ColorStateList.valueOf(colorText!!))
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        hookAllMethods(qsIconViewImplClass, "updateIcon", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (blackQSHeaderEnabled) {
                    try {
                        if (getIntField(param.args[1], "state") == Tile.STATE_ACTIVE
                        ) {
                            (param.args[0] as ImageView)
                                .setImageTintList(ColorStateList.valueOf(colorText!!))
                        }
                    } catch (throwable: Throwable) {
                        log(TAG + throwable)
                    }
                }
            }
        })

        hookAllMethods(qsIconViewImplClass, "setIcon", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (blackQSHeaderEnabled) {
                    try {
                        if (param.args[0] is ImageView &&
                            getIntField(param.args[1], "state") == Tile.STATE_ACTIVE
                        ) {
                            setObjectField(param.thisObject, "mTint", colorText)
                        }
                    } catch (throwable: Throwable) {
                        log(TAG + throwable)
                    }
                }
            }
        })

        try {
            // White QS Clock bug
            hookAllMethods(quickStatusBarHeaderClass, "onFinishInflate", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    try {
                        mClockViewQSHeader = getObjectField(param.thisObject, "mClockView")
                    } catch (ignored: Throwable) {
                    }

                    if (blackQSHeaderEnabled && mClockViewQSHeader != null) {
                        try {
                            (mClockViewQSHeader as TextView).setTextColor(Color.WHITE)
                        } catch (throwable: Throwable) {
                            log(TAG + throwable)
                        }
                    }
                }
            })

            // White QS Clock bug
            hookAllMethods(clockClass, "onColorsChanged", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (blackQSHeaderEnabled && mClockViewQSHeader != null) {
                        try {
                            (mClockViewQSHeader as TextView).setTextColor(Color.WHITE)
                        } catch (throwable: Throwable) {
                            log(TAG + throwable)
                        }
                    }
                }
            })
        } catch (ignored: Throwable) {
        }

        if (centralSurfacesImplClass != null) {
            hookAllConstructors(centralSurfacesImplClass, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    initColors(true)
                }
            })

            hookAllMethods(centralSurfacesImplClass, "updateTheme", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    initColors(false)
                }
            })
        }

        hookAllConstructors(qsTileViewImplClass, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!blackQSHeaderEnabled) return
                try {
                    if (!qsTextAlwaysWhite && !qsTextFollowAccent) {
                        setObjectField(param.thisObject, "colorLabelActive", colorText)
                        setObjectField(
                            param.thisObject,
                            "colorSecondaryLabelActive",
                            colorTextAlpha
                        )
                    }

                    setObjectField(param.thisObject, "colorLabelInactive", Color.WHITE)
                    setObjectField(param.thisObject, "colorSecondaryLabelInactive", -0x7f000001)

                    val sideView = getObjectField(param.thisObject, "sideView") as ViewGroup

                    val customDrawable = sideView.findViewById<ImageView>(
                        mContext.resources.getIdentifier(
                            "customDrawable",
                            "id",
                            mContext.packageName
                        )
                    )
                    customDrawable.setImageTintList(ColorStateList.valueOf(colorText!!))

                    val chevron = sideView.findViewById<ImageView>(
                        mContext.resources.getIdentifier(
                            "chevron",
                            "id",
                            mContext.packageName
                        )
                    )
                    chevron.setImageTintList(ColorStateList.valueOf(colorText!!))
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        hookAllMethods(qsIconViewImplClass, "getIconColorForState", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val (isDisabledState: Boolean, isActiveState: Boolean) = getTileState(param)

                if (blackQSHeaderEnabled) {
                    if (isDisabledState) {
                        param.result = -0x7f000001
                    } else if (isActiveState && !qsTextAlwaysWhite && !qsTextFollowAccent) {
                        param.result = colorText
                    } else if (!isActiveState) {
                        param.result = Color.WHITE
                    }
                }
            }
        })

        try {
            hookAllMethods(qsIconViewImplClass, "updateIcon", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (qsTextAlwaysWhite || qsTextFollowAccent) return

                    val (isDisabledState: Boolean, isActiveState: Boolean) = getTileState(param)

                    if (blackQSHeaderEnabled) {
                        val mIcon = param.args[0] as ImageView

                        if (isDisabledState) {
                            mIcon.setImageTintList(ColorStateList.valueOf(-0x7f000001))
                        } else if (isActiveState && !qsTextAlwaysWhite && !qsTextFollowAccent) {
                            mIcon.setImageTintList(ColorStateList.valueOf(colorText!!))
                        } else if (!isActiveState) {
                            mIcon.setImageTintList(ColorStateList.valueOf(Color.WHITE))
                        }
                    }
                }
            })
        } catch (ignored: Throwable) {
        }

        try {
            hookAllMethods(qsContainerImplClass, "updateResources", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!blackQSHeaderEnabled) return

                    try {
                        val res = mContext.resources
                        val view = (param.thisObject as ViewGroup).findViewById<ViewGroup>(
                            res.getIdentifier(
                                "qs_footer_actions",
                                "id",
                                mContext.packageName
                            )
                        )

                        try {
                            val pmButtonContainer = view.findViewById<ViewGroup>(
                                res.getIdentifier(
                                    "pm_lite",
                                    "id",
                                    mContext.packageName
                                )
                            )

                            (pmButtonContainer.getChildAt(0) as ImageView).setColorFilter(
                                Color.BLACK,
                                PorterDuff.Mode.SRC_IN
                            )
                        } catch (ignored: Throwable) {
                            val pmButton = view.findViewById<ImageView>(
                                res.getIdentifier(
                                    "pm_lite",
                                    "id",
                                    mContext.packageName
                                )
                            )

                            pmButton.setImageTintList(ColorStateList.valueOf(Color.BLACK))
                        }
                    } catch (ignored: Throwable) {
                    }
                }
            })
        } catch (ignored: Throwable) {
        }

        try { // Compose implementation of QS Footer actions
            val graphicsColorKtClass = findClass(
                "androidx.compose.ui.graphics.ColorKt",
                loadPackageParam.classLoader
            )

            hookAllConstructors(expandableControllerImplClass, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!blackQSHeaderEnabled) return

                    param.args[1] = callStaticMethod(graphicsColorKtClass, "Color", Color.WHITE)
                }
            })

            hookAllMethods(themeColorKtClass, "colorAttr", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!blackQSHeaderEnabled) return

                    val code = param.args[0] as Int
                    var result = 0

                    if (code != PM_LITE_BACKGROUND_CODE) {
                        try {
                            if (mContext.resources.getResourceName(code).split("/".toRegex())
                                    .dropLastWhile { it.isEmpty() }
                                    .toTypedArray()[1] == "onShadeInactiveVariant"
                            ) {
                                result = Color.WHITE // number button text
                            }
                        } catch (ignored: Throwable) {
                        }
                    }

                    if (result != 0) {
                        param.result = callStaticMethod(graphicsColorKtClass, "Color", result)
                    }
                }
            })

            hookAllConstructors(footerActionsViewModelClass, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!blackQSHeaderEnabled) return

                    // Power button
                    val power = getObjectField(param.thisObject, "power")
                    setObjectField(power, "iconTint", Color.BLACK)

                    // Settings button
                    val settings = getObjectField(param.thisObject, "settings")
                    setObjectField(settings, "iconTint", Color.WHITE)

                    // We must use the classes defined in the apk. Using our own will fail.
                    val stateFlowImplClass = findClass(
                        "kotlinx.coroutines.flow.StateFlowImpl",
                        loadPackageParam.classLoader
                    )
                    val readonlyStateFlowClass = findClass(
                        "kotlinx.coroutines.flow.ReadonlyStateFlow",
                        loadPackageParam.classLoader
                    )

                    try {
                        val zeroAlphaFlow =
                            stateFlowImplClass.getConstructor(Any::class.java).newInstance(0f)
                        setObjectField(
                            param.thisObject,
                            "backgroundAlpha",
                            readonlyStateFlowClass.constructors[0].newInstance(zeroAlphaFlow)
                        )
                    } catch (throwable: Throwable) {
                        log(TAG + throwable)
                    }
                }
            })

            hookAllMethods(footerActionsViewBinderClass, "bind", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!blackQSHeaderEnabled) return

                    val view = param.args[0] as LinearLayout
                    view.setBackgroundColor(Color.BLACK)
                    view.elevation = 0f
                }
            })
        } catch (ignored: Throwable) {
        }

        try {
            mBehindColors = gradientColorsClass.getDeclaredConstructor().newInstance()
        } catch (throwable: Throwable) {
            log(TAG + throwable)
        }

        hookAllMethods(scrimControllerClass, "updateScrims", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!blackQSHeaderEnabled) return

                try {
                    val mScrimBehind =
                        getObjectField(param.thisObject, "mScrimBehind")
                    val mBlankScreen =
                        getObjectField(param.thisObject, "mBlankScreen") as Boolean
                    val alpha = getFloatField(mScrimBehind, "mViewAlpha")
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
                calculateColors()
            }
        })

        hookAllMethods(scrimControllerClass, "updateThemeColors", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!blackQSHeaderEnabled) return

                try {
                    val states: ColorStateList = getColorAttr(
                        mContext.resources.getIdentifier(
                            "android:attr/colorBackgroundFloating",
                            "attr",
                            mContext.packageName
                        ), mContext
                    )
                    val surfaceBackground = states.defaultColor
                    val accentStates: ColorStateList =
                        getColorAttr(
                            mContext.resources.getIdentifier(
                                "colorAccent",
                                "attr",
                                "android"
                            ), mContext
                        )
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

        hookAllMethods(scrimControllerClass, "applyState", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!blackQSHeaderEnabled) return

                try {
                    val mClipsQsScrim =
                        getObjectField(param.thisObject, "mClipsQsScrim") as Boolean
                    if (mClipsQsScrim) {
                        setObjectField(param.thisObject, "mBehindTint", Color.BLACK)
                    }
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        try {
            val constants: Array<out Any>? = scrimStateEnum.getEnumConstants()
            if (constants != null) {
                for (constant in constants) {
                    when (constant.toString()) {
                        "KEYGUARD" -> hookAllMethods(
                            constant.javaClass,
                            "prepare",
                            object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    if (!blackQSHeaderEnabled) return

                                    val mClipQsScrim = getObjectField(
                                        param.thisObject,
                                        "mClipQsScrim"
                                    ) as Boolean

                                    if (mClipQsScrim) {
                                        val mScrimBehind = getObjectField(
                                            param.thisObject,
                                            "mScrimBehind"
                                        )
                                        val mTintColor =
                                            getIntField(mScrimBehind, "mTintColor")

                                        if (mTintColor != Color.BLACK) {
                                            setObjectField(
                                                mScrimBehind,
                                                "mTintColor",
                                                Color.BLACK
                                            )

                                            callMethod(
                                                mScrimBehind,
                                                "updateColorWithTint",
                                                false
                                            )
                                        }

                                        callMethod(mScrimBehind, "setViewAlpha", 1f)
                                    }
                                }
                            })

                        "BOUNCER" -> hookAllMethods(
                            constant.javaClass,
                            "prepare",
                            object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    if (!blackQSHeaderEnabled) return

                                    setObjectField(param.thisObject, "mBehindTint", Color.BLACK)
                                }
                            })

                        "SHADE_LOCKED" -> {
                            hookAllMethods(
                                constant.javaClass,
                                "prepare",
                                object : XC_MethodHook() {
                                    override fun afterHookedMethod(param: MethodHookParam) {
                                        if (!blackQSHeaderEnabled) return

                                        setObjectField(
                                            param.thisObject,
                                            "mBehindTint",
                                            Color.BLACK
                                        )

                                        val mClipQsScrim = getObjectField(
                                            param.thisObject,
                                            "mClipQsScrim"
                                        ) as Boolean

                                        if (mClipQsScrim) {
                                            val mScrimBehind = getObjectField(
                                                param.thisObject,
                                                "mScrimBehind"
                                            )
                                            val mTintColor =
                                                getIntField(mScrimBehind, "mTintColor")

                                            if (mTintColor != Color.BLACK) {
                                                setObjectField(
                                                    mScrimBehind,
                                                    "mTintColor",
                                                    Color.BLACK
                                                )

                                                callMethod(
                                                    mScrimBehind,
                                                    "updateColorWithTint",
                                                    false
                                                )
                                            }

                                            callMethod(mScrimBehind, "setViewAlpha", 1f)
                                        }
                                    }
                                })

                            hookAllMethods(
                                constant.javaClass,
                                "getBehindTint",
                                object : XC_MethodHook() {
                                    override fun beforeHookedMethod(param: MethodHookParam) {
                                        if (!blackQSHeaderEnabled) return

                                        param.result = Color.BLACK
                                    }
                                })
                        }

                        "UNLOCKED" -> hookAllMethods(
                            constant.javaClass,
                            "prepare",
                            object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    if (!blackQSHeaderEnabled) return

                                    setObjectField(
                                        param.thisObject,
                                        "mBehindTint",
                                        Color.BLACK
                                    )

                                    val mScrimBehind =
                                        getObjectField(param.thisObject, "mScrimBehind")
                                    val mTintColor =
                                        getIntField(mScrimBehind, "mTintColor")

                                    if (mTintColor != Color.BLACK) {
                                        setObjectField(
                                            mScrimBehind,
                                            "mTintColor",
                                            Color.BLACK
                                        )

                                        callMethod(
                                            mScrimBehind,
                                            "updateColorWithTint",
                                            false
                                        )
                                    }

                                    callMethod(mScrimBehind, "setViewAlpha", 1f)
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
    }

    private fun getTileState(param: XC_MethodHook.MethodHookParam): Pair<Boolean, Boolean> {
        val isDisabledState: Boolean = try {
            getObjectField(
                param.args[1],
                "disabledByPolicy"
            ) as Boolean ||
                    getObjectField(
                        param.args[1],
                        "state"
                    ) as Int == Tile.STATE_UNAVAILABLE
        } catch (throwable: Throwable) {
            getObjectField(
                param.args[1],
                "state"
            ) as Int == Tile.STATE_UNAVAILABLE
        }

        val isActiveState: Boolean = try {
            getObjectField(
                param.args[1],
                "state"
            ) as Int == Tile.STATE_ACTIVE
        } catch (throwable: Throwable) {
            try {
                param.args[1] as Int == Tile.STATE_ACTIVE
            } catch (throwable1: Throwable) {
                try {
                    param.args[1] as Boolean
                } catch (throwable2: Throwable) {
                    log(TAG + throwable2)
                    false
                }
            }
        }

        return Pair(isDisabledState, isActiveState)
    }

    private fun initColors(force: Boolean) {
        val isDark: Boolean = SystemUtil.isDarkMode
        if (isDark == this.isDark && !force) return

        this.isDark = isDark

        calculateColors()
    }

    private fun calculateColors() {
        try {
            val res = mContext.resources

            colorText = res.getColor(
                res.getIdentifier(
                    "android:color/system_neutral1_900",
                    "color",
                    mContext.packageName
                ), mContext.theme
            )

            colorTextAlpha = colorText!! and 0xFFFFFF or (Math.round(
                Color.alpha(
                    colorText!!
                ) * 0.8f
            ) shl 24)
        } catch (throwable: Throwable) {
            log(TAG + throwable)
        }
    }

    private fun setHeaderComponentsColor(mView: View, iconManager: Any, batteryIcon: Any) {
        if (!blackQSHeaderEnabled) return

        val textColor = Color.WHITE

        try {
            (mView.findViewById<View>(
                mContext.resources.getIdentifier(
                    "clock",
                    "id",
                    mContext.packageName
                )
            ) as TextView).setTextColor(textColor)

            (mView.findViewById<View>(
                mContext.resources.getIdentifier(
                    "date",
                    "id",
                    mContext.packageName
                )
            ) as TextView).setTextColor(textColor)
        } catch (ignored: Throwable) {
        }

        try {
            try { // A14 ap11
                callMethod(iconManager, "setTint", textColor, textColor)

                modernShadeCarrierGroupMobileViews.forEach(Consumer { view: Any ->
                    setMobileIconTint(
                        view,
                        textColor
                    )
                })

                setModernSignalTextColor(textColor)
            } catch (ignored: Throwable) { // A14 older
                callMethod(iconManager, "setTint", textColor)
            }

            for (i in 1..3) {
                val id = String.format("carrier%s", i)

                try {
                    (getObjectField(
                        mView.findViewById(
                            mContext.resources.getIdentifier(
                                id,
                                "id",
                                mContext.packageName
                            )
                        ), "mCarrierText"
                    ) as TextView).setTextColor(textColor)

                    (getObjectField(
                        mView.findViewById(
                            mContext.resources.getIdentifier(
                                id,
                                "id",
                                mContext.packageName
                            )
                        ), "mMobileSignal"
                    ) as ImageView).setImageTintList(ColorStateList.valueOf(textColor))

                    (getObjectField(
                        mView.findViewById(
                            mContext.resources.getIdentifier(
                                id,
                                "id",
                                mContext.packageName
                            )
                        ), "mMobileRoaming"
                    ) as ImageView).setImageTintList(ColorStateList.valueOf(textColor))
                } catch (ignored: Throwable) {
                }
            }

            callMethod(batteryIcon, "updateColors", textColor, textColor, textColor)
        } catch (throwable: Throwable) {
            log(TAG + throwable)
        }
    }

    private fun setMobileIconTint(modernStatusBarViewBinding: Any, textColor: Int) {
        callMethod(
            modernStatusBarViewBinding,
            "onIconTintChanged",
            textColor,
            textColor
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun setModernSignalTextColor(textColor: Int) {
        val res: Resources = mContext.resources
        if (shadeCarrierGroupController == null) return

        val carrierGroups =
            getObjectField(shadeCarrierGroupController, "mCarrierGroups") as Array<View>?

        carrierGroups?.let {
            for (shadeCarrier in it) {
                try {
                    shadeCarrier.findViewById<View>(
                        res.getIdentifier(
                            "carrier_combo",
                            "id",
                            mContext.packageName
                        )
                    )?.findViewById<TextView>(
                        res.getIdentifier(
                            "mobile_carrier_text",
                            "id",
                            mContext.packageName
                        )
                    )?.setTextColor(textColor)
                } catch (ignored: Throwable) {
                }
            }
        }
    }

    companion object {
        private val TAG = "Iconify - ${QSBlackThemeA14::class.java.simpleName}: "
        private var blackQSHeaderEnabled = false
        private const val PM_LITE_BACKGROUND_CODE = 1
    }
}