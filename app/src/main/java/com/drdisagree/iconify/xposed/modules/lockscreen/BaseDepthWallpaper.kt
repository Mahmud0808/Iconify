package com.drdisagree.iconify.xposed.modules.lockscreen

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.ACTION_EXTRACT_FAILURE
import com.drdisagree.iconify.data.common.Const.ACTION_EXTRACT_SUBJECT
import com.drdisagree.iconify.data.common.Const.ACTION_EXTRACT_SUCCESS
import com.drdisagree.iconify.data.common.Const.AI_PLUGIN_PACKAGE
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_DEPTH_WALLPAPER_BACKGROUND_TAG
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_DEPTH_WALLPAPER_FOREGROUND_TAG
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_LOCKSCREEN_CLOCK_TAG
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_LOCKSCREEN_CONTAINER_TAG
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_LOCKSCREEN_WEATHER_TAG
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_LOCKSCREEN_WIDGET_TAG
import com.drdisagree.iconify.data.common.XposedConst.DEPTH_WALL_BG_FILE
import com.drdisagree.iconify.data.common.XposedConst.DEPTH_WALL_FG_FILE
import com.drdisagree.iconify.data.common.XposedConst.DEPTH_WALL_KEY_FILE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.callbacks.AlbumArtCallback
import com.drdisagree.iconify.xposed.modules.extras.callbacks.BootCallback
import com.drdisagree.iconify.xposed.modules.extras.callbacks.DozeCallback
import com.drdisagree.iconify.xposed.modules.extras.callbacks.KeyguardShowingCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ComposeViewHost
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.LockscreenSceneInjector
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.findChildIndexContainsTag
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.findViewContainingTag
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setExtraField
import com.drdisagree.iconify.xposed.modules.lockscreen.AlbumArt.Companion.shouldShowAlbumArt
import com.drdisagree.iconify.xposed.utils.SceneContainer
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.util.zip.CRC32
import kotlin.math.max
import kotlin.math.roundToInt

@SuppressLint("DiscouragedApi")
abstract class BaseDepthWallpaper(context: Context) : ModPack(context) {

    private var showDepthWallpaper = false
    private var showLockscreenClock = false
    private var showCustomImages = false
    private var foregroundAlpha = 1.0f
    private var mPreviousState: String? = null
    private var mScrimController: Any? = null
    private var mForegroundDimmingOverlay: Drawable? = null
    private lateinit var mWallpaperForeground: FrameLayout
    private lateinit var mWallpaperBackground: FrameLayout
    private lateinit var mWallpaperBitmapContainer: FrameLayout
    private lateinit var mWallpaperDimmingOverlay: FrameLayout
    private var mWallpaperForegroundCacheValid = false
    private var mLayersCreated = false
    private var showOnAOD = true
    private var keepLockScreenShade = true
    protected var mAiMode = 0
    private var mPluginReceiverRegistered = false
    private lateinit var mPluginReceiver: BroadcastReceiver
    private var wallpaperProcessorThread: Thread? = null
    private val lsItemTags = listOf(
        ICONIFY_LOCKSCREEN_CONTAINER_TAG,
        ICONIFY_LOCKSCREEN_CLOCK_TAG,
        ICONIFY_LOCKSCREEN_WEATHER_TAG,
        ICONIFY_LOCKSCREEN_WIDGET_TAG
    )

    private var shouldShowForeground = true
    private var wallpaperZoom = 0f
    private var wallpaperDimAmount = 0f
    private var revealScrimRef: WeakReference<View>? = null
    private var behindScrimViewModelRef: WeakReference<Any>? = null
    private var bakedWallpaperPlacement: RectF? = null
    private var currentWallpaperPlacement: RectF? = null
    private var wallpaperCrop: Rect? = null
    private var wallpaperBitmapSize: Point? = null
    private var wallpaperZoomsOut = true
    private val wallpaperMaxScale: Float by lazy {
        val resources = Resources.getSystem()
        val id = resources.getIdentifier("config_wallpaperMaxScale", "dimen", FRAMEWORK_PACKAGE)
        if (id == 0) 1f
        else runCatching { resources.getFloat(id) }.getOrDefault(1f).coerceAtLeast(1f)
    }
    private var composeForegroundHost: FrameLayout? = null
    private var composeBackgroundHost: FrameLayout? = null
    private var isDozing = false
    private var shouldShowBackground = true
    private val albumArtVisibilityListener = AlbumArtCallback.AlbumArtVisibilityListener {
        updateForegroundVisibility()
    }

    abstract fun handleSubjectExtraction(scaledWallpaper: Bitmap? = null)

    protected fun onSubjectExtracted() {
        mWallpaperForegroundCacheValid = false
        Handler(Looper.getMainLooper()).post { setDepthWallpaper() }
    }

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            showDepthWallpaper = getBoolean(XposedKey.LOCKSCREEN_DEPTH_WALLPAPER)
            showLockscreenClock = getBoolean(XposedKey.CUSTOM_LOCKSCREEN_CLOCK)
            showCustomImages = getBoolean(XposedKey.DEPTH_WALLPAPER_CUSTOM_IMAGE)
            foregroundAlpha = getInt(XposedKey.DEPTH_WALLPAPER_FOREGROUND_IMAGE_OPACITY) / 100.0f
            showOnAOD = getBoolean(XposedKey.DEPTH_WALLPAPER_SHOW_ON_AOD)
            keepLockScreenShade = getBoolean(XposedKey.LOCKSCREEN_SHADE)
            mAiMode = getString(XposedKey.DEPTH_WALLPAPER_AI_MODE).toInt()
        }

        when (key.firstOrNull()) {
            XposedKey.LOCKSCREEN_DEPTH_WALLPAPER.name -> setCustomDepthWallpaper()

            XposedKey.DEPTH_WALLPAPER_CUSTOM_IMAGE.name -> {
                if (!showCustomImages) invalidateCache()
                setCustomDepthWallpaper()
                applyWallpaperScale()
            }

            XposedKey.DEPTH_WALLPAPER_BACKGROUND_IMAGE_FILE_URI.name,
            XposedKey.DEPTH_WALLPAPER_FOREGROUND_IMAGE_FILE_URI.name -> {
                mWallpaperForegroundCacheValid = false
                setCustomDepthWallpaper()
            }
        }

        if (SceneContainer.isEnabled && key.isNotEmpty()) setDepthWallpaper()
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        // Listen for album art visibility changes
        AlbumArtCallback.registerVisibilityListener(albumArtVisibilityListener)

        mPluginReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null) {
                    when (intent.action) {
                        ACTION_EXTRACT_SUCCESS -> {
                            onSubjectExtracted()
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(
                                    mContext,
                                    modRes.getString(R.string.depth_wallpaper_subject_extraction_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        ACTION_EXTRACT_FAILURE -> {
                            mWallpaperForegroundCacheValid = false
                            log(
                                this@BaseDepthWallpaper,
                                "Subject extraction failed\n${intent.getStringExtra("error")}"
                            )

                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(
                                    mContext,
                                    modRes.getString(R.string.depth_wallpaper_subject_extraction_failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }

        if (!mPluginReceiverRegistered) {
            IntentFilter().apply {
                addAction(ACTION_EXTRACT_SUCCESS)
                addAction(ACTION_EXTRACT_FAILURE)
            }.also { intentFilter ->
                mContext.registerReceiver(
                    mPluginReceiver,
                    intentFilter,
                    Context.RECEIVER_EXPORTED
                )
            }
            mPluginReceiverRegistered = true
        }

        val canvasEngineClass =
            findClass($$"$$SYSTEMUI_PACKAGE.wallpapers.ImageWallpaper$CanvasEngine")
        val scrimControllerClass = findClass("$SYSTEMUI_PACKAGE.statusbar.phone.ScrimController")

        canvasEngineClass
            .hookMethod("onSurfaceDestroyed")
            .runAfter { param ->
                // lockscreen wallpaper changed
                if (showDepthWallpaper && !showCustomImages && isLockScreenWallpaper(param.thisObject)) {
                    invalidateCache()
                }
            }

        canvasEngineClass
            .hookMethod("onCreate")
            .runAfter { param ->
                if (param.thisObject
                        .getField("mWallpaperManager")
                        .callMethod(
                            "getWallpaperInfo",
                            WallpaperManager.FLAG_LOCK
                        ) != null && !showCustomImages
                ) { // it's live wallpaper. we can't use that
                    invalidateCache()
                }
            }

        findClass($$"android.service.wallpaper.WallpaperService$Engine")
            .hookMethod("setZoom")
            .suppressError()
            .runAfter { param ->
                val engine = param.thisObject
                if (canvasEngineClass?.isInstance(engine) != true) return@runAfter
                if (!isLockScreenWallpaper(engine)) return@runAfter

                updateWallpaperZoom(engine)
            }

        findClass($$"android.service.wallpaper.WallpaperService$Engine")
            .hookMethod("doOffsetsChanged")
            .suppressError()
            .runAfter { param ->
                val engine = param.thisObject
                if (canvasEngineClass?.isInstance(engine) != true) return@runAfter
                if (!isLockScreenWallpaper(engine)) return@runAfter

                currentWallpaperPlacement = wallpaperPlacement(engine, displayBoundsOf(engine))
                applyWallpaperScale()
            }

        canvasEngineClass
            .hookMethod("drawFrameOnCanvas")
            .runAfter { param ->
                wallpaperProcessorThread?.interrupt()

                if (isLockScreenWallpaper(param.thisObject)) {
                    updateWallpaperZoom(param.thisObject)
                }

                if (showDepthWallpaper && !showCustomImages && isLockScreenWallpaper(param.thisObject)) {
                    wallpaperProcessorThread = Thread {
                        val wallpaperBitmap = Bitmap.createBitmap((param.args[0] as Bitmap))
                        val displayBounds = displayBoundsOf(param.thisObject)
                        wallpaperBitmapSize = Point(wallpaperBitmap.width, wallpaperBitmap.height)
                        wallpaperCrop = queryWallpaperCrop(param.thisObject, displayBounds)
                        val placement = wallpaperPlacement(param.thisObject, displayBounds)
                        val cacheIsValid: Boolean = assertCache(wallpaperBitmap, placement)

                        bakedWallpaperPlacement = placement
                        currentWallpaperPlacement = placement
                        applyWallpaperScale()

                        val scaledWallpaperBitmap = if (placement != null) {
                            renderAtPlacement(wallpaperBitmap, placement, displayBounds)
                        } else {
                            centerCrop(wallpaperBitmap, displayBounds)
                        }

                        try {
                            DEPTH_WALL_BG_FILE.parentFile?.mkdirs()
                            val out = FileOutputStream(DEPTH_WALL_BG_FILE)
                            scaledWallpaperBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            out.flush()
                            out.close()
                        } catch (throwable: IOException) {
                            log(this@BaseDepthWallpaper, throwable)
                        }

                        if (!mLayersCreated) {
                            createLayers()
                        }

                        mWallpaperBackground.post {
                            mWallpaperBitmapContainer.background =
                                scaledWallpaperBitmap.toDrawable(mContext.resources)
                            if (mScrimController != null) {
                                mWallpaperDimmingOverlay.setBackgroundColor(Color.BLACK)
                                mWallpaperDimmingOverlay.alpha = backgroundDimAlpha()
                            }
                        }

                        if (!cacheIsValid) {
                            handleSubjectExtraction(scaledWallpaperBitmap)

                            Thread.sleep(500)
                            if (DEPTH_WALL_FG_FILE.exists()) {
                                setDepthWallpaper()
                            }
                        }

                        wallpaperProcessorThread = null
                    }

                    wallpaperProcessorThread?.start()
                }
            }

        scrimControllerClass
            .hookConstructor()
            .runAfter { param -> mScrimController = param.thisObject }

        if (SceneContainer.isEnabled) {
            hookComposeLockscreen()
        } else {
            hookLegacyLockscreen()
        }

        setCustomDepthWallpaper()
    }

    private fun hookComposeLockscreen() {
        DozeCallback.getInstance().registerDozeChangeListener(object : DozeCallback.DozeListener {
            override fun onDozingStarted() {
                isDozing = true
                setDepthWallpaper()
            }

            override fun onDozingStopped() {
                isDozing = false
                setDepthWallpaper()
            }
        })

        findClass("$SYSTEMUI_PACKAGE.keyguard.ui.viewmodel.LockscreenBehindScrimViewModel")
            .hookConstructor()
            .suppressError()
            .runAfter { param -> behindScrimViewModelRef = WeakReference(param.thisObject) }

        findClass("$SYSTEMUI_PACKAGE.keyguard.ui.composable.LockscreenBehindScrimKt")
            .hookMethod("LockscreenBehindScrim")
            .suppressError()
            .runBefore { param ->
                val viewModel = param.args[0] ?: return@runBefore
                if (behindScrimViewModelRef?.get() !== viewModel) {
                    behindScrimViewModelRef = WeakReference(viewModel)
                }
            }

        val legacyRevealScrimId =
            mContext.resources.getIdentifier("light_reveal_scrim", "id", SYSTEMUI_PACKAGE)

        findClass("$SYSTEMUI_PACKAGE.statusbar.LightRevealScrim")
            .hookMethod("setRevealAmount")
            .suppressError()
            .runAfter { param ->
                val scrim = param.thisObject as View
                if (legacyRevealScrimId != 0 && scrim.id == legacyRevealScrimId) return@runAfter

                revealScrimRef = WeakReference(scrim)
                composeForegroundHost?.invalidate()
            }

        LockscreenSceneInjector.addBehindContent(
            order = -1,
            key = BACKGROUND_GROUP_KEY,
            isEnabled = { showDepthWallpaper }
        ) { context -> createComposeHost(context, isForeground = false) }

        LockscreenSceneInjector.addAboveClockRegion(
            key = FOREGROUND_GROUP_KEY,
            isEnabled = { showDepthWallpaper },
            modifier = { ComposeViewHost.windowFillModifier { composeWindowSize() } }
        ) { context -> createComposeHost(context, isForeground = true) }
    }

    private fun createComposeHost(context: Context, isForeground: Boolean): View {
        val host = object : FrameLayout(context) {
            override fun dispatchDraw(canvas: Canvas) {
                super.dispatchDraw(canvas)
                if (isForeground) drawRevealScrim(this, canvas)
            }
        }.apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }

        host.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                if (!mLayersCreated) createLayers()

                if (isForeground) {
                    composeForegroundHost = host
                    host.reAddView(mWallpaperForeground)
                } else {
                    composeBackgroundHost = host
                    host.reAddView(mWallpaperBackground)
                }

                setDepthWallpaper()
            }

            override fun onViewDetachedFromWindow(v: View) {
                if (isForeground) {
                    if (composeForegroundHost === host) composeForegroundHost = null
                } else if (composeBackgroundHost === host) {
                    composeBackgroundHost = null
                }
            }
        })

        if (isForeground) {
            var lastLocation = 0 to 0
            var lastSize = 0 to 0

            ComposeViewHost.runBeforeEachDraw(host) {
                if (mLayersCreated && shouldShowForeground) updateComposeForegroundDim()

                val location = IntArray(2).also { host.getLocationInWindow(it) }
                val current = location[0] to location[1]
                val size = composeWindowSize()

                if (current != lastLocation || size != lastSize) {
                    lastLocation = current
                    lastSize = size
                    host.requestLayout()
                }
            }
        }

        return host
    }

    private fun composeWindowSize(): Pair<Int, Int> {
        composeForegroundHost?.rootView
            ?.takeIf { it.width > 0 && it.height > 0 }
            ?.let { return it.width to it.height }

        val bounds = mContext.getSystemService(WindowManager::class.java)
            .currentWindowMetrics
            .bounds
        return bounds.width() to bounds.height()
    }

    private fun updateComposeForegroundDim() {
        val overlay = mForegroundDimmingOverlay ?: return
        val alpha = if (isDozing) 192 else (foregroundDimAlpha(systemWallpaperDim()) * 255).roundToInt()
        if (overlay.alpha != alpha) overlay.alpha = alpha
    }

    private fun foregroundDimAlpha(dim: Float): Float {
        if (dim <= 0f) return 0f

        val layerAlpha = foregroundAlpha.coerceIn(0f, 1f)
        val dimmed = 1f - dim.coerceIn(0f, 1f)
        return (1f - dimmed / (layerAlpha + (1f - layerAlpha) * dimmed)).coerceIn(0f, 1f)
    }

    private fun systemWallpaperDim(): Float {
        val viewModel = behindScrimViewModelRef?.get()
        val scrimVisible = (viewModel
            ?.getFieldSilently("isVisible\$delegate")
            ?.callMethodSilently("getValue") as? Boolean) ?: true
        val scrimAlpha = if (scrimVisible) {
            val dimAmount = (viewModel
                ?.getFieldSilently("aodDimInteractor")
                ?.getFieldSilently("repository")
                ?.getFieldSilently("wallpaperDimAmount\$delegate")
                ?.callMethodSilently("getFloatValue") as? Float) ?: 0f
            val dimAlpha = (dimAmount.coerceIn(0f, 1f) * 255).toInt()
            (255 - (255 - LOCKSCREEN_BEHIND_SCRIM_ALPHA) * (255 - dimAlpha) / 255) / 255f
        } else {
            0f
        }

        return 1f - (1f - scrimAlpha) * (1f - wallpaperDimAmount.coerceIn(0f, 1f))
    }

    private fun drawRevealScrim(host: View, canvas: Canvas) {
        if (isDozing) return

        val scrim = revealScrimRef?.get()?.takeIf { it.isAttachedToWindow && it.isShown } ?: return
        val revealAmount = (scrim.getFieldSilently("revealAmount") as? Float) ?: return
        if (revealAmount >= 1f || scrim.alpha <= 0f) return

        val hostLocation = IntArray(2).also { host.getLocationInWindow(it) }
        val scrimLocation = IntArray(2).also { scrim.getLocationInWindow(it) }
        val saveCount = canvas.saveLayerAlpha(
            0f,
            0f,
            host.width.toFloat(),
            host.height.toFloat(),
            (scrim.alpha * 255).roundToInt()
        )
        canvas.translate(
            (scrimLocation[0] - hostLocation[0]).toFloat(),
            (scrimLocation[1] - hostLocation[1]).toFloat()
        )
        canvas.clipRect(0, 0, scrim.width, scrim.height)
        scrim.draw(canvas)
        canvas.restoreToCount(saveCount)
    }

    private fun keyguardScrimAlpha(): Float = try {
        mScrimController?.getField("mScrimBehindAlphaKeyguard") as? Float
    } catch (_: Throwable) {
        null
    } ?: DEFAULT_KEYGUARD_SCRIM_ALPHA

    private fun backgroundDimAlpha(): Float =
        if (SceneContainer.isEnabled) 0f else keyguardScrimAlpha()

    private fun updateComposeDepthWallpaper() {
        if (!mLayersCreated) return

        if (Looper.myLooper() != Looper.getMainLooper()) {
            mWallpaperForeground.post { updateComposeDepthWallpaper() }
            return
        }

        val onLockscreen = composeForegroundHost != null || composeBackgroundHost != null
        if (showDepthWallpaper && onLockscreen) loadForegroundDrawable()

        val hasForeground = mWallpaperForegroundCacheValid && mWallpaperForeground.background != null

        mWallpaperDimmingOverlay.alpha = 0f
        mWallpaperBackground.visibility =
            if (showDepthWallpaper && showCustomImages && hasForeground && composeBackgroundHost != null && !isDozing) {
                View.VISIBLE
            } else {
                View.GONE
            }

        if (hasForeground) {
            mWallpaperForeground.alpha = 1f
            mWallpaperForeground.background.alpha = (foregroundAlpha * 255).toInt()
            updateComposeForegroundDim()
        }

        shouldShowForeground = showDepthWallpaper && hasForeground &&
                composeForegroundHost != null && (!isDozing || showOnAOD)
        updateForegroundVisibility()
    }

    private fun loadForegroundDrawable() {
        if ((mWallpaperForegroundCacheValid && mWallpaperForeground.background != null) ||
            !DEPTH_WALL_FG_FILE.exists()
        ) return

        try {
            FileInputStream(DEPTH_WALL_FG_FILE).use { inputStream ->
                val bitmapDrawable = BitmapDrawable.createFromStream(inputStream, "")
                bitmapDrawable!!.alpha = 255

                mForegroundDimmingOverlay =
                    bitmapDrawable.constantState!!.newDrawable().mutate()
                mForegroundDimmingOverlay!!.setTint(Color.BLACK)

                mWallpaperForeground.background = LayerDrawable(
                    arrayOf(bitmapDrawable, mForegroundDimmingOverlay)
                )
                mWallpaperForegroundCacheValid = true
            }
        } catch (_: Throwable) {
        }
    }

    private fun hookLegacyLockscreen() {
        val qsImplClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.QSImpl",
            "$SYSTEMUI_PACKAGE.qs.QSFragment",
            "$SYSTEMUI_PACKAGE.qs.composefragment.QSFragmentCompose"
        )
        val notificationPanelViewControllerClass =
            findClass("$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController")
        val scrimViewClass = findClass("$SYSTEMUI_PACKAGE.scrim.ScrimView")
        val statusBarKeyguardViewManagerClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.StatusBarKeyguardViewManager")
        val aodBurnInSectionClass =
            findClass("$SYSTEMUI_PACKAGE.keyguard.ui.view.layout.sections.AodBurnInSection")
        val scrimControllerClass = findClass("$SYSTEMUI_PACKAGE.statusbar.phone.ScrimController")

        aodBurnInSectionClass
            .hookMethod("addViews")
            .runAfter { param ->
                if (!showDepthWallpaper) return@runAfter

                val entryV = param.args[0] as View

                entryV.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        viewAttached(entryV)
                    }

                    override fun onViewDetachedFromWindow(v: View) {}
                })

                if (entryV.isAttachedToWindow) {
                    viewAttached(entryV)
                }
            }

        scrimViewClass
            .hookMethod("setViewAlpha")
            .runBefore { param ->
                if (!mLayersCreated) return@runBefore

                setDepthWallpaper()

                if (showOnAOD && mScrimController.getField("mState").toString() != "KEYGUARD") {
                    mWallpaperForeground.post { mWallpaperForeground.alpha = foregroundAlpha }
                } else if (mScrimController.getField("mNotificationsScrim") == param.thisObject) { // instead of using the mScrimName since older ones don't have that field
                    val mScrimBehindAlphaKeyguard = mScrimController.getField(
                        "mScrimBehindAlphaKeyguard"
                    ) as Float

                    var notificationAlpha = param.args[0] as Float

                    if (notificationAlpha < mScrimBehindAlphaKeyguard) {
                        notificationAlpha = 0f
                    }

                    val foregroundAlpha = if (notificationAlpha > mScrimBehindAlphaKeyguard) {
                        (1f - notificationAlpha) / (1f - mScrimBehindAlphaKeyguard)
                    } else {
                        1f
                    }

                    mWallpaperForeground.post { mWallpaperForeground.alpha = foregroundAlpha }
                }
            }

        statusBarKeyguardViewManagerClass
            .hookMethod("onStartedWakingUp")
            .suppressError()
            .runAfter { setDepthWallpaper() }

        notificationPanelViewControllerClass
            .hookConstructor()
            .runAfter { param ->
                mScrimController = param.thisObject.getField("mScrimController")
            }

        notificationPanelViewControllerClass
            .hookMethod(
                "onFinishInflate",
                "reInflateViews"
            )
            .runAfter { param ->
                mScrimController = param.thisObject.getField("mScrimController")
            }

        scrimControllerClass
            .hookMethod("applyAndDispatchState")
            .runAfter { setDepthWallpaper() }

        qsImplClass
            .hookMethod("setQsExpansion")
            .runAfter { param ->
                val isKeyguardState =
                    param.thisObject.callMethodSilently("isKeyguardState") as? Boolean
                        ?: KeyguardShowingCallback.getInstance().isKeyguardShowing

                if (isKeyguardState) {
                    setDepthWallpaper()
                }
            }

        KeyguardShowingCallback.getInstance().registerKeyguardShowingListener(
            object : KeyguardShowingCallback.KeyguardShowingListener {
                override fun onKeyguardShown() {
                    shouldShowBackground = true
                }

                override fun onKeyguardDismissed() {
                    shouldShowForeground = false
                    shouldShowBackground = false

                    updateForegroundVisibility()
                    setDepthWallpaper()
                }
            }
        )
    }

    fun viewAttached(entryV: View) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (!showDepthWallpaper) return@postDelayed

            val rootView = (entryV.parent as? ViewGroup)
                ?.rootView
                ?.findViewById<ViewGroup>(
                    mContext.resources.getIdentifier(
                        "keyguard_root_view",
                        "id",
                        mContext.packageName
                    )
                ) ?: return@postDelayed

            rootView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                override fun onChildViewAdded(p0: View?, p1: View?) {
                    hookClocks(rootView)
                }

                override fun onChildViewRemoved(p0: View?, p1: View?) {}
            })
            hookClocks(rootView.rootView);

            if (!mLayersCreated) {
                createLayers()
            }

            val idx = lsItemTags
                .map { rootView.findChildIndexContainsTag(it) }
                .firstOrNull { it != -1 }
                ?.plus(1)
                ?: 0

            (rootView.rootView as ViewGroup).reAddView(mWallpaperBackground, 0)
            rootView.reAddView(mWallpaperForeground, idx)
        }, 1000)
    }

    fun hookClocks(rootView: View) {
        val keyguardRoot: ViewGroup = rootView.findViewById(
            mContext.resources.getIdentifier(
                "keyguard_root_view",
                "id",
                mContext.packageName
            )
        )

        for (i in 0..<keyguardRoot.childCount) {
            val child = keyguardRoot.getChildAt(i)

            if (child.javaClass.name.startsWith("com.android.systemui.clocks") ||
                child.javaClass.name.startsWith("com.android.systemui.shared.clocks.view")
            ) {
                child.z = -5f
                child.setExtraField("DepthHooked", true)
                child.addOnAttachStateChangeListener(object :
                    OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(p0: View) {
                        p0.z = -5f
                    }

                    override fun onViewDetachedFromWindow(p0: View) {}
                })
                child.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
                    v.z = -5f
                }
            }
        }
    }

    private fun updateForegroundVisibility(targetAlpha: Float = 1f, duration: Long = 0L) {
        if (::mWallpaperForeground.isInitialized.not()) return

        // Hide foreground when album art is showing
        if (showDepthWallpaper && shouldShowForeground && !shouldShowAlbumArt) {
            // Smooth appearance
            if (duration == 0L) {
                mWallpaperForeground.visibility = View.VISIBLE
            } else {
                mWallpaperForeground.apply {
                    if (visibility != View.VISIBLE) {
                        visibility = View.VISIBLE
                        alpha = 0f
                    }
                    animate()
                        .alpha(targetAlpha)
                        .setDuration(duration)
                        .start()
                }
            }
        } else {
            mWallpaperForeground.visibility = View.GONE
        }
    }

    protected fun sendPluginIntent() {
        try {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    mContext,
                    modRes.getString(R.string.depth_wallpaper_subject_extraction_started),
                    Toast.LENGTH_LONG
                ).show()
            }

            mContext.sendBroadcast(
                Intent(ACTION_EXTRACT_SUBJECT).apply {
                    component = ComponentName(
                        AI_PLUGIN_PACKAGE,
                        "$AI_PLUGIN_PACKAGE.receivers.SubjectExtractionReceiver"
                    )
                    putExtra("sourcePath", DEPTH_WALL_BG_FILE.absolutePath)
                    putExtra("destinationPath", DEPTH_WALL_FG_FILE.absolutePath)
                    setPackage(mContext.packageName)
                    addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                }
            )
        } catch (_: Throwable) {
        }
    }

    private fun assertCache(wallpaperBitmap: Bitmap, placement: RectF?): Boolean {
        val key = try {
            wallpaperKey(wallpaperBitmap, placement)
        } catch (_: Throwable) {
            null
        }

        val cacheIsValid = key != null && try {
            DEPTH_WALL_FG_FILE.exists() &&
                    DEPTH_WALL_KEY_FILE.exists() &&
                    DEPTH_WALL_KEY_FILE.readText() == key
        } catch (_: Throwable) {
            false
        }

        if (!cacheIsValid) {
            invalidateCache()

            if (key != null) {
                try {
                    DEPTH_WALL_KEY_FILE.parentFile?.mkdirs()
                    DEPTH_WALL_KEY_FILE.writeText(key)
                } catch (_: Throwable) {
                }
            }
        }

        return cacheIsValid
    }

    private fun wallpaperKey(bitmap: Bitmap, placement: RectF?): String {
        val source = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }
        val buffer = ByteBuffer.allocate(source.byteCount)
        source.copyPixelsToBuffer(buffer)
        val checksum = CRC32().apply { update(buffer.array()) }.value

        val placementKey = placement?.let {
            "${it.left.roundToInt()},${it.top.roundToInt()},${it.width().roundToInt()}x${it.height().roundToInt()}"
        } ?: "center"

        return "${source.width}x${source.height}:$checksum@$placementKey"
    }

    private fun createLayers() {
        mWallpaperForeground = FrameLayout(mContext).apply {
            tag = ICONIFY_DEPTH_WALLPAPER_FOREGROUND_TAG
        }
        mWallpaperBackground = FrameLayout(mContext).apply {
            tag = ICONIFY_DEPTH_WALLPAPER_BACKGROUND_TAG
        }
        mWallpaperDimmingOverlay = FrameLayout(mContext)
        mWallpaperBitmapContainer = FrameLayout(mContext)

        val layoutParams = FrameLayout.LayoutParams(
            MATCH_PARENT,
            MATCH_PARENT
        )

        mWallpaperDimmingOverlay.setBackgroundColor(
            if (DEPTH_WALL_BG_FILE.exists()) {
                Color.BLACK
            } else {
                Color.TRANSPARENT
            }
        )
        mWallpaperDimmingOverlay.alpha = 0F

        mWallpaperDimmingOverlay.layoutParams = layoutParams
        mWallpaperBitmapContainer.layoutParams = layoutParams

        mWallpaperBackground.addView(mWallpaperBitmapContainer)
        mWallpaperBackground.addView(mWallpaperDimmingOverlay)

        mWallpaperForeground.layoutParams = layoutParams
        mWallpaperBackground.layoutParams = layoutParams

        mWallpaperForeground.id = View.generateViewId()

        mLayersCreated = true
        applyWallpaperScale()
    }

    private fun displayBoundsOf(engine: Any): Rect =
        (engine.callMethod("getDisplayContext") as Context)
            .getSystemService(WindowManager::class.java)
            .currentWindowMetrics
            .bounds

    private fun wallpaperPlacement(engine: Any, displayBounds: Rect): RectF? {
        val frame = (engine.getFieldSilently("mRelayoutResult")
            ?.getFieldSilently("frames")
            ?.getFieldSilently("frame") as? Rect)
            ?.takeIf { !it.isEmpty }
            ?: (engine.callMethodSilently("getSurfaceHolder") as? SurfaceHolder)
                ?.surfaceFrame
                ?.takeIf { !it.isEmpty }
            ?: return null

        val xOffset = ((engine.getFieldSilently("mPendingXOffset") as? Float) ?: 0.5f).coerceIn(0f, 1f)
        val yOffset = ((engine.getFieldSilently("mPendingYOffset") as? Float) ?: 0.5f).coerceIn(0f, 1f)
        val bitmapSize = wallpaperBitmapSize?.takeIf { it.x > 0 && it.y > 0 }
        val crop = wallpaperCrop?.takeIf { !it.isEmpty && bitmapSize != null }

        val (cropLeft, cropTop, cropWidth, cropHeight) = if (crop != null && bitmapSize != null) {
            val scaleX = frame.width().toFloat() / bitmapSize.x
            val scaleY = frame.height().toFloat() / bitmapSize.y
            listOf(crop.left * scaleX, crop.top * scaleY, crop.width() * scaleX, crop.height() * scaleY)
        } else {
            listOf(0f, 0f, frame.width().toFloat(), frame.height().toFloat())
        }

        val availableWidth = cropWidth - displayBounds.width()
        val availableHeight = cropHeight - displayBounds.height()
        val left = frame.left - (cropLeft + (if (availableWidth > 0) availableWidth * xOffset else 0f) + 0.5f).toInt()
        val top = frame.top - (cropTop + (if (availableHeight > 0) availableHeight * yOffset else 0f) + 0.5f).toInt()

        return RectF(
            left.toFloat(),
            top.toFloat(),
            (left + frame.width()).toFloat(),
            (top + frame.height()).toFloat()
        )
    }

    private fun queryWallpaperCrop(engine: Any, displayBounds: Rect): Rect? {
        val flags = getWallpaperFlag(engine)
        val which = if ((flags and WallpaperManager.FLAG_SYSTEM) != 0) {
            WallpaperManager.FLAG_SYSTEM
        } else {
            WallpaperManager.FLAG_LOCK
        }

        return try {
            (WallpaperManager.getInstance(mContext).callMethod(
                "getBitmapCrops",
                listOf(Point(displayBounds.width(), displayBounds.height())),
                which,
                false
            ) as? List<*>)?.firstOrNull() as? Rect
        } catch (_: Throwable) {
            null
        }
    }

    private fun renderAtPlacement(bitmap: Bitmap, placement: RectF, displayBounds: Rect): Bitmap {
        val output = createBitmap(displayBounds.width(), displayBounds.height())
        Canvas(output).drawBitmap(bitmap, null, placement, Paint(Paint.FILTER_BITMAP_FLAG))
        return output
    }

    private fun centerCrop(bitmap: Bitmap, displayBounds: Rect): Bitmap {
        val ratio = max(
            1f * displayBounds.width() / bitmap.width,
            1f * displayBounds.height() / bitmap.height
        )
        val desiredWidth = (ratio * bitmap.width).roundToInt()
        val desiredHeight = (ratio * bitmap.height).roundToInt()

        return Bitmap.createBitmap(
            bitmap.scale(desiredWidth, desiredHeight),
            (desiredWidth - displayBounds.width()) / 2,
            (desiredHeight - displayBounds.height()) / 2,
            displayBounds.width(),
            displayBounds.height()
        ).let { Bitmap.createBitmap(it) }
    }

    private fun updateWallpaperZoom(engine: Any) {
        wallpaperZoom = (engine.callMethodSilently("getZoom") as? Float) ?: wallpaperZoom
        wallpaperZoomsOut = (engine.callMethodSilently("shouldZoomOutWallpaper") as? Boolean) ?: true
        wallpaperDimAmount = (engine.getFieldSilently("mWallpaperDimAmount") as? Float) ?: wallpaperDimAmount
        applyWallpaperScale()
    }

    private fun wallpaperScale(): Float {
        if (showCustomImages || !wallpaperZoomsOut) return 1f
        return 1f + (wallpaperMaxScale - 1f) * (1f - wallpaperZoom.coerceIn(0f, 1f))
    }

    private fun applyWallpaperScale() {
        if (!mLayersCreated) return

        if (Looper.myLooper() != Looper.getMainLooper()) {
            mWallpaperForeground.post { applyWallpaperScale() }
            return
        }

        val scale = wallpaperScale()
        val baked = bakedWallpaperPlacement
        val current = currentWallpaperPlacement
        val (shiftX, shiftY) = if (showCustomImages || baked == null || current == null) {
            0f to 0f
        } else {
            (current.left - baked.left) * scale to (current.top - baked.top) * scale
        }

        listOf(mWallpaperForeground, mWallpaperBackground).forEach { layer ->
            layer.scaleX = scale
            layer.scaleY = scale
            layer.translationX = shiftX
            layer.translationY = shiftY
        }
    }

    private fun isLockScreenWallpaper(canvasEngine: Any): Boolean {
        return ((getWallpaperFlag(canvasEngine) and WallpaperManager.FLAG_LOCK) == WallpaperManager.FLAG_LOCK)
    }

    private fun setDepthWallpaper() {
        if (SceneContainer.isEnabled) {
            updateComposeDepthWallpaper()
            return
        }

        if (!mLayersCreated || mScrimController == null) return

        val state = mScrimController.getField("mState").toString()
        val showForeground = (showDepthWallpaper &&
                (state == "KEYGUARD" || (showOnAOD && state in setOf("AOD", "PULSING"))))

        if (showForeground) {
            mWallpaperForeground.layoutParams.apply {
                height = MATCH_PARENT
                width = MATCH_PARENT
            }
            mWallpaperBackground.layoutParams.apply {
                height = MATCH_PARENT
                width = MATCH_PARENT
            }

            loadForegroundDrawable()

            if (mWallpaperForegroundCacheValid && mWallpaperForeground.background != null) {
                mWallpaperForeground.background.alpha = (foregroundAlpha * 255).toInt()

                val (targetAlpha, requiresAnimation) = if (state != "KEYGUARD") { // AOD
                    mForegroundDimmingOverlay!!.alpha = 192
                    foregroundAlpha to true
                } else {
                    // this is the dimmed wallpaper coverage
                    mForegroundDimmingOverlay!!.alpha =
                        if (keepLockScreenShade) (mScrimController.getField("mScrimBehindAlphaKeyguard") as Float * 240).roundToInt() // A tad bit lower than max. show it a bit lighter than other stuff
                        else 0
                    foregroundAlpha to (showOnAOD && (mPreviousState in setOf("AOD", "PULSING")))
                }

                mWallpaperDimmingOverlay.alpha = backgroundDimAlpha()

                mWallpaperBackground.visibility =
                    if (shouldShowBackground) View.VISIBLE else View.GONE
                shouldShowForeground = shouldShowBackground
                updateForegroundVisibility(targetAlpha, if (requiresAnimation) 300L else 0L)
            }
        } else if (mLayersCreated) {
            shouldShowForeground = false
            updateForegroundVisibility()

            if (state == "UNLOCKED" || !shouldShowBackground) {
                mWallpaperBackground.visibility = View.GONE
            }
        }

        mWallpaperBackground.z = -2f
        mWallpaperForeground.z = -.5f

        if (mWallpaperForeground.parent != null) {
            lsItemTags
                .map { (mWallpaperForeground.parent as ViewGroup).findViewContainingTag(it) }
                .forEach { view -> view?.z = -1f }
        }

        mPreviousState = state
    }

    private fun getWallpaperFlag(canvasEngine: Any): Int {
        return canvasEngine.callMethod("getWallpaperFlags") as Int
    }

    private fun invalidateCache() { // invalidate lock screen wallpaper subject cache
        mWallpaperForegroundCacheValid = false

        if (mLayersCreated) {
            mWallpaperForeground.post {
                shouldShowForeground = false
                mWallpaperForeground.background = null
                mWallpaperBackground.visibility = View.GONE
                mWallpaperBitmapContainer.background = null
                updateForegroundVisibility()
            }
        }

        try {
            if (DEPTH_WALL_FG_FILE.exists()) {
                DEPTH_WALL_FG_FILE.delete()
            }
        } catch (_: Throwable) {
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

        BootCallback.registerBootListener {
            Handler(Looper.getMainLooper()).post {
                try {
                    if (DEPTH_WALL_BG_FILE.exists()) {
                        FileInputStream(DEPTH_WALL_BG_FILE).use { inputStream ->
                            val bitmapDrawable = BitmapDrawable.createFromStream(
                                inputStream,
                                ""
                            )!!.apply {
                                alpha = 255
                            }

                            mWallpaperBackground.post {
                                mWallpaperBitmapContainer.background = bitmapDrawable

                                if (mScrimController != null) {
                                    mWallpaperDimmingOverlay.setBackgroundColor(Color.BLACK)
                                    mWallpaperDimmingOverlay.alpha = backgroundDimAlpha()
                                }
                            }
                        }
                    }
                } catch (_: Throwable) {
                }

                // this sets the dimmed foreground wallpaper
                setDepthWallpaper()
            }
        }
    }

    companion object {
        private const val BACKGROUND_GROUP_KEY = 0x1C0B1C22
        private const val FOREGROUND_GROUP_KEY = 0x1C0B1C23
        private const val DEFAULT_KEYGUARD_SCRIM_ALPHA = 0.2f
        private const val LOCKSCREEN_BEHIND_SCRIM_ALPHA = 51
    }
}
