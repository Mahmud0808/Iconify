package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewTreeObserver.OnDrawListener
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.CUSTOM_QS_MARGIN
import com.drdisagree.iconify.common.Preferences.FIX_NOTIFICATION_COLOR
import com.drdisagree.iconify.common.Preferences.FIX_NOTIFICATION_FOOTER_BUTTON_COLOR
import com.drdisagree.iconify.common.Preferences.FIX_QS_TILE_COLOR
import com.drdisagree.iconify.common.Preferences.HIDE_QSLABEL_SWITCH
import com.drdisagree.iconify.common.Preferences.HIDE_QS_FOOTER_BUTTONS
import com.drdisagree.iconify.common.Preferences.HIDE_QS_ON_LOCKSCREEN
import com.drdisagree.iconify.common.Preferences.HIDE_QS_SILENT_TEXT
import com.drdisagree.iconify.common.Preferences.QQS_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.QS_TEXT_ALWAYS_WHITE
import com.drdisagree.iconify.common.Preferences.QS_TEXT_FOLLOW_ACCENT
import com.drdisagree.iconify.common.Preferences.QS_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.VERTICAL_QSTILE_SWITCH
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.Helpers.findClassInArray
import com.drdisagree.iconify.xposed.modules.utils.Helpers.hookAllMethodsMatchPattern
import com.drdisagree.iconify.xposed.modules.utils.Helpers.isPixelVariant
import com.drdisagree.iconify.xposed.modules.utils.SystemUtils.isSecurityPatchBeforeJune2024
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.findClassIfExists
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.setObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class QuickSettings(context: Context?) : ModPack(context!!) {

    private var fixQsTileColor = true
    private var fixNotificationColor = true
    private var fixNotificationFooterButtonsColor = true
    private var qsTextAlwaysWhite = false
    private var qsTextFollowAccent = false
    private var hideQsOnLockscreen = false
    private var hideSilentText = false
    private var hideFooterButtons = false
    private var qqsTopMargin = 100
    private var qsTopMargin = 100
    private var mParam: Any? = null
    private var mFooterButtonsContainer: ViewGroup? = null
    private var mFooterButtonsOnDrawListener: OnDrawListener? = null
    private var mSilentTextContainer: ViewGroup? = null
    private var mSilentTextOnDrawListener: OnDrawListener? = null
    private var mKeyguardStateController: Any? = null
    private val isAtLeastAndroid14 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    private var isVerticalQSTileActive = false
    private var isHideLabelActive = false
    private var customQsMarginsEnabled = false
    private var qsTilePrimaryTextSize: Float? = null
    private var qsTileSecondaryTextSize: Float? = null

    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized) return

        isVerticalQSTileActive = Xprefs.getBoolean(VERTICAL_QSTILE_SWITCH, false)
        isHideLabelActive = Xprefs.getBoolean(HIDE_QSLABEL_SWITCH, false)
        customQsMarginsEnabled = Xprefs.getBoolean(CUSTOM_QS_MARGIN, false)
        qqsTopMargin = Xprefs.getInt(QQS_TOPMARGIN, 100)
        qsTopMargin = Xprefs.getInt(QS_TOPMARGIN, 100)
        fixQsTileColor = isAtLeastAndroid14 &&
                Xprefs.getBoolean(FIX_QS_TILE_COLOR, false)
        fixNotificationColor = isAtLeastAndroid14 &&
                Xprefs.getBoolean(FIX_NOTIFICATION_COLOR, false) &&
                isSecurityPatchBeforeJune2024()
        fixNotificationFooterButtonsColor = isAtLeastAndroid14 &&
                Xprefs.getBoolean(FIX_NOTIFICATION_FOOTER_BUTTON_COLOR, false)
        qsTextAlwaysWhite = Xprefs.getBoolean(QS_TEXT_ALWAYS_WHITE, false)
        qsTextFollowAccent = Xprefs.getBoolean(QS_TEXT_FOLLOW_ACCENT, false)
        hideQsOnLockscreen = Xprefs.getBoolean(HIDE_QS_ON_LOCKSCREEN, false)
        hideSilentText = Xprefs.getBoolean(HIDE_QS_SILENT_TEXT, false)
        hideFooterButtons = Xprefs.getBoolean(HIDE_QS_FOOTER_BUTTONS, false)

        triggerQsElementVisibility()
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        setVerticalTiles(loadPackageParam)
        setQsMargin(loadPackageParam)
        fixQsTileAndLabelColorA14(loadPackageParam)
        fixNotificationColorA14(loadPackageParam)
        manageQsElementVisibility(loadPackageParam)
        disableQsOnSecureLockScreen(loadPackageParam)
    }

    private fun setVerticalTiles(loadPackageParam: LoadPackageParam) {
        val qsTileViewImplClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.tileimpl.QSTileViewImpl",
            loadPackageParam.classLoader
        )
        val fontSizeUtils = findClass(
            "$SYSTEMUI_PACKAGE.FontSizeUtils",
            loadPackageParam.classLoader
        )

        hookAllConstructors(qsTileViewImplClass, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!isVerticalQSTileActive) return

                mParam = param.thisObject

                try {
                    (mParam as LinearLayout).gravity = Gravity.CENTER
                    (mParam as LinearLayout).orientation = LinearLayout.VERTICAL

                    (getObjectField(
                        mParam,
                        "label"
                    ) as TextView).setGravity(Gravity.CENTER_HORIZONTAL)

                    (getObjectField(
                        mParam,
                        "secondaryLabel"
                    ) as TextView).setGravity(Gravity.CENTER_HORIZONTAL)

                    (getObjectField(
                        mParam,
                        "labelContainer"
                    ) as LinearLayout).setLayoutParams(
                        MarginLayoutParams(
                            MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT
                        )
                    )

                    (getObjectField(
                        mParam,
                        "sideView"
                    ) as View).visibility = View.GONE

                    (mParam as LinearLayout).removeView(
                        getObjectField(
                            mParam,
                            "labelContainer"
                        ) as LinearLayout
                    )

                    if (!isHideLabelActive) {
                        (getObjectField(
                            mParam,
                            "labelContainer"
                        ) as LinearLayout).gravity = Gravity.CENTER_HORIZONTAL

                        (mParam as LinearLayout).addView(
                            getObjectField(
                                mParam,
                                "labelContainer"
                            ) as LinearLayout
                        )
                    }

                    fixTileLayout(mParam as LinearLayout, mParam)

                    if (qsTilePrimaryTextSize == null || qsTileSecondaryTextSize == null) {
                        try {
                            callStaticMethod(
                                fontSizeUtils,
                                "updateFontSize",
                                mContext.resources.getIdentifier(
                                    "qs_tile_text_size",
                                    "dimen",
                                    mContext.packageName
                                ),
                                getObjectField(mParam, "label")
                            )

                            callStaticMethod(
                                fontSizeUtils,
                                "updateFontSize",
                                mContext.resources.getIdentifier(
                                    "qs_tile_text_size",
                                    "dimen",
                                    mContext.packageName
                                ),
                                getObjectField(mParam, "secondaryLabel")
                            )
                        } catch (ignored: Throwable) {
                        }

                        qsTilePrimaryTextSize = (getObjectField(
                            mParam,
                            "label"
                        ) as TextView).textSize

                        qsTileSecondaryTextSize = (getObjectField(
                            mParam,
                            "secondaryLabel"
                        ) as TextView).textSize
                    }
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        hookAllMethods(qsTileViewImplClass, "onConfigurationChanged", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!isVerticalQSTileActive) return

                fixTileLayout(param.thisObject as LinearLayout, mParam)
            }
        })
    }

    private fun setQsMargin(loadPackageParam: LoadPackageParam) {
        hookAllMethods(Resources::class.java, "getDimensionPixelSize", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!customQsMarginsEnabled) return

                val qqsHeaderResNames = arrayOf(
                    "qs_header_system_icons_area_height",
                    "qqs_layout_margin_top",
                    "qs_header_row_min_height",
                    "large_screen_shade_header_min_height"
                )

                for (resName in qqsHeaderResNames) {
                    try {
                        val resId = mContext.resources.getIdentifier(
                            resName,
                            "dimen",
                            mContext.packageName
                        )

                        if (param.args[0] == resId) {
                            param.result =
                                (qqsTopMargin * mContext.resources.displayMetrics.density).toInt()
                        }
                    } catch (ignored: Throwable) {
                    }
                }

                val qsHeaderResNames = arrayOf(
                    "qs_panel_padding_top",
                    "qs_panel_padding_top_combined_headers",
                    "qs_header_height"
                )

                for (resName in qsHeaderResNames) {
                    try {
                        val resId = mContext.resources.getIdentifier(
                            resName,
                            "dimen",
                            mContext.packageName
                        )

                        if (param.args[0] == resId) {
                            param.result =
                                (qsTopMargin * mContext.resources.displayMetrics.density).toInt()
                        }
                    } catch (ignored: Throwable) {
                    }
                }
            }
        })
        try {
            val quickStatusBarHeader = findClass(
                "$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader",
                loadPackageParam.classLoader
            )

            hookAllMethods(quickStatusBarHeader, "updateResources", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!customQsMarginsEnabled) return

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        try {
                            val res = mContext.resources

                            val qqsLP = callMethod(
                                getObjectField(
                                    param.thisObject,
                                    "mHeaderQsPanel"
                                ), "getLayoutParams"
                            ) as MarginLayoutParams
                            qqsLP.topMargin = mContext.resources.getDimensionPixelSize(
                                res.getIdentifier(
                                    "qqs_layout_margin_top",
                                    "dimen",
                                    mContext.packageName
                                )
                            )

                            callMethod(
                                getObjectField(
                                    param.thisObject,
                                    "mHeaderQsPanel"
                                ), "setLayoutParams", qqsLP
                            )
                        } catch (throwable: Throwable) {
                            log(TAG + throwable)
                        }
                    }
                }
            })
        } catch (throwable: Throwable) {
            log(TAG + throwable)
        }
    }

    private fun fixQsTileAndLabelColorA14(loadPackageParam: LoadPackageParam) {
        if (!isAtLeastAndroid14) return

        try {
            val qsTileViewImplClass = findClass(
                "$SYSTEMUI_PACKAGE.qs.tileimpl.QSTileViewImpl",
                loadPackageParam.classLoader
            )

            val removeQsTileTint: XC_MethodHook = object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (fixQsTileColor) {
                        try {
                            setObjectField(
                                param.thisObject,
                                "colorActive",
                                Color.WHITE
                            )
                            setObjectField(
                                param.thisObject,
                                "colorInactive",
                                Color.TRANSPARENT
                            )
                            setObjectField(
                                param.thisObject,
                                "colorUnavailable",
                                Color.TRANSPARENT
                            )
                        } catch (throwable: Throwable) {
                            log(TAG + throwable)
                        }
                    }
                }
            }

            hookAllConstructors(qsTileViewImplClass, removeQsTileTint)
            hookAllMethods(qsTileViewImplClass, "updateResources", removeQsTileTint)

            hookAllConstructors(qsTileViewImplClass, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!qsTextAlwaysWhite && !qsTextFollowAccent) return

                    @ColorInt val color: Int = qsIconLabelColor
                    @ColorInt val colorAlpha =
                        color and 0xFFFFFF or (Math.round(Color.alpha(color) * 0.8f) shl 24)

                    setObjectField(
                        param.thisObject,
                        "colorLabelActive",
                        color
                    )
                    setObjectField(
                        param.thisObject,
                        "colorSecondaryLabelActive",
                        colorAlpha
                    )
                }
            })

            hookAllMethods(qsTileViewImplClass, "getLabelColorForState", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (isQsIconLabelStateActive(param, 0)) {
                        param.result = qsIconLabelColor
                    }
                }
            })

            hookAllMethods(
                qsTileViewImplClass,
                "getSecondaryLabelColorForState",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (isQsIconLabelStateActive(param, 0)) {
                            @ColorInt val color: Int = qsIconLabelColor
                            @ColorInt val colorAlpha =
                                color and 0xFFFFFF or (Math.round(Color.alpha(color) * 0.8f) shl 24)
                            param.result = colorAlpha
                        }
                    }
                })
        } catch (throwable: Throwable) {
            log(TAG + throwable)
        }

        try {
            val qsIconViewImplClass = findClass(
                "$SYSTEMUI_PACKAGE.qs.tileimpl.QSIconViewImpl",
                loadPackageParam.classLoader
            )

            hookAllMethods(qsIconViewImplClass, "getIconColorForState", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (isQsIconLabelStateActive(param, 1)) {
                        param.result = qsIconLabelColor
                    }
                }
            })

            try {
                hookAllMethods(qsIconViewImplClass, "updateIcon", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (isQsIconLabelStateActive(param, 1)) {
                            val mIcon = param.args[0] as ImageView
                            mIcon.setImageTintList(ColorStateList.valueOf(qsIconLabelColor))
                        }
                    }
                })
            } catch (ignored: Throwable) {
            }
        } catch (throwable: Throwable) {
            log(TAG + throwable)
        }

        try {
            val qsContainerImplClass = findClass(
                "$SYSTEMUI_PACKAGE.qs.QSContainerImpl",
                loadPackageParam.classLoader
            )

            hookAllMethods(qsContainerImplClass, "updateResources", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!qsTextAlwaysWhite && !qsTextFollowAccent) return

                    try {
                        val res = mContext.resources
                        val view = (param.thisObject as ViewGroup).findViewById<ViewGroup>(
                            res.getIdentifier(
                                "qs_footer_actions",
                                "id",
                                mContext.packageName
                            )
                        )

                        @ColorInt val color: Int = qsIconLabelColor

                        try {
                            val pmButtonContainer = view.findViewById<ViewGroup>(
                                res.getIdentifier(
                                    "pm_lite",
                                    "id",
                                    mContext.packageName
                                )
                            )
                            (pmButtonContainer.getChildAt(0) as ImageView).setColorFilter(
                                color,
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
                            pmButton.setImageTintList(ColorStateList.valueOf(color))
                        }
                    } catch (ignored: Throwable) {
                    }
                }
            })
        } catch (ignored: Throwable) {
        }

        try { // Compose implementation of QS Footer actions
            val footerActionsButtonViewModelClass = findClass(
                "$SYSTEMUI_PACKAGE.qs.footer.ui.viewmodel.FooterActionsButtonViewModel",
                loadPackageParam.classLoader
            )

            hookAllConstructors(footerActionsButtonViewModelClass, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!qsTextAlwaysWhite && !qsTextFollowAccent) return
                    if (mContext.resources.getResourceName((param.args[0] as Int))
                            .split("/".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[1] == "pm_lite"
                    ) {
                        param.args[2] = qsIconLabelColor
                    }
                }
            })
        } catch (ignored: Throwable) {
        }

        // Auto brightness icon color
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

        hookAllMethods(brightnessControllerClass, "updateIcon", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!qsTextAlwaysWhite && !qsTextFollowAccent) return

                @ColorInt val color: Int = qsIconLabelColor
                try {
                    (getObjectField(param.thisObject, "mIcon") as ImageView)
                        .setImageTintList(ColorStateList.valueOf(color))
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        if (brightnessSliderControllerClass != null) {
            hookAllConstructors(brightnessSliderControllerClass, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!qsTextAlwaysWhite && !qsTextFollowAccent) return

                    @ColorInt val color: Int = qsIconLabelColor

                    try {
                        (getObjectField(param.thisObject, "mIcon") as ImageView)
                            .setImageTintList(ColorStateList.valueOf(color))
                    } catch (throwable: Throwable) {
                        try {
                            (getObjectField(param.thisObject, "mIconView") as ImageView)
                                .setImageTintList(ColorStateList.valueOf(color))
                        } catch (ignored: Throwable) {
                        }
                    }
                }
            })
        }

        hookAllMethods(brightnessMirrorControllerClass, "updateIcon", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!qsTextAlwaysWhite && !qsTextFollowAccent) return

                @ColorInt val color: Int = qsIconLabelColor

                try {
                    (getObjectField(param.thisObject, "mIcon") as ImageView)
                        .setImageTintList(ColorStateList.valueOf(color))
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })
    }

    private fun fixNotificationColorA14(loadPackageParam: LoadPackageParam) {
        if (!isAtLeastAndroid14) return

        try {
            val activatableNotificationViewClass = findClass(
                "$SYSTEMUI_PACKAGE.statusbar.notification.row.ActivatableNotificationView",
                loadPackageParam.classLoader
            )
            val notificationBackgroundViewClass = findClass(
                "$SYSTEMUI_PACKAGE.statusbar.notification.row.NotificationBackgroundView",
                loadPackageParam.classLoader
            )
            var footerViewClass = findClassIfExists(
                "$SYSTEMUI_PACKAGE.statusbar.notification.footer.ui.view.FooterView",
                loadPackageParam.classLoader
            )
            if (footerViewClass == null) {
                footerViewClass = findClass(
                    "$SYSTEMUI_PACKAGE.statusbar.notification.row.FooterView",
                    loadPackageParam.classLoader
                )
            }

            val removeNotificationTint: XC_MethodHook = object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!fixNotificationColor) return

                    val notificationBackgroundView = getObjectField(
                        param.thisObject,
                        "mBackgroundNormal"
                    ) as View

                    try {
                        setObjectField(
                            param.thisObject,
                            "mCurrentBackgroundTint",
                            param.args[0] as Int
                        )
                    } catch (ignored: Throwable) {
                    }

                    try {
                        setObjectField(notificationBackgroundView, "mTintColor", 0)
                    } catch (ignored: Throwable) {
                    }
                }

                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!fixNotificationColor) return

                    val notificationBackgroundView = getObjectField(
                        param.thisObject,
                        "mBackgroundNormal"
                    ) as View

                    try {
                        callMethod(notificationBackgroundView, "setColorFilter", 0)
                    } catch (ignored: Throwable) {
                    }

                    try {
                        (getObjectField(
                            notificationBackgroundView,
                            "mBackground"
                        ) as Drawable).colorFilter = null
                    } catch (ignored: Throwable) {
                    }

                    try {
                        setObjectField(notificationBackgroundView, "mTintColor", 0)
                    } catch (ignored: Throwable) {
                    }

                    Handler(Looper.getMainLooper()).post {
                        notificationBackgroundView.invalidate()
                    }
                }
            }

            hookAllMethods(
                activatableNotificationViewClass,
                "setBackgroundTintColor",
                removeNotificationTint
            )
            hookAllMethods(
                activatableNotificationViewClass,
                "updateBackgroundTint",
                removeNotificationTint
            )

            hookAllMethods(activatableNotificationViewClass,
                "calculateBgColor",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!fixNotificationColor) return

                        try {
                            param.result = getObjectField(
                                param.thisObject,
                                "mCurrentBackgroundTint"
                            )
                        } catch (ignored: Throwable) {
                        }
                    }
                }
            )

            hookAllMethodsMatchPattern(notificationBackgroundViewClass,
                "setCustomBackground.*",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!fixNotificationColor) return

                        setObjectField(param.thisObject, "mTintColor", 0)
                    }
                }
            )

            hookAllMethodsMatchPattern(footerViewClass,
                "updateColors.*",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!fixNotificationFooterButtonsColor) return

                        try {
                            val mManageButton = try {
                                getObjectField(param.thisObject, "mManageButton")
                            } catch (ignored: Throwable) {
                                getObjectField(param.thisObject, "mManageOrHistoryButton")
                            } as Button
                            val mClearAllButton = try {
                                getObjectField(param.thisObject, "mClearAllButton")
                            } catch (ignored: Throwable) {
                                getObjectField(param.thisObject, "mDismissButton")
                            } as Button

                            mManageButton.background?.colorFilter = null
                            mClearAllButton.background?.colorFilter = null

                            Handler(Looper.getMainLooper()).post {
                                mManageButton.invalidate()
                                mClearAllButton.invalidate()
                            }
                        } catch (ignored: Throwable) {
                        }
                    }
                }
            )
        } catch (throwable: Throwable) {
            log(TAG + throwable)
        }
    }

    private fun manageQsElementVisibility(loadPackageParam: LoadPackageParam) {
        try {
            val footerViewClass = findClassInArray(
                loadPackageParam.classLoader,
                "$SYSTEMUI_PACKAGE.statusbar.notification.footer.ui.view.FooterView",
                "$SYSTEMUI_PACKAGE.statusbar.notification.row.FooterView"
            )

            hookAllMethods(footerViewClass, "onFinishInflate", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val view = param.thisObject as View

                    val resId1 = mContext.resources.getIdentifier(
                        "manage_text",
                        "id",
                        mContext.packageName
                    )

                    val resId2 = mContext.resources.getIdentifier(
                        "dismiss_text",
                        "id",
                        mContext.packageName
                    )

                    if (resId1 != 0) {
                        mFooterButtonsContainer =
                            view.findViewById<View>(resId1).parent as ViewGroup
                    } else if (resId2 != 0) {
                        mFooterButtonsContainer =
                            view.findViewById<View>(resId2).parent as ViewGroup
                    }

                    triggerQsElementVisibility()
                }
            })
        } catch (throwable: Throwable) {
            log(TAG + throwable)
        }

        try {
            val sectionHeaderViewClass = findClass(
                "$SYSTEMUI_PACKAGE.statusbar.notification.stack.SectionHeaderView",
                loadPackageParam.classLoader
            )

            hookAllMethods(sectionHeaderViewClass, "onFinishInflate", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    mSilentTextContainer = param.thisObject as ViewGroup

                    triggerQsElementVisibility()
                }
            })
        } catch (throwable: Throwable) {
            log(TAG + throwable)
        }
    }

    private fun disableQsOnSecureLockScreen(loadPackageParam: LoadPackageParam) {
        val remoteInputQuickSettingsDisablerClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.policy.RemoteInputQuickSettingsDisabler",
            loadPackageParam.classLoader
        )
        val phoneStatusBarPolicyClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.PhoneStatusBarPolicy",
            loadPackageParam.classLoader
        )

        hookAllConstructors(phoneStatusBarPolicyClass, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                mKeyguardStateController = getObjectField(
                    param.thisObject,
                    "mKeyguardStateController"
                )

                if (mKeyguardStateController == null) {
                    log(TAG + "mKeyguardStateController is null")
                }
            }
        })

        hookAllMethods(remoteInputQuickSettingsDisablerClass,
            "adjustDisableFlags",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!hideQsOnLockscreen || mKeyguardStateController == null) return

                    val isUnlocked = !(getObjectField(
                        mKeyguardStateController,
                        "mShowing"
                    ) as Boolean) || getObjectField(
                        mKeyguardStateController,
                        "mCanDismissLockScreen"
                    ) as Boolean

                    /*
                     * Location: frameworks/base/core/java/android/app/StatusBarManager.java
                     * public static final int DISABLE2_QUICK_SETTINGS = 1;
                     */
                    param.result = if (hideQsOnLockscreen && !isUnlocked) {
                        param.args[0] as Int or 1 // DISABLE2_QUICK_SETTINGS
                    } else {
                        param.args[0]
                    }
                }
            })
    }

    private fun isQsIconLabelStateActive(param: MethodHookParam?, stateIndex: Int): Boolean {
        if (param?.args == null) return false

        if (!qsTextAlwaysWhite && !qsTextFollowAccent) return false

        val isActiveState: Boolean = try {
            getObjectField(
                param.args[stateIndex],
                "state"
            ) as Int == Tile.STATE_ACTIVE
        } catch (throwable: Throwable) {
            try {
                param.args[stateIndex] as Int == Tile.STATE_ACTIVE
            } catch (throwable1: Throwable) {
                try {
                    param.args[stateIndex] as Boolean
                } catch (throwable2: Throwable) {
                    log(TAG + throwable2)
                    false
                }
            }
        }

        return isActiveState
    }

    @get:ColorInt
    private val qsIconLabelColor: Int
        get() {
            try {
                if (qsTextAlwaysWhite) {
                    return Color.WHITE
                } else if (qsTextFollowAccent) {
                    return mContext.resources.getColor(
                        mContext.resources.getIdentifier(
                            if (isPixelVariant) {
                                "android:color/holo_green_light"
                            } else {
                                "android:color/holo_blue_light"
                            },
                            "color",
                            mContext.packageName
                        ), mContext.theme
                    )
                }
            } catch (throwable: Throwable) {
                log(TAG + throwable)
            }
            return Color.WHITE
        }

    private fun triggerQsElementVisibility() {
        if (mFooterButtonsContainer != null) {
            if (mFooterButtonsOnDrawListener == null) {
                mFooterButtonsOnDrawListener =
                    OnDrawListener { mFooterButtonsContainer!!.visibility = View.INVISIBLE }
            }

            try {
                if (hideFooterButtons) {
                    mFooterButtonsContainer!!.visibility = View.INVISIBLE
                    mFooterButtonsContainer!!.getViewTreeObserver()
                        .addOnDrawListener(mFooterButtonsOnDrawListener)
                } else {
                    mFooterButtonsContainer!!.getViewTreeObserver()
                        .removeOnDrawListener(mFooterButtonsOnDrawListener)
                    mFooterButtonsContainer!!.visibility = View.VISIBLE
                }
            } catch (ignored: Throwable) {
            }
        }

        if (mSilentTextContainer != null) {
            if (mSilentTextOnDrawListener == null) {
                mSilentTextOnDrawListener =
                    OnDrawListener { mSilentTextContainer!!.visibility = View.GONE }
            }

            try {
                if (hideSilentText) {
                    mSilentTextContainer!!.visibility = View.GONE
                    mSilentTextContainer!!.getViewTreeObserver()
                        .addOnDrawListener(mSilentTextOnDrawListener)
                } else {
                    mSilentTextContainer!!.getViewTreeObserver()
                        .removeOnDrawListener(mSilentTextOnDrawListener)
                    mSilentTextContainer!!.visibility = View.VISIBLE
                }
            } catch (ignored: Throwable) {
            }
        }
    }

    private fun fixTileLayout(tile: LinearLayout, param: Any?) {
        val mRes = mContext.resources
        val padding = mRes.getDimensionPixelSize(
            mRes.getIdentifier(
                "qs_tile_padding",
                "dimen",
                mContext.packageName
            )
        )

        tile.setPadding(padding, padding, padding, padding)
        tile.gravity = Gravity.CENTER
        tile.orientation = LinearLayout.VERTICAL

        if (!isHideLabelActive) {
            try {
                ((getObjectField(
                    tile,
                    "labelContainer"
                ) as LinearLayout).layoutParams as MarginLayoutParams).setMarginStart(0)

                ((getObjectField(
                    tile,
                    "labelContainer"
                ) as LinearLayout).layoutParams as MarginLayoutParams).topMargin = mContext.toPx(2)
            } catch (throwable: Throwable) {
                log(TAG + throwable)
            }
        }

        if (param != null) {
            (getObjectField(param, "label") as TextView)
                .setGravity(Gravity.CENTER_HORIZONTAL)

            (getObjectField(param, "secondaryLabel") as TextView)
                .setGravity(Gravity.CENTER_HORIZONTAL)
        }
    }

    companion object {
        private val TAG = "Iconify - ${QuickSettings::class.java.simpleName}: "
    }
}