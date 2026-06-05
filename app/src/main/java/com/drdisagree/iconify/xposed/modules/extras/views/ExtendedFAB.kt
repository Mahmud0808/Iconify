package com.drdisagree.iconify.xposed.modules.extras.views

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class ExtendedFAB @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ExtendedFloatingActionButton(
    createMonetM3Context(context),
    attrs,
    defStyleAttr
)