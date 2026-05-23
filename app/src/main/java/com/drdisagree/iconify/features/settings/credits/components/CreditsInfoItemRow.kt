package com.drdisagree.iconify.features.settings.credits.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PrefStringRes
import com.drdisagree.iconify.core.ui.components.preferences.PreferenceContainer
import com.drdisagree.iconify.core.ui.components.preferences.TitleSummaryBlock
import com.drdisagree.iconify.core.ui.utils.CARD_ITEM_SPACING
import com.drdisagree.iconify.features.settings.credits.models.CreditInfoModel

@Composable
fun CreditsInfoItemRow(
    item: CreditInfoModel,
    shape: RoundedCornerShape,
    onClick: (() -> Unit)?,
    isFirstItem: Boolean
) {
    PreferenceContainer(
        modifier = Modifier.padding(top = if (isFirstItem) 0.dp else CARD_ITEM_SPACING),
        shape = shape,
        minLine = if (item.desc.isBlank()) 1 else 2,
        onClick = onClick,
        isEnabled = true
    ) {
        val showFallbackImage = remember { mutableStateOf(true) }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
        ) {
            when (val icon = item.icon) {

                is Int -> {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }

                is String -> {
                    AsyncImage(
                        model = icon,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape),
                        onState = { state ->
                            showFallbackImage.value = state is AsyncImagePainter.State.Error ||
                                    state is AsyncImagePainter.State.Loading
                        }
                    )

                    if (showFallbackImage.value) {
                        Icon(
                            painter = painterResource(R.drawable.ic_user),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }

                else -> {
                    Icon(
                        painter = painterResource(R.drawable.ic_user),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
        TitleSummaryBlock(
            title = PrefStringRes.Hardcoded(item.title),
            summary = item.desc,
            isEnabled = true
        )
    }
}