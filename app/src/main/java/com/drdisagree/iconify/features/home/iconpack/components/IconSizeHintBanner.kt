package com.drdisagree.iconify.features.home.iconpack.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import com.github.yohannestz.iconsax_compose.iconsax.Iconsax
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.ui.components.extensions.bounceClick
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable

@Composable
fun IconSizeHintBanner(modifier: Modifier = Modifier, onDismiss: () -> Unit) {
    val navController = LocalNavController.current
    Card(
        onClick = {
            navController.navigate(NavRoutes.MainGraph.Home.More.StatusBar) {
                launchSingleTop = true
            }
        },
        modifier = modifier
            .padding(bottom = 8.dp)
            .bounceClick(pressedScale = 0.98f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Iconsax.Outline.InfoCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = stringResource(R.string.hint_icon_size_increase),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Iconsax.Outline.CloseCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IconSizeHintBannerPreview() {
    PreviewComposable {
        IconSizeHintBanner(onDismiss = {})
    }
}