package com.drdisagree.iconify.ui.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.drdisagree.iconify.R
import com.google.android.material.button.MaterialButton

class FilePickerPreference : Preference {

    private var mOnClick: () -> Unit? = {}
    private var mButtonText = ""

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context) : super(context) {
        init(null)
    }

    private fun init(attrs: AttributeSet?) {
        isSelectable = false
        layoutResource = R.layout.custom_preference_filepicker

        attrs?.let { it ->
            val a = context.obtainStyledAttributes(it, R.styleable.FilePickerPreference)
            val customText: String? = a.getString(R.styleable.FilePickerPreference_buttonText)
            mButtonText =
                customText ?: a.getResourceId(R.styleable.FilePickerPreference_buttonText, 0)
                    .takeIf { it != 0 }
                    ?.let { context.getString(it) } ?: context.getString(R.string.btn_pick_image)
            a.recycle()
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.findViewById<MaterialButton>(R.id.btn_picker)?.apply {
            text = mButtonText
            this.isEnabled = isEnabled
            setOnClickListener { mOnClick.invoke() }
        }
    }

    fun setOnClick(onClick: () -> Unit) {
        mOnClick = onClick
    }
}
