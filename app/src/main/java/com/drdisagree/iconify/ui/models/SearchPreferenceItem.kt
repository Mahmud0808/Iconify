package com.drdisagree.iconify.ui.models

import androidx.annotation.StringRes
import androidx.annotation.XmlRes
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat

class SearchPreferenceItem(
    @field:XmlRes val xml: Int,
    @field:StringRes val title: Int,
    val fragment: ControlledPreferenceFragmentCompat,
    val shouldAdd: Boolean = true
)
