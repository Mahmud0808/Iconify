package com.drdisagree.iconify.xposed.modules.quicksettings

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.Notification
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getAnyField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethodMatchPattern
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setFieldSilently
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

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
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        setQsMargin()
        fixNotificationColor()
        manageQsElementVisibility()
        compactMediaPlayer()
        blurMediaPlayerArtwork()
    }

    private fun setQsMargin() {
        fun getQqsMargin() = if (mContext.isLandscape) qqsTopMarginLand else qqsTopMarginPort
        fun getQsMargin() = if (mContext.isLandscape) qsTopMarginLand else qsTopMarginPort

        ResourceHookManager
            .hookDimen()
            .whenCondition { customQsMarginsEnabled }
            .forPackageName(SYSTEMUI_PACKAGE)
            .addResource("large_screen_shade_header_height") { getQqsMargin() }
            .addResource("qs_panel_padding_top") { getQsMargin().toFloat() }
            .apply()
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

        val actionsDialogLiteClass =
            findClass($$"$$SYSTEMUI_PACKAGE.globalactions.GlobalActionsDialogLite$ActionsDialogLite")
        val singlePressActionClass =
            findClass($$"$$SYSTEMUI_PACKAGE.globalactions.GlobalActionsDialogLite$SinglePressAction")

        actionsDialogLiteClass
            .hookMethod("onCreate")
            .parameters(Bundle::class.java)
            .runAfter { param ->
                if (!fixNotificationColor) return@runAfter

                val dialog = param.thisObject as Dialog

                val listView = dialog.findViewById<View>(
                    mContext.resources.getIdentifier(
                        "list",
                        "id",
                        FRAMEWORK_PACKAGE
                    )
                )
                listView.backgroundTintList = null
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
}