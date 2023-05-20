package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.LATEST_VERSION;
import static com.drdisagree.iconify.common.Preferences.UPDATE_CHECK_TIME;
import static com.drdisagree.iconify.common.Preferences.UPDATE_SCHEDULE;
import static com.drdisagree.iconify.common.Preferences.VER_CODE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;

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

    AppUpdates.CheckForUpdate checkForUpdate = null;

    @SuppressLint("SetTextI18n")
    private void failedToCheck() {
        ((TextView) findViewById(R.id.update_title)).setText(Iconify.getAppContext().getResources().getString(R.string.update_checking_failed));
        ((TextView) findViewById(R.id.current_version)).setText(Iconify.getAppContext().getResources().getString(R.string.current_version) + " " + BuildConfig.VERSION_NAME);
        ((TextView) findViewById(R.id.latest_version)).setText(Iconify.getAppContext().getResources().getString(R.string.latest_version) + " " + Iconify.getAppContext().getResources().getString(R.string.not_available));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_updates);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.app_updates);

        final Spinner check_update_every = findViewById(R.id.check_update_every);
        List<String> update_schedule = new ArrayList<>();
        update_schedule.add(getResources().getString(R.string.update_schedule1));
        update_schedule.add(getResources().getString(R.string.update_schedule2));
        update_schedule.add(getResources().getString(R.string.update_schedule3));
        update_schedule.add(getResources().getString(R.string.update_schedule4));
        update_schedule.add(getResources().getString(R.string.update_schedule5));

        ArrayAdapter<String> update_schedule_adapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, update_schedule);
        update_schedule_adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        check_update_every.setAdapter(update_schedule_adapter);

        check_update_every.setSelection(Prefs.getInt(UPDATE_SCHEDULE, 0));
        check_update_every.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                    case 4:
                        Prefs.putLong(UPDATE_CHECK_TIME, -1); // Never
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
    private class CheckForUpdate extends AsyncTask<Integer, Integer, String> {

        String jsonURL = LATEST_VERSION;

        @Override
        protected void onPreExecute() {
            findViewById(R.id.checking_for_update).setVisibility(View.VISIBLE);
            findViewById(R.id.checked_for_update).setVisibility(View.GONE);
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
            super.onPostExecute(jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject latestVersion = new JSONObject(jsonStr);

                    if (Integer.parseInt(latestVersion.getString(VER_CODE)) > BuildConfig.VERSION_CODE) {
                        findViewById(R.id.check_update).setBackgroundResource(R.drawable.container_outline);
                        ((TextView) findViewById(R.id.update_title)).setText(getResources().getString(R.string.new_update_available));
                        findViewById(R.id.download_update).setOnClickListener(v -> {
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
                        findViewById(R.id.download_update).setVisibility(View.VISIBLE);

                        String title, changes;

                        try {
                            JSONArray latestChangelog = latestVersion.getJSONArray("changelog");
                            StringBuilder release_note = new StringBuilder();

                            for (int i = 0; i < latestChangelog.length(); i++) {
                                release_note.append(latestChangelog.get(i));
                            }

                            title = release_note.substring(0, release_note.indexOf("\n\n"));
                            changes = release_note.substring(release_note.indexOf("\n##")).substring(1);

                            title = title.replace("### ", "<b>") + "</b>";
                            changes = Changelog.usernameToLink(changes.replace("## ", "<b>").replace(":\n", ":</b><br>").replace("- __", "<b>• ").replace("__\n", "</b><br>").replace("    - ", "&emsp;◦ ").replace("- ", "• ").replace("\n", "<br>"));

                            TextView changelog_title = findViewById(R.id.changelog_title);
                            TextView changelog_changes = findViewById(R.id.changelog_text);

                            changelog_title.setText(Html.fromHtml(title));
                            changelog_changes.setText(Html.fromHtml(changes));

                            SpannableString spannableString = new SpannableString(Html.fromHtml(changes));
                            URLSpan[] urls = spannableString.getSpans(0, spannableString.length(), URLSpan.class);

                            for (URLSpan urlSpan : urls) {
                                ClickableSpan clickableSpan = new ClickableSpan() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlSpan.getURL()));
                                        startActivity(intent);
                                    }
                                };

                                int start = spannableString.getSpanStart(urlSpan);
                                int end = spannableString.getSpanEnd(urlSpan);
                                spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                spannableString.removeSpan(urlSpan);
                            }

                            changelog_changes.setText(spannableString);
                            changelog_changes.setMovementMethod(LinkMovementMethod.getInstance());
                        } catch (Exception e) {
                            e.printStackTrace();

                            ((TextView) findViewById(R.id.changelog_title)).setText(Html.fromHtml(getResources().getString(R.string.individual_changelog_not_found)));
                            findViewById(R.id.changelog_text).setVisibility(View.GONE);
                        }
                        ((TextView) findViewById(R.id.show_changelog)).setText(getResources().getString(R.string.view_changelog));
                        findViewById(R.id.show_changelog).setOnClickListener(v -> {
                            if (findViewById(R.id.changelog).getVisibility() == View.GONE) {
                                ((TextView) findViewById(R.id.show_changelog)).setText(getResources().getString(R.string.hide_changelog));
                                findViewById(R.id.changelog).setVisibility(View.VISIBLE);
                            } else {
                                ((TextView) findViewById(R.id.show_changelog)).setText(getResources().getString(R.string.view_changelog));
                                findViewById(R.id.changelog).setVisibility(View.GONE);
                            }
                        });
                        findViewById(R.id.show_changelog).setVisibility(View.VISIBLE);
                    } else {
                        ((TextView) findViewById(R.id.update_title)).setText(getResources().getString(R.string.already_up_to_date));
                    }

                    ((TextView) findViewById(R.id.current_version)).setText(getResources().getString(R.string.current_version) + " " + BuildConfig.VERSION_NAME);
                    ((TextView) findViewById(R.id.latest_version)).setText(getResources().getString(R.string.latest_version) + " " + latestVersion.getString("versionName"));
                } catch (Exception e) {
                    failedToCheck();
                    e.printStackTrace();
                }
            } else {
                failedToCheck();
            }
            findViewById(R.id.checking_for_update).setVisibility(View.GONE);
            findViewById(R.id.checked_for_update).setVisibility(View.VISIBLE);
        }
    }
}
