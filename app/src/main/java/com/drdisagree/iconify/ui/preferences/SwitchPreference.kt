package com.drdisagree.iconify.ui.preferences

import android.content.Context
import android.content.res.Configuration
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat
import com.drdisagree.iconify.R
import com.drdisagree.iconify.utils.MiscUtil.showAlertDialog

class SwitchPreference : SwitchPreferenceCompat {

    private var requiresSystemUiRestart: Boolean = false
    private var requiresDeviceRestart = false
    private var requiresThemeSwitch = false
    private var requiresDeviceRotation = false

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initResource(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initResource(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initResource(attrs)
    }

    constructor(context: Context) : super(context) {
        initResource(null)
    }

    private fun initResource(attrs: AttributeSet?) {
        layoutResource = R.layout.custom_preference_switch
        widgetLayoutResource = R.layout.preference_material_switch

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.SwitchPreference)
            requiresSystemUiRestart =
                a.getBoolean(R.styleable.SwitchPreference_requiresSystemUiRestart, false)
            requiresDeviceRestart =
                a.getBoolean(R.styleable.SwitchPreference_requiresDeviceRestart, false)
            requiresThemeSwitch =
                a.getBoolean(R.styleable.SwitchPreference_requiresThemeSwitch, false)
            requiresDeviceRotation =
                a.getBoolean(R.styleable.SwitchPreference_requiresDeviceRotation, false)
            a.recycle()
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.findViewById<ImageView>(R.id.alert_icon)?.apply {
            visibility =
                if (requiresSystemUiRestart || requiresDeviceRestart || requiresThemeSwitch) View.VISIBLE else View.GONE

            if (requiresDeviceRestart) {
                val isDarkMode =
                    context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES == Configuration.UI_MODE_NIGHT_YES

                val typedValue = TypedValue()
                val theme = context.theme
                theme.resolveAttribute(
                    com.google.android.material.R.attr.colorError,
                    typedValue,
                    true
                )
                @ColorInt val color = typedValue.data
                colorFilter = BlendModeColorFilter(
                    color,
                    if (isDarkMode) BlendMode.SRC_ATOP else BlendMode.SRC_IN
                )
            }

            setOnClickListener {
                showAlertDialog(
                    context,
                    requiresSystemUiRestart,
                    requiresDeviceRestart,
                    requiresThemeSwitch,
                    requiresDeviceRotation
                )
            }
        }
    }
}