package com.drdisagree.iconify.ui.preferences.preferencesearch;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.drdisagree.iconify.R;

public class SearchPreference extends Preference implements View.OnClickListener {
    private final SearchConfiguration searchConfiguration = new SearchConfiguration();
    private String hint = null;

    @SuppressWarnings("unused")
    public SearchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.searchpreference_preference);
        parseAttrs(attrs);
    }

    @SuppressWarnings("unused")
    public SearchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.searchpreference_preference);
        parseAttrs(attrs);
    }

    @SuppressWarnings("unused")
    public SearchPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.searchpreference_preference);
    }

    private void parseAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, new int[]{R.attr.textHint});
        if (a.getText(0) != null) {
            hint = a.getText(0).toString();
            searchConfiguration.setTextHint(a.getText(0).toString());
        }
        a.recycle();
        a = getContext().obtainStyledAttributes(attrs, new int[]{R.attr.textClearHistory});
        if (a.getText(0) != null) {
            searchConfiguration.setTextClearHistory(a.getText(0).toString());
        }
        a.recycle();
        a = getContext().obtainStyledAttributes(attrs, new int[]{R.attr.textNoResults});
        if (a.getText(0) != null) {
            searchConfiguration.setTextNoResults(a.getText(0).toString());
        }
        a.recycle();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        EditText searchText = (EditText) holder.findViewById(R.id.search);
        searchText.setFocusable(false);
        searchText.setInputType(InputType.TYPE_NULL);
        searchText.setOnClickListener(this);

        if (hint != null) {
            searchText.setHint(hint);
        }

        holder.findViewById(R.id.search_card).setOnClickListener(this);
        holder.itemView.setOnClickListener(this);
        holder.itemView.setBackgroundColor(0x0);
    }

    @Override
    public void onClick(View view) {
        getSearchConfiguration().showSearchFragment();
    }

    /**
     * Returns the search configuration object for this preference
     *
     * @return The search configuration
     */
    public SearchConfiguration getSearchConfiguration() {
        return searchConfiguration;
    }
}
