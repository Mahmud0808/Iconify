package com.drdisagree.iconify.xposed.modules.quicksettings.themes


import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_QS_TEXT_COLOR
import com.drdisagree.iconify.data.common.Preferences.DUALTONE_QSPANEL
import com.drdisagree.iconify.data.common.Preferences.LIGHT_QSPANEL
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.SettingsLibUtils.Companion.getColorAttr
import com.drdisagree.iconify.xposed.modules.extras.utils.SettingsLibUtils.Companion.getColorAttrDefaultColor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.findMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethodMatchPattern
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.utils.OemUtils
import com.drdisagree.iconify.xposed.utils.SystemUtils
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.util.Arrays
import java.util.function.Consumer

@SuppressLint("DiscouragedApi")
class QSLightThemeA15(context: Context) : ModPack(context) {

    private var mBehindColors: Any? = null
    private var isDark: Boolean
    private var colorActive: Int? = null
    private var colorInactive: Int? = null
    private var unlockedScrimState: Any? = null
    private var customQsTextColor = false
    private var mScrimBehindTint = Color.BLACK
    private var shadeCarrierGroupController: Any? = null
    private var mClockViewQSHeader: Any? = null
    private val modernShadeCarrierGroupMobileViews = ArrayList<Any>()
    private val qsLightThemeOverlay = "IconifyComponentQSLT.overlay"
    private val qsDualToneOverlay = "IconifyComponentQSDT.overlay"
    private var mPrimaryLabelActiveColor: ColorStateList? = null
    private var mSecondaryLabelActiveColor: ColorStateList? = null
    private var mPrimaryLabelInactiveColor: ColorStateList? = null
    private var mSecondaryLabelInactiveColor: ColorStateList? = null

    init {
        isDark = SystemUtils.isDarkMode
    }

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            lightQSHeaderEnabled = getBoolean(LIGHT_QSPANEL, false)
            dualToneQSEnabled = lightQSHeaderEnabled && getBoolean(DUALTONE_QSPANEL, false)
            customQsTextColor = getBoolean(CUSTOM_QS_TEXT_COLOR, false)
        }

        if (key.isNotEmpty()) {
            key[0].let {
                if (it == LIGHT_QSPANEL || it == DUALTONE_QSPANEL) {
                    applyOverlays(true)
                }
            }
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val qsTileViewImplClass = findClass("$SYSTEMUI_PACKAGE.qs.tileimpl.QSTileViewImpl")
        val scrimControllerClass = findClass("$SYSTEMUI_PACKAGE.statusbar.phone.ScrimController")
        val gradientColorsClass =
            findClass("com.android.internal.colorextraction.ColorExtractor\$GradientColors")!!
        val qsPanelControllerClass = findClass("$SYSTEMUI_PACKAGE.qs.QSPanelController")
        val scrimStateEnum = findClass("$SYSTEMUI_PACKAGE.statusbar.phone.ScrimState")!!
        val qsIconViewImplClass = findClass("$SYSTEMUI_PACKAGE.qs.tileimpl.QSIconViewImpl")
        val centralSurfacesImplClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.CentralSurfacesImpl",
            suppressError = true
        )
        val qsCustomizerClass = findClass("$SYSTEMUI_PACKAGE.qs.customize.QSCustomizer")
        val qsContainerImplClass = findClass("$SYSTEMUI_PACKAGE.qs.QSContainerImpl")
        val shadeCarrierClass = findClass("$SYSTEMUI_PACKAGE.shade.carrier.ShadeCarrier")
        val batteryStatusChipClass = findClass("$SYSTEMUI_PACKAGE.statusbar.BatteryStatusChip")
        val qsFooterViewClass = findClass("$SYSTEMUI_PACKAGE.qs.QSFooterView")
        val brightnessControllerClass =
            findClass("$SYSTEMUI_PACKAGE.settings.brightness.BrightnessController")
        val brightnessMirrorControllerClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.policy.BrightnessMirrorController")
        val brightnessSliderControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.settings.brightness.BrightnessSliderController",
            suppressError = true
        )
        val quickStatusBarHeaderClass = findClass("$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader")
        val clockClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.policy.Clock",
            suppressError = true
        )
        val themeColorKtClass = findClass(
            "com.android.compose.theme.ColorKt",
            suppressError = true
        )
        val expandableControllerImplClass =
            findClass("com.android.compose.animation.ExpandableControllerImpl")
        val footerActionsViewModelClass =
            findClass("$SYSTEMUI_PACKAGE.qs.footer.ui.viewmodel.FooterActionsViewModel")
        val shadeHeaderControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.shade.ShadeHeaderController",
            "$SYSTEMUI_PACKAGE.shade.LargeScreenShadeHeaderController",
        )

        try { // A15 early implementation of QS Footer actions - doesn't seem to be leading to final A15 release
            val footerActionsViewBinderClass = findClass(
                "$SYSTEMUI_PACKAGE.qs.footer.ui.binder.FooterActionsViewBinder",
                throwException = true
            )
            val textButtonViewHolderClass = findClass(
                "$SYSTEMUI_PACKAGE.qs.footer.ui.binder.TextButtonViewHolder",
                throwException = true
            )
            val numberButtonViewHolderClass = findClass(
                "$SYSTEMUI_PACKAGE.qs.footer.ui.binder.NumberButtonViewHolder",
                throwException = true
            )

            // QS security footer count circle
            numberButtonViewHolderClass
                .hookConstructor()
                .runAfter { param ->
                    if (!lightQSHeaderEnabled || isDark) return@runAfter

                    (param.thisObject.getField("newDot") as ImageView)
                        .setColorFilter(Color.BLACK)

                    (param.thisObject.getField("number") as TextView)
                        .setTextColor(Color.BLACK)
                }

            // QS security footer
            textButtonViewHolderClass
                .hookConstructor()
                .runAfter { param ->
                    if (!lightQSHeaderEnabled || isDark) return@runAfter

                    (param.thisObject.getField("chevron") as ImageView)
                        .setColorFilter(Color.BLACK)

                    (param.thisObject.getField("icon") as ImageView)
                        .setColorFilter(Color.BLACK)

                    (param.thisObject.getField("newDot") as ImageView)
                        .setColorFilter(Color.BLACK)

                    (param.thisObject.getField("text") as TextView)
                        .setTextColor(Color.BLACK)
                }

            footerActionsViewBinderClass
                .hookMethod("bind")
                .suppressError()
                .runAfter { param ->
                    if (!lightQSHeaderEnabled || isDark) return@runAfter

                    val view = param.args[0] as LinearLayout
                    view.setBackgroundColor(Color.TRANSPARENT)
                    view.elevation = 0f
                }
        } catch (ignored: Throwable) {
        }

        // Background color of android 14's charging chip. Fix for light QS theme situation
        val batteryStatusChipColorHook: XC_MethodHook = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!lightQSHeaderEnabled || isDark) return

                (param.thisObject.getField("roundedContainer") as LinearLayout)
                    .background.setTint(colorInactive!!)

                val colorPrimary: Int =
                    getColorAttrDefaultColor(mContext, android.R.attr.textColorPrimaryInverse)
                val textColorSecondary: Int =
                    getColorAttrDefaultColor(mContext, android.R.attr.textColorSecondaryInverse)

                param.thisObject
                    .getField("batteryMeterView")
                    .callMethod(
                        "updateColors",
                        colorPrimary,
                        textColorSecondary,
                        colorPrimary
                    )
            }
        }

        batteryStatusChipClass
            .hookConstructor()
            .run(batteryStatusChipColorHook)

        batteryStatusChipClass
            .hookMethod("onConfigurationChanged")
            .run(batteryStatusChipColorHook)

        unlockedScrimState = scrimStateEnum.enumConstants?.let {
            Arrays.stream(it)
                .filter { c: Any -> c.toString() == "UNLOCKED" }
                .findFirst().get()
        }

        unlockedScrimState?.javaClass
            ?.hookMethod("prepare")
            ?.runAfter {
                if (!lightQSHeaderEnabled) return@runAfter

                unlockedScrimState.setField("mBehindTint", Color.TRANSPARENT)
            }

        qsPanelControllerClass
            .hookConstructor()
            .runAfter { calculateColors() }

        shadeHeaderControllerClass
            .hookMethod("onInit")
            .runAfter { param ->
                try {
                    val mView = param.thisObject.getField("mView") as View
                    val iconManager = param.thisObject.getField("iconManager")
                    val batteryIcon = param.thisObject.getField("batteryIcon")
                    val configurationControllerListener = param.thisObject.getField(
                        "configurationControllerListener"
                    )

                    configurationControllerListener.javaClass
                        .hookMethod(
                            "onConfigChanged",
                            "onDensityOrFontScaleChanged",
                            "onUiModeChanged",
                            "onThemeChanged"
                        )
                        .runAfter { setHeaderComponentsColor(mView, iconManager, batteryIcon) }

                    setHeaderComponentsColor(mView, iconManager, batteryIcon)
                } catch (throwable: Throwable) {
                    log(this@QSLightThemeA15, throwable)
                }
            }

        // A14 ap11 onwards - modern implementation of mobile icons
        val shadeCarrierGroupControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.shade.carrier.ShadeCarrierGroupController",
            suppressError = true
        )
        val mobileIconBinderClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.pipeline.mobile.ui.binder.MobileIconBinder",
            suppressError = true
        )

        shadeCarrierGroupControllerClass
            .hookConstructor()
            .runAfter { param -> shadeCarrierGroupController = param.thisObject }

        mobileIconBinderClass
            .hookMethod("bind")
            .runAfter { param ->
                if (param.args[1].javaClass.name
                        .contains("ShadeCarrierGroupMobileIconViewModel")
                ) {
                    modernShadeCarrierGroupMobileViews.add(param.result)

                    if (!isDark && lightQSHeaderEnabled) {
                        val textColor = getColorAttrDefaultColor(
                            android.R.attr.textColorPrimary,
                            mContext
                        )
                        setMobileIconTint(param.result, textColor)
                    }
                }
            }

        qsContainerImplClass
            .hookMethod("updateResources")
            .suppressError()
            .runAfter { param ->
                if (!lightQSHeaderEnabled || isDark) return@runAfter

                try {
                    val view = (param.thisObject as ViewGroup).findViewById<ViewGroup>(
                        mContext.resources.getIdentifier(
                            "qs_footer_actions",
                            "id",
                            mContext.packageName
                        )
                    ).also {
                        it.background?.setTint(Color.TRANSPARENT)
                        it.elevation = 0f
                    }

                    // Settings button
                    view.findViewById<View>(
                        mContext.resources.getIdentifier(
                            "settings_button_container",
                            "id",
                            mContext.packageName
                        )
                    ).findViewById<ImageView>(
                        mContext.resources.getIdentifier(
                            "icon",
                            "id",
                            mContext.packageName
                        )
                    ).imageTintList = ColorStateList.valueOf(Color.BLACK)

                    // Power menu button
                    try {
                        view.findViewById<ImageView?>(
                            mContext.resources.getIdentifier(
                                "pm_lite",
                                "id",
                                mContext.packageName
                            )
                        )
                    } catch (ignored: ClassCastException) {
                        view.findViewById<ViewGroup?>(
                            mContext.resources.getIdentifier(
                                "pm_lite",
                                "id",
                                mContext.packageName
                            )
                        )
                    }?.apply {
                        if (this is ImageView) {
                            imageTintList = ColorStateList.valueOf(Color.WHITE)
                        } else if (this is ViewGroup) {
                            (getChildAt(0) as ImageView).setColorFilter(
                                Color.WHITE,
                                PorterDuff.Mode.SRC_IN
                            )
                        }
                    }
                } catch (ignored: Throwable) {
                    // it will fail on compose implementation
                }
            }

        // QS Customize panel
        qsCustomizerClass
            .hookConstructor()
            .runAfter { param ->
                if (!lightQSHeaderEnabled || isDark) return@runAfter

                val mainView = param.thisObject as ViewGroup

                for (i in 0 until mainView.childCount) {
                    mainView.getChildAt(i).setBackgroundColor(mScrimBehindTint)
                }
            }

        // Mobile signal icons - this is the legacy model. new model uses viewmodels
        shadeCarrierClass
            .hookMethod("updateState")
            .runAfter { param ->
                if (!lightQSHeaderEnabled || isDark) return@runAfter

                (param.thisObject.getField("mMobileSignal") as ImageView).imageTintList =
                    ColorStateList.valueOf(Color.BLACK)
            }

        qsFooterViewClass
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                if (!lightQSHeaderEnabled || isDark) return@runAfter

                try {
                    (param.thisObject.getField("mBuildText") as TextView)
                        .setTextColor(Color.BLACK)
                } catch (ignored: Throwable) {
                }

                try {
                    (param.thisObject.getField("mEditButton") as ImageView)
                        .setColorFilter(Color.BLACK)
                } catch (ignored: Throwable) {
                }

                try {
                    param.thisObject.getField("mPageIndicator").setField(
                        "mTint",
                        ColorStateList.valueOf(Color.BLACK)
                    )
                } catch (ignored: Throwable) {
                }
            }

        // Auto Brightness Icon Color
        brightnessControllerClass
            .hookMethod("updateIcon")
            .suppressError()
            .runAfter { param ->
                if (!lightQSHeaderEnabled || isDark) return@runAfter

                try {
                    (param.thisObject.getField("mIcon") as ImageView).imageTintList =
                        ColorStateList.valueOf(Color.BLACK)
                } catch (throwable: Throwable) {
                    log(this@QSLightThemeA15, throwable)
                }
            }

        brightnessSliderControllerClass
            .hookConstructor()
            .runAfter { param ->
                if (!lightQSHeaderEnabled || isDark) return@runAfter

                try {
                    (param.thisObject.getField("mIcon") as ImageView).imageTintList =
                        ColorStateList.valueOf(Color.BLACK)
                } catch (throwable: Throwable) {
                    try {
                        (param.thisObject.getField(
                            "mIconView"
                        ) as ImageView).imageTintList =
                            ColorStateList.valueOf(Color.BLACK)
                    } catch (ignored: Throwable) {
                    }
                }
            }

        brightnessMirrorControllerClass
            .hookMethod("updateIcon")
            .suppressError()
            .runAfter { param ->
                if (!lightQSHeaderEnabled || isDark) return@runAfter

                try {
                    val iconColor = if ((param.args?.get(0) ?: false) as Boolean) {
                        Color.WHITE
                    } else {
                        Color.BLACK
                    }
                    val mIcon = param.thisObject.getField("mIcon") as ImageView

                    mIcon.imageTintList = ColorStateList.valueOf(iconColor)
                } catch (throwable: Throwable) {
                    log(this@QSLightThemeA15, throwable)
                }
            }

        // White QS Clock bug
        quickStatusBarHeaderClass
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                try {
                    mClockViewQSHeader = param.thisObject.getField("mClockView")
                } catch (ignored: Throwable) {
                }

                if (!isDark && lightQSHeaderEnabled && mClockViewQSHeader != null) {
                    try {
                        (mClockViewQSHeader as TextView).setTextColor(Color.WHITE)
                    } catch (throwable: Throwable) {
                        log(this@QSLightThemeA15, throwable)
                    }
                }
            }

        // White QS Clock bug
        clockClass
            .hookMethod("onColorsChanged")
            .suppressError()
            .runAfter {
                if (!lightQSHeaderEnabled || isDark) return@runAfter

                (mClockViewQSHeader as? TextView)?.setTextColor(Color.WHITE)
            }

        centralSurfacesImplClass
            .hookConstructor()
            .runAfter { applyOverlays(true) }

        centralSurfacesImplClass
            .hookMethod("updateTheme")
            .runAfter { applyOverlays(false) }

        // Composition makes it almost impossible to control icon color via reflection
        qsIconViewImplClass
            .hookConstructor()
            .runAfter { param ->
                if (!lightQSHeaderEnabled || isDark) return@runAfter

                val originalIcon = param.thisObject.getField("mIcon") as ImageView
                val replacementIcon = TintControlledImageView(originalIcon.context).apply {
                    setImageDrawable(originalIcon.drawable)
                    id = mContext.resources.getIdentifier(
                        "icon",
                        "id",
                        mContext.packageName
                    )
                }

                param.thisObject.setField("mIcon", replacementIcon)

                (param.thisObject as ViewGroup).apply {
                    val index = indexOfChild(originalIcon)
                    removeView(originalIcon)
                    addView(replacementIcon, index)
                }
            }

        qsTileViewImplClass
            .hookConstructor()
            .runAfter { param ->
                if (!lightQSHeaderEnabled || isDark) return@runAfter

                try {
                    mPrimaryLabelActiveColor = ColorStateList.valueOf(
                        param.thisObject.getField("colorLabelActive") as Int
                    )
                    mSecondaryLabelActiveColor = ColorStateList.valueOf(
                        param.thisObject.getField("colorSecondaryLabelActive") as Int
                    )
                    mPrimaryLabelInactiveColor = ColorStateList.valueOf(
                        param.thisObject.getField("colorLabelInactive") as Int
                    )
                    mSecondaryLabelInactiveColor = ColorStateList.valueOf(
                        param.thisObject.getField("colorSecondaryLabelInactive") as Int
                    )

                    if (!customQsTextColor) {
                        param.thisObject.setField("colorLabelActive", Color.WHITE)
                        param.thisObject.setField("colorSecondaryLabelActive", -0x7f000001)
                    }

                    param.thisObject.setField("colorLabelInactive", Color.BLACK)
                    param.thisObject.setField("colorSecondaryLabelInactive", -0x80000000)

                    val sideView = param.thisObject.getField("sideView") as ViewGroup

                    val customDrawable = sideView.findViewById<ImageView>(
                        mContext.resources.getIdentifier(
                            "customDrawable",
                            "id",
                            mContext.packageName
                        )
                    )
                    customDrawable.imageTintList = ColorStateList.valueOf(Color.WHITE)

                    val chevron = sideView.findViewById<ImageView>(
                        mContext.resources.getIdentifier(
                            "chevron",
                            "id",
                            mContext.packageName
                        )
                    )
                    chevron.imageTintList = ColorStateList.valueOf(Color.WHITE)
                } catch (throwable: Throwable) {
                    log(this@QSLightThemeA15, throwable)
                }
            }

        qsIconViewImplClass
            .hookMethod("getIconColorForState", "getColor")
            .runBefore { param ->
                if (!lightQSHeaderEnabled || isDark) return@runBefore

                val (isDisabledState: Boolean, isActiveState: Boolean) = Utils.getTileState(param)

                if (isDisabledState) {
                    param.result = -0x80000000
                } else {
                    if (isActiveState && !customQsTextColor) {
                        param.result = Color.WHITE
                    } else if (!isActiveState) {
                        param.result = Color.BLACK
                    }
                }
            }

        qsIconViewImplClass
            .hookMethod("updateIcon")
            .runAfter { param ->
                if (!lightQSHeaderEnabled || isDark) return@runAfter

                val (isDisabledState: Boolean, isActiveState: Boolean) = Utils.getTileState(param)

                val mIcon = param.args[0] as ImageView
                if (isDisabledState) {
                    param.result = -0x80000000
                } else {
                    if (isActiveState && !customQsTextColor) {
                        mIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)
                    } else if (!isActiveState) {
                        mIcon.imageTintList = ColorStateList.valueOf(Color.BLACK)
                    }
                }
            }

        qsContainerImplClass
            .hookMethod("updateResources")
            .suppressError()
            .runAfter { param ->
                if (!lightQSHeaderEnabled || isDark) return@runAfter

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

                        pmButton.imageTintList = ColorStateList.valueOf(Color.WHITE)
                    }
                } catch (ignored: Throwable) {
                }
            }

        // Compose implementation of QS Footer actions
        val graphicsColorKtClass = findClass(
            "androidx.compose.ui.graphics.ColorKt",
            suppressError = true
        )

        expandableControllerImplClass
            .hookConstructor()
            .runBefore { param ->
                if (!lightQSHeaderEnabled || isDark) return@runBefore

                param.args[1] = callStaticMethod(graphicsColorKtClass, "Color", Color.BLACK)
            }

        val colorAttrParams = themeColorKtClass?.let {
            it.findMethod("colorAttr")?.parameters
        } ?: emptyArray()
        val resIdIndex = colorAttrParams.indexOfFirst {
            it.type == Int::class.javaPrimitiveType
        }.takeIf { it != -1 } ?: 0

        themeColorKtClass
            .hookMethod("colorAttr")
            .runBefore { param ->
                if (!lightQSHeaderEnabled || isDark) return@runBefore

                val code = param.args[resIdIndex] as Int
                var result = 0

                if (code == PM_LITE_BACKGROUND_CODE) {
                    result = colorActive!!
                } else {
                    try {
                        when (mContext.resources.getResourceName(code).split("/")[1]) {
                            "underSurface", "onShadeActive", "shadeInactive" -> {
                                result = colorInactive!! // button backgrounds
                            }

                            "onShadeInactiveVariant" -> {
                                result = Color.BLACK // "number button" text
                            }
                        }
                    } catch (ignored: Throwable) {
                    }
                }

                if (result != 0) {
                    param.result = callStaticMethod(graphicsColorKtClass, "Color", result)
                }
            }

        footerActionsViewModelClass
            .hookConstructor()
            .runAfter { param ->
                if (!lightQSHeaderEnabled) return@runAfter

                if (!isDark) {
                    // Power button
                    val power = param.thisObject.getField("power")
                    power.setField("iconTint", Color.WHITE)
                    power.setField("backgroundColor", PM_LITE_BACKGROUND_CODE)

                    // Settings button
                    val settings = param.thisObject.getField("settings")
                    settings.setField("iconTint", Color.BLACK)
                }

                // We must use the classes defined in the apk. Using our own will fail.
                val stateFlowImplClass = findClass("kotlinx.coroutines.flow.StateFlowImpl")!!
                val readonlyStateFlowClass =
                    findClass("kotlinx.coroutines.flow.ReadonlyStateFlow")!!

                try {
                    val zeroAlphaFlow = stateFlowImplClass
                        .getConstructor(Any::class.java)
                        .newInstance(0f)

                    val readonlyStateFlowInstance = try {
                        readonlyStateFlowClass.constructors[0].newInstance(zeroAlphaFlow)
                    } catch (ignored: Throwable) {
                        readonlyStateFlowClass.constructors[0].newInstance(zeroAlphaFlow, null)
                    }

                    param.thisObject.setField(
                        "backgroundAlpha",
                        readonlyStateFlowInstance
                    )
                } catch (throwable: Throwable) {
                    log(this@QSLightThemeA15, throwable)
                }
            }

        try {
            mBehindColors = gradientColorsClass.getDeclaredConstructor().newInstance()
        } catch (throwable: Throwable) {
            log(this@QSLightThemeA15, throwable)
        }

        scrimControllerClass
            .hookMethod("updateScrims")
            .runAfter { param ->
                if (!dualToneQSEnabled) return@runAfter

                try {
                    val mScrimBehind = param.thisObject.getField("mScrimBehind")
                    val mBlankScreen = param.thisObject.getField("mBlankScreen") as Boolean
                    val alpha = mScrimBehind.getField("mViewAlpha") as Float
                    val animateBehindScrim = alpha != 0f && !mBlankScreen

                    mScrimBehind.callMethod(
                        "setColors",
                        mBehindColors,
                        animateBehindScrim
                    )
                } catch (throwable: Throwable) {
                    log(this@QSLightThemeA15, throwable)
                }
            }

        scrimControllerClass
            .hookMethod("updateThemeColors")
            .runAfter {
                calculateColors()

                if (!dualToneQSEnabled) return@runAfter

                try {
                    val states: ColorStateList = getColorAttr(
                        mContext.resources.getIdentifier(
                            "android:attr/colorSurfaceHeader",
                            "attr",
                            mContext.packageName
                        ), mContext
                    )
                    val surfaceBackground = states.defaultColor
                    val accentColor = getColorAttr(
                        mContext.resources.getIdentifier(
                            "colorAccent",
                            "attr",
                            "android"
                        ), mContext
                    ).defaultColor

                    mBehindColors.callMethod("setMainColor", surfaceBackground)
                    mBehindColors.callMethod("setSecondaryColor", accentColor)

                    val contrast = ColorUtils.calculateContrast(
                        mBehindColors.callMethod(
                            "getMainColor"
                        ) as Int, Color.WHITE
                    )

                    mBehindColors.callMethod("setSupportsDarkText", contrast > 4.5)
                } catch (throwable: Throwable) {
                    log(this@QSLightThemeA15, throwable)
                }
            }

        scrimControllerClass
            .hookMethodMatchPattern("applyState.*")
            .runAfter { param ->
                if (!lightQSHeaderEnabled) return@runAfter

                try {
                    val mClipsQsScrim = param.thisObject.getField("mClipsQsScrim") as Boolean

                    if (mClipsQsScrim) {
                        param.thisObject.setField("mBehindTint", Color.TRANSPARENT)
                    }
                } catch (throwable: Throwable) {
                    log(this@QSLightThemeA15, throwable)
                }
            }

        val constants: Array<out Any> = scrimStateEnum.enumConstants ?: arrayOf()
        constants.forEach { constant ->
            when (constant.toString()) {
                "KEYGUARD" -> constant.javaClass
                    .hookMethod("prepare")
                    .runAfter { param ->
                        if (!lightQSHeaderEnabled) return@runAfter

                        val mClipQsScrim = param.thisObject.getField(
                            "mClipQsScrim"
                        ) as Boolean

                        if (mClipQsScrim) {
                            val mScrimBehind = param.thisObject.getField(
                                "mScrimBehind"
                            )
                            val mTintColor = mScrimBehind.getField("mTintColor") as Int

                            if (mTintColor != Color.TRANSPARENT) {
                                mScrimBehind.setField(
                                    "mTintColor",
                                    Color.TRANSPARENT
                                )

                                mScrimBehind.callMethod(
                                    "updateColorWithTint",
                                    false
                                )
                            }

                            mScrimBehind.callMethod("setViewAlpha", 1f)
                        }
                    }

                "BOUNCER" -> constant.javaClass
                    .hookMethod("prepare")
                    .runAfter { param ->
                        if (!lightQSHeaderEnabled) return@runAfter

                        param.thisObject.setField("mBehindTint", Color.TRANSPARENT)
                    }

                "SHADE_LOCKED" -> {
                    constant.javaClass
                        .hookMethod("prepare")
                        .runAfter { param ->
                            if (!lightQSHeaderEnabled) return@runAfter

                            param.thisObject.setField(
                                "mBehindTint",
                                Color.TRANSPARENT
                            )

                            val mClipQsScrim = param.thisObject.getField(
                                "mClipQsScrim"
                            ) as Boolean
                            if (mClipQsScrim) {
                                val mScrimBehind = param.thisObject.getField(
                                    "mScrimBehind"
                                )
                                val mTintColor = mScrimBehind.getField("mTintColor") as Int

                                if (mTintColor != Color.TRANSPARENT) {
                                    mScrimBehind.setField(
                                        "mTintColor",
                                        Color.TRANSPARENT
                                    )

                                    mScrimBehind.callMethod(
                                        "updateColorWithTint",
                                        false
                                    )
                                }

                                mScrimBehind.callMethod(
                                    "setViewAlpha",
                                    1f
                                )
                            }
                        }

                    constant.javaClass
                        .hookMethod("getBehindTint")
                        .suppressError()
                        .runBefore { param ->
                            if (!lightQSHeaderEnabled) return@runBefore

                            param.result = Color.TRANSPARENT
                        }
                }

                "UNLOCKED" -> constant.javaClass
                    .hookMethod("prepare")
                    .runAfter { param ->
                        if (!lightQSHeaderEnabled) return@runAfter

                        param.thisObject.setField(
                            "mBehindTint",
                            Color.TRANSPARENT
                        )

                        val mScrimBehind = param.thisObject.getField("mScrimBehind")
                        val mTintColor = mScrimBehind.getField("mTintColor") as Int

                        if (mTintColor != Color.TRANSPARENT) {
                            mScrimBehind.setField(
                                "mTintColor",
                                Color.TRANSPARENT
                            )

                            mScrimBehind.callMethod(
                                "updateColorWithTint",
                                false
                            )
                        }

                        mScrimBehind.callMethod("setViewAlpha", 1f)
                    }
            }
        }
    }

    private fun applyOverlays(force: Boolean) {
        val isCurrentlyDark: Boolean = SystemUtils.isDarkMode
        if (isCurrentlyDark == isDark && !force) return

        isDark = isCurrentlyDark

        calculateColors()

        // Sony devices don't support QSLT/QSDT framework overlays due to
        // different QS resource structures. The Xposed hooks in calculateColors()
        // handle theming instead.
        if (OemUtils.isSony) return

        Utils.disableOverlays(qsLightThemeOverlay, qsDualToneOverlay)

        try {
            Thread.sleep(50)
        } catch (ignored: Throwable) {
        }

        if (lightQSHeaderEnabled) {
            if (!isCurrentlyDark) {
                Utils.enableOverlay(qsLightThemeOverlay)
            }
            if (dualToneQSEnabled) Utils.enableOverlay(qsDualToneOverlay)
        }
    }

    private fun calculateColors() {
        if (!lightQSHeaderEnabled) return

        try {
            if (unlockedScrimState != null) {
                unlockedScrimState.setField("mBehindTint", Color.TRANSPARENT)
            }

            colorActive = mContext.resources.getColor(
                mContext.resources.getIdentifier(
                    "android:color/system_accent1_600",
                    "color",
                    mContext.packageName
                ), mContext.theme
            )

            colorInactive = mContext.resources.getColor(
                mContext.resources.getIdentifier(
                    "android:color/system_neutral1_10",
                    "color",
                    mContext.packageName
                ), mContext.theme
            )

            mScrimBehindTint = if (isDark) {
                mContext.resources.getColor(
                    mContext.resources.getIdentifier(
                        "android:color/system_neutral1_1000",
                        "color",
                        mContext.packageName
                    ), mContext.theme
                )
            } else {
                mContext.resources.getColor(
                    mContext.resources.getIdentifier(
                        "android:color/system_neutral1_100",
                        "color",
                        mContext.packageName
                    ), mContext.theme
                )
            }
        } catch (throwable: Throwable) {
            log(this@QSLightThemeA15, throwable)
        }
    }

    private fun setHeaderComponentsColor(mView: View, iconManager: Any, batteryIcon: Any) {
        if (!lightQSHeaderEnabled) return

        val textColorPrimary: Int =
            getColorAttrDefaultColor(android.R.attr.textColorPrimary, mContext)
        val textColorSecondary: Int =
            getColorAttrDefaultColor(android.R.attr.textColorSecondary, mContext)

        try {
            (mView.findViewById<View>(
                mContext.resources.getIdentifier(
                    "clock",
                    "id",
                    mContext.packageName
                )
            ) as TextView).setTextColor(textColorPrimary)

            (mView.findViewById<View>(
                mContext.resources.getIdentifier(
                    "date",
                    "id",
                    mContext.packageName
                )
            ) as TextView).setTextColor(textColorPrimary)
        } catch (ignored: Throwable) {
        }

        try {
            try { // A14 ap11
                iconManager.callMethod("setTint", textColorPrimary, textColorPrimary)

                modernShadeCarrierGroupMobileViews.forEach(Consumer { view: Any ->
                    setMobileIconTint(
                        view,
                        textColorPrimary
                    )
                })

                setModernSignalTextColor(textColorPrimary)
            } catch (ignored: Throwable) { // A14 older
                iconManager.callMethod("setTint", textColorPrimary)
            }

            for (i in 1..3) {
                try {
                    (mView.findViewById<View>(
                        mContext.resources.getIdentifier(
                            "carrier$i",
                            "id",
                            mContext.packageName
                        )
                    ).getField("mCarrierText") as TextView).setTextColor(textColorPrimary)

                    (mView.findViewById<View>(
                        mContext.resources.getIdentifier(
                            "carrier$i",
                            "id",
                            mContext.packageName
                        )
                    ).getField("mMobileSignal") as ImageView).imageTintList =
                        ColorStateList.valueOf(textColorPrimary)

                    (mView.findViewById<View>(
                        mContext.resources.getIdentifier(
                            "carrier$i",
                            "id",
                            mContext.packageName
                        )
                    ).getField("mMobileRoaming") as ImageView).imageTintList =
                        ColorStateList.valueOf(textColorPrimary)
                } catch (ignored: Throwable) {
                }
            }

            batteryIcon.callMethod(
                "updateColors",
                textColorPrimary,
                textColorSecondary,
                textColorPrimary
            )
        } catch (throwable: Throwable) {
            log(this@QSLightThemeA15, throwable)
        }
    }

    private fun setMobileIconTint(modernStatusBarViewBinding: Any, textColor: Int) {
        modernStatusBarViewBinding.callMethod(
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
            shadeCarrierGroupController.getField("mCarrierGroups") as Array<View>?

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

    @SuppressLint("AppCompatCustomView")
    inner class TintControlledImageView : ImageView {

        constructor(context: Context?) : super(context)

        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

        constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        )

        override fun setImageTintList(tintList: ColorStateList?) {
            super.setImageTintList(getIconTintLightMode(tintList))
        }

        private fun getIconTintLightMode(tintList: ColorStateList?): ColorStateList {
            return when (tintList) {
                mPrimaryLabelActiveColor -> ColorStateList.valueOf(Color.WHITE)
                mSecondaryLabelActiveColor -> ColorStateList.valueOf(-0x7f000001)
                mPrimaryLabelInactiveColor -> ColorStateList.valueOf(Color.BLACK)
                mSecondaryLabelInactiveColor -> ColorStateList.valueOf(-0x80000000)
                else -> tintList ?: ColorStateList.valueOf(Color.BLACK)
            }
        }
    }

    companion object {
        private var lightQSHeaderEnabled = false
        private var dualToneQSEnabled = false
        private const val PM_LITE_BACKGROUND_CODE = 1
    }
}