package com.drdisagree.iconify.core.ui.components.topappbar

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
fun CollapsingTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    title: String,
    subtitle: String = "",
    @DrawableRes backIcon: Int? = null,
    showBackIcon: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
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
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceBright
                        else MaterialTheme.colorScheme.surface,
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