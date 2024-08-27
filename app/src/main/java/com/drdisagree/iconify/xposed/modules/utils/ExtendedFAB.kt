package com.drdisagree.iconify.xposed.modules.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import com.drdisagree.iconify.R
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class ExtendedFAB @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ExtendedFloatingActionButton(ContextThemeWrapper(object : ContextWrapper(context) {
        override fun getResources(): Resources {
            return modRes
        }
    }, R.style.Theme_MaterialComponents_DayNight), attrs, defStyleAttr)