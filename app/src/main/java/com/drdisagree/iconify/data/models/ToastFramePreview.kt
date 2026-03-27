package com.drdisagree.iconify.data.models

import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.drdisagree.iconify.data.states.UiText
import kotlinx.parcelize.Parcelize

@Parcelize
data class ToastFramePreview(
    val id: String,
    val title: UiText,
    @param:DrawableRes val toastStyle: Int,
    val isApplied: Boolean = false,
) : Parcelable