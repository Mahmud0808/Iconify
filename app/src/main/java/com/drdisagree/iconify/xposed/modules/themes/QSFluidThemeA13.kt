package com.drdisagree.iconify.xposed.modules.themes

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.service.quicksettings.Tile
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.FLUID_NOTIF_TRANSPARENCY
import com.drdisagree.iconify.common.Preferences.FLUID_POWERMENU_TRANSPARENCY
import com.drdisagree.iconify.common.Preferences.FLUID_QSPANEL
import com.drdisagree.iconify.xposed.HookRes
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.RoundedCornerProgressDrawable
import com.drdisagree.iconify.xposed.modules.utils.SettingsLibUtils
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.utils.SystemUtils
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.findClassIfExists
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.setObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import kotlin.math.max
import kotlin.math.min

@SuppressLint("DiscouragedApi")
class QSFluidThemeA13(context: Context?) : ModPack(context!!) {

    val colorActive = intArrayOf(
        mContext.resources.getColor(
            mContext.resources.getIdentifier(
                "android:color/system_accent1_400",
                "color",
                mContext.packageName
            ), mContext.theme
        )
    )
    val colorActiveAlpha = intArrayOf(
        Color.argb(
            (ACTIVE_ALPHA * 255).toInt(), Color.red(
                colorActive[0]
            ), Color.green(colorActive[0]), Color.blue(colorActive[0])
        )
    )
    var colorInactive = intArrayOf(
        SettingsLibUtils.getColorAttrDefaultColor(
            mContext,
            mContext.resources.getIdentifier("offStateColor", "attr", mContext.packageName)
        )
    )
    val colorInactiveAlpha = intArrayOf(changeAlpha(colorInactive[0], INACTIVE_ALPHA))
    private var wasDark: Boolean = SystemUtils.isDarkMode
    private var mSlider: SeekBar? = null
    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized) return

        Xprefs.apply {
            fluidQsThemeEnabled = getBoolean(FLUID_QSPANEL, false)
            fluidNotifEnabled = fluidQsThemeEnabled &&
                    getBoolean(FLUID_NOTIF_TRANSPARENCY, false)
            fluidPowerMenuEnabled = fluidQsThemeEnabled &&
                    getBoolean(FLUID_POWERMENU_TRANSPARENCY, false)
        }

        initResources()
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val qsPanelClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.QSPanel",
            loadPackageParam.classLoader
        )
        val qsTileViewImplClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.tileimpl.QSTileViewImpl",
            loadPackageParam.classLoader
        )
        val qsIconViewImplClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.tileimpl.QSIconViewImpl",
            loadPackageParam.classLoader
        )
        val footerViewClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.notification.row.FooterView",
            loadPackageParam.classLoader
        )
        val centralSurfacesImplClass = findClassIfExists(
            "$SYSTEMUI_PACKAGE.statusbar.phone.CentralSurfacesImpl",
            loadPackageParam.classLoader
        )
        val notificationExpandButtonClass = findClass(
            "com.android.internal.widget.NotificationExpandButton",
            loadPackageParam.classLoader
        )
        val brightnessSliderViewClass = findClass(
            "$SYSTEMUI_PACKAGE.settings.brightness.BrightnessSliderView",
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
        val activatableNotificationViewClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.notification.row.ActivatableNotificationView",
            loadPackageParam.classLoader
        )

        // Initialize resources and colors
        hookAllMethods(qsTileViewImplClass, "init", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                initResources()
            }
        })

        if (centralSurfacesImplClass != null) {
            hookAllConstructors(centralSurfacesImplClass, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    initResources()
                }
            })

            hookAllMethods(centralSurfacesImplClass, "updateTheme", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    initResources()
                }
            })
        }

        // QS tile color
        hookAllConstructors(qsTileViewImplClass, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                colorInactive[0] = SettingsLibUtils.getColorAttrDefaultColor(
                    mContext,
                    mContext.resources.getIdentifier("offStateColor", "attr", mContext.packageName)
                )
                colorInactiveAlpha[0] = changeAlpha(colorInactive[0], INACTIVE_ALPHA)
            }
        })

        hookAllMethods(qsTileViewImplClass, "getBackgroundColorForState", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!fluidQsThemeEnabled) return

                try {
                    if (param.args[0] as Int == Tile.STATE_ACTIVE) {
                        param.result = changeAlpha(colorActive[0], ACTIVE_ALPHA)
                    } else {
                        val inactiveColor = param.result as Int?

                        inactiveColor?.let {
                            colorInactive[0] = it
                            colorInactiveAlpha[0] = changeAlpha(it, INACTIVE_ALPHA)

                            if (param.args[0] as Int == Tile.STATE_INACTIVE) {
                                param.result = changeAlpha(it, INACTIVE_ALPHA)
                            } else if (param.args[0] as Int == Tile.STATE_UNAVAILABLE) {
                                param.result = changeAlpha(it, UNAVAILABLE_ALPHA)
                            }
                        }
                    }
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        // QS icon color
        hookAllMethods(qsIconViewImplClass, "getIconColorForState", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!fluidQsThemeEnabled) return

                try {
                    if (getObjectField(param.args[1], "state") as Int == Tile.STATE_ACTIVE
                    ) {
                        param.result = colorActive[0]
                    }
                } catch (ignored: Throwable) {
                }
            }
        })

        hookAllMethods(qsIconViewImplClass, "updateIcon", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!fluidQsThemeEnabled) return

                try {
                    if (param.args[0] is ImageView &&
                        getIntField(param.args[1], "state") == Tile.STATE_ACTIVE
                    ) {
                        (param.args[0] as ImageView)
                            .setImageTintList(ColorStateList.valueOf(colorActive[0]))
                    }
                } catch (ignored: Throwable) {
                }
            }
        })

        hookAllMethods(qsIconViewImplClass, "setIcon", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!fluidQsThemeEnabled) return
                try {
                    if (param.args[0] is ImageView &&
                        getIntField(param.args[1], "state") == Tile.STATE_ACTIVE
                    ) {
                        setObjectField(param.thisObject, "mTint", colorActive[0])
                    }
                } catch (ignored: Throwable) {
                }
            }
        })

        try {
            val qsContainerImplClass = findClass(
                "$SYSTEMUI_PACKAGE.qs.QSContainerImpl",
                loadPackageParam.classLoader
            )

            hookAllMethods(qsContainerImplClass, "updateResources", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!fluidQsThemeEnabled) return

                    try {
                        val res = mContext.resources
                        val view = (param.thisObject as ViewGroup).findViewById<ViewGroup>(
                            res.getIdentifier(
                                "qs_footer_actions",
                                "id",
                                mContext.packageName
                            )
                        )

                        view.background.setTint(Color.TRANSPARENT)
                        view.elevation = 0f
                        setAlphaTintedDrawables(view, INACTIVE_ALPHA)

                        try {
                            val securityFooter = (view.findViewById<View>(
                                res.getIdentifier(
                                    "security_footers_container",
                                    "id",
                                    mContext.packageName
                                )
                            ) as ViewGroup).getChildAt(0)

                            securityFooter.background.setTint(colorInactive[0])
                            securityFooter.background.alpha = (INACTIVE_ALPHA * 255).toInt()
                        } catch (ignored: Throwable) {
                        }

                        try {
                            val multiUserSwitch = view.findViewById<View>(
                                res.getIdentifier(
                                    "multi_user_switch",
                                    "id",
                                    mContext.packageName
                                )
                            )

                            multiUserSwitch.background.setTint(colorInactive[0])
                            multiUserSwitch.background.alpha = (INACTIVE_ALPHA * 255).toInt()
                        } catch (ignored: Throwable) {
                        }
                        try {
                            val pmButtonContainer = view.findViewById<ViewGroup>(
                                res.getIdentifier(
                                    "pm_lite",
                                    "id",
                                    mContext.packageName
                                )
                            )

                            pmButtonContainer.background.alpha = (ACTIVE_ALPHA * 255).toInt()
                            pmButtonContainer.background.setTint(colorActive[0])
                            (pmButtonContainer.getChildAt(0) as ImageView).setColorFilter(
                                colorActive[0], PorterDuff.Mode.SRC_IN
                            )
                        } catch (ignored: Throwable) {
                            val pmButton = view.findViewById<ImageView>(
                                res.getIdentifier(
                                    "pm_lite",
                                    "id",
                                    mContext.packageName
                                )
                            )

                            pmButton.background.alpha = (ACTIVE_ALPHA * 255).toInt()
                            pmButton.background.setTint(colorActive[0])
                            pmButton.setImageTintList(ColorStateList.valueOf(colorActive[0]))
                        }
                    } catch (ignored: Throwable) {
                    }
                }
            })
        } catch (ignored: Throwable) {
        }

        // Brightness slider and auto brightness color
        hookAllMethods(brightnessSliderViewClass, "onFinishInflate", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                mSlider = getObjectField(param.thisObject, "mSlider") as SeekBar

                try {
                    if (mSlider != null && fluidQsThemeEnabled) {
                        mSlider!!.progressDrawable = createBrightnessDrawable(mContext)

                        val progress = mSlider!!.progressDrawable as LayerDrawable
                        val progressSlider =
                            progress.findDrawableByLayerId(android.R.id.progress) as DrawableWrapper

                        try {
                            val actualProgressSlider = progressSlider.drawable as LayerDrawable?
                            val mBrightnessIcon = actualProgressSlider!!.findDrawableByLayerId(
                                mContext.resources.getIdentifier(
                                    "slider_icon",
                                    "id",
                                    mContext.packageName
                                )
                            )

                            mBrightnessIcon.setTintList(ColorStateList.valueOf(Color.TRANSPARENT))
                            mBrightnessIcon.alpha = 0
                        } catch (ignored: Throwable) {
                        }
                    }
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        hookAllMethods(brightnessControllerClass, "updateIcon", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!fluidQsThemeEnabled) return

                try {
                    (getObjectField(param.thisObject, "mIcon") as ImageView)
                        .setImageTintList(ColorStateList.valueOf(colorActive[0]))

                    (getObjectField(param.thisObject, "mIcon") as ImageView)
                        .setBackgroundTintList(ColorStateList.valueOf(colorActiveAlpha[0]))
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        if (brightnessSliderControllerClass != null) {
            hookAllConstructors(brightnessSliderControllerClass, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!fluidQsThemeEnabled) return

                    try {
                        (getObjectField(param.thisObject, "mIcon") as ImageView)
                            .setImageTintList(ColorStateList.valueOf(colorActive[0]))

                        (getObjectField(param.thisObject, "mIcon") as ImageView)
                            .setBackgroundTintList(ColorStateList.valueOf(colorActiveAlpha[0]))
                    } catch (throwable: Throwable) {
                        try {
                            (getObjectField(param.thisObject, "mIconView") as ImageView)
                                .setImageTintList(ColorStateList.valueOf(colorActive[0]))

                            (getObjectField(param.thisObject, "mIconView") as ImageView)
                                .setBackgroundTintList(ColorStateList.valueOf(colorActiveAlpha[0]))
                        } catch (ignored: Throwable) {
                        }
                    }
                }
            })
        }

        hookAllMethods(brightnessMirrorControllerClass, "updateIcon", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!fluidQsThemeEnabled) return

                try {
                    (getObjectField(param.thisObject, "mIcon") as ImageView)
                        .setImageTintList(ColorStateList.valueOf(colorActive[0]))

                    (getObjectField(param.thisObject, "mIcon") as ImageView)
                        .setBackgroundTintList(ColorStateList.valueOf(colorActiveAlpha[0]))
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        hookAllMethods(
            brightnessMirrorControllerClass,
            "updateResources",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!fluidQsThemeEnabled) return

                    try {
                        val mBrightnessMirror = getObjectField(
                            param.thisObject,
                            "mBrightnessMirror"
                        ) as FrameLayout

                        mBrightnessMirror.background.alpha = (INACTIVE_ALPHA * 255).toInt()
                    } catch (throwable: Throwable) {
                        log(TAG + throwable)
                    }
                }
            })

        hookAllMethods(qsPanelClass, "updateResources", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!fluidQsThemeEnabled) return

                try {
                    (getObjectField(param.thisObject, "mAutoBrightnessView") as View)
                        .background.setTint(colorActiveAlpha[0])
                } catch (ignored: Throwable) {
                }
            }
        })

        // QS tile primary label color
        hookAllMethods(qsTileViewImplClass, "getLabelColorForState", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!fluidQsThemeEnabled) return

                try {
                    if (param.args[0] as Int == Tile.STATE_ACTIVE) {
                        param.result = colorActive[0]
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
                    if (!fluidQsThemeEnabled) return

                    try {
                        if (param.args[0] as Int == Tile.STATE_ACTIVE) {
                            param.result = colorActive[0]
                        }
                    } catch (throwable: Throwable) {
                        log(TAG + throwable)
                    }
                }
            })

        hookAllConstructors(qsTileViewImplClass, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!fluidQsThemeEnabled) return

                colorInactive[0] = changeAlpha(
                    getObjectField(
                        param.thisObject,
                        "colorInactive"
                    ) as Int, 1.0f
                )

                initResources()

                // For LineageOS based roms
                try {
                    setObjectField(
                        param.thisObject,
                        "colorActive",
                        changeAlpha(colorActive[0], ACTIVE_ALPHA)
                    )

                    setObjectField(
                        param.thisObject,
                        "colorInactive",
                        changeAlpha(
                            getObjectField(param.thisObject, "colorInactive") as Int,
                            INACTIVE_ALPHA
                        )
                    )

                    setObjectField(
                        param.thisObject,
                        "colorUnavailable",
                        changeAlpha(
                            getObjectField(param.thisObject, "colorInactive") as Int,
                            UNAVAILABLE_ALPHA
                        )
                    )

                    setObjectField(
                        param.thisObject,
                        "colorLabelActive",
                        colorActive[0]
                    )

                    setObjectField(
                        param.thisObject,
                        "colorSecondaryLabelActive",
                        colorActive[0]
                    )
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }

                try {
                    if (mSlider != null) {
                        mSlider!!.progressDrawable = createBrightnessDrawable(mContext)

                        val progress = mSlider!!.progressDrawable as LayerDrawable
                        val progressSlider =
                            progress.findDrawableByLayerId(android.R.id.progress) as DrawableWrapper

                        try {
                            val actualProgressSlider = progressSlider.drawable as LayerDrawable?
                            val mBrightnessIcon = actualProgressSlider!!.findDrawableByLayerId(
                                mContext.resources.getIdentifier(
                                    "slider_icon",
                                    "id",
                                    mContext.packageName
                                )
                            )

                            mBrightnessIcon.setTintList(ColorStateList.valueOf(Color.TRANSPARENT))
                            mBrightnessIcon.alpha = 0
                        } catch (ignored: Throwable) {
                        }
                    }
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        hookAllMethods(qsTileViewImplClass, "updateResources", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!fluidQsThemeEnabled) return

                colorInactive[0] = changeAlpha(
                    getObjectField(
                        param.thisObject,
                        "colorInactive"
                    ) as Int, 1.0f
                )

                initResources()

                try {
                    setObjectField(
                        param.thisObject,
                        "colorActive",
                        changeAlpha(colorActive[0], ACTIVE_ALPHA)
                    )

                    setObjectField(
                        param.thisObject,
                        "colorInactive",
                        changeAlpha(
                            getObjectField(param.thisObject, "colorInactive") as Int,
                            INACTIVE_ALPHA
                        )
                    )

                    setObjectField(
                        param.thisObject,
                        "colorUnavailable",
                        changeAlpha(
                            getObjectField(param.thisObject, "colorInactive") as Int,
                            UNAVAILABLE_ALPHA
                        )
                    )

                    setObjectField(
                        param.thisObject,
                        "colorLabelActive",
                        colorActive[0]
                    )

                    setObjectField(
                        param.thisObject,
                        "colorSecondaryLabelActive",
                        colorActive[0]
                    )
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                }
            }
        })

        // Notifications
        hookAllMethods(
            activatableNotificationViewClass,
            "onFinishInflate",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!fluidQsThemeEnabled || !fluidNotifEnabled) return

                    val mBackgroundNormal =
                        getObjectField(param.thisObject, "mBackgroundNormal") as View?
                    mBackgroundNormal?.setAlpha(INACTIVE_ALPHA)
                }
            })

        // Notification expand/collapse pill
        hookAllMethods(notificationExpandButtonClass, "onFinishInflate", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!fluidQsThemeEnabled || !fluidNotifEnabled) return

                val mPillView = (param.thisObject as ViewGroup).findViewById<View?>(
                    mContext.resources.getIdentifier(
                        "expand_button_pill",
                        "id",
                        mContext.packageName
                    )
                )
                mPillView?.background?.alpha = (INACTIVE_ALPHA * 255).toInt()
            }
        })

        // Notification footer buttons
        val updateNotificationFooterButtons: XC_MethodHook = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!fluidQsThemeEnabled || !fluidNotifEnabled) return

                try {
                    val mManageButton =
                        getObjectField(param.thisObject, "mManageButton") as Button
                    val mClearAllButton = try {
                        getObjectField(param.thisObject, "mClearAllButton")
                    } catch (ignored: Throwable) {
                        getObjectField(param.thisObject, "mDismissButton")
                    } as Button

                    mManageButton.background?.alpha = (INACTIVE_ALPHA * 255).toInt()
                    mClearAllButton.background?.alpha = (INACTIVE_ALPHA * 255).toInt()
                } catch (ignored: Throwable) {
                }
            }
        }

        hookAllMethods(footerViewClass, "onFinishInflate", updateNotificationFooterButtons)
        hookAllMethods(footerViewClass, "updateColors", updateNotificationFooterButtons)

        // Power menu
        try {
            val globalActionsDialogLiteSinglePressActionClass = findClass(
                "$SYSTEMUI_PACKAGE.globalactions.GlobalActionsDialogLite\$SinglePressAction",
                loadPackageParam.classLoader
            )
            val globalActionsLayoutLiteClass = findClass(
                "$SYSTEMUI_PACKAGE.globalactions.GlobalActionsLayoutLite",
                loadPackageParam.classLoader
            )

            // Layout background
            hookAllMethods(globalActionsLayoutLiteClass, "onLayout", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!fluidPowerMenuEnabled) return

                    (param.thisObject as View).findViewById<View>(android.R.id.list)
                        .background.alpha = (INACTIVE_ALPHA * 255).toInt()
                }
            })

            // Button Color
            hookAllMethods(
                globalActionsDialogLiteSinglePressActionClass,
                "create",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!fluidPowerMenuEnabled) return

                        val itemView = param.result as View
                        val iconView = itemView.findViewById<ImageView>(android.R.id.icon)
                        iconView.background.alpha = (INACTIVE_ALPHA * 255).toInt()
                    }
                })
        } catch (ignored: Throwable) {
        }

        // Footer button A12
        try {
            if (Build.VERSION.SDK_INT < 33) {
                val footerActionsViewClass = findClass(
                    "$SYSTEMUI_PACKAGE.qs.FooterActionsView",
                    loadPackageParam.classLoader
                )

                val updateFooterButtons: XC_MethodHook = object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val parent = param.thisObject as ViewGroup
                        val childCount = parent.childCount
                        for (i in 0 until childCount) {
                            val childView = parent.getChildAt(i)
                            childView.background?.setTint(colorInactive[0])
                            childView.background?.alpha = (INACTIVE_ALPHA * 255).toInt()
                        }
                    }
                }

                hookAllMethods(footerActionsViewClass, "onFinishInflate", updateFooterButtons)
                hookAllMethods(footerActionsViewClass, "updateResources", updateFooterButtons)
            }
        } catch (ignored: Throwable) {
        }
    }

    private fun initResources() {
        val isDark: Boolean = SystemUtils.isDarkMode

        if (isDark != wasDark) {
            wasDark = isDark
        }

        colorActive[0] = mContext.resources.getColor(
            mContext.resources.getIdentifier(
                "android:color/system_accent1_400",
                "color",
                mContext.packageName
            ), mContext.theme
        )

        colorActiveAlpha[0] = Color.argb(
            (ACTIVE_ALPHA * 255).toInt(), Color.red(
                colorActive[0]
            ), Color.green(colorActive[0]), Color.blue(colorActive[0])
        )

        colorInactiveAlpha[0] = changeAlpha(colorInactive[0], INACTIVE_ALPHA)
    }

    private fun changeAlpha(color: Int, alpha: Float): Int {
        return changeAlpha(color, (alpha * 255).toInt())
    }

    private fun changeAlpha(color: Int, alpha: Int): Int {
        val alphaInRange = max(0.0, min(alpha.toDouble(), 255.0)).toInt()

        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        return Color.argb(alphaInRange, red, green, blue)
    }

    private fun createBrightnessDrawable(context: Context): LayerDrawable {
        val res = context.resources
        val cornerRadius = context.resources.getDimensionPixelSize(
            res.getIdentifier(
                "rounded_slider_corner_radius",
                "dimen",
                context.packageName
            )
        )
        val height = context.resources.getDimensionPixelSize(
            res.getIdentifier(
                "rounded_slider_height",
                "dimen",
                context.packageName
            )
        )
        val startPadding = context.toPx(15)
        val endPadding = context.toPx(15)

        // Create the background shape
        val radiusF = FloatArray(8)
        for (i in 0..7) {
            radiusF[i] = cornerRadius.toFloat()
        }

        val backgroundShape = ShapeDrawable(RoundRectShape(radiusF, null, null))
        backgroundShape.setIntrinsicHeight(height)
        backgroundShape.paint.setColor(changeAlpha(colorInactiveAlpha[0], UNAVAILABLE_ALPHA))

        // Create the progress drawable
        var progressDrawable: RoundedCornerProgressDrawable? = null
        try {
            progressDrawable =
                RoundedCornerProgressDrawable(createBrightnessForegroundDrawable(context))
            progressDrawable.alpha = (ACTIVE_ALPHA * 255).toInt()
            progressDrawable.setTint(colorActive[0])
        } catch (ignored: Throwable) {
        }

        // Create the start and end drawables
        val startDrawable = HookRes.modRes?.let {
            ResourcesCompat.getDrawable(
                it,
                R.drawable.ic_brightness_low,
                context.theme
            )
        }
        val endDrawable = HookRes.modRes?.let {
            ResourcesCompat.getDrawable(
                it,
                R.drawable.ic_brightness_full,
                context.theme
            )
        }
        if (startDrawable != null && endDrawable != null) {
            startDrawable.setTint(colorActive[0])
            endDrawable.setTint(colorActive[0])
        }

        // Create the layer drawable
        val layers = arrayOf(backgroundShape, progressDrawable, startDrawable, endDrawable)
        val layerDrawable = LayerDrawable(layers)
        layerDrawable.setId(0, android.R.id.background)
        layerDrawable.setId(1, android.R.id.progress)
        layerDrawable.setLayerGravity(2, Gravity.START or Gravity.CENTER_VERTICAL)
        layerDrawable.setLayerGravity(3, Gravity.END or Gravity.CENTER_VERTICAL)
        layerDrawable.setLayerInsetStart(2, startPadding)
        layerDrawable.setLayerInsetEnd(3, endPadding)

        return layerDrawable
    }

    private fun createBrightnessForegroundDrawable(context: Context): LayerDrawable {
        val res = context.resources
        val rectangleDrawable = GradientDrawable()
        val cornerRadius = context.resources.getDimensionPixelSize(
            res.getIdentifier(
                "rounded_slider_corner_radius",
                "dimen",
                context.packageName
            )
        )

        rectangleDrawable.setCornerRadius(cornerRadius.toFloat())
        rectangleDrawable.setColor(colorActive[0])

        val layerDrawable = LayerDrawable(arrayOf<Drawable>(rectangleDrawable))
        layerDrawable.setLayerGravity(0, Gravity.FILL_HORIZONTAL or Gravity.CENTER)

        val height = context.toPx(48)
        layerDrawable.setLayerSize(0, layerDrawable.getLayerWidth(0), height)

        return layerDrawable
    }

    fun setAlphaTintedDrawables(view: View, alpha: Float) {
        setAlphaTintedDrawables(view, (alpha * 255).toInt())
    }

    private fun setAlphaTintedDrawables(view: View, alpha: Int) {
        if (view is ViewGroup) {
            val childCount: Int = view.childCount

            for (i in 0 until childCount) {
                val child: View = view.getChildAt(i)
                setAlphaTintedDrawablesRecursively(child, alpha)
            }
        }
    }

    private fun setAlphaTintedDrawablesRecursively(view: View, alpha: Int) {
        val backgroundDrawable = view.background

        if (backgroundDrawable != null) {
            backgroundDrawable.setTint(colorInactive[0])
            backgroundDrawable.alpha = alpha
        }

        if (view is ViewGroup) {
            val childCount: Int = view.childCount

            for (i in 0 until childCount) {
                val child: View = view.getChildAt(i)
                setAlphaTintedDrawablesRecursively(child, alpha)
            }
        }
    }

    companion object {
        private val TAG = "Iconify - ${QSFluidThemeA13::class.java.simpleName}: "
        private const val ACTIVE_ALPHA = 0.2f
        private const val INACTIVE_ALPHA = ACTIVE_ALPHA + 0.2f
        private const val UNAVAILABLE_ALPHA = INACTIVE_ALPHA - 0.1f
        private var fluidQsThemeEnabled = false
        private var fluidNotifEnabled = false
        private var fluidPowerMenuEnabled = false
    }
}