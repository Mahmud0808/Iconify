package com.drdisagree.iconify.core.ui.components.lottie

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.drdisagree.iconify.R

@Composable
fun WandLottie(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_wand))

    val color = MaterialTheme.colorScheme.onSurface

    val colorProperty = rememberLottieDynamicProperty(
        property = LottieProperty.COLOR,
        keyPath = arrayOf("**"),
        value = color.toArgb()
    )

    val dynamicProperties = rememberLottieDynamicProperties(colorProperty)

    val lottie = rememberLottieAnimatable()

    LaunchedEffect(Unit) {
        lottie.animate(
            composition = composition,
            iterations = LottieConstants.IterateForever
        )
    }

    LottieAnimation(
        composition = lottie.composition,
        progress = { lottie.progress },
        dynamicProperties = dynamicProperties,
        modifier = modifier
    )
}

