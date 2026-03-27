package com.drdisagree.iconify.core.preferences

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class PrefStringRes {
    data class Hardcoded(val value: String) : PrefStringRes()

    class Composable(val producer: @androidx.compose.runtime.Composable () -> String) :
        PrefStringRes()
}

fun stringRes(value: String): PrefStringRes = PrefStringRes.Hardcoded(value)

fun stringRes(@StringRes id: Int): PrefStringRes =
    PrefStringRes.Composable { stringResource(id) }

fun stringRes(@StringRes id: Int, vararg formatArgs: Any): PrefStringRes =
    PrefStringRes.Composable { stringResource(id, *formatArgs) }

fun stringRes(producer: @Composable () -> String): PrefStringRes =
    PrefStringRes.Composable(producer)

@Composable
fun PrefStringRes.resolve(): String = when (this) {
    is PrefStringRes.Hardcoded -> value
    is PrefStringRes.Composable -> producer()
}

@Composable
fun PrefStringRes?.resolveOrNull(): String? = this?.resolve()