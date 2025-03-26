package com.drdisagree.iconify.xposed.modules.extras.utils

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
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.DEPTH_WALLPAPER_SWITCH
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_DEPTH_WALLPAPER_FOREGROUND_TAG
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_LOCKSCREEN_CONTAINER_TAG
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callStaticMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.isMethodAvailable
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs

object ViewHelper {

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

    fun setPaddings(
        viewGroup: ViewGroup,
        context: Context,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        viewGroup.setPadding(
            context.toPx(left),
            context.toPx(top),
            context.toPx(right),
            context.toPx(bottom)
        )
    }

    fun Context.toPx(dp: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        resources.displayMetrics
    ).toInt()

    fun findViewWithTagAndChangeColor(view: View?, tagContains: String, color: Int) {
        if (view == null) return

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
        tagContains: String,
        color1: Int,
        color2: Int,
        cornerRadius: Int
    ) {
        if (view == null) return

        val drawable = GradientDrawable()
        drawable.colors = intArrayOf(color1, color2)
        drawable.orientation = GradientDrawable.Orientation.LEFT_RIGHT
        drawable.cornerRadius = view.context.toPx(cornerRadius).toFloat()

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child: View = view.getChildAt(i)
                checkTagAndChangeBackgroundColor(child, tagContains, drawable)

                if (child is ViewGroup) {
                    checkTagAndChangeBackgroundColor(child, tagContains, drawable)
                }
            }
        } else {
            checkTagAndChangeBackgroundColor(view, tagContains, drawable)
        }

    }

    private fun checkTagAndChangeColor(view: View, tag: String, color: Int) {
        if (view.tag?.toString()?.let { isTagMatch(tag, it) } == true) {
            changeViewColor(view, color)
        }
    }

    private fun checkTagAndChangeBackgroundColor(view: View, tag: String, bkg: Drawable) {
        if (view.tag?.toString()?.let { isTagMatch(tag, it) } == true) {
            changeViewBackgroundColor(view, bkg)
        }
    }

    private fun changeViewColor(view: View, color: Int) {
        when (view) {
            is TextView -> {
                view.setTextColor(color)

                val drawablesRelative: Array<Drawable?> = view.compoundDrawablesRelative
                for (drawable in drawablesRelative) {
                    drawable?.let {
                        it.mutate()
                        it.setTint(color)
                        it.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
                    }
                }

                val drawables: Array<Drawable?> = view.compoundDrawables
                for (drawable in drawables) {
                    drawable?.let {
                        it.mutate()
                        it.setTint(color)
                        it.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
                    }
                }
            }

            is ImageView -> {
                view.setColorFilter(color)
            }

            is ViewGroup -> {
                view.setBackgroundTintList(ColorStateList.valueOf(color))
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

    private fun changeViewBackgroundColor(view: View, bkg: Drawable) {
        view.background = bkg
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

    private fun setTextMargins(child: View, topMarginInDp: Int) {
        if (child.tag?.toString()?.let { isTagMatch("nolineheight", it) } == true) {
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

    fun applyTextScalingRecursively(view: View?, scaleFactor: Float) {
        if (view == null) return

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

    private fun setTextScaling(view: View, scaleFactor: Float) {
        val originalSize = (view as TextView).textSize
        val newSize = originalSize * scaleFactor
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize)
    }

    fun View.findViewContainsTag(tag: String): View? {
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)

                if (child.tag?.toString()?.let { isTagMatch(tag, it) } == true) {
                    return child
                }

                if (child is ViewGroup) {
                    val result = child.findViewContainsTag(tag)
                    if (result != null) {
                        return result
                    }
                }
            }
        } else {
            if (getTag()?.toString()?.let { isTagMatch(tag, it) } == true) {
                return this
            }
        }

        return null
    }

    fun View.findViewIdContainsTag(tag: String): Int {
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)

                if (child.tag?.toString()?.let { isTagMatch(tag, it) } == true) {
                    return i
                }

                if (child is ViewGroup) {
                    val result = child.findViewContainsTag(tag)
                    if (result != null) {
                        return i
                    }
                }
            }
        } else {
            if (getTag()?.toString()?.let { isTagMatch(tag, it) } == true) {
                return 0
            }
        }

        return -1
    }

    fun View.findChildIndexContainsTag(tag: String): Int {
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                if (getChildAt(i).tag?.toString()?.let { isTagMatch(tag, it) } == true) {
                    return i
                }
            }
        }
        return -1
    }

    private fun isTagMatch(tagToCheck: String, targetTag: String): Boolean {
        val parts = targetTag.split("|")
        return parts.any { it.trim() == tagToCheck }
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
        if (radius == 0f) {
            return this
        }

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
        val renderScript = android.renderscript.RenderScript.create(context)
        val blurInput = android.renderscript.Allocation.createFromBitmap(renderScript, tempImage)
        val blurOutput = android.renderscript.Allocation.createFromBitmap(renderScript, bitmap)

        android.renderscript.ScriptIntrinsicBlur.create(
            renderScript,
            android.renderscript.Element.U8_4(renderScript)
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

            val showDepthWallpaper = Xprefs.getBoolean(DEPTH_WALLPAPER_SWITCH, false)
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
            addView(view, index.coerceAtMost(childCount))
        }
    }

    fun View?.removeViewFromParent() {
        if (this == null) return
        (parent as? ViewGroup)?.removeView(this)
    }

    fun View?.getExpandableView(): Any? {
        if (this == null) {
            log("getExpandableView", "View is null")
            return null
        }

        val expandableClass = findClass("$SYSTEMUI_PACKAGE.animation.Expandable")
        val expandableCompanionFromViewClass = findClass(
            "$SYSTEMUI_PACKAGE.animation.Expandable\$Companion\$fromView",
            "$SYSTEMUI_PACKAGE.animation.Expandable\$Companion\$fromView\$1",
            "$SYSTEMUI_PACKAGE.animation.Expandable\$Companion\$fromView\$2",
            "$SYSTEMUI_PACKAGE.animation.Expandable\$Companion\$fromView\$3",
            suppressError = true
        )

        return if (expandableClass.isMethodAvailable("fromView", View::class.java)) {
            expandableClass!!.callStaticMethod("fromView", this)
        } else {
            try {
                expandableCompanionFromViewClass!!
                    .getConstructor(View::class.java)
                    .newInstance(this)
            } catch (_: Throwable) {
                val refObjectRefClass = findClass("kotlin.jvm.internal.Ref\$ObjectRef")

                val refObject = refObjectRefClass!!
                    .getConstructor()
                    .newInstance()

                try {
                    refObject.setField("element", callMethod("getAnimatedView"))
                } catch (_: Throwable) {
                    refObject.setField("element", this)
                }

                expandableCompanionFromViewClass!!
                    .getConstructor(refObjectRefClass)
                    .newInstance(refObject)
            }
        }
    }

    fun Drawable.getColored(context: Context, color: Int): Drawable {

        val colorDrawable = this.getColoredBitmap(color)

        return colorDrawable?.toDrawable(context.resources) ?: this
    }

    private fun Drawable?.getColoredBitmap(color: Int): Bitmap? {
        if (this == null) return null

        val colorBitmap = (this as BitmapDrawable).bitmap
        val grayscaleBitmap = colorBitmap.toGrayscale()
        val paint = Paint().apply {
            isAntiAlias = true
            colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
        }
        val canvas = Canvas(grayscaleBitmap)
        val rect = Rect(0, 0, grayscaleBitmap.width, grayscaleBitmap.height)
        canvas.drawBitmap(grayscaleBitmap, rect, rect, paint)

        return grayscaleBitmap
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