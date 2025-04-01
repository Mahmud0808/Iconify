package com.drdisagree.iconify.ui.preferences

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.drdisagree.iconify.R

/**
 * The Base preference with two target areas divided by a vertical divider
 */
open class TwoTargetPreference : Preference {

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    private fun init() {
        layoutResource = R.layout.custom_preference_two_target
        val secondTargetResId = secondTargetResId

        if (secondTargetResId != 0) {
            widgetLayoutResource = secondTargetResId
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val divider = holder.findViewById(R.id.two_target_divider)
        val widgetFrame = holder.findViewById(android.R.id.widget_frame)
        val shouldHideSecondTarget = shouldHideSecondTarget()

        divider?.visibility = if (shouldHideSecondTarget) View.GONE else View.VISIBLE
        widgetFrame?.visibility = if (shouldHideSecondTarget) View.GONE else View.VISIBLE
        widgetFrame?.isEnabled = isEnabled
    }

    protected open fun shouldHideSecondTarget(): Boolean {
        return secondTargetResId == 0
    }

    protected open val secondTargetResId: Int
        get() = 0
}
