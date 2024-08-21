package com.drdisagree.iconify.ui.preferences.preferencesearch;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.XmlRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import java.util.ArrayList;
import java.util.Arrays;

public class SearchConfiguration {
    private static final String ARGUMENT_INDEX_FILES = "items";
    private static final String ARGUMENT_INDEX_INDIVIDUAL_PREFERENCES = "individual_prefs";
    private static final String ARGUMENT_FUZZY_ENABLED = "fuzzy";
    private static final String ARGUMENT_HISTORY_ENABLED = "history_enabled";
    private static final String ARGUMENT_SEARCH_BAR_ENABLED = "search_bar_enabled";
    private static final String ARGUMENT_BREADCRUMBS_ENABLED = "breadcrumbs_enabled";
    private static final String ARGUMENT_REVEAL_ANIMATION_SETTING = "reveal_anim_setting";
    private static final String ARGUMENT_TEXT_HINT = "text_hint";
    private static final String ARGUMENT_TEXT_CLEAR_HISTORY = "text_clear_history";
    private static final String ARGUMENT_TEXT_NO_RESULTS = "text_no_results";
    private final ArrayList<String> bannedKeys = new ArrayList<>();
    private ArrayList<SearchIndexItem> filesToIndex = new ArrayList<>();
    private ArrayList<PreferenceItem> preferencesToIndex = new ArrayList<>();
    private boolean historyEnabled = true;
    private boolean breadcrumbsEnabled = false;
    private boolean fuzzySearchEnabled = true;
    private boolean searchBarEnabled = true;
    private AppCompatActivity activity;
    private int containerResId = android.R.id.content;
    private RevealAnimationSetting revealAnimationSetting = null;
    private String textClearHistory;
    private String textNoResults;
    private String textHint;

    public SearchConfiguration() {

    }

    /**
     * Creates a new search configuration
     *
     * @param activity The Activity that receives callbacks. Must implement SearchPreferenceResultListener.
     */
    @SuppressWarnings("unused")
    public SearchConfiguration(AppCompatActivity activity) {
        setActivity(activity);
    }

    static SearchConfiguration fromBundle(Bundle bundle) {
        SearchConfiguration config = new SearchConfiguration();
        config.filesToIndex = bundle.getParcelableArrayList(ARGUMENT_INDEX_FILES);
        config.preferencesToIndex = bundle.getParcelableArrayList(ARGUMENT_INDEX_INDIVIDUAL_PREFERENCES);
        config.historyEnabled = bundle.getBoolean(ARGUMENT_HISTORY_ENABLED);
        config.revealAnimationSetting = bundle.getParcelable(ARGUMENT_REVEAL_ANIMATION_SETTING);
        config.fuzzySearchEnabled = bundle.getBoolean(ARGUMENT_FUZZY_ENABLED);
        config.breadcrumbsEnabled = bundle.getBoolean(ARGUMENT_BREADCRUMBS_ENABLED);
        config.searchBarEnabled = bundle.getBoolean(ARGUMENT_SEARCH_BAR_ENABLED);
        config.textHint = bundle.getString(ARGUMENT_TEXT_HINT);
        config.textClearHistory = bundle.getString(ARGUMENT_TEXT_CLEAR_HISTORY);
        config.textNoResults = bundle.getString(ARGUMENT_TEXT_NO_RESULTS);
        return config;
    }

    /**
     * Shows the fragment
     *
     * @return A reference to the fragment
     */
    public SearchPreferenceFragment showSearchFragment() {
        if (activity == null) {
            throw new IllegalStateException("setActivity() not called");
        }

        Bundle arguments = this.toBundle();
        SearchPreferenceFragment fragment = new SearchPreferenceFragment();
        fragment.setArguments(arguments);
        activity.getSupportFragmentManager().beginTransaction()
                .add(containerResId, fragment, SearchPreferenceFragment.TAG)
                .addToBackStack(SearchPreferenceFragment.TAG)
                .commit();
        return fragment;
    }

    private Bundle toBundle() {
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList(ARGUMENT_INDEX_FILES, filesToIndex);
        arguments.putParcelableArrayList(ARGUMENT_INDEX_INDIVIDUAL_PREFERENCES, preferencesToIndex);
        arguments.putBoolean(ARGUMENT_HISTORY_ENABLED, historyEnabled);
        arguments.putParcelable(ARGUMENT_REVEAL_ANIMATION_SETTING, revealAnimationSetting);
        arguments.putBoolean(ARGUMENT_FUZZY_ENABLED, fuzzySearchEnabled);
        arguments.putBoolean(ARGUMENT_BREADCRUMBS_ENABLED, breadcrumbsEnabled);
        arguments.putBoolean(ARGUMENT_SEARCH_BAR_ENABLED, searchBarEnabled);
        arguments.putString(ARGUMENT_TEXT_HINT, textHint);
        arguments.putString(ARGUMENT_TEXT_CLEAR_HISTORY, textClearHistory);
        arguments.putString(ARGUMENT_TEXT_NO_RESULTS, textNoResults);
        return arguments;
    }

    /**
     * Sets the current activity that also receives callbacks
     *
     * @param activity The Activity that receives callbacks. Must implement SearchPreferenceResultListener.
     */
    public void setActivity(@NonNull AppCompatActivity activity) {
        this.activity = activity;
        if (!(activity instanceof SearchPreferenceResultListener)) {
            throw new IllegalArgumentException("Activity must implement SearchPreferenceResultListener");
        }
    }

    /**
     * Sets the container to use when loading the fragment
     *
     * @param containerResId Resource id of the container
     */
    public void setFragmentContainerViewId(@IdRes int containerResId) {
        this.containerResId = containerResId;
    }

    /**
     * Display a reveal animation
     *
     * @param centerX     Origin of the reveal animation
     * @param centerY     Origin of the reveal animation
     * @param width       Size of the main container
     * @param height      Size of the main container
     * @param colorAccent Accent color to use
     */
    @SuppressWarnings("unused")
    public void useAnimation(int centerX, int centerY, int width, int height, @ColorInt int colorAccent) {
        revealAnimationSetting = new RevealAnimationSetting(centerX, centerY, width, height, colorAccent);
    }

    /**
     * Adds a new file to the index
     *
     * @param resId The preference file to index
     */
    public SearchIndexItem index(@XmlRes int resId) {
        SearchIndexItem item = new SearchIndexItem(resId, this);
        filesToIndex.add(item);
        return item;
    }

    /**
     * Indexes a single preference
     *
     * @return the indexed PreferenceItem to configure it with chaining
     * @see PreferenceItem for the available methods for configuring it
     */
    @SuppressWarnings("unused")
    public PreferenceItem indexItem() {
        PreferenceItem preferenceItem = new PreferenceItem();
        preferencesToIndex.add(preferenceItem);
        return preferenceItem;
    }

    /**
     * Indexes a single android preference
     *
     * @param preference to get its key, summary, title and entries
     * @return the indexed PreferenceItem to configure it with chaining
     * @see PreferenceItem for the available methods for configuring it
     */
    @SuppressWarnings("unused")
    public PreferenceItem indexItem(@NonNull Preference preference) {
        PreferenceItem preferenceItem = new PreferenceItem();

        if (preference.getKey() != null) {
            preferenceItem.key = preference.getKey();
        }
        if (preference.getSummary() != null) {
            preferenceItem.summary = preference.getSummary().toString();
        }
        if (preference.getTitle() != null) {
            preferenceItem.title = preference.getTitle().toString();
        }
        if (preference instanceof ListPreference listPreference) {
            if (listPreference.getEntries() != null) {
                preferenceItem.entries = Arrays.toString(listPreference.getEntries());
            }
        }
        preferencesToIndex.add(preferenceItem);
        return preferenceItem;
    }

    ArrayList<String> getBannedKeys() {
        return bannedKeys;
    }

    /**
     * @param key of the preference to be ignored
     */
    @SuppressWarnings("unused")
    public void ignorePreference(@NonNull String key) {
        bannedKeys.add(key);
    }

    ArrayList<SearchIndexItem> getFiles() {
        return filesToIndex;
    }

    ArrayList<PreferenceItem> getPreferencesToIndex() {
        return preferencesToIndex;
    }

    boolean isHistoryEnabled() {
        return historyEnabled;
    }

    /**
     * Show a history of recent search terms if nothing was typed yet. Default is true
     *
     * @param historyEnabled True if history should be enabled
     */
    public void setHistoryEnabled(boolean historyEnabled) {
        this.historyEnabled = historyEnabled;
    }

    boolean isBreadcrumbsEnabled() {
        return breadcrumbsEnabled;
    }

    /**
     * Show breadcrumbs in the list of search results, containing of
     * the prefix given in addResourceFileToIndex, PreferenceCategory and PreferenceScreen.
     * Default is false
     *
     * @param breadcrumbsEnabled True if breadcrumbs should be shown
     */
    public void setBreadcrumbsEnabled(boolean breadcrumbsEnabled) {
        this.breadcrumbsEnabled = breadcrumbsEnabled;
    }

    boolean isFuzzySearchEnabled() {
        return fuzzySearchEnabled;
    }

    /**
     * Allow to enable and disable fuzzy searching. Default is true
     *
     * @param fuzzySearchEnabled True if search should be fuzzy
     */
    public void setFuzzySearchEnabled(boolean fuzzySearchEnabled) {
        this.fuzzySearchEnabled = fuzzySearchEnabled;
    }

    boolean isSearchBarEnabled() {
        return searchBarEnabled;
    }

    /**
     * Show the search bar above the list. When setting this to false, you have to use {@see SearchPreferenceFragment#setSearchTerm(String) setSearchTerm} instead
     * Default is true
     *
     * @param searchBarEnabled True if search bar should be shown
     */
    public void setSearchBarEnabled(boolean searchBarEnabled) {
        this.searchBarEnabled = searchBarEnabled;
    }

    RevealAnimationSetting getRevealAnimationSetting() {
        return revealAnimationSetting;
    }

    @SuppressWarnings("unused")
    public String getTextClearHistory() {
        return textClearHistory;
    }

    public void setTextClearHistory(String textClearHistory) {
        this.textClearHistory = textClearHistory;
    }

    public String getTextNoResults() {
        return textNoResults;
    }

    public void setTextNoResults(String textNoResults) {
        this.textNoResults = textNoResults;
    }

    public String getTextHint() {
        return textHint;
    }

    public void setTextHint(String textHint) {
        this.textHint = textHint;
    }

    /**
     * Adds a given R.xml resource to the search index
     */
    public static class SearchIndexItem implements Parcelable {
        public static final Creator<SearchIndexItem> CREATOR = new Creator<SearchIndexItem>() {
            @Override
            public SearchIndexItem createFromParcel(Parcel in) {
                return new SearchIndexItem(in);
            }

            @Override
            public SearchIndexItem[] newArray(int size) {
                return new SearchIndexItem[size];
            }
        };
        private final @XmlRes int resId;
        private final SearchConfiguration searchConfiguration;
        private String breadcrumb = "";

        /**
         * Includes the given R.xml resource in the index
         *
         * @param resId The resource to index
         */
        private SearchIndexItem(@XmlRes int resId, SearchConfiguration searchConfiguration) {
            this.resId = resId;
            this.searchConfiguration = searchConfiguration;
        }

        private SearchIndexItem(Parcel parcel) {
            this.breadcrumb = parcel.readString();
            this.resId = parcel.readInt();
            this.searchConfiguration = null;
        }

        /**
         * Adds a breadcrumb
         *
         * @param breadcrumb The breadcrumb to add
         * @return For chaining
         */
        @SuppressWarnings("unused")
        public SearchIndexItem addBreadcrumb(@StringRes int breadcrumb) {
            assertNotParcel();
            return addBreadcrumb(searchConfiguration.activity.getString(breadcrumb));
        }

        /**
         * Adds a breadcrumb
         *
         * @param breadcrumb The breadcrumb to add
         * @return For chaining
         */
        public SearchIndexItem addBreadcrumb(String breadcrumb) {
            assertNotParcel();
            this.breadcrumb = Breadcrumb.concat(this.breadcrumb, breadcrumb);
            return this;
        }

        /**
         * Throws an exception if the item does not have a searchConfiguration (thus, is restored from a parcel)
         */
        private void assertNotParcel() {
            if (searchConfiguration == null) {
                throw new IllegalStateException("SearchIndexItems that are restored from parcel can not be modified.");
            }
        }

        @XmlRes
        int getResId() {
            return resId;
        }

        String getBreadcrumb() {
            return breadcrumb;
        }

        SearchConfiguration getSearchConfiguration() {
            return searchConfiguration;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.breadcrumb);
            dest.writeInt(this.resId);
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }
}
