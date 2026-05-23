package com.drdisagree.iconify.features.onboarding.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.others.DecorativeShape
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.components.svg.DynamicColorImageVectors
import com.drdisagree.iconify.core.ui.components.svg.undrawSelectChoice
import com.drdisagree.iconify.core.ui.components.texts.AutoResizeableText
import com.drdisagree.iconify.core.ui.permission.rememberNotificationPermissionLauncher
import com.drdisagree.iconify.core.utils.SystemUtils.hasNotificationPermission
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PageThree(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
) {
    val previewMode = LocalInspectionMode.current

    val scale = remember { Animatable(0f) }
    val scaleMainShape = remember { Animatable(0.75f) }
    val semiCircleShape = MaterialShapes.SemiCircle.toShape()

    var permissionCardChecked by rememberSaveable {
        mutableStateOf(if (previewMode) false else hasNotificationPermission())
    }

    val requestNotificationPermission = rememberNotificationPermissionLauncher(
        onGranted = {
            permissionCardChecked = true
        },
        onDenied = {
            permissionCardChecked = false
        }
    )

    if (!previewMode) {
        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    permissionCardChecked = hasNotificationPermission()
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 2) {
            launch {
                scale.animateTo(
                    1f,
                    tween(1500, easing = FastOutSlowInEasing)
                )
            }
            launch {
                scaleMainShape.animateTo(
                    1f,
                    tween(1500, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        DecorativeShape(
            size = 250,
            shape = MaterialShapes.Clover8Leaf.toShape(),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
            scale = scale.value,
            modifier = Modifier.align(Alignment.Center)
        )

        DecorativeShape(
            size = 65,
            shape = MaterialShapes.Puffy.toShape(),
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-120).dp, x = 30.dp)
        )

        DecorativeShape(
            size = 70,
            shape = MaterialShapes.Fan.toShape(),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(y = (-100).dp, x = 10.dp)
        )

        DecorativeShape(
            size = 80,
            shape = MaterialShapes.Flower.toShape(),
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(y = (-180).dp)
        )

        DecorativeShape(
            size = 120,
            shape = MaterialShapes.Arch.toShape(),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-10).dp, x = (-20).dp)
        )

        DecorativeShape(
            size = 100,
            shape = MaterialShapes.Ghostish.toShape(),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(y = (-160).dp, x = (-20).dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 80.dp)
                .background(Color.Transparent)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 65.dp, start = 20.dp, end = 20.dp)
                    .align(Alignment.CenterHorizontally)
                    .graphicsLayer {
                        scaleX = scaleMainShape.value
                        scaleY = scaleMainShape.value
                        shape = semiCircleShape
                        clip = true
                    }
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.grant_permission),
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        top = 55.dp,
                        bottom = 30.dp,
                        start = 20.dp,
                        end = 20.dp
                    )
                )
            }

            Image(
                imageVector = DynamicColorImageVectors.undrawSelectChoice(),
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 100.dp)
            )

            AutoResizeableText(
                text = stringResource(R.string.optional_permission),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            PermissionCard(
                isChecked = permissionCardChecked,
                title = stringResource(id = R.string.notification_perm_title),
                description = stringResource(id = R.string.notification_perm_desc),
                onClick = {
                    if (!permissionCardChecked) {
                        requestNotificationPermission()
                    }
                },
                modifier = Modifier.padding(top = 28.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
                .height(40.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PermissionCard(
    modifier: Modifier = Modifier,
    isChecked: Boolean,
    onClick: () -> Unit,
    title: String,
    description: String
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .clip(MaterialTheme.shapes.large)
            .border(CardDefaults.outlinedCardBorder())
            .clickable(onClick = withHaptic { onClick() }),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
            contentColor = if (isChecked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = if (isChecked) Icons.Rounded.CheckCircle else Icons.Rounded.CheckCircleOutline,
                contentDescription = null,
                tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.5f
                ),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.9f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PageThreePreview() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    PageThree(pagerState = pagerState)
}