package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.OLDER_CHANGELOGS;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.fragment.LoadingDialog;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Objects;

public class Changelog extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    private static LoadingDialog loadingDialog;
    private final ArrayList<String> changelogs = new ArrayList<>();
    private ViewGroup container;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout grabbing_changelogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changelog);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_changelog));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        grabbing_changelogs = findViewById(R.id.grabbing_changelogs);

        // List of changelog
        container = findViewById(R.id.changelog_list);

        Changelog.GrabChangelog grabChangelog = new Changelog.GrabChangelog();
        grabChangelog.execute();
    }

    // Function to add new changelog in list
    private void addChangelog(ArrayList<String> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.list_changelog, container, false);

            TextView changes = list.findViewById(R.id.changelog_text);
            changes.setText(pack.get(i));

            container.addView(list);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    private class GrabChangelog extends AsyncTask<Integer, Integer, String> {

        boolean connectionAvailable = false;

        @Override
        protected void onPreExecute() {
            ;
        }

        @Override
        protected String doInBackground(Integer... integers) {
            try {
                URL myUrl = new URL(OLDER_CHANGELOGS.replace("{VersionCode}", String.valueOf(BuildConfig.VERSION_CODE)));
                URLConnection connection = myUrl.openConnection();
                connection.setConnectTimeout(5000);
                connection.connect();
                connectionAvailable = true;
            } catch (Exception e) {
                connectionAvailable = false;
            }

            if (connectionAvailable) {
                for (int i = BuildConfig.VERSION_CODE; i >= 1; i--) {
                    String parseChangelog = OLDER_CHANGELOGS.replace("{VersionCode}", String.valueOf(i));
                    HttpURLConnection urlConnection = null;
                    BufferedReader bufferedReader = null;

                    try {
                        URL url = new URL(parseChangelog);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.connect();

                        InputStream inputStream = urlConnection.getInputStream();

                        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                        StringBuilder stringBuffer = new StringBuilder();

                        String line;

                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuffer.append(line.replace(">>", "\t\t>>")).append("\n");
                        }
                        if (stringBuffer.length() == 0) {
                            return null;
                        } else {
                            if (stringBuffer.toString().lastIndexOf("\n") == stringBuffer.toString().length() - 1)
                                changelogs.add(stringBuffer.substring(0, stringBuffer.toString().lastIndexOf("\n")));
                            else
                                changelogs.add(stringBuffer.toString());
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
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String string) {
            grabbing_changelogs.setVisibility(View.GONE);
            if (connectionAvailable) {
                if (!changelogs.isEmpty()) {
                    addChangelog(changelogs);
                } else {
                    changelogs.add(getResources().getString(R.string.changelog_not_found));
                    addChangelog(changelogs);
                }
            } else {
                changelogs.add(getResources().getString(R.string.no_internet_connection));
                addChangelog(changelogs);
            }
        }
    }
}