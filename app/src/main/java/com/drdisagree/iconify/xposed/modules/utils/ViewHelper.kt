package com.drdisagree.iconify.xposed.modules.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.RenderMode
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.common.Preferences
import com.drdisagree.iconify.common.Resources.LOCKSCREEN_CLOCK_LOTTIE
import com.drdisagree.iconify.config.XPrefs.Xprefs
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.callMethod
import java.io.InputStream


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

                    else -> {
                        XposedBridge.log("Unsupported type: $layoutParams")
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

    private fun checkTagAndChangeColor(view: View, tag: String, color: Int) {
        if (view.tag?.toString()?.let { isTagMatch(tag, it) } == true) {
            changeViewColor(view, color)
        }
    }

    private fun changeViewColor(view: View, color: Int) {
        when (view) {
            is TextView -> {
                view.setTextColor(color)

                val drawablesRelative: Array<Drawable?> = view.getCompoundDrawablesRelative()
                for (drawable in drawablesRelative) {
                    drawable?.let {
                        it.mutate()
                        it.setTint(color)
                        it.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
                    }
                }

                val drawables: Array<Drawable?> = view.getCompoundDrawables()
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
                view.setProgressTintList(ColorStateList.valueOf(color))
                view.setProgressBackgroundTintList(ColorStateList.valueOf(color))
            }

            else -> {
                view.background.mutate().setTint(color)
            }
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
                child.setLayoutParams(params)
            }

            is FrameLayout.LayoutParams -> {
                params.topMargin += topMarginInDp
                child.setLayoutParams(params)
            }

            is RelativeLayout.LayoutParams -> {
                params.topMargin += topMarginInDp
                child.setLayoutParams(params)
            }

            else -> {
                XposedBridge.log("Invalid params: $params")
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

    fun loadLottieAnimationView(
        appContext: Context,
        lottieAnimationViewClass: Class<*>? = null,
        parent: View,
        styleIndex: Int? = null
    ) {
        if (parent !is ViewGroup ||
            parent.findViewContainsTag("lottie") == null ||
            (lottieAnimationViewClass == null && styleIndex == null)
        ) return

        var isXposedMode = true
        val currentStyleIndex: Int? = try {
            Xprefs!!.getInt(Preferences.LSCLOCK_STYLE, 0)
        } catch (ignored: Throwable) {
            if (styleIndex == null) {
                throw IllegalStateException("Parameter \"styleIndex\" cannot be null")
            }

            isXposedMode = false
            styleIndex
        }
        val rawResName = LOCKSCREEN_CLOCK_LOTTIE + currentStyleIndex

        val lottieAnimView: Any = if (isXposedMode) {
            if (lottieAnimationViewClass == null) {
                throw IllegalStateException("Parameter \"lottieAnimationViewClass\" cannot be null")
            }

            lottieAnimationViewClass
                .getConstructor(Context::class.java)
                .newInstance(appContext)
        } else {
            LottieAnimationView(appContext)
        }

        val animationParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        ).apply {
            gravity = Gravity.CENTER
        }

        @SuppressLint("DiscouragedApi")
        val anim: Int = appContext.resources.getIdentifier(
            rawResName,
            "raw",
            BuildConfig.APPLICATION_ID
        )

        if (anim == 0x0) {
            if (isXposedMode) {
                XposedBridge.log("Iconify - ${ViewHelper::class.simpleName}: $rawResName not found")
            } else {
                Log.w(ViewHelper::class.simpleName, "$rawResName not found")
            }
            return
        }

        val rawRes: InputStream = appContext.resources.openRawResource(anim)

        if (isXposedMode) {
            lottieAnimView.let {
                callMethod(it, "setLayoutParams", animationParams)
                callMethod(it, "setAnimation", rawRes, "cacheKey")
                callMethod(it, "setRepeatCount", LottieDrawable.INFINITE)
                callMethod(it, "setScaleType", ImageView.ScaleType.FIT_CENTER)
                callMethod(it, "setAdjustViewBounds", true)
                callMethod(it, "enableMergePathsForKitKatAndAbove", true)
                callMethod(it, "playAnimation")
            }
        } else {
            (lottieAnimView as LottieAnimationView).apply {
                layoutParams = animationParams
                setAnimation(rawRes, "cacheKey")
                repeatCount = LottieDrawable.INFINITE
                renderMode = RenderMode.HARDWARE
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                enableMergePathsForKitKatAndAbove(true)
                playAnimation()
            }
        }

        (parent.findViewContainsTag("lottie") as LinearLayout).let {
            it.gravity = Gravity.CENTER

            if (isXposedMode) {
                callMethod(it, "addView", lottieAnimView);
            } else {
                it.addView(lottieAnimView as LottieAnimationView)
            }
        }
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

    private fun isTagMatch(tagToCheck: String, targetTag: String): Boolean {
        val parts = targetTag.split("|")
        return parts.any { it.trim() == tagToCheck }
    }
}
