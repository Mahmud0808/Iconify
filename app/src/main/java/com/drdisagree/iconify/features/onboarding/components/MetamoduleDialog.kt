package com.drdisagree.iconify.features.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText
import com.drdisagree.iconify.core.ui.components.others.BlurBehindDialog
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.utils.AppUtils.openUrl
import com.github.yohannestz.iconsax_compose.iconsax.Iconsax

private val metamoduleExamples = listOf(
    "Mountify" to "https://github.com/backslashxx/mountify",
    "Hybrid Mount" to "https://github.com/Hybrid-Mount/meta-hybrid_mount",
)

private const val EXAMPLES_PLACEHOLDER = "\u0000"

private const val METAMODULE_GUIDE_URL = "https://kernelsu.org/guide/metamodule.html"

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MetamoduleDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        BlurBehindDialog()

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Iconsax.Outline.Layer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.metamodule_not_found_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Text(
                    text = stringResource(R.string.metamodule_what_is),
                    color = MaterialTheme.colorScheme.onSurface.secondaryText(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                val examplesTemplate = stringResource(
                    R.string.metamodule_examples,
                    EXAMPLES_PLACEHOLDER
                )
                val linkStyles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                )
                val examplesText = remember(examplesTemplate, linkStyles) {
                    buildAnnotatedString {
                        append(examplesTemplate.substringBefore(EXAMPLES_PLACEHOLDER))
                        metamoduleExamples.forEachIndexed { index, (name, url) ->
                            if (index > 0) append(", ")
                            withLink(LinkAnnotation.Url(url, linkStyles)) {
                                append(name)
                            }
                        }
                        append(examplesTemplate.substringAfter(EXAMPLES_PLACEHOLDER, ""))
                    }
                }

                Text(
                    text = examplesText,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(
                        onClick = withHaptic { openUrl(context, METAMODULE_GUIDE_URL) },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Text(text = stringResource(R.string.btn_learn_more))
                    }

                    Button(
                        onClick = withHaptic { onDismiss() },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Text(text = stringResource(R.string.btn_close))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MetamoduleDialogPreview() {
    PreviewComposable {
        MetamoduleDialog(onDismiss = {})
    }
}
