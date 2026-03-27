package com.drdisagree.iconify.features.onboarding.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.others.DecorativeShape
import com.drdisagree.iconify.core.ui.components.others.DisclaimerItemLayout
import com.drdisagree.iconify.core.ui.components.texts.AutoResizeableText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PageTwo(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    pagerState: PagerState
) {
    val scale = remember { Animatable(0f) }
    val scaleMainShape = remember { Animatable(0.75f) }
    val clanShellShape = MaterialShapes.ClamShell.toShape()

    val disclaimerList = listOf(
        stringResource(R.string.disclaimer_1_title) to stringResource(R.string.disclaimer_1_description),
        stringResource(R.string.disclaimer_2_title) to stringResource(R.string.disclaimer_2_description),
        stringResource(R.string.disclaimer_3_title) to stringResource(R.string.disclaimer_3_description),
        stringResource(R.string.disclaimer_4_title) to stringResource(R.string.disclaimer_4_description),
        stringResource(R.string.disclaimer_5_title) to stringResource(R.string.disclaimer_5_description),
    )

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 1) {
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
            shape = MaterialShapes.Cookie12Sided.toShape(),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
            scale = scale.value,
            modifier = Modifier.align(Alignment.Center)
        )

        DecorativeShape(
            size = 65,
            shape = MaterialShapes.SoftBoom.toShape(),
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-120).dp, x = 30.dp)
        )

        DecorativeShape(
            size = 70,
            shape = MaterialShapes.PuffyDiamond.toShape(),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(y = (-100).dp, x = 10.dp)
        )

        DecorativeShape(
            size = 80,
            shape = MaterialShapes.Pentagon.toShape(),
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(y = (-180).dp)
        )

        DecorativeShape(
            size = 120,
            shape = MaterialShapes.Bun.toShape(),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-10).dp, x = (-20).dp)
        )

        DecorativeShape(
            size = 100,
            shape = MaterialShapes.SemiCircle.toShape(),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(y = (-160).dp, x = (-20).dp)
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Box(
                    modifier = modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 65.dp, start = 20.dp, end = 20.dp)
                            .graphicsLayer {
                                scaleX = scaleMainShape.value
                                scaleY = scaleMainShape.value
                                shape = clanShellShape
                                clip = true
                            }
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AutoResizeableText(
                            text = stringResource(R.string.disclaimer),
                            style = MaterialTheme.typography.headlineMediumEmphasized,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(35.dp)
                        )
                    }
                }
            }

            itemsIndexed(disclaimerList) { i, item ->
                DisclaimerItemLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    disclaimerItem = item
                )
            }

            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(25.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
                .height(60.dp)
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

@Preview(showBackground = true)
@Composable
fun PageTwoPreview() {
    val lazyListState = rememberLazyListState()
    val pagerState = rememberPagerState(pageCount = { 3 })
    PageTwo(pagerState = pagerState, lazyListState = lazyListState)
}