package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.LATEST_VERSION;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityAppUpdatesBinding;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.TaskExecutor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AppUpdates extends BaseActivity {

    private ActivityAppUpdatesBinding binding;
    private AppUpdates.CheckForUpdate checkForUpdate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppUpdatesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewHelper.setHeader(this, binding.header.toolbar, R.string.app_updates);

        List<String> update_schedule = new ArrayList<>();
        update_schedule.add(getResources().getString(R.string.update_schedule1));
        update_schedule.add(getResources().getString(R.string.update_schedule2));
        update_schedule.add(getResources().getString(R.string.update_schedule3));
        update_schedule.add(getResources().getString(R.string.update_schedule4));

        ArrayAdapter<String> update_schedule_adapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, update_schedule);
        update_schedule_adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        binding.checkUpdateEvery.setAdapter(update_schedule_adapter);

        binding.checkUpdateEvery.setSelection(Prefs.getInt(UPDATE_SCHEDULE, 0));
        binding.checkUpdateEvery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Prefs.putInt(UPDATE_SCHEDULE, position);
                // Save update checking time
                switch (position) {
                    case 0:
                        Prefs.putLong(UPDATE_CHECK_TIME, 0); // Every Time
                        break;
                    case 1:
                        Prefs.putLong(UPDATE_CHECK_TIME, (long) 1000 * 60 * 60); // Every Hour
                        break;
                    case 2:
                        Prefs.putLong(UPDATE_CHECK_TIME, (long) 1000 * 60 * 60 * 24); // Every Day
                        break;
                    case 3:
                        Prefs.putLong(UPDATE_CHECK_TIME, (long) 1000 * 60 * 60 * 24 * 7); // Every Week
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        checkForUpdate = new AppUpdates.CheckForUpdate();
        checkForUpdate.execute();
    }

    @SuppressLint("SetTextI18n")
    private void failedToCheck() {
        binding.updateTitle.setText(getApplicationContext().getResources().getString(R.string.update_checking_failed));
        binding.currentVersion.setText(getApplicationContext().getResources().getString(R.string.current_version) + " " + BuildConfig.VERSION_NAME);
        binding.latestVersion.setText(getApplicationContext().getResources().getString(R.string.latest_version) + " " + getApplicationContext().getResources().getString(R.string.not_available));
    }

    @Override
    public void onDestroy() {
        if (checkForUpdate != null) checkForUpdate.cancel(true);
        super.onDestroy();
    }

    public void onBackPressed() {
        if (checkForUpdate != null) checkForUpdate.cancel(true);
        super.onBackPressed();
    }

    @SuppressLint("StaticFieldLeak")
    private class CheckForUpdate extends TaskExecutor<Integer, Integer, String> {

        String jsonURL = LATEST_VERSION;

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
                        binding.checkUpdate.setBackgroundResource(R.drawable.container_outline);
                        binding.updateTitle.setText(getResources().getString(R.string.update_available));
                        binding.downloadUpdate.setOnClickListener(v -> {
                            try {
                                String apkUrl = latestVersion.getString("apkUrl");
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(apkUrl));
                                startActivity(i);
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
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
