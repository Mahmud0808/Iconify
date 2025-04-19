package com.drdisagree.iconify.xposed.modules.lockscreen

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.res.Resources
import android.content.res.XResources
import android.content.res.XResources.DrawableLoader
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.HIDE_LOCKSCREEN_LOCK_ICON
import com.drdisagree.iconify.data.common.Preferences.HIDE_QS_ON_LOCKSCREEN
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WALLPAPER_BLUR
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WALLPAPER_BLUR_RADIUS
import com.drdisagree.iconify.xposed.HookRes.Companion.resParams
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.TimeUtils.isSecurityPatchAfter
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyBlur
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.hideView
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookLayout
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.util.Calendar

class Lockscreen(context: Context) : ModPack(context) {

    private var wallpaperBlurEnabled = false
    private var wallpaperBlurRadius = 6.25f
    private var hideLockscreenLockIcon = false
    private var hideQsOnLockscreen = false
    private var mKeyguardStateController: Any? = null

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            wallpaperBlurEnabled = getBoolean(LOCKSCREEN_WALLPAPER_BLUR, false)
            wallpaperBlurRadius = getSliderInt(LOCKSCREEN_WALLPAPER_BLUR_RADIUS, 25) / 100f * 25f
            hideLockscreenLockIcon = getBoolean(HIDE_LOCKSCREEN_LOCK_ICON, false)
            hideQsOnLockscreen = getBoolean(HIDE_QS_ON_LOCKSCREEN, false)
        }

        when (key.firstOrNull()) {
            HIDE_LOCKSCREEN_LOCK_ICON -> hideLockscreenLockIcon()
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        blurredWallpaper()
        hideLockscreenLockIcon()
        disableQsOnSecureLockScreen()
    }

    private fun blurredWallpaper() {
        val canvasEngineClass =
            findClass("$SYSTEMUI_PACKAGE.wallpapers.ImageWallpaper\$CanvasEngine")

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
        if (!isComposeLockscreen) {
            val xResources: XResources = resParams[SYSTEMUI_PACKAGE]?.res ?: return

            xResources
                .hookLayout()
                .packageName(SYSTEMUI_PACKAGE)
                .resource("layout", "status_bar_expanded")
                .suppressError()
                .run { liparam ->
                    liparam.view.findViewById<View>(
                        liparam.res.getIdentifier(
                            "lock_icon_view",
                            "id",
                            mContext.packageName
                        )
                    ).apply {
                        if (!hideLockscreenLockIcon) return@apply

                        layoutParams.height = 0
                        layoutParams.width = 0
                        visibility = View.GONE
                        viewTreeObserver.addOnDrawListener {
                            visibility = View.GONE
                        }
                        requestLayout()
                    }
                }
        } else {
            val aodBurnInLayerClass =
                findClass("$SYSTEMUI_PACKAGE.keyguard.ui.view.layout.sections.AodBurnInLayer")
            var aodBurnInLayerHooked = false

            // Apparently ROMs like CrDroid doesn't even use AodBurnInLayer class
            // So we hook which ever is available
            val keyguardStatusViewClass = findClass(
                "com.android.keyguard.KeyguardStatusView",
                suppressError = Build.VERSION.SDK_INT >= 36 // Android 16
            )
            var keyguardStatusViewHooked = false

            fun hideLockIcon(param: XC_MethodHook.MethodHookParam) {
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

        if (!hideLockscreenLockIcon) return

        val xResources: XResources = resParams[SYSTEMUI_PACKAGE]?.res ?: return

        listOf(
            "ic_device_lock_off",
            "ic_device_lock_on",
            "ic_kg_fingerprint",
            "ic_lock",
            "ic_lock_24dp",
            "ic_lock_aod",
            "ic_lock_face",
            "ic_lock_lock",
            "ic_lock_locked",
            "ic_lock_open",
            "ic_lock_open_24dp",
            "ic_unlock",
            "ic_unlocked",
            "ic_unlocked_aod"
        ).forEach { drawableResource ->
            try {
                xResources.setReplacement(
                    SYSTEMUI_PACKAGE,
                    "drawable",
                    drawableResource,
                    object : DrawableLoader() {
                        override fun newDrawable(res: XResources, id: Int): Drawable? {
                            return GradientDrawable().apply {
                                shape = GradientDrawable.OVAL
                                setColor(Color.TRANSPARENT)
                            }.constantState?.newDrawable()
                        }
                    }
                )
            } catch (ignored: Resources.NotFoundException) {
            }
        }
    }

    private fun disableQsOnSecureLockScreen() {
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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val remoteInputQuickSettingsDisablerClass =
                findClass("$SYSTEMUI_PACKAGE.statusbar.policy.RemoteInputQuickSettingsDisabler")

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
        } else {
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
                param: XC_MethodHook.MethodHookParam,
                methodName: String
            ) {
                val isUnlocked = try {
                    !(mKeyguardStateController.getField("mShowing") as Boolean) ||
                            mKeyguardStateController.getField("mCanDismissLockScreen") as Boolean
                } catch (ignored: Throwable) {
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

    companion object {
        /*
         * Source: frameworks/base/core/java/android/app/StatusBarManager.java
         */
        private const val DISABLE2_QUICK_SETTINGS = 1

        val isComposeLockscreen: Boolean = run {
            val hasAodBurnInLayer = findClass(
                "$SYSTEMUI_PACKAGE.keyguard.ui.view.layout.sections.AodBurnInLayer",
                suppressError = true
            ) != null

            val hasBatteryMeterViewEx = findClass(
                "com.nothing.systemui.battery.BatteryMeterViewEx",
                suppressError = true
            ) != null

            val isSupportedAndroidVersion =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM

            val isAfterSecurityPatch = isSecurityPatchAfter(
                Calendar.getInstance().apply { set(2024, Calendar.NOVEMBER, 30) }
            )

            hasAodBurnInLayer && !hasBatteryMeterViewEx && isSupportedAndroidVersion && isAfterSecurityPatch
        }
    }
}