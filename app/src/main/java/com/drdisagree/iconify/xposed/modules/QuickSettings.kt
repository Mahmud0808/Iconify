package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
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
import androidx.core.content.res.ResourcesCompat
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.FIX_NOTIFICATION_COLOR
import com.drdisagree.iconify.common.Preferences.FIX_QS_TILE_COLOR
import com.drdisagree.iconify.common.Preferences.HIDE_QSLABEL_SWITCH
import com.drdisagree.iconify.common.Preferences.HIDE_QS_FOOTER_BUTTONS
import com.drdisagree.iconify.common.Preferences.HIDE_QS_SILENT_TEXT
import com.drdisagree.iconify.common.Preferences.QQS_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.QS_TEXT_ALWAYS_WHITE
import com.drdisagree.iconify.common.Preferences.QS_TEXT_FOLLOW_ACCENT
import com.drdisagree.iconify.common.Preferences.QS_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.VERTICAL_QSTILE_SWITCH
import com.drdisagree.iconify.config.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.Helpers.isPixelVariant
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.toPx
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
    private var qsTextAlwaysWhite = false
    private var qsTextFollowAccent = false
    private var hideFooterButtons = false
    private var hideSilentText = false
    private var qqsTopMargin = 100
    private var qsTopMargin = 100
    private var mParam: Any? = null
    private var mFooterButtonsContainer: ViewGroup? = null
    private var mFooterButtonsOnDrawListener: OnDrawListener? = null
    private var mSilentTextContainer: ViewGroup? = null
    private var mSilentTextOnDrawListener: OnDrawListener? = null

    override fun updatePrefs(vararg key: String) {
        if (Xprefs == null) return

        isVerticalQSTileActive = Xprefs!!.getBoolean(VERTICAL_QSTILE_SWITCH, false)
        isHideLabelActive = Xprefs!!.getBoolean(HIDE_QSLABEL_SWITCH, false)
        qqsTopMarginEnabled = Xprefs!!.getInt(QQS_TOPMARGIN, -1) != -1
        qsTopMarginEnabled = Xprefs!!.getInt(QS_TOPMARGIN, -1) != -1
        qqsTopMargin = Xprefs!!.getInt(QQS_TOPMARGIN, 100)
        qsTopMargin = Xprefs!!.getInt(QS_TOPMARGIN, 100)
        fixQsTileColor = Build.VERSION.SDK_INT >= 34 &&
                Xprefs!!.getBoolean(FIX_QS_TILE_COLOR, true)
        fixNotificationColor = Build.VERSION.SDK_INT >= 34 &&
                Xprefs!!.getBoolean(FIX_NOTIFICATION_COLOR, true)
        qsTextAlwaysWhite = Xprefs!!.getBoolean(QS_TEXT_ALWAYS_WHITE, false)
        qsTextFollowAccent = Xprefs!!.getBoolean(QS_TEXT_FOLLOW_ACCENT, false)
        hideSilentText = Xprefs!!.getBoolean(HIDE_QS_SILENT_TEXT, false)
        hideFooterButtons = Xprefs!!.getBoolean(HIDE_QS_FOOTER_BUTTONS, false)

        triggerQsElementVisibility()
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        setVerticalTiles(loadPackageParam)
        setQsMargin(loadPackageParam)
        fixQsTileAndLabelColorA14(loadPackageParam)
        fixNotificationColorA14(loadPackageParam)
        manageQsElementVisibility(loadPackageParam)
    }

    private fun setVerticalTiles(loadPackageParam: LoadPackageParam) {
        val qsTileViewImpl = findClass(
            "$SYSTEMUI_PACKAGE.qs.tileimpl.QSTileViewImpl",
            loadPackageParam.classLoader
        )
        val fontSizeUtils = findClass(
            "$SYSTEMUI_PACKAGE.FontSizeUtils",
            loadPackageParam.classLoader
        )

        hookAllConstructors(qsTileViewImpl, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!isVerticalQSTileActive) return

                mParam = param.thisObject

                try {
                    (param.thisObject as LinearLayout).gravity = Gravity.CENTER
                    (param.thisObject as LinearLayout).orientation = LinearLayout.VERTICAL

                    (getObjectField(
                        param.thisObject,
                        "label"
                    ) as TextView).setGravity(Gravity.CENTER_HORIZONTAL)

                    (getObjectField(
                        param.thisObject,
                        "secondaryLabel"
                    ) as TextView).setGravity(Gravity.CENTER_HORIZONTAL)

                    (getObjectField(
                        param.thisObject,
                        "labelContainer"
                    ) as LinearLayout).setLayoutParams(
                        MarginLayoutParams(
                            MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT
                        )
                    )

                    (getObjectField(
                        param.thisObject,
                        "sideView"
                    ) as View).visibility = View.GONE

                    (param.thisObject as LinearLayout).removeView(
                        getObjectField(
                            param.thisObject,
                            "labelContainer"
                        ) as LinearLayout
                    )

                    if (!isHideLabelActive) {
                        (getObjectField(
                            param.thisObject,
                            "labelContainer"
                        ) as LinearLayout).gravity = Gravity.CENTER_HORIZONTAL

                        (param.thisObject as LinearLayout).addView(
                            getObjectField(
                                param.thisObject,
                                "labelContainer"
                            ) as LinearLayout
                        )
                    }

                    fixTileLayout(param.thisObject as LinearLayout, mParam)

                    if (QsTilePrimaryTextSize == null || QsTileSecondaryTextSize == null) {
                        try {
                            callStaticMethod(
                                fontSizeUtils,
                                "updateFontSize",
                                mContext.resources.getIdentifier(
                                    "qs_tile_text_size",
                                    "dimen",
                                    mContext.packageName
                                ),
                                getObjectField(param.thisObject, "label")
                            )

                            callStaticMethod(
                                fontSizeUtils,
                                "updateFontSize",
                                mContext.resources.getIdentifier(
                                    "qs_tile_text_size",
                                    "dimen",
                                    mContext.packageName
                                ),
                                getObjectField(param.thisObject, "secondaryLabel")
                            )
                        } catch (ignored: Throwable) {
                        }

                        val primaryText =
                            getObjectField(param.thisObject, "label") as TextView
                        val secondaryText = getObjectField(
                            param.thisObject,
                            "secondaryLabel"
                        ) as TextView

                        QsTilePrimaryTextSize = primaryText.textSize
                        QsTileSecondaryTextSize = secondaryText.textSize
                    }
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        hookAllMethods(qsTileViewImpl, "onConfigurationChanged", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!isVerticalQSTileActive) return
                fixTileLayout(param.thisObject as LinearLayout, mParam)
            }
        })
    }

    private fun setQsMargin(loadPackageParam: LoadPackageParam) {
        hookAllMethods(Resources::class.java, "getDimensionPixelSize", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (qqsTopMarginEnabled) {
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
                }

                if (qsTopMarginEnabled) {
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
            }
        })
        try {
            val quickStatusBarHeader = findClass(
                "$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader",
                loadPackageParam.classLoader
            )

            hookAllMethods(quickStatusBarHeader, "updateResources", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!qqsTopMarginEnabled) return

                    if (Build.VERSION.SDK_INT >= 33) {
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
        try {
            val qsTileViewImplClass = findClass(
                "$SYSTEMUI_PACKAGE.qs.tileimpl.QSTileViewImpl",
                loadPackageParam.classLoader
            )

            val removeQsTileTint: XC_MethodHook = object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (fixQsTileColor && Build.VERSION.SDK_INT >= 34) {
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
        } else {
            log(TAG + "Not a crash... BrightnessSliderController class not found.")
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
        if (Build.VERSION.SDK_INT < 34) return

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
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!fixNotificationColor) return

                    val notificationBackgroundView =
                        getObjectField(param.thisObject, "mBackgroundNormal") as View

                    try {
                        setObjectField(
                            param.thisObject,
                            "mCurrentBackgroundTint",
                            param.args[0] as Int
                        )
                    } catch (ignored: Throwable) {
                    }

                    try {
                        callMethod(
                            getObjectField(
                                notificationBackgroundView,
                                "mBackground"
                            ), "clearColorFilter"
                        )
                    } catch (ignored: Throwable) {
                    }

                    try {
                        callMethod(notificationBackgroundView, "setColorFilter", 0)
                    } catch (ignored: Throwable) {
                    }

                    setObjectField(notificationBackgroundView, "mTintColor", 0)

                    notificationBackgroundView.invalidate()
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

            val replaceTintColor: XC_MethodHook = object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!fixNotificationColor) return

                    setObjectField(param.thisObject, "mTintColor", 0)
                }
            }

            try {
                hookAllMethods(
                    notificationBackgroundViewClass,
                    "setCustomBackground$1",
                    replaceTintColor
                )
            } catch (ignored: Throwable) {
            }

            try {
                hookAllMethods(
                    notificationBackgroundViewClass,
                    "setCustomBackground",
                    replaceTintColor
                )
            } catch (ignored: Throwable) {
            }

            val removeButtonTint: XC_MethodHook = object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!fixNotificationColor) return

                    val mClearAllButton =
                        getObjectField(param.thisObject, "mClearAllButton") as Button
                    val mManageButton =
                        getObjectField(param.thisObject, "mManageButton") as Button

                    mClearAllButton.background.clearColorFilter()
                    mManageButton.background.clearColorFilter()

                    mClearAllButton.invalidate()
                    mManageButton.invalidate()
                }
            }

            try {
                hookAllMethods(footerViewClass, "updateColors", removeButtonTint)
            } catch (ignored: Throwable) {
            }

            try {
                hookAllMethods(footerViewClass, "updateColors$3", removeButtonTint)
            } catch (ignored: Throwable) {
            }
        } catch (throwable: Throwable) {
            log(TAG + throwable)
        }
    }

    private fun manageQsElementVisibility(loadPackageParam: LoadPackageParam) {
        try {
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
                    return ResourcesCompat.getColor(
                        mContext.resources,
                        if (isPixelVariant) android.R.color.holo_green_light else android.R.color.holo_blue_light,
                        mContext.theme
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
        private var isVerticalQSTileActive = false
        private var isHideLabelActive = false
        private var QsTilePrimaryTextSize: Float? = null
        private var QsTileSecondaryTextSize: Float? = null
        private var qqsTopMarginEnabled = false
        private var qsTopMarginEnabled = false
    }
}
