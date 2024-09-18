package com.drdisagree.iconify.ui.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import com.drdisagree.iconify.R

class ListPreference : ListPreference {

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initResource()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initResource()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initResource()
    }

    constructor(context: Context) : super(context) {
        initResource()
    }

    private fun initResource() {
        layoutResource = R.layout.custom_preference_list
    }
}
