package com.drdisagree.iconify.data.states

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
sealed class UiText : Parcelable {

    @Parcelize
    data class Text(val value: String) : UiText()

    @Parcelize
    data class Res(
        @param:StringRes val resId: Int,
        val args: @RawValue Array<out Any> = emptyArray()
    ) : UiText() {

        companion object {
            fun of(
                @StringRes resId: Int,
                vararg args: Any
            ): Res = Res(resId, args)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Res
            return resId == other.resId && args.contentEquals(other.args)
        }

        override fun hashCode(): Int =
            31 * resId + args.contentHashCode()
    }
}

/**
 * Extension to convert UiText to String inside Compose
 */
@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Text -> value
    is UiText.Res -> if (args.isEmpty()) {
        stringResource(resId)
    } else {
        stringResource(resId, *args)
    }
}