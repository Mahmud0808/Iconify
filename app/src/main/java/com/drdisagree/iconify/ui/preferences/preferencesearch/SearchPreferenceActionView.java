package com.drdisagree.iconify.ui.preferences.preferencesearch;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentManager;

public class SearchPreferenceActionView extends SearchView {
    protected SearchPreferenceFragment searchFragment;
    protected SearchConfiguration searchConfiguration = new SearchConfiguration();
    protected AppCompatActivity activity;

    public SearchPreferenceActionView(Context context) {
        super(context);
        initView();
    }

    public SearchPreferenceActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SearchPreferenceActionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        searchConfiguration.setSearchBarEnabled(false);
        setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchFragment != null) {
                    searchFragment.setSearchTerm(newText);
                }
                return true;
            }
        });
        setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && (searchFragment == null || !searchFragment.isVisible())) {
                searchFragment = searchConfiguration.showSearchFragment();
                searchFragment.setHistoryClickListener(entry -> setQuery(entry, false));
            }
        });
    }

    @SuppressWarnings("unused")
    public SearchConfiguration getSearchConfiguration() {
        return searchConfiguration;
    }

    /**
     * Hides the search fragment
     *
     * @return true if it was hidden, so the calling activity should not go back itself.
     */
    @SuppressWarnings("unused")
    public boolean cancelSearch() {
        setQuery("", false);

        boolean didSomething = false;
        if (!isIconified()) {
            setIconified(true);
            didSomething = true;
        }
        if (searchFragment != null && searchFragment.isVisible()) {
            removeFragment();
            didSomething = true;
        }
        return didSomething;
    }

    protected void removeFragment() {
        if (searchFragment.isVisible()) {
            FragmentManager fm = activity.getSupportFragmentManager();
            fm.beginTransaction().remove(searchFragment).commit();
            fm.popBackStack(SearchPreferenceFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    @SuppressWarnings("unused")
    public void setActivity(AppCompatActivity activity) {
        searchConfiguration.setActivity(activity);
        this.activity = activity;
    }
}
