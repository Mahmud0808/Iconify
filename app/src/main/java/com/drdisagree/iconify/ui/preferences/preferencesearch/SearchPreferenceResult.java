package com.drdisagree.iconify.ui.preferences.preferencesearch;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.google.android.material.appbar.AppBarLayout;

import java.util.Objects;

public class SearchPreferenceResult {
    private final String key;
    private final int file;
    private final String screen;

    SearchPreferenceResult(String key, int file, String screen) {
        this.key = key;
        this.file = file;
        this.screen = screen;
    }

    public static void highlight(final PreferenceFragmentCompat prefsFragment, final String key) {
        new Handler(Looper.getMainLooper()).post(() -> doHighlight(prefsFragment, key));
    }

    private static void doHighlight(final PreferenceFragmentCompat prefsFragment, final String key) {
        final Preference prefResult = prefsFragment.findPreference(key);

        if (prefResult == null) {
            Log.w("doHighlight", "Preference with key " + key + " not found on given screen " + prefsFragment.getClass().getSimpleName() + ".");
            return;
        }

        final RecyclerView recyclerView = prefsFragment.getListView();
        final RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();

        if (adapter instanceof PreferenceGroup.PreferencePositionCallback callback) {
            final int position = callback.getPreferenceAdapterPosition(prefResult);

            if (prefsFragment.getView() != null && prefsFragment.getView().findViewById(R.id.collapsing_toolbar) != null) {
                AppBarLayout appBarLayout = (AppBarLayout) prefsFragment.getView().findViewById(R.id.collapsing_toolbar).getParent();
                appBarLayout.setExpanded(false);
            }

            if (position != RecyclerView.NO_POSITION) {
                recyclerView.post(() -> recyclerView.scrollToPosition(position));

                recyclerView.postDelayed(() -> {
                    RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
                    if (holder != null) {
                        Drawable background = holder.itemView.getBackground();
                        if (background instanceof RippleDrawable) {
                            forceRippleAnimation((RippleDrawable) background);
                            return;
                        }
                    }
                    highlightFallback(prefsFragment, prefResult);
                }, 200);
                return;
            }
        }

        highlightFallback(prefsFragment, prefResult);
    }

    /**
     * Alternative (old) highlight method if ripple does not work
     */
    private static void highlightFallback(PreferenceFragmentCompat prefsFragment, final Preference prefResult) {
        prefsFragment.scrollToPreference(prefResult);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                final RecyclerView recyclerView = prefsFragment.getListView();
                final RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();

                if (adapter instanceof PreferenceGroup.PreferencePositionCallback callback) {
                    final int position = callback.getPreferenceAdapterPosition(prefResult);

                    if (prefsFragment.getView() != null && prefsFragment.getView().findViewById(R.id.collapsing_toolbar) != null) {
                        AppBarLayout appBarLayout = (AppBarLayout) prefsFragment.getView().findViewById(R.id.collapsing_toolbar).getParent();
                        appBarLayout.setExpanded(false);
                    }

                    if (position != RecyclerView.NO_POSITION) {
                        recyclerView.post(() -> recyclerView.scrollToPosition(position));

                        recyclerView.postDelayed(() -> {
                            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
                            if (holder != null) {
                                Drawable background = holder.itemView.getBackground();
                                if (background instanceof RippleDrawable) {
                                    forceRippleAnimation((RippleDrawable) background);
                                }
                            }
                        }, 200);
                    }
                }
            } catch (Exception e) {
                Log.e("highlightFallback", "Failed to highlight preference", e);
            }
        }, 400);
    }

    protected static void forceRippleAnimation(RippleDrawable background) {
        final RippleDrawable rippleDrawable = background;
        rippleDrawable.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});
        new Handler(Looper.getMainLooper()).postDelayed(() -> rippleDrawable.setState(new int[]{}), 300);
    }

    /**
     * Returns the key of the preference pressed
     *
     * @return The key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the file in which the result was found
     *
     * @return The file in which the result was found
     */
    public int getResourceFile() {
        return file;
    }

    /**
     * Returns the screen in which the result was found
     *
     * @return The screen in which the result was found
     */
    public String getScreen() {
        return screen;
    }

    /**
     * Highlight the preference that was found
     *
     * @param prefsFragment Fragment that contains the preference
     */
    @SuppressWarnings("unused")
    public void highlight(final PreferenceFragmentCompat prefsFragment) {
        new Handler(Looper.getMainLooper()).post(() -> doHighlight(prefsFragment, getKey()));
    }

    /**
     * Closes the search results page
     *
     * @param activity The current activity
     */
    @SuppressWarnings("unused")
    public void closeSearchPage(AppCompatActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        fm.beginTransaction().remove(Objects.requireNonNull(fm.findFragmentByTag(SearchPreferenceFragment.TAG))).commit();
    }
}