package com.drdisagree.iconify.features.settings.appupdates.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.dialogs.LoadingDialog
import com.drdisagree.iconify.data.config.Config
import com.drdisagree.iconify.features.changelog.models.ChangelogData
import com.drdisagree.iconify.features.settings.appupdates.states.AppUpdatesState
import com.drdisagree.iconify.features.settings.appupdates.viewmodels.AppUpdatesViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun AppUpdateContent(appUpdatesViewModel: AppUpdatesViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uiState = appUpdatesViewModel.uiState
    val onUpdateCheckClick = { appUpdatesViewModel.checkForUpdates() }
    val onDownloadClick = {
        appUpdatesViewModel.downloadAndInstallUpdate(
            context,
            (uiState as AppUpdatesState.UpdateAvailable).downloadUrl
        )
    }

    when (uiState) {
        is AppUpdatesState.Loading -> {
            LoadingDialog(text = stringResource(R.string.checking_for_update))
        }

        is AppUpdatesState.Downloading -> {
            LoadingDialog(
                text = stringResource(
                    R.string.downloading_update,
                    uiState.progress.toInt()
                )
            )
        }

        is AppUpdatesState.Installing -> {
            LoadingDialog(text = stringResource(R.string.installing_update))
        }

        else -> Unit
    }

    ColumnWrapper(modifier = Modifier.padding(top = 24.dp)) {
        if (BuildConfig.DEBUG && !Config.ENABLE_AUTO_UPDATE_IN_DEBUG) {
            AutoUpdateDisabled()
        } else {
            when (uiState) {
                is AppUpdatesState.Idle,
                is AppUpdatesState.Loading,
                is AppUpdatesState.Downloading,
                is AppUpdatesState.Installing -> {
                    CheckForUpdatesButton(onUpdateCheckClick)
                }

                is AppUpdatesState.UpdateAvailable -> {
                    NewUpdateFound(uiState, onDownloadClick)
                }

                is AppUpdatesState.UpToDate -> {
                    AlreadyUpToDate(uiState, onUpdateCheckClick)
                }

                is AppUpdatesState.Error -> {
                    UpdateError(uiState, onUpdateCheckClick)
                }
            }
        }
    }
}

@Composable
private fun ColumnWrapper(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

@Composable
private fun ColumnScope.AutoUpdateDisabled() {
    Icon(
        imageVector = Icons.Rounded.Info,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(48.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.auto_update_disabled),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColumnScope.CheckForUpdatesButton(onUpdateCheckClick: () -> Unit) {
    Button(
        onClick = onUpdateCheckClick,
        shapes = ButtonDefaults.shapes(),
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        Text(text = stringResource(R.string.check_for_updates_button))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColumnScope.NewUpdateFound(
    uiState: AppUpdatesState.UpdateAvailable,
    onDownloadClick: () -> Unit,
) {
    var showChangelog by rememberSaveable { mutableStateOf(false) }

    Icon(
        imageVector = Icons.Rounded.NewReleases,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(48.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.update_available),
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.update_dialog_desc, uiState.versionName),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(
            shapes = ButtonDefaults.shapes(),
            onClick = { showChangelog = !showChangelog }
        ) {
            Text(
                text = if (showChangelog) stringResource(R.string.hide_changelog)
                else stringResource(R.string.view_changelog)
            )
        }
        Button(
            shapes = ButtonDefaults.shapes(),
            onClick = onDownloadClick
        ) {
            Text(text = stringResource(R.string.download_update_button))
        }
    }

    if (showChangelog) {
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.shapes.large
                )
                .padding(16.dp)
        ) {
            Text(
                text = if (uiState.changelog.titleArg != null) {
                    stringResource(
                        uiState.changelog.titleRes,
                        uiState.changelog.titleArg
                    )
                } else {
                    stringResource(uiState.changelog.titleRes)
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            uiState.changelog.contents.forEach { line ->
                MarkdownText(
                    markdown = line.trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    linkColor = MaterialTheme.colorScheme.primary,
                    linkifyMask = 0,
                    isTextSelectable = true
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun ColumnScope.AlreadyUpToDate(
    uiState: AppUpdatesState.UpToDate,
    onUpdateCheckClick: () -> Unit
) {
    Icon(
        imageVector = Icons.Rounded.CheckCircleOutline,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(48.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.already_up_to_date),
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = stringResource(
            R.string.current_version_number,
            uiState.currentVersion
        ),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = stringResource(
            R.string.latest_version_number,
            uiState.latestVersion
        ),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(24.dp))
    CheckForUpdatesButton(onUpdateCheckClick)
}

@Composable
private fun ColumnScope.UpdateError(
    uiState: AppUpdatesState.Error,
    onUpdateCheckClick: () -> Unit
) {
    Icon(
        imageVector = Icons.Rounded.ErrorOutline,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(48.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.update_checking_failed),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = stringResource(
            R.string.current_version_number,
            uiState.currentVersion
        ),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = stringResource(
            R.string.latest_version_number,
            stringResource(R.string.not_available)
        ),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(24.dp))
    CheckForUpdatesButton(onUpdateCheckClick)
}

@Preview(showBackground = true)
@Composable
private fun Preview_AutoUpdateDisabled() {
    ColumnWrapper {
        AutoUpdateDisabled()
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun Preview_CheckForUpdatesButton() {
    ColumnWrapper {
        CheckForUpdatesButton {}
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun Preview_NewUpdateFound() {
    ColumnWrapper {
        NewUpdateFound(
            uiState = AppUpdatesState.UpdateAvailable(
                versionName = "1.2.3",
                changelog = ChangelogData(
                    titleRes = R.string.update_available,
                    titleArg = null,
                    contents = listOf(
                        "- Added new feature",
                        "- Fixed bugs",
                        "- Improved performance"
                    )
                ),
                downloadUrl = "https://example.com/app.apk"
            ),
            onDownloadClick = {}
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun Preview_AlreadyUpToDate() {
    ColumnWrapper {
        AlreadyUpToDate(
            uiState = AppUpdatesState.UpToDate(
                currentVersion = "1.2.3",
                latestVersion = "1.2.3"
            ),
            onUpdateCheckClick = {}
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun Preview_UpdateError() {
    ColumnWrapper {
        UpdateError(
            uiState = AppUpdatesState.Error(
                currentVersion = "1.2.3"
            ),
            onUpdateCheckClick = {}
        )
    }
}