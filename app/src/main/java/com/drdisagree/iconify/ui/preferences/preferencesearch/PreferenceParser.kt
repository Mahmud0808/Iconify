package com.drdisagree.iconify.ui.preferences.preferencesearch;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.drdisagree.iconify.config.ExtendedPrefs;
import com.drdisagree.iconify.config.PreferenceHelper;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class PreferenceParser {
    private static final int MAX_RESULTS = 10;
    private static final String NS_ANDROID = "http://schemas.android.com/apk/res/android";
    private static final String NS_SEARCH = "http://schemas.android.com/apk/it.dhd.oxygencustomizer.customprefs.preferencesearch";
    private static final List<String> BLACKLIST = Arrays.asList(SearchPreference.class.getName(), "PreferenceCategory");
    private static final List<String> CONTAINERS = Arrays.asList("PreferenceCategory", "PreferenceScreen");
    private final Context mContext;
    private final ArrayList<PreferenceItem> allEntries = new ArrayList<>();

    PreferenceParser(Context context) {
        mContext = context;
        PreferenceHelper.init(
                ExtendedPrefs.from(
                        getDefaultSharedPreferences(
                                context.createDeviceProtectedStorageContext()
                        )
                )
        );
    }

    @SuppressWarnings("UseCompareMethod")
    private static int floatCompare(float x, float y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    void addResourceFile(SearchConfiguration.SearchIndexItem item) {
        allEntries.addAll(parseFile(item));
    }

    void addPreferenceItems(ArrayList<PreferenceItem> preferenceItems) {
        preferenceItems.removeIf(item -> !PreferenceHelper.isVisible(item.key));
        allEntries.addAll(preferenceItems);
    }

    private ArrayList<PreferenceItem> parseFile(SearchConfiguration.SearchIndexItem item) {
        ArrayList<PreferenceItem> results = new ArrayList<>();
        XmlPullParser xpp = mContext.getResources().getXml(item.getResId());
        List<String> bannedKeys = item.getSearchConfiguration().getBannedKeys();

        try {
            xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            xpp.setFeature(XmlPullParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES, true);
            ArrayList<String> breadcrumbs = new ArrayList<>();
            ArrayList<String> keyBreadcrumbs = new ArrayList<>();
            if (!TextUtils.isEmpty(item.getBreadcrumb())) {
                breadcrumbs.add(item.getBreadcrumb());
            }
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xpp.getEventType() == XmlPullParser.START_TAG) {
                    PreferenceItem result = parseSearchResult(xpp);
                    result.resId = item.getResId();

                    try {
                        if (!isSearchable(result)) {
                            if (!bannedKeys.contains(result.key))
                                bannedKeys.add(result.key);
                        } else
                            bannedKeys.remove(result.key);
                    } catch (Exception ignored) {
                    }

                    if (!BLACKLIST.contains(xpp.getName())
                            && result.hasData()
                            && !"true".equals(getAttribute(xpp, NS_SEARCH, "ignore"))
                            && !bannedKeys.contains(result.key)
                            && shouldAddPreferenceItem(results, result)) {
                        result.breadcrumbs = joinBreadcrumbs(breadcrumbs);
                        result.keyBreadcrumbs = cleanupKeyBreadcrumbs(keyBreadcrumbs);
                        if (!results.contains(result)) {
                            results.add(result);
                        }
                    }
                    if (CONTAINERS.contains(xpp.getName())) {
                        breadcrumbs.add(result.title == null ? "" : result.title);
                    }
                    if (xpp.getName().equals("PreferenceScreen")) {
                        keyBreadcrumbs.add(getAttribute(xpp, "key"));
                    }
                } else if (xpp.getEventType() == XmlPullParser.END_TAG && CONTAINERS.contains(xpp.getName())) {
                    breadcrumbs.remove(breadcrumbs.size() - 1);
                    if (xpp.getName().equals("PreferenceScreen")) {
                        keyBreadcrumbs.remove(keyBreadcrumbs.size() - 1);
                    }
                }

                xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    private boolean isSearchable(PreferenceItem result) {
        if (TextUtils.isEmpty(result.key)) return false;
        return PreferenceHelper.isVisible(result.key);
    }

    private boolean shouldAddPreferenceItem(ArrayList<PreferenceItem> results, PreferenceItem result) {
        boolean isAlreadyAdded = false;
        for (PreferenceItem item : results) {
            if (item.key.equals(result.key) && item.resId == result.resId) {
                isAlreadyAdded = true;
                break;
            }
        }
        return !isAlreadyAdded;
    }

    private ArrayList<String> cleanupKeyBreadcrumbs(ArrayList<String> keyBreadcrumbs) {
        ArrayList<String> result = new ArrayList<>();
        for (String keyBreadcrumb : keyBreadcrumbs) {
            if (keyBreadcrumb != null) {
                result.add(keyBreadcrumb);
            }
        }
        return result;
    }

    private String joinBreadcrumbs(ArrayList<String> breadcrumbs) {
        String result = "";
        for (String crumb : breadcrumbs) {
            if (!TextUtils.isEmpty(crumb)) {
                result = Breadcrumb.concat(result, crumb);
            }
        }
        return result;
    }

    private String getAttribute(XmlPullParser xpp, @Nullable String namespace, @NonNull String attribute) {
        for (int i = 0; i < xpp.getAttributeCount(); i++) {
            if (attribute.equals(xpp.getAttributeName(i)) &&
                    (namespace == null || namespace.equals(xpp.getAttributeNamespace(i)))) {
                return xpp.getAttributeValue(i);
            }
        }
        return null;
    }

    private String getAttribute(XmlPullParser xpp, @NonNull String attribute) {
        if (hasAttribute(xpp, NS_SEARCH, attribute)) {
            return getAttribute(xpp, NS_SEARCH, attribute);
        } else {
            return getAttribute(xpp, NS_ANDROID, attribute);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private boolean hasAttribute(XmlPullParser xpp, @Nullable String namespace, @NonNull String attribute) {
        return getAttribute(xpp, namespace, attribute) != null;
    }

    private PreferenceItem parseSearchResult(XmlPullParser xpp) {
        PreferenceItem result = new PreferenceItem();
        result.title = readString(getAttribute(xpp, "title"));
        result.summary = readString(getAttribute(xpp, "summary"));
        result.key = readString(getAttribute(xpp, "key"));
        result.entries = readStringArray(getAttribute(xpp, "entries"));
        result.keywords = readString(getAttribute(xpp, NS_SEARCH, "keywords"));
        return result;
    }

    private String readStringArray(@Nullable String s) {
        if (s == null) {
            return null;
        }
        if (s.startsWith("@")) {
            try {
                int id = Integer.parseInt(s.substring(1));
                String[] elements = mContext.getResources().getStringArray(id);
                return TextUtils.join(",", elements);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return s;
    }

    private String readString(@Nullable String s) {
        if (s == null) {
            return null;
        }
        if (s.startsWith("@")) {
            try {
                int id = Integer.parseInt(s.substring(1));
                return mContext.getString(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return s;
    }

    List<PreferenceItem> searchFor(final String keyword, boolean fuzzy) {
        if (TextUtils.isEmpty(keyword)) {
            return new ArrayList<>();
        }
        ArrayList<PreferenceItem> results = new ArrayList<>();

        for (PreferenceItem item : allEntries) {
            if ((fuzzy && item.matchesFuzzy(keyword))
                    || (!fuzzy && item.matches(keyword))) {
                results.add(item);
            }
        }

        results.sort((i1, i2) -> floatCompare(i2.getScore(keyword), i1.getScore(keyword)));

        if (results.size() > MAX_RESULTS) {
            return results.subList(0, MAX_RESULTS);
        } else {
            return results;
        }
    }
}
