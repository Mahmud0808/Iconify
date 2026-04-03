package com.drdisagree.iconify.xposed.modules.quicksettings

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnDrawListener
import android.widget.Button
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.DisplayUtils.isLandscape
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyBlur
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethodMatchPattern
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setFieldSilently
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class QuickSettings(context: Context) : ModPack(context) {

    private var fixNotificationColor = true
    private var fixNotificationFooterButtonsColor = true
    private var hideSilentText = false
    private var hideFooterButtons = false
    private var qqsTopMarginPort = 100
    private var qsTopMarginPort = 100
    private var qqsTopMarginLand = 0
    private var qsTopMarginLand = 0
    private var mFooterButtonsContainer: ViewGroup? = null
    private var mFooterButtonsOnDrawListener: OnDrawListener? = null
    private var mSilentTextContainer: ViewGroup? = null
    private var mSilentTextOnDrawListener: OnDrawListener? = null
    private var customQsMarginsEnabled = false
    private var compactMediaPlayerEnabled = false
    private var blurMediaPlayerArtwork = false
    private var blurMediaPlayerArtworkRadius = 15f

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
            hideSilentText = getBoolean(XposedKey.HIDE_QS_SILENT_TEXT)
            hideFooterButtons = getBoolean(XposedKey.HIDE_QS_FOOTER_BUTTONS)
            compactMediaPlayerEnabled = getBoolean(XposedKey.COMPACT_MEDIA_PLAYER)
            blurMediaPlayerArtwork = getBoolean(XposedKey.BLUR_MEDIA_PLAYER_ARTWORK)
            blurMediaPlayerArtworkRadius =
                getFloat(XposedKey.BLUR_MEDIA_PLAYER_ARTWORK_RADIUS) / 100f * 25f
        }

        triggerQsElementVisibility()
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        setQsMargin()
        fixNotificationColorA14()
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

    private fun fixNotificationColorA14() {
        val activatableNotificationViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.row.ActivatableNotificationView")
        val notificationBackgroundViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.row.NotificationBackgroundView")
        val footerViewClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.notification.footer.ui.view.FooterView",
            "$SYSTEMUI_PACKAGE.statusbar.notification.row.FooterView"
        )

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

                (notificationBackgroundView?.getFieldSilently(
                    "mBackground"
                ) as? Drawable)?.colorFilter = null

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

                try {
                    val mManageButton = try {
                        param.thisObject.getField("mManageButton")
                    } catch (_: Throwable) {
                        param.thisObject.getField("mManageOrHistoryButton")
                    } as Button
                    val mClearAllButton = try {
                        param.thisObject.getField("mClearAllButton")
                    } catch (_: Throwable) {
                        param.thisObject.getField("mDismissButton")
                    } as Button

                    mManageButton.background?.colorFilter = null
                    mClearAllButton.background?.colorFilter = null

                    Handler(Looper.getMainLooper()).post {
                        mManageButton.invalidate()
                        mClearAllButton.invalidate()
                    }
                } catch (_: Throwable) {
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
                    mFooterButtonsContainer = view.findViewById<View?>(resId1)?.parent as? ViewGroup
                } else if (resId2 != 0) {
                    mFooterButtonsContainer = view.findViewById<View?>(resId2)?.parent as? ViewGroup
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

    private fun compactMediaPlayer() {
        val mediaViewControllerClass =
            findClass(
                "$SYSTEMUI_PACKAGE.media.controls.ui.prefController.MediaViewController",
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
            "$SYSTEMUI_PACKAGE.media.controls.ui.prefController.MediaControlPanel",
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
            } catch (_: Throwable) {
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
            } catch (_: Throwable) {
            }
        }
    }
}