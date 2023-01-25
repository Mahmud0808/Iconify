package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.LATEST_VERSION;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
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

    @SuppressLint("StaticFieldLeak")
    private static LinearLayout check_update, changelog, checking_for_update, checked_for_update;
    @SuppressLint("StaticFieldLeak")
    private static TextView update_title, current_version, latest_version, changelog_text, show_changelog;
    Button download_update;

    @SuppressLint("SetTextI18n")
    private static void failedToCheck() {
        update_title.setText(Iconify.getAppContext().getResources().getString(R.string.update_checking_failed));
        current_version.setText(Iconify.getAppContext().getResources().getString(R.string.current_version) + " " + BuildConfig.VERSION_NAME);
        latest_version.setText(Iconify.getAppContext().getResources().getString(R.string.latest_version) + " " + Iconify.getAppContext().getResources().getString(R.string.not_available));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_updates);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("App Updates");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        checking_for_update = findViewById(R.id.checking_for_update);
        checked_for_update = findViewById(R.id.checked_for_update);
        check_update = findViewById(R.id.check_update);
        update_title = findViewById(R.id.update_title);
        current_version = findViewById(R.id.current_version);
        latest_version = findViewById(R.id.latest_version);
        download_update = findViewById(R.id.download_update);
        changelog = findViewById(R.id.changelog);
        changelog_text = findViewById(R.id.changelog_text);
        show_changelog = findViewById(R.id.show_changelog);

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

        check_update_every.setSelection(Prefs.getInt("UPDATE_SCHEDULE", 0));
        check_update_every.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Prefs.putInt("UPDATE_SCHEDULE", position);
                // Save update checking time
                switch (position) {
                    case 0:
                        Prefs.putLong("UPDATE_CHECK_TIME", (long) 0); // Every Time
                        break;
                    case 1:
                        Prefs.putLong("UPDATE_CHECK_TIME", (long) 1000 * 60 * 60); // Every Hour
                        break;
                    case 2:
                        Prefs.putLong("UPDATE_CHECK_TIME", (long) 1000 * 60 * 60 * 24); // Every Day
                        break;
                    case 3:
                        Prefs.putLong("UPDATE_CHECK_TIME", (long) 1000 * 60 * 60 * 24 * 7); // Every Week
                        break;
                    case 4:
                        Prefs.putLong("UPDATE_CHECK_TIME", -1); // Never
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        AppUpdates.CheckForUpdate checkForUpdate = new AppUpdates.CheckForUpdate();
        checkForUpdate.execute();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    private class CheckForUpdate extends AsyncTask<Integer, Integer, String> {

        String jsonURL = LATEST_VERSION;

        @Override
        protected void onPreExecute() {
            checking_for_update.setVisibility(View.VISIBLE);
            checked_for_update.setVisibility(View.GONE);
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

                StringBuilder stringBuffer = new StringBuilder();

                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line).append("\n");
                }
                if (stringBuffer.length() == 0) {
                    return null;
                } else {
                    return stringBuffer.toString();
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

                    if (Integer.parseInt(latestVersion.getString("versionCode")) > BuildConfig.VERSION_CODE) {
                        check_update.setBackgroundResource(R.drawable.container_outline);
                        update_title.setText(getResources().getString(R.string.new_update_available));
                        download_update.setOnClickListener(v -> {
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
                        download_update.setVisibility(View.VISIBLE);

                        try {
                            JSONArray latestChangelog = latestVersion.getJSONArray("changelog");

                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < latestChangelog.length(); i++) {
                                builder.append(latestChangelog.getString(i));
                                if (i != latestChangelog.length() - 1)
                                    builder.append("\n");
                            }
                            changelog_text.setText(builder.toString().replace(">>", "\t\t>>"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        show_changelog.setText(getResources().getString(R.string.view_changelog));
                        show_changelog.setOnClickListener(v -> {
                            if (changelog.getVisibility() == View.GONE) {
                                show_changelog.setText(getResources().getString(R.string.hide_changelog));
                                changelog.setVisibility(View.VISIBLE);
                            } else {
                                show_changelog.setText(getResources().getString(R.string.view_changelog));
                                changelog.setVisibility(View.GONE);
                            }
                        });
                        show_changelog.setVisibility(View.VISIBLE);
                    } else {
                        update_title.setText(getResources().getString(R.string.already_up_to_date));
                    }

                    current_version.setText(getResources().getString(R.string.current_version) + " " + BuildConfig.VERSION_NAME);
                    latest_version.setText(getResources().getString(R.string.latest_version) + " " + latestVersion.getString("versionName"));
                } catch (Exception e) {
                    failedToCheck();
                    e.printStackTrace();
                }
            } else {
                failedToCheck();
            }
            checking_for_update.setVisibility(View.GONE);
            checked_for_update.setVisibility(View.VISIBLE);
        }
    }

}
