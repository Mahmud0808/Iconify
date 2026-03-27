package com.drdisagree.iconify.core.ui.components.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.text.HtmlCompat
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText
import com.drdisagree.iconify.core.ui.components.others.BlurBehindDialog
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.svg.DynamicColorImageVectors
import com.drdisagree.iconify.core.ui.components.svg.undraw404Error

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ErrorDialog(
    title: Any,
    description: Any,
    onDismiss: () -> Unit
) {
    val title = if (title is Int) stringResource(title) else title as String
    val desc = if (description is Int) stringResource(description) else description as String

    Dialog(onDismissRequest = onDismiss) {
        BlurBehindDialog()

        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Image(
                    imageVector = DynamicColorImageVectors.undraw404Error(),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .width(180.dp)
                        .padding(bottom = 16.dp)
                )

                val titleText = remember(title) {
                    HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
                }

                Text(
                    text = titleText,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.wrapContentWidth()
                )

                val descText = remember(description) {
                    HtmlCompat.fromHtml(desc, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
                }

                Text(
                    text = descText,
                    color = MaterialTheme.colorScheme.onSurface.secondaryText(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Button(
                    onClick = onDismiss,
                    shapes = ButtonDefaults.shapes(),
                    contentPadding = PaddingValues(horizontal = 30.dp, vertical = 12.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = stringResource(R.string.btn_close), fontSize = 16.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorDialogPreview() {
    PreviewComposable {
        ErrorDialog(
            title = "Error!",
            description = "Something went wrong.",
            onDismiss = {}
        )
    }
}