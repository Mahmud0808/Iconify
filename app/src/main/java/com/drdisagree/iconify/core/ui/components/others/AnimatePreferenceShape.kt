package com.drdisagree.iconify.core.ui.components.others

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.drdisagree.iconify.core.ui.utils.ItemPosition
import com.drdisagree.iconify.core.ui.utils.cardCorners

@Composable
fun animatedPreferenceShape(position: ItemPosition): RoundedCornerShape {
    val target = cardCorners(position)

    val topStart by animateDpAsState(target.topStart, label = "topStart")
    val topEnd by animateDpAsState(target.topEnd, label = "topEnd")
    val bottomStart by animateDpAsState(target.bottomStart, label = "bottomStart")
    val bottomEnd by animateDpAsState(target.bottomEnd, label = "bottomEnd")

    return RoundedCornerShape(
        topStart = topStart,
        topEnd = topEnd,
        bottomStart = bottomStart,
        bottomEnd = bottomEnd
    )
}