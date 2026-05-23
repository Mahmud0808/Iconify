package com.drdisagree.iconify.features.changelog.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.utils.ChangelogParser
import com.drdisagree.iconify.features.changelog.models.ChangelogData
import com.drdisagree.iconify.features.changelog.states.ChangelogState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

class ChangelogViewModel : ViewModel() {

    var uiState by mutableStateOf<ChangelogState>(ChangelogState.Loading)
        private set

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (BuildConfig.DEBUG) {
                    loadDebugChangelog()
                } else {
                    loadReleaseChangelog()
                }
            } catch (_: Exception) {
                uiState = ChangelogState.Error
            }
        }
    }

    private fun loadReleaseChangelog() {
        val url =
            "https://api.github.com/repos/Mahmud0808/Iconify/releases/latest"

        val json = JSONObject(URL(url).readText())
        val parsed = ChangelogParser.parseReleaseChangelog(json)

        uiState = ChangelogState.Success(parsed)
    }

    private fun loadDebugChangelog() {
        val releaseJson = URL(
            "https://api.github.com/repos/Mahmud0808/Iconify/releases/latest"
        ).readText()

        val baseTag = JSONObject(releaseJson).getString("tag_name")

        val compareUrl =
            "https://api.github.com/repos/Mahmud0808/Iconify/compare/$baseTag...HEAD"

        val compareJson = URL(compareUrl).readText()
        val commits = JSONObject(compareJson).getJSONArray("commits")

        val changes = mutableSetOf<String>()

        for (i in 0 until commits.length()) {
            val commit = commits.getJSONObject(i)
            if (commit.getJSONArray("parents").length() > 1) continue

            val subject = commit
                .getJSONObject("commit")
                .getString("message")
                .lineSequence()
                .first()

            changes += "- $subject"
        }

        uiState = ChangelogState.Success(
            ChangelogData(
                titleRes = R.string.changelog_changes_since,
                titleArg = baseTag,
                contents = changes.toList()
            )
        )
    }
}