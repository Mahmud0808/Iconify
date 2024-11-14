package com.drdisagree.iconify.utils

import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.models.InfoModel
import com.drdisagree.iconify.utils.FileUtils.readJsonFileFromAssets
import org.json.JSONArray
import org.json.JSONException

@Throws(JSONException::class)
fun parseContributors(): ArrayList<InfoModel> {
    val contributorsList = ArrayList<InfoModel>()
    val jsonStr = readJsonFileFromAssets("Misc/contributors.json")
    val jsonArray = JSONArray(jsonStr)

    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        val name = jsonObject.getString("login")

        if (name == "Mahmud0808") continue // Skip the main developer

        val picture = jsonObject.getString("avatar_url")
        val profileUrl = jsonObject.getString("html_url")
        val contributions = jsonObject.getInt("contributions")

        contributorsList.add(
            InfoModel(
                name,
                appContextLocale.resources.getString(R.string.total_contributions, contributions),
                profileUrl,
                picture
            )
        )
    }

    return contributorsList
}