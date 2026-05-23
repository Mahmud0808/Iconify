@file:Suppress("DEPRECATION")

package com.drdisagree.iconify.xposed.modules.extras.utils.misc

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_DEPTH_WALLPAPER_FOREGROUND_TAG
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_LOCKSCREEN_CONTAINER_TAG
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs

@Suppress("unused")
object ViewHelper {

    fun Context.toPx(dp: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        resources.displayMetrics
    ).toInt()

    fun setMargins(viewGroup: Any, context: Context, left: Int, top: Int, right: Int, bottom: Int) {
        when (viewGroup) {
            is View -> {
                when (val layoutParams = viewGroup.layoutParams) {
                    is LinearLayout.LayoutParams -> {
                        layoutParams.setMargins(
                            context.toPx(left),
                            context.toPx(top),
                            context.toPx(right),
                            context.toPx(bottom)
                        )
                    }

                    is FrameLayout.LayoutParams -> {
                        layoutParams.setMargins(
                            context.toPx(left),
                            context.toPx(top),
                            context.toPx(right),
                            context.toPx(bottom)
                        )
                    }

                    is RelativeLayout.LayoutParams -> {
                        layoutParams.setMargins(
                            context.toPx(left),
                            context.toPx(top),
                            context.toPx(right),
                            context.toPx(bottom)
                        )
                    }

                    is ConstraintLayout.LayoutParams -> {
                        layoutParams.setMargins(
                            context.toPx(left),
                            context.toPx(top),
                            context.toPx(right),
                            context.toPx(bottom)
                        )
                    }

                    else -> {
                        if (layoutParams != null) {
                            log(this@ViewHelper, "Unsupported type: $layoutParams")
                        }
                    }
                }
            }

            is MarginLayoutParams -> {
                viewGroup.setMargins(
                    context.toPx(left),
                    context.toPx(top),
                    context.toPx(right),
                    context.toPx(bottom)
                )
            }

            else -> {
                throw IllegalArgumentException("The viewGroup object has to be either a View or a ViewGroup.MarginLayoutParams. Found ${viewGroup.javaClass.simpleName} instead.")
            }
        }
    }

    fun View.setPaddingDp(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        setPadding(
            context.toPx(left),
            context.toPx(top),
            context.toPx(right),
            context.toPx(bottom)
        )
    }

    fun findViewWithTagAndChangeColor(view: View?, tagContains: String, color: Int) {
        if (view == null) return

        fun checkTagAndChangeColor(view: View, tag: String, color: Int) {
            if (view.containsTag(tag)) {
                when (view) {
                    is TextView -> {
                        view.setTextColor(color)

                        val drawablesRelative: Array<Drawable?> = view.compoundDrawablesRelative
                        for (drawable in drawablesRelative) {
                            drawable?.let {
                                it.mutate()
                                it.setTint(color)
                                it.colorFilter =
                                    PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
                            }
                        }

                        val drawables: Array<Drawable?> = view.compoundDrawables
                        for (drawable in drawables) {
                            drawable?.let {
                                it.mutate()
                                it.setTint(color)
                                it.colorFilter =
                                    PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
                            }
                        }
                    }

                    is ImageView -> {
                        view.setColorFilter(color)
                    }

                    is ViewGroup -> {
                        view.backgroundTintList = ColorStateList.valueOf(color)
                    }

                    is ProgressBar -> {
                        view.progressTintList = ColorStateList.valueOf(color)
                        view.progressBackgroundTintList = ColorStateList.valueOf(color)
                    }

                    else -> {
                        view.background.mutate().setTint(color)
                    }
                }
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child: View = view.getChildAt(i)
                checkTagAndChangeColor(child, tagContains, color)

                if (child is ViewGroup) {
                    findViewWithTagAndChangeColor(child, tagContains, color)
                }
            }
        } else {
            checkTagAndChangeColor(view, tagContains, color)
        }
    }

    fun findViewWithTagAndChangeColor(
        view: View?,
        targetTag: String,
        color1: Int,
        color2: Int,
        cornerRadius: Int
    ) {
        if (view == null) return

        fun checkTagAndChangeBackgroundColor(view: View, tag: String, drawable: Drawable) {
            if (view.containsTag(tag)) {
                view.background = drawable
            }
        }

        val drawable = GradientDrawable()
        drawable.colors = intArrayOf(color1, color2)
        drawable.orientation = GradientDrawable.Orientation.LEFT_RIGHT
        drawable.cornerRadius = view.context.toPx(cornerRadius).toFloat()

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child: View = view.getChildAt(i)
                checkTagAndChangeBackgroundColor(child, targetTag, drawable)

                if (child is ViewGroup) {
                    checkTagAndChangeBackgroundColor(child, targetTag, drawable)
                }
            }
        } else {
            checkTagAndChangeBackgroundColor(view, targetTag, drawable)
        }

    }

    fun applyFontRecursively(view: View?, typeface: Typeface?) {
        if (view == null) return

        if (view is ViewGroup) {
            val childCount: Int = view.childCount

            for (i in 0 until childCount) {
                val child: View = view.getChildAt(i)

                if (child is ViewGroup) {
                    applyFontRecursively(child, typeface)
                } else (child as? TextView)?.setTypeface(typeface)
            }
        } else (view as? TextView)?.setTypeface(typeface)
    }

    fun applyTextMarginRecursively(context: Context, view: View?, topMargin: Int) {
        if (view == null) return

        fun setTextMargins(child: View, topMarginInDp: Int) {
            if (child.containsTag("nolineheight")) {
                return
            }

            when (val params = child.layoutParams) {
                is LinearLayout.LayoutParams -> {
                    params.topMargin += topMarginInDp
                    child.layoutParams = params
                }

                is FrameLayout.LayoutParams -> {
                    params.topMargin += topMarginInDp
                    child.layoutParams = params
                }

                is RelativeLayout.LayoutParams -> {
                    params.topMargin += topMarginInDp
                    child.layoutParams = params
                }

                else -> {
                    log(this@ViewHelper, "Invalid params: $params")
                }
            }
        }

        val topMarginInDp = context.toPx(topMargin)

        if (view is ViewGroup) {
            val childCount: Int = view.childCount

            for (i in 0 until childCount) {
                val child: View = view.getChildAt(i)

                if (child is ViewGroup) {
                    applyTextMarginRecursively(context, child, topMargin)
                } else if (child is TextView) {
                    setTextMargins(child, topMarginInDp)
                }
            }
        } else if (view is TextView) {
            setTextMargins(view, topMarginInDp)
        }
    }

    fun applyTextScalingRecursively(view: View?, scaleFactor: Float) {
        if (view == null) return

        fun setTextScaling(view: View, scaleFactor: Float) {
            val originalSize = (view as TextView).textSize
            val newSize = originalSize * scaleFactor
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize)
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child: View = view.getChildAt(i)

                if (child is ViewGroup) {
                    applyTextScalingRecursively(child, scaleFactor)
                } else if (child is TextView) {
                    setTextScaling(child, scaleFactor)
                }
            }
        } else if (view is TextView) {
            setTextScaling(view, scaleFactor)
        }
    }

    fun View.findViewContainingTag(tag: String): View? {
        val queue = ArrayDeque<View>()
        queue.add(this)

        while (queue.isNotEmpty()) {
            val view = queue.removeFirst()

            if (view.containsTag(tag)) {
                return view
            }

            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    queue.add(view.getChildAt(i))
                }
            }
        }

        return null
    }

    fun View.findViewsContainingTag(tag: String): List<View> {
        val result = mutableListOf<View>()
        val queue = ArrayDeque<View>()
        queue.add(this)

        while (queue.isNotEmpty()) {
            val view = queue.removeFirst()

            if (view.containsTag(tag)) {
                result.add(view)
            }

            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    queue.add(view.getChildAt(i))
                }
            }
        }

        return result
    }

    fun View.findChildIndexContainsTag(tag: String): Int {
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                if (getChildAt(i).containsTag(tag)) {
                    return i
                }
            }
        }
        return -1
    }

    private fun View.containsTag(targetTag: String): Boolean {
        return tag?.toString()?.split("|")?.any { it.trim() == targetTag } == true
    }

    fun Drawable.applyBlur(context: Context, radius: Float): Drawable {
        if (radius == 0f) {
            return this
        }

        val blurredBitmap = drawableToBitmap().applyBlur(context, radius.coerceIn(1f, 25f))

        return blurredBitmap.toDrawable(context.resources)
    }

    private fun Drawable.drawableToBitmap(): Bitmap {
        if (this is BitmapDrawable) {
            return bitmap
        }

        val bitmap = createBitmap(intrinsicWidth, intrinsicHeight)

        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)

        return bitmap
    }

    @Suppress("deprecation")
    fun Bitmap.applyBlur(context: Context, radius: Float): Bitmap {
        if (radius == 0f) return this

        var tempImage = this

        if (config == Bitmap.Config.HARDWARE) {
            tempImage = copy(Bitmap.Config.ARGB_8888, true)
        }

        try {
            tempImage = rgb565toArgb888()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val bitmap = createBitmap(tempImage.width, tempImage.height)
        val renderScript = RenderScript.create(context)
        val blurInput = Allocation.createFromBitmap(renderScript, tempImage)
        val blurOutput = Allocation.createFromBitmap(renderScript, bitmap)

        ScriptIntrinsicBlur.create(
            renderScript,
            Element.U8_4(renderScript)
        ).apply {
            setInput(blurInput)
            setRadius(radius.coerceIn(0.0001f, 25f))
            forEach(blurOutput)
        }

        blurOutput.copyTo(bitmap)
        renderScript.destroy()

        return bitmap
    }

    private fun Bitmap.rgb565toArgb888(): Bitmap {
        val numPixels = width * height
        val pixels = IntArray(numPixels)

        // Get JPEG pixels. Each int is the color values for one pixel.
        getPixels(pixels, 0, width, 0, 0, width, height)

        // Create a Bitmap of the appropriate format.
        val result = createBitmap(width, height)

        // Set RGB pixels.
        result.setPixels(pixels, 0, result.width, 0, 0, result.width, result.height)

        return result
    }

    fun View?.hideView() {
        if (this == null) return

        fun makeInvisible() {
            apply {
                if (isVisible) {
                    visibility = View.INVISIBLE
                }
                if (this is TextView) {
                    setTextColor(Color.TRANSPARENT)
                    compoundDrawablesRelative.forEach { it?.setTint(Color.TRANSPARENT) }
                    compoundDrawables.forEach { it?.setTint(Color.TRANSPARENT) }
                } else if (this is ImageView) {
                    setColorFilter(Color.TRANSPARENT)
                }
            }
        }

        fun makeSizeZero() {
            apply {
                layoutParams.apply {
                    if (height != 0) height = 0
                    if (width != 0) width = 0
                }
            }
        }

        makeSizeZero()
        makeInvisible()

        viewTreeObserver?.addOnGlobalLayoutListener {
            makeSizeZero()
            makeInvisible()
        }
        viewTreeObserver?.addOnDrawListener {
            makeInvisible()
        }
    }

    fun View?.assignIdsToViews() {
        if (this == null) return

        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)

                if (child is ViewGroup) {
                    child.assignIdsToViews()
                }

                if (child.id == View.NO_ID) {
                    child.id = View.generateViewId()
                }
            }
        } else {
            if (id == View.NO_ID) {
                id = View.generateViewId()
            }
        }
    }

    fun ViewGroup?.getLsItemsContainer(): LinearLayout? {
        if (this == null) return null

        synchronized(ViewHelper) {
            var layout: LinearLayout? = findViewWithTag(ICONIFY_LOCKSCREEN_CONTAINER_TAG)

            val showDepthWallpaper = Xprefs.getBoolean(XposedKey.LOCKSCREEN_DEPTH_WALLPAPER)
            val idx = if (showDepthWallpaper) {
                val tempIdx = findChildIndexContainsTag(ICONIFY_DEPTH_WALLPAPER_FOREGROUND_TAG)
                if (tempIdx == -1) 0 else tempIdx
            } else {
                0
            }

            if (layout == null) {
                layout = LinearLayout(this.context).apply {
                    id = View.generateViewId()
                    tag = ICONIFY_LOCKSCREEN_CONTAINER_TAG
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.VERTICAL
                }
                addView(layout, idx)
            } else {
                if (indexOfChild(layout) != idx) {
                    reAddView(layout, idx)
                }
            }

            return layout
        }
    }

    fun ViewGroup.reAddView(childView: View?) {
        reAddView(childView, -1)
    }

    fun ViewGroup.reAddView(childView: View?, index: Int) {
        childView?.let { view ->
            val currentIndex = indexOfChild(view)

            if (currentIndex != -1) {
                val tempChildCount = childCount
                val adjustedIndex = if (index >= tempChildCount) tempChildCount - 1 else index

                if ((index != -1 && currentIndex == adjustedIndex) ||
                    (index == -1 && currentIndex == tempChildCount - 1)
                ) return
            }

            view.removeViewFromParent()
            addView(view, index.coerceIn(0, childCount))
        }
    }

    fun View?.removeViewFromParent() {
        if (this == null) return
        try {
            (parent as? ViewGroup)?.removeView(this)
        } catch (_: Exception) {
        }
    }

    fun Drawable.getColored(context: Context, color: Int): Drawable {
        val colorBitmap = (this as BitmapDrawable).bitmap
        val grayscaleBitmap = colorBitmap.toGrayscale()
        val paint = Paint().apply {
            isAntiAlias = true
            colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
        }
        val canvas = Canvas(grayscaleBitmap)
        val rect = Rect(0, 0, grayscaleBitmap.width, grayscaleBitmap.height)

        canvas.drawBitmap(grayscaleBitmap, rect, rect, paint)

        return grayscaleBitmap.toDrawable(context.resources)
    }

    fun Drawable?.getColoredBitmap(color: Int, intensity: Int): Bitmap? {
        if (this == null) return null

        val colorBitmap = (this as BitmapDrawable).bitmap
        val filteredBitmap = createBitmap(
            colorBitmap.width,
            colorBitmap.height,
            colorBitmap.config ?: Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(filteredBitmap)
        val paint = Paint()
        val fadeFilter = ColorUtils.blendARGB(Color.TRANSPARENT, color, intensity / 100f)
        paint.colorFilter = PorterDuffColorFilter(fadeFilter, PorterDuff.Mode.SRC_ATOP)
        canvas.drawBitmap(colorBitmap, 0f, 0f, paint)

        return filteredBitmap
    }

    fun Bitmap.toGrayscale(): Bitmap {
        val grayscaleBitmap = createBitmap(width, height)
        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint().apply {
            isAntiAlias = true
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        val rect = Rect(0, 0, width, height)
        canvas.drawBitmap(this, rect, rect, paint)
        return grayscaleBitmap
    }

    fun Drawable.toGrayscale(context: Context): Drawable {
        val grayscaleBitmap = drawableToBitmap().toGrayscale()
        return grayscaleBitmap.toDrawable(context.resources)
    }

    fun Drawable.getGrayscaleBlurredImage(context: Context, radius: Float): Drawable {
        val grayscaleBitmap = drawableToBitmap().getGrayscaleBlurredImage(context, radius)
        return grayscaleBitmap.toDrawable(context.resources)
    }

    fun Bitmap.getGrayscaleBlurredImage(context: Context, radius: Float): Bitmap {
        return applyBlur(context, radius).toGrayscale()
    }

    fun Bitmap?.centerCropBitmap(targetWidth: Int, targetHeight: Int): Bitmap? {
        if (this == null) return null

        val srcAspectRatio = width.toFloat() / height.toFloat()
        val targetAspectRatio = targetWidth.toFloat() / targetHeight.toFloat()

        val scale: Float
        val dx: Float
        val dy: Float

        if (srcAspectRatio > targetAspectRatio) {
            scale = targetHeight.toFloat() / height.toFloat()
            dx = (targetWidth - width * scale) / 2
            dy = 0f
        } else {
            scale = targetWidth.toFloat() / width.toFloat()
            dx = 0f
            dy = (targetHeight - height * scale) / 2
        }

        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)

        val resultBitmap =
            createBitmap(targetWidth, targetHeight, config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(this, matrix, Paint(Paint.FILTER_BITMAP_FLAG))

        return resultBitmap
    }

    @SuppressLint("UseKtx")
    fun Drawable.toCircularDrawable(context: Context): Drawable {
        val bitmap = this.toBitmap()
        val circularBitmap = bitmap.toCircularBitmap()
        return BitmapDrawable(context.resources, circularBitmap)
    }

    fun Bitmap.toCircularBitmap(): Bitmap {
        var tempImage = this

        if (config == Bitmap.Config.HARDWARE) {
            tempImage = copy(Bitmap.Config.ARGB_8888, true)
        }

        val width = tempImage.width
        val height = tempImage.height
        val diameter = width.coerceAtMost(height)
        val output = createBitmap(diameter, diameter)

        val paint = Paint()
        paint.isAntiAlias = true

        val canvas = Canvas(output)
        val rect = Rect(0, 0, diameter, diameter)
        val rectF = RectF(rect)

        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawOval(rectF, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val left = (width - diameter) / 2
        val top = (height - diameter) / 2
        canvas.drawBitmap(tempImage, Rect(left, top, left + diameter, top + diameter), rect, paint)

        return output
    }
}