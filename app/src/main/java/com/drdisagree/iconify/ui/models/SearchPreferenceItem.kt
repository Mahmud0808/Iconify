package com.drdisagree.iconify.ui.models

import androidx.annotation.StringRes
import androidx.annotation.XmlRes
import androidx.fragment.app.Fragment

class SearchPreferenceItem(
    @field:XmlRes val xml: Int,
    @field:StringRes val title: Int,
    val fragment: Fragment,
    val shouldAdd: Boolean = true
)
