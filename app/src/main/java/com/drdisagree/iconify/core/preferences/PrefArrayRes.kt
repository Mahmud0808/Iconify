package com.drdisagree.iconify.core.preferences

import androidx.annotation.ArrayRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource

sealed class PrefArrayRes {
    data class Hardcoded(val values: List<PrefStringRes>) : PrefArrayRes()

    class Composable(val producer: @androidx.compose.runtime.Composable () -> List<String>) :
        PrefArrayRes()
}

// From a hardcoded list of PrefStringRes
@JvmName("arrayResFromPrefStringRes")
fun arrayRes(values: List<PrefStringRes>): PrefArrayRes =
    PrefArrayRes.Hardcoded(values)

// From a plain string list
@JvmName("arrayResFromStrings")
fun arrayRes(values: List<String>): PrefArrayRes =
    PrefArrayRes.Hardcoded(values.map { PrefStringRes.Hardcoded(it) })

// From an array resource ID (e.g. R.array.my_entries)
fun arrayRes(@ArrayRes id: Int): PrefArrayRes =
    PrefArrayRes.Composable { stringArrayResource(id).toList() }

@Composable
fun PrefArrayRes.resolve(): List<PrefStringRes> = when (this) {
    is PrefArrayRes.Hardcoded -> values
    is PrefArrayRes.Composable -> producer().map { PrefStringRes.Hardcoded(it) }
}

@Composable
fun PrefArrayRes.resolveToStrings(): List<String> = when (this) {
    is PrefArrayRes.Hardcoded -> values.map { it.resolve() }
    is PrefArrayRes.Composable -> producer()
}

@Composable
fun PrefArrayRes?.resolveOrNull(): List<PrefStringRes>? = this?.resolve()