package com.drdisagree.iconify.features.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalInnerPadding
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.preferences.PrefStringRes
import com.drdisagree.iconify.core.preferences.resolve
import com.drdisagree.iconify.core.search.SearchHighlightState
import com.drdisagree.iconify.core.search.SearchablePreference
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText
import com.drdisagree.iconify.core.ui.components.others.animatedPreferenceShape
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.utils.ItemPosition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val layoutDirection = LocalLayoutDirection.current
    val parentInnerPadding = LocalInnerPadding.current
    val safeInsets = WindowInsets.safeDrawing.asPaddingValues()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // ── Search bar ─────────────────────────────────────────────────
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = viewModel::onQueryChanged,
                    onSearch = { /* no-op, real-time filtering */ },
                    expanded = false,
                    onExpandedChange = {},
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_settings_hint),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    leadingIcon = {
                        IconButton(onClick = withHaptic { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = query.isNotEmpty(),
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut(),
                        ) {
                            IconButton(onClick = withHaptic { viewModel.clearQuery() }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Clear",
                                )
                            }
                        }
                    },
                    modifier = Modifier.focusRequester(focusRequester),
                )
            },
            expanded = false,
            onExpandedChange = {},
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .padding(
                    start = safeInsets.calculateStartPadding(layoutDirection),
                    end = safeInsets.calculateEndPadding(layoutDirection),
                ),
            tonalElevation = 4.dp,
        ) {}

        // ── Results ────────────────────────────────────────────────────
        val showResults = query.isNotEmpty() && results.isNotEmpty()
        val showHistory = query.isEmpty() && history.isNotEmpty()

        val contentAlpha by animateFloatAsState(
            targetValue = if (showResults || showHistory) 1f else 0f,
            animationSpec = tween(300),
            label = "contentAlpha"
        )

        if (query.isNotEmpty() && results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.search_no_results),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.secondaryText(),
                    )
                }
            }
        } else {
            val listItems = if (query.isEmpty()) history else results
            val isHistory = query.isEmpty()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(contentAlpha),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = parentInnerPadding.calculateBottomPadding() +
                            safeInsets.calculateBottomPadding(),
                    start = 16.dp +
                            parentInnerPadding.calculateStartPadding(layoutDirection) +
                            safeInsets.calculateStartPadding(layoutDirection),
                    end = 16.dp +
                            parentInnerPadding.calculateEndPadding(layoutDirection) +
                            safeInsets.calculateEndPadding(layoutDirection)
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (isHistory && history.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.recent_searches),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = viewModel::clearHistory) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = stringResource(R.string.searchpreference_clear_history),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                itemsIndexed(
                    items = listItems,
                    key = { index, item -> "${item.key}_$index" },
                ) { index, pref ->
                    val position = when {
                        listItems.size == 1 -> ItemPosition.SOLO
                        index == 0 -> ItemPosition.FIRST
                        index == listItems.size - 1 -> ItemPosition.LAST
                        else -> ItemPosition.MIDDLE
                    }
                    SearchResultItem(
                        pref = pref,
                        viewModel = viewModel,
                        isHistory = isHistory,
                        position = position,
                        onClick = {
                            viewModel.addToHistory(pref)
                            SearchHighlightState.requestHighlight(pref.key)
                            navController.navigate(pref.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    pref: SearchablePreference,
    viewModel: SearchViewModel,
    isHistory: Boolean,
    position: ItemPosition,
    onClick: () -> Unit,
) {
    val shape = animatedPreferenceShape(position)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                role = Role.Button,
                onClick = withHaptic { onClick() },
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Icon ───────────────────────────────────────────────────
        if (isHistory) {
            Icon(
                imageVector = Icons.Rounded.History,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // ── Text ───────────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {
            // Title
            Text(
                text = pref.title.resolveToString(viewModel),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // Breadcrumb trail
            val breadcrumbText = buildBreadcrumbString(pref, viewModel)
            if (breadcrumbText.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    breadcrumbText.forEachIndexed { index, segment ->
                        if (index > 0) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurface.secondaryText(),
                            )
                            Spacer(Modifier.width(2.dp))
                        }
                        Text(
                            text = segment,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.secondaryText(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (index < breadcrumbText.size - 1) {
                            Spacer(Modifier.width(2.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Resolves a PrefStringRes using viewModel for non-compose resolution,
 * with @Composable fallback via resolve().
 */
@Composable
private fun PrefStringRes.resolveToString(viewModel: SearchViewModel): String {
    return viewModel.resolveText(this) ?: this.resolve()
}

@Composable
private fun buildBreadcrumbString(
    pref: SearchablePreference,
    viewModel: SearchViewModel,
): List<String> {
    return pref.breadcrumbs.mapNotNull { bc ->
        viewModel.resolveText(bc) ?: bc.resolve().takeIf { it.isNotBlank() }
    }
}
