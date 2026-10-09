package com.drdisagree.iconify.xposed.modules.quicksettings

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.Notification
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.graphics.ColorUtils
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.DisplayUtils.isLandscape
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.applyBlur
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.hideView
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ComposeToolkit
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getAnyField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getExtraFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethodMatchPattern
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setExtraField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setFieldSilently
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Method
import java.util.WeakHashMap

@SuppressLint("DiscouragedApi")
class QuickSettings(context: Context) : ModPack(context) {

    private var fixNotificationColor = true
    private var fixNotificationFooterButtonsColor = true
    private var fixNotificationExpandButtonColor = true
    private var hideSilentText = false
    private var hideFooterButtons = false
    private var qqsTopMarginPort = 100
    private var qsTopMarginPort = 100
    private var qqsTopMarginLand = 0
    private var qsTopMarginLand = 0
    private var customQsMarginsEnabled = false
    private var compactMediaPlayerEnabled = false
    private var blurMediaPlayerArtwork = false
    private var blurMediaPlayerArtworkRadius = 15f
    private var coloredNotificationView = false
    private var shadeHeaderDimensionsClass: Class<*>? = null
    private var defaultExpandedHeaderHeight: Float? = null
    private var expandedHeaderHeightWritable = true
    private var expandedHeaderDepth = 0
    private val paddedToOriginalHeaderModifier = WeakHashMap<Any, Any>()

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            customQsMarginsEnabled = getBoolean(XposedKey.CUSTOM_QS_MARGINS)
            qqsTopMarginPort = getInt(XposedKey.QQS_TOP_MARGIN_PORTRAIT)
            qsTopMarginPort = getInt(XposedKey.QS_TOP_MARGIN_PORTRAIT)
            qqsTopMarginLand = getInt(XposedKey.QQS_TOP_MARGIN_LANDSCAPE)
            qsTopMarginLand = getInt(XposedKey.QS_TOP_MARGIN_LANDSCAPE)
            fixNotificationColor = getBoolean(XposedKey.FIX_NOTIFICATION_COLOR)
            fixNotificationFooterButtonsColor =
                getBoolean(XposedKey.FIX_NOTIFICATION_FOOTER_BUTTON_COLOR)
            fixNotificationExpandButtonColor =
                getBoolean(XposedKey.FIX_NOTIFICATION_EXPAND_BUTTON_COLOR)
            hideSilentText = getBoolean(XposedKey.HIDE_QS_SILENT_TEXT)
            hideFooterButtons = getBoolean(XposedKey.HIDE_QS_FOOTER_BUTTONS)
            compactMediaPlayerEnabled = getBoolean(XposedKey.COMPACT_MEDIA_PLAYER)
            blurMediaPlayerArtwork = getBoolean(XposedKey.BLUR_MEDIA_PLAYER_ARTWORK)
            blurMediaPlayerArtworkRadius =
                getFloat(XposedKey.BLUR_MEDIA_PLAYER_ARTWORK_RADIUS) / 100f * 25f
            coloredNotificationView = getBoolean(XposedKey.COLORED_NOTIFICATION_VIEW)
        }

        applyExpandedHeaderHeight()
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        setQsMargin()
        fixNotificationColor()
        manageQsElementVisibility()
        compactMediaPlayer()
        blurMediaPlayerArtwork()
    }

    private fun getQqsMargin() = if (mContext.isLandscape) qqsTopMarginLand else qqsTopMarginPort

    private fun getQsMargin() = if (mContext.isLandscape) qsTopMarginLand else qsTopMarginPort

    private val isAndroid17Qpr1OrLater: Boolean
        get() = Build.VERSION.SDK_INT >= 37 && (Build.VERSION.SDK_INT > 37 ||
                Build.getMinorSdkVersion(Build.VERSION.SDK_INT_FULL) >= 1)

    private fun setQsMargin() {
        ResourceHookManager
            .hookDimen()
            .whenCondition { customQsMarginsEnabled }
            .forPackageName(SYSTEMUI_PACKAGE)
            .addResource("large_screen_shade_header_height") { getQqsMargin() }
            .addResource("qs_panel_padding_top") {
                if (isAndroid17Qpr1OrLater) {
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        getQsMargin().toFloat(),
                        mContext.resources.displayMetrics
                    )
                } else {
                    getQsMargin().toFloat()
                }
            }
            .apply()

        setSceneContainerQsMargin()
    }

    private fun setSceneContainerQsMargin() {
        shadeHeaderDimensionsClass = findClass(
            $$"$$SYSTEMUI_PACKAGE.shade.ui.composable.ShadeHeader$Dimensions",
            suppressError = true
        ) ?: return
        val shadeHeaderClass = findClass(
            "$SYSTEMUI_PACKAGE.shade.ui.composable.ShadeHeaderKt",
            suppressError = true
        ) ?: return
        val paddingMethod = ComposeToolkit.modifierFunction(
            "androidx.compose.foundation.layout.PaddingKt",
            "padding",
            Float::class.javaPrimitiveType,
            Float::class.javaPrimitiveType,
            Float::class.javaPrimitiveType,
            Float::class.javaPrimitiveType
        )

        shadeHeaderClass
            .hookMethod("ExpandedShadeHeader")
            .suppressError()
            .runBefore {
                expandedHeaderDepth++
                applyExpandedHeaderHeight()
            }
            .runAfter { expandedHeaderDepth-- }

        findClass("androidx.compose.foundation.layout.SizeKt", suppressError = true)
            .hookMethodMatchPattern("defaultMinSize-.*\\\$default")
            .suppressError()
            .runBefore { param ->
                if (expandedHeaderDepth <= 0 || !customQsMarginsEnabled) return@runBefore
                val defaultHeight = defaultExpandedHeaderHeight ?: return@runBefore
                if (param.args.getOrNull(2) as? Float != defaultHeight) return@runBefore

                param.args[2] = getQsMargin().toFloat()
            }

        if (paddingMethod == null) return

        shadeHeaderClass
            .hookMethod("CollapsedShadeHeader")
            .suppressError()
            .runBefore { param ->
                val index = (param.method as Method).parameterTypes.indexOfFirst {
                    it.name == COMPOSE_MODIFIER_CLASS
                }
                if (index == -1) return@runBefore

                val modifier = param.args[index] ?: return@runBefore
                val original = paddedToOriginalHeaderModifier[modifier] ?: modifier
                param.args[index] = original

                if (!customQsMarginsEnabled) return@runBefore

                val statusBarHeightPx = param.args
                    .firstOrNull { it?.javaClass?.simpleName == "ShadeHeaderViewModel" }
                    .getFieldSilently($$"statusBarHeightPx$delegate")
                    .callMethodSilently("getValue") as? Number ?: return@runBefore
                val density = mContext.resources.displayMetrics.density
                val extraDp = getQqsMargin() - statusBarHeightPx.toFloat() / density
                if (extraDp <= 0f) return@runBefore

                val padded = try {
                    paddingMethod.invoke(null, original, 0f, 0f, 0f, extraDp)
                } catch (_: Throwable) {
                    null
                } ?: return@runBefore

                paddedToOriginalHeaderModifier[padded] = original
                param.args[index] = padded
            }
    }

    private fun applyExpandedHeaderHeight() {
        val dimensionsClass = shadeHeaderDimensionsClass ?: return

        try {
            val field = dimensionsClass.getDeclaredField(EXPANDED_HEADER_HEIGHT).apply {
                isAccessible = true
            }
            val defaultHeight = defaultExpandedHeaderHeight
                ?: field.getFloat(null).also { defaultExpandedHeaderHeight = it }
            if (!expandedHeaderHeightWritable) return

            val height = if (customQsMarginsEnabled) getQsMargin().toFloat() else defaultHeight
            if (field.getFloat(null) != height) field.setFloat(null, height)
        } catch (_: Throwable) {
            expandedHeaderHeightWritable = false
        }
    }

    private fun fixNotificationColor() {
        val activatableNotificationViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.row.ActivatableNotificationView")
        val notificationBackgroundViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.row.NotificationBackgroundView")
        val footerViewClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.notification.footer.ui.view.FooterView",
            "$SYSTEMUI_PACKAGE.statusbar.notification.row.FooterView"
        )
        val notificationBuilderClass = findClass($$"android.app.Notification$Builder")

        activatableNotificationViewClass
            .hookMethod("setBackgroundTintColor", "updateBackgroundTint")
            .runBefore { param ->
                if (!fixNotificationColor) return@runBefore

                val notificationBackgroundView = param.thisObject.getFieldSilently(
                    "mBackgroundNormal"
                ) as? View

                if (param.args.size > 0 && param.args[0] is Int) {
                    param.thisObject.setFieldSilently("mCurrentBackgroundTint", param.args[0])
                }

                notificationBackgroundView?.setFieldSilently("mTintColor", 0)
            }
            .runAfter { param ->
                if (!fixNotificationColor) return@runAfter

                val notificationBackgroundView = param.thisObject.getFieldSilently(
                    "mBackgroundNormal"
                ) as? View

                notificationBackgroundView?.callMethodSilently("setColorFilter", 0)

                try {
                    (notificationBackgroundView.getFieldSilently("mBackground") as? Drawable)
                        ?.colorFilter = null
                } catch (_: Throwable) {
                }

                notificationBackgroundView?.setFieldSilently("mTintColor", 0)

                Handler(Looper.getMainLooper()).post {
                    notificationBackgroundView?.invalidate()
                }
            }

        activatableNotificationViewClass
            .hookMethod("calculateBgColor")
            .runBefore { param ->
                if (!fixNotificationColor) return@runBefore

                try {
                    param.result = param.thisObject.getField(
                        "mCurrentBackgroundTint"
                    )
                } catch (_: Throwable) {
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

                val mClearAllButton = param.thisObject.getField("mClearAllButton") as Button
                val mHistoryButton = param.thisObject.getField("mHistoryButton") as Button
                val mSettingsButton = param.thisObject.getField("mSettingsButton") as Button

                listOf(mClearAllButton, mHistoryButton, mSettingsButton).forEach { button ->
                    button.background?.mutate()?.let {
                        it.colorFilter = null
                        it.setTintList(null)
                        it.setTintMode(null)
                        it.alpha = 255
                    }
                    button.backgroundTintList = null
                    button.backgroundTintMode = null
                }

                Handler(Looper.getMainLooper()).post {
                    mClearAllButton.invalidate()
                    mHistoryButton.invalidate()
                    mSettingsButton.invalidate()
                }
            }

        notificationBuilderClass
            .hookConstructor()
            .parameters(
                Context::class.java,
                Notification::class.java
            )
            .runAfter { param ->
                if (!fixNotificationExpandButtonColor || coloredNotificationView) return@runAfter

                val builder = param.thisObject as Notification.Builder

                val mParams = builder.getField("mParams")
                builder.callMethod("getColors", mParams)

                val mColors = builder.getField("mColors")
                val mPrimaryTextColor = mColors.getAnyField(
                    "mPrimaryTextColor",
                    "mTextColor"
                ) as Int
                val mBackgroundColor = mColors.getField("mBackgroundColor") as Int

                mColors.setField(
                    "mProtectionColor",
                    ColorUtils.blendARGB(mPrimaryTextColor, mBackgroundColor, 0.9f)
                )
            }

        val actionsDialogLiteClass = findClass(
            $$"$$SYSTEMUI_PACKAGE.globalactions.GlobalActionsDialogLite$ActionsDialogLite",
            suppressError = true
        )
        val actionsDialogLiteDelegateClass = findClass(
            $$"$$SYSTEMUI_PACKAGE.globalactions.GlobalActionsDialogLite$ActionsDialogLiteDelegate",
            suppressError = true
        )
        val singlePressActionClass =
            findClass($$"$$SYSTEMUI_PACKAGE.globalactions.GlobalActionsDialogLite$SinglePressAction")

        fun Dialog.clearActionListTint() {
            if (!fixNotificationColor) return

            findViewById<View>(
                mContext.resources.getIdentifier(
                    "list",
                    "id",
                    FRAMEWORK_PACKAGE
                )
            )?.backgroundTintList = null
        }

        actionsDialogLiteClass
            .hookMethod("onCreate")
            .parameters(Bundle::class.java)
            .runAfter { param -> (param.thisObject as Dialog).clearActionListTint() }

        actionsDialogLiteDelegateClass
            .hookMethod("onCreate")
            .parameters(Dialog::class.java, Bundle::class.java)
            .runAfter { param -> (param.args[0] as? Dialog)?.clearActionListTint() }

        if (actionsDialogLiteClass == null && actionsDialogLiteDelegateClass == null) {
            log(this@QuickSettings, "Power menu dialog class not found")
        }

        singlePressActionClass
            .hookMethod("create")
            .runAfter { param ->
                if (!fixNotificationColor) return@runAfter

                val mIconView = param.thisObject.getField("mIconView") as View
                mIconView.backgroundTintList = null
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

                val mFooterButtonsContainer = listOf(
                    "dismiss_text",
                    "settings_button",
                    "history_button",
                    "manage_text"
                ).map {
                    mContext.resources.getIdentifier(
                        it,
                        "id",
                        SYSTEMUI_PACKAGE
                    )
                }.firstOrNull { it != 0 }?.let {
                    view.findViewById<View?>(it)?.parent as? ViewGroup
                }

                if (mFooterButtonsContainer != null) {
                    if (hideFooterButtons) mFooterButtonsContainer.hideView()
                } else {
                    log(this, "Footer buttons not found")
                }
            }

        val sectionHeaderViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.stack.SectionHeaderView")

        sectionHeaderViewClass
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                val mSilentTextContainer = param.thisObject as ViewGroup

                if (hideSilentText) mSilentTextContainer.hideView()
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

                val mediaHostStateClass = mediaHostState.javaClass

                if (mediaHostStateClass.getExtraFieldSilently("hooked") == true) return@runBefore

                mediaHostStateClass.setExtraField("hooked", true)

                // For a14 and above
                mediaHostStateClass
                    .hookMethod("getExpansion")
                    .suppressError()
                    .runBefore runBefore2@{ param2 ->
                        if (!compactMediaPlayerEnabled) return@runBefore2

                        param2.result = 0f
                    }

                // For some a13 and below ROMs
                mediaHostStateClass
                    .hookConstructor()
                    .runAfter { param2 ->
                        if (!compactMediaPlayerEnabled) return@runAfter

                        param2.thisObject.setFieldSilently("expansion", 0f)
                    }
            }
    }

    private fun blurMediaPlayerArtwork() {
        val mediaControlPanelClass = findClass(
            "$SYSTEMUI_PACKAGE.media.controls.ui.controller.MediaControlPanel",
            "$SYSTEMUI_PACKAGE.media.controls.ui.MediaControlPanel",
            "$SYSTEMUI_PACKAGE.media.MediaControlPanel"
        )

        try {
            mediaControlPanelClass
                .hookMethod("getScaledBackground", "scaleDrawable")
                .throwError()
                .runAfter { param ->
                    if (!blurMediaPlayerArtwork) return@runAfter

                    val artwork = param.result as? Drawable

                    if (artwork != null) {
                        param.result = artwork.applyBlur(mContext, blurMediaPlayerArtworkRadius)
                    }
                }
        } catch (_: Throwable) {
            mediaControlPanelClass
                .hookMethod("addGradientToPlayerAlbum")
                .runAfter { param ->
                    if (!blurMediaPlayerArtwork) return@runAfter

                    val playerAlbumDrawable = param.result as? LayerDrawable
                    val artwork = playerAlbumDrawable?.getDrawable(0)

                    if (artwork != null) {
                        val blurredArtwork = artwork.applyBlur(
                            mContext,
                            blurMediaPlayerArtworkRadius
                        )
                        playerAlbumDrawable.setDrawable(0, blurredArtwork)
                        param.result = playerAlbumDrawable
                    }
                }
        }
    }

    companion object {
        private const val COMPOSE_MODIFIER_CLASS = ComposeToolkit.MODIFIER_CLASS
        private const val EXPANDED_HEADER_HEIGHT = "ExpandedHeight"
    }
}