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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.google.android.material.appbar.CollapsingToolbarLayout;

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
import java.util.Objects;

public class AppUpdates extends AppCompatActivity {

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
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.app_updates));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onDestroy() {
        if (checkForUpdate != null) checkForUpdate.cancel(true);
        super.onDestroy();
    }

    public void onBackPressed() {
        if (checkForUpdate != null) checkForUpdate.cancel(true);
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

                        StringBuilder title = null;
                        StringBuilder changes = null;

                        try {
                            JSONArray latestChangelog = latestVersion.getJSONArray("changelog");

                            title = new StringBuilder();
                            changes = new StringBuilder();
                            boolean firstLine = true;

                            for (int i = 0; i < latestChangelog.length(); i++) {
                                if (firstLine) {
                                    title.append(latestChangelog.getString(i));
                                    firstLine = false;
                                } else {
                                    if (latestChangelog.getString(i).contains(":"))
                                        changes.append("<b>").append(latestChangelog.getString(i)).append("</b><br>");
                                    else
                                        changes.append(latestChangelog.getString(i).replace(">>", "&emsp;•")).append("<br>");
                                }
                            }

                            if (title.length() != 0 && changes.length() != 0) {
                                if (changes.toString().indexOf("<br>") == 0)
                                    changes = new StringBuilder(changes.substring(4, changes.toString().length()));

                                if (changes.toString().lastIndexOf("<br>") == changes.toString().length() - 4)
                                    changes = new StringBuilder(changes.substring(0, changes.toString().length() - 4));

                                ((TextView) findViewById(R.id.changelog_title)).setText(Html.fromHtml(title.toString()));
                                ((TextView) findViewById(R.id.changelog_text)).setText(Html.fromHtml(changes.toString()));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();

                            if (title != null && changes != null) {
                                ((TextView) findViewById(R.id.changelog_title)).setText(Html.fromHtml(title.toString()));
                                ((TextView) findViewById(R.id.changelog_text)).setText(Html.fromHtml(changes.toString()));
                            } else {
                                ((TextView) findViewById(R.id.changelog_title)).setText(Html.fromHtml(getResources().getString(R.string.individual_changelog_not_found)));
                                findViewById(R.id.changelog_text).setVisibility(View.GONE);
                            }
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
