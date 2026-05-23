package com.drdisagree.iconify.features.home.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable

private data class CategoryItem(
    val title: Int,
    val subtitle: Int? = null,
    val icon: Int,
    val smallVariant: Boolean = false,
    val route: NavRoutes.MainGraph.Home
)

@Composable
fun HomeCategories(modifier: Modifier = Modifier) {
    val navController = LocalNavController.current

    val categories = listOf(
        CategoryItem(
            title = R.string.activity_title_icon_pack,
            subtitle = R.string.activity_desc_icon_pack,
            icon = R.drawable.ic_styles_iconpack,
            route = NavRoutes.MainGraph.Home.IconPack
        ),
        CategoryItem(
            title = R.string.more,
            icon = R.drawable.ic_arrow_end_long,
            smallVariant = true,
            route = NavRoutes.MainGraph.Home.More.Root
        ),
        CategoryItem(
            title = R.string.activity_title_cellular_icons,
            subtitle = R.string.activity_desc_cellular_icons,
            icon = R.drawable.ic_styles_signal_icons,
            route = NavRoutes.MainGraph.Home.CellularIcons
        ),
        CategoryItem(
            title = R.string.activity_title_wifi_icons,
            subtitle = R.string.activity_desc_wifi_icons,
            icon = R.drawable.ic_styles_wifi_icons,
            route = NavRoutes.MainGraph.Home.WifiIcons
        ),
        CategoryItem(
            title = R.string.activity_title_settings_icons,
            subtitle = R.string.activity_desc_settings_icons,
            icon = R.drawable.ic_settings_icon_pack,
            route = NavRoutes.MainGraph.Home.SettingsIcons
        ),
        CategoryItem(
            title = R.string.activity_title_notification,
            subtitle = R.string.activity_desc_notification,
            icon = R.drawable.ic_styles_notification,
            route = NavRoutes.MainGraph.Home.Notification
        ),
        CategoryItem(
            title = R.string.activity_title_toast_frame,
            subtitle = R.string.activity_desc_toast_frame,
            icon = R.drawable.ic_styles_toast_frame,
            route = NavRoutes.MainGraph.Home.ToastFrame
        ),
        CategoryItem(
            title = R.string.activity_title_icon_shape,
            subtitle = R.string.activity_desc_icon_shape,
            icon = R.drawable.ic_styles_icon_shape,
            route = NavRoutes.MainGraph.Home.IconShape
        )
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            categories
                .filterIndexed { index, _ -> index % 2 == 0 }
                .forEachIndexed { rowIndex, item ->
                    val (backgroundColor, foregroundColor) = when (rowIndex % 4) {
                        0 -> MaterialTheme.colorScheme.primaryContainer
                            .copy(alpha = 0.2f)
                            .compositeOver(MaterialTheme.colorScheme.surfaceContainerHigh) to MaterialTheme.colorScheme.onPrimaryContainer
                            .copy(alpha = 0.25f)
                            .compositeOver(MaterialTheme.colorScheme.onSurface)

                        1 -> MaterialTheme.colorScheme.secondaryContainer
                            .copy(alpha = 0.2f)
                            .compositeOver(MaterialTheme.colorScheme.surfaceContainerHigh) to MaterialTheme.colorScheme.onSecondaryContainer
                            .copy(alpha = 0.25f)
                            .compositeOver(MaterialTheme.colorScheme.onSurface)

                        2 -> MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurface

                        else -> MaterialTheme.colorScheme.tertiaryContainer
                            .copy(alpha = 0.2f)
                            .compositeOver(MaterialTheme.colorScheme.surfaceContainerHigh) to MaterialTheme.colorScheme.onTertiaryContainer
                            .copy(alpha = 0.25f)
                            .compositeOver(MaterialTheme.colorScheme.onSurface)
                    }

                    HomeCategoryCard(
                        title = stringResource(item.title),
                        subtitle = item.subtitle?.let { stringResource(it) },
                        icon = item.icon,
                        backgroundColor = backgroundColor,
                        foregroundColor = foregroundColor,
                        smallVariant = item.smallVariant,
                        onClick = {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            categories
                .filterIndexed { index, _ -> index % 2 == 1 }
                .forEachIndexed { rowIndex, item ->
                    val (backgroundColor, foregroundColor) = when (rowIndex % 4) {
                        0 -> MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurface

                        1 -> MaterialTheme.colorScheme.tertiaryContainer
                            .copy(alpha = 0.2f)
                            .compositeOver(MaterialTheme.colorScheme.surfaceContainerHigh) to MaterialTheme.colorScheme.onTertiaryContainer
                            .copy(alpha = 0.25f)
                            .compositeOver(MaterialTheme.colorScheme.onSurface)

                        2 -> MaterialTheme.colorScheme.primaryContainer
                            .copy(alpha = 0.2f)
                            .compositeOver(MaterialTheme.colorScheme.surfaceContainerHigh) to MaterialTheme.colorScheme.onPrimaryContainer
                            .copy(alpha = 0.25f)
                            .compositeOver(MaterialTheme.colorScheme.onSurface)

                        else -> MaterialTheme.colorScheme.secondaryContainer
                            .copy(alpha = 0.2f)
                            .compositeOver(MaterialTheme.colorScheme.surfaceContainerHigh) to MaterialTheme.colorScheme.onSecondaryContainer
                            .copy(alpha = 0.25f)
                            .compositeOver(MaterialTheme.colorScheme.onSurface)
                    }

                    HomeCategoryCard(
                        title = stringResource(item.title),
                        subtitle = item.subtitle?.let { stringResource(it) },
                        icon = item.icon,
                        backgroundColor = backgroundColor,
                        foregroundColor = foregroundColor,
                        smallVariant = item.smallVariant,
                        onClick = {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeCategoriesPreview() {
    PreviewComposable {
        HomeCategories()
    }
}