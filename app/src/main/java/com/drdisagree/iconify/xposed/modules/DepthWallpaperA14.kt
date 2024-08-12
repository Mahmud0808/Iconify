package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.drdisagree.iconify.IExtractSubjectCallback
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.CUSTOM_DEPTH_WALLPAPER_SWITCH
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_BACKGROUND_MOVEMENT_MULTIPLIER
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_CHANGED
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_FOREGROUND_ALPHA
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_FOREGROUND_MOVEMENT_MULTIPLIER
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_ON_AOD
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_SWITCH
import com.drdisagree.iconify.config.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.HookEntry.Companion.enqueueProxyCommand
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.Helpers.findClassInArray
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.getFloatField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max

@SuppressLint("DiscouragedApi")
class DepthWallpaperA14(context: Context?) : ModPack(context!!) {

    private var showDepthWallpaper = false
    private var showCustomImages = false
    private var backgroundMovement = 1.0f
    private var foregroundMovement = 3.0f
    private var foregroundAlpha = 1.0f
    private var mScrimController: Any? = null
    private var mForegroundDimmingOverlay: Drawable? = null
    private var mWallpaperForeground: FrameLayout? = null
    private var mWallpaperBackground: FrameLayout? = null
    private var mWallpaperBitmapContainer: FrameLayout? = null
    private var mWallpaperDimmingOverlay: FrameLayout? = null
    private var mWallpaperForegroundCacheValid = false
    private var mLayersCreated = false
    private var showOnAOD = true
    private var foregroundPath = Environment.getExternalStorageDirectory()
        .toString() + "/.iconify_files/depth_wallpaper_fg.png"
    private var backgroundPath = Environment.getExternalStorageDirectory()
        .toString() + "/.iconify_files/depth_wallpaper_bg.png"

    override fun updatePrefs(vararg key: String) {
        if (Xprefs == null) return

        showDepthWallpaper = Xprefs!!.getBoolean(DEPTH_WALLPAPER_SWITCH, false)
        showCustomImages = Xprefs!!.getBoolean(CUSTOM_DEPTH_WALLPAPER_SWITCH, false)
        backgroundMovement = Xprefs!!.getFloat(DEPTH_WALLPAPER_BACKGROUND_MOVEMENT_MULTIPLIER, 1.0f)
        foregroundMovement = Xprefs!!.getFloat(DEPTH_WALLPAPER_FOREGROUND_MOVEMENT_MULTIPLIER, 3.0f)
        foregroundAlpha = Xprefs!!.getInt(DEPTH_WALLPAPER_FOREGROUND_ALPHA, 80) / 100.0f
        showOnAOD = Xprefs!!.getBoolean(DEPTH_WALLPAPER_ON_AOD, true)

        if (key.isNotEmpty()) {
            key[0].let {
                if (it == DEPTH_WALLPAPER_SWITCH ||
                    it == DEPTH_WALLPAPER_CHANGED ||
                    it == CUSTOM_DEPTH_WALLPAPER_SWITCH
                ) {
                    if (it == DEPTH_WALLPAPER_CHANGED) {
                        mWallpaperForegroundCacheValid = false
                    }

                    if (it == CUSTOM_DEPTH_WALLPAPER_SWITCH && !showCustomImages) {
                        invalidateCache()
                    }

                    setCustomDepthWallpaper()
                }
            }
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val qsImplClass = findClassInArray(
            loadPackageParam.classLoader,
            "$SYSTEMUI_PACKAGE.qs.QSImpl",
            "$SYSTEMUI_PACKAGE.qs.QSFragment"
        )
        val canvasEngineClass = findClass(
            "$SYSTEMUI_PACKAGE.wallpapers.ImageWallpaper\$CanvasEngine",
            loadPackageParam.classLoader
        )
        val centralSurfacesImplClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.CentralSurfacesImpl",
            loadPackageParam.classLoader
        )
        val scrimControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.ScrimController",
            loadPackageParam.classLoader
        )
        val scrimViewClass = findClass(
            "$SYSTEMUI_PACKAGE.scrim.ScrimView",
            loadPackageParam.classLoader
        )

        hookAllMethods(scrimViewClass, "setViewAlpha", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!mLayersCreated) return

                if (showOnAOD && getObjectField(
                        mScrimController,
                        "mState"
                    ).toString() != "KEYGUARD"
                ) {
                    mWallpaperForeground?.post { mWallpaperForeground?.setAlpha(foregroundAlpha) }
                } else if (getObjectField(
                        mScrimController,
                        "mNotificationsScrim"
                    ) == param.thisObject
                ) { // instead of using the mScrimName since older ones don't have that field
                    val mScrimBehindAlphaKeyguard = getFloatField(
                        mScrimController,
                        "mScrimBehindAlphaKeyguard"
                    )

                    var notificationAlpha = param.args[0] as Float

                    if (notificationAlpha < mScrimBehindAlphaKeyguard) {
                        notificationAlpha = 0f
                    }

                    val foregroundAlpha = if (notificationAlpha > mScrimBehindAlphaKeyguard) {
                        (1f - notificationAlpha) / (1f - mScrimBehindAlphaKeyguard)
                    } else {
                        1f
                    }

                    mWallpaperForeground?.post { mWallpaperForeground?.setAlpha(foregroundAlpha) }
                }
            }
        })

        hookAllMethods(centralSurfacesImplClass, "start", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val scrimBehind = getObjectField(mScrimController, "mScrimBehind") as View
                val rootView = scrimBehind.parent as ViewGroup

                val targetView = rootView.findViewById<ViewGroup>(
                    mContext.resources.getIdentifier(
                        "notification_container_parent",
                        "id",
                        mContext.packageName
                    )
                )

                if (!mLayersCreated) {
                    createLayers()
                }

                rootView.addView(mWallpaperBackground, 0)

                targetView.addView(mWallpaperForeground, 1)
            }
        })

        hookAllMethods(centralSurfacesImplClass, "onStartedWakingUp", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                setDepthWallpaper()
            }
        })

        hookAllMethods(canvasEngineClass, "onSurfaceDestroyed", object : XC_MethodHook() {
            // lockscreen wallpaper changed
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (showDepthWallpaper && !showCustomImages && isLockScreenWallpaper(param.thisObject)) {
                    invalidateCache()
                }
            }
        })

        hookAllMethods(canvasEngineClass, "onCreate", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (callMethod(
                        getObjectField(
                            param.thisObject,
                            "mWallpaperManager"
                        ),
                        "getWallpaperInfo",
                        WallpaperManager.FLAG_LOCK
                    ) != null && !showCustomImages
                ) { // it's live wallpaper. we can't use that
                    invalidateCache()
                }
            }
        })

        hookAllMethods(canvasEngineClass, "drawFrameOnCanvas", object : XC_MethodHook() {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (showDepthWallpaper && !showCustomImages && isLockScreenWallpaper(param.thisObject)) {
                    val wallpaperBitmap = Bitmap.createBitmap((param.args[0] as Bitmap))

                    val cacheIsValid: Boolean = assertCache(wallpaperBitmap)

                    val displayBounds = (callMethod(
                        param.thisObject,
                        "getDisplayContext"
                    ) as Context)
                        .getSystemService(
                            WindowManager::class.java
                        )
                        .currentWindowMetrics
                        .bounds

                    val ratioW = 1f * displayBounds.width() / wallpaperBitmap.width
                    val ratioH = 1f * displayBounds.height() / wallpaperBitmap.height

                    val desiredHeight = Math.round(
                        max(
                            ratioH.toDouble(),
                            ratioW.toDouble()
                        ) * wallpaperBitmap.height
                    ).toInt()
                    val desiredWidth = Math.round(
                        max(
                            ratioH.toDouble(),
                            ratioW.toDouble()
                        ) * wallpaperBitmap.width
                    ).toInt()

                    val xPixelShift = (desiredWidth - displayBounds.width()) / 2
                    val yPixelShift = (desiredHeight - displayBounds.height()) / 2

                    var scaledWallpaperBitmap = Bitmap.createScaledBitmap(
                        wallpaperBitmap,
                        desiredWidth,
                        desiredHeight,
                        true
                    )

                    // crop to display bounds
                    scaledWallpaperBitmap = Bitmap.createBitmap(
                        scaledWallpaperBitmap,
                        xPixelShift,
                        yPixelShift,
                        displayBounds.width(),
                        displayBounds.height()
                    )
                    val finalScaledWallpaperBitmap = scaledWallpaperBitmap

                    if (!mLayersCreated) {
                        createLayers()
                    }

                    mWallpaperBackground!!.post {
                        mWallpaperBitmapContainer!!.background = BitmapDrawable(
                            mContext.resources,
                            finalScaledWallpaperBitmap
                        )
                        if (mScrimController != null) {
                            mWallpaperDimmingOverlay!!.setBackgroundColor(Color.BLACK)
                            mWallpaperDimmingOverlay!!.alpha = getFloatField(
                                mScrimController,
                                "mScrimBehindAlphaKeyguard"
                            )
                        }
                    }

                    if (!cacheIsValid) {
                        val callback = object : IExtractSubjectCallback.Stub() {
                            override fun onStart(message: String) {
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onResult(success: Boolean, message: String) {
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        enqueueProxyCommand { proxy ->
                            proxy?.extractSubject(
                                finalScaledWallpaperBitmap,
                                foregroundPath,
                                callback
                            )
                        }
                    }
                }
            }
        })

        hookAllConstructors(scrimControllerClass, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                mScrimController = param.thisObject
            }
        })

        hookAllMethods(scrimControllerClass, "applyAndDispatchState", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                setDepthWallpaper()
            }
        })

        hookAllMethods(qsImplClass, "setQsExpansion", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (callMethod(param.thisObject, "isKeyguardState") as Boolean) {
                    setDepthWallpaper()
                }
            }
        })

        /*
         * Custom depth wallpaper images
         */
        val keyguardBottomAreaViewClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.KeyguardBottomAreaView",
            loadPackageParam.classLoader
        )

        hookAllMethods(
            keyguardBottomAreaViewClass,
            "onConfigurationChanged",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    setCustomDepthWallpaper()
                }
            })

        setCustomDepthWallpaper()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun assertCache(wallpaperBitmap: Bitmap): Boolean {
        var cacheIsValid = false

        try {
            val wallpaperCacheFile = File(backgroundPath)

            val compressedBitmap = ByteArrayOutputStream()
            wallpaperBitmap.compress(Bitmap.CompressFormat.JPEG, 100, compressedBitmap)
            if (wallpaperCacheFile.exists()) {
                val cacheStream = FileInputStream(wallpaperCacheFile)

                if (cacheStream.readAllBytes().contentEquals(compressedBitmap.toByteArray())) {
                    cacheIsValid = true
                } else {
                    val newCacheStream = FileOutputStream(wallpaperCacheFile)
                    compressedBitmap.writeTo(newCacheStream)
                    newCacheStream.close()
                }
                cacheStream.close()
            }
            compressedBitmap.close()
        } catch (ignored: Throwable) {
        }

        if (!cacheIsValid) {
            invalidateCache()
        }

        return cacheIsValid
    }

    private fun createLayers() {
        mWallpaperBackground = FrameLayout(mContext)
        mWallpaperDimmingOverlay = FrameLayout(mContext)
        mWallpaperBitmapContainer = FrameLayout(mContext)
        val layoutParams = FrameLayout.LayoutParams(-1, -1)

        mWallpaperDimmingOverlay!!.setBackgroundColor(
            if (File(backgroundPath).exists()) {
                Color.BLACK
            } else {
                Color.TRANSPARENT
            }
        )
        mWallpaperDimmingOverlay!!.layoutParams = layoutParams
        mWallpaperBitmapContainer!!.setLayoutParams(layoutParams)

        mWallpaperBackground!!.addView(mWallpaperBitmapContainer)
        mWallpaperBackground!!.addView(mWallpaperDimmingOverlay)
        mWallpaperBackground!!.setLayoutParams(layoutParams)

        mWallpaperForeground = FrameLayout(mContext)
        mWallpaperForeground!!.setLayoutParams(layoutParams)

        mLayersCreated = true
    }

    private fun isLockScreenWallpaper(canvasEngine: Any): Boolean {
        return ((getWallpaperFlag(canvasEngine) and WallpaperManager.FLAG_LOCK) == WallpaperManager.FLAG_LOCK)
    }

    private fun setDepthWallpaper() {
        if (mScrimController == null) return

        val state = getObjectField(mScrimController, "mState").toString()
        val showForeground = (showDepthWallpaper &&
                (state == "KEYGUARD" || (showOnAOD && (state == "AOD" || state == "PULSING"))))

        if (showForeground) {
            if (!mWallpaperForegroundCacheValid && File(foregroundPath).exists()) {
                try {
                    FileInputStream(foregroundPath).use { inputStream ->
                        val bitmapDrawable = BitmapDrawable.createFromStream(
                            inputStream,
                            ""
                        )
                        bitmapDrawable!!.alpha = 255

                        mForegroundDimmingOverlay = bitmapDrawable.constantState!!
                            .newDrawable().mutate()
                        mForegroundDimmingOverlay!!.setTint(Color.BLACK)

                        mWallpaperForeground!!.background = LayerDrawable(
                            arrayOf(
                                bitmapDrawable,
                                mForegroundDimmingOverlay
                            )
                        )
                        mWallpaperForegroundCacheValid = true
                    }
                } catch (ignored: Throwable) {
                }
            }

            if (mWallpaperForegroundCacheValid) {
                mWallpaperForeground!!.background.alpha = (foregroundAlpha * 255).toInt()

                if (state != "KEYGUARD") { // AOD
                    mForegroundDimmingOverlay!!.alpha = 192
                } else {
                    // this is the dimmed wallpaper coverage
                    mForegroundDimmingOverlay!!.alpha = Math.round(
                        getFloatField(
                            mScrimController,
                            "mScrimBehindAlphaKeyguard"
                        ) * 240
                    ) // A tad bit lower than max. show it a bit lighter than other stuff

                    mWallpaperDimmingOverlay!!.alpha = getFloatField(
                        mScrimController,
                        "mScrimBehindAlphaKeyguard"
                    )
                }

                mWallpaperBackground!!.visibility = View.VISIBLE
                mWallpaperForeground!!.visibility = View.VISIBLE
            }
        } else if (mLayersCreated) {
            mWallpaperForeground!!.visibility = View.GONE

            if (state == "UNLOCKED") {
                mWallpaperBackground!!.visibility = View.GONE
            }
        }
    }

    private fun getWallpaperFlag(canvasEngine: Any): Int {
        return callMethod(canvasEngine, "getWallpaperFlags") as Int
    }

    private fun invalidateCache() { // invalidate lock screen wallpaper subject cache
        mWallpaperForegroundCacheValid = false

        if (mLayersCreated) {
            mWallpaperForeground!!.post {
                mWallpaperForeground!!.visibility = View.GONE
                mWallpaperForeground!!.background = null
                mWallpaperBackground!!.visibility = View.GONE
                mWallpaperBitmapContainer!!.background = null
            }
        }

        try {
            if (File(foregroundPath).exists()) {
                File(foregroundPath).delete()
            }
        } catch (ignored: Throwable) {
        }
    }

    /*
     * Custom depth wallpaper images
     */
    private fun setCustomDepthWallpaper() {
        if (!showDepthWallpaper || !showCustomImages) return

        if (!mLayersCreated) {
            createLayers()
        }

        try {
            val mainHandler = Handler(Looper.getMainLooper())
            val executor = Executors.newSingleThreadScheduledExecutor()

            executor.scheduleAtFixedRate({
                val androidDir =
                    File(Environment.getExternalStorageDirectory().toString() + "/Android")

                if (androidDir.isDirectory()) {
                    mainHandler.post {
                        try {
                            if (File(backgroundPath).exists()) {
                                FileInputStream(backgroundPath).use { inputStream ->
                                    val bitmapDrawable = BitmapDrawable.createFromStream(
                                        inputStream,
                                        ""
                                    )
                                    bitmapDrawable!!.alpha = 255

                                    mWallpaperBackground!!.post {
                                        mWallpaperBitmapContainer!!.background = bitmapDrawable

                                        if (mScrimController != null) {
                                            mWallpaperDimmingOverlay!!.setBackgroundColor(Color.BLACK)
                                            mWallpaperDimmingOverlay!!.alpha = getFloatField(
                                                mScrimController,
                                                "mScrimBehindAlphaKeyguard"
                                            )
                                        }

                                        mWallpaperBackground!!.visibility = View.VISIBLE
                                    }
                                }
                            }
                        } catch (ignored: Throwable) {
                        }

                        // this sets the dimmed foreground wallpaper
                        setDepthWallpaper()
                    }

                    executor.shutdown()
                    executor.shutdownNow()
                }
            }, 0, 5, TimeUnit.SECONDS)
        } catch (ignored: Throwable) {
        }
    }

    companion object {
        private val TAG = "Iconify - ${DepthWallpaperA14::class.java.simpleName}: "
    }
}