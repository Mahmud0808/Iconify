package com.drdisagree.iconify.xposed.modules.volume

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.edit
import androidx.core.view.isEmpty
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.views.ExtendedSlider
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.google.android.material.slider.Slider
import com.google.android.material.slider.TickVisibilityMode
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.util.WeakHashMap
import kotlin.math.roundToInt

@SuppressLint("DiscouragedApi", "DefaultLocale")
class PerAppVolume(context: Context) : ModPack(context) {

    private var showAppVolume = false

    private val mainHandler = Handler(Looper.getMainLooper())
    private val appVolumeSources = linkedMapOf<String, AppVolumeSource>()
    private val retainedAppVolumeSources = linkedMapOf<String, AppVolumeSource>()
    private val appVolumeCardButtons = WeakHashMap<ImageButton, Unit>()
    private val appVolumeSettingsLongPressViews = WeakHashMap<View, Unit>()
    private var appVolumeButtonView: View? = null
    private var appVolumeSheetView: View? = null
    private var appVolumeSliderDragging = false
    private var appVolumeSheetRefreshPending = false
    private var playbackCallbackRegistered = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            showAppVolume = getBoolean(XposedKey.VOLUME_PANEL_APP_VOLUME)
        }

        if (showAppVolume) {
            mainHandler.post {
                registerPlaybackCallback()
                refreshPlaybackSources()
            }
        } else {
            appVolumeSources.clear()
            retainedAppVolumeSources.clear()
            dismissFloatingPerAppVolumeOverlay()
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        hookPerAppVolumeSettingsLongPress()

        if (showAppVolume) {
            mainHandler.post {
                registerPlaybackCallback()
                refreshPlaybackSources()
            }
        }
    }

    private fun registerPlaybackCallback() {
        if (playbackCallbackRegistered) return

        val audioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return

        runCatching {
            audioManager.registerAudioPlaybackCallback(
                object : AudioManager.AudioPlaybackCallback() {
                    override fun onPlaybackConfigChanged(configs: MutableList<AudioPlaybackConfiguration>) {
                        updatePlaybackSources(configs)
                    }
                },
                mainHandler
            )

            playbackCallbackRegistered = true
            refreshPlaybackSources()
        }
    }

    private fun refreshPlaybackSources() {
        val audioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return

        val currentConfigs = runCatching {
            audioManager.callMethodSilently("getActivePlaybackConfigurations") as? List<*>
        }.getOrNull()

        updatePlaybackSources(
            currentConfigs?.filterIsInstance<AudioPlaybackConfiguration>().orEmpty()
        )
    }

    private fun updatePlaybackSources(configs: List<AudioPlaybackConfiguration>) {
        if (!showAppVolume) {
            appVolumeSources.clear()
            retainedAppVolumeSources.clear()
            mainHandler.post {
                dismissFloatingPerAppVolumeOverlay()
            }
            return
        }

        val now = System.currentTimeMillis()
        val previousSources = retainedAppVolumeSources.toMap()
        val nextSources = linkedMapOf<String, AppVolumeSource>()

        configs.forEach { config ->
            if (!isPlaybackConfigActive(config)) return@forEach

            val uid = config.callMethodSilently("getClientUid") as? Int ?: return@forEach
            val packageName = resolvePackageName(uid) ?: return@forEach
            if (packageName == mContext.packageName || packageName == "android") return@forEach

            val proxy = config.callMethodSilently("getPlayerProxy")
            val previousSource = previousSources[packageName]
            val volume = previousSource?.volume ?: readStoredAppVolume(packageName)

            val source = previousSource?.copy(
                volume = volume,
                proxies = mutableListOf(),
                lastSeenAtMillis = now
            ) ?: AppVolumeSource(
                packageName = packageName,
                label = resolveAppLabel(packageName),
                icon = resolveAppIcon(packageName),
                volume = volume,
                proxies = mutableListOf(),
                lastSeenAtMillis = now
            )

            if (proxy != null) {
                source.proxies.add(proxy)
            }

            source.volume = volume
            source.lastSeenAtMillis = now
            nextSources[packageName] = source
            retainedAppVolumeSources[packageName] = source
        }

        previousSources.values.forEach { previousSource ->
            if (nextSources.containsKey(previousSource.packageName)) return@forEach

            val recentlySeen = now - previousSource.lastSeenAtMillis <= MUTED_SOURCE_KEEP_MS
            val isMutedByIconify = previousSource.volume <= 0.001f
            val sheetIsOpen = appVolumeSheetView != null

            // Important: a player muted through PlayerProxy.setVolume(0f) can disappear
            // from active playback callbacks even while the app is still playing.
            // Keep explicitly muted sources in the UI so the user can restore them.
            if (isMutedByIconify || sheetIsOpen || recentlySeen) {
                nextSources[previousSource.packageName] = previousSource
            } else {
                retainedAppVolumeSources.remove(previousSource.packageName)
            }
        }

        appVolumeSources.clear()
        appVolumeSources.putAll(nextSources)
        retainedAppVolumeSources.putAll(nextSources)

        appVolumeSources.values.forEach { source ->
            applyVolumeToSource(source)
        }

        mainHandler.post {
            if (appVolumeSliderDragging) {
                appVolumeSheetRefreshPending = true
            } else {
                refreshFloatingPerAppVolumeSheet()
            }
        }
    }

    private fun isPlaybackConfigActive(config: AudioPlaybackConfiguration): Boolean {
        val isActive = config.callMethodSilently("isActive") as? Boolean
        if (isActive != null) return isActive

        val state = config.callMethodSilently("getPlayerState") as? Int ?: return false
        return state == PLAYER_STATE_STARTED
    }

    private fun resolvePackageName(uid: Int): String? {
        val packages = mContext.packageManager.getPackagesForUid(uid).orEmpty()
        return packages.firstOrNull { it != "android" && it != mContext.packageName }
            ?: packages.firstOrNull()
    }

    private fun resolveAppLabel(packageName: String): String {
        return runCatching {
            val info = mContext.packageManager.getApplicationInfo(packageName, 0)
            mContext.packageManager.getApplicationLabel(info).toString()
        }.getOrDefault(packageName)
    }

    private fun resolveAppIcon(packageName: String) =
        runCatching {
            mContext.packageManager.getApplicationIcon(packageName)
        }.getOrNull()

    private fun readStoredAppVolume(packageName: String): Float {
        return getAppVolumePrefs().getFloat(packageName, 1f).coerceIn(0f, 1f)
    }

    private fun writeStoredAppVolume(packageName: String, volume: Float) {
        getAppVolumePrefs().edit { putFloat(packageName, volume.coerceIn(0f, 1f)) }
    }

    private fun getAppVolumePrefs() = mContext
        .createDeviceProtectedStorageContext()
        .getSharedPreferences(PER_APP_VOLUME_PREFS, Context.MODE_PRIVATE)

    private fun setSourceVolume(packageName: String, volume: Float) {
        val coercedVolume = volume.coerceIn(0f, 1f)
        val source = appVolumeSources[packageName]
            ?: retainedAppVolumeSources[packageName]
            ?: AppVolumeSource(
                packageName = packageName,
                label = resolveAppLabel(packageName),
                icon = resolveAppIcon(packageName),
                volume = coercedVolume,
                proxies = mutableListOf(),
                lastSeenAtMillis = System.currentTimeMillis()
            )

        source.volume = coercedVolume
        source.lastSeenAtMillis = System.currentTimeMillis()
        appVolumeSources[packageName] = source
        retainedAppVolumeSources[packageName] = source

        writeStoredAppVolume(packageName, source.volume)

        if (coercedVolume > 0.001f && !appVolumeSliderDragging) {
            // Re-read active players before applying a restore from 0%.
            // At 0%, some players can be temporarily absent from callbacks.
            refreshPlaybackSources()
        }

        applyVolumeToSource(appVolumeSources[packageName] ?: source)

        if (appVolumeSliderDragging) {
            appVolumeSheetRefreshPending = true
        } else {
            refreshFloatingPerAppVolumeSheet()
        }
    }

    private fun applyVolumeToSource(source: AppVolumeSource) {
        val volume = source.volume.coerceIn(0f, 1f)

        source.proxies.forEach { proxy ->
            proxy.callMethodSilently("setVolume", volume)
        }
    }

    private fun hookPerAppVolumeSettingsLongPress() {
        val settingsButtonBinderClass = findClass(
            "$SYSTEMUI_PACKAGE.volume.dialog.settings.ui.binder.VolumeDialogSettingsButtonViewBinder",
            suppressError = true
        )

        settingsButtonBinderClass
            .hookMethod("bind")
            .suppressError()
            .runAfter { param ->
                if (!showAppVolume) return@runAfter

                registerPlaybackCallback()
                refreshPlaybackSources()

                val bindView = param.args.firstOrNull { it is View } as? View ?: return@runAfter
                installPerAppVolumeLongPress(bindView)

                val root = findDecorRootSilently(bindView)
                root?.post {
                    findSettingsButton(root)?.let { settingsButton ->
                        installPerAppVolumeLongPress(settingsButton)
                    }
                }
            }

        val volumeDialogViewBinderClass = findClass(
            "$SYSTEMUI_PACKAGE.volume.dialog.ui.binder.VolumeDialogViewBinder",
            suppressError = true
        )

        volumeDialogViewBinderClass
            .hookMethod("bind")
            .suppressError()
            .runAfter { param ->
                if (!showAppVolume) return@runAfter

                val dialog = param.args.firstOrNull { it is Dialog } as? Dialog
                val root = dialog?.window?.decorView as? ViewGroup ?: return@runAfter

                root.post {
                    findSettingsButton(root)?.let { settingsButton ->
                        installPerAppVolumeLongPress(settingsButton)
                    }
                }
            }
    }

    private fun findSettingsButton(root: ViewGroup): View? {
        return findViewByResourceName(root, "volume_panel_dialog_settings_button")
            ?: findBottomSettingsLikeButton(root)
    }

    private fun installPerAppVolumeLongPress(view: View) {
        if (appVolumeSettingsLongPressViews.containsKey(view)) return

        appVolumeSettingsLongPressViews[view] = Unit
        view.isLongClickable = true
        view.setOnLongClickListener {
            if (!showAppVolume) return@setOnLongClickListener false

            registerPlaybackCallback()
            refreshPlaybackSources()
            showFloatingPerAppVolumeSheet()
            true
        }
    }

    private fun findDecorRootSilently(view: View): ViewGroup? {
        var current: View? = view

        repeat(12) {
            val parent = current?.parent as? View
                ?: return current as? ViewGroup
            current = parent
        }

        return current as? ViewGroup
    }

    private fun attachPerAppVolumeCardButton(root: ViewGroup) {
        if (root.findViewWithTag<ImageButton>(PER_APP_VOLUME_CARD_BUTTON_TAG) != null) return

        val settingsButton = findViewByResourceName(root, "volume_panel_dialog_settings_button")
            ?: findBottomSettingsLikeButton(root)
            ?: return
        val parent = settingsButton.parent as? ViewGroup ?: return

        val button = createPerAppVolumeCardButton()
        val insertIndex = parent.indexOfChild(settingsButton).let { index ->
            if (index >= 0) index + 1 else parent.childCount
        }

        val params = createPerAppVolumeCardButtonLayoutParams(parent, settingsButton)

        runCatching {
            parent.clipChildren = false
            parent.clipToPadding = false
            parent.addView(button, insertIndex.coerceIn(0, parent.childCount), params)
            parent.requestLayout()
            appVolumeCardButtons[button] = Unit
            updatePerAppVolumeCardButton(button)
        }
    }

    private fun createPerAppVolumeCardButton(): ImageButton {
        return ImageButton(mContext).apply {
            tag = PER_APP_VOLUME_CARD_BUTTON_TAG
            alpha = 0.96f
            visibility = View.GONE
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(
                mContext.toPx(9),
                mContext.toPx(9),
                mContext.toPx(9),
                mContext.toPx(9)
            )
            background = null
            contentDescription = "Per-app volume"
            setOnClickListener {
                refreshPlaybackSources()
                showFloatingPerAppVolumeSheet()
            }
        }
    }

    private fun createPerAppVolumeCardButtonLayoutParams(
        parent: ViewGroup,
        reference: View
    ): ViewGroup.LayoutParams {
        val size = resolveViewSize(reference)

        return when (parent) {
            is LinearLayout -> LinearLayout.LayoutParams(size, size).apply {
                gravity = Gravity.CENTER
                leftMargin = mContext.toPx(2)
                rightMargin = mContext.toPx(2)
            }

            is FrameLayout -> FrameLayout.LayoutParams(size, size, Gravity.CENTER).apply {
                leftMargin = mContext.toPx(2)
                rightMargin = mContext.toPx(2)
            }

            else -> ViewGroup.MarginLayoutParams(size, size).apply {
                leftMargin = mContext.toPx(2)
                rightMargin = mContext.toPx(2)
            }
        }
    }

    private fun resolveViewSize(reference: View): Int {
        val width = if (reference.width > 0) reference.width else reference.layoutParams?.width ?: 0
        val height =
            if (reference.height > 0) reference.height else reference.layoutParams?.height ?: 0

        return when {
            width > 0 && height > 0 -> minOf(width, height)
            width > 0 -> width
            height > 0 -> height
            else -> mContext.toPx(48)
        }.coerceIn(mContext.toPx(38), mContext.toPx(56))
    }

    @Suppress("SameParameterValue")
    private fun findViewByResourceName(root: ViewGroup, name: String): View? {
        val id = mContext.resources.getIdentifier(name, "id", mContext.packageName)
        if (id == 0) return null

        return root.findViewById(id)
    }

    private fun findBottomSettingsLikeButton(root: ViewGroup): View? {
        var bestView: View? = null
        var bestScore = Int.MIN_VALUE

        fun visit(view: View) {
            if (view.tag == PER_APP_VOLUME_CARD_BUTTON_TAG) return

            val group = view as? ViewGroup
            val isCandidate = view.isShown &&
                    view !is SeekBar &&
                    (view.isClickable || view.hasOnClickListeners())

            if (isCandidate) {
                val location = IntArray(2)
                runCatching {
                    view.getLocationOnScreen(location)
                }

                val width = if (view.width > 0) view.width else view.layoutParams?.width ?: 0
                val height = if (view.height > 0) view.height else view.layoutParams?.height ?: 0
                val bottom = location[1] + height
                val compactBonus =
                    if (width <= mContext.toPx(90) && height <= mContext.toPx(90)) 3000 else 0
                val score = bottom * 10 + compactBonus

                if (score > bestScore) {
                    bestScore = score
                    bestView = view
                }
            }

            if (group != null) {
                for (i in 0 until group.childCount) {
                    visit(group.getChildAt(i))
                }
            }
        }

        visit(root)
        return bestView
    }

    private fun updatePerAppVolumeCardButtons() {
        val audioManager =
            mContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        val shouldShow = showAppVolume &&
                (appVolumeSources.isNotEmpty() || audioManager?.isMusicActive == true)

        appVolumeCardButtons.keys.toList().forEach { button ->
            if (button.parent == null) {
                appVolumeCardButtons.remove(button)
                return@forEach
            }

            button.visibility = if (shouldShow) View.VISIBLE else View.GONE
            updatePerAppVolumeCardButton(button)
        }
    }

    private fun updatePerAppVolumeCardButton(button: ImageButton) {
        val firstSource = appVolumeSources.values.firstOrNull()
        val fallbackIcon = runCatching {
            AppCompatResources.getDrawable(mContext, android.R.drawable.ic_media_play)
        }.getOrNull()

        button.setImageDrawable(firstSource?.icon ?: fallbackIcon)
    }

    private fun shouldShowFloatingPerAppVolumeOverlay(): Boolean {
        if (!showAppVolume) return false

        val audioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        return appVolumeSources.isNotEmpty() || audioManager?.isMusicActive == true
    }

    private fun updateFloatingPerAppVolumeOverlay() {
        if (!shouldShowFloatingPerAppVolumeOverlay()) {
            dismissFloatingPerAppVolumeButton()
            return
        }

        val existingButton = appVolumeButtonView
        if (existingButton != null) {
            updateFloatingPerAppVolumeButton(existingButton)
            return
        }

        val button = createFloatingPerAppVolumeButton()
        val windowManager =
            mContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return

        val params = WindowManager.LayoutParams(
            mContext.toPx(58),
            mContext.toPx(58),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            x = mContext.toPx(14)
            y = 0
            title = "Iconify per-app volume button"
        }

        runCatching {
            windowManager.addView(button, params)
            appVolumeButtonView = button
            updateFloatingPerAppVolumeButton(button)
        }
    }

    private fun createFloatingPerAppVolumeButton(): ImageButton {
        return ImageButton(mContext).apply {
            alpha = 0.96f
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(
                mContext.toPx(10),
                mContext.toPx(10),
                mContext.toPx(10),
                mContext.toPx(10)
            )
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.argb(230, 18, 22, 30))
            }
            setOnClickListener {
                refreshPlaybackSources()
                showFloatingPerAppVolumeSheet()
            }
        }
    }

    private fun updateFloatingPerAppVolumeButton(button: View) {
        val imageButton = button as? ImageButton ?: return
        val firstSource = appVolumeSources.values.firstOrNull()
        val fallbackIcon = runCatching {
            AppCompatResources.getDrawable(mContext, android.R.drawable.ic_media_play)
        }.getOrNull()

        imageButton.setImageDrawable(firstSource?.icon ?: fallbackIcon)
    }

    private fun dismissFloatingPerAppVolumeOverlay() {
        dismissFloatingPerAppVolumeSheet()
        dismissFloatingPerAppVolumeButton()
    }

    private fun dismissFloatingPerAppVolumeButton() {
        val view = appVolumeButtonView ?: return
        val windowManager =
            mContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return

        runCatching {
            windowManager.removeView(view)
        }

        appVolumeButtonView = null
    }

    private fun showFloatingPerAppVolumeSheet() {
        dismissFloatingPerAppVolumeSheet(animated = false)

        val windowManager =
            mContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return

        val overlay = FrameLayout(mContext).apply {
            setBackgroundColor(Color.argb(SHEET_DIM_ALPHA, 0, 0, 0))
            alpha = 1f
            isClickable = true
            setOnClickListener {
                dismissFloatingPerAppVolumeSheet()
            }
        }

        val sheet = createFloatingPerAppVolumeSheetContent().apply {
            alpha = 1f
            translationY = 0f
            setOnClickListener {
                // Consume clicks inside the sheet.
            }
        }

        overlay.addView(
            sheet,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            ).apply {
                leftMargin = mContext.toPx(10)
                rightMargin = mContext.toPx(10)
                bottomMargin = mContext.toPx(10)
            }
        )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
            title = "Iconify per-app volume sheet"
        }

        runCatching {
            windowManager.addView(overlay, params)
            appVolumeSheetView = overlay

            sheet.post {
                // Keep the final visible state as the fallback.
                sheet.alpha = 1f
                sheet.translationY = 0f
                overlay.setBackgroundColor(Color.argb(SHEET_DIM_ALPHA, 0, 0, 0))
                animateFloatingSheetIn(overlay, sheet)
            }
        }
    }

    private fun dismissFloatingPerAppVolumeSheet(animated: Boolean = true) {
        val overlay = appVolumeSheetView as? ViewGroup ?: return
        val windowManager =
            mContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return

        fun removeNow() {
            runCatching {
                windowManager.removeView(overlay)
            }

            if (appVolumeSheetView === overlay) {
                appVolumeSheetView = null
            }
        }

        if (!animated) {
            removeNow()
            return
        }

        val sheet = overlay.getChildAt(0)
        if (sheet == null) {
            removeNow()
            return
        }

        animateFloatingSheetOut(overlay, sheet) {
            removeNow()
        }
    }

    private fun refreshFloatingPerAppVolumeSheet() {
        val overlay = appVolumeSheetView as? ViewGroup ?: return
        if (overlay.isEmpty()) return

        val newSheet = createFloatingPerAppVolumeSheetContent().apply {
            alpha = 1f
            translationY = 0f
            setOnClickListener {
                // Consume clicks inside the sheet.
            }
        }

        overlay.removeAllViews()
        overlay.addView(
            newSheet,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            ).apply {
                leftMargin = mContext.toPx(10)
                rightMargin = mContext.toPx(10)
                bottomMargin = mContext.toPx(10)
            }
        )
    }

    private fun animateFloatingSheetIn(overlay: View, sheet: View) {
        val startY = sheet.height.toFloat().coerceAtLeast(mContext.toPx(160).toFloat())

        overlay.setBackgroundColor(Color.argb(SHEET_DIM_ALPHA, 0, 0, 0))
        sheet.alpha = 1f
        sheet.translationY = startY

        sheet.animate()
            .translationY(0f)
            .setDuration(SHEET_ANIMATION_MS)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                sheet.alpha = 1f
                sheet.translationY = 0f
                overlay.setBackgroundColor(Color.argb(SHEET_DIM_ALPHA, 0, 0, 0))
            }
            .start()

        mainHandler.postDelayed({
            // Hard fallback: never leave the overlay invisible/untouchable.
            sheet.alpha = 1f
            sheet.translationY = 0f
            overlay.setBackgroundColor(Color.argb(SHEET_DIM_ALPHA, 0, 0, 0))
        }, SHEET_ANIMATION_MS + 80L)
    }

    private fun animateFloatingSheetOut(overlay: View, sheet: View, endAction: () -> Unit) {
        ValueAnimator.ofInt(SHEET_DIM_ALPHA, 0).apply {
            duration = SHEET_ANIMATION_MS / 2
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                overlay.setBackgroundColor(
                    Color.argb(animator.animatedValue as Int, 0, 0, 0)
                )
            }
            start()
        }

        sheet.animate()
            .alpha(0f)
            .translationY(sheet.height.toFloat().coerceAtLeast(mContext.toPx(180).toFloat()))
            .setDuration(SHEET_ANIMATION_MS / 2)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction(endAction)
            .start()
    }

    private fun createFloatingPerAppVolumeSheetContent(): View {
        val container = LinearLayout(mContext).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                mContext.toPx(18),
                mContext.toPx(12),
                mContext.toPx(18),
                mContext.toPx(18)
            )
            background = GradientDrawable().apply {
                cornerRadius = mContext.toPx(30).toFloat()
                setColor(Color.argb(250, 22, 24, 31))
            }
            elevation = mContext.toPx(10).toFloat()
            clipChildren = false
            clipToPadding = false
        }

        container.addView(View(mContext).apply {
            background = GradientDrawable().apply {
                cornerRadius = mContext.toPx(2).toFloat()
                setColor(Color.argb(120, 255, 255, 255))
            }
        }, LinearLayout.LayoutParams(mContext.toPx(42), mContext.toPx(4)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            bottomMargin = mContext.toPx(14)
        })

        container.addView(TextView(mContext).apply {
            text = modRes.getString(R.string.volume_sheet_app_volume)
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, mContext.toPx(2))
        })

        container.addView(TextView(mContext).apply {
            text = if (appVolumeSources.isEmpty()) {
                modRes.getString(R.string.volume_sheet_no_active_source)
            } else {
                modRes.getString(
                    R.string.volume_sheet_active_sources_count_format,
                    appVolumeSources.size
                )
            }
            textSize = 12f
            setTextColor(Color.argb(185, 255, 255, 255))
            gravity = Gravity.CENTER
            setPadding(0, mContext.toPx(4), 0, mContext.toPx(14))
        })

        if (appVolumeSources.isEmpty()) {
            container.addView(TextView(mContext).apply {
                text = modRes.getString(R.string.volume_sheet_play_music_or_video)
                textSize = 13f
                setTextColor(Color.argb(220, 255, 255, 255))
                gravity = Gravity.CENTER
                setPadding(0, mContext.toPx(10), 0, mContext.toPx(10))
            })
        } else {
            appVolumeSources.values.forEach { source ->
                container.addView(createPerAppVolumeRow(source))
            }
        }

        return ScrollView(mContext).apply {
            isFillViewport = false
            overScrollMode = View.OVER_SCROLL_NEVER
            addView(container)
        }
    }

    private fun beginAppVolumeSliderDrag(view: View?) {
        appVolumeSliderDragging = true
        requestAppVolumeParentsDisallowIntercept(view, true)
    }

    private fun endAppVolumeSliderDrag(view: View?) {
        requestAppVolumeParentsDisallowIntercept(view, false)

        if (!appVolumeSliderDragging) return

        appVolumeSliderDragging = false

        if (appVolumeSheetRefreshPending) {
            appVolumeSheetRefreshPending = false
            refreshPlaybackSources()
            mainHandler.postDelayed({
                if (!appVolumeSliderDragging) {
                    refreshFloatingPerAppVolumeSheet()
                }
            }, 120L)
        }
    }

    private fun requestAppVolumeParentsDisallowIntercept(view: View?, disallow: Boolean) {
        var parent: ViewParent? = view?.parent

        repeat(12) {
            parent?.requestDisallowInterceptTouchEvent(disallow)
            parent = (parent as? View)?.parent
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createPerAppVolumeRow(source: AppVolumeSource): View {
        val row = LinearLayout(mContext).apply {
            orientation = LinearLayout.VERTICAL
            clipChildren = false
            clipToPadding = false
            setPadding(
                mContext.toPx(14),
                mContext.toPx(8),
                mContext.toPx(14),
                mContext.toPx(8)
            )
            background = GradientDrawable().apply {
                cornerRadius = mContext.toPx(18).toFloat()
                setColor(Color.argb(42, 255, 255, 255))
            }
        }

        val header = LinearLayout(mContext).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            clipChildren = false
            clipToPadding = false
        }

        header.addView(ImageView(mContext).apply {
            setImageDrawable(source.icon)
        }, LinearLayout.LayoutParams(mContext.toPx(30), mContext.toPx(30)).apply {
            rightMargin = mContext.toPx(10)
        })

        header.addView(TextView(mContext).apply {
            text = source.label
            textSize = 14f
            setTextColor(Color.WHITE)
            maxLines = 1
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val percentText = TextView(mContext).apply {
            text = "%d%%".format((source.volume * 100f).roundToInt().coerceIn(0, 100))
            textSize = 13f
            setTextColor(Color.argb(230, 255, 255, 255))
            gravity = Gravity.END
        }

        header.addView(
            percentText,
            LinearLayout.LayoutParams(mContext.toPx(46), ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        row.addView(
            header, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val slider = ExtendedSlider(mContext).apply {
            valueFrom = 0f
            valueTo = 100f
            stepSize = 1f
            value = (source.volume * 100f).coerceIn(0f, 100f)
            isEnabled = true
            tickVisibilityMode = TickVisibilityMode.TICK_VISIBILITY_AUTO_HIDE

            setLabelFormatter { value ->
                "${value.toInt()}%"
            }

            setPadding(
                mContext.toPx(0),
                mContext.toPx(6),
                mContext.toPx(0),
                mContext.toPx(6)
            )

            setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        beginAppVolumeSliderDrag(view)
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        endAppVolumeSliderDrag(view)
                    }
                }
                false
            }

            addOnChangeListener { _, value, fromUser ->
                val progressInt = value.toInt().coerceIn(0, 100)
                percentText.text = "%d%%".format(progressInt)

                if (fromUser) {
                    setSourceVolume(source.packageName, progressInt / 100f)
                }
            }

            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    beginAppVolumeSliderDrag(slider)
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    endAppVolumeSliderDrag(slider)
                }
            })
        }

        row.addView(
            FrameLayout(mContext).apply {
                isClickable = true
                isFocusable = true
                clipChildren = false
                clipToPadding = false
                setPadding(0, mContext.toPx(4), 0, mContext.toPx(2))
                setOnTouchListener { view, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN,
                        MotionEvent.ACTION_MOVE -> {
                            beginAppVolumeSliderDrag(view)
                        }

                        MotionEvent.ACTION_UP,
                        MotionEvent.ACTION_CANCEL -> {
                            endAppVolumeSliderDrag(view)
                        }
                    }

                    false
                }
                addView(
                    slider, FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER
                    ).apply {
                        marginStart = -mContext.toPx(12)
                        marginEnd = -mContext.toPx(12)
                    }
                )
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                mContext.toPx(56)
            )
        )

        return row.apply {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = mContext.toPx(8)
            }
            layoutParams = params
        }
    }

    private data class AppVolumeSource(
        val packageName: String,
        val label: String,
        val icon: Drawable?,
        var volume: Float,
        val proxies: MutableList<Any>,
        var lastSeenAtMillis: Long
    )

    companion object {
        private const val SHEET_ANIMATION_MS = 260L
        private const val SHEET_DIM_ALPHA = 118
        private const val PER_APP_VOLUME_PREFS = "iconify_per_app_volume"
        private const val PER_APP_VOLUME_CARD_BUTTON_TAG = "iconify_per_app_volume_card_button"
        private const val PLAYER_STATE_STARTED = 2
        private const val MUTED_SOURCE_KEEP_MS = 30 * 60 * 1000L
    }
}
