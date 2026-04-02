package com.drdisagree.iconify.data.models

import android.os.Parcelable
import com.drdisagree.iconify.data.states.UiText
import kotlinx.parcelize.Parcelize

@Parcelize
data class SettingsIconsPreview(
    val title: UiText,
    val summary: UiText,
    val icons: List<Int>,
) : Parcelable