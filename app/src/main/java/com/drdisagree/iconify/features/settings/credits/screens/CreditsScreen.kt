package com.drdisagree.iconify.features.settings.credits.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.innerPaddingValues
import com.drdisagree.iconify.core.ui.components.scaffolds.AppScaffold
import com.drdisagree.iconify.core.utils.parseContributors
import com.drdisagree.iconify.core.utils.parseSpecialThanks
import com.drdisagree.iconify.core.utils.parseTranslators
import com.drdisagree.iconify.features.settings.credits.components.DeveloperIntroCard
import com.drdisagree.iconify.features.settings.credits.components.creditsInfoSection
import com.drdisagree.iconify.features.settings.credits.models.CreditInfoModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen() {
    val context = LocalContext.current

    val creditItems: List<CreditInfoModel> = remember { parseSpecialThanks(context) }
    val contributorItems: List<CreditInfoModel> = remember { parseContributors(context) }
    val translatorItems: List<CreditInfoModel> = remember { parseTranslators(context) }

    val thanksTitleString = stringResource(R.string.section_title_thanks)
    val contributorsTitleString = stringResource(R.string.section_title_contributors)
    val translatorsTitleString = stringResource(R.string.section_title_translators)

    AppScaffold(
        title = stringResource(R.string.settings_credits_title),
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
        ) {
            item(key = "developer_intro") {
                DeveloperIntroCard()
            }

            creditsInfoSection(
                title = thanksTitleString,
                items = creditItems,
            )

            creditsInfoSection(
                title = contributorsTitleString,
                items = contributorItems,
            )

            creditsInfoSection(
                title = translatorsTitleString,
                items = translatorItems,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreditsScreenPreview() {
    PreviewComposable {
        CreditsScreen()
    }
}