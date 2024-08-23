package com.drdisagree.iconify.ui.preferences

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceViewHolder
import com.drdisagree.iconify.R
import com.drdisagree.iconify.utils.MiscUtil.showSystemUiRestartDialog

class EditTextPreference : EditTextPreference {

    private var requiresRestart: Boolean = false

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
        layoutResource = R.layout.custom_preference_edit_text

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.SwitchPreference)
            requiresRestart = a.getBoolean(R.styleable.SwitchPreference_requiresRestart, false)
            a.recycle()
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.findViewById<ImageView>(R.id.alert_icon)?.apply {
            visibility = if (requiresRestart) View.VISIBLE else View.GONE

            setOnClickListener {
                showSystemUiRestartDialog(context)
            }
        }
    }
}
