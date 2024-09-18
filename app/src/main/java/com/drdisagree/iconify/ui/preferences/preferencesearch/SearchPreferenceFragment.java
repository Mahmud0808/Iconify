package com.drdisagree.iconify.ui.preferences.preferencesearch;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SearchPreferenceFragment extends Fragment implements SearchPreferenceAdapter.SearchClickListener {
    /**
     * Default tag used on the library's Fragment transactions with {@link SearchPreferenceFragment}
     */
    public static final String TAG = SearchPreferenceFragment.class.getSimpleName();

    private static final String SHARED_PREFS_FILE = "preferenceSearch";
    private static final int MAX_HISTORY = 5;
    private PreferenceParser searcher;
    private List<PreferenceItem> results;
    private List<HistoryItem> history;
    private SharedPreferences prefs;
    private SearchViewHolder viewHolder;
    private SearchConfiguration searchConfiguration;
    private SearchPreferenceAdapter adapter;
    private HistoryClickListener historyClickListener;
    private CharSequence searchTermPreset = null;
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            updateSearchResults(editable.toString());
            viewHolder.clearButton.setVisibility(editable.toString().isEmpty() ? View.GONE : View.VISIBLE);
        }
    };

    private void initSearch() {
        searcher = new PreferenceParser(requireContext());

        assert getArguments() != null;
        searchConfiguration = SearchConfiguration.fromBundle(getArguments());
        ArrayList<SearchConfiguration.SearchIndexItem> files = searchConfiguration.getFiles();
        for (SearchConfiguration.SearchIndexItem file : files) {
            searcher.addResourceFile(file);
        }
        searcher.addPreferenceItems(searchConfiguration.getPreferencesToIndex());
        loadHistory();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = requireContext().getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        initSearch();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflater.getContext().setTheme(R.style.PrefsThemeToolbar);
        View rootView = inflater.inflate(R.layout.searchpreference_fragment, container, false);
        viewHolder = new SearchViewHolder(rootView);

        viewHolder.clearButton.setOnClickListener(view -> viewHolder.searchView.setText(""));
        if (searchConfiguration.isHistoryEnabled()) {
            viewHolder.moreButton.setVisibility(View.VISIBLE);
        }
        if (searchConfiguration.getTextHint() != null) {
            viewHolder.searchView.setHint(searchConfiguration.getTextHint());
        }
        if (searchConfiguration.getTextNoResults() != null) {
            viewHolder.noResults.setText(searchConfiguration.getTextNoResults());
        }
        viewHolder.moreButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), viewHolder.moreButton);
            popup.getMenuInflater().inflate(R.menu.search_more, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.clear_history) {
                    clearHistory();
                }
                return true;
            });
            popup.show();
        });

        viewHolder.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SearchPreferenceAdapter();
        adapter.setSearchConfiguration(searchConfiguration);
        adapter.setOnItemClickListener(this);
        viewHolder.recyclerView.setAdapter(adapter);

        viewHolder.searchView.addTextChangedListener(textWatcher);

        if (!searchConfiguration.isSearchBarEnabled()) {
            viewHolder.searchContainer.setVisibility(View.GONE);
        }

        if (searchTermPreset != null) {
            viewHolder.searchView.setText(searchTermPreset);
        }

        RevealAnimationSetting anim = searchConfiguration.getRevealAnimationSetting();
        if (anim != null) {
            AnimationUtils.registerCircularRevealAnimation(requireContext(), rootView, anim);
        }

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.search_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void loadHistory() {
        history = new ArrayList<>();
        if (!searchConfiguration.isHistoryEnabled()) {
            return;
        }

        int size = prefs.getInt("history_size", 0);
        for (int i = 0; i < size; i++) {
            String title = prefs.getString("history_" + i, null);
            history.add(new HistoryItem(title));
        }
    }

    private void saveHistory() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("history_size", history.size());
        for (int i = 0; i < history.size(); i++) {
            editor.putString("history_" + i, history.get(i).getTerm());
        }
        editor.apply();
    }

    private void clearHistory() {
        viewHolder.searchView.setText("");
        history.clear();
        saveHistory();
        updateSearchResults("");
    }

    private void addHistoryEntry(String entry) {
        HistoryItem newItem = new HistoryItem(entry);
        if (!history.contains(newItem)) {
            if (history.size() >= MAX_HISTORY) {
                history.remove(history.size() - 1);
            }
            history.add(0, newItem);
            saveHistory();
            updateSearchResults(viewHolder.searchView.getText().toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        initSearch();

        updateSearchResults(viewHolder.searchView.getText().toString());

        if (searchConfiguration.isSearchBarEnabled()) {
            showKeyboard();
        }

        if (getContext() != null) {
            if (viewHolder.toolbar != null) {
                ((AppCompatActivity) getContext()).setSupportActionBar(viewHolder.toolbar);
                viewHolder.toolbar.setTitle(R.string.search_title);
            }

            if (getContext() instanceof AppCompatActivity) {
                Objects.requireNonNull(((AppCompatActivity) getContext())
                        .getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void showKeyboard() {
        viewHolder.searchView.post(() -> {
            viewHolder.searchView.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(viewHolder.searchView, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null && imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void setSearchTerm(CharSequence term) {
        if (viewHolder != null) {
            viewHolder.searchView.setText(term);
        } else {
            searchTermPreset = term;
        }
    }

    private void updateSearchResults(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            showHistory();
            return;
        }

        results = searcher.searchFor(keyword, searchConfiguration.isFuzzySearchEnabled());

        Set<String> seen = new HashSet<>();
        List<PreferenceItem> distinctResults = new ArrayList<>();

        for (PreferenceItem result : results) {
            String key = result.title + result.summary + result.breadcrumbs;
            if (seen.add(key)) {
                distinctResults.add(result);
            }
        }

        adapter.setContent(new ArrayList<>(distinctResults));

        setEmptyViewShown(distinctResults.isEmpty());
    }

    private void setEmptyViewShown(boolean shown) {
        if (shown) {
            viewHolder.noResults.setVisibility(View.VISIBLE);
            viewHolder.recyclerView.setVisibility(View.GONE);
        } else {
            viewHolder.noResults.setVisibility(View.GONE);
            viewHolder.recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showHistory() {
        viewHolder.noResults.setVisibility(View.GONE);
        viewHolder.recyclerView.setVisibility(View.VISIBLE);

        adapter.setContent(new ArrayList<>(history));
        setEmptyViewShown(history.isEmpty());
    }

    @Override
    public void onItemClicked(ListItem item, int position) {
        if (item.getType() == HistoryItem.TYPE) {
            CharSequence text = ((HistoryItem) item).getTerm();
            viewHolder.searchView.setText(text);
            viewHolder.searchView.setSelection(text.length());
            if (historyClickListener != null) {
                historyClickListener.onHistoryEntryClicked(text.toString());
            }
        } else {
            hideKeyboard();

            try {
                final SearchPreferenceResultListener callback = (SearchPreferenceResultListener) getActivity();
                PreferenceItem r = results.get(position);
                addHistoryEntry(r.title);
                String screen = null;
                if (!r.keyBreadcrumbs.isEmpty()) {
                    screen = r.keyBreadcrumbs.get(r.keyBreadcrumbs.size() - 1);
                }
                SearchPreferenceResult result = new SearchPreferenceResult(r.key, r.resId, screen);
                assert callback != null;
                callback.onSearchResultClicked(result);
            } catch (ClassCastException e) {
                throw new ClassCastException(requireActivity() + " must implement SearchPreferenceResultListener");
            }
        }
    }

    public void setHistoryClickListener(HistoryClickListener historyClickListener) {
        this.historyClickListener = historyClickListener;
    }

    public interface HistoryClickListener {
        void onHistoryEntryClicked(String entry);
    }

    private static class SearchViewHolder {
        private final ImageView clearButton;
        private final ImageView moreButton;
        private final EditText searchView;
        private final RecyclerView recyclerView;
        private final TextView noResults;
        private final MaterialCardView searchContainer;
        private final MaterialToolbar toolbar;

        SearchViewHolder(View root) {
            searchView = root.findViewById(R.id.search);
            clearButton = root.findViewById(R.id.clear);
            recyclerView = root.findViewById(R.id.list);
            moreButton = root.findViewById(R.id.more);
            noResults = root.findViewById(R.id.no_results);
            searchContainer = root.findViewById(R.id.search_card);
            toolbar = root.findViewById(R.id.toolbar);
        }
    }
}
