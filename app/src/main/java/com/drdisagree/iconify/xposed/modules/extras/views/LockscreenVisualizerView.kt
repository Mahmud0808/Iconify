package com.drdisagree.iconify.xposed.modules.extras.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.media.AudioManager
import android.media.audiofx.Visualizer
import android.os.SystemClock
import android.os.Looper
import android.os.Handler
import android.view.View
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

class LockscreenVisualizerView(context: Context) : View(context) {

    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DEFAULT_STATIC_COLOR
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 8f
    }

    private var visualizer: Visualizer? = null
    private var linked = false
    private var enabledByPreference = true

    private var colorMode = COLOR_MODE_LAVA
    private var staticColor = DEFAULT_STATIC_COLOR
    private var gradientStartColor = DEFAULT_STATIC_COLOR
    private var gradientEndColor = DEFAULT_GRADIENT_END_COLOR
    private var lavaSpeedSeconds = 24f
    private var sensitivity = DEFAULT_SENSITIVITY
    private var visualizerHeightDp = DEFAULT_HEIGHT_DP
    private var barThicknessDp = DEFAULT_BAR_THICKNESS_DP
    private var smoothness = DEFAULT_SMOOTHNESS
    private var renderFps = DEFAULT_FPS
    private var lastFrameTimeMs = 0L

    private val frameHandler = Handler(Looper.getMainLooper())

    private val barCount = 34
    private val levels = FloatArray(barCount)
    private val targetLevels = FloatArray(barCount)
    private val smoothedTargets = FloatArray(barCount)
    private val systemUiAverageWindows = Array(barCount) { FloatArray(SYSTEMUI_AVERAGE_WINDOW) }
    private val systemUiAveragePositions = IntArray(barCount)
    private val systemUiAverageCounts = IntArray(barCount)

    private var consecutiveValidFrames = 0
    private var consecutiveEmptyFrames = 0
    private var streamValid = false
    private var framePosted = false
    private var lastValidAudioTime = 0L
    private var lastMusicActiveTime = 0L
    private var hidingToRemove = false
    private var hideFinishedCallback: (() -> Unit)? = null
    private var revealProgress = 1f
    private var revealTarget = 1f

    private val frameRunnable = object : Runnable {
        override fun run() {
            framePosted = false

            if (!hidingToRemove && audioManager.isMusicActive) {
                lastMusicActiveTime = System.currentTimeMillis()
                linkVisualizer()
            }

            val now = SystemClock.uptimeMillis()
            val deltaMs = if (lastFrameTimeMs == 0L) {
                BASE_FRAME_MS
            } else {
                (now - lastFrameTimeMs).coerceIn(1L, 80L).toFloat()
            }
            lastFrameTimeMs = now

            tickAnimation(deltaMs)

            if ((enabledByPreference || hidingToRemove) && isAttachedToWindow) {
                frameHandler.postDelayed(this, frameDelayMs())
                framePosted = true
            }
        }
    }

    init {
        alpha = 0.9f
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        setWillNotDraw(false)
    }

    fun configure(
        colorMode: Int,
        staticColor: Int,
        gradientStartColor: Int,
        gradientEndColor: Int,
        lavaSpeedSeconds: Float,
        sensitivity: Float,
        visualizerHeightDp: Float,
        barThicknessDp: Float,
        smoothness: Float,
        renderFps: Int
    ) {
        this.colorMode = colorMode
        this.staticColor = staticColor
        this.gradientStartColor = gradientStartColor
        this.gradientEndColor = gradientEndColor
        this.lavaSpeedSeconds = lavaSpeedSeconds.coerceIn(5f, 60f)
        this.sensitivity = sensitivity.coerceIn(0.5f, 3.0f)
        this.visualizerHeightDp = visualizerHeightDp.coerceIn(180f, 760f)
        this.barThicknessDp = barThicknessDp.coerceIn(6f, 32f)
        this.smoothness = smoothness.coerceIn(0f, 100f)
        this.renderFps = if (renderFps >= 120) 120 else 60

        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (enabledByPreference || hidingToRemove) {
            startAnimationLoop()
            if (!hidingToRemove && audioManager.isMusicActive) {
                linkVisualizer()
            }
        }
    }

    override fun onDetachedFromWindow() {
        unlinkVisualizer()
        frameHandler.removeCallbacks(frameRunnable)
        framePosted = false
        super.onDetachedFromWindow()
    }

    fun setVisualizerEnabled(enabled: Boolean) {
        enabledByPreference = enabled

        if (enabled) {
            showFromBottom(resetLevels = true)
            if (audioManager.isMusicActive) {
                linkVisualizer()
            }
        } else {
            hideAndRemove()
        }
    }

    fun showFromBottom(resetLevels: Boolean = true) {
        enabledByPreference = true
        hidingToRemove = false
        hideFinishedCallback = null
        revealProgress = 0f
        revealTarget = 1f

        if (resetLevels) {
            clearLevels()
        }

        startAnimationLoop()
        invalidate()
    }

    fun hideAndRemove(onFinished: (() -> Unit)? = null) {
        enabledByPreference = false
        hidingToRemove = true
        hideFinishedCallback = onFinished
        revealTarget = 0f

        unlinkVisualizer()
        streamValid = false
        lastMusicActiveTime = 0L
        lastValidAudioTime = 0L

        startAnimationLoop()
        invalidate()
    }

    private fun linkVisualizer() {
        if (linked) return

        runCatching {
            visualizer = Visualizer(0).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) = Unit

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            fft?.let { updateFft(it) }
                        }
                    },
                    Visualizer.getMaxCaptureRate() * 3 / 4,
                    false,
                    true
                )
                enabled = true
            }

            linked = true
        }.onFailure {
            linked = false
            runCatching { visualizer?.release() }
            visualizer = null
        }
    }

    private fun unlinkVisualizer() {
        linked = false
        streamValid = false
        consecutiveValidFrames = 0
        consecutiveEmptyFrames = 0
        clearSystemUiAverages()

        runCatching {
            visualizer?.enabled = false
            visualizer?.release()
        }

        visualizer = null
    }

    private fun startAnimationLoop() {
        if (framePosted) return

        lastFrameTimeMs = SystemClock.uptimeMillis()
        frameHandler.postDelayed(frameRunnable, frameDelayMs())
        framePosted = true
    }

    private fun updateFft(fft: ByteArray) {
        val now = System.currentTimeMillis()

        if (audioManager.isMusicActive) {
            lastMusicActiveTime = now
        }

        if (fft.size < 4 || now - lastMusicActiveTime > MUSIC_ACTIVE_GRACE_MS) {
            markEmptyFrame()
            return
        }

        val frameEnergy = calculateFrameEnergy(fft)

        if (frameEnergy < NOISE_GATE) {
            markEmptyFrame()
            return
        }

        lastValidAudioTime = now
        consecutiveValidFrames++
        consecutiveEmptyFrames = 0

        if (consecutiveValidFrames >= VALID_FRAME_THRESHOLD) {
            streamValid = true
        }

        if (!streamValid) return

        val halfSize = fft.size / 2
        val usableBins = max(1, halfSize - 2)

        for (i in 0 until barCount) {
            val normalizedIndex = i / (barCount - 1f)
            val bin = spectrumBin(normalizedIndex, usableBins)
            val index = (bin * 2).coerceAtMost(fft.size - 2)

            val real = fft[index].toInt()
            val imag = fft[index + 1].toInt()
            val magnitude = real * real + imag * imag

            val dbValue = if (magnitude > 0) {
                10f * log10(magnitude.toFloat())
            } else {
                0f
            }

            val averagedDb = systemUiAverage(i, dbValue)
            val bandGain = spectrumGain(normalizedIndex)
            val value = (averagedDb * SYSTEMUI_DB_FUZZ_FACTOR * bandGain * sensitivity) /
                SYSTEMUI_DB_HEIGHT_NORMALIZER

            targetLevels[i] = max(targetLevels[i], value.coerceIn(0f, 1f))
        }
    }

    private fun spectrumBin(normalizedIndex: Float, usableBins: Int): Int {
        val fraction = when {
            normalizedIndex < 0.30f -> {
                val t = (normalizedIndex / 0.30f).coerceIn(0f, 1f)
                lerp(0.015f, 0.20f, t.pow(1.08f))
            }

            normalizedIndex < 0.72f -> {
                val t = ((normalizedIndex - 0.30f) / 0.42f).coerceIn(0f, 1f)
                lerp(0.12f, 0.58f, t.pow(1.03f))
            }

            else -> {
                val t = ((normalizedIndex - 0.72f) / 0.28f).coerceIn(0f, 1f)
                lerp(0.36f, 0.92f, t.pow(0.92f))
            }
        }

        return 2 + (fraction * usableBins).toInt().coerceIn(0, usableBins - 1)
    }

    private fun spectrumGain(normalizedIndex: Float): Float {
        return when {
            normalizedIndex < 0.30f -> 1.02f
            normalizedIndex < 0.72f -> 1.28f
            else -> 1.72f
        }
    }

    private fun calculateFrameEnergy(fft: ByteArray): Float {
        var sum = 0f
        var count = 0

        var i = 2
        while (i + 1 < fft.size) {
            val real = fft[i].toInt()
            val imag = fft[i + 1].toInt()
            val magnitude = real * real + imag * imag
            if (magnitude > 0) {
                sum += 10f * log10(magnitude.toFloat())
            }
            count++
            i += 2
        }

        if (count == 0) return 0f

        return (sum / count) / SYSTEMUI_FRAME_DB_NORMALIZER
    }

    private fun systemUiAverage(index: Int, value: Float): Float {
        val window = systemUiAverageWindows[index]
        val position = systemUiAveragePositions[index]

        window[position] = value / SYSTEMUI_AVERAGE_WINDOW
        systemUiAveragePositions[index] = (position + 1) % SYSTEMUI_AVERAGE_WINDOW
        if (systemUiAverageCounts[index] < SYSTEMUI_AVERAGE_WINDOW) {
            systemUiAverageCounts[index]++
        }

        var sum = 0f
        for (i in 0 until systemUiAverageCounts[index]) {
            sum += window[i]
        }

        return sum
    }

    private fun clearSystemUiAverages() {
        for (i in 0 until barCount) {
            systemUiAverageWindows[i].fill(0f)
            systemUiAveragePositions[i] = 0
            systemUiAverageCounts[i] = 0
        }
    }

    private fun markEmptyFrame() {
        consecutiveEmptyFrames++

        if (consecutiveEmptyFrames > VALID_FRAME_THRESHOLD) {
            consecutiveValidFrames = 0
        }

        if (
            consecutiveEmptyFrames >= EMPTY_FRAME_THRESHOLD &&
            System.currentTimeMillis() - lastValidAudioTime > VALID_AUDIO_GRACE_MS
        ) {
            streamValid = false
        }
    }

    private fun tickAnimation(deltaMs: Float) {
        val now = System.currentTimeMillis()
        val hasRecentMusic = now - lastMusicActiveTime <= MUSIC_ACTIVE_GRACE_MS
        val hasRecentValidAudio = now - lastValidAudioTime <= VALID_AUDIO_GRACE_MS
        val shouldAnimate = !hidingToRemove && streamValid && (hasRecentMusic || hasRecentValidAudio)

        if (linked && (hidingToRemove || (!hasRecentMusic && !hasRecentValidAudio))) {
            unlinkVisualizer()
        }

        val animation = animationParams()
        val frameScale = (deltaMs / BASE_FRAME_MS).coerceIn(0.25f, 4.8f)
        val targetSmoothing = timeScaledApproach(animation.targetSmoothing, frameScale)
        val attack = timeScaledApproach(animation.attack, frameScale)
        val release = timeScaledApproach(animation.release, frameScale)
        val decay = timeScaledDecay(animation.decay, frameScale)
        val targetDecay = timeScaledDecay(animation.targetDecay, frameScale)
        val revealStep = timeScaledApproach(REVEAL_ANIMATION, frameScale)

        revealProgress += (revealTarget - revealProgress) * revealStep
        if (kotlin.math.abs(revealTarget - revealProgress) < 0.01f) {
            revealProgress = revealTarget
        }

        val visualizerHeight = min(context.dp(visualizerHeightDp).toFloat(), height * 0.70f)
        val minimumActiveLevel = if (shouldAnimate) minimumActiveLevel(visualizerHeight) else 0f

        for (i in 0 until barCount) {
            val rawSource = if (shouldAnimate) max(targetLevels[i], minimumActiveLevel) else 0f

            smoothedTargets[i] += (rawSource - smoothedTargets[i]) * targetSmoothing

            val diff = smoothedTargets[i] - levels[i]

            if (kotlin.math.abs(diff) >= DEAD_ZONE) {
                levels[i] += diff * if (diff > 0f) attack else release
            } else if (!shouldAnimate) {
                levels[i] *= decay
            }

            targetLevels[i] *= targetDecay

            if (levels[i] < 0.006f) levels[i] = 0f
        }

        if (hidingToRemove && revealProgress <= 0f) {
            clearLevels()
            val callback = hideFinishedCallback
            hideFinishedCallback = null
            hidingToRemove = false
            callback?.invoke()
            return
        }

        invalidate()
    }

    private fun clearLevels() {
        for (i in 0 until barCount) {
            levels[i] = 0f
            targetLevels[i] = 0f
            smoothedTargets[i] = 0f
        }
        clearSystemUiAverages()
        invalidate()
    }

    private fun minimumActiveLevel(visualizerHeight: Float): Float {
        if (visualizerHeight <= 0f) return 0f

        return (context.dp(BOTTOM_OFFSCREEN_DP + MIN_VISIBLE_BAR_DP).toFloat() / visualizerHeight)
            .coerceIn(0f, 0.25f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width <= 0 || height <= 0) return

        val visualizerHeight = min(context.dp(visualizerHeightDp).toFloat(), height * 0.70f)
        val baseline = height + context.dp(BOTTOM_OFFSCREEN_DP).toFloat() +
            visualizerHeight * (1f - revealProgress)
        val barGap = width.toFloat() / barCount
        val stroke = min(context.dp(barThicknessDp).toFloat(), barGap * 0.82f)

        paint.strokeWidth = stroke
        applyColorStyle()

        for (i in 0 until barCount) {
            val level = levels[i]
            if (level <= 0f) continue

            val x = barGap * i + barGap / 2f
            val barHeight = level * visualizerHeight

            applyColorStyleForBar(i, level)

            canvas.drawLine(
                x,
                baseline,
                x,
                baseline - barHeight,
                paint
            )
        }
    }

    private fun Context.dp(value: Float): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun frameDelayMs(): Long {
        return if (renderFps >= 120) 8L else 16L
    }

    private fun timeScaledApproach(base: Float, frameScale: Float): Float {
        return (1f - (1f - base).pow(frameScale)).coerceIn(0f, 1f)
    }

    private fun timeScaledDecay(base: Float, frameScale: Float): Float {
        return base.pow(frameScale).coerceIn(0f, 1f)
    }

    private fun animationParams(): AnimationParams {
        val normalizedSmoothness = smoothness / 100f

        return AnimationParams(
            attack = lerp(0.085f, 0.022f, normalizedSmoothness),
            release = lerp(0.070f, 0.018f, normalizedSmoothness),
            targetSmoothing = lerp(0.34f, 0.10f, normalizedSmoothness),
            decay = lerp(0.985f, 0.996f, normalizedSmoothness),
            targetDecay = lerp(0.90f, 0.982f, normalizedSmoothness)
        )
    }

    private fun applyColorStyle() {
        paint.shader = null

        when (colorMode) {
            COLOR_MODE_GRADIENT -> {
                paint.shader = LinearGradient(
                    0f,
                    0f,
                    width.toFloat(),
                    0f,
                    gradientStartColor,
                    gradientEndColor,
                    Shader.TileMode.CLAMP
                )
            }

            COLOR_MODE_LAVA -> {
                paint.color = lavaColor()
            }

            COLOR_MODE_SPECTRUM -> {
                paint.color = DEFAULT_STATIC_COLOR
            }

            else -> {
                paint.color = staticColor
            }
        }
    }

    private fun applyColorStyleForBar(index: Int, level: Float) {
        if (colorMode != COLOR_MODE_SPECTRUM) return

        paint.shader = null
        paint.color = spectrumColor(index, level)
    }

    private fun spectrumColor(index: Int, level: Float): Int {
        val normalizedIndex = index / (barCount - 1f)
        val baseHue = when {
            normalizedIndex < 0.30f -> lerp(36f, 18f, normalizedIndex / 0.30f)
            normalizedIndex < 0.72f -> lerp(18f, 330f, (normalizedIndex - 0.30f) / 0.42f)
            else -> lerp(330f, 265f, (normalizedIndex - 0.72f) / 0.28f)
        }

        val speedMs = (lavaSpeedSeconds * 1000f).coerceAtLeast(1f)
        val phase = (System.currentTimeMillis() % speedMs.toLong()) / speedMs
        val wave = sin(phase * Math.PI.toFloat() * 2f - normalizedIndex * Math.PI.toFloat() * 4f)
        val hue = (baseHue + wave * 18f + level * 10f + 360f) % 360f
        val saturation = (0.70f + level * 0.22f).coerceIn(0.55f, 0.98f)
        val value = (0.82f + level * 0.18f).coerceIn(0.65f, 1f)

        return Color.HSVToColor(225, floatArrayOf(hue, saturation, value))
    }

    private fun lavaColor(): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(staticColor, hsv)

        val speedMs = (lavaSpeedSeconds * 1000f).coerceAtLeast(1f)
        val phase = (System.currentTimeMillis() % speedMs.toLong()) / speedMs
        val wave = sin(phase * Math.PI.toFloat() * 2f)

        hsv[0] = (hsv[0] + wave * 28f + 360f) % 360f
        hsv[1] = (hsv[1] * 0.92f).coerceIn(0.35f, 1f)
        hsv[2] = (hsv[2] * (0.92f + (wave + 1f) * 0.08f)).coerceIn(0.45f, 1f)

        return Color.HSVToColor(Color.alpha(staticColor), hsv)
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction.coerceIn(0f, 1f)
    }

    private data class AnimationParams(
        val attack: Float,
        val release: Float,
        val targetSmoothing: Float,
        val decay: Float,
        val targetDecay: Float
    )

    private fun Context.dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    companion object {
        private const val VALID_FRAME_THRESHOLD = 2
        private const val EMPTY_FRAME_THRESHOLD = 60
        private const val NOISE_GATE = 0.010f
        private const val SYSTEMUI_AVERAGE_WINDOW = 2
        private const val SYSTEMUI_DB_FUZZ_FACTOR = 1.0f
        private const val SYSTEMUI_DB_HEIGHT_NORMALIZER = 58f
        private const val SYSTEMUI_FRAME_DB_NORMALIZER = 38f
        private const val DEFAULT_SENSITIVITY = 1.42f
        private const val DEFAULT_HEIGHT_DP = 500f
        private const val DEFAULT_BAR_THICKNESS_DP = 19f
        private const val DEFAULT_SMOOTHNESS = 50f
        private const val DEFAULT_FPS = 120
        private const val BASE_FRAME_MS = 16.666f
        private const val DEAD_ZONE = 0.006f
        private const val REVEAL_ANIMATION = 0.075f
        private const val BOTTOM_OFFSCREEN_DP = 14f
        private const val MIN_VISIBLE_BAR_DP = 6f
        private const val MUSIC_ACTIVE_GRACE_MS = 4_000L
        private const val VALID_AUDIO_GRACE_MS = 3_500L
        private const val COLOR_MODE_STATIC = 0
        private const val COLOR_MODE_LAVA = 1
        private const val COLOR_MODE_GRADIENT = 2
        private const val COLOR_MODE_SPECTRUM = 3
        private val DEFAULT_STATIC_COLOR = Color.rgb(224, 184, 99)
        private val DEFAULT_GRADIENT_END_COLOR = Color.rgb(255, 106, 136)
    }
}
