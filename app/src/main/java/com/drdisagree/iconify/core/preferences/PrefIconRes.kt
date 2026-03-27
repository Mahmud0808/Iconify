package com.drdisagree.iconify.core.preferences

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource

sealed class PrefIconRes {

    data class Drawable(@param:DrawableRes val resId: Int) : PrefIconRes()

    data class Vector(val imageVector: ImageVector) : PrefIconRes()

    class Composable(val producer: @androidx.compose.runtime.Composable () -> Painter) :
        PrefIconRes()
}

fun iconRes(@DrawableRes resId: Int): PrefIconRes =
    PrefIconRes.Drawable(resId)

fun iconRes(imageVector: ImageVector): PrefIconRes =
    PrefIconRes.Vector(imageVector)

fun iconRes(producer: @Composable () -> Painter): PrefIconRes =
    PrefIconRes.Composable(producer)

@Composable
fun PrefIconRes.resolve(): Painter = when (this) {
    is PrefIconRes.Drawable -> painterResource(resId)
    is PrefIconRes.Vector -> rememberVectorPainter(imageVector)
    is PrefIconRes.Composable -> producer()
}

@Composable
fun PrefIconRes?.resolveOrNull(): Painter? = this?.resolve()