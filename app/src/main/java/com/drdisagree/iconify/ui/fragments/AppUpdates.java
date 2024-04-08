package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.LATEST_VERSION_URL;
import static com.drdisagree.iconify.common.Preferences.UPDATE_CHECK_TIME;
import static com.drdisagree.iconify.common.Preferences.UPDATE_SCHEDULE;
import static com.drdisagree.iconify.common.Preferences.VER_CODE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentAppUpdatesBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.extension.TaskExecutor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppUpdates extends BaseFragment {

    public static final String KEY_NEW_UPDATE = "new_update_available";
    private FragmentAppUpdatesBinding binding;
    private CheckForUpdate checkForUpdate = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAppUpdatesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.app_updates);

        // Update Schedule
        binding.updateSchedule.setSelectedIndex(Prefs.getInt(UPDATE_SCHEDULE, 1));
        binding.updateSchedule.setOnItemSelectedListener(
                index -> {
                    Prefs.putInt(UPDATE_SCHEDULE, index);

                    switch (index) {
                        case 0 -> Prefs.putLong(UPDATE_CHECK_TIME, 6); // Every 6 Hours
                        case 1 -> Prefs.putLong(UPDATE_CHECK_TIME, 12); // Every 12 Hour
                        case 2 -> Prefs.putLong(UPDATE_CHECK_TIME, 24); // Every Day
                        case 3 -> Prefs.putLong(UPDATE_CHECK_TIME, (long) 24 * 7); // Every Week
                    }
                }
        );

        try {
            checkForUpdate = new CheckForUpdate();
            checkForUpdate.execute();
        } catch (Exception ignored) {
        }

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void failedToCheck() {
        binding.updateTitle.setText(Iconify.getAppContextLocale().getResources().getString(R.string.update_checking_failed));
        binding.currentVersion.setText(Iconify.getAppContextLocale().getResources().getString(R.string.current_version) + " " + BuildConfig.VERSION_NAME);
        binding.latestVersion.setText(Iconify.getAppContextLocale().getResources().getString(R.string.latest_version) + " " + Iconify.getAppContextLocale().getResources().getString(R.string.not_available));
    }

    @Override
    public void onDestroy() {
        if (checkForUpdate != null) checkForUpdate.cancel(true);
        super.onDestroy();
    }

    @Override
    public void onStop() {
        if (checkForUpdate != null) checkForUpdate.cancel(true);
        super.onStop();
    }

    @SuppressLint("StaticFieldLeak")
    private class CheckForUpdate extends TaskExecutor<Integer, Integer, String> {

        String jsonURL = LATEST_VERSION_URL;

        @Override
        protected void onPreExecute() {
            binding.checkingForUpdate.setVisibility(View.VISIBLE);
            binding.checkedForUpdate.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(Integer... integers) {
            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            try {
                URL url = new URL(jsonURL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder changes = new StringBuilder();

                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    changes.append(line).append("\n");
                }
                if (changes.length() == 0) {
                    return null;
                } else {
                    return changes.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String jsonStr) {
            if (jsonStr != null) {
                try {
                    JSONObject latestVersion = new JSONObject(jsonStr);

                    if (Integer.parseInt(latestVersion.getString(VER_CODE)) > BuildConfig.VERSION_CODE) {
                        binding.updateTitle.setText(getResources().getString(R.string.update_available));
                        binding.checkUpdate.setBackgroundResource(R.drawable.container_outline);
                        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.checkUpdate.getLayoutParams();
                        layoutParams.setMargins(ViewHelper.dp2px(16), ViewHelper.dp2px(16), ViewHelper.dp2px(16), 0);
                        binding.checkUpdate.setLayoutParams(layoutParams);
                        binding.downloadUpdate.setOnClickListener(v -> {
                            try {
                                String apkUrl = latestVersion.getString("apkUrl");
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(apkUrl));
                                startActivity(i);
                            } catch (JSONException e) {
                                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        });
                        binding.downloadUpdate.setVisibility(View.VISIBLE);

                        String title, changes;

                        try {
                            JSONArray latestChangelog = latestVersion.getJSONArray("changelog");
                            StringBuilder release_note = new StringBuilder();

                            for (int i = 0; i < latestChangelog.length(); i++) {
                                release_note.append(latestChangelog.get(i));
                            }

                            title = release_note.substring(0, release_note.indexOf("\n\n"));
                            changes = release_note.substring(release_note.indexOf("\n##")).substring(1);

                            title = title.replace("### ", "");
                            changes = Changelog.usernameToLink(changes.replace("## ", "<b>").replace(":\n", ":</b><br>").replace("- __", "<br><b>• ").replace("__\n", "</b><br>").replace("    - ", "&emsp;◦ ").replace("- ", "• ").replace("\n", "<br>"));

                            binding.changelogTitle.setText(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY));
                            binding.changelogText.setText(HtmlCompat.fromHtml(changes, HtmlCompat.FROM_HTML_MODE_LEGACY));

                            SpannableString spannableString = new SpannableString(HtmlCompat.fromHtml(changes, HtmlCompat.FROM_HTML_MODE_LEGACY));
                            URLSpan[] urls = spannableString.getSpans(0, spannableString.length(), URLSpan.class);

                            for (URLSpan urlSpan : urls) {
                                ClickableSpan clickableSpan = new ClickableSpan() {
                                    @Override
                                    public void onClick(@NonNull View view) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlSpan.getURL()));
                                        startActivity(intent);
                                    }
                                };

                                int start = spannableString.getSpanStart(urlSpan);
                                int end = spannableString.getSpanEnd(urlSpan);
                                spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                spannableString.removeSpan(urlSpan);
                            }

                            binding.changelogText.setText(spannableString);
                            binding.changelogText.setMovementMethod(LinkMovementMethod.getInstance());
                        } catch (Exception e) {
                            e.printStackTrace();

                            binding.changelogTitle.setText(HtmlCompat.fromHtml(getResources().getString(R.string.individual_changelog_not_found), HtmlCompat.FROM_HTML_MODE_LEGACY));
                            binding.changelogText.setVisibility(View.GONE);
                        }
                        binding.showChangelog.setText(getResources().getString(R.string.view_changelog));
                        binding.showChangelog.setOnClickListener(v -> {
                            if (binding.changelog.getVisibility() == View.GONE) {
                                binding.showChangelog.setText(getResources().getString(R.string.hide_changelog));
                                binding.changelog.setVisibility(View.VISIBLE);
                            } else {
                                binding.showChangelog.setText(getResources().getString(R.string.view_changelog));
                                binding.changelog.setVisibility(View.GONE);
                            }
                        });
                        binding.showChangelog.setVisibility(View.VISIBLE);
                    } else {
                        binding.updateTitle.setText(getResources().getString(R.string.already_up_to_date));
                    }

                    binding.currentVersion.setText(getResources().getString(R.string.current_version) + " " + BuildConfig.VERSION_NAME);
                    binding.latestVersion.setText(getResources().getString(R.string.latest_version) + " " + latestVersion.getString("versionName"));
                } catch (Exception e) {
                    failedToCheck();
                    e.printStackTrace();
                }
            } else {
                failedToCheck();
            }
            binding.checkingForUpdate.setVisibility(View.GONE);
            binding.checkedForUpdate.setVisibility(View.VISIBLE);
        }
    }
}