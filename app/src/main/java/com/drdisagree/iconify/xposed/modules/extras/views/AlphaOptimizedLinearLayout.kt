package com.drdisagree.iconify.xposed.modules.extras.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

/**
 * A linear layout which does not have overlapping renderings commands and therefore does not need a
 * layer when alpha is changed.
 */
class AlphaOptimizedLinearLayout : LinearLayout {

    constructor(context: Context?) : super(context)

    constructor(
        context: Context?,
        attrs: AttributeSet?
    ) : super(
        context,
        attrs
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    override fun hasOverlappingRendering(): Boolean {
        return false
    }
}