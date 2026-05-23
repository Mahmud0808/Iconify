package com.drdisagree.iconify.core.ui.components.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.ui.components.others.withHaptic

private val SINGLE_LINE_LIST_ITEM_MIN_HEIGHT = 56.dp
private val TWO_LINE_LIST_ITEM_MIN_HEIGHT = 72.dp
private val THREE_LINE_LIST_ITEM_MIN_HEIGHT = 88.dp

@Composable
fun PreferenceContainer(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    isEnabled: Boolean,
    minLine: Int = 1,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    spaceAmongItems: Dp = 16.dp,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(
                min = when {
                    minLine <= 1 -> SINGLE_LINE_LIST_ITEM_MIN_HEIGHT
                    minLine == 2 -> TWO_LINE_LIST_ITEM_MIN_HEIGHT
                    else -> THREE_LINE_LIST_ITEM_MIN_HEIGHT
                }
            )
            .clip(shape)
            .background(containerColor, shape)
            .alpha(if (isEnabled) 1f else 0.38f)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        enabled = isEnabled,
                        role = Role.Button,
                        onClick = withHaptic { onClick() }
                    )
                } else Modifier
            )
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spaceAmongItems),
        content = content
    )
}