package com.drdisagree.iconify.core.ui.components.scaffolds

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.common.LocalInnerPadding
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.common.LocalSettings
import com.drdisagree.iconify.core.ui.components.dialogs.LoadingDialog
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.components.topappbar.ActionItem
import com.drdisagree.iconify.core.ui.components.topappbar.CollapsingTopAppBar
import com.drdisagree.iconify.core.ui.components.topappbar.TopAppBarAction
import com.drdisagree.iconify.core.utils.AppUtils
import com.drdisagree.iconify.data.states.ImportExportState
import com.drdisagree.iconify.features.common.viewmodels.ImportExportViewModel
import com.drdisagree.iconify.features.common.viewmodels.SystemActionViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String = "",
    @DrawableRes backIcon: Int? = null,
    showBackIcon: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    showActionIcon: Boolean = true,
    actions: List<TopAppBarAction> = emptyList(),
    importExportViewModel: ImportExportViewModel? = if (LocalInspectionMode.current) null else hiltViewModel(),
    content: @Composable (
        innerPadding: PaddingValues,
        scrollBehavior: TopAppBarScrollBehavior
    ) -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val previewMode = LocalInspectionMode.current
    val parentInnerPadding = LocalInnerPadding.current
    val safeInsets = WindowInsets.safeDrawing.asPaddingValues()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val importExportState by (importExportViewModel?.state?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(ImportExportState.Idle) })

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { importExportViewModel?.onExportUriReceived(it) }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { importExportViewModel?.onImportUriReceived(it) }
        }
    }

    if (importExportState is ImportExportState.AwaitingConfirmation) {
        val uri = (importExportState as ImportExportState.AwaitingConfirmation).uri

        AlertDialog(
            onDismissRequest = { importExportViewModel?.cancelImport() },
            title = { Text(stringResource(R.string.import_settings_confirmation_title)) },
            text = { Text(stringResource(R.string.import_settings_confirmation_desc)) },
            confirmButton = {
                Button(
                    shapes = ButtonDefaults.shapes(),
                    onClick = withHaptic { importExportViewModel?.confirmImport(uri) }
                ) { Text(stringResource(R.string.btn_positive)) }
            },
            dismissButton = {
                OutlinedButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = withHaptic { importExportViewModel?.cancelImport() }
                ) { Text(stringResource(R.string.btn_negative)) }
            }
        )
    }

    if (importExportState is ImportExportState.Loading) {
        LoadingDialog {}
    }

    LaunchedEffect(importExportState) {
        when (importExportState) {
            is ImportExportState.Success -> {
                Toast.makeText(
                    context,
                    (importExportState as ImportExportState.Success).messageRes,
                    Toast.LENGTH_SHORT
                ).show()

                importExportViewModel?.resetState()

                activity?.let { AppUtils.restartApplication(it) }
            }

            is ImportExportState.Failure -> {
                Toast.makeText(
                    context,
                    (importExportState as ImportExportState.Failure).messageRes,
                    Toast.LENGTH_SHORT
                ).show()

                importExportViewModel?.resetState()
            }

            else -> Unit
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CollapsingTopAppBar(
                scrollBehavior = scrollBehavior,
                title = title,
                subtitle = subtitle,
                backIcon = backIcon,
                showBackIcon = showBackIcon,
                onBackClick = onBackClick,
                actions = {
                    val searchAction = searchAction()
                    val allActions = if (previewMode)
                        listOf(searchAction) + defaultActions(systemActionViewModel = null) + actions
                    else
                        listOf(searchAction) + defaultActions(
                            onImport = {
                                importExportViewModel?.createImportIntent()
                                    ?.let { importLauncher.launch(it) }
                            },
                            onExport = {
                                importExportViewModel?.createExportIntent()
                                    ?.let { exportLauncher.launch(it) }
                            }
                        ) + actions

                    allActions.forEachIndexed { index, action ->
                        ActionItem(
                            action = action,
                            showActionIcon = showActionIcon,
                            isLastItem = index == allActions.lastIndex
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        val adjustedPadding = PaddingValues(
            top = innerPadding.calculateTopPadding(),
            bottom = innerPadding.calculateBottomPadding() +
                    parentInnerPadding.calculateBottomPadding(),
            start = innerPadding.calculateStartPadding(layoutDirection) +
                    safeInsets.calculateStartPadding(layoutDirection),
            end = innerPadding.calculateEndPadding(layoutDirection) +
                    safeInsets.calculateEndPadding(layoutDirection)
        )
        content(adjustedPadding, scrollBehavior)
    }
}

@Composable
private fun searchAction(): TopAppBarAction {
    val navController = LocalNavController.current

    return TopAppBarAction(
        icon = R.drawable.ic_search,
        label = "Search",
        onClick = {
            val popped = navController.popBackStack<NavRoutes.MainGraph.Search>(inclusive = false)
            if (!popped) {
                navController.navigate(NavRoutes.MainGraph.Search) {
                    launchSingleTop = true
                }
            }
        }
    )
}

@Composable
private fun defaultActions(
    onImport: () -> Unit = {},
    onExport: () -> Unit = {},
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
): List<TopAppBarAction> {
    val settings = LocalSettings.current
    val navController = LocalNavController.current

    return listOf(
        TopAppBarAction(
            icon = R.drawable.ic_menu,
            label = "Menu",
            subItems = buildList {
                add(
                    TopAppBarAction(
                        icon = R.drawable.ic_changelog,
                        label = stringResource(R.string.changelog),
                        onClick = {
                            navController.navigate(NavRoutes.MainGraph.Changelog) {
                                launchSingleTop = true
                            }
                        }
                    )
                )
                add(
                    TopAppBarAction(
                        icon = R.drawable.ic_upload_file,
                        label = stringResource(R.string.import_export),
                        subItems = listOf(
                            TopAppBarAction(
                                R.drawable.ic_file_import,
                                stringResource(R.string.import_settings),
                                onClick = onImport
                            ),
                            TopAppBarAction(
                                R.drawable.ic_file_export,
                                stringResource(R.string.export_settings),
                                onClick = onExport
                            ),
                        ),
                    )
                )
                if (settings.isPlaygroundUnlocked) {
                    add(
                        TopAppBarAction(
                            icon = Icons.Rounded.DeveloperMode,
                            label = "Playground",
                            onClick = {
                                navController.navigate(NavRoutes.MainGraph.Playground) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    )
                }
                add(
                    TopAppBarAction(
                        icon = R.drawable.ic_xposed_restart_systemui,
                        label = stringResource(R.string.btn_restart_systemui),
                        onClick = { systemActionViewModel?.triggerRestartSystemUI() }
                    )
                )
            }
        ),
    )
}