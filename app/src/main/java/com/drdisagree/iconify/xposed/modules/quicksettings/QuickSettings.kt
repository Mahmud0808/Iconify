package com.drdisagree.iconify.xposed.modules.quicksettings

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewGroup.TEXT_ALIGNMENT_CENTER
import android.view.ViewTreeObserver.OnDrawListener
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.COMPACT_MEDIA_PLAYER
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_QS_MARGIN
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_QS_TEXT_COLOR
import com.drdisagree.iconify.data.common.Preferences.FIX_NOTIFICATION_COLOR
import com.drdisagree.iconify.data.common.Preferences.FIX_NOTIFICATION_FOOTER_BUTTON_COLOR
import com.drdisagree.iconify.data.common.Preferences.FIX_QS_TILE_COLOR
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HIDE_QSLABEL_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HIDE_QS_FOOTER_BUTTONS
import com.drdisagree.iconify.data.common.Preferences.HIDE_QS_ON_LOCKSCREEN
import com.drdisagree.iconify.data.common.Preferences.HIDE_QS_SILENT_TEXT
import com.drdisagree.iconify.data.common.Preferences.QQS_TOPMARGIN_LANDSCAPE
import com.drdisagree.iconify.data.common.Preferences.QQS_TOPMARGIN_PORTRAIT
import com.drdisagree.iconify.data.common.Preferences.QS_TOPMARGIN_LANDSCAPE
import com.drdisagree.iconify.data.common.Preferences.QS_TOPMARGIN_PORTRAIT
import com.drdisagree.iconify.data.common.Preferences.SELECTED_QS_TEXT_COLOR
import com.drdisagree.iconify.data.common.Preferences.HORIZONTAL_QSTILE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.VERTICAL_QSTILE_SWITCH
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.DisplayUtils.isLandscape
import com.drdisagree.iconify.xposed.modules.extras.utils.DisplayUtils.isNightMode
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethodMatchPattern
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.isMethodAvailable
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setFieldSilently
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class QuickSettings(context: Context) : ModPack(context) {

    private var fixQsTileColor = true
    private var fixNotificationColor = true
    private var fixNotificationFooterButtonsColor = true
    private var customQsTextColor = false
    private var selectedQsTextColor = 0
    private var qsTextAccentColor = Color.BLUE
    private var hideQsOnLockscreen = false
    private var hideSilentText = false
    private var hideFooterButtons = false
    private var qqsTopMarginPort = 100
    private var qsTopMarginPort = 100
    private var qqsTopMarginLand = 0
    private var qsTopMarginLand = 0
    private var mParam: Any? = null
    private var mFooterButtonsContainer: ViewGroup? = null
    private var mFooterButtonsOnDrawListener: OnDrawListener? = null
    private var mSilentTextContainer: ViewGroup? = null
    private var mSilentTextOnDrawListener: OnDrawListener? = null
    private var mKeyguardStateController: Any? = null
    private val isAtLeastAndroid14 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    private var isVerticalQSTileActive = false
    private var isHorizontalQSTileActive = false
    private var isHideLabelActive = false
    private var customQsMarginsEnabled = false
    private var qsTilePrimaryTextSize: Float? = null
    private var qsTileSecondaryTextSize: Float? = null
    private var compactMediaPlayerEnabled = false
    private var showHeaderClock = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            isVerticalQSTileActive = getBoolean(VERTICAL_QSTILE_SWITCH, false)
            isHorizontalQSTileActive = getBoolean(HORIZONTAL_QSTILE_SWITCH, false)
            isHideLabelActive = getBoolean(HIDE_QSLABEL_SWITCH, false)
            customQsMarginsEnabled = getBoolean(CUSTOM_QS_MARGIN, false)
            qqsTopMarginPort = getSliderInt(QQS_TOPMARGIN_PORTRAIT, 100)
            qsTopMarginPort = getSliderInt(QS_TOPMARGIN_PORTRAIT, 100)
            qqsTopMarginLand = getSliderInt(QQS_TOPMARGIN_LANDSCAPE, 0)
            qsTopMarginLand = getSliderInt(QS_TOPMARGIN_LANDSCAPE, 0)
            fixQsTileColor = isAtLeastAndroid14 &&
                    getBoolean(FIX_QS_TILE_COLOR, false)
            fixNotificationColor = isAtLeastAndroid14 &&
                    getBoolean(FIX_NOTIFICATION_COLOR, false)
            fixNotificationFooterButtonsColor = isAtLeastAndroid14 &&
                    getBoolean(FIX_NOTIFICATION_FOOTER_BUTTON_COLOR, false)
            customQsTextColor = getBoolean(CUSTOM_QS_TEXT_COLOR, false)
            selectedQsTextColor = getString(SELECTED_QS_TEXT_COLOR, "0")!!.toInt()
            hideQsOnLockscreen = getBoolean(HIDE_QS_ON_LOCKSCREEN, false)
            hideSilentText = getBoolean(HIDE_QS_SILENT_TEXT, false)
            hideFooterButtons = getBoolean(HIDE_QS_FOOTER_BUTTONS, false)
            showHeaderClock = getBoolean(HEADER_CLOCK_SWITCH, false)
            compactMediaPlayerEnabled = getBoolean(COMPACT_MEDIA_PLAYER, false)
            isPixelVariant = getIsPixelVariant()
        }

        triggerQsElementVisibility()
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        initQsAccentColor()
        setVerticalTiles()
        setHorizontalTiles()
        setQsMargin()
        fixQsTileAndLabelColorA14()
        fixNotificationColorA14()
        manageQsElementVisibility()
        disableQsOnSecureLockScreen()
        compactMediaPlayer()
    }

    private fun setVerticalTiles() {
        val qsTileViewImplClass = findClass("$SYSTEMUI_PACKAGE.qs.tileimpl.QSTileViewImpl")
        val fontSizeUtils = findClass("$SYSTEMUI_PACKAGE.FontSizeUtils")

        qsTileViewImplClass
            .hookConstructor()
            .runAfter { param ->
                if (!isVerticalQSTileActive) return@runAfter

                mParam = param.thisObject

                try {
                    (mParam as LinearLayout).gravity = Gravity.CENTER
                    (mParam as LinearLayout).orientation = LinearLayout.VERTICAL

                    (mParam.getField(
                        "label"
                    ) as TextView).textAlignment = TEXT_ALIGNMENT_CENTER

                    (mParam.getField(
                        "secondaryLabel"
                    ) as TextView).textAlignment = TEXT_ALIGNMENT_CENTER

                    (mParam.getField(
                        "labelContainer"
                    ) as LinearLayout).layoutParams = MarginLayoutParams(
                        MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT
                    )

                    (mParam.getField("sideView") as View).visibility = View.GONE

                    (mParam as LinearLayout).removeView(
                        mParam.getField("labelContainer") as LinearLayout
                    )

                    if (!isHideLabelActive) {
                        (mParam.getField(
                            "labelContainer"
                        ) as LinearLayout).gravity = Gravity.CENTER_HORIZONTAL

                        (mParam as LinearLayout).addView(
                            mParam.getField("labelContainer") as LinearLayout
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
                                mParam.getField("label")
                            )

                            callStaticMethod(
                                fontSizeUtils,
                                "updateFontSize",
                                mContext.resources.getIdentifier(
                                    "qs_tile_text_size",
                                    "dimen",
                                    mContext.packageName
                                ),
                                mParam.getField("secondaryLabel")
                            )
                        } catch (ignored: Throwable) {
                        }

                        qsTilePrimaryTextSize = (mParam.getField(
                            "label"
                        ) as TextView).textSize

                        qsTileSecondaryTextSize = (mParam.getField(
                            "secondaryLabel"
                        ) as TextView).textSize
                    }
                } catch (throwable: Throwable) {
                    log(this@QuickSettings, throwable)
                }
            }

        qsTileViewImplClass
            .hookMethod("onConfigurationChanged")
            .runAfter { param ->
                if (!isVerticalQSTileActive) return@runAfter

                fixTileLayout(param.thisObject as LinearLayout, mParam)
            }
    }

    private fun setHorizontalTiles() {
        val qsTileViewImplClass = findClass("$SYSTEMUI_PACKAGE.qs.tileimpl.QSTileViewImpl")

        qsTileViewImplClass
            .hookConstructor()
            .runAfter { param ->
                if (!isHorizontalQSTileActive || isVerticalQSTileActive) return@runAfter

                try {
                    val tile = param.thisObject as LinearLayout

                    tile.orientation = LinearLayout.HORIZONTAL
                    tile.gravity = Gravity.CENTER_VERTICAL or Gravity.START

                    val labelContainer = param.thisObject.getField(
                        "labelContainer"
                    ) as LinearLayout

                    labelContainer.layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    ).apply {
                        marginStart = mContext.toPx(8)
                    }

                    (param.thisObject.getField(
                        "label"
                    ) as TextView).apply {
                        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                    }

                    (param.thisObject.getField(
                        "secondaryLabel"
                    ) as TextView).apply {
                        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                    }

                    (param.thisObject.getField("sideView") as View).visibility = View.GONE
                } catch (throwable: Throwable) {
                    log(this@QuickSettings, throwable)
                }
            }

        qsTileViewImplClass
            .hookMethod("onConfigurationChanged")
            .runAfter { param ->
                if (!isHorizontalQSTileActive || isVerticalQSTileActive) return@runAfter

                try {
                    val tile = param.thisObject as LinearLayout

                    tile.orientation = LinearLayout.HORIZONTAL
                    tile.gravity = Gravity.CENTER_VERTICAL or Gravity.START

                    val labelContainer = param.thisObject.getField(
                        "labelContainer"
                    ) as LinearLayout

                    (labelContainer.layoutParams as LinearLayout.LayoutParams).apply {
                        width = 0
                        weight = 1f
                        marginStart = mContext.toPx(8)
                    }
                } catch (throwable: Throwable) {
                    log(this@QuickSettings, throwable)
                }
            }
    }

    private fun setQsMargin() {
        fun getQqsMargin() = if (mContext.isLandscape) qqsTopMarginLand else qqsTopMarginPort
        fun getQsMargin() = if (mContext.isLandscape) qsTopMarginLand else qsTopMarginPort

        ResourceHookManager
            .hookDimen()
            .whenCondition { customQsMarginsEnabled }
            .forPackageName(SYSTEMUI_PACKAGE)
            .addResource("qs_header_system_icons_area_height") { getQqsMargin() }
            .addResource("qqs_layout_margin_top") { getQqsMargin() }
            .addResource("qs_header_row_min_height") { getQqsMargin() }
            .addResource("qs_panel_padding_top") { getQsMargin() }
            .addResource("qs_panel_padding_top_combined_headers") { getQsMargin() }
            .addResource("qs_header_height") { getQsMargin() }
            .forPackageName(FRAMEWORK_PACKAGE)
            .addResource("quick_qs_offset_height") { getQqsMargin() }
            .addResource("quick_qs_total_height") { getQsMargin() }
            .apply()

        val quickStatusBarHeader = findClass("$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader")

        quickStatusBarHeader
            .hookMethod("updateResources")
            .runAfter { param ->
                if (!customQsMarginsEnabled) return@runAfter

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    try {
                        val res = mContext.resources

                        val qqsLP = param.thisObject
                            .getField("mHeaderQsPanel")
                            .callMethod("getLayoutParams") as MarginLayoutParams
                        qqsLP.topMargin = mContext.resources.getDimensionPixelSize(
                            res.getIdentifier(
                                "qqs_layout_margin_top",
                                "dimen",
                                mContext.packageName
                            )
                        )

                        param.thisObject
                            .getField("mHeaderQsPanel")
                            .callMethod(
                                "setLayoutParams",
                                qqsLP
                            )
                    } catch (throwable: Throwable) {
                        log(this@QuickSettings, throwable)
                    }
                }
            }
    }

    private fun fixQsTileAndLabelColorA14() {
        if (!isAtLeastAndroid14) return

        val qsTileViewImplClass = findClass("$SYSTEMUI_PACKAGE.qs.tileimpl.QSTileViewImpl")!!

        if (fixQsTileColor && qsTileViewImplClass.isMethodAvailable("setStateLayer")) {
            qsTileViewImplClass
                .hookMethod("setStateLayer")
                .replace { param ->
                    val currentState = param.thisObject.getField("currentState") as Int

                    val ld: LayerDrawable = (param.thisObject.getField(
                        "backgroundDrawable"
                    ) as LayerDrawable).mutate() as LayerDrawable

                    when (currentState) {
                        Tile.STATE_ACTIVE -> ld.setTint(Color.WHITE)

                        Tile.STATE_INACTIVE -> ld.setTint(Color.TRANSPARENT)

                        else -> ld.setTint(Color.TRANSPARENT)
                    }
                }
        }

        val removeQsTileTint: XC_MethodHook = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!fixQsTileColor) return

                try {
                    param.thisObject.setField(
                        "colorActive",
                        Color.WHITE
                    )
                    param.thisObject.setField(
                        "colorInactive",
                        Color.TRANSPARENT
                    )
                    param.thisObject.setField(
                        "colorUnavailable",
                        Color.TRANSPARENT
                    )
                } catch (throwable: Throwable) {
                    log(this@QuickSettings, throwable)
                }
            }
        }

        qsTileViewImplClass
            .hookConstructor()
            .run(removeQsTileTint)

        qsTileViewImplClass
            .hookMethod("updateResources")
            .suppressError()
            .run(removeQsTileTint)

        qsTileViewImplClass
            .hookConstructor()
            .runAfter { param ->
                if (!customQsTextColor) return@runAfter

                initQsAccentColor()

                @ColorInt val color: Int = qsIconLabelColor
                @ColorInt val colorAlpha =
                    color and 0xFFFFFF or (Math.round(Color.alpha(color) * 0.8f) shl 24)

                param.thisObject.setField("colorLabelActive", color)
                param.thisObject.setField("colorSecondaryLabelActive", colorAlpha)
            }

        qsTileViewImplClass
            .hookMethod("getLabelColorForState")
            .runBefore { param ->
                if (isQsIconLabelStateActive(param, 0)) {
                    param.result = qsIconLabelColor
                }
            }

        qsTileViewImplClass
            .hookMethod("getSecondaryLabelColorForState")
            .runBefore { param ->
                if (isQsIconLabelStateActive(param, 0)) {
                    @ColorInt val color: Int = qsIconLabelColor
                    @ColorInt val colorAlpha =
                        color and 0xFFFFFF or (Math.round(Color.alpha(color) * 0.8f) shl 24)
                    param.result = colorAlpha
                }
            }

        val qsIconViewImplClass = findClass("$SYSTEMUI_PACKAGE.qs.tileimpl.QSIconViewImpl")

        qsIconViewImplClass
            .hookMethod("getIconColorForState", "getColor")
            .runBefore { param ->
                val stateIndex = if (param.args.size > 1) 1 else 0

                if (isQsIconLabelStateActive(param, stateIndex)) {
                    param.result = qsIconLabelColor
                }
            }

        qsIconViewImplClass
            .hookMethod("updateIcon")
            .runAfter { param ->
                val stateIndex = if (param.args.size > 1) 1 else 0

                if (isQsIconLabelStateActive(param, stateIndex)) {
                    val mIcon = param.args[0] as ImageView
                    mIcon.imageTintList = ColorStateList.valueOf(qsIconLabelColor)
                }
            }

        val qsContainerImplClass = findClass("$SYSTEMUI_PACKAGE.qs.QSContainerImpl")

        qsContainerImplClass
            .hookMethod("updateResources")
            .suppressError()
            .runAfter { param ->
                if (!customQsTextColor) return@runAfter

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
                        pmButton.imageTintList = ColorStateList.valueOf(color)
                    }
                } catch (ignored: Throwable) {
                }
            }

        // Compose implementation of QS Footer actions
        val footerActionsButtonViewModelClass =
            findClass("$SYSTEMUI_PACKAGE.qs.footer.ui.viewmodel.FooterActionsButtonViewModel")

        footerActionsButtonViewModelClass
            .hookConstructor()
            .runBefore { param ->
                if (!customQsTextColor) return@runBefore

                if (mContext.resources.getResourceName((param.args[0] as Int))
                        .split("/".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[1] == "pm_lite"
                ) {
                    param.args[2] = qsIconLabelColor
                }
            }

        // Auto brightness icon color
        val brightnessControllerClass =
            findClass("$SYSTEMUI_PACKAGE.settings.brightness.BrightnessController")
        val brightnessMirrorControllerClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.policy.BrightnessMirrorController")
        val brightnessSliderControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.settings.brightness.BrightnessSliderController",
            suppressError = true
        )

        brightnessControllerClass
            .hookConstructor()
            .runAfter { initQsAccentColor() }

        brightnessControllerClass
            .hookMethod("updateIcon")
            .suppressError()
            .runAfter { param ->
                if (!customQsTextColor) return@runAfter

                @ColorInt val color: Int = qsIconLabelColor
                try {
                    (param.thisObject.getField("mIcon") as ImageView).imageTintList =
                        ColorStateList.valueOf(color)
                } catch (throwable: Throwable) {
                    log(this@QuickSettings, throwable)
                }
            }

        brightnessSliderControllerClass
            .hookConstructor()
            .runAfter { param ->
                if (!customQsTextColor) return@runAfter

                initQsAccentColor()

                @ColorInt val color: Int = qsIconLabelColor

                try {
                    (param.thisObject.getField("mIcon") as ImageView).imageTintList =
                        ColorStateList.valueOf(color)
                } catch (throwable: Throwable) {
                    try {
                        (param.thisObject.getField("mIconView") as ImageView).imageTintList =
                            ColorStateList.valueOf(color)
                    } catch (ignored: Throwable) {
                    }
                }
            }

        brightnessMirrorControllerClass
            .hookMethod("updateIcon")
            .suppressError()
            .runAfter { param ->
                if (!customQsTextColor) return@runAfter

                @ColorInt val color: Int = qsIconLabelColor

                try {
                    (param.thisObject.getField("mIcon") as ImageView).imageTintList =
                        ColorStateList.valueOf(color)
                } catch (throwable: Throwable) {
                    log(this@QuickSettings, throwable)
                }
            }
    }

    private fun fixNotificationColorA14() {
        if (!isAtLeastAndroid14) return

        val activatableNotificationViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.row.ActivatableNotificationView")
        val notificationBackgroundViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.row.NotificationBackgroundView")
        val footerViewClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.notification.footer.ui.view.FooterView",
            "$SYSTEMUI_PACKAGE.statusbar.notification.row.FooterView"
        )

        val removeNotificationTint: XC_MethodHook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!fixNotificationColor) return

                val notificationBackgroundView = param.thisObject.getField(
                    "mBackgroundNormal"
                ) as View

                try {
                    param.thisObject.setField(
                        "mCurrentBackgroundTint",
                        param.args[0] as Int
                    )
                } catch (ignored: Throwable) {
                }

                try {
                    notificationBackgroundView.setField("mTintColor", 0)
                } catch (ignored: Throwable) {
                }
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                if (!fixNotificationColor) return

                val notificationBackgroundView = param.thisObject.getField(
                    "mBackgroundNormal"
                ) as View

                notificationBackgroundView.callMethodSilently("setColorFilter", 0)

                (notificationBackgroundView.getFieldSilently(
                        "mBackground"
                ) as? Drawable)?.colorFilter = null

                notificationBackgroundView.setFieldSilently("mTintColor", 0)

                Handler(Looper.getMainLooper()).post {
                    notificationBackgroundView.invalidate()
                }
            }
        }

        activatableNotificationViewClass
            .hookMethod("setBackgroundTintColor")
            .run(removeNotificationTint)

        activatableNotificationViewClass
            .hookMethod("updateBackgroundTint")
            .run(removeNotificationTint)

        activatableNotificationViewClass
            .hookMethod("calculateBgColor")
            .runBefore { param ->
                if (!fixNotificationColor) return@runBefore

                try {
                    param.result = param.thisObject.getField(
                        "mCurrentBackgroundTint"
                    )
                } catch (ignored: Throwable) {
                }
            }

        notificationBackgroundViewClass
            .hookMethodMatchPattern("setCustomBackground.*")
            .runBefore { param ->
                if (!fixNotificationColor) return@runBefore

                param.thisObject.setField("mTintColor", 0)
            }

        footerViewClass
            .hookMethodMatchPattern("updateColors.*")
            .runAfter { param ->
                if (!fixNotificationFooterButtonsColor) return@runAfter

                try {
                    val mManageButton = try {
                        param.thisObject.getField("mManageButton")
                    } catch (ignored: Throwable) {
                        param.thisObject.getField("mManageOrHistoryButton")
                    } as Button
                    val mClearAllButton = try {
                        param.thisObject.getField("mClearAllButton")
                    } catch (ignored: Throwable) {
                        param.thisObject.getField("mDismissButton")
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

    private fun manageQsElementVisibility() {
        val footerViewClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.notification.footer.ui.view.FooterView",
            "$SYSTEMUI_PACKAGE.statusbar.notification.row.FooterView"
        )

        footerViewClass
            .hookMethod("onFinishInflate")
            .runAfter { param ->
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

        val sectionHeaderViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.stack.SectionHeaderView")

        sectionHeaderViewClass
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                mSilentTextContainer = param.thisObject as ViewGroup

                triggerQsElementVisibility()
            }
    }

    private fun disableQsOnSecureLockScreen() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return

        val remoteInputQuickSettingsDisablerClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.policy.RemoteInputQuickSettingsDisabler")
        val phoneStatusBarPolicyClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.PhoneStatusBarPolicy")
        val scrimManagerClass = findClass(
            "$SYSTEMUI_PACKAGE.ambient.touch.scrim.ScrimManager",
            "$SYSTEMUI_PACKAGE.dreams.touch.scrim.ScrimManager"
        )

        val getKeyguardStateController = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                param.thisObject.getFieldSilently("mKeyguardStateController")?.let {
                    mKeyguardStateController = it
                }
            }
        }

        phoneStatusBarPolicyClass
            .hookConstructor()
            .run(getKeyguardStateController)

        scrimManagerClass
            .hookConstructor()
            .run(getKeyguardStateController)

        remoteInputQuickSettingsDisablerClass
            .hookMethod("adjustDisableFlags")
            .runBefore { param ->
                if (!hideQsOnLockscreen || mKeyguardStateController == null) return@runBefore

                val isUnlocked = try {
                    !(mKeyguardStateController.getField("mShowing") as Boolean) ||
                            mKeyguardStateController.getField("mCanDismissLockScreen") as Boolean
                } catch (ignored: Throwable) {
                    mKeyguardStateController.callMethod("isUnlocked") as Boolean
                }

                param.result = if (hideQsOnLockscreen && !isUnlocked) {
                    param.args[0] as Int or DISABLE2_QUICK_SETTINGS
                } else {
                    param.args[0]
                }
            }
    }

    private fun compactMediaPlayer() {
        val mediaViewControllerClass =
            findClass(
                "$SYSTEMUI_PACKAGE.media.controls.ui.controller.MediaViewController",
                "$SYSTEMUI_PACKAGE.media.controls.ui.MediaViewController"
            )

        mediaViewControllerClass
            .hookMethod("obtainViewState")
            .runBefore { param ->
                if (!compactMediaPlayerEnabled) return@runBefore

                val mediaHostState = param.args[0] ?: return@runBefore

                // For a14 and above
                mediaHostState.javaClass
                    .hookMethod("getExpansion")
                    .suppressError()
                    .runBefore runBefore2@{ param2 ->
                        if (!compactMediaPlayerEnabled) return@runBefore2

                        param2.result = 0f
                    }

                // For some a13 and below ROMs
                mediaHostState.javaClass
                    .hookConstructor()
                    .runAfter { param2 ->
                        if (!compactMediaPlayerEnabled) return@runAfter

                        param2.thisObject.setFieldSilently("expansion", 0f)
                    }
            }
    }

    private fun isQsIconLabelStateActive(param: MethodHookParam?, stateIndex: Int): Boolean {
        if (param?.args == null) return false

        if (!customQsTextColor) return false

        val isActiveState: Boolean = try {
            param.args[stateIndex].getField(
                "state"
            ) as Int == Tile.STATE_ACTIVE
        } catch (throwable: Throwable) {
            try {
                param.args[stateIndex] as Int == Tile.STATE_ACTIVE
            } catch (throwable1: Throwable) {
                try {
                    param.args[stateIndex] as Boolean
                } catch (throwable2: Throwable) {
                    log(this@QuickSettings, throwable2)
                    false
                }
            }
        }

        return isActiveState
    }

    @get:ColorInt
    private val qsIconLabelColor: Int
        get() {
            return when (selectedQsTextColor) {
                0 -> Color.WHITE
                1 -> qsTextAccentColor
                2 -> if (mContext.isNightMode) Color.WHITE else Color.BLACK
                3 -> if (mContext.isNightMode) Color.BLACK else Color.WHITE
                else -> Color.WHITE
            }
        }

    private fun initQsAccentColor() {
        qsTextAccentColor = mContext.resources.getColor(
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

    private fun triggerQsElementVisibility() {
        if (mFooterButtonsContainer != null) {
            if (mFooterButtonsOnDrawListener == null) {
                mFooterButtonsOnDrawListener =
                    OnDrawListener { mFooterButtonsContainer!!.visibility = View.INVISIBLE }
            }

            try {
                if (hideFooterButtons) {
                    mFooterButtonsContainer!!.visibility = View.INVISIBLE
                    mFooterButtonsContainer!!.viewTreeObserver
                        .addOnDrawListener(mFooterButtonsOnDrawListener)
                } else {
                    mFooterButtonsContainer!!.viewTreeObserver
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
                    mSilentTextContainer!!.viewTreeObserver
                        .addOnDrawListener(mSilentTextOnDrawListener)
                } else {
                    mSilentTextContainer!!.viewTreeObserver
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

        tile.apply {
            setPadding(padding, padding, padding, padding)
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL
        }

        if (!isHideLabelActive) {
            try {
                ((tile.getField(
                    "labelContainer"
                ) as LinearLayout).layoutParams as MarginLayoutParams).marginStart = 0

                ((tile.getField(
                    "labelContainer"
                ) as LinearLayout).layoutParams as MarginLayoutParams).topMargin = mContext.toPx(2)
            } catch (throwable: Throwable) {
                log(this@QuickSettings, throwable)
            }
        }

        if (param != null) {
            (param.getField(
                "label"
            ) as TextView).textAlignment = TEXT_ALIGNMENT_CENTER

            (param.getField(
                "secondaryLabel"
            ) as TextView).textAlignment = TEXT_ALIGNMENT_CENTER
        }
    }

    companion object {
        /*
         * Source: frameworks/base/core/java/android/app/StatusBarManager.java
         */
        private const val DISABLE2_QUICK_SETTINGS = 1
        var isPixelVariant = getIsPixelVariant()

        private fun getIsPixelVariant(): Boolean {
            for (i in 0..25) {
                if (Xprefs.getBoolean("IconifyComponentQSSP$i.overlay")) {
                    return true
                }
            }
            return false
        }
    }
}