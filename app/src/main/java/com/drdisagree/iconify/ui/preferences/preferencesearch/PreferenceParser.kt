package com.drdisagree.iconify.ui.preferences.preferencesearch

import android.content.Context
import android.text.TextUtils
import com.drdisagree.iconify.config.PrefsHelper.isVisible
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchConfiguration.SearchIndexItem
import org.xmlpull.v1.XmlPullParser

internal class PreferenceParser(private val mContext: Context) {

    private val allEntries = ArrayList<PreferenceItem>()

    fun addResourceFile(item: SearchIndexItem) {
        allEntries.addAll(parseFile(item))
    }

    fun addPreferenceItems(preferenceItems: ArrayList<PreferenceItem>) {
        preferenceItems.removeIf { item: PreferenceItem -> !isVisible(item.key) }
        allEntries.addAll(preferenceItems)
    }

    private fun parseFile(item: SearchIndexItem): ArrayList<PreferenceItem> {
        val results = ArrayList<PreferenceItem>()
        val xpp: XmlPullParser = mContext.resources.getXml(item.resId)
        val bannedKeys: MutableList<String> = item.searchConfiguration.bannedKeys

        try {
            xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            xpp.setFeature(XmlPullParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES, true)
            val breadcrumbs = ArrayList<String>()
            val keyBreadcrumbs = ArrayList<String?>()
            if (!TextUtils.isEmpty(item.breadcrumb)) {
                breadcrumbs.add(item.breadcrumb)
            }
            while (xpp.eventType != XmlPullParser.END_DOCUMENT) {
                if (xpp.eventType == XmlPullParser.START_TAG) {
                    val result = parseSearchResult(xpp)
                    result.resId = item.resId

                    try {
                        if (!isSearchable(result)) {
                            if (!bannedKeys.contains(result.key)) bannedKeys.add(result.key)
                        } else bannedKeys.remove(result.key)
                    } catch (ignored: Exception) {
                    }

                    if (!BLACKLIST.contains(xpp.name)
                        && result.hasData()
                        && "true" != getAttribute(xpp, NS_SEARCH, "ignore") && !bannedKeys.contains(
                            result.key
                        )
                        && shouldAddPreferenceItem(results, result)
                    ) {
                        result.breadcrumbs = joinBreadcrumbs(breadcrumbs)
                        result.keyBreadcrumbs = cleanupKeyBreadcrumbs(keyBreadcrumbs)
                        if (!results.contains(result)) {
                            results.add(result)
                        }
                    }
                    if (CONTAINERS.contains(xpp.name)) {
                        breadcrumbs.add(if (result.title == null) "" else result.title)
                    }
                    if (xpp.name == "PreferenceScreen") {
                        keyBreadcrumbs.add(getAttribute(xpp, "key"))
                    }
                } else if (xpp.eventType == XmlPullParser.END_TAG && CONTAINERS.contains(xpp.name)) {
                    breadcrumbs.removeAt(breadcrumbs.size - 1)
                    if (xpp.name == "PreferenceScreen") {
                        keyBreadcrumbs.removeAt(keyBreadcrumbs.size - 1)
                    }
                }

                xpp.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results
    }

    private fun isSearchable(result: PreferenceItem): Boolean {
        if (TextUtils.isEmpty(result.key)) return false
        return isVisible(result.key)
    }

    private fun shouldAddPreferenceItem(
        results: ArrayList<PreferenceItem>,
        result: PreferenceItem
    ): Boolean {
        var isAlreadyAdded = false
        for (item in results) {
            if (item.key == result.key && item.resId == result.resId) {
                isAlreadyAdded = true
                break
            }
        }
        return !isAlreadyAdded
    }

    private fun cleanupKeyBreadcrumbs(keyBreadcrumbs: ArrayList<String?>): ArrayList<String> {
        val result = ArrayList<String>()
        for (keyBreadcrumb in keyBreadcrumbs) {
            if (keyBreadcrumb != null) {
                result.add(keyBreadcrumb)
            }
        }
        return result
    }

    private fun joinBreadcrumbs(breadcrumbs: ArrayList<String>): String? {
        var result: String? = ""
        for (crumb in breadcrumbs) {
            if (!TextUtils.isEmpty(crumb)) {
                result = Breadcrumb.concat(result, crumb)
            }
        }
        return result
    }

    private fun getAttribute(xpp: XmlPullParser, namespace: String?, attribute: String): String? {
        for (i in 0 until xpp.attributeCount) {
            if (attribute == xpp.getAttributeName(i) &&
                (namespace == null || namespace == xpp.getAttributeNamespace(i))
            ) {
                return xpp.getAttributeValue(i)
            }
        }
        return null
    }

    private fun getAttribute(xpp: XmlPullParser, attribute: String): String? {
        return if (hasAttribute(xpp, NS_SEARCH, attribute)) {
            getAttribute(xpp, NS_SEARCH, attribute)
        } else {
            getAttribute(xpp, NS_ANDROID, attribute)
        }
    }

    private fun hasAttribute(xpp: XmlPullParser, namespace: String?, attribute: String): Boolean {
        return getAttribute(xpp, namespace, attribute) != null
    }

    private fun parseSearchResult(xpp: XmlPullParser): PreferenceItem {
        val result = PreferenceItem()
        result.title = readString(getAttribute(xpp, "title"))
        result.summary = readString(getAttribute(xpp, "summary"))
        result.key = readString(getAttribute(xpp, "key"))
        result.entries = readStringArray(getAttribute(xpp, "entries"))
        result.keywords = readString(getAttribute(xpp, NS_SEARCH, "keywords"))
        return result
    }

    private fun readStringArray(s: String?): String? {
        if (s == null) {
            return null
        }
        if (s.startsWith("@")) {
            try {
                val id = s.substring(1).toInt()
                val elements = mContext.resources.getStringArray(id)
                return TextUtils.join(",", elements)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return s
    }

    private fun readString(s: String?): String? {
        if (s == null) {
            return null
        }
        if (s.startsWith("@")) {
            try {
                val id = s.substring(1).toInt()
                return mContext.getString(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return s
    }

    fun searchFor(keyword: String?, fuzzy: Boolean): List<PreferenceItem> {
        if (TextUtils.isEmpty(keyword)) {
            return ArrayList()
        }
        val results = ArrayList<PreferenceItem>()

        for (item in allEntries) {
            if ((fuzzy && item.matchesFuzzy(keyword))
                || (!fuzzy && item.matches(keyword))
            ) {
                results.add(item)
            }
        }

        results.sortWith { i1: PreferenceItem, i2: PreferenceItem ->
            floatCompare(
                i2.getScore(keyword),
                i1.getScore(keyword)
            )
        }

        return if (results.size > MAX_RESULTS) {
            results.subList(0, MAX_RESULTS)
        } else {
            results
        }
    }

    companion object {
        private const val MAX_RESULTS = 10
        private const val NS_ANDROID = "http://schemas.android.com/apk/res/android"
        private const val NS_SEARCH =
            "http://schemas.android.com/apk/com.drdisagree.iconify.ui.preferencesearch.preferencesearch"
        private val BLACKLIST: List<String> = listOf(
            SearchPreference::class.java.name, "PreferenceCategory"
        )
        private val CONTAINERS: List<String> =
            mutableListOf("PreferenceCategory", "PreferenceScreen")

        private fun floatCompare(x: Float, y: Float): Int {
            return if ((x < y)) -1 else (if ((x == y)) 0 else 1)
        }
    }
}
