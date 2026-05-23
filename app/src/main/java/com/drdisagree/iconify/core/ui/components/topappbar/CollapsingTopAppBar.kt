package com.drdisagree.iconify.core.ui.components.topappbar

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalDarkMode
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import kotlinx.coroutines.delay

data class TopAppBarAction(
    val icon: Any,
    val label: String,
    val onClick: () -> Unit = {},
    val subItems: List<TopAppBarAction> = emptyList()
)

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
fun CollapsingTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    title: String,
    subtitle: String = "",
    @DrawableRes backIcon: Int? = null,
    showBackIcon: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit) = {}
) {
    val isDarkTheme = LocalDarkMode.current
    val navController = LocalNavController.current
    val expandedFontSize = 32.sp
    val collapsedFontSize = 22.sp
    val fontSize by animateFloatAsState(
        targetValue = lerp(
            expandedFontSize.value,
            collapsedFontSize.value,
            scrollBehavior.state.collapsedFraction
        ),
        label = "fontSizeAnimation"
    )

    var backVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(showBackIcon) {
        if (showBackIcon) {
            backVisible = true
        } else if (backVisible) {
            backVisible = false
        }
    }

    LargeFlexibleTopAppBar(
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = fontSize.sp)
            )
        },
        subtitle = {
            if (subtitle.isNotEmpty()) {
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurface.secondaryText(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        navigationIcon = {
            AnimatedVisibility(
                visible = backVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                IconButton(
                    onClick = withHaptic {
                        onBackClick?.invoke()
                            ?: navController.popBackStack()
                    },
                    modifier = Modifier
                        .padding(start = 16.dp, end = 4.dp)
                        .size(40.dp),
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceBright
                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                ) {
                    Icon(
                        painter = painterResource(
                            backIcon
                                ?: R.drawable.ic_home_as_up_indicator
                        ),
                        contentDescription = "Navigate back",
                        modifier = Modifier.size(24.dp),
                        tint = LocalContentColor.current
                    )
                }
            }
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
        windowInsets = TopAppBarDefaults.windowInsets,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    )
}

@Composable
fun ActionItem(action: TopAppBarAction, showActionIcon: Boolean, isLastItem: Boolean) {
    val isDarkTheme = LocalDarkMode.current
    val expanded = rememberSaveable { mutableStateOf(false) }

    var actionVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(showActionIcon) {
        if (showActionIcon) {
            actionVisible = true
        } else if (actionVisible) {
            actionVisible = false
        }
    }

    Box(modifier = Modifier.padding(start = 4.dp, end = 16.dp)) {
        AnimatedVisibility(
            visible = actionVisible,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
        ) {
            IconButton(
                onClick = {
                    if (action.subItems.isNotEmpty()) expanded.value = true
                    else action.onClick()
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceBright
                    else MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shapes = IconButtonDefaults.shapes(),
                modifier = Modifier
                    .width(if (isLastItem) 36.dp else 40.dp)
                    .height(40.dp)
            ) {
                when (action.icon) {
                    is Int -> {
                        Icon(
                            painter = painterResource(id = action.icon),
                            contentDescription = action.label
                        )
                    }

                    is ImageVector -> {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.label
                        )
                    }
                }
            }
        }

        if (action.subItems.isNotEmpty()) {
            ActionDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded.value = false },
                initialItems = action.subItems
            )
        }
    }
}

@Composable
private fun ActionDropdownMenu(
    expanded: MutableState<Boolean>,
    onDismissRequest: () -> Unit,
    initialItems: List<TopAppBarAction>
) {
    val menuStack = remember {
        mutableStateListOf<Pair<String?, List<TopAppBarAction>>>(null to initialItems)
    }

    LaunchedEffect(expanded.value) {
        if (!expanded.value) {
            delay(200)
            menuStack.clear()
            menuStack.add(null to initialItems)
        }
    }

    MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))) {
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = onDismissRequest,
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            val currentLevel = menuStack.lastOrNull() ?: (null to initialItems)
            val (currentParentName, currentItems) = currentLevel

            key(currentParentName) {
                Column {
                    if (currentParentName != null) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.ChevronLeft,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurface.secondaryText()
                                        )
                                    }
                                    Text(
                                        text = currentParentName.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.secondaryText(),
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            },
                            onClick = { menuStack.removeAt(menuStack.size - 1) },
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    currentItems.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    when (item.icon) {
                                        is Int -> {
                                            Icon(
                                                painter = painterResource(id = item.icon),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        is ImageVector -> {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    Text(item.label, style = MaterialTheme.typography.bodyMedium)
                                }
                            },
                            trailingIcon = {
                                if (item.subItems.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Rounded.ChevronRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            onClick = {
                                if (item.subItems.isNotEmpty()) {
                                    menuStack.add(item.label to item.subItems)
                                } else {
                                    item.onClick()
                                    expanded.value = false
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    }
}