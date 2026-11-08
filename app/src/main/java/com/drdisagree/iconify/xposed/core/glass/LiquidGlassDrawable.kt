package com.drdisagree.iconify.xposed.core.glass

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.view.SurfaceControl
import android.view.View
import com.drdisagree.iconify.xposed.core.reflect.callMethodSilently

class LiquidGlassDrawable(
    private val host: View,
    private val cornerRadius: Float,
    private val blurRadius: Float,
    private val tint: Int,
    private val style: () -> LiquidGlassStyle
) : Drawable(), BackdropClient, View.OnAttachStateChangeListener {

    private val shader: RuntimeShader = LiquidGlassShader.create()
    private val node = RenderNode("IconifyLiquidGlass")
    private val clipPath = Path()
    private val clipRect = RectF()
    private val fallbackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = tint }
    private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val matrix = Matrix()
    private val location = IntArray(2)
    private var frame: BackdropFrame? = null
    private var released = false

    override val displayId: Int
        get() = host.display?.displayId ?: 0

    init {
        host.addOnAttachStateChangeListener(this)
        if (host.isAttachedToWindow) BackdropCapture.register(this)
    }

    fun release() {
        if (released) return
        released = true
        host.removeOnAttachStateChangeListener(this)
        BackdropCapture.unregister(this)
        node.discardDisplayList()
        frame = null
    }

    override fun onViewAttachedToWindow(v: View) {
        if (!released) BackdropCapture.register(this)
    }

    override fun onViewDetachedFromWindow(v: View) {
        BackdropCapture.unregister(this)
    }

    override fun captureRect(out: Rect): Boolean {
        if (released || !host.isAttachedToWindow || !host.isShown) return false
        if (host.width <= 0 || host.height <= 0) return false
        host.getLocationOnScreen(location)
        out.set(location[0], location[1], location[0] + host.width, location[1] + host.height)
        return true
    }

    override fun excludeLayer(): SurfaceControl? =
        host.callMethodSilently("getViewRootImpl")?.callMethodSilently("getSurfaceControl") as? SurfaceControl

    override fun onBackdrop(frame: BackdropFrame) {
        if (released) return
        this.frame = frame
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        if (bounds.isEmpty) return

        clipRect.set(bounds)
        clipPath.reset()
        clipPath.addRoundRect(clipRect, cornerRadius, cornerRadius, Path.Direction.CW)

        val current = frame
        if (current == null || !canvas.isHardwareAccelerated || current.bitmap.isRecycled) {
            canvas.drawPath(clipPath, fallbackPaint)
            return
        }

        val pad = (blurRadius + bounds.height() * 0.2f).toInt()
        val width = bounds.width() + pad * 2
        val height = bounds.height() + pad * 2

        host.getLocationOnScreen(location)
        val left = location[0] + bounds.left - pad
        val top = location[1] + bounds.top - pad

        node.setPosition(0, 0, width, height)
        val recording = node.beginRecording(width, height)
        matrix.reset()
        matrix.setScale(1f / current.scale, 1f / current.scale)
        matrix.postTranslate(
            (current.screenRect.left - left).toFloat(),
            (current.screenRect.top - top).toFloat()
        )
        recording.drawBitmap(current.bitmap, matrix, bitmapPaint)
        node.endRecording()

        LiquidGlassShader.update(
            shader,
            pad.toFloat(),
            pad.toFloat(),
            bounds.width().toFloat(),
            bounds.height().toFloat(),
            cornerRadius,
            tint,
            style()
        )
        val glass = RenderEffect.createRuntimeShaderEffect(shader, "image")
        val effect = if (blurRadius > 0f) {
            RenderEffect.createChainEffect(
                glass,
                RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
            )
        } else {
            glass
        }
        node.setRenderEffect(effect)

        canvas.save()
        canvas.clipPath(clipPath)
        canvas.translate((bounds.left - pad).toFloat(), (bounds.top - pad).toFloat())
        canvas.drawRenderNode(node)
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {
        node.alpha = alpha / 255f
        fallbackPaint.alpha = Color.alpha(tint) * alpha / 255
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
