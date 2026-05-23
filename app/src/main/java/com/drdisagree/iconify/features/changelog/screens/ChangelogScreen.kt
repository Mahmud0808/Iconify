package com.drdisagree.iconify.features.changelog.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.dialogs.LoadingDialogContent
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.innerPaddingValues
import com.drdisagree.iconify.core.ui.components.scaffolds.AppScaffold
import com.drdisagree.iconify.features.changelog.states.ChangelogState
import com.drdisagree.iconify.features.changelog.viewmodels.ChangelogViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ChangelogScreen(
    changelogViewModel: ChangelogViewModel? = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        changelogViewModel?.load()
    }

    AppScaffold(
        title = stringResource(R.string.activity_title_changelog),
        showBackIcon = true,
    ) { innerPadding, _ ->
        val padding = innerPaddingValues(
            innerPadding = innerPadding,
            top = 16.dp,
            start = 16.dp,
            end = 16.dp,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding,
            horizontalAlignment = if (changelogViewModel?.uiState is ChangelogState.Success)
                Alignment.Start
            else
                Alignment.CenterHorizontally,
            verticalArrangement = if (changelogViewModel?.uiState is ChangelogState.Success)
                Arrangement.Top
            else
                Arrangement.Center
        ) {
            when (val state = changelogViewModel?.uiState ?: return@LazyColumn) {
                is ChangelogState.Loading -> {
                    item {
                        LoadingDialogContent(showText = false)
                    }
                }

                is ChangelogState.Error -> {
                    item {
                        Column(
                            modifier = Modifier.wrapContentSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.changelog_not_found),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                is ChangelogState.Success -> {
                    item {
                        Text(
                            text = if (state.data.titleArg != null) {
                                stringResource(state.data.titleRes, state.data.titleArg)
                            } else {
                                stringResource(state.data.titleRes)
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    state.data.contents.forEach { line ->
                        item {
                            MarkdownText(
                                markdown = line,
                                style = MaterialTheme.typography.bodyMedium,
                                linkColor = MaterialTheme.colorScheme.primary,
                                linkifyMask = 0,
                                isTextSelectable = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChangelogScreenPreview() {
    PreviewComposable {
        ChangelogScreen(null)
    }
}