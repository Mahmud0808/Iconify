package com.drdisagree.iconify.xposed.modules.lockscreen

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyBlur
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.hideView
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.MethodHookParam
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHelpers.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHelpers.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHelpers.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHelpers.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.hookMethod
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

class Lockscreen(context: Context) : ModPack(context) {

    private var wallpaperBlurEnabled = false
    private var wallpaperBlurRadius = 6.25f
    private var hideLockscreenLockIcon = false
    private var hideQsOnLockscreen = false
    private var mKeyguardStateController: Any? = null

    override fun onPreferenceUpdated(vararg key: String) {
        Xprefs.apply {
            wallpaperBlurEnabled = getBoolean(XposedKey.LOCKSCREEN_WALLPAPER_BLUR)
            wallpaperBlurRadius = getInt(XposedKey.LOCKSCREEN_WALLPAPER_BLUR_RADIUS) / 100f * 25f
            hideLockscreenLockIcon = getBoolean(XposedKey.HIDE_LOCKSCREEN_LOCK_ICON)
            hideQsOnLockscreen = false
        }

        when (key.firstOrNull()) {
            XposedKey.HIDE_LOCKSCREEN_LOCK_ICON.name -> hideLockscreenLockIcon()
        }
    }

    override fun onPackageLoaded(packageReadyParam: PackageReadyParam) {
        blurredWallpaper()
        hideLockscreenLockIcon()
        disableQsOnSecureLockScreen()
    }

    private fun blurredWallpaper() {
        val canvasEngineClass =
            findClass($$"$$SYSTEMUI_PACKAGE.wallpapers.ImageWallpaper$CanvasEngine")

        canvasEngineClass
            .hookMethod("drawFrameOnCanvas")
            .parameters(Bitmap::class.java)
            .runBefore { param ->
                val canvasEngine = param.thisObject
                val isLockscreenWallpaper = (canvasEngine.callMethodSilently(
                    "getWallpaperFlags"
                ) as? Int ?: WallpaperManager.FLAG_LOCK) == WallpaperManager.FLAG_LOCK

                if (wallpaperBlurEnabled && wallpaperBlurRadius > 0 && isLockscreenWallpaper) {
                    val bitmap = param.args[0] as Bitmap
                    val displayContext = canvasEngine.callMethod("getDisplayContext") as Context

                    param.args[0] = bitmap.applyBlur(displayContext, wallpaperBlurRadius)
                }
            }
    }

    @SuppressLint("DiscouragedApi")
    private fun hideLockscreenLockIcon() {
        val aodBurnInLayerClass =
            findClass("$SYSTEMUI_PACKAGE.keyguard.ui.view.layout.sections.AodBurnInLayer")
        var aodBurnInLayerHooked = false

        // Apparently ROMs like CrDroid doesn't even use AodBurnInLayer class
        // So we hook whichever is available
        val keyguardStatusViewClass = findClass(
            "com.android.keyguard.KeyguardStatusView",
            suppressError = true // Android 16
        )
        var keyguardStatusViewHooked = false

        fun hideLockIcon(param: MethodHookParam) {
            val entryV = param.thisObject as View

            // If both are already hooked, return. We only want to hook one
            if (aodBurnInLayerHooked && keyguardStatusViewHooked) return

            entryV.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!hideLockscreenLockIcon) return@postDelayed

                        val rootView = v.parent as? ViewGroup ?: return@postDelayed

                        // If rootView is not R.id.keyguard_root_view, detach and return
                        if (rootView.id != mContext.resources.getIdentifier(
                                "keyguard_root_view",
                                "id",
                                mContext.packageName
                            )
                        ) {
                            entryV.removeOnAttachStateChangeListener(this)
                            return@postDelayed
                        }

                        listOf(
                            "device_entry_icon_bg",
                            "device_entry_icon_fg"
                        ).map { resourceName ->
                            val resourceId = mContext.resources.getIdentifier(
                                resourceName,
                                "id",
                                mContext.packageName
                            )
                            if (resourceId != -1) {
                                rootView.findViewById<View?>(resourceId)
                            } else {
                                null
                            }
                        }.forEach { view ->
                            view.hideView()
                        }
                    }, 1000)
                }

                override fun onViewDetachedFromWindow(v: View) {}
            })
        }

        aodBurnInLayerClass
            .hookConstructor()
            .runAfter { param ->
                if (!hideLockscreenLockIcon) return@runAfter

                aodBurnInLayerHooked = true

                hideLockIcon(param)
            }

        keyguardStatusViewClass
            .hookConstructor()
            .runAfter { param ->
                if (!hideLockscreenLockIcon) return@runAfter

                keyguardStatusViewHooked = true

                hideLockIcon(param)
            }
    }

    private fun disableQsOnSecureLockScreen() {
        val phoneStatusBarPolicyClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.PhoneStatusBarPolicy")
        val scrimManagerClass = findClass(
            "$SYSTEMUI_PACKAGE.ambient.touch.scrim.ScrimManager",
            "$SYSTEMUI_PACKAGE.dreams.touch.scrim.ScrimManager"
        )
        val notificationPanelViewControllerClass =
            findClass("$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController")

        fun getKeyguardStateController(param: MethodHookParam) {
            param.thisObject.getFieldSilently("mKeyguardStateController")
        }

        phoneStatusBarPolicyClass
            .hookConstructor()
            .runAfter { getKeyguardStateController(it) }

        scrimManagerClass
            .hookConstructor()
            .runAfter { getKeyguardStateController(it) }

        notificationPanelViewControllerClass
            .hookConstructor()
            .runAfter { getKeyguardStateController(it) }

        notificationPanelViewControllerClass
            .hookMethod("onFinishInflate", "reInflateViews")
            .runAfter { getKeyguardStateController(it) }

        var mActivityStarter: Any? = null

        val keyguardQuickAffordanceInteractorClass =
            findClass("$SYSTEMUI_PACKAGE.keyguard.domain.interactor.KeyguardQuickAffordanceInteractor")

        keyguardQuickAffordanceInteractorClass
            .hookConstructor()
            .runAfter { param ->
                mActivityStarter = param.thisObject.getFieldSilently("activityStarter")
            }

        val qsTiles = listOf(
            "$SYSTEMUI_PACKAGE.qs.tiles.AirplaneModeTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.AlarmTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.BatterySaverTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.BluetoothTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.CameraToggleTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.CastTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.ColorCorrectionTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.ColorInversionTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.DataSaverTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.DeviceControlsTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.DndTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.DreamTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.FontScalingTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.HearingDevicesTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.HotspotTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.InternetTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.InternetTileNewImpl",
            "$SYSTEMUI_PACKAGE.qs.tiles.LocationTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.MicrophoneToggleTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.ModesTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.NfcTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.NightDisplayTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.OneHandedModeTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.QRCodeScannerTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.QuickAccessWalletTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.RecordIssueTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.ReduceBrightColorsTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.RotationLockTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.ScreenRecordTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.SensorPrivacyToggleTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.UiModeNightTile",
            "$SYSTEMUI_PACKAGE.qs.tiles.WorkModeTile"
        )

        fun handleTileClick(
            mActivityStarter: Any?,
            param: MethodHookParam,
            methodName: String
        ) {
            val isUnlocked = try {
                !(mKeyguardStateController.getField("mShowing") as Boolean) ||
                        mKeyguardStateController.getField("mCanDismissLockScreen") as Boolean
            } catch (_: Throwable) {
                mKeyguardStateController.callMethod("isUnlocked") as Boolean
            }

            if (!isUnlocked && hideQsOnLockscreen) {
                mActivityStarter.callMethod(
                    "postQSRunnableDismissingKeyguard",
                    Runnable {
                        Handler(Looper.getMainLooper()).postDelayed({
                            param.thisObject.callMethod(
                                methodName,
                                param.args[0]
                            )
                        }, 800)
                    }
                )
                param.result = null
            }
        }

        qsTiles.forEach { tileClassName ->
            val tileClass = findClass(tileClassName, suppressError = true)

            tileClass.hookMethod("handleClick")
                .runBefore { param ->
                    handleTileClick(
                        mActivityStarter,
                        param,
                        "handleClick"
                    )
                }

            tileClass.hookMethod("handleSecondaryClick")
                .runBefore { param ->
                    handleTileClick(
                        mActivityStarter,
                        param,
                        "handleSecondaryClick"
                    )
                }
        }
    }
}