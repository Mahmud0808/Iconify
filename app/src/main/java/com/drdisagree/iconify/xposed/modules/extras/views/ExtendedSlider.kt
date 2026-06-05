package com.drdisagree.iconify.xposed.modules.extras.views

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.google.android.material.color.DynamicColors
import com.google.android.material.slider.Slider

class ExtendedSlider @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.sliderStyle
) : Slider(
    createMonetM3Context(context),
    attrs,
    defStyleAttr
)

fun createMonetM3Context(baseContext: Context?): Context {
    val m3Wrapper = ContextThemeWrapper(
        object : ContextWrapper(baseContext) {
            override fun getResources(): Resources {
                return modRes
            }
        },
        com.google.android.material.R.style.Theme_Material3_DayNight
    )

    return try {
        DynamicColors.wrapContextIfAvailable(m3Wrapper)
    } catch (_: Exception) {
        m3Wrapper
    }
}