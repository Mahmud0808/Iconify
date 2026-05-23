package com.drdisagree.iconify.core.utils

import com.drdisagree.iconify.R
import com.drdisagree.iconify.features.changelog.models.ChangelogData
import org.json.JSONObject

object ChangelogParser {

    fun parseReleaseChangelog(json: JSONObject?): ChangelogData {
        if (json == null || json.toString().isEmpty()) {
            return ChangelogData(
                titleRes = R.string.changelog_not_found,
                contents = emptyList()
            )
        }

        return try {
            val data = json.getString("body")

            val title = json.getString("tag_name")
            val changes = data.substringAfter("\r\n\r\n")

            ChangelogData(
                titleRes = R.string.changelog_changes_in,
                titleArg = title,
                contents = usernameToLink(changes).split("\n")
            )
        } catch (_: Exception) {
            ChangelogData(
                titleRes = R.string.changelog_not_found,
                contents = emptyList()
            )
        }
    }

    private fun usernameToLink(str: String): String {
        val regex = "@([A-Za-z\\d_-]+)".toRegex()
        return regex.replace(str) {
            val username = it.groupValues[1]
            "<a href=\"https://github.com/$username\">@$username</a>"
        }
    }
}