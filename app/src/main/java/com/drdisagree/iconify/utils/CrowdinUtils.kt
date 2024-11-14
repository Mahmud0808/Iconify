package com.drdisagree.iconify.utils

import com.drdisagree.iconify.ui.models.InfoModel
import com.drdisagree.iconify.utils.FileUtils.readJsonFileFromAssets
import org.json.JSONArray

fun parseTranslators(): ArrayList<InfoModel> {
    val contributorsList = ArrayList<InfoModel>()
    val jsonStr = readJsonFileFromAssets("Misc/translators.json")
    val jsonArray = JSONArray(jsonStr)

    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        val name = jsonObject.getString("name").replace(Regex("\\s*\\(.*\\)"), "")
        val username = jsonObject.getString("username")

        if (username == "DrDisagree") continue // Skip the main developer

        val picture = jsonObject.getString("picture")
        val languagesArray = jsonObject.getJSONArray("languages")
        val languagesList = ArrayList<String>()
        for (j in 0 until languagesArray.length()) {
            languagesList.add(languagesArray.getJSONObject(j).getString("name"))
        }
        val languages = languagesList.joinToString(", ")
        val url = "https://crowdin.com/profile/$username"

        contributorsList.add(InfoModel(name, languages, url, picture))
    }

    return contributorsList
}