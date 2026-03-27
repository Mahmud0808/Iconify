package com.drdisagree.iconify.core.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.others.AnimatedGradientBorder
import com.drdisagree.iconify.core.ui.components.others.BlurBehindDialog
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.utils.CARD_CORNER_LARGE

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingDialog(onDismiss: () -> Unit = {}) {
    Dialog(onDismissRequest = onDismiss) {
        BlurBehindDialog()

        AnimatedGradientBorder(
            borderWidth = 2.dp,
            shape = RoundedCornerShape(CARD_CORNER_LARGE),
            gradientColors = listOf(
                MaterialTheme.colorScheme.primaryFixedDim,
                MaterialTheme.colorScheme.tertiaryFixedDim,
                MaterialTheme.colorScheme.tertiaryFixedDim,
                MaterialTheme.colorScheme.primaryFixedDim,
            )
        ) {
            Surface(
                shape = RoundedCornerShape(CARD_CORNER_LARGE - 2.dp),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 36.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ContainedLoadingIndicator(modifier = Modifier.size(60.dp))
                    Text(
                        text = stringResource(R.string.loading_dialog_wait),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.wrapContentWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingDialogPreview() {
    PreviewComposable {
        LoadingDialog(onDismiss = {})
    }
}