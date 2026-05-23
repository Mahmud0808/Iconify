package com.drdisagree.iconify.core.utils

import android.content.Context
import com.drdisagree.iconify.R
import com.drdisagree.iconify.features.settings.credits.models.CreditInfoModel
import org.json.JSONArray
import org.json.JSONException

fun parseSpecialThanks(context: Context): List<CreditInfoModel> = buildList {
    add(
        CreditInfoModel(
            R.drawable.ic_link,
            "Icons8.com",
            context.getString(R.string.info_icons8_desc),
            "https://icons8.com",
        )
    )
    add(
        CreditInfoModel(
            R.drawable.ic_link,
            "iconsax.io",
            context.getString(R.string.info_iconsax_desc),
            "http://iconsax.io",
        )
    )
    add(
        CreditInfoModel(
            R.drawable.ic_user,
            "Siavash",
            context.getString(R.string.info_xposed_desc),
            "https://t.me/siavash7999",
        )
    )
    add(
        CreditInfoModel(
            R.drawable.ic_user,
            "Jai",
            context.getString(R.string.info_shell_desc),
            "https://t.me/Jai_08",
        )
    )
    add(
        CreditInfoModel(
            R.drawable.ic_user,
            "1perialf",
            context.getString(R.string.info_rro_desc),
            "https://t.me/Rodolphe06",
        )
    )
    add(
        CreditInfoModel(
            R.drawable.ic_user,
            "modestCat",
            context.getString(R.string.info_rro_desc),
            "https://t.me/ModestCat03",
        )
    )
    add(
        CreditInfoModel(
            R.drawable.ic_user,
            "Sanely Insane",
            context.getString(R.string.info_tester_desc),
            "https://t.me/sanely_insane",
        )
    )
    add(
        CreditInfoModel(
            R.drawable.ic_user,
            "Jaguar",
            context.getString(R.string.info_tester_desc),
            "https://t.me/Jaguar0066",
        )
    )
    add(
        CreditInfoModel(
            R.drawable.ic_user,
            "hani & TeamFiles",
            context.getString(R.string.info_betterqs_desc),
            "https://github.com/itsHanibee",
        )
    )
    add(
        CreditInfoModel(
            R.drawable.ic_user,
            "AAGaming",
            context.getString(R.string.info_binaries_desc),
            "https://aagaming.me",
        )
    )
    add(
        CreditInfoModel(
            R.drawable.ic_link,
            "Buttercup Theme",
            context.getString(R.string.info_buttercup_desc),
            "https://t.me/buttercup_theme",
        )
    )
}

@Throws(JSONException::class)
fun parseContributors(context: Context): ArrayList<CreditInfoModel> {
    val excludedContributors = ArrayList<String>().apply {
        add("Mahmud0808")
        add("crowdin-bot")
    }

    val contributorsList = ArrayList<CreditInfoModel>()
    val jsonStr = AssetsUtils.readJson(context = context, fileName = "Misc/contributors.json")
    val jsonArray = JSONArray(jsonStr)

    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        val name = jsonObject.getString("login")

        if (excludedContributors.contains(name)) continue // Skip the excluded contributors

        val picture = jsonObject.getString("avatar_url")
        val commitsUrl = "https://github.com/Mahmud0808/Iconify/commits?author=$name"
        val contributions = jsonObject.getInt("contributions")

        contributorsList.add(
            CreditInfoModel(
                icon = picture,
                title = name,
                desc = context.getString(R.string.total_contributions, contributions),
                url = commitsUrl
            )
        )
    }

    return contributorsList
}

fun parseTranslators(context: Context): ArrayList<CreditInfoModel> {
    val contributorsList = ArrayList<CreditInfoModel>()
    val jsonStr = AssetsUtils.readJson(context = context, fileName = "Misc/translators.json")
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

        contributorsList.add(
            CreditInfoModel(
                icon = picture,
                title = name,
                desc = languages,
                url = url
            )
        )
    }

    return contributorsList
}